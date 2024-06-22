import valuetuples.ValueTuplesHandler;

import java.io.*;
import java.lang.reflect.MalformedParametersException;
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
        requestsTimes = Collections.synchronizedList(new ArrayList<>());
        this.commandProcessingFunction = this::process;
        executorService = Executors.newFixedThreadPool(concurrentClients);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    System.out.printf("[%1$tY-%1$tm-%1$td %1$tT] Connection from %2$s .%n%n", System.currentTimeMillis(), socket.getInetAddress());
                    executorService.submit(() -> {
                        try (socket) {
                            int requestsCounter = 0;
                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            while (true) {
                                String command = br.readLine();
                                if (command == null) {
                                    System.err.println("Client abruptly closed connection");
                                    System.out.printf("[%1$tY-%1$tm-%1$td %1$tT] Disconnection of %2$s after %3$d requests .%n\n", System.currentTimeMillis(), socket.getInetAddress(), requestsCounter);
                                    break;
                                }
                                if (command.equals(quitCommand)) {
                                    System.out.printf("[%1$tY-%1$tm-%1$td %1$tT] Disconnection of %2$s after %3$d requests .%n\n", System.currentTimeMillis(), socket.getInetAddress(), requestsCounter);
                                    break;
                                }
                                bw.write(commandProcessingFunction.apply(socket.getInetAddress() + ">" + command) + System.lineSeparator());
                                bw.flush();
                                requestsCounter++;
                            }
                        } catch (IOException e) {
                            System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] IO error: %s %3$s", System.currentTimeMillis(), socket.getInetAddress(), e);
                        }
                    });
                } catch (IOException e) {
                    System.err.printf("[%1$tY-%1$tm-%1$td %1$tT] Cannot accept connection due to %2$s", System.currentTimeMillis(), e);
                }
            }
        } finally {
            executorService.shutdown();
        }
    }

    private String process(String requestAndIpAddress) {
        String ipAddress = requestAndIpAddress.split(">")[0];
        String request;
        try {
            request = requestAndIpAddress.split(">")[1];
        } catch (ArrayIndexOutOfBoundsException e) {

            System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Empty request%n", System.currentTimeMillis(), ipAddress);

            return "ERR;(SyntaxError) Empty request";
        }
        if (requestAndIpAddress.split(">").length != 2) {
            System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Unknown character used", System.currentTimeMillis(), ipAddress);
            return "ERR;(SyntaxError) Unknown character used";
        }
        double startTime = System.nanoTime();
        String response;
        double outputValue = 0.0;
        String[] splitRequest;
        String requestType;
        String computationKind;
        String requestInfo;
        String variableValuesFunction;
        try {
            splitRequest = request.split(";");
            requestType = splitRequest[0];
            computationKind = requestType.split("_")[0];
            requestInfo = requestType.split("_")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Missing argument in %3$s%n", System.currentTimeMillis(), ipAddress, request);

            return "ERR;(SyntaxError) Missing argument";
        }

        if (computationKind.equals("STAT")) {
            if (requestsTimes.isEmpty()) {
                System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) (Invalid Request) No computation has ever been done in %3$s%n", System.currentTimeMillis(), ipAddress, request);
                return "ERR;(SyntaxError) (Invalid Request) No computation has ever been done";
            }

            try {
                if (!requestType.split("_")[2].equals("TIME")) {
                    System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Unknown command %3$s in %4$s%n", System.currentTimeMillis(), ipAddress, requestType.split("_")[2], request);
                    return String.format("ERR;(SyntaxError) Unknown command %1$s", requestType.split("_")[2]);
                } else {
                    if (requestInfo.equals("REQS")) {
                        System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Too many argument in %4$s%n", System.currentTimeMillis(), ipAddress, requestType.split("_")[2], request);
                        return "ERR;(SyntaxError) Too many argument";
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                if (!requestInfo.equals("REQS")) {
                    System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Missing argument in %3$s%n", System.currentTimeMillis(), ipAddress, request);
                    return "ERR;(SyntaxError) Missing argument";
                }

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
                    System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Unknown command in %3$s%n", System.currentTimeMillis(), ipAddress, request);
                    return "ERR;(SyntaxError) Unknown command" + requestInfo;
            }
        } else if (computationKind.equals("MAX") || computationKind.equals("MIN") || computationKind.equals("AVG") || computationKind.equals("COUNT")) {
            String[] expressions;
            try {
                variableValuesFunction = splitRequest[1];
                expressions = new String[splitRequest.length - 2];
                System.arraycopy(splitRequest, 2, expressions, 0, expressions.length);
            } catch (ArrayIndexOutOfBoundsException e) {

                System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Missing argument in %3$s%n", System.currentTimeMillis(), ipAddress, request);
                return "ERR;(SyntaxError) Missing argument";
            }
            ValueTuplesHandler valueTuples;
            try {
                valueTuples = new ValueTuplesHandler(requestInfo, variableValuesFunction);

            } catch (MalformedParametersException e) {
                System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) %3$s in %4$s%n", System.currentTimeMillis(), ipAddress, e.getMessage(), request);
                return "ERR;(SyntaxError) " + e.getMessage();
            }
            try {
                if (!computationKind.equals("COUNT")) {
                    valueTuples.setExpressions(expressions);
                }

            } catch (RuntimeException e) {
                System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (ComputationException) %3$s in %4$s%n", System.currentTimeMillis(), ipAddress, e.getMessage(), request);
                return "ERR;(ComputationException) " + e.getMessage();
            }

            outputValue = switch (computationKind) {
                case "MIN" -> valueTuples.getMin();
                case "MAX" -> valueTuples.getMax();
                case "COUNT" -> valueTuples.size();
                case "AVG" -> valueTuples.getAVG();
                default -> outputValue;
            };
        } else {
            System.err.printf("[%1$tY-%1$tm-%1$td %1$tT from %2$s] (SyntaxError) Unknown command %3$s in %4$s%n", System.currentTimeMillis(), ipAddress, computationKind, request);

            System.err.println("Client " + ipAddress + ":(SyntaxError) Unknown command " + computationKind + "in request: " + request);
            return "ERR;(SyntaxError) Unknown command " + computationKind;
        }
        double endTime = System.nanoTime();
        double responseTime = (endTime - startTime) / 1_000_000_000.0;
        requestsTimes.add(responseTime);

        response = String.format("OK;%.3f;%.6f", responseTime, outputValue);
        return response;
    }
}