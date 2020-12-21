package jms.utils;

import javafx.scene.control.Alert;
import jms.Global;
import jms.message.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetUtils {
    public static void sendMessage(Message message) {
        try {
            Socket socket = new Socket(Global.opponent_ip, Global.opponent_port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING, "对手连接出错，稍后再试");
            alert.showAndWait();
        }
    }
}
