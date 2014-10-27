import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Peer{
	private Socket leftLink, rightLink;
	public int leftListenPort = -1, rightListenPort = -1;

	//Starting new network constructor
	public Peer	(int listenPort) {
		beginListening(listenPort);
		printInfo();
	}

	//Connecting to existing network
	public Peer	(int listenPort, String connectAddress, int connectPort) {
		DataOutputStream out;
		DataInputStream in;
		
		Socket leftSocket = null;
		
		try {
			leftSocket = new Socket(connectAddress, connectPort);
			out = new DataOutputStream(leftSocket.getOutputStream());
			in = new DataInputStream(leftSocket.getInputStream());

			Message message = new Message(1, "Trying to connect", listenPort);
			out.write(message.Serialize());

			byte[] buffer = new byte[leftSocket.getReceiveBufferSize()];	
			int size = in.read(buffer);

			Message m = Message.Deserialize(buffer);
			System.out.println("Modtaget: "+m);

			if(m.getCode() == 1){
				Socket rightSocket = new Socket(InetAddress.getByName(m.getContent()), m.getPort());
				Message messageSucces = new Message(2, "Connection Established");
				out.write(messageSucces.Serialize());
				
				leftLink = leftSocket;
				rightLink = rightSocket;
			}else if(m.getCode() == 2){
				leftListenPort = connectPort;
				leftLink = leftSocket;
			}
		} catch (IOException ex) {
			System.err.println("TCP: IO Error, connection lost: " + ex.getMessage());
		} finally {
		}

		beginListening(listenPort);
		printInfo();
	}
	
	public void printInfo(){
		System.out.println("leftListen:" + leftListenPort);
		System.out.println("rightListen:" + rightListenPort);
	}

	public void beginListening(int listenPort){
		SocketHandler connectionListener = new SocketHandler(listenPort, this, new SocketHandlerCallback(){
			public void action(Peer peer, Socket source){
				new ConnectionHandler(peer, source).start();
			}
		});

		connectionListener.start();
	}

	public Socket getLink(boolean left){
		return (left) ? leftLink : rightLink;
	}

	public void setLink(boolean left, Socket s){
		if(left)
			leftLink = s;
		else
			rightLink = s;
	}

	public static void main(String[] args) {
		int listenPort = 0;
		
		try
        {
            listenPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Parameters must be given at least a listen port, or a listen and connect port & -address E.g.");
            System.out.println("java Peer 8080");
            System.out.println("java Peer 8080 localhost 8081");
        }

		if(args.length == 1){
			System.out.println("Starting new P2P network");
			new Peer(listenPort);
	    }else{
	    	try
        	{
            	String address = args[1];
            	int connectPort = Integer.parseInt(args[2]);
        		new Peer(listenPort, address, connectPort);	
        	}catch(Exception e){
        		System.out.println("More errors");
        	}
	    }
	}
}