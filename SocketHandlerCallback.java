import java.net.*;

public interface SocketHandlerCallback{
	public void action(Peer peer, Socket source);
}