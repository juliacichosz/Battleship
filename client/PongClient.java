import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class PongClient {

    public static Ships ships = new Ships();

    PongClient(String host, int port, String mapPath) throws IOException {
        Socket s = new Socket(host, port);
        File mapFile = BattleShip.generateMapFile(mapPath, ships);
        PongSession session = new PongSession(s, PingPongProtocol.PING, mapFile, ships);
        new Thread(session, "[PongClient-]").start();
    }

    /*
    // UNCOMMENT IN ORDER TO PLAY ON ONE DEVICE.
    // Run PongClient's main method after running PongServer's main method.

    public static void main(String[] args) {
        try {
            //for example:
            int port = 8080; //port to make connection
            String mapPath = "clientsMap.txt"; //path to file with generated map for Client

            String addr = SrvUtil.findAddress().toString().substring(1);
            PongClient c = new PongClient(addr, port, mapPath);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    */
}
