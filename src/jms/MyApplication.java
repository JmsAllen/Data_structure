package jms;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jms.ui.Gobang;

public class MyApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        final int SCENE_WIDTH = 400;
        final int SCENE_HEIGHT = 200;
        final int BTN_WIDTH = 100;
        final int BTN_HEIGHT = 80;
        final int MARGIN = 40;

        Pane pane = new Pane();

        Scene scene = new Scene(pane, SCENE_WIDTH, SCENE_HEIGHT);

        // 创建主界面按钮  单机版--对战版--网络版
        double btnX = SCENE_WIDTH / 2.0 - BTN_WIDTH / 2.0;
        double btnY = SCENE_HEIGHT / 2.0 - MARGIN;
        Button singletonBtn = Gobang.getBtn(BTN_WIDTH, BTN_HEIGHT,btnX - BTN_WIDTH - MARGIN, btnY, "单机版");
        Button fightBtn = Gobang.getBtn(BTN_WIDTH, BTN_HEIGHT, btnX, btnY, "对战版");
        Button netBtn = Gobang.getBtn(BTN_WIDTH, BTN_HEIGHT, btnX + BTN_WIDTH + MARGIN, btnY, "网络版");

        singletonBtn.setOnAction(event -> {
            new Gobang();
            primaryStage.close();
        });

        fightBtn.setOnAction(event -> {
            new SettingStage();
            primaryStage.close();
        });

        netBtn.setOnAction(event -> System.out.println("网络版"));

        pane.getChildren().addAll(singletonBtn, fightBtn, netBtn);

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
