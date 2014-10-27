import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

public class peerTest{

	public static void main(String[] args){
		try{
			int port = Integer.parseInt(args[0]);
			InetAddress add = InetAddress.getByName(args[1]);
			new peerTest(port, add);	
		} catch(UnknownHostException e){
			System.out.println("UnknownHostException: " + e.getMessage());
		}
	}

	public peerTest(int port, InetAddress address){
		try{
			ServerSocket socket = new ServerSocket(port);
			MessageListener mL = new MessageListener(socket);
			mL.start();
		} catch (IOException e){
			System.out.println("failed to make socket: " + e.getMessage());
		}

	}

	private class MessageListener extends Thread
    {

        ServerSocket serverSocket;

        public MessageListener(ServerSocket socket)
        {
            this.serverSocket = socket;
        }

        public void run()
        {
            System.out.println("messageListener Running...");
            DataInputStream in = null;
            Socket socket = null;
            boolean missingCon = true;
            while(missingCon){
            	try{
            	socket = serverSocket.accept();
            	if(socket != null)
            		missingCon = false;
            	}
            	catch (IOException e){
            		System.out.println("IOException from accepting connection: " + e.getMessage());
            	}
            }

            try
            {
                in = new DataInputStream(socket.getInputStream());

                while (true)
                {
                    byte[] buffer = new byte[socket.getReceiveBufferSize()];
                    int size = in.read(buffer);

                    if (size == -1)
                    {
                        in.close();
                        System.out.println("Source closed " + socket.getInetAddress() + " - " + socket.getPort());
                        return;
                    }

                    Message message = Message.Deserialize(buffer);
                    String s = "Received: " + message.getCode() + ": " + message.getContent();
                    System.out.println(s);
                    message = new Message(5, s);
                    byte[] sb = message.Serialize();
                    socket.getOutputStream().write(sb);
                }
            } catch (IOException ex)
            {
                if (in != null)
                {
                    try
                    {
                        socket.close();
                    } catch (IOException e)
                    {
                        System.out.println("Couldn't close socket: " + e);
                    }
                }

                System.err.println("TCP: IO Error: " + ex.getMessage());
            }
        }

    }
}