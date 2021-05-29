import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Recognize {
    public static boolean isConnectedRecognizeServer = false;   // 是否连接识别服务器
    public static Socket socket;
    public static InputStream inputStream;
    public static OutputStream outputStream;
    public static int socketId=999;
}
