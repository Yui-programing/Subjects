package BT_Nop3;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.jsoup.Jsoup;

public class bai3_3124410089_NguyenTruongHiep_thread implements Runnable {
    private Socket clientSocket;

    public bai3_3124410089_NguyenTruongHiep_thread(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println(
                "[" + threadName + "] Bắt đầu xử lý client: " + clientSocket.getInetAddress().getHostAddress());
        try (Socket autoCloseablSocket = clientSocket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {
            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                String msg = clientMessage.trim();
                System.out.println("[" + threadName + "] Nhận từ client "
                        + clientSocket.getInetAddress().getHostAddress() + ": " + msg);

                if (msg.toLowerCase().startsWith("weather")) {

                    String location = msg.length() > 7 ? msg.substring(7).trim() : "";
                    if (location.isEmpty())
                        writer.println("Lỗi dữ liệu đầu vào!");
                    else
                        writer.println(weatherHandler(location));

                } else if (msg.toLowerCase().startsWith("calc")) {

                    String input = msg.length() > 4 ? msg.substring(4).trim() : "";
                    if (input.isEmpty())
                        writer.println("Lỗi dữ liệu đầu vào!");
                    else
                        writer.println(calcHandler(input));
                } else if ("bye".equalsIgnoreCase(clientMessage.trim())) {
                        writer.println("Client đã đóng socket");
                    break;
                } else {
                    writer.println("Lỗi dữ liệu đầu vào!");
                }

            }
        } catch (IOException e) {
            System.err.println("[" + threadName + "] Lỗi I/O với client "
                    + (clientSocket != null ? clientSocket.getInetAddress().getHostAddress() : "unknown") + ": "
                    + e.getMessage());
        }
        System.out.println("[" + threadName + "] Kết thúc xử lý client: "
                + (clientSocket != null ? clientSocket.getInetAddress().getHostAddress() : "unknown"));
    }

    // su li thoi tiec
    private String weatherHandler(String location) {
        String apikey = "T2t70XhQSYeqDEJ8soNvQjcfQaIiZk0j";
        location = location.trim();
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = String.format("https://api.tomorrow.io/v4/weather/realtime?location=%s&apikey=%s",
                encodedLocation, apikey);

        try {
            String json = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .execute()
                    .body();
            JSONObject object = new JSONObject(json);
            int temp = object.getJSONObject("data")
                    .getJSONObject("values")
                    .optInt("temperature");
            String name = object.getJSONObject("location")
                    .optString("name");
            String result = String.format("--> Chức năng tra nhiệt độ: %s có nhiệt độ hiện tại là %d độ C", name,
                    temp);
            return result;
        } catch (Exception e) {
            return "Lỗi kết nối API: " + e.getMessage();
        }

    }

    private String calcHandler(String input) {
        String encodedInput = URLEncoder.encode(input.trim(), StandardCharsets.UTF_8);
        String url = String.format("http://api.mathjs.org/v4/?expr=%s", encodedInput);
        try {
            String result = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .execute()
                    .body()
                    .trim();
            if (result.toLowerCase().startsWith("error"))
                return "-->Phép tính có lỗi, không tính kết quả được.";
            return "-->Kết quả phép tính: " + result;
        } catch (Exception e) {
            return "Lỗi kết nối API: " + e.getMessage();
        }
    }

}
