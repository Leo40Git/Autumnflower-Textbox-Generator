package adudecalledleo.aftbg.app.ui.util;

import java.util.LinkedList;
import java.util.List;

public final class ErrorMessageBuilder {
    private final String firstError;
    private List<String> errors;

    public ErrorMessageBuilder(String error) {
        this.firstError = error;
    }

    public void add(String error) {
        if (this.errors == null) {
            this.errors = new LinkedList<>();
            this.errors.add(firstError);
        }
        this.errors.add(error);
    }

    @Override
    public String toString() {
        if (this.errors == null) {
            return this.firstError;
        } else {
            var sb = new StringBuilder("<html>")
                    .append(this.errors.size()).append(" errors:<br>");
            for (String error : this.errors) {
                sb.append("&nbsp;- ").append(error).append("<br>");
            }
            return sb.append("</html>").toString();
        }
    }
}
