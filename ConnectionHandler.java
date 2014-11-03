
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.io.IOException;

public class ConnectionHandler extends Thread {

    private final Peer peer;
    private final Socket socket;

    public ConnectionHandler(Peer peer, Socket socket) {
        this.peer = peer;
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("TCP Server Running...");
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

                switch (message.getCode()) {
                    case 1:
                        handleConnect(socket);
                        break;
                    case 2:
                        peerJoined(socket);
                        break;
                    case 3:
                        put(message);
                        break;
                    case 4:
                        get(message);
                        break;
                    case 6: //heartbeat

                    case 7: //panic
                        System.out.println("received panic message: " + message.getContent());
                        panic(message);
                    default:
                        //Eventuel fejlhåndtering
                        break;
                }
            }
        } catch (IOException ex) {
            if (socket == peer.getLink(true)) { //if it is the left socket that died
                System.out.println("My left buddy, " + socket.getPort() + ", died. I'm panicking!!");
                panic(new Message(7, String.valueOf(socket.getPort()), socket));
            } else if (socket == peer.getLink(false)) { //if the right socket died
                System.out.println("My right buddy, " + socket.getPort() + ", died. I'm keeping it coolio.");
            }
            if (in != null) {
                try {
                    System.out.println("closing socket");
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Couldn't close socket: " + e);
                }
            }
            System.err.println("TCP  : IO Error: " + ex.getMessage());
        }
    }

    private void put(Message message) {
        String m = message.getContent();
        int putkey = Integer.parseInt(m.split(Message.SEPERATOR)[0]);
        String value = m.split(Message.SEPERATOR)[1];
        peer.data.put(putkey, value);
        System.out.println("Put " + value + " at " + putkey);
        // TODO backup
    }

    private void get(Message message) {
        String m = message.getContent();
        int getkey = Integer.parseInt(m.split(Message.SEPERATOR)[0]);
        int getport = Integer.parseInt(m.split(Message.SEPERATOR)[1]);
        String getaddress;
        if (m.split(Message.SEPERATOR).length < 3) {
            getaddress = socket.getInetAddress().getHostAddress();
        } else {
            getaddress = m.split(Message.SEPERATOR)[2];
        }
        if (peer.data.containsKey(getkey)) {
            System.out.println("Data here. Returning...");
            try {
                Message getmessage = new Message(4, peer.data.get(getkey));
                Socket peerSocket = new Socket(InetAddress.getByName(getaddress), getport);
                peerSocket.getOutputStream().write(getmessage.Serialize());
            } catch (IOException e) {
                System.out.println("failed to send get request: " + e.getMessage());
            }
        } else {
            System.out.println("Data not here. Forwarding..");
            try {
                Message getmessage = new Message(5, getkey + Message.SEPERATOR + getport + Message.SEPERATOR + getaddress);
                Socket peerSocket = peer.getLink(false);
                peerSocket.getOutputStream().write(getmessage.Serialize());
            } catch (IOException e) {
                System.out.println("failed to send get request: " + e.getMessage());
            }
        }
    }

    private void handleConnect(Socket incommingSocket) {
        if (peer.newNetwork) {
            peer.setLink(false, incommingSocket);
            //peer.leftListenPort = incommingListenPort;
            peer.setLink(true, incommingSocket);
            //peer.rightListenPort = incommingListenPort;

            try {
                DataOutputStream out = new DataOutputStream(incommingSocket.getOutputStream());
                Message m = new Message(2, "Success", peer.getLink(true).getPort());
                out.write(m.Serialize());
            } catch (IOException e) {
                System.out.println("Fejl her: " + e.toString());
            }

            peer.printInfo();
            peer.newNetwork = false;
        } else {

            try {
                DataOutputStream out = new DataOutputStream(incommingSocket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Socket s = peer.getLink(false);
                Message m = new Message(1, s.getInetAddress().getHostAddress(), peer.getLink(false).getPort());
                out.write(m.Serialize());

                byte[] buffer = new byte[socket.getReceiveBufferSize()];
                int size = in.read(buffer);

                Message m2 = Message.Deserialize(buffer);

                if (m2.getCode() == 2) {
                    peer.setLink(false, incommingSocket);
                    //peer.rightListenPort = incommingListenPort;
                }

            } catch (IOException e) {
                System.out.println("Fejl her: " + e.toString());
            }
            peer.printInfo();
        }
    }

    private void peerJoined(Socket incommingSocket) {
        peer.setLink(true, incommingSocket);
        //peer.leftListenPort = incommingListenPort;

        peer.printInfo();
    }

    //Called if the Peer loses connection to one of its connected peers
    //if left is missing. Panic! If right is missing, wait.
    private void panic(Message message) {
        //skal være en socket, lige nu er det kun porten!!
        int missing = Integer.parseInt(message.getContent());

        Socket to = peer.getLink(false);
        Socket sender = message.getSender();

        if (missing == to.getPort()) {
            try {
                //Give the peer missing a left buddy, you as a left buddy.
                DataOutputStream out = new DataOutputStream(message.getSender().getOutputStream());
                Message m = new Message(2, "I'm your new left friend.");
                out.write(m.Serialize());
                peer.setLink(false, to);
            } catch (IOException e) {
                System.out.println("Could not make the new connection between " + sender + " and " + peer.listenPort);
            }
        } else {
            try {
                DataOutputStream out = new DataOutputStream(peer.getLink(false).getOutputStream());
                Message m = new Message(7, message.getContent());
                out.write(m.Serialize());
            } catch (IOException e) {
                System.out.println("I was unable to pass on a panic signal from : " + message.getSender());
            }
        }
    }

}
