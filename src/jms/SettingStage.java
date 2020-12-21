package jms;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jms.message.*;
import jms.ui.Gobang;
import jms.ui.GobangFight;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class SettingStage extends Stage {
    public SettingStage() {
        double x = 80.0;
        double y = 40.0;
        double fontSize = 20.0;
        double intervalX = 95.0;
        double intervalY = 75.0;

        // 创建画布
        Pane pane = new Pane();

        double scene_width = 400;
        double scene_height = 400;
        Scene scene = new Scene(pane, scene_width, scene_height);

        // 添加文本框
        Label my_IP_label = getLabel(x, y, fontSize, "我的IP：");
        Label port_label = getLabel(x, y + intervalY, fontSize, "我的端口：");
        Label opponent_IP_label = getLabel(x, y + intervalY * 2, fontSize, "对手IP：");
        Label opponent_port_label = getLabel(x, y + intervalY * 3, fontSize, "对手端口：");

        pane.getChildren().addAll(my_IP_label, port_label, opponent_IP_label, opponent_port_label);

        // 添加输入框
        TextField myIpText = getText(x + intervalX, y);
        TextField myPortText = getText(x + intervalX, y + intervalY);
        TextField opponentIpText = getText(x + intervalX, y + intervalY * 2);
        TextField opponentPortText = getText(x + intervalX, y + intervalY * 3);

        pane.getChildren().addAll(myIpText, myPortText, opponentIpText, opponentPortText);

        // 设置按钮
        double btnWidth = 65.0;
        double btnHeight = 45.0;
        Button determine = Gobang.getBtn(btnWidth, btnHeight, scene_width / 2 - btnWidth / 2 - 60, 320, "确定");
        Button cancel = Gobang.getBtn(btnWidth, btnHeight, scene_width / 2 - btnWidth / 2 + btnWidth, 320, "取消");

        pane.getChildren().addAll(determine, cancel);

        determine.setOnAction(event -> {
            // 获取文本框中的数据
            Global.my_ip = myIpText.getText();
            int my_port_text = Integer.parseInt(myPortText.getText());
            Global.my_port = my_port_text;
            Global.opponent_ip = opponentIpText.getText();
            Global.opponent_port = Integer.parseInt(opponentPortText.getText());

            GobangFight gobang = new GobangFight();
            SettingStage.this.close();

            new Thread(() -> {
                // 接收 ip 和 port
                ServerSocket serverSocket;
                Socket accept;
                ObjectInputStream ois;
                try {
                    serverSocket = new ServerSocket(my_port_text);
                    while (true) {
                        accept = serverSocket.accept();

                        ois = new ObjectInputStream(accept.getInputStream());
                        Object obj = ois.readObject();

                        gobang.update((Message) obj);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        cancel.setOnAction(event -> this.close());

        // 设置 Stage 关闭按钮作用
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        this.setOnCloseRequest(event -> {
            alert.setTitle("提示");
            alert.setHeaderText("确定要退出吗？");
            Optional<ButtonType> optional = alert.showAndWait();
            if (optional.orElse(null) == ButtonType.OK) System.exit(0);
            else event.consume();
        });

        this.setScene(scene);

        this.show();
    }

    private Label getLabel(double x, double y, double font, String name) {
        Label label = new Label(name);
        label.setFont(Font.font(font));
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    private TextField getText(double x, double y) {
        TextField textField = new TextField();
        textField.setLayoutX(x);
        textField.setLayoutY(y);

        return textField;
    }
}
