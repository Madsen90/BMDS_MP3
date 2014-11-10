import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageSender{
	public static void Send(Message message, Socket socket) throws IOException{
		int hashCode = message.hashCode();
		socket.getOutputStream().write(message.Serialize());
		socket.setSoTimeout(2000);

		byte[] buffer = new byte[socket.getReceiveBufferSize()];
        int size = socket.getInputStream().read(buffer);

        if (size == -1) {
        
        }

        Message replyMessage = Message.Deserialize(buffer);
        if (replyMessage.hashCode() != hashCode){
        //	throw new Exception();
        }
	}
}