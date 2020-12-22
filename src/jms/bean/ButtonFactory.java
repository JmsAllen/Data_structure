package jms.bean;

import jms.bean.asideButtonAbs.AsideButton;

public class ButtonFactory extends AsideButton {
    public ButtonFactory() {

    }

    public static AsideButton getAsideButton(double width, double height, double x, double y, String text) {
        AsideButton asideButton = new AsideButton(text);
        asideButton.setLayoutX(x);
        asideButton.setLayoutY(y);
        asideButton.setPrefSize(width, height);
        return asideButton;
    }
}
