import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.io.IOException;

public class ConnectionHandler extends Thread {
	private Peer peer;
	private Socket socket;

	public ConnectionHandler(Peer peer, Socket socket){
		this.peer = peer;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("TCP Server Running...");
   		DataInputStream in = null;
   
  		try {
        	in = new DataInputStream(socket.getInputStream());
			System.out.println("Listening to new source");
        
			while (true) {
				byte[] buffer = new byte[socket.getReceiveBufferSize()];	
				int size = in.read(buffer);

				if(size == -1){
					in.close();
					System.out.println("Source closed " + socket.getInetAddress() + " - " + socket.getPort());
					return;
				}

				Message message = Message.Deserialize(buffer); 
				System.out.println("Received: " + message.getCode() + ": " + message.getContent());
				
				switch(message.getCode()){
					case 1:
						peer.handleConnect();
						break;
					case 3:
						//Put
						break;
					case 4:
						//Get
						break;
					default:
						//Eventuel fejlhåndtering
						break;
				}

			}
		} catch (IOException ex) {
			if(in != null){
				try{
					socket.close();
				}catch(IOException e){ 
					System.out.println("Couldn't close socket: "+e);
				}
			}

			System.err.println("TCP: IO Error: " + ex.getMessage());
		} 
	}

	public synchronized void handleConnect(Socket incomming){
		Message message = Message.Deserialize(buffer); 
		System.out.println("Received: " + message.getCode() + ": " + message.getContent());
		
	}


}