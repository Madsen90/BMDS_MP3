import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Peer {
    private LinkedList<Message> messages = new LinkedList<>();
    private Socket leftLink, rightLink;
    private int leftListenPort = -1, rightListenPort = -1; 
    public int listenPort;
    public boolean newNetwork;
    public HashMap<Integer, String> data = new HashMap<>();
    public HashMap<Integer, String> backup = new HashMap<>();
    public HashSet<Integer> log = new HashSet<>();
    private int pulse;
    boolean isSending = false;

    //Starting new network constructor
    public Peer(int listenPort) {
        messages.add(new Message(CodeType.Success, "Please virker"));

        startMessageLoop();

/*        newNetwork = true;
        this.listenPort = listenPort;
        beginListening(listenPort);
        printInfo();*/

    }

    //Connecting to existing network
    public Peer(int listenPort, String connectAddress, int connectPort) {
        newNetwork = false;
        this.listenPort = listenPort;
        DataOutputStream out;
        DataInputStream in;

        Socket leftSocket = null;

        try {
            leftSocket = new Socket(connectAddress, connectPort);
            out = new DataOutputStream(leftSocket.getOutputStream());
            in = new DataInputStream(leftSocket.getInputStream());

            Message message = new Message(CodeType.Connecting, "Trying to connect", listenPort);
            out.write(message.Serialize());

            byte[] buffer = new byte[leftSocket.getReceiveBufferSize()];
            int size = in.read(buffer);

            Message m = Message.Deserialize(buffer);
            System.out.println("Received: " + m);

            if (m.getCode() == CodeType.Connecting) {
                Socket rightSocket = new Socket(InetAddress.getByName(m.getContent()), m.getPort());
                DataOutputStream joinOut = new DataOutputStream(rightSocket.getOutputStream());

                Message joinMessage = new Message(CodeType.ConnectionEstablished, "Joined network", listenPort);
                joinOut.write(joinMessage.Serialize());

                //Message messageSucces = new Message(CodeType.ConnectionEstablished, "Connection Established");
                //out.write(messageSucces.Serialize());

                setLink(false, leftSocket, connectPort);
                setLink(true, rightSocket, m.getPort());
                new ConnectionHandler(this, rightSocket).start();
            } else if (m.getCode() == CodeType.ConnectionEstablished) {
                setLink(true, leftSocket, connectPort);
                setLink(false, leftSocket, connectPort);
            }
            new ConnectionHandler(this, leftSocket).start();
        } catch (IOException ex) {
            System.err.println("TCP: IO Error, connection lost: " + ex.getMessage());
        } finally {
        }

        beginListening(listenPort);
        printInfo();
    }

    public void printInfo() {
        if (leftLink != null) {
            System.out.println("leftListen: " + leftLink.getPort() + " " + leftListenPort);
        }

        if (rightLink != null) {
            System.out.println("rightListen: " + rightLink.getPort() + " " + rightListenPort);
        }
    }

    public void beginListening(int listenPort) {
        SocketHandler connectionListener = new SocketHandler(listenPort, this, new SocketHandlerCallback() {
            public void action(Peer peer, Socket source) {
                new ConnectionHandler(peer, source).start();
            }
        });

        connectionListener.start();
        startMessageLoop();
    }

    private void startMessageLoop(){
        System.out.println("Message loop startet");
        Socket socket = null;
        try{
            socket = new Socket("localhost", 1001);
        
        }catch(Exception e){

        }
        
        MessageSender messageSender = null;

        while(true){
            if(!isSending){
                if(!messages.isEmpty()){
                    System.out.println("Creating new message");
                    Message m = messages.getFirst();
                    isSending = true;
                    messageSender = new MessageSender(new MessageSenderCallback(){
                        public void action(){
                            finnishedSending();

                        }
                    }, socket, m);
                }
            }else {
                System.out.println("Still trying to send");
                if(messageSender != null){
                    messageSender.trySend = false;
                }
            }

            try{
                Thread.sleep(1000);
            }catch(Exception e){

            }
        }
    }

    public synchronized void finnishedSending(){
        isSending = false;
    }

    public int getListenPort(boolean left){
        return (left) ? leftListenPort : rightListenPort;
    }

    public Socket getLink(boolean left) {
        return (left) ? leftLink : rightLink;
    }

    public void setLink(boolean left, Socket s, int listenPort) {
        if (left) {
            leftListenPort = listenPort;
            leftLink = s;
        } else {
            rightListenPort = listenPort;
            rightLink = s;
        }
    }

    public synchronized void addMessage(Message message){
        messages.add(message);
    }

    public int getPulse() {
        return pulse;
    }

    public static void main(String[] args) {
        new Peer(-1);
        /*int listenPort = 0;

        try {
            listenPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Parameters must be given at least a listen port, or a listen and connect port & -address E.g.");
            System.out.println("java Peer 8080");
            System.out.println("java Peer 8080 localhost 8081");
        }

        if (args.length == 1) {
            System.out.println("Starting new P2P network");
            new Peer(listenPort);
        } else {
            try {
                String address = args[1];
                int connectPort = Integer.parseInt(args[2]);
                new Peer(listenPort, address, connectPort);
            } catch (Exception e) {
                System.out.println("More errors");
            }
        }*/
    }
}
