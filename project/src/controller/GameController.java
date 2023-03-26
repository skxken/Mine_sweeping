package controller;

import javax.swing.*;//new
import java.util.*;

import components.GridComponent;
import entity.GridStatus;
import minesweeper.GamePanel;
import entity.Player;
import minesweeper.MainFrame;
import minesweeper.ScoreBoard;

import java.util.Timer;

import static minesweeper.GamePanel.*;
import static minesweeper.MainFrame.*;


public class GameController {

    private Player p1;
    private Player p2;

    private Player onTurn;

    private GamePanel gamePanel;
    private ScoreBoard scoreBoard;

    public Timer timer = new Timer();//用于记录切换回合的时间
    public Timer second = new Timer();//用于记录显示的时间
    public Timer aiPlaying = new Timer();//新，用于记录ai回合的延时
    public static int timeOfaStep = 15000;//设置一步的时间，默认15s
    public static boolean hasWin = false;
    public static int times = 0;

    public GameController(Player p1, Player p2) {
        if(!OnePlayer) {
            if (!hasWin)
                changeTurn();
            changeTime(0);
        }
        else
        {
            changeTime(0);
        }
        this.init(p1, p2);
        this.onTurn = p1;
    }

    /**
     * 初始化游戏。在开始游戏前，应先调用此方法，给予游戏必要的参数。
     *
     * @param p1 玩家1
     * @param p2 玩家2
     */
    public void init(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.onTurn = p1;
        //TODO: 在初始化游戏的时候，还需要做什么？
    }

    /**
     * 进行下一个回合时应调用本方法。
     * 在这里执行每个回合结束时需要进行的操作。
     * <p>
     * (目前这里没有每个玩家进行n回合的计数机制的，请自行修改完成哦~）
     */
    public void nextTurn() {
        if(!OnePlayer) {
            timer.cancel();
            second.cancel();
            setTime(1);
        }
        if (onTurn == p1) {
            onTurn = p2;
        } else if (onTurn == p2) {
            onTurn = p1;
        }
        //System.out.println("Now it is " + onTurn.getUserName() + "'s turn.");
        //TODO: 在每个回合结束的时候，还需要做什么 (例如...检查游戏是否结束？)
        if(!OnePlayer) {
            if (!hasWin)
                changeTurn();
            changeTime(0);
        }
        scoreBoard.update();
        if (useAI != 0 && onTurn == p2 && hasWin == false)//进入电脑回合,有修改
        {
            aiPlaying = new Timer();
            times = 0;
            aiPlaying.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!hasWin)
                        aiPlaying();
                    else controller.setOnTurn(p1);
                    times++;
                    if (times == Click_times)
                        aiPlaying.cancel();
                }
            }, 1000, 1000);

        }
    }

    public void GameOver() {
        JFrame gameOver = new JFrame();
        gameOver.setLocation(550, 300);
        gameOver.setTitle("Game Over!");
        gameOver.setSize(400, 200);
        JLabel player = new JLabel();
        gameOver.add(player);
        if (!OnePlayer) {
            if (useAI != 0)
                setOnTurn(p1);

            if (p1.getScore() > p2.getScore() || (p1.getScore() == p2.getScore() && p1.getMistake() < p2.getMistake()) && hasWin == false) {
                String s = p1.getUserName() + " wins the game!";
                player.setText(s);
                gameOver.setVisible(true);
                hasWin = true;
            } else if (p1.getScore() < p2.getScore() || (p1.getScore() == p2.getScore() && p1.getMistake() > p2.getMistake()) && hasWin == false) {
                String s = p2.getUserName() + " wins the game!";
                player.setText(s);
                gameOver.setVisible(true);
                hasWin = true;
            } else if (hasWin == false) {
                player.setText("It ends in a draw!");
                gameOver.setVisible(true);
                hasWin = true;
            }
            second.cancel();
            timer.cancel();
        } else {
            for (GridComponent grid : grids) {
                if (grid.getGridStatus() == GridStatus.Clicked && grid.getContent() == -1) {
                    player.setText("You lost the game");
                    gameOver.setVisible(true);
                    hasWin = true;
                    second.cancel();
                    break;
                }
            }
            if (!hasWin) {
                player.setText("You win the game");
                gameOver.setVisible(true);
                hasWin = true;
                second.cancel();
            }
        }
    }

    public void changeTurn()//用于回合更换
    {
        if (timeOfaStep > 0) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   while (GamePanel.turnNum % GamePanel.Click_times != 0)
                                       GamePanel.turnNum++;
                                   if (!hasWin) {
                                       if (useAI != 0 && XRay_Btn_Click_Turn % 2 == 1)
                                           MainFrame.CheatAndCancel();
                                       nextTurn();
                                   } else {
                                       timer.cancel();
                                       second.cancel();
                                   }
                               }
                           }
                    , timeOfaStep * GamePanel.Click_times);
        }
    }

    public void aiPlaying()//ai的具体行动，有更改，useAI==1为简单模式，2为普通模式（原来的AI），3为困难模式（虽然也不怎么困难）
    {
        int[][] mineMap = new int[X][Y];//用于在困难难度下找出不可能是雷的格子，1代表不可能是雷
        boolean b = false;
        if (MineCount != 0 && useAI != 1) {
            for (int row = 0; row < X; row++) {
                for (int col = 0; col < Y; col++) {
                    if (grids.get(row * Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col).getContent() >= 1 && grids.get(row * Y + col).getContent() <= 8) {
                        int k = 0;
                        if (row != 0) {
                            if (grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Covered || (grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col).getContent() == -1) || grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Flag) {
                                k++;
                            }
                            if (col != 0) {
                                if (grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Covered || (grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col - 1).getContent() == -1) || grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                            }
                            if (col != Y - 1) {
                                if (grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Covered || (grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col + 1).getContent() == -1) || grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                            }
                        }
                        if (col != 0) {
                            if (grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Covered || (grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col - 1).getContent() == -1) || grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                k++;
                            }
                            if (row != X - 1) {
                                if (grids.get(row * Y + col - 1 + Y).getGridStatus() == GridStatus.Covered || (grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + Y + col - 1).getContent() == -1) || grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                            }
                        }
                        if (row != X - 1) {
                            if (grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Covered || (grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y).getContent() == -1) || grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Flag) {
                                k++;
                            }
                            if (col != Y - 1) {
                                if (grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Covered || (grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y + 1).getContent() == -1) || grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                            }
                        }
                        if (col != Y - 1) {
                            if (grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Covered || (grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + 1).getContent() == -1) || grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                k++;
                            }
                        }
                        if (k == grids.get(row * Y + col).getContent()) {
                            if (row != 0) {
                                if (grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Covered && b == false) {
                                    grids.get(row * Y - Y + col).onMouseRightClicked();
                                    b = true;
                                }
                                if (col != 0) {
                                    if (grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Covered && b == false) {
                                        grids.get(row * Y - Y + col - 1).onMouseRightClicked();
                                        b = true;
                                    }
                                }
                                if (col != Y - 1) {
                                    if (grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Covered && b == false) {
                                        grids.get(row * Y - Y + col + 1).onMouseRightClicked();
                                        b = true;
                                    }
                                }
                            }
                            if (col != 0) {
                                if (grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Covered && b == false) {
                                    grids.get(row * Y + col - 1).onMouseRightClicked();
                                    b = true;
                                }
                                if (row != X - 1) {
                                    if (grids.get(row * Y + col - 1 + Y).getGridStatus() == GridStatus.Covered && b == false) {
                                        grids.get(row * Y + col - 1 + Y).onMouseRightClicked();
                                        b = true;
                                    }
                                }
                            }
                            if (row != X - 1) {
                                if (grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Covered && b == false) {
                                    grids.get(row * Y + col + Y).onMouseRightClicked();
                                    b = true;
                                }
                                if (col != Y - 1) {
                                    if (grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Covered && b == false) {
                                        grids.get(row * Y + col + Y + 1).onMouseRightClicked();
                                        b = true;
                                    }
                                }
                            }
                            if (col != Y - 1) {
                                if (grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Covered && b == false) {
                                    grids.get(row * Y + col + 1).onMouseRightClicked();
                                    b = true;
                                }
                            }
                        }
                    }
                    if (b == true)
                        break;
                }
                if (b == true)
                    break;
            }
            if (b == false && useAI == 3)//标不可能是雷的格子，并进行第二次判断
            {
                for (int row = 0; row < X; row++) {
                    for (int col = 0; col < Y; col++) {
                        if (grids.get(row * Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col).getContent() >= 1 && grids.get(row * Y + col).getContent() <= 8) {
                            int k = 0;
                            if (row != 0) {
                                if ((grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col).getContent() == -1) || grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                                if (col != 0) {
                                    if ((grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col - 1).getContent() == -1) || grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                                if (col != Y - 1) {
                                    if ((grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col + 1).getContent() == -1) || grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                            }
                            if (col != 0) {
                                if ((grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col - 1).getContent() == -1) || grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                                if (row != X - 1) {
                                    if ((grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + Y + col - 1).getContent() == -1) || grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                            }
                            if (row != X - 1) {
                                if ((grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y).getContent() == -1) || grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                                if (col != Y - 1) {
                                    if ((grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y + 1).getContent() == -1) || grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                            }
                            if (col != Y - 1) {
                                if ((grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + 1).getContent() == -1) || grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                            }
                            if (k == grids.get(row * Y + col).getContent()) {
                                if (row != 0) {
                                    mineMap[row - 1][col] = 1;
                                    if (col != 0) {
                                        mineMap[row - 1][col - 1] = 1;
                                    }
                                    if (col != Y - 1) {
                                        mineMap[row - 1][col + 1] = 1;
                                    }
                                }
                                if (col != 0) {
                                    mineMap[row][col - 1] = 1;
                                    if (row != X - 1) {
                                        mineMap[row + 1][col - 1] = 1;
                                    }
                                }
                                if (row != X - 1) {
                                    mineMap[row + 1][col] = 1;
                                    if (col != Y - 1) {
                                        mineMap[row + 1][col + 1] = 1;
                                    }
                                }
                                if (col != Y - 1) {
                                    mineMap[row][col + 1] = 1;
                                }
                            }
                        }
                    }
                }
                for (int row = 0; row < X; row++) {//二次判断
                    for (int col = 0; col < Y; col++) {
                        if (grids.get(row * Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col).getContent() >= 1 && grids.get(row * Y + col).getContent() <= 8) {
                            int k = 0;
                            if (row != 0) {
                                if ((grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Covered && mineMap[row - 1][col] == 0) || (grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col).getContent() == -1) || grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                                if (col != 0) {
                                    if ((grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Covered && mineMap[row - 1][col - 1] == 0) || (grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col - 1).getContent() == -1) || grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                                if (col != Y - 1) {
                                    if ((grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Covered && mineMap[row - 1][col + 1] == 0) || (grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col + 1).getContent() == -1) || grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                            }
                            if (col != 0) {
                                if ((grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Covered && mineMap[row][col - 1] == 0) || (grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col - 1).getContent() == -1) || grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                                if (row != X - 1) {
                                    if ((grids.get(row * Y + col - 1 + Y).getGridStatus() == GridStatus.Covered && mineMap[row + 1][col - 1] == 0) || (grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + Y + col - 1).getContent() == -1) || grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                            }
                            if (row != X - 1) {
                                if ((grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Covered && mineMap[row + 1][col] == 0) || (grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y).getContent() == -1) || grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                                if (col != Y - 1) {
                                    if ((grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Covered && mineMap[row + 1][col + 1] == 0) || (grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y + 1).getContent() == -1) || grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    }
                                }
                            }
                            if (col != Y - 1) {
                                if ((grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Covered && mineMap[row][col + 1] == 0) || (grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + 1).getContent() == -1) || grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                }
                            }
                            if (k == grids.get(row * Y + col).getContent()) {
                                if (row != 0) {
                                    if (grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Covered && b == false && mineMap[row - 1][col] == 0) {
                                        grids.get(row * Y - Y + col).onMouseRightClicked();
                                        b = true;
                                    }
                                    if (col != 0) {
                                        if (grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Covered && b == false && mineMap[row - 1][col - 1] == 0) {
                                            grids.get(row * Y - Y + col - 1).onMouseRightClicked();
                                            b = true;
                                        }
                                    }
                                    if (col != Y - 1) {
                                        if (grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Covered && b == false && mineMap[row - 1][col + 1] == 0) {
                                            grids.get(row * Y - Y + col + 1).onMouseRightClicked();
                                            b = true;
                                        }
                                    }
                                }
                                if (col != 0) {
                                    if (grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Covered && b == false && mineMap[row][col - 1] == 0) {
                                        grids.get(row * Y + col - 1).onMouseRightClicked();
                                        b = true;
                                    }
                                    if (row != X - 1) {
                                        if (grids.get(row * Y + col - 1 + Y).getGridStatus() == GridStatus.Covered && b == false && mineMap[row + 1][col - 1] == 0) {
                                            grids.get(row * Y + col - 1 + Y).onMouseRightClicked();
                                            b = true;
                                        }
                                    }
                                }
                                if (row != X - 1) {
                                    if (grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Covered && b == false && mineMap[row + 1][col] == 0) {
                                        grids.get(row * Y + col + Y).onMouseRightClicked();
                                        b = true;
                                    }
                                    if (col != Y - 1) {
                                        if (grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Covered && b == false && mineMap[row + 1][col + 1] == 0) {
                                            grids.get(row * Y + col + Y + 1).onMouseRightClicked();
                                            b = true;
                                        }
                                    }
                                }
                                if (col != Y - 1) {
                                    if (grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Covered && b == false && mineMap[row][col + 1] == 0) {
                                        grids.get(row * Y + col + 1).onMouseRightClicked();
                                        b = true;
                                    }
                                }
                            }
                        }
                        if (b == true)
                            break;
                    }
                    if (b == true)
                        break;
                }
            }
            if (b == false && (turnNum % Click_times) >= (Click_times / 2))//点左键
            {
                for (int row = 0; row < X; row++) {
                    for (int col = 0; col < Y; col++) {
                        if (mineMap[row][col] == 1 && grids.get(row * Y + col).getGridStatus() == GridStatus.Covered) {
                            grids.get(row * Y + col).onMouseLeftClicked();
                            b = true;
                        }
                        if (b == true)
                            break;
                    }
                    if (b == true)
                        break;
                }
            } else if (b == false) //点中键
            {
                for (int row = 0; row < X; row++) {
                    for (int col = 0; col < Y; col++) {
                        if (grids.get(row * Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col).getContent() >= 1 && grids.get(row * Y + col).getContent() <= 8) {
                            int k = 0;
                            int k1 = 0;
                            if (row != 0) {
                                if ((grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col).getContent() == -1) || grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                } else if (grids.get(row * Y - Y + col).getGridStatus() == GridStatus.Covered)
                                    k1++;
                                if (col != 0) {
                                    if ((grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col - 1).getContent() == -1) || grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    } else if (grids.get(row * Y - Y + col - 1).getGridStatus() == GridStatus.Covered)
                                        k1++;
                                }
                                if (col != Y - 1) {
                                    if ((grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y - Y + col + 1).getContent() == -1) || grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    } else if (grids.get(row * Y - Y + col + 1).getGridStatus() == GridStatus.Covered)
                                        k1++;
                                }
                            }
                            if (col != 0) {
                                if ((grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col - 1).getContent() == -1) || grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                } else if (grids.get(row * Y + col - 1).getGridStatus() == GridStatus.Covered)
                                    k1++;
                                if (row != X - 1) {
                                    if ((grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + Y + col - 1).getContent() == -1) || grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    } else if (grids.get(row * Y + Y + col - 1).getGridStatus() == GridStatus.Covered)
                                        k1++;
                                }
                            }
                            if (row != X - 1) {
                                if ((grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y).getContent() == -1) || grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                } else if (grids.get(row * Y + col + Y).getGridStatus() == GridStatus.Covered)
                                    k1++;
                                if (col != Y - 1) {
                                    if ((grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + Y + 1).getContent() == -1) || grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Flag) {
                                        k++;
                                    } else if (grids.get(row * Y + col + Y + 1).getGridStatus() == GridStatus.Covered)
                                        k1++;
                                }
                            }
                            if (col != Y - 1) {
                                if ((grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Clicked && grids.get(row * Y + col + 1).getContent() == -1) || grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Flag) {
                                    k++;
                                } else if (grids.get(row * Y + col + 1).getGridStatus() == GridStatus.Covered)
                                    k1++;
                            }
                            if (k == grids.get(row * Y + col).getContent() && k1 != 0) {
                                grids.get(row * Y + col).onMouseMiddleClicked();
                                b = true;
                            }
                        }
                        if (b == true)
                            break;
                    }
                    if (b == true)
                        break;
                }
            }
            if (b == false) {
                //System.out.println("!");
                Random random = new Random();
                int row1, col1;
                do {
                    row1 = random.nextInt(X);
                    col1 = random.nextInt(Y);
                }
                while (grids.get(row1 * Y + col1).getGridStatus() != GridStatus.Covered);
                grids.get(row1 * Y + col1).onMouseLeftClicked();
            }
        }
        if (MineCount != 0 && useAI == 1) {
            Random random = new Random();
            int row1, col1;
            do {
                row1 = random.nextInt(X);
                col1 = random.nextInt(Y);
            }
            while (grids.get(row1 * Y + col1).getGridStatus() != GridStatus.Covered);
            if (grids.get(row1 * Y + col1).getContent() != -1)
                grids.get(row1 * Y + col1).onMouseLeftClicked();
            else {
                int ran = random.nextInt(2);
                if (ran == 0)
                    grids.get(row1 * Y + col1).onMouseLeftClicked();
                else
                    grids.get(row1 * Y + col1).onMouseRightClicked();
            }
        }
    }


    public void changeTime(int num)//用于显示剩余时间
    {
        second = new Timer();
        second.schedule(new TimerTask() {
            @Override
            public void run() {
                MainFrame.setTime(num);
            }
        }, 0, 1000);
    }

    /**
     * 获取正在进行当前回合的玩家。
     *
     * @return 正在进行当前回合的玩家
     */
    public Player getOnTurnPlayer() {
        return onTurn;
    }

    public void setOnTurn(Player player) {
        onTurn = player;
    }


    public Player getP1() {
        return p1;
    }

    public Player getP2() {
        return p2;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void setScoreBoard(ScoreBoard scoreBoard) {
        this.scoreBoard = scoreBoard;
    }

}