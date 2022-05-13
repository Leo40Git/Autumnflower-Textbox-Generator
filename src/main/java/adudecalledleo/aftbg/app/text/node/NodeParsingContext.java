package adudecalledleo.aftbg.app.text.node;

import java.util.*;

import adudecalledleo.aftbg.app.data.DataTracker;
import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.text.util.StringScanner;

public record NodeParsingContext(DataTracker metadata, DOMParser.SpanTracker spanTracker) {
    public List<Node> parse(String contents, int offset, List<DOMParser.Error> errors) {
        if (contents.isEmpty()) {
            return List.of();
        }

        final var nodes = new LinkedList<Node>();
        final var scanner = new StringScanner(contents);
        final var sb = new StringBuilder();
        boolean escaped = false;
        char c;
        while ((c = scanner.peek()) != StringScanner.EOF) {
            if (escaped) {
                escaped = false;
                switch (c) {
                    case 'u' -> {
                        scanner.next();
                        parseUnicodeEscape(errors, spanTracker, offset, scanner, sb);
                    }
                    default -> {
                        sb.append(c);
                        scanner.next();
                    }
                }
            } else switch (c) {
                case '\\' -> {
                    escaped = true;
                    scanner.next();
                }
                case '[' -> {
                    if (!sb.isEmpty()) {
                        nodes.add(new TextNode(sb.toString()));
                        sb.setLength(0);
                    }

                    scanner.next();
                    String name = scanner.until(']')
                            .orElseGet(() -> {
                                errors.add(new DOMParser.Error(offset + scanner.tell(), scanner.remaining() + 1,
                                        "malformed opening tag"));
                                return "";
                            })
                            .trim();

                    if (name.isEmpty()) {
                        sb.append('[');
                        continue;
                    }

                    final int openEnd = offset + scanner.tell();
                    final int openStart = offset + openEnd - name.length() - 2;
                    final String openingTagContents = name;

                    boolean autoCloseTag = false;
                    if (name.endsWith("/")) {
                        name = name.substring(0, name.length() - 2);
                        autoCloseTag = true;
                    } else if (name.endsWith(" /")) {
                        name = name.substring(0, name.length() - 3);
                        autoCloseTag = true;
                    }

                    Map<String, Attribute> attrs = new LinkedHashMap<>();
                    int eqIndex = name.indexOf('=');
                    int spIndex = name.indexOf(' ');
                    if (spIndex > 0) {
                        // standard attrs
                        String attrString = name.substring(spIndex + 1);
                        name = name.substring(0, spIndex);
                        spanTracker.markNodeDeclOpening(name, openStart, openEnd);
                        attrs = parseAttributes(errors, spanTracker, offset + openStart + spIndex + 2, name, sb, attrString);
                    } else if (eqIndex >= 0) {
                        // compact [<tag>=<value>] (equal to [<tag> value="<value>"])
                        String value = name.substring(eqIndex + 1);
                        name = name.substring(0, eqIndex);
                        spanTracker.markNodeDeclOpening(name, openStart, openEnd);
                        attrs.put("value", new Attribute("value", Span.INVALID,
                                value, new Span(offset + openStart + eqIndex + 2, value.length())));
                        spanTracker.markNodeDeclAttribute(name, "value", -1, -1, value,
                                offset + openStart + eqIndex + 2, offset + openStart + eqIndex + 2 + value.length());
                    } else {
                        // no attrs
                        spanTracker.markNodeDeclOpening(name, openStart, openEnd);
                    }

                    var handler = NodeRegistry.getHandler(name);
                    if (handler == null) {
                        int nameStart = openStart + 1;
                        int nameLength = 0;
                        if (spIndex > 0) {
                            nameLength = spIndex;
                        } else if (eqIndex > 0) {
                            nameLength = eqIndex;
                        } else {
                            nameLength = name.length();
                        }
                        errors.add(new DOMParser.Error(nameStart, nameLength,
                                "unknown tag \"" + name + "\""));
                    }
                    final int contentStart = scanner.tell();
                    final String nameF = name;
                    String myContents;
                    if (autoCloseTag) {
                        myContents = "";
                    } else {
                        myContents = scanner.until("[/%s]".formatted(name))
                                .orElseGet(() -> {
                                    errors.add(new DOMParser.Error(openStart, openEnd - openStart + scanner.remaining() + 1,
                                            "missing closing tag for \"" + nameF + "\""));
                                    return null;
                                });
                    }

                    boolean success = false;
                    if (handler != null && myContents != null) {
                        Span openingSpan = new Span(openStart, openEnd - openStart);
                        Span closingSpan;
                        if (autoCloseTag) {
                            closingSpan = openingSpan;
                        } else {
                            closingSpan = new Span(offset + scanner.tell() - name.length() - 3, name.length() + 3);
                        }
                        var node = handler.parse(this, offset + contentStart, errors,
                                openingSpan, closingSpan,
                                attrs, myContents);
                        if (node != null) {
                            success = true;
                            nodes.add(node);
                        }
                    }
                    if (!success) {
                        sb.append('[').append(openingTagContents).append(']');
                        if (!autoCloseTag && myContents != null)
                            sb.append(myContents).append("[/").append(name).append(']');
                    }

                    if (myContents != null)
                        spanTracker.markNodeDeclClosing(name, offset + scanner.tell() - name.length() - 3, offset + scanner.tell());
                }
                default -> {
                    sb.append(c);
                    scanner.next();
                }
            }
        }

        if (!sb.isEmpty()) {
            nodes.add(new TextNode(sb.toString()));
        }

        return nodes;
    }

    private static Map<String, Attribute> parseAttributes(List<DOMParser.Error> errors,
                                                          DOMParser.SpanTracker spanTracker, int offset,
                                                          String node, StringBuilder sb, String attrString) {
        sb.setLength(0);

        // step 1 - split string into substrings using spaces as a delimiter while respecting quotes
        record Entry(int start, String contents) { }

        List<Entry> entries = new ArrayList<>();
        final var scanner = new StringScanner(attrString);
        boolean escaped = false;
        int start = 0;
        char quoteChar = 0;
        int quoteCharPos = 0;
        char c;
        while ((c = scanner.peek()) != StringScanner.EOF) {
            if (escaped) {
                escaped = false;
                sb.append(c);
                scanner.next();
                spanTracker.markEscaped(offset + scanner.tell() - 2, offset + scanner.tell());
            } else {
                switch (c) {
                    case '\\' -> {
                        escaped = true;
                        sb.append(c);
                        scanner.next();
                    }
                    case '"', '\'' -> {
                        if (quoteChar == 0) {
                            quoteChar = c;
                            quoteCharPos = scanner.tell();
                        } else if (quoteChar == c) {
                            quoteChar = 0;
                            quoteCharPos = scanner.tell();
                        } else {
                            sb.append(c);
                        }
                        scanner.next();
                    }
                    case ' ' -> {
                        if (quoteChar == 0) {
                            if (!sb.isEmpty()) {
                                entries.add(new Entry(start, sb.toString()));
                                sb.setLength(0);
                                start = scanner.tell() + 1;
                            }
                        } else {
                            sb.append(c);
                        }
                        scanner.next();
                    }
                    default -> {
                        sb.append(c);
                        scanner.next();
                    }
                }
            }
        }

        if (escaped) {
            errors.add(new DOMParser.Error(offset + scanner.end() - 1, offset + scanner.end(),
                    "escaping end of text?"));
        }

        if (quoteChar != 0) {
            errors.add(new DOMParser.Error(offset + quoteCharPos, offset + quoteCharPos + scanner.remaining(),
                    "unclosed quote " + quoteChar));
        }

        if (!sb.isEmpty()) {
            entries.add(new Entry(start, sb.toString()));
        }

        // step 2 - parse substrings into key-value pairs
        Map<String, Attribute> attrs = new LinkedHashMap<>();
        String key, value;
        for (var entry : entries) {
            String contents = entry.contents();
            int eqIndex = contents.indexOf('=');
            if (eqIndex < 0) {
                key = toAttributeKey(contents);
                attrs.put(key, new Attribute(key, new Span(offset + entry.start(), key.length()), "", Span.INVALID));
                spanTracker.markNodeDeclAttribute(node, key, offset + entry.start(), offset + entry.start() + key.length(), "",
                        -1, -1);
                continue;
            }
            try {
                key = toAttributeKey(contents.substring(0, eqIndex));
            } catch (IllegalArgumentException ignored) {
                errors.add(new DOMParser.Error(offset + entry.start(), 1,
                        "got '=' without key"));
                continue;
            }
            value = contents.substring(eqIndex + 1).trim();
            if ((value.startsWith("\"") && value.endsWith("\""))
             || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }

            String parsedValue = parseEscapes(errors, spanTracker, offset + entry.start() + eqIndex + 1, value, sb);
            attrs.put(key, new Attribute(key, new Span(offset + entry.start(), key.length()),
                    parsedValue, new Span(offset + entry.start() + eqIndex + 1, value.length())));
            spanTracker.markNodeDeclAttribute(node, key, offset + entry.start(), offset + entry.start() + key.length(), parsedValue,
                    offset + entry.start() + eqIndex + 1, offset + entry.start() + eqIndex + 1 + value.length());
        }

        sb.setLength(0);
        return attrs;
    }

    private static String toAttributeKey(String str) {
        String currentKey = str.trim().toLowerCase(Locale.ROOT);
        if (currentKey.isEmpty()) {
            throw new IllegalArgumentException("empty attribute key");
        }
        return currentKey;
    }

    public static String parseEscapes(List<DOMParser.Error> errors,
                                      DOMParser.SpanTracker spanTracker, int offset,
                                      String string, StringBuilder sb) {
        sb.setLength(0);
        var scanner = new StringScanner(string);
        boolean escaped = false;
        char c;
        while ((c = scanner.peek()) != StringScanner.EOF) {
            if (escaped) {
                escaped = false;
                switch (c) {
                    case 'u' -> {
                        scanner.next();
                        parseUnicodeEscape(errors, spanTracker, offset, scanner, sb);
                    }
                    default -> {
                        sb.append(c);
                        scanner.next();
                    }
                }
            } else if (c == '\\') {
                escaped = true;
                scanner.next();
            } else {
                sb.append(c);
                scanner.next();
            }
        }

        if (escaped) {
            errors.add(new DOMParser.Error(offset + scanner.end() - 1, offset + scanner.end(),
                    "escaping end of text?"));
        }

        return sb.toString();
    }

    private static void parseUnicodeEscape(List<DOMParser.Error> errors,
                                           DOMParser.SpanTracker spanTracker, int offset,
                                           StringScanner scanner, StringBuilder sb) {
        String charIdStr = scanner.read(4).orElseGet(() -> {
            errors.add(new DOMParser.Error(offset + scanner.tell() - 2, 2,
                    "unicode escape has <4 digits"));
            sb.append("\\u");
            return "";
        });
        if (charIdStr.isEmpty()) {
            return;
        }

        int charId;
        try {
            charId = Integer.parseUnsignedInt(charIdStr, 16);
        } catch (NumberFormatException ignored) {
            errors.add(new DOMParser.Error(offset + scanner.tell() - 6, 6,
                    "unicode escape is not valid hex number"));
            sb.append("\\u").append(charIdStr);
            return;
        }
        sb.append((char) charId);
        spanTracker.markEscaped(offset + scanner.tell() - 6, offset + scanner.tell());
    }
}
