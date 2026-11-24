package BT_Nop4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private String host;
    private int port;
    private int bufferSize;

    public Client(String host, int port, int bufferSize) {
        this.host = host;
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket();
                Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(host);
            while (true) {
                System.out.print("Nhập dữ liệu: ");
                String input = sc.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("client yeu cau ket thuc");
                    break;
                }
                sendData(socket, address, input);
                String receivedData = receiveData(socket);
                System.out.println(receivedData);

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // Gửi dữ liệu đến Server
    private void sendData(DatagramSocket socket, InetAddress address, String input) throws IOException {
        byte[] dataBytes = input.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, address, port);
        socket.send(packet);
    }

    // Nhận dữ liệu từ Server
    private String receiveData(DatagramSocket socket) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(packet);
        byte[] dataBytes = Arrays.copyOf(packet.getData(), packet.getLength());
        return new String(dataBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 40089, 1024);
        client.start();
    }
}
