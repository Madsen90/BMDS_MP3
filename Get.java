import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Get
{

    public static void main(String[] args)
    {

        try
        {
            if (args.length > 3)
            {
                throw new ArrayIndexOutOfBoundsException();
            }
            int peerPort = Integer.parseInt(args[0]);
            InetAddress peerAddress = InetAddress.getByName(args[1]);
            int key = Integer.parseInt(args[2]);

            new Get(peerPort, peerAddress, key);

        } catch (UnknownHostException e)
        {
            System.out.println("The address " + args[1] + "does not seem to be a valid InetAddress");
            System.out.println("Otherwise you may not have access to the network at which the address is present");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
        {
            System.out.println("parameters must be given as a port, an address and a key");
            System.out.println("Fx.: java get localhost 8080 1");
        }
    }

    public Get(int peerPort, InetAddress peerAddress, int key){
        String keyString = ""+key;
        
        Message getmessage = new Message(4, keyString); //4 is code for get
        byte[] messageInBytes = getmessage.Serialize();
        
        try{
            Socket peerSocket = new Socket(peerAddress, peerPort);
            peerSocket.getOutputStream().write(messageInBytes);
            MessageListener mL = new MessageListener(peerSocket);
            mL.start();
        } catch(IOException e){
            System.out.println("failed to send get request: " + e.getMessage());
        }
    }

    private class MessageListener extends Thread
    {

        Socket socket;

        public MessageListener(Socket socket)
        {
            this.socket = socket;
        }

        public void run()
        {
            System.out.println("messageListener Running...");
            DataInputStream in = null;

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

                    switch(message.getCode()){
                        case -1 : 
                            System.out.println("Error from peer - most likely no result in cloud");
                            break;
                        case 5 : 
                            System.out.println("Success! Value of key is: " + message.getContent());
                            break;
                        default :
                            System.out.println("Error - Unknown code returned: " + message.getCode() + " - message content is: " + message.getContent());
                            break;
                    }
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
