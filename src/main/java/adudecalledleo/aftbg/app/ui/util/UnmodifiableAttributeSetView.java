package adudecalledleo.aftbg.app.ui.util;

import java.util.Enumeration;

import javax.swing.text.*;

public final class UnmodifiableAttributeSetView implements MutableAttributeSet {
    private final AttributeSet delegate;
    private UnmodifiableAttributeSetView resolveParentView;

    public UnmodifiableAttributeSetView(AttributeSet delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getAttributeCount() {
        return delegate.getAttributeCount();
    }

    @Override
    public boolean isDefined(Object attrName) {
        return delegate.isDefined(attrName);
    }

    @Override
    public boolean isEqual(AttributeSet attr) {
        return delegate.isEqual(attr);
    }

    @Override
    public AttributeSet copyAttributes() {
        return delegate.copyAttributes();
    }

    @Override
    public Object getAttribute(Object key) {
        return delegate.getAttribute(key);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return delegate.getAttributeNames();
    }

    @Override
    public boolean containsAttribute(Object name, Object value) {
        return delegate.containsAttribute(name, value);
    }

    @Override
    public boolean containsAttributes(AttributeSet attributes) {
        return delegate.containsAttributes(attributes);
    }

    @Override
    public AttributeSet getResolveParent() {
        // don't allow resolve parent to be modified, either
        var resolveParent = delegate.getResolveParent();
        if (resolveParent == null) {
            return null;
        }
        if (resolveParentView == null || resolveParentView.delegate != resolveParent) {
            resolveParentView = new UnmodifiableAttributeSetView(resolveParent);
        }
        return resolveParentView;
    }

    // region MutableAttributeSet methods (no-ops)
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

    @Override
    public void setResolveParent(AttributeSet parent) { }
    // endregion
}
