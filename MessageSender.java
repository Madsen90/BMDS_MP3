
import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MessageSender extends Thread {

    Message message;
    Callback callback;
    public boolean trySend = true;
    Socket socket;

    public MessageSender(Callback messageSenderCallback, Socket s, Message m) {
        this.callback = messageSenderCallback;
        socket = s;
        message = m;
        start();
    }

    @Override
    public void run() {
        if(Peer.DEBUG)System.out.printf("Sending message: %s | %d -> %d\n", message, socket.getLocalPort(), socket.getPort());
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(message.Serialize());
            if (callback != null) {
                callback.action(socket, true);
            }
        } catch (IOException e) {
            if(Peer.DEBUG)System.out.println("Sending failed: " + e.getMessage());
            if (callback != null) {
                callback.action(socket, false);
            }
        }
    }

    public interface Callback {

        public void action(Socket socket, boolean success);
    }
}
