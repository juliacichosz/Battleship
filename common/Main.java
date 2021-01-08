import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void startAsServer(int port, String mapPath) {
        try {
            InetAddress addr = SrvUtil.findAddress();
            PongServer pongServer = new PongServer(addr, port, mapPath);
            new Thread(pongServer, "PongServer").start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void startAsClient(int port, String mapPath) {
        try {
            String addr = SrvUtil.findAddress().toString().substring(1);
            PongClient c = new PongClient(addr, port, mapPath);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String mode = args[1].substring(1,7);
        int port = Integer.parseInt(args[3]);
        String mapPath = args[5];

        switch (mode) {
            case "server" -> startAsServer(port, mapPath);
            case "client" -> startAsClient(port, mapPath);
        }
    }
}
