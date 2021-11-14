package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.node.ErrorNode;
import adudecalledleo.aftbg.text.node.ModifierNode;
import adudecalledleo.aftbg.text.node.NodeList;
import adudecalledleo.aftbg.text.node.Span;
import adudecalledleo.aftbg.util.TriState;

import java.util.Locale;

public final class GimmickModifierNode extends ModifierNode {
    public static final char KEY = 'g';

    private final GimmickSpec spec;

    public GimmickModifierNode(int start, int length, GimmickSpec spec, Span... argSpans) {
        super(start, length, KEY, argSpans);
        this.spec = spec;
    }

    public GimmickSpec getSpec() {
        return spec;
    }

    public static final class Parser implements ModifierParser {
        private static final String ERROR_PREFIX = "Gimmick modifier: ";

        @Override
        public void parse(int start, int argsStart, String args, NodeList nodes) {
            if (args == null) {
                nodes.add(new GimmickModifierNode(start, 2, GimmickSpec.DEFAULT));
                return;
            }

            boolean reset = false;
            TriState rainbow = TriState.DEFAULT;
            GimmickSpec.Flip flip = GimmickSpec.Flip.DEFAULT;

            String[] parts = args.split(",");
            Span[] spans = new Span[parts.length];
            int ao = 0;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if ("reset".equalsIgnoreCase(part)) {
                    reset = true;
                    int l = "reset".length();
                    spans[i] = new Span(argsStart + ao, l);
                    ao += l + 1;
                    continue;
                }
                int eIndex = part.indexOf('=');
                if (eIndex < 0) {
                    int l = part.length();
                    nodes.add(new ErrorNode(argsStart + ao, l,
                            ERROR_PREFIX + "Missing = separator between key and value!"));
                    spans[i] = new Span(argsStart + ao, l);
                    ao += l + 1;
                    continue;
                }
                String key = part.substring(0, eIndex);
                String value = part.substring(eIndex + 1);
                int prefixLen = key.length() + 1;
                int valueLen = value.length();
                switch (key.toLowerCase(Locale.ROOT)) {
                    case "rainbow" -> {
                        switch (value.toLowerCase(Locale.ROOT)) {
                            case "true", "on", "yes" -> rainbow = TriState.TRUE;
                            case "false", "off", "no" -> rainbow = TriState.FALSE;
                            default -> nodes.add(new ErrorNode(argsStart + ao + prefixLen, valueLen,
                                    ERROR_PREFIX + "Is that a yes or a no?"));
                        }
                    }
                    case "flip" -> {
                        switch (value.toLowerCase(Locale.ROOT)) {
                            case "none", "false", "off", "no" -> flip = GimmickSpec.Flip.NONE;
                            case "h", "horz", "horiz", "horizontal" -> flip = GimmickSpec.Flip.HORIZONTAL;
                            case "v", "vert", "vertical" -> flip = GimmickSpec.Flip.VERTICAL;
                            case "both" -> flip = GimmickSpec.Flip.BOTH;
                            default -> nodes.add(new ErrorNode(argsStart + ao + prefixLen, valueLen,
                                    ERROR_PREFIX + "Unknown value!"));
                        }
                    }
                }
                spans[i] = new Span(argsStart + ao, prefixLen + valueLen);
                ao += prefixLen + valueLen + 1;
            }

            nodes.add(new GimmickModifierNode(start, 2 + args.length() + 2,
                    new GimmickSpec(reset, rainbow, flip),
                    spans));
        }
    }
}
