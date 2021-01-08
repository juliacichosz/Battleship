import java.io.File;
import java.io.IOException;
import java.net.*;

public class PongServer implements Runnable {

    public static Ships ships = new Ships();
    private final ServerSocket serverSocket;
    private static File mapFile;

    public PongServer(InetAddress address, int port, String mapPath) throws IOException {
        mapFile = BattleShip.generateMapFile(mapPath, ships);
        serverSocket = new ServerSocket(port, 10000, address);
        System.out.println("Running PongServer at address: " + address + ", port: " + port);
    }

    @Override
    public void run() {
        int sessionId = 0;
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Got request from " + socket.getRemoteSocketAddress() + ", starting session " + sessionId);
                PongSession session = new PongSession(socket, PingPongProtocol.PONG, mapFile, ships);
                new Thread(session, "SrvSession-" + sessionId).start();
                sessionId++;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /*
    // UNCOMMENT IN ORDER TO PLAY ON ONE DEVICE.
    // Run PongServer's main method before running PongClient's main method.

    public static void main(String[] args) {
        try {
            //for example:
            int port = 8080; //port to make connection
            String mapPath = "serversMap.txt"; //path to file with generated map for Server

            InetAddress addr = SrvUtil.findAddress();
            PongServer pongServer = new PongServer(addr, port, mapPath);
            new Thread(pongServer, "PongServer").start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    */
}
