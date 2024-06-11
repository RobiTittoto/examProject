import javax.swing.*;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String line = input.nextLine();
        System.out.println(process(line));
    }

    public enum valuesKind{
        GRID,
        LIST
    }

    static String process(String line) {
        long startTime = System.currentTimeMillis();

        String[] input = line.split(";");
        switch (input[0].split("_")[0]) {
            case "stat":
                //StatRequest
                System.out.println("stat");
                if (input[0].split("_")[1].equals("AVG")) {
                    System.out.println(process("AVG"));
                } else if (input[0].split("_")[1].equals("MAX")) {
                    System.out.println(process("MAX"));
                }
                break;
            case "MIN":                                             //ComputationRequest
                System.out.println("MIN");
                makeGird(input[1], valuesKind.GRID);
                break;
            case "MAX":
                System.out.println("MAX");
                break;
            case "AVG":
                System.out.println("AVG");
                break;
            case "COUNT":
                System.out.println("COUNT");
                break;

        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        return input[0] + " " + elapsedTime + " ms";
    }

    protected static void makeGird(String variableValuesFunction, valuesKind valuesKind){ //es. x0:-1:0.1:1,x1:-10:1:20
        List<Map<String, Double>> grid = new ArrayList<>();

        String[] variablesInfo = variableValuesFunction.split(",");
        for(String variableInfo : variablesInfo){
            String[] variableInfoParts = variableInfo.split(":");
            String variableName = variableInfoParts[0];
            double firstValue = Double.parseDouble(variableInfoParts[1]);
            double stepValue = Double.parseDouble(variableInfoParts[2]);
            double endValue = Double.parseDouble(variableInfoParts[3]);
            List<Double> values = new ArrayList<>();
            for (double j = firstValue; j < endValue; j+=stepValue) {
                values.add(j);
            }
            if(valuesKind== Main.valuesKind.GRID)
                grid = addInGrid(grid,variableName,values);
            System.out.println(grid);
            if (valuesKind == Main.valuesKind.LIST)
                grid =null;
        }
    }
    private static List<Map<String, Double>> addInGrid(List<Map<String, Double>> setA, String VariableName, List<Double> values){
        if (setA==null)
            setA = new ArrayList<>();
        //nel nome manca un riferimento al fatto che è tornata la nuova mappa, ma si potrebbe mettere la lista nello stato della classe
        List<Map<String, Double>> result = new ArrayList<>(); //scelgo ArrayList perchè più veloce nella navigazione rispetto al LinkedList
        for(Map<String,Double> value : setA){ // la lista potrebbe essere nulla
            for(int i=0; i<value.size(); i++){
                Map<String,Double> temp = new HashMap<>();
                temp.put(VariableName, values.get(i));
                result.add(temp);
            }
        }
        return result;
    }
    private List<Map<String, Double>> addInList(List<Map<String, Double>> setA, String VariableName, List<Double> values){
        if (setA==null)
            setA = new ArrayList<>();
        //nel nome manca un riferimento al fatto che è tornata la nuova mappa, ma si potrebbe mettere la lista nello stato della classe
        List<Map<String, Double>> result = new ArrayList<>(); //scelgo ArrayList perché più veloce nella navigazione rispetto al LinkedList
        for(Map<String,Double> value : setA){ // la lista potrebbe essere nulla
            for(int i=0; i<value.size(); i++){
                Map<String,Double> temp = new HashMap<>(value);
                temp.put(VariableName, values.get(i));
                result.add(temp);
            }
        }
        return result;
    }















    protected static void makeGridTouple(String variableValuesFunction) {
        String[] variableValues = variableValuesFunction.split(",");
        int variableNum = variableValues.length;

        List<List<Double>> vectors = new ArrayList<>();
        for (int i = 0; i < variableNum; i++) {
            double firstValue = Double.parseDouble(variableValues[i].split(":")[1]);
            double endValue = Double.parseDouble(variableValues[i].split(":")[3]);
            double stepValue = Double.parseDouble(variableValues[i].split(":")[2]);
            List<Double> values = new ArrayList<>();
            for (double j = firstValue; j < endValue; j+=stepValue) {
                System.out.println("Adding " + j);
                values.add(j);
            }
            vectors.add(values);
        }
        List<List<Double>> grid = cartesianProduct(vectors);
        System.out.println(grid);

    }

    public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> result = new ArrayList<>();
        if (lists == null || lists.isEmpty()) {
            return result;
        }

        cartesianProductRecursive(lists, result, 0, new ArrayList<>());
        return result;
    }

    private static <T> void cartesianProductRecursive(List<List<T>> lists, List<List<T>> result, int depth, List<T> current) {
        if (depth == lists.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (T element : lists.get(depth)) {
            current.add(element);
            cartesianProductRecursive(lists, result, depth + 1, current);
            current.remove(current.size() - 1);
        }
    }


}
