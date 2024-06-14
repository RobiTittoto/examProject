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
        Function<String, String> process = Main::process; //meglio con process come statico o dinamico?
        (new ExecutorLineProcessingServer(port,quitCommand,process,threadsNumber)).start();

    }

    public enum valuesKind{
        GRID,
        LIST
    }

    private static String process(String request) {
        long startTime = System.currentTimeMillis();
        String[] splitRequest = request.split(";");
        String computationKind = splitRequest[0].split("_")[0];
        String valuesKind = splitRequest[0].split("_")[1];
        String variableValuesFunction = splitRequest[1];
        String[] expressions = new String[splitRequest.length - 2];
        System.arraycopy(splitRequest, 2, expressions, 0, expressions.length);


        String response = "";

        if (computationKind.equals("STAT")) {
            System.out.println("stat request");
        } else if (computationKind.equals("MAX") || computationKind.equals("MIN") || computationKind.equals("AVG") || computationKind.equals("COUNT")) {

            Set<Map<String, Double>> valueTuples = setValueTuples(valuesKind, variableValuesFunction);
            System.out.println(valueTuples);
            NavigableSet<Double> calculatedValues = new TreeSet<>();

            for (String expression : expressions) {

                Node parsedExpression = (new Parser(expression)).parse();
                for (Map<String, Double> valueTuple : valueTuples) {
                    calculatedValues.add(getValue(parsedExpression,valueTuple));
                }
            }
            Double outputValue = 0.0;
            switch (computationKind) {
                case "MIN":
                    outputValue = calculatedValues.getFirst();
                    break;
                case "MAX":
                    outputValue = calculatedValues.getLast();
                    break;
                case "COUNT":
                    outputValue = (double) calculatedValues.size();
                    break;
                case "AVG":
                    double sum = 0;
                    for (Double value : calculatedValues) {
                        sum += value;
                    }
                    outputValue = sum / calculatedValues.size();
                    break;
            }

            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            response = "OK;" + elapsedTime + ";" + outputValue;
        }
        return response;
    }

    public static Set<Map<String, Double>> setValueTuples(String stringValuesKind, String variableValuesFunction) {
        Set<Map<String, Double>> valueTuples = new HashSet<>();
        valuesKind vk;
        if (stringValuesKind.equalsIgnoreCase("GRID")) {
            vk = valuesKind.GRID;
        } else if ((stringValuesKind.equalsIgnoreCase("LIST"))) {
            vk = valuesKind.LIST;
        } else {
            System.out.println("Errore");
            return null;
        }
        String[] variablesInfo = variableValuesFunction.split(",");
        for (String variableInfo : variablesInfo) {
            String[] variableInfoParts = variableInfo.split(":");
            String variableName = variableInfoParts[0];
            BigDecimal firstValue = new BigDecimal(variableInfoParts[1]);
            BigDecimal stepValue = new BigDecimal(variableInfoParts[2]);
            BigDecimal endValue = new BigDecimal(variableInfoParts[3]);
            List<Double> values = new ArrayList<>();
            for (BigDecimal j = firstValue; j.compareTo(endValue) <= 0; j = j.add(stepValue)) {
                values.add(j.setScale(10, RoundingMode.HALF_UP).doubleValue());
            }
            if (valueTuples.isEmpty()) {
                for (Double aDouble : values) {
                    Map<String, Double> temp = new HashMap<>();
                    temp.put(variableName, aDouble);
                    valueTuples.add(temp);
                }
            }
            if (vk == valuesKind.GRID) {
                Set<Map<String, Double>> grid = new HashSet<>();
                for (Map<String, Double> value : valueTuples) {
                    for (Double aDouble : values) {
                        Map<String, Double> temp = new HashMap<>(value);
                        temp.put(variableName, aDouble);
                        grid.add(temp);
                    }
                }
                valueTuples = grid;
            } else {
                Set<Map<String, Double>> list = new HashSet<>();
                int i = 0;
                for (Map<String, Double> value : valueTuples) {
                    Map<String, Double> temp = new HashMap<>(value);
                    temp.put(variableName, values.get(i));
                    //System.out.println("inserito nella lista "+ variableName +" con il valore "+values.get(i));
                    list.add(temp);
                    i++;
                }
                valueTuples = list;
            }
        }
        return valueTuples;
    }

    public static double executeExpression(Node operator, Map<String, Double> valueTuple) {

        double valueFirstChild = getValue(operator.getChildren().get(0), valueTuple);
        double valueSecondChild = getValue(operator.getChildren().get(1), valueTuple);
        // Cast to Operator only after confirming it is an instance of Operator
        if (operator instanceof Operator opt) {
            return opt.getType().getFunction().apply(new double[]{valueFirstChild, valueSecondChild});
        } else {
            throw new IllegalArgumentException("Node is not an instance of Operator");
        }
    }

    private static double getValue(Node node, Map<String, Double> valueTuple) {
        return switch (node) {
            case Variable variable -> valueTuple.get(variable.getName());
            case Constant constant -> constant.getValue();
            case Operator operator -> executeExpression(operator, valueTuple);
            case null, default -> throw new IllegalArgumentException("Unknown Node type");
        };
    }
}
