package adudecalledleo.aftbg.app.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import adudecalledleo.aftbg.app.text.DOMParser;
import adudecalledleo.aftbg.app.util.Pair;

public final class ErrorReportDialog extends JDialog {
    public ErrorReportDialog(Component owner, List<Pair<Integer, DOMParser.Error>> errors) {
        super(ModalDialog.getWindowAncestor(owner), ModalityType.MODELESS);
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
            TableColumn[] tblErrorsCols = createColumns("B#", "Description", "S", "E");
            for (var col : tblErrorsCols) {
                tblErrors.addColumn(col);
            }
            tblErrors.getTableHeader().setReorderingAllowed(false);
            tblErrors.setAutoCreateColumnsFromModel(false);
            tblErrors.setModel(mdlErrors);

            tblErrors.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            lockColumnSize(tblErrors, tblErrorsCols[0]);
            lockColumnSize(tblErrors, tblErrorsCols[3]);
            tblErrorsCols[2].setMinWidth(tblErrorsCols[3].getMinWidth());
            tblErrorsCols[2].setMaxWidth(tblErrorsCols[3].getMaxWidth());

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

        private TableColumn[] createColumns(String... titles) {
            var cols = new TableColumn[titles.length];
            for (int i = 0; i < cols.length; i++) {
                cols[i] = new TableColumn(i);
                cols[i].setHeaderValue(titles[i]);
            }
            return cols;
        }

        private void lockColumnSize(JTable table, TableColumn col) {
            final var model = table.getModel();
            final int colInd = col.getModelIndex();
            var headerRenderer = col.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            int width = headerRenderer
                    .getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, colInd)
                    .getPreferredSize().width;
            var renderer = col.getCellRenderer();
            if (renderer == null) {
                renderer = table.getDefaultRenderer(model.getColumnClass(colInd));
            }
            for (int i = 0; i < model.getRowCount(); i++) {
                width = Math.max(width,
                        renderer.getTableCellRendererComponent(table, model.getValueAt(i, colInd), false, false, i, colInd)
                                .getPreferredSize().width);
            }
            col.setMinWidth(width);
            col.setMaxWidth(width);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ErrorReportDialog.this.setVisible(false);
            ErrorReportDialog.this.dispose();
        }
    }
}
