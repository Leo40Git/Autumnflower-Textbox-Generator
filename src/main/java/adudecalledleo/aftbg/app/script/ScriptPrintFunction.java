package adudecalledleo.aftbg.app.script;

import org.graalvm.polyglot.HostAccess;

@FunctionalInterface
public interface ScriptPrintFunction {
    @HostAccess.Export
    void print(String msg);
}
