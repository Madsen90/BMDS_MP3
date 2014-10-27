import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Source {
	public Source(InetAddress address, int port){
		DataOutputStream out;
		Socket socket = null;
		
		try {
			socket = new Socket(address, port);
			out = new DataOutputStream(socket.getOutputStream());
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Connected to server, enter input:");
			
			String s = input.readLine();
			Message message = new Message(1, s);
			out.write(message.Serialize());

		} catch (IOException ex) {
			System.err.println("TCP: IO Error, connection lost: " + ex.getMessage());
		} finally {
			try {
				if(socket != null)
					socket.close();
			} catch (IOException ex) {
				System.err.println("TCP: IO Error: " + ex.getMessage());
			}
		}
	}
	
	public static void main(String[] args) {
		try
        {
            InetAddress address = InetAddress.getByName(args[0]);
            int port = Integer.parseInt(args[1]);
			new Source(address, port);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Parameters must be gives as an internet address and a receiving port. E.g.");
            System.out.println("java Source localhost 8080");
        } catch (UnknownHostException e)
        {
            System.out.println("The address " + args[0] + "does not seem to be a valid InetAddress");
            System.out.println("Otherwise you may not have access to the network at which the address is present");
        }
	}
}
