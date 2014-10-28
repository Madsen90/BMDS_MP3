import java.io.*;
import java.net.*;
public class MessageListener extends Thread
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
                
                socket.close();
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