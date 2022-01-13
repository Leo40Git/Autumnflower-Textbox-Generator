package adudecalledleo.aftbg.app.ui;

import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.game.GameDefinition;
import adudecalledleo.aftbg.app.ui.util.DialogUtils;

public final class AppFrame extends JFrame {
    public AppFrame(GameDefinition gameDef) {
        var panel = new MainPanel(this);
        panel.updateGameDefinition(gameDef);

        setTitle(BuildInfo.name());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (panel.isProjectEmpty()) {
                    System.exit(0);
                } else {
                    switch (JOptionPane.showConfirmDialog(panel,
                            "Do you want to save your project before exiting?", "Exit",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                        case JOptionPane.YES_OPTION:
                            try {
                                panel.saveProject(false);
                            } catch (IOException | IllegalStateException e) {
                                JOptionPane.showMessageDialog(AppFrame.this,
                                        "Failed to write project!\n" + e + "\n" +
                                                DialogUtils.logFileInstruction() + "\n" +
                                                "To prevent your work from being lost, the current operation has been cancelled.",
                                        "Exit", JOptionPane.ERROR_MESSAGE);
                                break;
                            }
                        case JOptionPane.NO_OPTION:
                            System.exit(0);
                        default:
                        case JOptionPane.CANCEL_OPTION:
                            break;
                    }
                }
            }
        });
        setContentPane(panel);
        setJMenuBar(panel.getMenuBar());
        pack();
    }
}
