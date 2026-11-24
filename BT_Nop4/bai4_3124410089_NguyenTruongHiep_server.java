package BT_Nop4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import BT_Nop4.bai4_3124410089_NguyenTruongHiep_thread;

public class bai4_3124410089_NguyenTruongHiep_server {
    private static final int ABC = 89; // mssv: 3124410089
    private static final int PORT = 40000 + ABC;
    private static final int MAX_N = 500 + ABC;
    private static final int MAX_ATTEMPTS = 12;

    private static final int N_THREADS = 10;
    private int bufferSize;
    private DatagramPacket receivePacket;

    private static final ConcurrentHashMap<Integer, Session> sessions = new ConcurrentHashMap<>();
    private static final AtomicInteger sessionIdGenerator = new AtomicInteger(1001);

    public bai4_3124410089_NguyenTruongHiep_server(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(N_THREADS);
        System.out.println("ThreadPool Server đang khởi tạo với " + N_THREADS + " thread...");
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server đang lắng nghe trên cổng " + PORT + "...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Đang shutdown ExecutorService...");
                shutdownExecutor(executorService);
                System.out.println("ExecutorService đã shutdown.");
            }));
            while (!executorService.isShutdown()) {
                try {
                    String input = receivedData(socket);
                    bai4_3124410089_NguyenTruongHiep_thread clientHandle = new bai4_3124410089_NguyenTruongHiep_thread(
                            socket, input, receivePacket.getAddress(), receivePacket.getPort(),
                            sessionIdGenerator, sessions, MAX_N, MAX_ATTEMPTS);
                    executorService.execute(clientHandle);
                    

                } catch (IOException e) {
                    if (executorService.isShutdown()) {
                        System.out.println("Server socket đã đóng do executor shutdown.");
                        break;
                    }
                    System.err.println("Lỗi khi chấp nhận kết nối client: " + e.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("Không thể khởi động server trên cổng " + PORT + ": " + ex.getMessage());
            shutdownExecutor(executorService);
        } finally {
            if (!executorService.isTerminated()) {
                System.out.println("Thực hiện shutdown cuối cùng cho ExecutorService...");
                shutdownExecutor(executorService);
            }
            System.out.println("Server đã dừng.");
        }
    }

    // Phương thức shutdown ExecutorService an toàn
    private static void shutdownExecutor(ExecutorService executor) {
        executor.shutdown(); // Ngừng nhận task mới
        try {
            // Đợi 60s cho các task hiện tại hoàn thành
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Hủy các task đang chạy nếu quá thời gian chờ
                // Đợi tiếp 60s để các task bị hủy phản hồi
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("ExecutorService không thể dừng hẳn.");
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // nhan data tu cclient
    private String receivedData(DatagramSocket socket) throws IOException {
        receivePacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(receivePacket);
        byte[] receivedBytes = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
        return new String(receivedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        bai4_3124410089_NguyenTruongHiep_server server = new bai4_3124410089_NguyenTruongHiep_server(1024);
        server.run();
    }
}
