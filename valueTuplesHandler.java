import java.util.*;

public class valueTuplesHandler {
    private enum valuesKind {
        GRID,
        LIST;

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private List<Map<String, Double>> valueTuples;
    private Iterator<Map<String, Double>> iterator;

    public valueTuplesHandler() {
        valueTuples = new ArrayList<>();
    }

    public static valueTuplesHandler getTuplesHandler() {
        return new valueTuplesHandler();
    }

    public List<Map<String, Double>> getValueTuples() {
        return valueTuples;
    }

    public void setValueTuplesTuples(String variableValuesFunction, String stringValuesKind) {
        valuesKind vk;
        if (stringValuesKind.equals("GRID")) {
            vk = valueTuplesHandler.valuesKind.GRID;
        } else if ((stringValuesKind.equals("LIST"))) {
            vk = valueTuplesHandler.valuesKind.LIST;
        } else {
            System.out.println("Errore");
            return;
        }
        String[] variablesInfo = variableValuesFunction.split(",");
        for (String variableInfo : variablesInfo) {
            String[] variableInfoParts = variableInfo.split(":");
            String variableName = variableInfoParts[0];
            double firstValue = Double.parseDouble(variableInfoParts[1]);
            double stepValue = Double.parseDouble(variableInfoParts[2]);
            double endValue = Double.parseDouble(variableInfoParts[3]);
            List<Double> values = new ArrayList<>();
            for (double j = firstValue; j <= endValue; j += stepValue) {
                values.add(j);
                //System.out.println("Aggiunto valore " + j);
            }
            if ((valueTuples.isEmpty())) {
                for (Double value : values) {
                    Map<String, Double> temp = new HashMap<>();
                    temp.put(variableName, value);
                    valueTuples.add(temp);
                }
            }
            if (vk == valuesKind.GRID) {
                List<Map<String, Double>> grid = new ArrayList<>();
                for (Map<String, Double> value : valueTuples) {
                    for (Double aDouble : values) {
                        Map<String, Double> temp = new HashMap<>(value);
                        temp.put(variableName, aDouble);
                        //System.out.println("inserito nella lista "+ variableName + " con il valore "+values.get(i));
                        grid.add(temp);
                    }
                }
                valueTuples = grid;
            } else {
                List<Map<String, Double>> list = new ArrayList<>();
                int i = 0;
                for (Map<String, Double> value : valueTuples) {
                    Map<String, Double> temp = new HashMap<>(value);
                    temp.put(variableName, values.get(i));
                    //System.out.println("inserito nella lista "+ variableName + " con il valore "+values.get(i));
                    list.add(temp);
                    i++;
                }
                valueTuples = list;
            }
        }
        iterator = valueTuples.iterator();
    }

    public Map<String,Double> getNextValueTuple(){
        return iterator.next();
    }
    public  Iterator<Map<String,Double>> getIterator(){
        return valueTuples.iterator();
    }
}
