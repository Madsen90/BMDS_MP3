
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

public class Put {

    public static void main(String[] args) {
        try {
            if (args.length > 4) {
                throw new ArrayIndexOutOfBoundsException();
            }
            int peerPort = Integer.parseInt(args[1]);
            InetAddress peerAddress = InetAddress.getByName(args[0]);
            int key = Integer.parseInt(args[2]);
            String value = args[3];
            new Put(peerPort, peerAddress, key, value);

        } catch (UnknownHostException e) {
            System.out.println("The address " + args[1] + "does not seem to be a valid host address");
            System.out.println("Otherwise you may not have access to the network at which the address is present");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("parameters must be given as a port, an address, a key and a value");
            System.out.println("Fx.: java Put localhost 8080 1 \"This is the value to send\" ");
        }
    }

    public Put(int peerPort, InetAddress peerAddress, int key, String value) {
        Message putMessage = new Message(CodeType.Put, value, 0, key); //3 is put code
        byte[] messageInBytes = putMessage.Serialize();

        try {
            Socket peerSocket = new Socket(peerAddress, peerPort);
            peerSocket.getOutputStream().write(messageInBytes);
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }
}
