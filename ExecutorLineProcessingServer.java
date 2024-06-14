import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class ExecutorLineProcessingServer {
    private final int port;
    private final String quitCommand;
    private final Function<String, Double> commandProcessingFunction;
    private final ExecutorService executorService;

    private int requestCount = 0;
    private final List<Double> requestsTimes = new ArrayList<>();

    public ExecutorLineProcessingServer(int port, String quitCommand, int concurrentClients) {
        this.port = port;
        this.quitCommand = quitCommand;
        this.commandProcessingFunction = this::process;
        executorService = Executors.newFixedThreadPool(concurrentClients);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    System.out.println("New client : " + socket.getInetAddress());
                    executorService.submit(() -> {
                        try (socket) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            while (true) {
                                String command = br.readLine();
                                double startTime = System.nanoTime();
                                if (command == null) {
                                    System.err.println("Client abruptly closed connection");
                                    break;
                                }
                                if (command.equals(quitCommand)) {
                                    break;
                                }
                                Double calculatedValue = commandProcessingFunction.apply(command);
                                double endTime = System.nanoTime();
                                double responseTime = (endTime - startTime) / 1_000_000_000.0;
                                bw.write("OK;" + responseTime + ";" + calculatedValue + System.lineSeparator());
                                bw.flush();
                                synchronized (this) {
                                    requestCount++;
                                    requestsTimes.add(responseTime);
                                }
                            }
                        } catch (IOException e) {
                            System.err.printf("IO error: %s", e);
                        }
                    });
                } catch (IOException e) {
                    System.err.printf("Cannot accept connection due to %s", e);
                }
            }
        } finally {
            executorService.shutdown();
        }
    }

    private Double process(String request) {
        double outputValue = 0.0;
        String[] splitRequest = request.split(";");
        String requestType = splitRequest[0];

        String computationKind = requestType.split("_")[0];
        System.out.println(computationKind);
        String requestInfo = requestType.split("_")[1];


        if (computationKind.equals("STAT")) {
            switch (requestInfo) {
                case "REQS":
                    outputValue = requestCount;
                    break;
                case "MAX":

                    outputValue = Collections.max(requestsTimes);
                    break;
                case "AVG":
                    double sum = 0.0;
                    for (Double requestTime : requestsTimes) {
                        sum += requestTime;
                    }
                    outputValue = sum / requestsTimes.size();
                    break;
                case null, default:
                    outputValue = 0000000000.0;

            }
            System.out.println(outputValue);
        } else if (computationKind.equals("MAX") || computationKind.equals("MIN") || computationKind.equals("AVG") || computationKind.equals("COUNT")) {
            String variableValuesFunction = splitRequest[1];
            String[] expressions = new String[splitRequest.length - 2];
            System.arraycopy(splitRequest, 2, expressions, 0, expressions.length);
            ValueTuplesHandler valueTuples = new ValueTuplesHandler(requestInfo, variableValuesFunction);
            valueTuples.setExpressions(expressions);
            switch (computationKind) {
                case "MIN":
                    outputValue = valueTuples.getMin();
                    break;
                case "MAX":
                    outputValue = valueTuples.getMax();
                    break;
                case "COUNT":
                    outputValue = valueTuples.getCount();
                    break;
                case "AVG":
                    outputValue = valueTuples.getAVG();
                    break;
            }

        }
        return outputValue;
    }
}