package adudecalledleo.aftbg.app.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.util.Enumeration;

public final class ImmutableAttributeSet extends SimpleAttributeSet {
    public ImmutableAttributeSet(AttributeSet source) {
        // copied from addAttributes
        Enumeration<?> names = source.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            super.addAttribute(name, source.getAttribute(name));
        }
    }

    @Override
    public void addAttribute(Object name, Object value) { }

    @Override
    public void addAttributes(AttributeSet attributes) { }

    @Override
    public void removeAttribute(Object name) { }

    @Override
    public void removeAttributes(Enumeration<?> names) { }

    @Override
    public void removeAttributes(AttributeSet attributes) { }
}
