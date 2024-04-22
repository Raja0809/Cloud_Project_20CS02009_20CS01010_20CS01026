import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Define Signal enum
enum Signal {
    REQUEST, // Signals meant to request the critical section
    REPLY, // Signals meant to reply to a request
    RELEASE // Signals meant to release the critical section
}

public class Lamport {
    private int processId;
    private int logicalClock;
    private int listenPort;
    private Map<Integer, InetAddress> nodeList;
    private PriorityQueue<Request> requestQueue;
    private Set<Integer> replyMap;
    private Lock clockLock;

    public Lamport(int id, int lport, String filename) {
        processId = id;
        listenPort = lport;
        logicalClock = 0;
        nodeList = new HashMap<>();
        requestQueue = new PriorityQueue<>();
        replyMap = new HashSet<>();
        clockLock = new ReentrantLock();
        loadConfig(filename);
    }

    private void loadConfig(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int id = Integer.parseInt(parts[0]);
                InetAddress address = InetAddress.getByName(parts[1]);
                nodeList.put(id, address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNode(int id, InetAddress address) {
        nodeList.put(id, address);
    }

    public void request() {
        clockLock.lock();
        logicalClock++;
        clockLock.unlock();
        broadcast(Signal.REQUEST);
        requestQueue.add(new Request(logicalClock, processId));
    }

    public void receive() {
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleData(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleData(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            SyncData data = (SyncData) in.readObject();
            clockLock.lock();
            logicalClock = Math.max(logicalClock, data.timestamp) + 1;
            clockLock.unlock();
            switch (data.msgType) {
                case REQUEST:
                    requestQueue.add(new Request(data.timestamp, data.senderId));
                    if (requestQueue.peek().senderId != processId)
                        unicast(Signal.REPLY, data.senderId);
                    break;
                case REPLY:
                    replyMap.add(data.senderId);
                    break;
                case RELEASE:
                    if (requestQueue.peek().senderId == data.senderId)
                        requestQueue.poll();
                    else {
                        System.err.println("Invalid release");
                        System.exit(1);
                    }
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void handleQueue() {
        while (true) {
            if (!requestQueue.isEmpty()) {
                Request top = requestQueue.peek();
                if (top.senderId == processId) {
                    if (replyMap.size() == nodeList.size() - 1) {
                        clockLock.lock();
                        logicalClock++;
                        clockLock.unlock();
                        System.out.println(logicalClock + ": Entering Critical Section");
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        broadcast(Signal.RELEASE);
                        replyMap.clear();
                        requestQueue.poll();
                        System.out.println(logicalClock + ": Exiting Critical Section");
                    }
                }
            }
        }
    }

    public void broadcast(Signal sig) {
        nodeList.forEach((id, address) -> {
            if (id != processId)
                unicast(sig, id);
        });
    }

    public void unicast(Signal sig, int sysId) {
        try (Socket socket = new Socket(nodeList.get(sysId), listenPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            clockLock.lock();
            SyncData data = new SyncData(logicalClock, processId, sig);
            out.writeObject(data);
            clockLock.unlock();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printConfig() {
        clockLock.lock();
        System.out.println("ID: " + processId + " Port: " + listenPort + " Clock: " + logicalClock);
        System.out.println("Node List:");
        nodeList.forEach((id, address) -> {
            System.out.println("ID: " + id + " IP: " + address.getHostAddress());
        });
        System.out.println("Request Queue:");
        requestQueue.forEach(request -> {
            System.out.println("ID: " + request.senderId + " TIMESTAMP: " + request.timestamp);
        });
        System.out.println("Reply Map:");
        replyMap.forEach(System.out::println);
        clockLock.unlock();
    }

    private class Request implements Comparable<Request> {
        int timestamp;
        int senderId;

        public Request(int timestamp, int senderId) {
            this.timestamp = timestamp;
            this.senderId = senderId;
        }

        @Override
        public int compareTo(Request o) {
            return Integer.compare(timestamp, o.timestamp);
        }
    }
}
