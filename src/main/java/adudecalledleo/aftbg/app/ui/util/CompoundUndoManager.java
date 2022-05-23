package adudecalledleo.aftbg.app.ui.util;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import adudecalledleo.aftbg.Main;

/*
 * This class will merge individual edits into a single larger edit.
 * That is, characters entered sequentially will be grouped together and
 * undone as a group. Any attribute changes will be considered as part
 * of the group and will therefore be undone when the group is undone.
 */
public final class CompoundUndoManager extends UndoManager implements PropertyChangeListener, DocumentListener {
    private final JTextComponent editor;
    private final boolean excludeChangeEvents;
    private final UndoAction undoAction;
    private final RedoAction redoAction;

    private CompoundEdit compoundEdit;
    // These fields are used to help determine whether the edit is an
    // incremental edit. The offset and length should increase by 1 for
    // each character added or decrease by 1 for each character removed.
    private int lastOffset;
    private int lastLength;

    public CompoundUndoManager(JTextComponent editor, boolean excludeChangeEvents) {
        this.editor = editor;
        this.excludeChangeEvents = excludeChangeEvents;
        undoAction = new UndoAction();
        redoAction = new RedoAction();
        editor.addPropertyChangeListener("document", this);
        editor.getDocument().addUndoableEditListener(this);
    }

    /*
     * Moves this CompoundUndoManager to the editor's new document, should it get replaced.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == editor && "document".equals(evt.getPropertyName())) {
            if (evt.getOldValue() instanceof AbstractDocument document) {
                document.removeUndoableEditListener(this);
                document.removeDocumentListener(this);
            }
            discardAllEdits();
            compoundEdit = null;
            undoAction.updateState();
            redoAction.updateState();
            editor.getDocument().addDocumentListener(this);
        }
    }

    /*
     * Add a DocumentLister before the undo is done, so we can position
     * the caret correctly as each edit is undone.
     */
    @Override
    public void undo() {
        editor.getDocument().addDocumentListener(this);
        super.undo();
        editor.getDocument().removeDocumentListener(this);
    }

    /*
     * Add a DocumentLister before the redo is done, so we can position
     * the caret correctly as each edit is redone.
     */
    @Override
    public void redo() {
        editor.getDocument().addDocumentListener(this);
        super.redo();
        editor.getDocument().removeDocumentListener(this);
    }

    /*
     * Whenever an UndoableEdit happens the edit will either be absorbed
     * by the current compound edit or a new compound edit will be started.
     */
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        AbstractDocument.DefaultDocumentEvent event =
                (AbstractDocument.DefaultDocumentEvent) e.getEdit();
        if (excludeChangeEvents && event.getType() == DocumentEvent.EventType.CHANGE) {
            return;
        }

        // Start a new compound edit
        if (compoundEdit == null) {
            compoundEdit = startCompoundEdit(e.getEdit());
            return;
        }

        int offsetChange = editor.getCaretPosition() - lastOffset;
        int lengthChange = editor.getDocument().getLength() - lastLength;

        // Check for an attribute change
        if (event.getType() == DocumentEvent.EventType.CHANGE) {
            if (offsetChange == 0) {
                compoundEdit.addEdit(e.getEdit());
                return;
            }
        }

        // Check for an incremental edit or backspace.
        // The change in caret position and document length should both be
        //  either 1 or -1.
        if (offsetChange == lengthChange && Math.abs(offsetChange) == 1) {
            compoundEdit.addEdit(e.getEdit());
            lastOffset = editor.getCaretPosition();
            lastLength = editor.getDocument().getLength();
            return;
        }

        // Not incremental edit, end previous edit and start a new one
        compoundEdit.end();
        compoundEdit = startCompoundEdit(e.getEdit());
    }

    /*
     * Each CompoundEdit will store a group of related incremental edits
     * (i.e. each character typed or backspaced is an incremental edit)
     */
    private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
        // Track caret and document information of this compound edit
        lastOffset = editor.getCaretPosition();
        lastLength = editor.getDocument().getLength();

        // The compound edit is used to store incremental edits
        compoundEdit = new MyCompoundEdit();
        compoundEdit.addEdit(anEdit);

        // The compound edit is added to the UndoManager. All incremental
        //  edits stored in the compound edit will be undone/redone at once
        addEdit(compoundEdit);

        undoAction.updateState();
        redoAction.updateState();

        return compoundEdit;
    }

    /*
     * The action to undo changes to the document.
     * The state of the action is managed by the CompoundUndoManager.
     */
    public Action getUndoAction() {
        return undoAction;
    }

    /*
     * The action to redo changes to the document.
     * The state of the action is managed by the CompoundUndoManager.
     */
    public Action getRedoAction() {
        return redoAction;
    }

    //region Implement DocumentListener
    /*
     * Updates to the document as a result of undo/redo will cause the
     * caret to be repositioned.
     */
    public void insertUpdate(final DocumentEvent e) {
        SwingUtilities.invokeLater(() -> {
            int offset = e.getOffset() + e.getLength();
            offset = Math.min(offset, editor.getDocument().getLength());
            editor.setCaretPosition(offset);
        });
    }

    public void removeUpdate(DocumentEvent e) {
        editor.setCaretPosition(e.getOffset());
    }

    public void changedUpdate(DocumentEvent e) { }
    //endregion

    class MyCompoundEdit extends CompoundEdit {
        public boolean isInProgress() {
            // in order for the canUndo() and canRedo() methods to work,
            //  assume that the compound edit is never in progress
            return false;
        }

        public void undo() throws CannotUndoException {
            // End the edit so future edits don't get absorbed by this edit
            if (compoundEdit != null) {
                compoundEdit.end();
            }

            super.undo();

            // Always start a new compound edit after an undo
            compoundEdit = null;
        }
    }

    /*
     * Perform the Undo and update the state of the undo/redo Actions
     */
    private final class UndoAction extends AbstractAction {
        public UndoAction() {
            putValue(Action.NAME, "Undo");
            putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                CompoundUndoManager.this.undo();
                editor.requestFocusInWindow();
            } catch (CannotUndoException ex) {
                Main.logger().error("Failed to undo!", ex);
                UIManager.getLookAndFeel().provideErrorFeedback(editor);
            }

            updateState();
            redoAction.updateState();
        }

        private void updateState() {
            setEnabled(CompoundUndoManager.this.canUndo());
        }
    }

    /*
     * Perform the Redo and update the state of the undo/redo Actions
     */
    private final class RedoAction extends AbstractAction {
        public RedoAction() {
            putValue(Action.NAME, "Redo");
            putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                CompoundUndoManager.this.redo();
                editor.requestFocusInWindow();
            } catch (CannotRedoException ex) {
                Main.logger().error("Failed to redo!", ex);
                UIManager.getLookAndFeel().provideErrorFeedback(editor);
            }

            updateState();
            undoAction.updateState();
        }

        private void updateState() {
            setEnabled(CompoundUndoManager.this.canRedo());
        }
    }
}
