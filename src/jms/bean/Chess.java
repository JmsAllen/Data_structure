package jms.bean;
import javafx.scene.shape.Circle;

@SuppressWarnings("all")
public class Chess extends Circle {
    private Circle circle = null;
    private int x;
    private int y;
    private int color;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Chess{");
        sb.append("circle=").append(circle);
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", color=").append(color);
        sb.append('}');
        return sb.toString();
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Chess() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Chess(Circle circle, int x, int y) {
        this.x = x;
        this.y = y;
        this.circle = circle;
    }

    public Chess(Circle circle, int x, int y, int color) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.circle = circle;
    }

    public Circle getCircle() {
        return circle;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
