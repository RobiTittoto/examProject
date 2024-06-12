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

    protected static void makeTouples(String variableValuesFunction, valuesKind valuesKind){ //es. x0:-1:0.1:1,x1:-10:1:20
        List<Map<String, Double>> touples = new ArrayList<>();

        String[] variablesInfo = variableValuesFunction.split(",");
        for(String variableInfo : variablesInfo){
            String[] variableInfoParts = variableInfo.split(":");
            String variableName = variableInfoParts[0];
            double firstValue = Double.parseDouble(variableInfoParts[1]);
            double stepValue = Double.parseDouble(variableInfoParts[2]);
            double endValue = Double.parseDouble(variableInfoParts[3]);
            List<Double> values = new ArrayList<>();
            for (double j = firstValue; j <= endValue; j+=stepValue) {
                values.add(j);
                //System.out.println("Aggiunto valore " + j);
            }
            if((touples.isEmpty())){
                for(Double value : values){
                    Map<String,Double> temp = new HashMap<>();
                    temp.put(variableName,value);
                    touples.add(temp);
                }
            }
            if(valuesKind== Main.valuesKind.GRID){
                    List<Map<String, Double>> grid = new ArrayList<>();
                    for(Map<String,Double> value : touples){
                        for (Double aDouble : values) {
                            Map<String, Double> temp = new HashMap<>(value);
                            temp.put(variableName, aDouble);
                            //System.out.println("inserito nella lista "+ variableName + " con il valore "+values.get(i));
                            grid.add(temp);
                        }
                    }
                    touples=grid;
                } else if (valuesKind == Main.valuesKind.LIST) {
                    List<Map<String, Double>> list = new ArrayList<>();
                    int i=0;
                    for(Map<String,Double> value : touples){
                        Map<String,Double> temp = new HashMap<>(value);
                        temp.put(variableName, values.get(i));
                        //System.out.println("inserito nella lista "+ variableName + " con il valore "+values.get(i));
                        list.add(temp);
                        i++;
                    }
                    touples=list;
                }
            }
        }

}
