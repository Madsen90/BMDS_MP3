import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MessageSender extends Thread{
	Message m;
	MessageSenderCallback messageSenderCallback;
	public boolean trySend = true;
	Socket socket;

	public MessageSender(MessageSenderCallback messageSenderCallback, Socket s, Message m){
		this.m = m;
		this.messageSenderCallback = messageSenderCallback;
		socket = s;
		start();
	}
	
	@Override
    public void run() {
    	System.out.println("Trying to send");
		try{
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(m.Serialize());
			messageSenderCallback.action();
			return;
		}catch(Exception e){
			System.out.println("Sending failed");
		}

    }

}