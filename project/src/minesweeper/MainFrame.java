package minesweeper;


import components.GridComponent;
import controller.GameController;
import entity.GridStatus;
import entity.Player;
import sun.audio.AudioData;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import static controller.GameController.hasWin;
import static controller.GameController.times;
import static minesweeper.GamePanel.*;

public class MainFrame extends JFrame {
    public static GameController controller;
    private int xCount;
    private int yCount;
    private int mineCount;
    public static int time;//当前回合剩余时间，单位:s
    public static int X;
    public static int Y;
    public static int MineCount;
    public static JLabel l = new JLabel();//用于显示剩余雷数
    public static JLabel l1 = new JLabel();//用于显示剩余时间
    public static JLabel l2 = new JLabel();//用于显示剩余步数
    public static String path = System.getProperty("user.dir");//用于调取目前所在路径

    public static int XRay_Btn_Click_Turn; //new，用于透视雷/取消判定
    private static ArrayList<GridComponent> gridsTmp = new ArrayList<GridComponent>();   //new,暂存grid
    //下面的这些变量是为了读档准备的
    private static String numOfGrids = new String();//各格的数字
    private static String statusOfGrids = new String();//各格的状态
    private static Player player1 = new Player();//p1的相关数据
    private static Player player2 = new Player();//p2的相关数据
    private static int remainMines = 0;
    private static int readFile = 0;//标记是否为读档，若是读档则为1

    private static ContinuousAudioDataStream BGM;//new BGM
    public static int sound = 1;         //1播放 0不新播放  2结束播放

    public static int useAI = 0;//是否使用人机对战，0代表否，1代表简单 2普通 3 困难
    private JScrollPane scrollPane;    //主页背景
    private ImageIcon icon;           //主页背景
    private static String P1_Name;  //new
    private static String P2_Name;   //new
    private static String step_Left; //new 玩家剩余步数
    public static boolean Ban_First_Mine_Check = false;  //用于replay，禁用开局保护
    public static int Theme = 0;     //主题切换  0代表雷 1代表花园
    public static boolean OnePlayer = false; //false为人机或双人    true为单人模式
    public static int mine_count;  //单人模式下总雷数

    public MainFrame() {//无参，用于启动初始界面
        //System.out.println(path);
        JFrame initial_interface = new JFrame();
        initial_interface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initial_interface.setSize(640, 370);
        initial_interface.setLocationRelativeTo(null);
        initial_interface.setVisible(true);
        initial_interface.setResizable(false);
        initial_interface.setTitle("");
        icon = new ImageIcon(path + "/pictures/background.png");
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.drawImage(icon.getImage(), 0, 0, null);
                super.paintComponent(g);
            }
        };
        panel.setLayout(null);
        panel.setOpaque(false);
        scrollPane = new JScrollPane(panel);
        initial_interface.getContentPane().add(scrollPane);
        JLabel contributor =new JLabel();
        contributor.setSize(250,50);
        contributor.setLocation(470,275);
        contributor.setText("<html><body>version: release 1.0.1<br>contributors: 孙开轩 林逸");
        panel.add(contributor);

        JButton Single_Play = new JButton("SinglePlayer Mode");     //单人模式
        Single_Play.setSize(160, 60);
        Single_Play.setLocation(40, 210);
        panel.add(Single_Play);
        Single_Play.addActionListener(a -> {
            initial_interface.dispose();
            OnePlayer = true;
            useAI = 0;
            GamePanel.turnNum = 0;
            grids.clear();
            new MainFrame(9, 9, 10);
        });
        JButton Multi_Play = new JButton("MultiPlayer Mode");     //双人模式
        Multi_Play.setSize(160, 60);
        Multi_Play.setLocation(230, 210);
        panel.add(Multi_Play);
        Multi_Play.addActionListener(a -> {
            initial_interface.dispose();
            OnePlayer = false;
            useAI = 0;
            GamePanel.turnNum = 0;
            grids.clear();
            new MainFrame(9, 9, 10);
        });
        JButton Computer_Human = new JButton("Computer vs. Human");       //人机模式
        Computer_Human.setSize(160, 60);
        Computer_Human.setLocation(420, 210);
        panel.add(Computer_Human);
        Computer_Human.addActionListener(a -> {
            initial_interface.dispose();
            CVH();
            /*useAI = 1;
            GamePanel.turnNum = 0;
            grids.clear();
            new MainFrame(9, 9, 10);*/
        });
    }

    public MainFrame(int xCount, int yCount, int mineCount) {             //参数暂留，用于settings传参
        //todo: change the count of xCount, yCount and mineCount by passing parameters from constructor
        this.xCount = xCount;//grid of row
        this.yCount = yCount;// grid of column
        this.mineCount = mineCount;// mine count
        X = this.xCount;
        Y = this.yCount;
        MineCount = this.mineCount;
        mine_count = this.mineCount;
        Ban_First_Mine_Check = false;
        int tempTime=time;

        this.setTitle("Minesweeper");
        this.setLayout(null);
        this.setSize(yCount * GridComponent.gridSize + 20, xCount * GridComponent.gridSize + 260);
        this.setLocationRelativeTo(null);
        Player p1 = new Player(P1_Name);
        Player p2 = new Player(P2_Name);
        if (useAI != 0)//电脑替代了p2，,用户名为Computer
            p2.setUserName("Computer");

        controller = new GameController(p1, p2);
        GamePanel gamePanel = new GamePanel(xCount, yCount, mineCount);
        controller.setGamePanel(gamePanel);
        ScoreBoard scoreBoard = new ScoreBoard(p1, p2, xCount, yCount);
        controller.setScoreBoard(scoreBoard);
        //Audio.BGM();

        JPanel step_left = new JPanel();//new
        step_left.setSize(100, 20);
        step_left.setLocation(yCount * GridComponent.gridSize / 2 - 50, 0);
        String s = "Step left: " + String.valueOf(Click_times);
        l2.setText(s);
        if (!OnePlayer)
            step_left.add(l2);
        step_left.setVisible(true);
        if(OnePlayer&&readFile!=1)
            time=0;
        if (readFile == 1)//如果是读档
        {
            try {
                time=tempTime;
                readFile = 0;
                p1.setUserName(player1.getUserName());
                p1.setScore(player1.getScore());
                p1.setMistake(player1.getMistake());
                p2.setUserName(player2.getUserName());
                p2.setScore(player2.getScore());
                p2.setMistake(player2.getMistake());
                if ((turnNum / Click_times) % 2 == 0)
                    controller.setOnTurn(p1);
                else
                    controller.setOnTurn(p2);
                scoreBoard.update();
                for (int i = 0; i < grids.size(); i++) {
                    if (numOfGrids.charAt(i) == '!')
                        grids.get(i).setContent(-1);
                    else
                        grids.get(i).setContent(numOfGrids.charAt(i) - '0');
                    if (statusOfGrids.charAt(i) == 'c')
                        grids.get(i).setStatus(GridStatus.Covered);
                    else if (statusOfGrids.charAt(i) == 'u')
                        grids.get(i).setStatus(GridStatus.Clicked);
                    else
                        grids.get(i).setStatus(GridStatus.Flag);
                    grids.get(i).repaint();
                }
                MineCount = remainMines;
                mineDecrease();
                l2.setText(step_Left);

            } catch (Exception exception) {
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
        } else hasWin = false;

        gamePanel.setLocation(0, 20);//new
        scoreBoard.setLocation(0, GridComponent.gridSize * xCount + 20);//new

        JPanel mineNum = new JPanel();//new
        mineNum.setSize(100, 20);
        mineNum.add(l);
        s = "Mines left: " + String.valueOf(MineCount);
        l.setText(s);
        mineNum.setVisible(true);

        JPanel timeLeft = new JPanel();
        timeLeft.setSize(100, 20);
        timeLeft.setLocation(yCount * GridComponent.gridSize - 100, 0);
        if (GameController.timeOfaStep != 0&&!OnePlayer) {
            time = GameController.timeOfaStep * GamePanel.Click_times / 1000;
            s = "Time left: " + String.valueOf(time);
        }
        else if(!OnePlayer)
            s = "No time limit";
        else if(OnePlayer)
        {
            s="Time used: "+String.valueOf(time);
        }
        l1.setText(s);
        timeLeft.add(l1);
        timeLeft.setVisible(true);


        this.add(mineNum);
        this.add(timeLeft);
        this.add(gamePanel);
        this.add(scoreBoard);
        this.add(step_left);

        JButton saveBtn = new JButton("Save");    //保存存档用
        saveBtn.setSize(110, 20);
        saveBtn.setLocation(5, gamePanel.getHeight() + scoreBoard.getHeight() + 20);//change
        add(saveBtn);
        saveBtn.addActionListener(e -> {
            SaveGUI(p1, p2);
        });

        JButton readBtn = new JButton("Read");       //读取存档用
        readBtn.setSize(110, 20);
        readBtn.setLocation(130, gamePanel.getHeight() + scoreBoard.getHeight() + 20);
        add(readBtn);
        readBtn.addActionListener(e -> {
            ReadGUI();
        });

        JButton restart = new JButton("Restart");
        restart.setSize(110, 20);
        restart.setLocation(5, gamePanel.getHeight() + scoreBoard.getHeight() + 45);//change
        add(restart);
        restart.addActionListener(e -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                GamePanel.turnNum = 0;
                grids = new ArrayList<GridComponent>();
                this.dispose();
                doSomething(this.xCount, this.yCount, this.mineCount);
            } else {
                forbidden();
            }
        });
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JButton XRay_Btn = new JButton("Cheat/cancel");      //透视雷部分
        XRay_Btn.setSize(110, 20);                //11/5/23:33 修改width为110
        XRay_Btn.setLocation(5, gamePanel.getHeight() + scoreBoard.getHeight() + 70);
        add(XRay_Btn);
        XRay_Btn.addActionListener(e -> {
            CheatAndCancel();
        });


        JButton settings = new JButton("Settings");           //设置框用
        settings.setSize(110, 20);
        settings.setLocation(130, gamePanel.getHeight() + scoreBoard.getHeight() + 45);
        add(settings);
        settings.addActionListener(e -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                this.settings();
            } else {
                forbidden();
            }
        });

        JButton BackBtn = new JButton("Back");       //回退主界面
        BackBtn.setSize(110, 20);
        BackBtn.setLocation(130, gamePanel.getHeight() + scoreBoard.getHeight() + 70);//change
        add(BackBtn);
        BackBtn.addActionListener(e -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                controller.timer.cancel();
                controller.second.cancel();

                this.dispose();
                new MainFrame();
            } else {
                forbidden();
            }
        });

        JButton Replay = new JButton("Replay");       //重玩此局
        Replay.setSize(110, 20);
        Replay.setLocation(5, gamePanel.getHeight() + scoreBoard.getHeight() + 95);//change
        add(Replay);
        Replay.addActionListener(e -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                Replay(p1, p2);
            } else {
                forbidden();
            }
        });

        JButton Exit = new JButton("Exit");       //退出
        Exit.setSize(110, 20);
        Exit.setLocation(130, gamePanel.getHeight() + scoreBoard.getHeight() + 95);
        add(Exit);
        Exit.addActionListener(e -> {
            System.exit(0);
        });


        if (sound == 1) {
            try {
                InputStream in = new FileInputStream(path + "/sounds/bgm.wav");
                AudioStream as = new AudioStream(in);
                AudioData data = as.getData();
                BGM = new ContinuousAudioDataStream(data);
                AudioPlayer.player.start(BGM);
                sound = 0;
            } catch (Exception exception) {
                //System.out.println(exception);
            }
        }
    }


    public static void doSomething() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.dispose();
            //AudioPlayer.player.stop(BGM);
        });
    }

    public static void doSomething(int xCount, int yCount, int mineCount) {
        SwingUtilities.invokeLater(() -> {
            controller.second.cancel();
            controller.timer.cancel();
            if (sound == 1) {
                AudioPlayer.player.stop(BGM);
            }
            MainFrame mainFrame = new MainFrame(xCount, yCount, mineCount);
            mainFrame.setVisible(true);
            if (sound == 2) {
                AudioPlayer.player.stop(BGM);
            }
        });
    }

    public static void mineDecrease() {           //new
        String s = "Mines left: " + String.valueOf(MineCount);
        l.setText(s);
    }

    public static void setTime(int num) {
        String s;
        if(!OnePlayer) {
            if (GameController.timeOfaStep == 0)
                s = "No time limit";
            else {
                if (num == 0) {
                    time = time - 1;
                } else {
                    time = GameController.timeOfaStep * GamePanel.Click_times / 1000;
                }
                s = "Time left: " + String.valueOf(time);
            }
        }
        else
        {
            time=time+1;
            s="Time used: "+String.valueOf(time);
        }
        l1.setText(s);
    }

    public static void stepDecrease() {
        String s;
        s = "Step left: " + (Click_times - (turnNum % Click_times));
        l2.setText(s);
    }

    public static void CheatAndCancel() {
        if (useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) {
            if (XRay_Btn_Click_Turn % 2 == 0) {
                gridsTmp.clear();
                for (GridComponent grid : GamePanel.grids) {
                    GridComponent grid_tmp = new GridComponent(grid.getRow(), grid.getCol(), grid.getContent(), grid.getGridStatus());
                    gridsTmp.add(grid_tmp);
                }
                for (GridComponent grid : GamePanel.grids) {
                    grid.setStatus(GridStatus.Clicked);
                    grid.repaint();
                }
            } else {
                for (int i = 0; i < gridsTmp.size(); i++) {
                    GamePanel.grids.get(i).setStatus(gridsTmp.get(i).getGridStatus());
                    GamePanel.grids.get(i).repaint();
                }
            }
            XRay_Btn_Click_Turn += 1;
        } else {
            forbidden();
        }
    }

    public static void forbidden() {
        JFrame WrongInput = new JFrame();
        WrongInput.setTitle("Forbidden!");
        WrongInput.setLocation(550, 300);
        WrongInput.setSize(400, 160);
        WrongInput.setVisible(true);
        WrongInput.setLayout(null);
        JLabel txt4 = new JLabel();
        txt4.setText("You can't do this when cheating or computer playing");
        txt4.setSize(300, 60);
        txt4.setLocation(5, 5);
        txt4.setVisible(true);
        WrongInput.add(txt4);
    }

    public void Replay(Player p1, Player p2) {
        JFrame Warming = new JFrame();
        Warming.setTitle("Tips");
        Warming.setLocation(550, 300);
        Warming.setSize(400, 160);
        Warming.setVisible(true);
        Warming.setLayout(null);
        JLabel wrong_info = new JLabel();
        wrong_info.setText("<html><body>Replay means this chessboard is same as the last, so you may click mine even first click<html><body>");
        wrong_info.setSize(200, 80);
        wrong_info.setLocation(5, 5);
        wrong_info.setVisible(true);
        Warming.add(wrong_info);
        Ban_First_Mine_Check = true;
        turnNum = 0;
        times = 0;
        p1.setScore(0);
        p1.setMistake(0);
        p2.setScore(0);
        p2.setMistake(0);
        MainFrame.controller.init(p1, p2);
        MainFrame.controller.getScoreBoard().update();
        controller.timer.cancel();        //这部分对时间操作
        controller.second.cancel();       //这部分对时间操作
        if(!OnePlayer) {
            setTime(1);
            controller.changeTurn();
            controller.changeTime(0);
        }
        else
        {
            time=0;
            String s="Time used: 0";
            l1.setText(s);
            controller.changeTime(0);
        }
        hasWin = false;
        MineCount = this.mineCount;
        l.setText("Mines left: " + String.valueOf(MineCount));
        l2.setText("Step left: " + String.valueOf(Click_times));

        for (GridComponent grid : grids) {
            grid.setStatus(GridStatus.Covered);
            grid.repaint();
        }
    }

    public void CVH() {                       //人机对战GUI界面
        JFrame CVH_interface = new JFrame();
        CVH_interface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CVH_interface.setSize(640, 370);
        CVH_interface.setLocationRelativeTo(null);
        CVH_interface.setVisible(true);
        CVH_interface.setResizable(false);
        CVH_interface.setTitle("Computer vs. Human");
        icon = new ImageIcon(path + "/pictures/CVH_background.png");
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.drawImage(icon.getImage(), 0, 0, null);
                super.paintComponent(g);
            }
        };
        panel.setLayout(null);
        panel.setOpaque(false);
        scrollPane = new JScrollPane(panel);
        CVH_interface.getContentPane().add(scrollPane);

        JButton Low = new JButton("Low Level");     //低难度
        Low.setSize(160, 60);
        Low.setLocation(40, 210);
        panel.add(Low);
        Low.addActionListener(a -> {
            CVH_interface.dispose();
            OnePlayer = false;
            useAI = 1;
            GamePanel.turnNum = 0;
            grids.clear();
            new MainFrame(9, 9, 10);
        });
        JButton Middle = new JButton("Middle Level");     //中等难度
        Middle.setSize(160, 60);
        Middle.setLocation(230, 210);
        panel.add(Middle);
        Middle.addActionListener(a -> {
            CVH_interface.dispose();
            OnePlayer = false;
            useAI = 2;
            GamePanel.turnNum = 0;
            grids.clear();
            new MainFrame(9, 9, 10);
        });
        JButton High = new JButton("High Level");       //高难度
        High.setSize(160, 60);
        High.setLocation(420, 210);
        panel.add(High);
        High.addActionListener(a -> {
            CVH_interface.dispose();
            OnePlayer = false;
            useAI = 3;
            GamePanel.turnNum = 0;
            grids.clear();
            new MainFrame(9, 9, 10);
        });
    }

    public void ReadGUI() {
        if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
            JFrame Read = new JFrame();
            Read.setTitle("Read");
            Read.setLocation(550, 300);
            Read.setSize(800, 150);
            Read.setVisible(true);
            Read.setLayout(null);

            JLabel txt1 = new JLabel();
            txt1.setText("Input save's name to read save(don't need include filename extension)(we don't take responsibility if you change the save illegally)");
            txt1.setSize(800, 20);
            txt1.setLocation(5, 5);
            txt1.setVisible(true);
            Read.add(txt1);
            JTextField read = new JTextField();
            read.setLocation(5, 40);
            read.setSize(420, 20);
            Read.add(read);
            JButton OK = new JButton("OK");
            OK.setLocation(5, 75);
            OK.setSize(160, 20);
            Read.add(OK);
            OK.addActionListener(a -> {
                if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                    String fileName = read.getText();
                    this.readFileData(fileName);
                    Read.dispose();
                } else forbidden();
            });
            JButton Cancel = new JButton("Cancel");
            Cancel.setLocation(260, 75);
            Cancel.setSize(160, 20);
            Read.add(Cancel);
            Cancel.addActionListener(a -> {
                Read.dispose();
            });
        } else {
            forbidden();
        }
        /*if (useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) {
                String fileName = JOptionPane.showInputDialog(this, "Input save's name to read save(don't need include filename extension)(we don't take responsibility if you change the save illegally)");
                this.readFileData(fileName);
            } else {
                forbidden();
            }*/
    }

    public void SaveGUI(Player p1, Player p2) {
        if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
            JFrame Save = new JFrame();
            Save.setTitle("Save");
            Save.setLocation(550, 300);
            Save.setSize(450, 150);
            Save.setVisible(true);
            Save.setLayout(null);

            JLabel txt1 = new JLabel();
            txt1.setText("Input save's name to save game(don't need include filename extension)");
            txt1.setSize(420, 20);
            txt1.setLocation(5, 5);
            txt1.setVisible(true);
            Save.add(txt1);
            JTextField save = new JTextField();
            save.setLocation(5, 40);
            save.setSize(420, 20);
            Save.add(save);
            JButton OK = new JButton("OK");
            OK.setLocation(5, 75);
            OK.setSize(160, 20);
            Save.add(OK);
            OK.addActionListener(a -> {
                if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                    String fileName = save.getText();
                    if (fileName == null || fileName.equals("")) {
                        JFrame WrongInput = new JFrame();
                        WrongInput.setTitle("Wrong Input!");
                        WrongInput.setLocation(550, 300);
                        WrongInput.setSize(400, 160);
                        WrongInput.setVisible(true);
                        WrongInput.setLayout(null);
                        JLabel txt4 = new JLabel();
                        txt4.setText("Please input name correctly (No empty name or etc.)");
                        txt4.setSize(320, 60);
                        txt4.setLocation(5, 5);
                        txt4.setVisible(true);
                        WrongInput.add(txt4);
                    } else {
                        this.writeDataToFile(fileName, p1, p2);
                        Save.dispose();
                    }
                } else forbidden();
            });
            JButton Cancel = new JButton("Cancel");
            Cancel.setLocation(260, 75);
            Cancel.setSize(160, 20);
            Save.add(Cancel);
            Cancel.addActionListener(a -> {
                Save.dispose();
            });
        } else {
            forbidden();
        }
    }

    public void settings() {
        JFrame setting = new JFrame();
        setting.setTitle("Settings");
        setting.setLocation(550, 300);
        setting.setSize(400, 400);
        setting.setVisible(true);
        setting.setLayout(null);

        JLabel txt1 = new JLabel();
        txt1.setText("Setting difficulty:");
        txt1.setSize(100, 20);
        txt1.setLocation(5, 5);
        txt1.setVisible(true);
        setting.add(txt1);
        JButton easyBtn = new JButton("easy");
        easyBtn.setSize(80, 20);
        easyBtn.setLocation(5, 30);
        setting.add(easyBtn);
        easyBtn.addActionListener(a -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                grids.clear();
                turnNum = 0;
                doSomething(9, 9, 10);
                this.dispose();
                setting.dispose();
            } else forbidden();
        });
        JButton normalBtn = new JButton("normal");
        normalBtn.setSize(80, 20);
        normalBtn.setLocation(95, 30);
        setting.add(normalBtn);
        normalBtn.addActionListener(a -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                grids.clear();
                turnNum = 0;
                doSomething(16, 16, 40);
                this.dispose();
                setting.dispose();
            } else forbidden();
        });
        JButton hardBtn = new JButton("hard");
        hardBtn.setSize(80, 20);
        hardBtn.setLocation(185, 30);
        setting.add(hardBtn);
        hardBtn.addActionListener(a -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                grids.clear();
                turnNum = 0;
                doSomething(16, 30, 99);
                this.dispose();
                setting.dispose();
            } else forbidden();
        });
        JButton customBtn = new JButton("custom");
        customBtn.setSize(80, 20);
        customBtn.setLocation(275, 30);
        setting.add(customBtn);
        customBtn.addActionListener(a -> {
            JFrame set_custom = new JFrame();
            set_custom.setTitle("set customized variables");
            set_custom.setLocation(550, 300);
            set_custom.setSize(400, 160);
            set_custom.setVisible(true);
            set_custom.setLayout(null);
            JLabel txt2 = new JLabel();
            txt2.setText("<html><body>Set customized variables:" +
                    "<br>Please input row, column, mine_number in the three TexTField below correctly.<html><body>");
            txt2.setSize(240, 50);
            txt2.setLocation(5, 5);
            txt2.setVisible(true);
            set_custom.add(txt2);

            JTextField rowText = new JTextField();
            rowText.setSize(50, 20);
            rowText.setLocation(5, 60);
            set_custom.add(rowText);
            JTextField columnText = new JTextField();
            columnText.setSize(50, 20);
            columnText.setLocation(60, 60);
            set_custom.add(columnText);
            JTextField mineCountText = new JTextField();
            mineCountText.setSize(50, 20);
            mineCountText.setLocation(115, 60);
            set_custom.add(mineCountText);


            JButton customCancel = new JButton("Cancel");
            customCancel.setSize(80, 20);
            customCancel.setLocation(260, 60);
            set_custom.add(customCancel);
            customCancel.addActionListener(b -> {
                set_custom.dispose();
            });

            JButton customOK = new JButton("OK");
            customOK.setSize(80, 20);
            customOK.setLocation(170, 60);
            set_custom.add(customOK);
            customOK.addActionListener(b -> {
                if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                    boolean bo = true;
                    int row = 0, col = 0, mine_count = 0;
                    try {
                        row = Integer.parseInt(rowText.getText());
                        if (row >= 9 & row <= 24)
                            row = row;
                        else bo = false;
                    } catch (Exception exception) {
                        bo = false;
                    }
                    try {
                        col = Integer.parseInt(columnText.getText());
                        if (col >= 9 & col <= 30)
                            col = col;
                        else bo = false;
                    } catch (Exception exception) {
                        bo = false;
                    }
                    try {
                        mine_count = Integer.parseInt(mineCountText.getText());
                        if (mine_count >= 1 & mine_count <= row * col / 2)
                            mine_count = mine_count;
                        else bo = false;
                    } catch (Exception exception) {
                        bo = false;
                    }
                    if (bo) {
                        grids.clear();
                        turnNum = 0;
                        doSomething(row, col, mine_count);
                        this.dispose();
                        set_custom.dispose();
                        setting.dispose();
                    } else {
                        JFrame WrongInput = new JFrame();
                        WrongInput.setTitle("Wrong Input!");
                        WrongInput.setLocation(550, 300);
                        WrongInput.setSize(400, 160);
                        WrongInput.setVisible(true);
                        WrongInput.setLayout(null);
                        JLabel txt4 = new JLabel();
                        txt4.setText("<html><body>Please input correctly<br>(row[9,24], column[9,30], mineCount[1,half of row*column]).<html><body>");
                        txt4.setSize(200, 60);
                        txt4.setLocation(5, 5);
                        txt4.setVisible(true);
                        WrongInput.add(txt4);
                    }
                } else forbidden();
            });
        });

        JLabel txt2 = new JLabel();
        txt2.setText("<html><body>Set one round click number:" +
                "<br>Input number to set one round click number(range:1-5)<html><body>");
        txt2.setSize(200, 45);
        txt2.setLocation(5, 65);
        txt2.setVisible(true);
        setting.add(txt2);

        JTextField ClickNum = new JTextField();
        ClickNum.setSize(50, 20);
        ClickNum.setLocation(200, 80);
        ClickNum.setText("1");
        setting.add(ClickNum);

        JLabel txt3 = new JLabel();                                    //new 设置每次点击时间限制，0代表无限制
        txt3.setText("<html><body>Set one step times:" +
                "<br>Input number to set one step times(0or3-100) (s)" +
                "<br>0 means doesn't have time limit one step<html><body>");
        txt3.setSize(280, 65);
        txt3.setLocation(5, 110);
        txt3.setVisible(true);
        setting.add(txt3);

        JTextField StepTime = new JTextField();
        StepTime.setSize(50, 20);
        StepTime.setLocation(280, 135);
        StepTime.setText("15");
        setting.add(StepTime);

        JLabel txt5 = new JLabel();                                    //new sound设置
        txt5.setText("<html><body>Set sound:" +
                "<br>tick to turn on sound else turn off<body>");
        txt5.setSize(200, 45);
        txt5.setLocation(5, 165);
        txt5.setVisible(true);
        setting.add(txt5);

        JCheckBox chk_sound = new JCheckBox("", true);
        chk_sound.setSize(50, 50);
        chk_sound.setLocation(200, 170);
        setting.add(chk_sound);

        JLabel txt6 = new JLabel();                                    //new 玩家名字设置
        txt6.setText("<html><body>Set name: (up p1, down p2)" +
                "<br>if empty, generate randomly. p1,p2 can't have same name<body>");
        txt6.setSize(200, 45);
        txt6.setLocation(5, 210);
        txt6.setVisible(true);
        setting.add(txt6);

        JTextField P1_name = new JTextField();
        P1_name.setSize(50, 20);
        P1_name.setLocation(260, 210);
        P1_name.setText("");
        setting.add(P1_name);
        JTextField P2_name = new JTextField();
        P2_name.setSize(50, 20);
        P2_name.setLocation(260, 235);
        P2_name.setText("");
        setting.add(P2_name);

        JLabel txt7 = new JLabel();                                    //new 设置主题
        txt7.setText("Set theme:");
        txt7.setSize(100, 20);
        txt7.setLocation(5, 260);
        txt7.setVisible(true);
        setting.add(txt7);

        JComboBox cmb = new JComboBox();    //创建JComboBox
        cmb.addItem("扫雷主题");    //向下拉列表中添加一项
        cmb.addItem("花园主题");
        cmb.setLocation(130, 265);
        cmb.setSize(100, 20);
        setting.add(cmb);

        JButton settingOK = new JButton("OK");
        settingOK.setSize(80, 20);
        settingOK.setLocation(270, 270);
        setting.add(settingOK);
        settingOK.addActionListener(b -> {
            if ((useAI == 0 || !MainFrame.controller.getOnTurnPlayer().equals(MainFrame.controller.getP2())) && XRay_Btn_Click_Turn % 2 == 0) {
                boolean bo = true;
                int step_time = 15;
                if (ClickNum.getText().equals("1") || ClickNum.getText().equals("2") || ClickNum.getText().equals("3") || ClickNum.getText().equals("4") || ClickNum.getText().equals("5")) {

                } else bo = false;

                try {
                    step_time = Integer.parseInt(StepTime.getText());
                    if ((step_time >= 3 && step_time <= 100) || step_time == 0) {

                    } else bo = false;
                } catch (Exception exception) {
                    bo = false;
                }
                if (P1_name.getText() != null && !P1_name.getText().equals("") && P2_name.getText() != null && !P2_name.getText().equals("")) {
                    if (P1_name.getText().equals(P2_name.getText()))
                        bo = false;
                }

                if (bo) {
                    Click_times = Integer.parseInt(ClickNum.getText());
                    GameController.timeOfaStep = step_time * 1000;
                    if (chk_sound.isSelected()) {
                        sound = 1;
                    } else sound = 2;
                    P1_Name = P1_name.getText();
                    P2_Name = P2_name.getText();
                    if (cmb.getSelectedItem().toString().equals("扫雷主题"))
                        Theme = 0;
                    else Theme = 1;
                    setTime(0);
                    this.dispose();
                    setting.dispose();
                    GamePanel.turnNum = 0;
                    grids.clear();
                    doSomething(xCount, yCount, mineCount);

                } else {
                    JFrame WrongInput = new JFrame();
                    WrongInput.setTitle("Wrong Input!");
                    WrongInput.setLocation(550, 330);
                    WrongInput.setSize(350, 80);
                    WrongInput.setVisible(true);
                    WrongInput.setLayout(null);
                    JLabel txt4 = new JLabel();
                    txt4.setText("Please input correctly!!!");
                    txt4.setSize(200, 20);
                    txt4.setLocation(5, 5);
                    txt4.setVisible(true);
                    WrongInput.add(txt4);
                }
            } else forbidden();
        });

        JButton settingCancel = new JButton("Cancel");
        settingCancel.setSize(80, 20);
        settingCancel.setLocation(270, 300);
        setting.add(settingCancel);
        settingCancel.addActionListener(a -> {
            setting.dispose();
        });
    }

    public void readFileData(String fileName) {
        //todo: read date from file

        try {
            readFile = 1;
            FileReader fr = new FileReader(path + "/saves/" + fileName + ".txt");
            BufferedReader reader = new BufferedReader(fr);
            this.xCount = Integer.valueOf(reader.readLine());
            this.yCount = Integer.valueOf(reader.readLine());
            Click_times = Integer.valueOf(reader.readLine());
            this.mineCount = Integer.valueOf(reader.readLine());
            remainMines = Integer.valueOf(reader.readLine());
            turnNum = Integer.valueOf(reader.readLine());
            useAI = Integer.valueOf(reader.readLine());
            numOfGrids = reader.readLine();
            statusOfGrids = reader.readLine();
            player1.setUserName(reader.readLine());
            player2.setUserName(reader.readLine());
            player1.setScore(Integer.valueOf(reader.readLine()));
            player2.setScore(Integer.valueOf(reader.readLine()));
            player1.setMistake(Integer.valueOf(reader.readLine()));
            player2.setMistake(Integer.valueOf(reader.readLine()));
            GameController.timeOfaStep = Integer.valueOf(reader.readLine());
            hasWin = Boolean.parseBoolean(reader.readLine());
            step_Left = reader.readLine();
            OnePlayer = Boolean.parseBoolean(reader.readLine());
            if(OnePlayer)
            time=Integer.valueOf(reader.readLine());
            grids = new ArrayList<GridComponent>();
            this.dispose();
            doSomething(this.xCount, this.yCount, this.mineCount);

        } catch (Exception exception) {              //rewrite
            //System.out.println("There is no this file!");
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

    public void writeDataToFile(String fileName, Player p1, Player p2) {
        //todo: write data into file
        try {
            FileWriter fw = new FileWriter( path+"/saves/" + fileName + ".txt");
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(String.valueOf(X));//行数
            writer.newLine();
            writer.write(String.valueOf(Y));//列数
            writer.newLine();
            writer.write(String.valueOf(Click_times));//单回合次数
            writer.newLine();
            writer.write(String.valueOf(this.mineCount));
            writer.newLine();
            writer.write(String.valueOf(MineCount));//剩余的雷数
            writer.newLine();
            writer.write(String.valueOf(turnNum));//已进行的步数
            writer.newLine();
            writer.write(String.valueOf(useAI));//是否为人机模式,新加
            writer.newLine();
            String s1 = new String();//读格子的数字，-1用！代替
            String s2 = new String();//读格子的状态，covered用c代替，Clicked用u代替，Flag用f代替
            for (int i = 0; i < grids.size(); i++) {
                if (i == 0) {
                    if (grids.get(i).getContent() != -1)
                        s1 = String.valueOf(grids.get(i).getContent());
                    else
                        s1 = "!";
                    if (grids.get(i).getGridStatus() == GridStatus.Clicked)
                        s2 = "u";
                    else if (grids.get(i).getGridStatus() == GridStatus.Covered)
                        s2 = "c";
                    else
                        s2 = "f";
                } else {
                    if (grids.get(i).getContent() != -1)
                        s1 += String.valueOf(grids.get(i).getContent());
                    else
                        s1 += "!";
                    if (grids.get(i).getGridStatus() == GridStatus.Clicked)
                        s2 += "u";
                    else if (grids.get(i).getGridStatus() == GridStatus.Covered)
                        s2 += "c";
                    else
                        s2 += "f";
                }
            }
            writer.write(s1);
            writer.newLine();
            writer.write(s2);
            writer.newLine();
            writer.write(p1.getUserName());//玩家一名字
            writer.newLine();
            writer.write(p2.getUserName());//玩家二名字
            writer.newLine();
            writer.write(String.valueOf(p1.getScore()));//玩家一分数
            writer.newLine();
            writer.write(String.valueOf(p2.getScore()));//玩家二分数
            writer.newLine();
            writer.write(String.valueOf(p1.getMistake()));//玩家一失误
            writer.newLine();
            writer.write(String.valueOf(p2.getMistake()));//玩家二失误
            writer.newLine();
            writer.write(String.valueOf(GameController.timeOfaStep));//每步时间限制
            writer.newLine();
            writer.write(String.valueOf(hasWin));//是否已经赢
            writer.newLine();
            writer.write(String.valueOf(l2.getText()));
            writer.newLine();
            writer.write(String.valueOf(OnePlayer));
            writer.newLine();
            if(OnePlayer)
            {
                writer.write(String.valueOf(time));
                writer.newLine();
            }
            writer.close();
        } catch (Exception exception) {
            //System.out.println("There is no this file!");
            JFrame WrongInput = new JFrame();
            WrongInput.setTitle("Wrong");
            WrongInput.setLocation(550, 330);
            WrongInput.setSize(350, 80);
            WrongInput.setVisible(true);
            WrongInput.setLayout(null);
            JLabel txt4 = new JLabel();
            txt4.setText("There is no file folder");
            txt4.setSize(200, 20);
            txt4.setLocation(5, 5);
            txt4.setVisible(true);
            WrongInput.add(txt4);
        }
    }

}
