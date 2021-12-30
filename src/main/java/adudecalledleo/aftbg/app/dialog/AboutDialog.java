package adudecalledleo.aftbg.app.dialog;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.AppResources;
import adudecalledleo.aftbg.app.game.ExtensionDefinition;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.worker.BrowseWorker;
import adudecalledleo.aftbg.logging.Logger;
import org.jetbrains.annotations.Nullable;

public final class AboutDialog extends ModalDialog {
    private final GameDefinition gameDef;

    public AboutDialog(Component owner, GameDefinition gameDef) {
        super(owner);
        this.gameDef = gameDef;
        setIconImage(AppResources.Icons.ABOUT.getAsImage());
        setTitle("About " + BuildInfo.name());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(new ContentPane());
        pack();

        var dim = getSize();
        setSize(Math.max(dim.width, 500), Math.max(dim.height, 450));
    }

    private final class ContentPane extends JPanel {
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

        private Component createExtInfo() {
            if (gameDef.extensions().isEmpty()) {
                JLabel lbl = new JLabel("No extensions loaded!");
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setVerticalAlignment(JLabel.CENTER);
                return lbl;
            }

            List<ExtensionDefinition> exts = new ArrayList<>(gameDef.extensions());

            Box descBox = new Box(BoxLayout.PAGE_AXIS);
            descBox.add(new JLabel("Select an extension!"));
            descBox.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

            DefaultListModel<String> extListModel = new DefaultListModel<>();
            for (var ext : exts) {
                extListModel.addElement(ext.name());
            }

            JList<String> extList = new JList<>();
            extList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            extList.setModel(extListModel);
            extList.addListSelectionListener(e -> {
                var ext = exts.get(e.getFirstIndex());
                descBox.removeAll();
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
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(extList), BorderLayout.LINE_START);
            panel.add(descBox, BorderLayout.CENTER);
            return panel;
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
