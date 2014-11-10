
import java.net.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer {

    public static final boolean DEBUG = true;

    private final MessageListener.Disconnect onDisconnect = new MessageListener.Disconnect() {

        @Override
        public void action(Socket socket) {
            if (socket == getLink(true)) { //if it is the left socket that died
                System.out.println("My left buddy, " + getListenPort(true) + ", died. I'm coolio !!");
                setLink(true, null, -1);

            } else if (socket == getLink(false)) { //if the right socket died
                System.out.println("My right buddy, " + getListenPort(false) + ", died. I'm keeping it panicking.");
                setLink(false, null, -1);
                panic(new Message(CodeType.Panic, socket.getLocalAddress().getHostAddress(), listenPort));

                boolean failed = false;
                for (Integer key : backup.keySet()) {
                    try {
                        data.put(key, backup.get(key));
                        Message getmessage = new Message(CodeType.Backup, data.get(key), listenPort, key);
                        getLink(true).getOutputStream().write(getmessage.Serialize());
                    } catch (IOException e) {
                        System.out.println("failed to send backup message: " + e.getMessage());
                        failed = true;
                    }
                }
                if (!failed) {
                    backup.clear();
                }
            }
            System.err.println("Stupid Connection Closed: " + socket.getPort());
        }
    };
    private final MessageListener.Callback callback = new MessageListener.Callback() {
        @Override
        public void action(Socket socket, final Message message) {
            if (logContains(message.hashCode())) {
                if (message.getCode() == CodeType.Get) {
                    System.out.println("Get not found. Returning...");
                    try {
                        Message returnMessage = new Message(CodeType.Failure, "Value not found");
                        new MessageSender(null, new Socket(InetAddress.getByName(message.getContent()), message.getPort()), returnMessage);
                    } catch (IOException e) {
                        System.out.println("Failed to respond to get request: " + e.getMessage());
                    }

                }
                return;
            }
            addToLog(message.hashCode());

            switch (message.getCode()) {

                case Connecting:
                    final MessageSender.Callback connectingDone = new MessageSender.Callback() {
                        @Override
                        public void action(Socket socket, boolean success) {
                            if (success) {
                                setLink(false, socket, message.getPort());
                                System.out.println("Accepted: " + message.getPort() + " as right buddy");
                                printInfo();
                            }
                        }
                    };

                    if (getLink(false) != null) {
                        new MessageSender(connectingDone, getLink(false),
                                new Message(CodeType.PleaseConnect, socket.getInetAddress().getHostAddress(), message.getPort()));
                    } else {
                        connectingDone.action(socket, true);
                    }
                    break;

                case PleaseConnect:
                    try {
                        Socket s = new Socket(InetAddress.getByName(message.getContent()), message.getPort());
                        new MessageSender(new MessageSender.Callback() {
                            @Override
                            public void action(Socket socket, boolean success) {
                                if(success){
                                    new MessageListener(callback, onDisconnect, socket);
                                    setLink(true, socket, message.getPort());
                                    System.out.println("Connected my left buddy to: " + message.getPort());
                                    sendBackup();
                                    printInfo();
                                } else {
                                    System.out.println("Failure");
                                }
                            }
                        }, s, new Message(CodeType.Connected, "I am here", listenPort));
                    } catch (IOException ex) {
                        Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;

                case Connected:
                    setLink(false, socket, message.getPort());
                    System.out.println("Accepted: " + message.getPort() + " as right buddy");
                    printInfo();
                    break;

                case Put:
                    put(message);
                    break;
                case Get:
                    get(message);
                    break;
                case Delete:
                    delete(message, true);
                    break;
                case Backup:
                    backup(message);
                    break;
                case Panic:
                    panic(message);
                    break;
            }
        }
    };

    private LinkedList<Message> messages = new LinkedList<>();
    private Socket leftLink, rightLink;
    private int leftListenPort = -1, rightListenPort = -1;
    public int listenPort;
    public boolean newNetwork;
    public HashMap<Integer, String> data = new HashMap<>();
    public HashMap<Integer, String> backup = new HashMap<>();
    private PriorityQueue<Integer> log = new PriorityQueue<>();
    boolean isSending = false;

    public Peer(final int listenPort, String connectAddress, int connectPort) {
        this.listenPort = listenPort;

        beginListening(listenPort, callback);

        try {
            setLink(true, new Socket(connectAddress, connectPort), connectPort);
            new MessageListener(callback, onDisconnect, leftLink).start();
        } catch (IOException ex) {
            System.err.println("Could not connect: " + ex.getMessage());
            return;
        }

        new MessageSender(new MessageSender.Callback() {

            @Override
            public void action(Socket socket, boolean success) {
                if (!success) {
                    System.out.println("Could not hello. Retrying...");
                    new MessageSender(this, getLink(true), new Message(CodeType.Connecting, "Yay!", listenPort));
                }
            }
        }, getLink(true), new Message(CodeType.Connecting, "HELLO", listenPort));
    }

    private void put(Message message) {

        data.put(message.getKey(), message.getContent());
        System.out.println("Put " + message.getContent() + " at " + message.getKey());

        Message deleteMessage = new Message(CodeType.Delete, message.getContent(), listenPort, message.getKey());
        addToLog(deleteMessage.hashCode());
        delete(deleteMessage, false);

        new MessageSender(null, getLink(true), new Message(CodeType.Backup, message.getContent(), listenPort, message.getKey()));
    }

    private void get(Message message) {
        if (data.containsKey(message.getKey())) {
            System.out.println("Data here. Returning...");
            try {
                Socket peerSocket = new Socket(InetAddress.getByName(message.getContent()), message.getPort());
                Message returnMessage = new Message(CodeType.Success, data.get(message.getKey()), listenPort, message.getKey());
                new MessageSender(null, peerSocket, returnMessage);
            } catch (IOException e) {
                System.out.println("Failed to respond to get request: " + e.getMessage());
            }
        } else {
            System.out.println("Data not here. Forwarding..");
            new MessageSender(null, getLink(true), message);
        }
    }

    private void sendBackup() {
        System.out.println("Sending backup...");
        for (Integer key : data.keySet()) {
            Message backupMessage = new Message(CodeType.Backup, data.get(key), listenPort, key);
            new MessageSender(null, getLink(true), backupMessage);
        }
    }

    private void backup(Message message) {
        backup.put(message.getKey(), message.getContent());
    }

    private void delete(Message message, boolean really) {
        if (really) {
            data.remove(message.getKey());
            backup.remove(message.getKey());
            System.out.println("Removed key: " + message.getKey());
        }

        new MessageSender(null, getLink(true), message);
    }

    private void panic(final Message message) {
        final Peer peer = this;
        final MessageSender.Callback connectCallback = new MessageSender.Callback() {
            @Override
            public void action(Socket socket, boolean success) {
                if (success) {
                    setLink(true, socket, message.getPort());
                    new ConnectionHandler(peer, socket).start();
                    sendBackup();
                    printInfo();
                }
            }
        };

        if (getLink(true) == null) { // TODO check if he is online
            try {
                //Give the peer missing a left buddy, you as a left buddy.
                Socket peerSocket = new Socket(InetAddress.getByName(message.getContent()), message.getPort());
                Message m = new Message(CodeType.Connected, "I'm your new right friend.", listenPort);
                new MessageSender(connectCallback, peerSocket, m);

            } catch (IOException e) {
                System.out.println("Could not make the new connection to " + message.getContent() + ":" + message.getPort());
            }
        } else {
            new MessageSender(new MessageSender.Callback() {
                @Override
                public void action(Socket socket, boolean success) {
                    if (!success) {
                        try {
                            Socket peerSocket = new Socket(InetAddress.getByName(message.getContent()), message.getPort());
                            Message m = new Message(CodeType.Connected, "I'm your new right friend.", listenPort);
                            new MessageSender(connectCallback, peerSocket, m);
                        } catch (IOException ex) {
                            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, getLink(true), message);
        }
    }

    public void printInfo() {
        if (leftLink != null) {
            System.out.printf("LEFT: %d -> %d (%d)\n", leftLink.getLocalPort(), leftListenPort, leftLink.getPort());
        }

        if (rightLink != null) {
            System.out.printf("RIGHT: %d -> %d (%d)\n", rightLink.getLocalPort(), rightListenPort, rightLink.getPort());
        }
    }

    public final void beginListening(int listenPort, final MessageListener.Callback callback) {
        SocketHandler socketListener = new SocketHandler(listenPort, this, new SocketHandler.Callback() {
            @Override
            public void action(Peer peer, Socket source) {
                System.out.println("New Connection From: " + source.getInetAddress() + ":" + source.getPort());
                new MessageListener(callback, onDisconnect, source).start();
            }
        });

        socketListener.start();
    }

    public synchronized void addToLog(Integer x) {
        if (log.size() > 100000) {
            for (int i = 100000; i > 10000; i--) {
                log.poll();
            }
        }
        log.add(x);
    }

    public synchronized boolean logContains(Integer x) {
        return log.contains(x);
    }

    public int getListenPort(boolean left) {
        return (left) ? leftListenPort : rightListenPort;
    }

    public Socket getLink(boolean left) {
        return (left) ? leftLink : rightLink;
    }

    public void setLink(boolean left, Socket s, int listenPort) {
        if (left) {
            /*if(leftLink != null) {
                try {
                    leftLink.close();
                } catch (IOException ex) {
                    System.out.println("Could not close left socket");
                }
            }*/
            leftListenPort = listenPort;
            leftLink = s;
        } else {
            /*if(rightLink != null) {
                try {
                    rightLink.close();
                } catch (IOException ex) {
                    System.out.println("Could not close right socket");
                }
            }*/
            rightListenPort = listenPort;
            rightLink = s;
        }
    }

    public static void main(String[] args) {
        int listenPort = 1001;

        try {
            listenPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Parameters must be given at least a listen port, or a listen and connect port & -address E.g.");
            System.out.println("java Peer 8080");
            System.out.println("java Peer 8080 localhost 8081");
        }

        if (args.length == 1) {
            System.out.println("Starting new P2P network");
            new Peer(listenPort, "127.0.0.1", listenPort);
        } else {
            try {
                String address = args[1];
                int connectPort = Integer.parseInt(args[2]);
                new Peer(listenPort, address, connectPort);
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
