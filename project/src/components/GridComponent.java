package components;

import Sound.Audio;
import entity.GridStatus;
import minesweeper.GamePanel;
import minesweeper.MainFrame;
import minesweeper.MainFrame;

import javax.swing.*;
import java.awt.*;

import static controller.GameController.hasWin;
import static minesweeper.GamePanel.*;
import static minesweeper.MainFrame.*;

public class GridComponent extends BasicComponent {
    public static int gridSize = 30;


    private int row;
    private int col;
    private GridStatus status = GridStatus.Covered;
    private int content = 0;
    private final static Image image0;
    private final static Image image1;
    private final static Image image2;
    private final static Image image3;
    private final static Image image4;
    private final static Image image5;
    private final static Image image6;
    private final static Image image7;
    private final static Image image8;
    private final static Image imageCover;
    private final static Image imageFlag;
    private final static Image imageMine;
    private final static Image image00;
    private final static Image image01;
    private final static Image image02;
    private final static Image image03;
    private final static Image image04;
    private final static Image image05;
    private final static Image image06;
    private final static Image image07;
    private final static Image image08;
    private final static Image image0Cover;
    private final static Image image0Flag;
    private final static Image image0Mine;

    public GridComponent(int x, int y) {
        this.setSize(gridSize, gridSize);
        this.row = x;
        this.col = y;
    }

    public GridComponent(int x, int y, int content, GridStatus status) {            //暂时用于透视雷
        this.setSize(gridSize, gridSize);
        this.row = x;
        this.col = y;
        this.content = content;
        this.status = status;
    }

    static {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        image0 = toolkit.getImage(path + "/pictures/0.png");
        image1 = toolkit.getImage(path + "/pictures/1.png");
        image2 = toolkit.getImage(path + "/pictures/2.png");
        image3 = toolkit.getImage(path + "/pictures/3.png");
        image4 = toolkit.getImage(path + "/pictures/4.png");
        image5 = toolkit.getImage(path + "/pictures/5.png");
        image6 = toolkit.getImage(path + "/pictures/6.png");
        image7 = toolkit.getImage(path + "/pictures/7.png");
        image8 = toolkit.getImage(path + "/pictures/8.png");
        imageCover = toolkit.getImage(path + "/pictures/cover.png");
        imageFlag = toolkit.getImage(path + "/pictures/flag.png");
        imageMine = toolkit.getImage(path + "/pictures/mine.png");
        image00 = toolkit.getImage(path + "/pictures/00.png");
        image01 = toolkit.getImage(path + "/pictures/01.png");
        image02 = toolkit.getImage(path + "/pictures/02.png");
        image03 = toolkit.getImage(path + "/pictures/03.png");
        image04 = toolkit.getImage(path + "/pictures/04.png");
        image05 = toolkit.getImage(path + "/pictures/05.png");
        image06 = toolkit.getImage(path + "/pictures/06.png");
        image07 = toolkit.getImage(path + "/pictures/07.png");
        image08 = toolkit.getImage(path + "/pictures/08.png");
        image0Cover = toolkit.getImage(path + "/pictures/0cover.png");
        image0Flag = toolkit.getImage(path + "/pictures/0flag.png");
        image0Mine = toolkit.getImage(path + "/pictures/0mine.png");
    }

    @Override
    public void onMouseEntered() {
        if (this.status == GridStatus.Covered)
            this.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }

    @Override
    public void onMouseExited() {
        this.setBorder(null);
    }

    @Override
    public void onMouseLeftClicked() {
        //System.out.printf("Gird (%d,%d) is left-clicked.\n", row, col);
        content = grids.get(row * Y + col).content;
        while (GamePanel.turnNum == 0 && content == -1 && !Ban_First_Mine_Check) {
            MainFrame.controller.getGamePanel().initialGame2(X, Y, MineCount);
            content = grids.get(row * Y + col).content;
            if (content != -1) {
                break;                 //代替原turnNum+1
            }
        }
        if (this.status == GridStatus.Covered) {  //change
            if (content == -1) {
                if (sound != 2) {
                    Audio boom = new Audio(path + "/sounds/boom.wav");         //new,播放声音
                    boom.play();
                }
                MainFrame.controller.getOnTurnPlayer().costScore();
                MineCount--;//new
                MainFrame.mineDecrease();
                if (MineCount == 0 && !OnePlayer)
                    controller.GameOver();
            }
            this.status = GridStatus.Clicked;
            if (content == 0) {
                uncover();
            }
            repaint();
            GamePanel.turnNum += 1;           //5.13.13:41改动位置
            stepDecrease();          //new
            this.setBorder(null);
            if ((turnNum) % Click_times == 0 && !hasWin) {
                MainFrame.controller.nextTurn();
            }
        }
        MainFrame.controller.getScoreBoard().update();
        if (OnePlayer) {
            if (status == GridStatus.Clicked && content == -1 && !hasWin) {
                controller.GameOver();
            } else {
                int a = 0;
                for (GridComponent grid : grids) {
                    if (grid.status == GridStatus.Clicked && grid.getContent() != -1)
                        a += 1;
                }
                if (a == X * Y - mine_count)
                    controller.GameOver();
            }
        }
        //TODO: 在左键点击一个格子的时候，还需要做什么？
    }

    @Override
    public void onMouseRightClicked() {
        //System.out.printf("Gird (%d,%d) is right-clicked.\n", row, col);
        if (!OnePlayer) {
            if (this.status == GridStatus.Covered) {
                if (this.content == -1) {
                    if (sound != 2) {
                        Audio flag_right = new Audio(path + "/sounds/flag_right.wav");         //new,播放声音
                        flag_right.play();
                    }
                    MainFrame.controller.getOnTurnPlayer().addScore();
                    this.status = GridStatus.Flag;
                    MineCount--;//new
                    MainFrame.mineDecrease();
                    if (MineCount == 0 && !OnePlayer)
                        controller.GameOver();
                } else if (this.content != 0) {
                    if (sound != 2) {
                        Audio flag_wrong = new Audio(path + "/sounds/flag_wrong.wav");         //new,播放声音
                        flag_wrong.play();
                    }
                    MainFrame.controller.getOnTurnPlayer().addMistake();
                    this.status = GridStatus.Clicked;
                } else {
                    if (sound != 2) {
                        Audio flag_wrong = new Audio(path + "/sounds/flag_wrong.wav");         //new,播放声音
                        flag_wrong.play();
                    }
                    MainFrame.controller.getOnTurnPlayer().addMistake();
                    uncover();
                }
                repaint();
                GamePanel.turnNum += 1;
                stepDecrease();          //new
                this.setBorder(null);
                if ((turnNum) % Click_times == 0 && !hasWin) {
                    MainFrame.controller.nextTurn();
                }
            }
            MainFrame.controller.getScoreBoard().update();
        } else {
            if (this.status == GridStatus.Covered) {
                if (this.content == -1) {
                    if (sound != 2) {
                        //Audio flag_right = new Audio(path + "/sounds/flag_right.wav");         //new,播放声音
                        //flag_right.play();
                    }
                    MainFrame.controller.getOnTurnPlayer().addScore();
                    this.status = GridStatus.Flag;
                    MineCount--;//new
                    MainFrame.mineDecrease();
                    if (MineCount == 0 && !OnePlayer)
                        controller.GameOver();
                } else if (this.content != 0) {
                    if (sound != 2) {
                        //Audio flag_wrong = new Audio(path + "/sounds/flag_wrong.wav");         //new,播放声音
                        //flag_wrong.play();
                    }
                    MainFrame.controller.getOnTurnPlayer().addMistake();
                    this.status = GridStatus.Flag;
                    MineCount--;
                    MainFrame.mineDecrease();
                } else {
                    this.status = GridStatus.Flag;
                    MineCount--;
                    MainFrame.mineDecrease();
                    if (sound != 2) {
                        //Audio flag_wrong = new Audio(path + "/sounds/flag_wrong.wav");         //new,播放声音
                        //flag_wrong.play();
                    }
                    MainFrame.controller.getOnTurnPlayer().addMistake();
                    //uncover();
                }
                repaint();
                GamePanel.turnNum += 1;
                stepDecrease();          //new
                this.setBorder(null);
                if ((turnNum) % Click_times == 0 && !hasWin) {
                    MainFrame.controller.nextTurn();
                }
            } else if (status == GridStatus.Flag) {
                status = GridStatus.Covered;
                MineCount += 1;
                MainFrame.mineDecrease();
                repaint();
            }
            MainFrame.controller.getScoreBoard().update();
        }
        if (OnePlayer) {
            int a = 0;
            for (GridComponent grid : grids) {
                if (grid.status == GridStatus.Clicked && grid.getContent() != -1)
                    a += 1;
            }
            if (a == X * Y - mine_count)
                controller.GameOver();
        }
        //TODO: 在右键点击一个格子的时候，还需要做什么？
    }

    @Override
    public void onMouseMiddleClicked() {
        //System.out.printf("Gird (%d,%d) is middle-clicked.\n", row, col);
        if (this.status == GridStatus.Clicked) {
            int num = 0;
            if (row != 0) {
                int k = -1;
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col)
                        k = i;
                }
                if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                    num++;
                if (col != 0) {
                    for (int i = 0; i < grids.size(); i++) {
                        if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col - 1)
                            k = i;
                    }
                    if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                        num++;
                }
                if (col != Y - 1) {
                    for (int i = 0; i < grids.size(); i++) {
                        if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col + 1)
                            k = i;
                    }
                    if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                        num++;
                }
            }
            if (col != 0) {
                int k = -1;
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getCol() == col - 1 && grids.get(i).getRow() == row)
                        k = i;
                }
                if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                    num++;
                if (row != X - 1) {
                    for (int i = 0; i < grids.size(); i++) {
                        if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col - 1)
                            k = i;
                    }
                    if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                        num++;
                }
            }
            if (row != X - 1) {
                int k = -1;
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col)
                        k = i;
                }
                if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                    num++;
                if (col != Y - 1) {
                    for (int i = 0; i < grids.size(); i++) {
                        if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col + 1)
                            k = i;
                    }
                    if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                        num++;
                }
            }
            if (col != Y - 1) {
                int k = -1;
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getCol() == col + 1 && grids.get(i).getRow() == row)
                        k = i;
                }
                if (grids.get(k).getGridStatus() == GridStatus.Flag || (grids.get(k).getGridStatus() == GridStatus.Clicked && grids.get(k).getContent() == -1))
                    num++;
            }
            if (num == content)
                uncoverMiddle();
            GamePanel.turnNum += 1;
            stepDecrease();          //new
            if ((turnNum) % Click_times == 0 && !hasWin) {
                MainFrame.controller.nextTurn();
            }
        }
        MainFrame.controller.getScoreBoard().update();
        if (OnePlayer) {
            boolean b = true;
            for (GridComponent grid : grids) {
                if (grid.status == GridStatus.Clicked && grid.getContent() == -1) {
                    b = false;
                    Audio boom = new Audio(path + "/sounds/boom.wav");         //new,播放声音
                    boom.play();
                    controller.GameOver();
                    break;
                }
            }
            if (b) {
                int a = 0;
                for (GridComponent grid : grids) {
                    if (grid.status == GridStatus.Clicked && grid.getContent() != -1)
                        a += 1;
                }
                if (a == X * Y - mine_count)
                    controller.GameOver();
            }
        }
    }

    public void uncover()//针对点开空格开一片的方法
    {
        status = GridStatus.Clicked;
        repaint();
        if (row != 0) {
            int k = -1;
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
            if (col != 0) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col - 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
            if (col != Y - 1) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col + 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
        }
        if (col != 0) {
            int k = -1;
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getCol() == col - 1 && grids.get(i).getRow() == row)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
            if (row != X - 1) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col - 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
        }
        if (row != X - 1) {
            int k = -1;
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
            if (col != Y - 1) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col + 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
        }
        if (col != Y - 1) {
            int k = -1;
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getCol() == col + 1 && grids.get(i).getRow() == row)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
        }
    }

    public void uncoverMiddle()//针对点中键开周边的方法，上面方法的改版
    {
        if (row != 0) {
            int k = -1;
            //System.out.println(grids.size());
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
            if (col != 0) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col - 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
            if (col != Y - 1) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row - 1 && grids.get(i).getCol() == col + 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
        }
        if (col != 0) {
            int k = -1;
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getCol() == col - 1 && grids.get(i).getRow() == row)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
            if (row != X - 1) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col - 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
        }
        if (row != X - 1) {
            int k = -1;
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
            if (col != Y - 1) {
                for (int i = 0; i < grids.size(); i++) {
                    if (grids.get(i).getRow() == row + 1 && grids.get(i).getCol() == col + 1)
                        k = i;
                }
                if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                    grids.get(k).uncover();
                else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                    grids.get(k).setStatus(GridStatus.Clicked);
                    grids.get(k).repaint();
                }
            }
        }
        if (col != Y - 1) {
            int k = -1;
            for (int i = 0; i < grids.size(); i++) {
                if (grids.get(i).getCol() == col + 1 && grids.get(i).getRow() == row)
                    k = i;
            }
            if (grids.get(k).getContent() == 0 && grids.get(k).getGridStatus() == GridStatus.Covered)
                grids.get(k).uncover();
            else if (grids.get(k).getGridStatus() != GridStatus.Flag) {
                grids.get(k).setStatus(GridStatus.Clicked);
                grids.get(k).repaint();
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (Theme == 0) {
            if (this.status == GridStatus.Covered) {
                g.drawImage(imageCover, 0, 0, getWidth() - 1, getHeight() - 1, this);
            }
            if (this.status == GridStatus.Clicked) {
                if (this.content == 0)
                    g.drawImage(image0, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 1)
                    g.drawImage(image1, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 2)
                    g.drawImage(image2, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 3)
                    g.drawImage(image3, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 4)
                    g.drawImage(image4, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 5)
                    g.drawImage(image5, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 6)
                    g.drawImage(image6, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 7)
                    g.drawImage(image7, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 8)
                    g.drawImage(image8, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == -1)
                    g.drawImage(imageMine, 0, 0, getWidth() - 1, getHeight() - 1, this);

            }
            if (this.status == GridStatus.Flag) {
                g.drawImage(imageFlag, 0, 0, getWidth() - 1, getHeight() - 1, this);
            }
        } else {
            if (this.status == GridStatus.Covered) {
                g.drawImage(image0Cover, 0, 0, getWidth() - 1, getHeight() - 1, this);
            }
            if (this.status == GridStatus.Clicked) {
                if (this.content == 0)
                    g.drawImage(image00, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 1)
                    g.drawImage(image01, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 2)
                    g.drawImage(image02, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 3)
                    g.drawImage(image03, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (this.content == 4)
                    g.drawImage(image04, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 5)
                    g.drawImage(image05, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 6)
                    g.drawImage(image06, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 7)
                    g.drawImage(image07, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == 8)
                    g.drawImage(image08, 0, 0, getWidth() - 1, getHeight() - 1, this);
                else if (content == -1)
                    g.drawImage(image0Mine, 0, 0, getWidth() - 1, getHeight() - 1, this);

            }
            if (this.status == GridStatus.Flag) {
                g.drawImage(image0Flag, 0, 0, getWidth() - 1, getHeight() - 1, this);
            }
        }
    }

    public void setContent(int content) {
        this.content = content;
    }

    public void addContent() {
        content++;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.printComponents(g);
        draw(g);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public GridStatus getGridStatus() {
        return status;
    }

    public int getContent() {
        return content;
    }

    public void setStatus(GridStatus status) {
        this.status = status;
    }
}
