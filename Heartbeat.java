/*import java.io.DataOutputStream;
import java.net.*;
import java.io.IOException;

public class Heartbeat extends Thread {
	private Peer peer;
	private Socket socket;
    private long pulse;

	public Heartbeat(Socket socket, Peer peer, long Pulse){
		this.socket = socket;
		this.peer = peer;
        this.pulse = pulse;
	}
		
	@Override
	public void run() {
		System.out.println("Heart is beating");
        try {
            while(true){
                heartbeat(socket);
                this.sleep(pulse);
                }
            } catch (Exception e) {
                    
                }
	}*/
        
        /*
        every pulse milliseconds the Peer sends a heartbeat to its connected
        peers, if it misses two heartbeats, the other peers will declare it 
        as dead.
        */
/*        private void heartbeat(Socket socket) {
                try{
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    Message m = new Message(CodeType.Heartbeat, peer.listenPort + " is still alive");
                    
                    out.write(m.Serialize());
		}catch(IOException e){
                    System.out.println("something bad happened, I might be dead.");
                }
        }
}*/