import java.net.*;

public class Peer{

	public Peer	(int listenPort) {
		SocketHandler connectionListener = new SocketHandler(listenPort, this, new SocketHandlerCallback(){
			public void action(Peer peer, Socket source){
				new ConnectionHandler(peer, source).start();
			}
		});

		connectionListener.start();
	}


	public static void main(String[] args) {
		try
        {
            int listenPort = Integer.parseInt(args[0]);
			new Peer(listenPort);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Parameters must be given as a source port, and a sink port. E.g.");
            System.out.println("java Server 8080 8081");
        } 
	}


}