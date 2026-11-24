package BT_Nop4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.InetAddress;

class Session {
    final int secretNumber;
    int attempts = 0;
    final Map<Integer, String> history = new HashMap<>();

    Session(int secretNumber) {
        this.secretNumber = secretNumber;
    }
}

public class bai4_3124410089_NguyenTruongHiep_thread implements Runnable {
    private DatagramSocket socket;
    private DatagramPacket receivePacket;

    private InetAddress clientAddr;
    private int clientPort;

    private int maxN;
    private int maxAttempts;

    private final AtomicInteger sessionIdGenerator;
    private final ConcurrentHashMap<Integer, Session> sessions;
    private String input;

    public bai4_3124410089_NguyenTruongHiep_thread(DatagramSocket socket, String input, InetAddress clientAddr, int clientPort,
            AtomicInteger sessionIdGenerator, ConcurrentHashMap<Integer, Session> sessions,
            int MAX_N, int MAX_ATTEMPTS) {
        this.socket = socket;
        this.input = input;
        this.clientAddr = clientAddr;
        this.clientPort = clientPort;
        this.maxN = MAX_N;
        this.maxAttempts = MAX_ATTEMPTS;
        this.sessionIdGenerator = sessionIdGenerator;
        this.sessions = sessions;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println(
                "[" + threadName + "] Bắt đầu xử lý client: " + clientAddr.getHostAddress());
        try {
            String respone = playGame(input);
            sendData(socket, respone);
        } catch (Exception e) {
            System.out.println("ERROR");
        }
    }

    public String playGame(String input) {
        if (input.isEmpty())
            return "ERR SYNTAX";
        String[] parts = input.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "start":
                if (parts.length != 1)
                    return "ERR SYNTAX";
                int sessionId = sessionIdGenerator.getAndIncrement();
                int secretNumber = new Random().nextInt(maxN) + 1;
                System.out.println("Phiên " + sessionId + " - Số bí mật: " + secretNumber);
                sessions.put(sessionId, new Session(secretNumber));
                return "OK " + sessionId + " " + maxN + " " + maxAttempts;

            case "guess":
                if (parts.length != 3)
                    return "ERR SYNTAX";
                int id;
                int guessNumber;
                try {
                    id = Integer.parseInt(parts[1]);
                    guessNumber = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    return "ERR SYNTAX";
                }
                if (guessNumber < 1 || guessNumber > maxN)
                    return "ERR SYNTAX";
                Session session = sessions.get(id);

                if (session == null)
                    return "ERR SESSION";

                synchronized (session) {
                    if (session.history.containsKey(guessNumber)) {
                        return session.history.get(guessNumber);
                    }
                    session.attempts++;
                    String result;
                    if (guessNumber == session.secretNumber) {
                        result = guessNumber + " CORRECT " + session.attempts + "/" + maxAttempts;
                        sessions.remove(id);
                    } else if (session.attempts >= maxAttempts) {
                        result = guessNumber + " LOSE " + session.secretNumber;
                        sessions.remove(id);
                    } else if (guessNumber < session.secretNumber)
                        result = guessNumber + " HIGHER " + session.attempts + "/" + maxAttempts;
                    else
                        result = guessNumber + " LOWER " + session.attempts + "/" + maxAttempts;
                    session.history.put(guessNumber, result);
                    return result;
                }
            case "quit":
                if (parts.length != 2)
                    return "ERR SYNTAX";
                try {
                    int quitId = Integer.parseInt(parts[1]);
                    if (sessions.remove(quitId) == null) {
                        return "ERR SESSION";
                    }
                    return "BYE";
                } catch (NumberFormatException e) {
                    return "ERR SYNTAX";
                }
            default:
                return "ERR SYNTAX";
        }

    }

    // gui data den client
    private void sendData(DatagramSocket socket, String receivedData) throws IOException {
        String response = receivedData;
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);// chuyen doi byte
        DatagramPacket packet = new DatagramPacket(responseBytes, responseBytes.length, clientAddr, clientPort);
        socket.send(packet);
    }

}
