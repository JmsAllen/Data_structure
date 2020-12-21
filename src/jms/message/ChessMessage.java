package jms.message;

public class ChessMessage extends Message {
    private int x;
    private int y;
    private boolean isBlack;

    public ChessMessage(int x, int y, boolean isBlack) {
        this.x = x;
        this.y = y;
        this.isBlack = isBlack;
    }

    public ChessMessage() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ChessMessage{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", isBlack=").append(isBlack);
        sb.append('}');
        return sb.toString();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isBlack() {
        return isBlack;
    }

}
