package jms.message;

/**
 * 对战版中，悔棋信息发送
 */
public class ReplayMessage extends Message {
    // 可以发送的悔棋请求的次数
    private final int TIMES = 3;
    private int flag;
    // 发送悔棋请求
    public static final int REQUEST = 0;
    // 同意悔棋请求
    public static final int AGREE = 1;
    // 拒绝悔棋请求
    public static final int REFUSE = 2;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getTIMES() {
        return TIMES;
    }
}
