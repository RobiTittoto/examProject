import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final Server server ;
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }
    public void run() { try {
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); while (true) {
            String line = br.readLine();
            if (line.equals( server.getQuitCommand() )) {
                socket.close();
                break; }
            bw.write(server. process(line) + System.lineSeparator());
            bw.flush(); }
    } catch (IOException e) { /* ... */
    } finally { /* ... */ } }
}