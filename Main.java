import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        final int port = 1000;
        final String quitCommand = "BYE";
        final int threadsNumber = Runtime.getRuntime().availableProcessors();
        (new ExecutorLineProcessingServer(port,quitCommand,threadsNumber)).start();
    }

}
