import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class LineProcessingServer {
    private final int port;
    private final String quitCommand;
    private final Function<String, String> commandProcessingFunction;
    private final ExecutorService executorService;

    private final List<Double> requestsTimes;

    public LineProcessingServer(int port, String quitCommand, int concurrentClients) {
        this.port = port;
        this.quitCommand = quitCommand;
        requestsTimes= Collections.synchronizedList(new ArrayList<>());
        this.commandProcessingFunction = this::process;
        executorService = Executors.newFixedThreadPool(concurrentClients);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    System.out.println("Opened connection with client "+ socket.getInetAddress());
                    executorService.submit(() -> {
                        try (socket) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            while (true) {
                                String command = br.readLine();
                                if (command == null) {
                                    System.err.println("Client abruptly closed connection");
                                    System.out.println("Closed connection with client "+socket.getInetAddress());
                                    break;
                                }
                                if (command.equals(quitCommand)) {
                                    System.out.println("Closed connection with client "+socket.getInetAddress());
                                    break;
                                }
                                bw.write(commandProcessingFunction.apply(command) + System.lineSeparator());
                                bw.flush();
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
    private String process(String request) {
        double startTime = System.nanoTime();
        String response;
        double outputValue=0.0;
        String[] splitRequest;
        String requestType;
        String computationKind;
        String requestInfo;
        String variableValuesFunction;
        try{
            splitRequest = request.split(";");
            requestType = splitRequest[0];
            computationKind = requestType.split("_")[0];
            requestInfo = requestType.split("_")[1];
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("Syntax error in line: " + request);
            return "ERR;(SyntaxError) Missing argument";
        }

        if (computationKind.equals("STAT")) {
            if(requestsTimes.isEmpty()){
                return "ERR;(Invalid Request) No computation has ever been done";
            }
            try{
                if(!requestType.split("_")[2].equals("TIME"))
                    return "ERR;(SyntaxError) Unknown command";
            } catch (ArrayIndexOutOfBoundsException e){
                return "ERR;(SyntaxError) Missing argument";
            }
            switch (requestInfo) {
                case "REQS":
                    outputValue = requestsTimes.size();
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
                    System.err.println("Syntax error in line" + request);
                    return "ERR;(SyntaxError) Unknown command" + requestInfo;
            }
        } else if (computationKind.equals("MAX") || computationKind.equals("MIN") || computationKind.equals("AVG") || computationKind.equals("COUNT")) {
            try{
                variableValuesFunction = splitRequest[1];
            } catch (ArrayIndexOutOfBoundsException e){
                return "ERR;(SyntaxError) Missing argument";
            }

            String[] expressions = new String[splitRequest.length - 2];
            System.arraycopy(splitRequest, 2, expressions, 0, expressions.length);
            ValueTuplesHandler valueTuples = new ValueTuplesHandler(requestInfo, variableValuesFunction);
            try {
                valueTuples.setExpressions(expressions);
            }catch (IllegalArgumentException e){
                System.err.println("Syntax error in expression");
                return "ERR;(ComputationException)" + e;
            }

            outputValue = switch (computationKind) {
                case "MIN" -> valueTuples.getMin();
                case "MAX" -> valueTuples.getMax();
                case "COUNT" -> valueTuples.getCount();
                case "AVG" -> valueTuples.getAVG();
                default -> outputValue;
            };
        } else {
            System.err.println("Syntax error in line" + request);
            return "ERR;(SyntaxError) Unknown command" + computationKind;
        }
        double endTime = System.nanoTime();
        double responseTime = (endTime - startTime) / 1_000_000_000.0;
        requestsTimes.add(responseTime);

        response = "OK;"+responseTime+";" + outputValue;
        return response;
    }
}