import java.io.DataInputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final String quitCommand;
    public ClientHandler(Socket socket, String quitCommand) {
        this.socket = socket;
        this.quitCommand = quitCommand;
    }
    public void run() {}
}
