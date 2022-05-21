package adudecalledleo.aftbg.app.text;

import java.util.LinkedList;
import java.util.List;

import adudecalledleo.aftbg.app.text.node.Document;
import adudecalledleo.aftbg.app.text.node.NodeParsingContext;

public final class DOMParser {
    private DOMParser() { }

    public static Result parse(String contents, DOMParserData metadata, SpanTracker spanTracker) {
        if (contents.isEmpty()) {
            return new Result(new Document(), List.of());
        }
        var ctx = new NodeParsingContext(metadata, spanTracker);
        var errors = new LinkedList<DOMParser.Error>();
        var result = ctx.parse(DOMInputSanitizer.apply(contents), 0, errors);
        return new Result(new Document(result), errors);
    }

    public static Result parse(String contents, DOMParserData metadata) {
        return parse(contents, metadata, SpanTracker.NO_OP);
    }

    public record Error(int start, int length, String message) {
        public int end() {
            return start + length;
        }
    }

    public record Result(Document document, List<Error> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    public interface SpanTracker {
        SpanTracker NO_OP = new SpanTracker() { };

        default void markEscaped(int start, int end) { }
        default void markNodeDecl(String node, int start, int end) { }
        default void markNodeDeclOpening(String node, int start, int end) {
            markNodeDecl(node, start, end);
        }
        default void markNodeDeclClosing(String node, int start, int end) {
            markNodeDecl(node, start, end);
        }
        default void markNodeDeclLeaf(String node, int start, int end) {
            markNodeDecl(node, start, end);
        }
        default void markNodeDeclAttribute(String node, String key, int keyStart, int keyEnd, String value, int valueStart, int valueEnd) { }
    }
}
