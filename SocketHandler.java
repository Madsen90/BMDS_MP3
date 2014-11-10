
import java.net.*;
import java.io.IOException;

public class SocketHandler extends Thread {

    private Peer peer;
    private int port;
    private Callback callback;

    public SocketHandler(int port, Peer peer, Callback socketHandlerCallback) {
        this.port = port;
        this.peer = peer;
        this.callback = socketHandlerCallback;
    }

    @Override
    public void run() {
        System.out.println("Listening on: " + port);
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                Socket source = serverSocket.accept();
                callback.action(peer, source);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    public interface Callback{
	public void action(Peer peer, Socket source);
}
}
