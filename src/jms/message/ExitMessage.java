package jms.message;

public class ExitMessage extends Message {
    private int flag;
    public static final int REQUEST = 0;
    public static final int AGREE = 1;
    public static final int REFUSE = 2;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
