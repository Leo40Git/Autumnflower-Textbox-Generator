package adudecalledleo.aftbg.app;

import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.*;

import adudecalledleo.aftbg.BuildInfo;
import adudecalledleo.aftbg.app.component.MainPanel;
import adudecalledleo.aftbg.face.FacePool;
import adudecalledleo.aftbg.game.GameDefinition;
import adudecalledleo.aftbg.window.WindowContext;

public final class AppFrame extends JFrame {
    public AppFrame(Path basePath, GameDefinition gameDef, WindowContext winCtx, FacePool faces) {
        var panel = new MainPanel();
        panel.updateGameDefinition(basePath, gameDef, faces);
        panel.updateWindowContext(winCtx);

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
