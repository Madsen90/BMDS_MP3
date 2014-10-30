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
				System.out.println("Received: " + message);
				
				switch(message.getCode()){
					case 1:
						handleConnect(socket, message.getPort());
						break;
					case 2:
						peerJoined(socket, message.getPort());
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

	private void handleConnect(Socket incommingSocket, int incommingListenPort){
		if(peer.newNetwork){
			peer.setLink(false, incommingSocket);
			peer.leftListenPort = incommingListenPort;
			peer.setLink(true, incommingSocket);
			peer.rightListenPort = incommingListenPort;
			
			try{
				DataOutputStream out = new DataOutputStream(incommingSocket.getOutputStream());
				Message m = new Message(2, "Success", peer.leftListenPort);
				out.write(m.Serialize());		
			}catch(Exception e){
				System.out.println("Fejl her: " + e.toString());
			}

			peer.printInfo();
			peer.newNetwork = false;
		}else{

			try{
				DataOutputStream out = new DataOutputStream(incommingSocket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream());
				
				Socket s = peer.getLink(false);
				Message m = new Message(1, s.getInetAddress().getHostAddress(), peer.rightListenPort);
				out.write(m.Serialize());

				byte[] buffer = new byte[socket.getReceiveBufferSize()];	
				int size = in.read(buffer);

				Message m2 = Message.Deserialize(buffer);
				
				if(m2.getCode() == 2){
					peer.setLink(false, incommingSocket);
					peer.rightListenPort = incommingListenPort;
				}

			}catch(Exception e){
				System.out.println("Fejl her: " + e.toString());
			}
			peer.printInfo();
		}
	}

	private void peerJoined(Socket incommingSocket, int incommingListenPort){
		peer.setLink(true, incommingSocket);
		peer.leftListenPort = incommingListenPort;

		peer.printInfo();
	}
}