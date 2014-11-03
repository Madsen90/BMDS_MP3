
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.io.IOException;

public class ConnectionHandler extends Thread {

    private final Peer peer; // Peer running ConnectionHandler
    private final Socket socket; // A socket the Peer receives messages on

    public static final boolean direction = true; // The direction of get, backup, delete, and panic in. Everything.

    public ConnectionHandler(Peer peer, Socket socket) {
        this.peer = peer;
        this.socket = socket;
    }

    @Override
    public void run() {
        DataInputStream in = null;

        try {
            in = new DataInputStream(socket.getInputStream());
            System.out.println("Listening to new source");

            while (true) {
                byte[] buffer = new byte[socket.getReceiveBufferSize()];
                int size = in.read(buffer);

                if (size == -1) {
                    in.close();
                    System.out.println("Source closed " + socket.getInetAddress() + " - " + socket.getPort());
                    return;
                }

                Message message = Message.Deserialize(buffer);
                System.out.println("Received: " + message);

                if(peer.log.contains(message.hashCode())){
                    continue; // TODO RESPOND TO GET
                }
                peer.log.add(message.hashCode());

                switch (message.getCode()) {
                    case Connecting:
                        handleConnect(message);
                        break;
                    case ConnectionEstablished:
                        peerJoined(message);
                        break;
                    case Put:
                        put(message);
                        break;
                    case Get:
                        get(message);
                        break;
                    case Heartbeat: //heartbeat
                        break;
                    case Panic: //panic
                        System.out.println("received panic message: " + message.getContent());
                        panic(message);
                        break;
                    case Backup:
                        backup(message);
                        break;
                    default:
                        System.err.println("Fejl");
                        break;
                }
            }
        } catch (IOException ex) {
            if (socket == peer.getLink(direction)) { //if it is the left socket that died
                System.out.println("My left buddy, " + peer.getListenPort(direction) + ", died. I'm panicking!!");
                peer.setLink(direction,null,-1);

            } else if (socket == peer.getLink(!direction)) { //if the right socket died
                System.out.println("My right buddy, " + peer.getListenPort(!direction)  + ", died. I'm keeping it coolio.");
                panic(new Message(CodeType.Panic, socket.getLocalAddress().getHostAddress(), peer.listenPort));
                peer.setLink(!direction,null,-1);

                boolean failed = false;
                for(Integer key : peer.backup.keySet()){
                    try{
                        peer.data.put(key,peer.backup.get(key));
                        Message getmessage = new Message(CodeType.Backup, peer.data.get(key), peer.listenPort, key);
                        peer.getLink(direction).getOutputStream().write(getmessage.Serialize());
                    } catch (IOException e) {
                        System.out.println("failed to send backup message: " + e.getMessage());
                        failed = true;
                    }
                }
                if(!failed){
                    peer.backup.clear();
                }
            }

            if (in != null) {
                try {
                    System.out.println("Closing socket");
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Couldn't close socket: " + e);
                }
            }
            System.err.println("Connection Closed: " + ex.getMessage());
        }
    }
    private void backup(Message message){
        peer.backup.put(message.getKey(),message.getContent());
    }
    private void delete(Message message, boolean really){
        if(really){
            peer.data.remove(message.getKey());
            peer.backup.remove(message.getKey());
            System.out.println("Removed key: "+message.getKey());
        }

        try{
            peer.getLink(direction).getOutputStream().write(message.Serialize());
        } catch (IOException e) {
            System.out.println("failed to send delete message: " + e.getMessage());
        }
    }
    private void delete(Message message){
        delete(message,true);
    }

    private void put(Message message) {
        peer.data.put(message.getKey(), message.getContent());
        System.out.println("Put " + message.getContent() + " at " + message.getKey());

        Message deletemessage = new Message(CodeType.Delete, message.getContent(), peer.listenPort, message.getKey());
        peer.log.add(deletemessage.hashCode());
        delete(deletemessage,false);

        try{
            Message getmessage = new Message(CodeType.Backup, message.getContent(), peer.listenPort, message.getKey());
            peer.getLink(direction).getOutputStream().write(getmessage.Serialize());
        } catch (IOException e) {
            System.out.println("failed to send backup message: " + e.getMessage());
        }
    }

    private void get(Message message) {
        if (peer.data.containsKey(message.getKey())) { // TODO check if exists in backup
            System.out.println("Data here. Returning...");
            try {
                Message getmessage = new Message(CodeType.Success, peer.data.get(message.getKey()));
                Socket peerSocket = new Socket(InetAddress.getByName(message.getContent()), message.getPort());
                peerSocket.getOutputStream().write(getmessage.Serialize());
            } catch (IOException e) {
                System.out.println("failed to respond to get request: " + e.getMessage());
            }
        } else {
            System.out.println("Data not here. Forwarding..");
            try {
                peer.getLink(direction).getOutputStream().write(message.Serialize());
            } catch (IOException e) {
                System.out.println("failed to forward get request: " + e.getMessage());
            }
        }
    }

    private void handleConnect(Message message) {
        if (peer.newNetwork) {
            
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                Message m = new Message(CodeType.ConnectionEstablished, "Success", peer.listenPort);
                out.write(m.Serialize());
                peer.setLink(false, socket, message.getPort());
                peer.setLink(true, socket, message.getPort());
                peer.printInfo();
                peer.newNetwork = false;            
            } catch (IOException e) {
                System.out.println("Fejl her: " + e.toString());
            }

        } else {

            try {

                boolean leftIsRight = peer.getLink(false) == peer.getLink(true);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Socket s = peer.getLink(!direction);
                Message m = new Message(CodeType.Connecting, s.getInetAddress().getHostAddress(), peer.getListenPort(!direction));
                out.write(m.Serialize());

                if(!leftIsRight)
                    peer.getLink(!direction).close();
                // byte[] buffer = new byte[socket.getReceiveBufferSize()];
                // int size = in.read(buffer);

                //Message m2 = Message.Deserialize(buffer);

                // if (m2.getCode() == CodeType.ConnectionEstablished) {
                peer.setLink(!direction, socket, message.getPort());
                //     //peer.rightListenPort = incommingListenPort;
                // }

            } catch (IOException e) {
                System.out.println("Fejl her: " + e.toString());
            }
            peer.printInfo();
        }
    }

    private void peerJoined(Message message) {
        peer.setLink(direction, socket, message.getPort());
        peer.printInfo();
        for(Integer key : peer.data.keySet()){
            try{
                Message getmessage = new Message(CodeType.Backup, peer.data.get(key), peer.listenPort, key);
                peer.getLink(direction).getOutputStream().write(getmessage.Serialize());
            } catch (IOException e) {
                System.out.println("failed to send backup message: " + e.getMessage());
            }
        }
    }

    private void panic(Message message) {
        panic(message, 0);
    }
    //Called if the Peer loses connection to one of its connected peers
    //if left is missing. Panic! If right is missing, wait.
    private void panic(Message message, int count) {

        boolean actuallyNotNull = false;
        try {
            Socket next = peer.getLink(direction);
            Socket testSocket = new Socket(socket.getInetAddress(), socket.getPort());
            testSocket.close();
        } catch(IOException e) {
            actuallyNotNull = true;
        }

        if (peer.getLink(direction) == null || actuallyNotNull) { // TODO check if he is online
            try {
                //Give the peer missing a left buddy, you as a left buddy.
                Message m = new Message(CodeType.ConnectionEstablished, "I'm your new right friend.");
                Socket peerSocket = new Socket(InetAddress.getByName(message.getContent()), message.getPort());
                peerSocket.getOutputStream().write(m.Serialize());

                peer.setLink(direction, peerSocket, message.getPort());
                new ConnectionHandler(peer, peerSocket).start();
            } catch (IOException e) {
                System.out.println("Could not make the new connection to "+message.getContent()+":"+message.getPort());
            }
        } else {
            try {
                peer.getLink(direction).getOutputStream().write(message.Serialize());
            } catch (IOException e) {
                System.out.println("I was unable to pass on a panic signal to " +peer.getListenPort(direction));

                if(count < 4) {
                    System.out.println("Retrying...");
                    try {
                        Thread.sleep(2000, count+1);
                    } catch(InterruptedException ex){
                        System.err.println("ERROR!!");
                    }
                    panic(message);
                } else {
                    System.out.println("Retried too many times... I don't know what to do.");
                }
            }
        }
    }
}
