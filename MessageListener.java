
import java.io.*;
import java.net.*;

public class MessageListener extends Thread {

    private final Callback callback;
    private final Disconnect disconnect;
    private final Socket socket;
    private final MessageDelivery delivery;

    public MessageListener(Callback callback, Disconnect disconnect, Socket socket, MessageDelivery delivery) {
        this.callback = callback;
        this.socket = socket;
        this.disconnect = disconnect;
        this.delivery = delivery;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            if(Peer.DEBUG)System.out.println("Listening at: "+socket.getPort());
            while (true) {
                byte[] buffer = new byte[socket.getReceiveBufferSize()];
                int size = in.read(buffer);

                if (size == -1) {
                    System.out.println("Socket closed " + socket.getInetAddress() + " - " + socket.getPort());
                    break;
                }
                Message m = Message.Deserialize(buffer);

                if(m.getOrigin() != -1){
                    delivery.process(m);
                } else {
                    if(Peer.DEBUG)System.out.println("Received: "+m+" from: "+socket.getPort());
                    callback.action(socket, m);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } finally {
            disconnect.action(socket);
        }
    }

    public interface Callback {

        public void action(Socket socket, Message message);
    }
    
    public interface Disconnect {
        public void action(Socket socket);
    }
}
