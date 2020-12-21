package jms.message;

public class WinMessage extends Message {
    private int flag;
    public static final int WIN = 1; // 胜利
    public static final int FAILURE = 2; // 失败

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
