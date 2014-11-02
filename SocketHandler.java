import java.net.*;
import java.io.IOException;

public class SocketHandler extends Thread {
	private Peer peer;
	private int port;
	private SocketHandlerCallback socketHandlerCallback;

	public SocketHandler(int port, Peer peer, SocketHandlerCallback socketHandlerCallback){
		this.port = port;
		this.peer = peer;
		this.socketHandlerCallback = socketHandlerCallback;
	}
		
	@Override
	public void run() {
		System.out.println("SocketHandler Running: " + port);
		while(true){
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				System.out.println("Listening on: "+port);
				Socket source = serverSocket.accept();
				socketHandlerCallback.action(peer, source);
                                
	      	} catch (IOException ex) {
	    	    System.err.println("TCP: IdO Error:" + ex.getMessage());
	    	}
	    }
	}
}