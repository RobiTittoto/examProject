import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ValueTuplesHandler {
    private enum valuesKind {
        GRID,
        LIST


    }

    private Set<Map<String, Double>> valueTuples;
    private Iterator<Map<String, Double>> iterator;

    public ValueTuplesHandler() {
        valueTuples = new HashSet<>();
    }

    public static ValueTuplesHandler getTuplesHandler() {
        return new ValueTuplesHandler();
    }

    public Set<Map<String, Double>> getValueTuples() {
        return valueTuples;
    }

    public void setValueTuples(String variableValuesFunction, String stringValuesKind) {
        valuesKind vk;
        if (stringValuesKind.equalsIgnoreCase("GRID")) {
            vk = ValueTuplesHandler.valuesKind.GRID;
        } else if ((stringValuesKind.equalsIgnoreCase("LIST"))) {
            vk = ValueTuplesHandler.valuesKind.LIST;
        } else {
            System.out.println("Errore");
            return;
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
        System.out.println(valueTuples);
    }

    public Map<String,Double> getNextValueTuple(){
        return iterator.next();
    }
    public  Iterator<Map<String,Double>> getIterator(){
        return valueTuples.iterator();
    }
}
