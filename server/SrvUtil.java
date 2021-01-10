import java.net.*;

public class SrvUtil {
    static InetAddress findAddress() throws SocketException, UnknownHostException {
        // in macOS change parameter "lo" to "en0"
        var lo = NetworkInterface.getByName("lo");
        return lo.inetAddresses()
                .filter(a -> a instanceof Inet4Address)
                .findFirst()
                .orElse(InetAddress.getLocalHost());
    }
}
