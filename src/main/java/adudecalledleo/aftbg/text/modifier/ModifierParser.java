package adudecalledleo.aftbg.text.modifier;

import adudecalledleo.aftbg.text.TextParserException;

public interface ModifierParser {
    Modifier parse(String args, int pos) throws TextParserException;
}
