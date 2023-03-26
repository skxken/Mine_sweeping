package minesweeper;

import components.GridComponent;


import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.ArrayList;

import static minesweeper.MainFrame.X;
import static minesweeper.MainFrame.Y;

public class GamePanel extends JPanel {
    private GridComponent[][] mineField;
    private int[][] chessboard;
    private final Random random = new Random();
    public static ArrayList<GridComponent> grids = new ArrayList<GridComponent>();
    public static int turnNum = 0;      //新加的，用来判断是否为第一轮，为了第一下不是雷。 但后来觉得没必要这么写，但又懒得改了(^.^) 我错了，这玩意有用...
    public static int Click_times = 1;

    /**
     * 初始化一个具有指定行列数格子、并埋放了指定雷数的雷区。
     *
     * @param xCount    count of grid in column
     * @param yCount    count of grid in row
     * @param mineCount mine count
     */
    public GamePanel(int xCount, int yCount, int mineCount) {
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.setBackground(Color.WHITE);
        this.setSize(GridComponent.gridSize * yCount, GridComponent.gridSize * xCount);

        initialGame(xCount, yCount, mineCount);

        repaint();
    }

    public void initialGame(int xCount, int yCount, int mineCount) {
        try {
            mineField = new GridComponent[xCount][yCount];
            generateChessBoard(xCount, yCount, mineCount);

            for (int i = 0; i < xCount; i++) {
                for (int j = 0; j < yCount; j++) {
                    GridComponent gridComponent = new GridComponent(i, j);
                    gridComponent.setContent(chessboard[i][j]);
                    gridComponent.setLocation(j * GridComponent.gridSize, i * GridComponent.gridSize);
                    mineField[i][j] = gridComponent;
                    this.add(mineField[i][j]);
                    grids.add(gridComponent);
                }
            }
            generateNumOfMine(xCount, yCount);

        } catch (Exception exception) {             //new
            JFrame Wrong = new JFrame();
            Wrong.setTitle("Wrong");
            Wrong.setLocation(550, 300);
            Wrong.setSize(400, 160);
            Wrong.setVisible(true);
            Wrong.setLayout(null);
            JLabel wrong_info = new JLabel();
            wrong_info.setText("<html><body>Wrong<br>maybe you change the save file or there is something wrong with this file or you haven't saved it<html><body>");
            wrong_info.setSize(200, 80);
            wrong_info.setLocation(5, 5);
            wrong_info.setVisible(true);
            Wrong.add(wrong_info);
        }
    }

    public void initialGame2(int xCount, int yCount, int mineCount) {
        mineField = new GridComponent[xCount][yCount];
        generateChessBoard(xCount, yCount, mineCount);

        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < yCount; j++) {
                int k = 0;
                while (grids.get(k).getRow() != i | grids.get(k).getCol() != j) {
                    k++;
                }
                grids.get(k).setContent(chessboard[i][j]);
                mineField[i][j] = grids.get(k);
                this.add(mineField[i][j]);
            }
        }
        generateNumOfMine(xCount, yCount);
    }

    public void generateChessBoard(int xCount, int yCount, int mineCount) {
        //todo: generate chessboard by your own algorithm

        generateMine(xCount, yCount, mineCount);
        if (checkIsCrowd(xCount, yCount, mineCount))
            generateChessBoard(xCount, yCount, mineCount);
    }

    /**
     * 获取一个指定坐标的格子。
     * 注意请不要给一个棋盘之外的坐标哦~
     *
     * @param x 第x列
     * @param y 第y行
     * @return 该坐标的格子
     */
    public GridComponent getGrid(int x, int y) {
        try {
            return mineField[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public void generateMine(int xCount, int yCount, int mineCount) {
        try {
            int ranX, ranY, ran, counter = 0;
            chessboard = new int[xCount][yCount];
            while (counter < mineCount) {
                ranX = random.nextInt(xCount);
                ranY = random.nextInt(yCount);
                if (chessboard[ranX][ranY] != -1) {
                    chessboard[ranX][ranY] = -1;
                    counter += 1;
                }
            }
        } catch (Exception exception) {          //new
            JFrame Wrong = new JFrame();
            Wrong.setTitle("Wrong");
            Wrong.setLocation(550, 300);
            Wrong.setSize(400, 160);
            Wrong.setVisible(true);
            Wrong.setLayout(null);
            JLabel wrong_info = new JLabel();
            wrong_info.setText("<html><body>Wrong<br>maybe you change the save file or there is something wrong with this file or you haven't saved it<html><body>");
            wrong_info.setSize(200, 80);
            wrong_info.setLocation(5, 5);
            wrong_info.setVisible(true);
            Wrong.add(wrong_info);
        }
    }

    public void generateNumOfMine(int xCount, int yCount) {
        for (int row = 0; row < X; row++) {
            for (int col = 0; col < Y; col++) {
                if (grids.get(row * Y + col).getContent() == -1) {
                    if (row != 0) {
                        if (grids.get(row * Y - Y + col).getContent() != -1) {
                            grids.get(row * Y - Y + col).addContent();
                        }
                        if (col != 0) {
                            if (grids.get(row * Y - Y + col - 1).getContent() != -1) {
                                grids.get(row * Y - Y + col - 1).addContent();
                            }
                        }
                        if (col != Y - 1) {
                            if (grids.get(row * Y - Y + col + 1).getContent() != -1) {
                                grids.get(row * Y - Y + col + 1).addContent();
                            }
                        }
                    }
                    if (col != 0) {
                        if (grids.get(row * Y + col - 1).getContent() != -1) {
                            grids.get(row * Y + col - 1).addContent();
                        }
                        if (row != X - 1) {
                            if (grids.get(row * Y + col - 1 + Y).getContent() != -1) {
                                grids.get(row * Y + col - 1 + Y).addContent();
                            }
                        }
                    }
                    if (row != X - 1) {
                        if (grids.get(row * Y + col + Y).getContent() != -1) {
                            grids.get(row * Y + col + Y).addContent();
                        }
                        if (col != Y - 1) {
                            if (grids.get(row * Y + col + Y + 1).getContent() != -1) {
                                grids.get(row * Y + col + Y + 1).addContent();
                            }
                        }
                    }
                    if (col != Y - 1) {
                        if (grids.get(row * Y + col + 1).getContent() != -1) {
                            grids.get(row * Y + col + 1).addContent();
                        }
                    }
                }
            }
        }
    }

    public boolean checkIsCrowd(int xCount, int yCount, int mineCount) {    //判断是否3*3全为雷
        for (int i = 1; i < xCount - 1; i++) {
            for (int j = 1; j < yCount - 1; j++) {
                int counter = 0;
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        if (di == 0 && dj == 0) {
                            continue;
                        }
                        if (i + di < xCount & j + dj < yCount) {
                            if (chessboard[i + di][j + dj] == -1) {
                                counter++;
                            }
                        }
                    }
                }
                if (counter == 8 & chessboard[i][j] == -1)
                    return true;
            }
        }
        return false;
    }
}
