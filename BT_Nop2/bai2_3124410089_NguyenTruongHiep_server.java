package BT_Nop2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.json.JSONObject;
import org.jsoup.Jsoup;

public class bai2_3124410089_NguyenTruongHiep_server {
    private int port;
    private int bufferSize;
    private DatagramPacket receivePacket;

    public bai2_3124410089_NguyenTruongHiep_server(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start() {
        System.out.println("server đang lắng nghe");
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                String receivedData = receivedData(socket);
                System.out.println("Server nhan " + receivedData);

                if (receivedData.toLowerCase().startsWith("weather")) {
                    String location = receivedData.substring("weather ".length());
                    sendData(socket, weatherHandler(location));
                } else if (receivedData.toLowerCase().startsWith("convert")) {
                    StringTokenizer token = new StringTokenizer(receivedData, " ");
                    token.nextToken();
                    sendData(socket, convertHandler(token.nextToken(), token.nextToken(), token.nextToken()));
                } else if (receivedData.equalsIgnoreCase("bye")) {
                    System.out.println("Server đóng kết nối.");
                    break;
                } else {
                    sendData(socket, "Lỗi dữ liệu đầu vào!");
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // su li thoi tiec
    private String weatherHandler(String location) {
        String apikey = "T2t70XhQSYeqDEJ8soNvQjcfQaIiZk0j";
        location = location.trim();
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = String.format("https://api.tomorrow.io/v4/weather/realtime?location=%s&apikey=%s",
                encodedLocation, apikey);
        return connectApi(url, 1);
    }

    // su li chuyen doi co so
    private String convertHandler(String from, String to, String number) {
        String url = String.format("https://networkcalc.com/api/binary/%s?from=%s&to=%s", number, from, to);
        return connectApi(url, 2);
    }

    // connect Api
    private String connectApi(String url, int type) {
        try {
            String json = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .execute()
                    .body();
            return jsonHandler(json, type);

        } catch (Exception e) {
            return "Lỗi kết nối API: " + e.getMessage();
        }
    }

    // Json handler
    String jsonHandler(String json, int type) {
        try {
            JSONObject object = new JSONObject(json);
            String result = "";
            switch (type) {
                case 1: // Su li weather
                    int temp = object.getJSONObject("data")
                            .getJSONObject("values")
                            .optInt("temperature");
                    String name = object.getJSONObject("location")
                            .optString("name");
                    result = String.format("--> Chức năng tra nhiệt độ: %s có nhiệt độ hiện tại là %d độ C\n", name,
                            temp);
                    break;
                case 2:// Su li chuyen doi co so
                    String ori = object.optString("original");
                    String converted = object.optString("converted");
                    String from = object.optString("from");
                    String to = object.optString("to");
                    result = String.format("--> Kết quả đổi %s từ cơ số %s sang cơ số %s: %s \n", ori, from, to,
                            converted);
                    break;
                default:
                    break;
            }
            return result;
        } catch (Exception e) {
            return "Lỗi kết nối API: " + e.getMessage();
        }
    }

    // nhan data tu cclient
    private String receivedData(DatagramSocket socket) throws IOException {
        receivePacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(receivePacket);
        byte[] receivedBytes = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
        return new String(receivedBytes, StandardCharsets.UTF_8);
    }

    // gui data den client
    private void sendData(DatagramSocket socket, String receivedData) throws IOException {
        String response = receivedData;
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);// chuyen doi byte
        DatagramPacket packet = new DatagramPacket(responseBytes, responseBytes.length, receivePacket.getAddress(),
                receivePacket.getPort());
        socket.send(packet);
    }

    public static void main(String[] args) {
        bai2_3124410089_NguyenTruongHiep_server server = new bai2_3124410089_NguyenTruongHiep_server(1234, 1024);
        server.start();
    }
}
