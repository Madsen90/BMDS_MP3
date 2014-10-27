import java.net.*;

public class Peer{
	private Socket leftLink, rightLink;

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
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Parameters must be given at least a listen port, or a listen and connect port & -address E.g.");
            System.out.println("java Peer 8080");
            System.out.println("java Peer 8080 localhost 8081");
        }


		if(args[1] == null){

			new Peer(listenPort);
	        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
	        {
	            System.out.println("Parameters must be given as a source port, and a sink port. E.g.");
	            System.out.println("java Server 8080 8081");
	        } 
	    }
	}


}