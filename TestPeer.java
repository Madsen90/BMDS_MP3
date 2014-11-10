import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class TestPeer{
	
	public TestPeer(){
		ServerSocket socket = null;
		try{
			socket = new ServerSocket(1001);
			Socket socket2 = socket.accept();
			socket2.setSoTimeout(1000);
			while(true){
				DataInputStream in = new DataInputStream(socket2.getInputStream());

				byte[] buffer = new byte[socket2.getReceiveBufferSize()];
	            int size = in.read(buffer);

	            Message m = Message.Deserialize(buffer);
	            System.out.println(m.getContent());
	        }
         
		}
		catch (IOException e){
			System.out.println("IOException from accepting connection: " + e.getMessage());
		}
	}

	public static void main(String[] args){
		new TestPeer();
	}
}