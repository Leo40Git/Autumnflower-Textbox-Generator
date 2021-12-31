package adudecalledleo.aftbg.app.dialog;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.AppPreferences;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.component.MainPanel;
import adudecalledleo.aftbg.app.component.render.StringListCellRenderer;
import adudecalledleo.aftbg.app.game.ExtensionDefinition;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.util.GameDefinitionUpdateListener;
import adudecalledleo.aftbg.app.worker.BrowseWorker;
import adudecalledleo.aftbg.logging.Logger;
import org.jetbrains.annotations.Nullable;

public final class AboutDialog extends ModalDialog {
    private final MainPanel mainPanel;
    private GameDefinition gameDef;
    private final ContentPane contentPane;

    public AboutDialog(MainPanel mainPanel) {
        super(mainPanel);
        this.mainPanel = mainPanel;
        this.gameDef = mainPanel.getGameDefinition();
        setIconImage(AppResources.Icons.ABOUT.getAsImage());
        setTitle("About " + BuildInfo.name());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(contentPane = new ContentPane());
        pack();

        var dim = getSize();
        setSize(Math.max(dim.width, 600), Math.max(dim.height, 480));

        mainPanel.updateListeners.add(contentPane);
    }

    @Override
    public void dispose() {
        super.dispose();
        mainPanel.updateListeners.remove(contentPane);
    }

    private final class ContentPane extends JPanel implements GameDefinitionUpdateListener {
        public ContentPane() {
            super(new BorderLayout());

            JPanel namePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(BuildInfo.name());
            label.setFont(label.getFont().deriveFont(Font.BOLD, 20));
            namePanel.add(label, BorderLayout.CENTER);
            label = new JLabel("v" + BuildInfo.version());
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 10));
            Color foreground = label.getForeground();
            label.setForeground(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 158));
            label.setVerticalAlignment(JLabel.TOP);
            label.setAlignmentY(TOP_ALIGNMENT);
            namePanel.add(label, BorderLayout.LINE_END);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Application", createAppInfo());
            tabbedPane.addTab("Game Definition", createGameDefInfo());
            tabbedPane.addTab("Extensions", createExtInfo());

            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            add(namePanel, BorderLayout.PAGE_START);
            add(tabbedPane, BorderLayout.CENTER);
        }

        private Component createAppInfo() {
            Box box = new Box(BoxLayout.PAGE_AXIS);
            box.add(new JLabel("<html><b>%s</b> (%s)</html>".formatted(BuildInfo.name(), BuildInfo.abbreviatedName())));
            box.add(new JLabel("version " + BuildInfo.version()));
            JLabel lblCredits = new JLabel("Credits:");
            lblCredits.setFont(lblCredits.getFont().deriveFont(Font.BOLD));
            box.add(lblCredits);
            for (String credit : BuildInfo.credits()) {
                box.add(new JLabel(credit));
            }

            JPanel btnPanel = new JPanel();
            int cols = 0;
            cols += addLinkButton(btnPanel, BuildInfo.homepageUrl(), "Homepage");
            cols += addLinkButton(btnPanel, BuildInfo.issuesUrl(), "Issue Tracker");
            cols += addLinkButton(btnPanel, BuildInfo.sourceUrl(), "Source Code");
            btnPanel.setLayout(new GridLayout(1, cols, 2, 0));

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(box, BorderLayout.CENTER);
            panel.add(btnPanel, BorderLayout.PAGE_END);
            return panel;
        }

        private Component createGameDefInfo() {
            Box box = new Box(BoxLayout.PAGE_AXIS);
            JLabel lblName = new JLabel(gameDef.name());
            lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
            box.add(lblName);
            for (String desc : gameDef.description()) {
                box.add(new JLabel(desc));
            }
            JLabel lblCredits = new JLabel("Credits:");
            lblCredits.setFont(lblCredits.getFont().deriveFont(Font.BOLD));
            box.add(lblCredits);
            for (String credit : gameDef.credits()) {
                box.add(new JLabel(credit));
            }
            return box;
        }

        private static final int EXT_LIST_WIDTH = 200;
        private List<ExtensionDefinition> exts;
        private JList<String> extList;
        private DefaultListModel<String> extListModel;
        private Box descBox;
        private JButton btnUnloadExt;

        private Component createExtInfo() {
            descBox = new Box(BoxLayout.PAGE_AXIS);
            descBox_noSelection(descBox);
            descBox.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

            extListModel = new DefaultListModel<>();

            extList = new JList<>();
            extList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            extList.setCellRenderer(new StringListCellRenderer(EXT_LIST_WIDTH));
            extList.setModel(extListModel);

            JButton btnLoadExt = new JButton("Load");
            btnLoadExt.addActionListener(e -> SwingUtilities.invokeLater(mainPanel::promptLoadExtension));
            btnUnloadExt = new JButton("Unload");
            btnUnloadExt.setEnabled(false);
            btnUnloadExt.addActionListener(e -> {
                if (exts.isEmpty()) {
                    return;
                }

                int i = extList.getSelectedIndex();
                if (i < 0) {
                    return;
                }

                var ext = exts.get(i);

                int result = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to unload the \"%s\" extension?".formatted(ext.name()),
                        "Unload Extension",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }

                if (gameDef.unloadExtension(ext)) {
                    AppPreferences.getLastExtensions().remove(ext.filePath());
                    SwingUtilities.invokeLater(() -> mainPanel.updateGameDefinition(gameDef));
                }
            });

            extList.addListSelectionListener(e -> extList_selectionChanged());

            JPanel btnPanel = new JPanel(new GridLayout(1, 2, 2, 0));
            btnPanel.add(btnLoadExt);
            btnPanel.add(btnUnloadExt);

            JPanel extListPanel = new JPanel(new BorderLayout());
            extListPanel.add(new JScrollPane(extList), BorderLayout.CENTER);
            extListPanel.add(btnPanel, BorderLayout.PAGE_END);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(extListPanel, BorderLayout.LINE_START);
            panel.add(descBox, BorderLayout.CENTER);

            updateGameDefinition(gameDef);

            return panel;
        }

        @Override
        public void updateGameDefinition(GameDefinition gameDef) {
            AboutDialog.this.gameDef = gameDef;
            exts = new ArrayList<>(gameDef.extensions());
            extListModel.clear();
            if (exts.isEmpty()) {
                extList_empty();
            } else {
                extList.setEnabled(true);
                for (var ext : exts) {
                    extListModel.addElement(ext.name());
                }
                extList_selectionChanged();
            }
        }

        private void extList_empty() {
            extListModel.clear();
            extListModel.addElement("(none)");
            extList.setSelectedIndex(-1);
            extList.setEnabled(false);
        }

        private void extList_selectionChanged() {
            descBox.removeAll();
            int i = extList.getSelectedIndex();
            if (exts.isEmpty() || i < 0) {
                btnUnloadExt.setEnabled(false);
                descBox_noSelection(descBox);
            } else {
                btnUnloadExt.setEnabled(true);
                var ext = exts.get(i);
                JLabel lblName = new JLabel(ext.name());
                lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
                descBox.add(lblName);
                for (String desc : ext.description()) {
                    descBox.add(new JLabel(desc));
                }
                JLabel lblCredits = new JLabel("Credits:");
                lblCredits.setFont(lblCredits.getFont().deriveFont(Font.BOLD));
                descBox.add(lblCredits);
                for (String credit : ext.credits()) {
                    descBox.add(new JLabel(credit));
                }
            }
            descBox.repaint();
        }

        private void descBox_noSelection(Box descBox) {
            descBox.add(new JLabel("Select an extension!"));
        }

        private int addLinkButton(JPanel btnPanel, @Nullable URL url, String text) {
            if (!isBrowsingSupported() || url == null) {
                return 0;
            }
            URI uri;
            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                Logger.error("Failed to convert URL \"%s\" to URI".formatted(url), e);
                return 0;
            }

            JButton btn = new JButton(text);
            btn.addActionListener(e -> new BrowseWorker(uri).execute());
            btnPanel.add(btn);
            return 1;
        }
    }

    private static Boolean browsingSupportedValue = null;

    private static boolean isBrowsingSupported() {
        if (browsingSupportedValue == null) {
            if (Desktop.isDesktopSupported()) {
                browsingSupportedValue = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
            } else {
                browsingSupportedValue = false;
            }
        }
        return browsingSupportedValue;
    }
}
