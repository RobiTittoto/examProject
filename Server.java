import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int port;
    private final String quitCommand;

    public Server(int port, String quitCommand) { this.port = port;
        this.quitCommand = quitCommand; }
    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port); while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(socket, this) ;
            clientHandler.start() ; }
    }


    public String process(String input) {
        return input; }




    public String getQuitCommand() {
        return quitCommand; }
}
