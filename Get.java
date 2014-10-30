import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

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
            System.out.println("Fx.: java Get localhost 8080 1");
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

    
}
