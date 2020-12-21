package jms.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jms.bean.Chess;
import jms.message.*;
import jms.utils.NetUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class GobangFight extends Stage {
    private boolean me;
    private int restoreCnt;
    private int width;
    private int height;
    private int padding;
    private int margin;
    private boolean _win = false;
    private int lineCount;
    private boolean isBlack = true;
    private final int RADIUS = 15;
    private Chess[][] chessList;
    private boolean replay = true;
    private boolean canplay = true;
    private Stage stage;
    private Pane pane;
    private List<Chess> chessQueue = new ArrayList<>();

    private void regret() {
        Node node = pane.getChildren().get(pane.getChildren().size() - 1);
        if (node instanceof Circle) {
            isBlack = !isBlack;
            Circle circle = (Circle) node;
            int _x = ((int) circle.getCenterX() - margin + padding / 2) / padding;
            int _y = ((int) circle.getCenterY() - margin + padding / 2) / padding;
            chessList[_x][_y] = null;
            pane.getChildren().remove(pane.getChildren().size() - 1);
        }
    }

    public void update(Message message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (message instanceof ChessMessage) {
                    ChessMessage chessMessage = (ChessMessage) message;

                    int _x = chessMessage.getX();
                    int _y = chessMessage.getY();

                    Chess chess = new Chess();
                    try {
                        if (isHasPiece(_x, _y)) {
                            if (chessMessage.isBlack()) {
                                chess = new Chess(new Circle(_x * padding + margin, _y * padding + margin, RADIUS, Color.BLACK), _x, _y);
                                isBlack = false;
                            } else {
                                chess = new Chess(new Circle(_x * padding + margin, _y * padding + margin, RADIUS, Color.WHITE), _x, _y);
                                isBlack = true;
                            }
                            chessList[chess.getX()][chess.getY()] = chess;
                            pane.getChildren().add(chess.getCircle());

                        }
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("警告");
                        alert.setHeaderText("无效操作");
                        alert.setContentText("请将棋子落在有效区域");
                        alert.showAndWait();
                        return;
                    }

                    // 判断输赢
                    if (chess.getCircle() == null) return;
                    if (isWin(chess)) {
                        WinMessage winMessage = new WinMessage();
                        winMessage.setFlag(WinMessage.WIN);
                        NetUtils.sendMessage(winMessage);

                        _win = true;
                    }
                }

                if (message instanceof ReplayMessage) {
                    ReplayMessage replayMessage = (ReplayMessage) message;
                    int flag = replayMessage.getFlag();
                    if (flag == ReplayMessage.REQUEST) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "是否同意对方悔棋？");
                        Optional<ButtonType> optional = alert.showAndWait();
                        if (optional.orElse(null) == ButtonType.OK) {
                            replayMessage.setFlag(ReplayMessage.AGREE);
                        } else {
                            replayMessage.setFlag(ReplayMessage.REFUSE);
                        }
                        NetUtils.sendMessage(replayMessage);
                    } else if (flag == ReplayMessage.AGREE) {
                        // 对方同意悔棋
                        replay = true;
                        regret();
                        NetUtils.sendMessage(new RegretChessMassage());
                    } else if (flag == ReplayMessage.REFUSE) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "对方不同意悔棋");
                        alert.initOwner(stage);
                        alert.show();
                    }
                }

                if (message instanceof RegretChessMassage) {
                    regret();
                }

                if (message instanceof WinMessage) {
                    WinMessage winMessage = (WinMessage) message;
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    if (winMessage.getFlag() == WinMessage.FAILURE) {
                        alert.setTitle("提示");
                        alert.setContentText("失败了");
                        alert.show();
                    }
                }

                if (message instanceof AgainMessage) {
                    AgainMessage againMessage = (AgainMessage) message;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    if (againMessage.getFlag() == AgainMessage.REQUEST) {
                        alert.setTitle("提示");
                        alert.setContentText("对方邀请再来一局");
                        Optional<ButtonType> optional = alert.showAndWait();
                        if (optional.orElse(null) == ButtonType.OK) {
                            againMessage.setFlag(AgainMessage.AGREE);
                            NetUtils.sendMessage(againMessage);
                            resetCheckerboard(pane);
                        } else {
                            againMessage.setFlag(AgainMessage.REFUSE);
                            Alert refuse = new Alert(Alert.AlertType.INFORMATION);
                            refuse.setTitle("提示");
                            refuse.setContentText("对方拒绝了邀请");
                            refuse.showAndWait();
                            NetUtils.sendMessage(againMessage);
                        }
                    } else if (againMessage.getFlag() == AgainMessage.AGREE) {
                        resetCheckerboard(pane);
                    }
                }

                if (message instanceof ExitMessage) {
                    ExitMessage exitMessage = (ExitMessage) message;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    if (exitMessage.getFlag() == AgainMessage.REQUEST) {
                        alert.setTitle("提示");
                        alert.setContentText("对方想要逃跑");
                        Optional<ButtonType> optional = alert.showAndWait();
                        if (optional.orElse(null) == ButtonType.OK) {
                            exitMessage.setFlag(AgainMessage.AGREE);
                            NetUtils.sendMessage(exitMessage);
                            return;
                        } else {
                            exitMessage.setFlag(AgainMessage.REFUSE);
                            NetUtils.sendMessage(exitMessage);
                            return;
                        }
                    } else if (exitMessage.getFlag() == AgainMessage.AGREE) {
                        System.exit(0);
                    } else if (exitMessage.getFlag() == AgainMessage.REFUSE) {
                        Alert refuse = new Alert(Alert.AlertType.INFORMATION);
                        refuse.setTitle("提示");
                        refuse.setContentText("对方不允许你逃跑！");
                        refuse.show();
                    }
                }

                if (message instanceof RoundsMessage) {
                    RoundsMessage roundsMessage = (RoundsMessage) message;
                    if (!me) {
                        me = !me;
                        return;
                    }
                }
            }
        });
    }

    public boolean isHasPiece(int x, int y) throws Exception {
        try {
            return chessList[x][y] == null;
        } catch (RuntimeException e) {
            throw new Exception();
        }
    }

    public GobangFight() {
        // 画布宽度
        this.width = 600;
        // 画布高度
        this.height = 600;
        // 棋盘每格宽度
        this.padding = 40;
        // 棋盘和画布之间的宽度
        this.margin = 20;
        // 棋盘长宽线段的数量
        this.lineCount = 14;
        // 记录棋盘
        this.chessList = new Chess[lineCount][lineCount];

        this.stage = this;
        // 轮次
        this.me = true;
        pane = getPane();

        Scene scene = new Scene(pane, width, height);

        playChess();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        stage.setOnCloseRequest(event -> {
            alert.setTitle("提示");
            alert.setHeaderText("确定要退出吗？");
            alert.initOwner(stage);
            Optional<ButtonType> optional = alert.showAndWait();
            if (optional.orElse(null) == ButtonType.OK) System.exit(0);
            else event.consume();
        });

        stage.setScene(scene);

        stage.show();
    }

    public static Button getBtn(double width, double height, double x, double y, String text) {
        Button button = new Button(text);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefSize(width, height);
        return button;
    }

    public Pane getPane() {
        Pane pane = new Pane();
        Line line_row;
        Line line_col;
        int increment = 0;

        // 设置棋盘线条
        for (int i = 0; i < lineCount; i++) {
            line_row = new Line(margin, margin + increment, width - margin - padding, margin + increment);
            line_col = new Line(margin + increment, margin, margin + increment, height - padding - margin);
            pane.getChildren().add(line_row);
            pane.getChildren().add(line_col);

            increment += padding;
        }

        // 设置背景颜色 #8C793F
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(140, 121, 63), null, null)));

        // 设置按钮
        final int BTN_WIDTH = 80;
        final int BTN_HEIGHT = 30;

        setBtn(pane, getStartBtn(BTN_WIDTH, BTN_HEIGHT));
        setBtn(pane, getRegretBtn(BTN_WIDTH, BTN_HEIGHT));
        setBtn(pane, getSaveBtn(BTN_WIDTH, BTN_HEIGHT));
//        setBtn(pane, getRestoreBtn(BTN_WIDTH, BTN_HEIGHT));
        setBtn(pane, getExitBtn(BTN_WIDTH, BTN_HEIGHT));

        return pane;
    }

    private void setBtn(Pane pane, Button button) {
        pane.getChildren().add(button);
    }

    public void playChess() {
        pane.setOnMouseClicked(event -> {
            if (_win) {
                return;
            }

            if (!me) {
                return;
            } else {
                RoundsMessage roundsMessage = new RoundsMessage();
                me = false;
                NetUtils.sendMessage(roundsMessage);
            }

            double x = event.getX();
            double y = event.getY();

            int _x = ((int) x - margin + padding / 2) / padding;
            int _y = ((int) y - margin + padding / 2) / padding;

            Chess chess = new Chess();
            try {
                if (isHasPiece(_x, _y)) {
                    if (isBlack) {
                        chess = new Chess(new Circle(_x * padding + margin, _y * padding + margin, RADIUS, Color.BLACK), _x, _y);
                        isBlack = false;
                    } else {
                        chess = new Chess(new Circle(_x * padding + margin, _y * padding + margin, RADIUS, Color.WHITE), _x, _y);
                        isBlack = true;
                    }
                    chessList[chess.getX()][chess.getY()] = chess;
                    pane.getChildren().add(chess.getCircle());

                    // 发送数据
                    NetUtils.sendMessage(new ChessMessage(_x, _y, !isBlack));
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("警告");
                alert.setHeaderText("无效操作");
                alert.setContentText("请将棋子落在有效区域");
                alert.showAndWait();
                return;
            }

            // 判断输赢
            if (chess.getCircle() == null) return;
            if (isWin(chess)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "胜利了");
                alert.setTitle("提示");
                alert.show();

                WinMessage winMessage = new WinMessage();
                winMessage.setFlag(WinMessage.FAILURE);
                NetUtils.sendMessage(winMessage);
                _win = true;
            }
        });
    }

    /**
     * @return 1 = 黑色， 2 = 白色，0 = 无棋子
     */
    public int getColor(Chess chess) {
        if (chess == null) {
            return 0;
        }
        Color fill = (Color) chess.getCircle().getFill();
        String s = fill.toString();
        if (s.equals("0x000000ff")) {
            chess.setColor(1);
            return 1;
        }
        chess.setColor(2);
        return 2;
    }

    public boolean isWin(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        final int SIZE = 4;
        boolean[] result = new boolean[SIZE];

        // 横向判断
        result[0] = judgeRow(chess, x, y);

        // 纵向判断
        result[1] = judgeCol(chess, x, y);

        // 左斜向判断
        result[2] = judgeLeftOblique(chess, x, y);

        // 右斜向判断
        result[3] = judgeRightOblique(chess, x, y);

        for (int i = 0; i < SIZE; i++) {
            if (result[i]) return true;
        }
        return false;
    }

    public boolean judgeRightOblique(Chess chess, int x, int y) {
        int cnt = 1;
        int color = getColor(chess);

        // 右斜——右上判断
        for (int i = x + 1, j = y - 1; i < lineCount && j >= 0; i++, j--) {
            if (color == getColor(chessList[i][j])) cnt++;
            else break;
        }

        // 右斜——左下判断
        for (int i = x - 1, j = y + 1; i >= 0 && j < lineCount; i--, j++) {
            if (color == getColor(chessList[i][j])) cnt++;
            else break;
        }
        return cnt >= 5;
    }

    public boolean judgeLeftOblique(Chess chess, int x, int y) {
        int cnt = 1;
        int color = getColor(chess);

        // 左斜——左上判断
        for (int i = x - 1, j = y - 1; i >= 0 && j >= 0; i--, j--) {
            if (color == getColor(chessList[i][j])) cnt++;
            else break;
        }

        // 左斜——右下判断
        for (int i = x + 1, j = y + 1; i < lineCount && j < lineCount; i++, j++) {
            if (color == getColor(chessList[i][j])) cnt++;
            else break;
        }
        return cnt >= 5;
    }

    public boolean judgeCol(Chess chess, int x, int y) {
        int cnt = 1;
        int color = getColor(chess);

        // 向下判断
        for (int i = y + 1; i < lineCount; i++) {
            if (color == getColor(chessList[x][i])) cnt++;
            else break;
        }

        // 向上判断
        for (int i = y - 1; i >= 0; i--) {
            if (color == getColor(chessList[x][i])) cnt++;
            else break;
        }
        return cnt >= 5;
    }

    public boolean judgeRow(Chess chess, int x, int y) {
        int cnt = 1;
        int color = getColor(chess);

        // 向右判断
        for (int i = x + 1; i < lineCount; i++) {
            if (color == getColor(chessList[i][y])) cnt++;
            else break;
        }

        // 向左判断
        for (int i = x - 1; i >= 0; i--) {
            if (color == getColor(chessList[i][y])) cnt++;
            else break;
        }

        return cnt >= 5;
    }

    /**
     * 重置棋盘
     */
    private void resetCheckerboard(Pane pane) {
        if (!_win) return;
        _win = false;
        isBlack = true;
        chessList = new Chess[lineCount][lineCount];
        pane.getChildren().removeIf(node -> node instanceof Circle);
    }

    public Button getStartBtn(double b_width, double b_height) {
        Button startBtn = getBtn(b_width, b_height, margin, height - padding, "再来一局");
        startBtn.setOnAction(event -> {
            if (!_win) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "按钮此局结束后再点击");
                alert.show();
                return;
            }

            AgainMessage againMessage = new AgainMessage();
            againMessage.setFlag(AgainMessage.REQUEST);
            NetUtils.sendMessage(againMessage);
//            resetCheckerboard(pane);
        });
        return startBtn;
    }

    public Button getRegretBtn(double b_width, double b_height) {
        Button regretBtn = getBtn(b_width, b_height, margin + b_width + b_height, height - padding, "悔棋");

        regretBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (_win) return;

                // 发送悔棋信息
                ReplayMessage replayMessage = new ReplayMessage();
                replayMessage.setFlag(ReplayMessage.REQUEST);
                if(replay) {
                    NetUtils.sendMessage(replayMessage);
                    replay = false;
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "无法再次悔棋");
                    alert.setTitle("提示");
                    alert.show();
                    return;
                }
            }
        });
        return regretBtn;
    }

    public Button getSaveBtn(double b_width, double b_height) {
        Button saveBtn = getBtn(b_width, b_height, margin + (b_width + b_height) * 2, height - padding, "保存棋谱");
        saveBtn.setOnAction(event -> {
            if (!_win) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "棋局结束可保存棋谱");
                alert.show();
                return;
            }

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(stage);

            if (file == null) return;

            // 保存到硬盘
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                String format;
                Chess chess;
                Circle circle;
                for (int i = 0; i < lineCount - 1; i++) {
                    for (int j = 0; j < lineCount - 1; j++) {
                        chess = chessList[i][j];
                        if (chess != null) {
                            circle = chess.getCircle();
                            format = String.format("x=%d,y=%d,color=%d,centerX=%f,centerY=%f,radius=%f,fill=%s",
                                    chess.getX(), chess.getY(), chess.getColor(),
                                    circle.getCenterX(), circle.getCenterY(), circle.getRadius(), circle.getFill().toString());
                            bw.write(format);
                            bw.newLine();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return saveBtn;
    }

    /**
     * 获取打谱按钮
     */
    public Button getRestoreBtn(double b_width, double b_height) {
        Button restoreBtn = getBtn(b_width, b_height, margin + (b_width + b_height) * 3, height - padding, "打谱");
        restoreBtn.setOnAction(event -> {
            double btn_height = 35;
            double btn_width = 35;
            double btn_margin = 50;
            double interval = 50;
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText("打谱会重置棋盘，是否确定？");
            Optional<ButtonType> optional = alert.showAndWait();

            // 重置棋盘
            if (optional.orElse(null) == ButtonType.OK) {
                _win = true;
                resetCheckerboard(pane);
                // 重置棋盘后下面变量会重置为 false，打谱时不允许落子
                _win = true;
            } else event.consume();

            // 反序列化棋谱文件
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);

            String s;
            Chess chess = null;
            String key;
            String[] kv;

            int x = 0;
            int y = 0;
            int color = 0;
            double centerX = 0;
            double centerY = 0;
            double radius = 0;
            Paint fill = null;
            if (file == null) return;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // 获取Chess的属性
                while ((s = reader.readLine()) != null) {
                    kv = s.split(",");
                    for (String str : kv) {
                        String[] vals = str.split("=");
                        key = vals[0];
                        switch (key) {
                            case "x":
                                x = Integer.parseInt(vals[1]);
                                break;
                            case "y":
                                y = Integer.parseInt(vals[1]);
                                break;
                            case "color":
                                color = Integer.parseInt(vals[1]);
                                break;
                            case "centerX":
                                centerX = Double.parseDouble(vals[1]);
                                break;
                            case "centerY":
                                centerY = Double.parseDouble(vals[1]);
                                break;
                            case "radius":
                                radius = Double.parseDouble(vals[1]);
                                break;
                            case "fill":
                                if (vals[1].contains("0x000000ff")) {
                                    fill = Color.BLACK;
                                } else {
                                    fill = Color.WHITE;
                                }
                                break;
                        }
                        chess = new Chess(new Circle(centerX, centerY
                                , radius, fill), x, y, color);
                    }
                    chessQueue.add(chess);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 按钮置入
//            setBtn(pane, getAddPieceButton(pane, btn_width,
//                    btn_height, btn_margin, interval));
//            setBtn(pane, getReduceButton(pane, btn_width,
//                    btn_height, interval));
//            setBtn(pane, getCancelButton(pane, btn_width,
//                    btn_height, btn_margin, interval));
        });
        return restoreBtn;
    }

    private Button getReduceButton(Pane pane, double btn_width, double btn_height, double interval) {
        Button cancelBtn = getBtn(btn_width, btn_height, this.width - interval, height / 2.0, "<");
        cancelBtn.setOnAction(event -> {
            // 获取 pane 中的最后一个元素
            Node node = pane.getChildren().get(pane.getChildren().size() - 1);
            if (node instanceof Circle) {
                isBlack = !isBlack;
                Circle circle = (Circle) node;
                int _x = ((int) circle.getCenterX() - margin + padding / 2) / padding;
                int _y = ((int) circle.getCenterY() - margin + padding / 2) / padding;
                chessList[_x][_y] = null;
                pane.getChildren().remove(pane.getChildren().size() - 1);
                restoreCnt--;
            }
        });
        return cancelBtn;
    }

    public Button getCancelButton(Pane pane, double btn_width, double btn_height, double btn_margin, double interval) {
        Button cancelBtn = getBtn(btn_width, btn_height, this.width - interval, height / 2.0 + btn_margin, "X");
        cancelBtn.setOnAction(event -> {
            // 移除按钮
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText("是否确定结束打谱？");
            alert.setContentText("结束打谱后点击【再来一局】可开始新局");
            Optional<ButtonType> optional = alert.showAndWait();
            if (optional.orElse(null) == ButtonType.OK) {
                Node btn;
                int lastIndex = pane.getChildren().size() - 1;
                for (int i = 0; i < 3; i++) {
                    btn = pane.getChildren().get(lastIndex--);
                    pane.getChildren().remove(btn);
                }
            } else {
                event.consume();
                return;
            }

            // 移除已打谱的棋子
            for (int i = 0; i < restoreCnt; i++) {
                pane.getChildren().removeIf(node -> node instanceof Circle);
            }

            // 恢复默认计数器
            restoreCnt = 0;
        });

        return cancelBtn;
    }

    public Button getAddPieceButton(Pane pane, double b_width, double b_height, double margin, double interval) {
        Button addPiece = getBtn(b_width, b_height, this.width - interval, height / 2.0 - margin, ">");
        addPiece.setOnAction(event -> {
            if (restoreCnt < chessQueue.size()) {
                Chess chess = chessQueue.get(restoreCnt++);
                chessList[chess.getX()][chess.getY()] = chess;
                Circle circle = chess.getCircle();
                pane.getChildren().add(circle);
            }
        });

        return addPiece;
    }

    public Button getExitBtn(double b_width, double b_height) {
        Button exitBtn = getBtn(b_width, b_height, margin + (b_width + b_height) * 3, height - padding, "退出");

        exitBtn.setOnAction(new EventHandler<ActionEvent>() {
            boolean flag = true;

            @Override
            public void handle(ActionEvent event) {
                if (_win) return;
                ExitMessage exitMessage = new ExitMessage();
                exitMessage.setFlag(ExitMessage.REQUEST);
                if (flag) {
                    NetUtils.sendMessage(exitMessage);
                    flag = false;
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("退出请求只能发送一次");
                    alert.show();
                }
            }
        });
        return exitBtn;
    }
}
