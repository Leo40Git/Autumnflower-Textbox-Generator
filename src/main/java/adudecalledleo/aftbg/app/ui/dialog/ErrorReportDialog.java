package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.util.Pair;

public final class ErrorReportDialog extends ModalDialog {
    public ErrorReportDialog(Component owner, List<Pair<Integer, DOMParser.Error>> errors) {
        super(owner);
        setTitle("Textbox error(s)");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        ContentPane contentPane;
        setContentPane(contentPane = new ContentPane(errors));
        getRootPane().setDefaultButton(contentPane.btnOK);
        pack();
    }

    private final class ContentPane extends JPanel implements ActionListener {
        public final JButton btnOK;

        public ContentPane(List<Pair<Integer, DOMParser.Error>> errors) {
            super(new BorderLayout());

            var mdlErrors = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            mdlErrors.addColumn("boxNum");
            mdlErrors.addColumn("desc");
            mdlErrors.addColumn("from");
            mdlErrors.addColumn("to");
            for (var error : errors) {
                var e = error.right();
                mdlErrors.addRow(new Object[] { (error.left() + 1), e.message(), (e.start() + 1), (e.end() + 1) });
            }

            JTable tblErrors = new JTable();
            tblErrors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            // TODO make "Description" the largest column
            // TODO minimum sizes
            tblErrors.addColumn(createColumn(0, "Box #"));
            tblErrors.addColumn(createColumn(1, "Description"));
            tblErrors.addColumn(createColumn(2, "From"));
            tblErrors.addColumn(createColumn(3, "To"));
            tblErrors.setAutoCreateColumnsFromModel(false);
            tblErrors.setModel(mdlErrors);

            btnOK = new JButton("OK");
            btnOK.addActionListener(this);

            JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
            bottomPanel.add(new JLabel("Fix these errors and try generating again."));
            bottomPanel.add(btnOK);

            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            add(new JLabel("The following error(s) were detected:"), BorderLayout.PAGE_START);
            add(new JScrollPane(tblErrors), BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.PAGE_END);
        }

        private TableColumn createColumn(int index, String title) {
            var col = new TableColumn(index);
            col.setHeaderValue(title);
            return col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ErrorReportDialog.this.setVisible(false);
            ErrorReportDialog.this.dispose();
        }
    }
}
