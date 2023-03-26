package components;

import controller.GameController;
import minesweeper.MainFrame;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static minesweeper.MainFrame.OnePlayer;
import static minesweeper.MainFrame.useAI;

public abstract class BasicComponent extends JComponent {
    public BasicComponent() {
        initial();
    }

    private void initial() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) {
                    if (!OnePlayer || !GameController.hasWin) {
                        if (e.getButton() == 1) {
                            onMouseLeftClicked();
                        }
                        if (e.getButton() == 3) {
                            onMouseRightClicked();
                        }
                        if (e.getButton() == 2) {
                            onMouseMiddleClicked();
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                onMouseEntered();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onMouseExited();
            }
        });
    }

    /**
     * invoke this method when mouse left clicked
     */
    public abstract void onMouseLeftClicked();

    /**
     * invoke this method when mouse right clicked
     */
    public abstract void onMouseRightClicked();

    public abstract void onMouseMiddleClicked();

    public abstract void onMouseExited();

    public abstract void onMouseEntered();
}
