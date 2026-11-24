package BT_Nop1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String serverAddress;
    private int port;
    private String url;
    Scanner sc = new Scanner(System.in);

    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.sc = new Scanner(System.in);
    }

    void Start() {
        try (Socket socket = new Socket(serverAddress, port)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"),
                            true)) {
                while (true) {
                    url = getInput(sc);
                    writer.println(url);
                   
                    if (url.equalsIgnoreCase("exit")) {
                        System.out.println("Client đóng kết nối");
                        break;
                    }
                    String respone;
                    while((respone = reader.readLine()) != null){
                        if(respone.equals("END")) break;
                        System.out.println(respone);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    String getInput(Scanner sc) {
        System.out.println("Nhập dữ liệu: ");
        return sc.nextLine();
    }
    public static void main(String[] args) {
        Client client = new Client("localhost", 1234);
        client.Start();
    }
}