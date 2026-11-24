package BT_Nop1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class bai1_3124410089_NguyenTruongHiep_server {
    private int port = 1234;
    private String data;
    private String spid;

    void Start() {
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("Server đang lắng nghe!");
            while (true) {
                try (Socket client = socket.accept();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true)) {

                    System.out.println("Client" + "is connected.");
                    while ((data = reader.readLine()) != null) {
                        System.out.println("Server nhận data: " + data);

                        if (data.equalsIgnoreCase("exit")) {
                            socket.close();
                        } else if (data == null || !data.toLowerCase().contains("tiki.vn")) {
                            writer.println("Lỗi tra cứu thông tin, Vui lòng thử lại!");
                            writer.println("END");
                        }

                        else {
                            spid = urlHandler(data);
                            String ketqua = connectApi(spid);
                            writer.println(ketqua);
                            writer.println("END"); // bao het tin nhan
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("lỗi kết nối" + e.getMessage());
        }
    }

    String urlHandler(String url) {

        int startIndex = url.lastIndexOf("-p");
        int endIndex = url.indexOf(".html");
        String spid = url.substring(startIndex + 2, endIndex);
        return spid;

    }

    String connectApi(String spid) {
        try {
            String api1 = "https://tiki.vn/api/v2/products/" + spid;
            String api2 = "https://tiki.vn/api/v2/reviews?product_id=" + spid + "&limit=10";
            String jsonProduct = Jsoup.connect(api1)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .execute()
                    .body();
            String jsonReview = Jsoup.connect(api2)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .execute()
                    .body();
            return jsonHandler(jsonProduct, jsonReview);
        } catch (Exception e) {
            return "Lỗi kết nối API: " + e.getMessage();
        }
    }

    String jsonHandler(String product, String review) {
        try {
            JSONObject objproduct = new JSONObject(product);
            JSONObject objreview = new JSONObject(review);
            StringBuilder result = new StringBuilder();

            String name = objproduct.optString("name"); // ten san pham
            int price = objproduct.optInt("price"); // gia ban hien tai
            int countReview = objreview.optInt("reviews_count"); // so luong danh gia
            double ratingAverage = objreview.optDouble("rating_average");// diem danh gia trung binh
            JSONArray listReviews = objreview.optJSONArray("data"); // list cac danh gia

            int limit = Math.min(10, listReviews.length());
            result.append(String.format("Tên sản phẩm: %s. Giá: %,d \n", name, price));
            result.append(
                    String.format("Sản phẩm có %d review với điểm trung bình là: %5f \n", countReview, ratingAverage));
            result.append("Dưới đây là các review về sản phẩm trên Tiki\n");

            for (int i = 0; i < limit; i++) {
                JSONObject item = listReviews.getJSONObject(i);
                String content = item.optString("content").trim();
                if (!content.isEmpty())
                    result.append(String.format("+ %s \n", content));
            }
            return result.toString();
        } catch (Exception e) {
            return "Lỗi sử lí JSON: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        bai1_3124410089_NguyenTruongHiep_server server = new bai1_3124410089_NguyenTruongHiep_server();
        server.Start();
    }
}
