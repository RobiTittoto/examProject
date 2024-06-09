import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final String quitCommand;

    public ClientHandler(Socket socket, String quitCommand) {
        this.socket = socket;
        this.quitCommand = quitCommand;
    }

    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                String line = br.readLine();
                if (line.equals(quitCommand)) {
                    socket.close();
                    break;
                }
                bw.write(process(line) + System.lineSeparator());
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String process(String line) {
        return "";
    }
}
