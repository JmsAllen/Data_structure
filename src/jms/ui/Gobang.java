package jms.ui;

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
import jms.bean.ButtonFactory;
import jms.bean.Chess;
import jms.bean.asideButtonAbs.AsideButton;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Gobang extends Stage {
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
    private Stage stage;
    private List<Chess> chessQueue = new ArrayList<>();

    public boolean isHasPiece(int x, int y) throws Exception {
        try {
            return chessList[x][y] == null;
        } catch (RuntimeException e) {
            throw new Exception();
        }
    }

    public Gobang() {
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

        Pane pane = getPane();

        Scene scene = new Scene(pane, width, height);

        playChess(pane);

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
        // 横向线条
        Line line_row;
        // 纵向线条
        Line line_col;
        // 增量
        int increment = 0;

        // 设置棋盘线条
        for (int i = 0; i < lineCount; i++) {
            line_row = new Line(margin, margin + increment, width - margin - padding, margin + increment);
            line_col = new Line(margin + increment, margin, margin + increment, height - padding - margin);
            pane.getChildren().addAll(line_row, line_col);

            increment += padding;
        }

        // 设置背景颜色 #8C793F
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(140, 121, 63), null, null)));

        // 设置按钮
        final int BTN_WIDTH = 80;
        final int BTN_HEIGHT = 30;

        setBtn(pane, getStartBtn(pane, BTN_WIDTH, BTN_HEIGHT));
        setBtn(pane, getRegretBtn(pane, BTN_WIDTH, BTN_HEIGHT));
        setBtn(pane, getSaveBtn(BTN_WIDTH, BTN_HEIGHT));
        setBtn(pane, getRestoreBtn(pane, BTN_WIDTH, BTN_HEIGHT));
        setBtn(pane, getExitBtn(BTN_WIDTH, BTN_HEIGHT));

        return pane;
    }

    private void setBtn(Pane pane, Button button) {
        pane.getChildren().add(button);
    }

    public void playChess(Pane pane) {
        pane.setOnMouseClicked(event -> {
            if (_win) {
                return;
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
                System.out.println("win");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("提示");
                alert.setHeaderText("胜利了！");
                alert.showAndWait();
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

    /**
     * 判断右斜方向胜利
     *
     * @param chess
     * @param x
     * @param y
     * @return
     */
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

    /**
     * 判断左斜方向胜利
     *
     * @param chess
     * @param x
     * @param y
     * @return
     */
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

    /**
     * 判断列向胜利
     *
     * @param chess
     * @param x
     * @param y
     * @return
     */
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

    /**
     * 判断横向胜利
     *
     * @param chess
     * @param x
     * @param y
     * @return
     */
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
        // 重置 pane 中的棋子
        pane.getChildren().removeIf(node -> node instanceof Circle);
        // 清空 chessQueue 中的棋子
        chessQueue.clear();
    }

    public Button getStartBtn(Pane pane, double b_width, double b_height) {
        Button startBtn = getBtn(b_width, b_height, margin, height - padding, "再来一局");
        startBtn.setOnAction(event -> resetCheckerboard(pane));
        return startBtn;
    }

    /**
     * 悔棋
     *
     * @param pane
     * @param b_width
     * @param b_height
     * @return
     */
    public Button getRegretBtn(Pane pane, double b_width, double b_height) {
        Button regretBtn = getBtn(b_width, b_height, margin + b_width + b_height, height - padding, "悔棋");
        regretBtn.setOnAction(event -> {
            if (_win) return;

            Node node = pane.getChildren().get(pane.getChildren().size() - 1);
            if (node instanceof Circle) {
                isBlack = !isBlack;
                Circle circle = (Circle) node;
                int _x = ((int) circle.getCenterX() - margin + padding / 2) / padding;
                int _y = ((int) circle.getCenterY() - margin + padding / 2) / padding;
                chessList[_x][_y] = null;
                pane.getChildren().remove(pane.getChildren().size() - 1);
            }
        });
        return regretBtn;
    }

    /**
     * 保存棋谱
     *
     * @param b_width
     * @param b_height
     * @return
     */
    public Button getSaveBtn(double b_width, double b_height) {
        Button saveBtn = getBtn(b_width, b_height, margin + (b_width + b_height) * 2, height - padding, "保存棋谱");
        saveBtn.setOnAction(event -> {
            if (!_win) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("胜利后才可以保存棋谱");
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
    public Button getRestoreBtn(Pane pane, double b_width, double b_height) {
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
            setBtn(pane, getAddPieceButton(pane, btn_width,
                    btn_height, btn_margin, interval));
            setBtn(pane, getReduceButton(pane, btn_width,
                    btn_height, interval));
            setBtn(pane, getCancelButton(pane, btn_width,
                    btn_height, btn_margin, interval));


//            for (Chess ch : chessQueue) {
//                Circle circle = ch.getCircle();
//                pane.getChildren().add(circle);
//            }
            chessQueue.forEach(ele -> pane.getChildren().add(ele.getCircle()));
        });
        return restoreBtn;
    }

    /**
     * 打谱后回退棋子
     *
     * @param pane
     * @param btn_width
     * @param btn_height
     * @param interval
     * @return
     */
    @SuppressWarnings("all")
    private Button getReduceButton(Pane pane, double btn_width, double btn_height, double interval) {

        AsideButton cancelBtn = ButtonFactory.getAsideButton(btn_width, btn_height, this.width - interval, height / 2.0, "<");
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

        AsideButton cancelBtn = ButtonFactory.getAsideButton(btn_width, btn_height, this.width - interval, height / 2.0 + btn_margin, "X");

        cancelBtn.setOnAction(event -> {
            // 移除按钮
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText("是否确定结束打谱？");
            alert.setContentText("结束打谱后点击【再来一局】可开始新局");
            Optional<ButtonType> optional = alert.showAndWait();
            if (optional.orElse(null) == ButtonType.OK) {
                chessQueue.clear();
                pane.getChildren().removeIf(ele -> ele instanceof AsideButton);
            } else {
                event.consume();
                return;
            }

            // 移除已打谱的棋子
            for (int i = 0; i < restoreCnt; i++) {
                pane.getChildren().removeIf(node -> node instanceof Circle);
            }

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("点击【再来一局】开始新局");
            a.show();

            // 恢复默认计数器
            restoreCnt = 0;
        });

        return cancelBtn;
    }

    public Button getAddPieceButton(Pane pane, double b_width, double b_height, double margin, double interval) {
        AsideButton addPiece = ButtonFactory.getAsideButton(b_width, b_height, this.width - interval, height / 2.0 - margin, ">");
        restoreCnt = chessQueue.size();
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
        Button exitBtn = getBtn(b_width, b_height, margin + (b_width + b_height) * 4, height - padding, "退出");

        exitBtn.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText("确定要退出吗？");
            Optional<ButtonType> optional = alert.showAndWait();
            if (optional.orElse(null) == ButtonType.OK) {
                System.exit(0);
            } else {
                event.consume();
            }
        });
        return exitBtn;
    }

}
