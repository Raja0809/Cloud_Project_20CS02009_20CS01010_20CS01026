import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        int port = 0;
        int sys_id = 0;
        String filename = "";

        if (args.length != 6) {
            System.err.println("Usage: java Main -p <PORT NO> -i <SYS ID> -f <CONFIG FILE>");
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p")) {
                port = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-i")) {
                sys_id = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-f")) {
                filename = args[++i];
            } else {
                System.err.println("Usage: java Main -p <PORT NO> -i <SYS ID> -f <CONFIG FILE>");
                return;
            }
        }

        Lamport lamport = new Lamport(sys_id, port, filename);

        Thread listenerThread = new Thread(() -> lamport.receive());
        Thread queueHandlerThread = new Thread(() -> lamport.handleQueue());
        Thread inputHandlerThread = new Thread(() -> handleInput(lamport));

        listenerThread.start();
        queueHandlerThread.start();
        inputHandlerThread.start();

        try {
            listenerThread.join();
            queueHandlerThread.join();
            inputHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void handleInput(Lamport lamport) {
        System.out.println("Available commands: REQUEST, EXIT, STATUS");
        BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(System.in));
        String input;
        try {
            while (true) {
                System.out.print(">");
                input = reader.readLine();
                if (input.equals("REQUEST")) {
                    lamport.request();
                } else if (input.equals("EXIT")) {
                    return;
                } else if (input.equals("STATUS")) {
                    lamport.printConfig();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
