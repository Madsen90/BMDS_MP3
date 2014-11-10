
import java.net.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDelivery extends Thread {

	private final Peer peer;

	private final Queue<MessageQuery> sendQueue = new LinkedList<MessageQuery>();

	public MessageDelivery(Peer peer){
		this.peer = peer;
	}

    @Override
    public void run() {
    	while(true){
    		// Send messages
    		if(queueNotEmpty()){
    			System.out.println("SENDING coolio !!");
    			final MessageQuery query = sendQueue.poll(); // TODO SYNCHRONIZE EVERYTHING
				new MessageSender(new MessageSender.Callback(){
		            @Override
		            public void action(Socket socket, boolean success) {
		                if(query.callback != null)
		                    query.callback.action(socket, success);
		            }
		        }, query.socket, query.message);
    		}

    		// Put send messages in an outbox of messages awaiting response
    		// Start a timer for said messages to retry
    	}
    }

    public synchronized boolean queueNotEmpty(){
    	return !sendQueue.isEmpty();
    }

    // Called when a new message gets in as a response.
    public synchronized void process(Message message){
    }

	public synchronized void enqueue(Socket socket, Message message, MessageSender.Callback callback){
		sendQueue.add(new MessageQuery(socket, message, callback));
	}

	private class MessageQuery{
		public final Socket socket;
		public final Message message;
		public final MessageSender.Callback callback;
		public MessageQuery(Socket socket, Message message, MessageSender.Callback callback){
			this.socket = socket;
			this.message = message;
			this.callback = callback;
		}
	}
}