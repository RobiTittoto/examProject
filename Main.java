import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        final String quitCommand = "BYE";
        final int threadsNumber = Runtime.getRuntime().availableProcessors();
        (new LineProcessingServer(Integer.parseInt(args[0]), quitCommand, threadsNumber)).start();
    }

}
