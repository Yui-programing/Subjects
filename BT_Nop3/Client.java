package BT_Nop3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private String hostName = "";
    private int port;
    String input;
    String respone;

    public Client(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public void run() {
        try (Socket socket = new Socket(hostName, port)) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter writer = new PrintWriter(
                            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    Scanner sc = new Scanner(System.in)) {
                while (true) {
                    System.out.print("Client gửi: ");
                    input = sc.nextLine();
                    writer.println(input);
                    respone = reader.readLine();
                    System.out.println(respone);
                    if (input.equalsIgnoreCase("bye")) {
                        break;
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 12345);
        client.run();
    }
}
