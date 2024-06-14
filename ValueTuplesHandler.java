import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ValueTuplesHandler extends HashSet<Map<String, Double>> {

    private Node parsedExpression;
    NavigableSet<Double> calculatedValues = new TreeSet<>();

    public enum valuesKind {
        GRID,
        LIST
    }

    public ValueTuplesHandler() {
        super();
    }

    public ValueTuplesHandler(String stringValuesKind, String variableValuesFunction) {
        valuesKind vk;
        if (stringValuesKind.equalsIgnoreCase("GRID")) {
            vk = valuesKind.GRID;
        } else if ((stringValuesKind.equalsIgnoreCase("LIST"))) {
            vk = valuesKind.LIST;
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
            if (this.isEmpty()) {
                for (Double aDouble : values) {
                    Map<String, Double> temp = new HashMap<>();
                    temp.put(variableName, aDouble);
                    this.add(temp);
                }
            }
            if (vk == valuesKind.GRID) {
                ValueTuplesHandler grid = new ValueTuplesHandler();
                for (Map<String, java.lang.Double> value : this) {
                    for (java.lang.Double aDouble : values) {
                        Map<String, java.lang.Double> temp = new HashMap<>(value);
                        temp.put(variableName, aDouble);
                        grid.add(temp);
                    }
                }
                this.clear();
                this.addAll(grid);
            } else {
                ValueTuplesHandler list = new ValueTuplesHandler();
                int i = 0;
                for (Map<String, java.lang.Double> value : this) {
                    Map<String, java.lang.Double> temp = new HashMap<>(value);
                    temp.put(variableName, values.get(i));
                    //System.out.println("inserito nella lista "+ variableName +" con il valore "+values.get(i));
                    list.add(temp);
                    i++;
                }
                this.clear();
                this.addAll(list);
            }
        }
    }


    public void setExpressions(String[] expressions) {
        for (String expression : expressions) {
            setExpression(expression);
        }
    }

    public void setExpression(String expression) {
        this.parsedExpression = (new Parser(expression)).parse();
        calculateValues();
    }

    private void calculateValues() {
        if (calculatedValues.isEmpty()) {
            for (Map<String, Double> valueTuple : this) {
                calculatedValues.add(getValue(parsedExpression, valueTuple));
            }
        }
    }


    private double getValue(Node node, Map<String, Double> valueTuple) {
        return switch (node) {
            case Variable variable -> valueTuple.get(variable.getName());
            case Constant constant -> constant.getValue();
            case Operator operator -> executeExpression(operator, valueTuple);
            case null, default -> throw new IllegalArgumentException("Unknown Node type");
        };
    }

    private double executeExpression(Node operator, Map<String, Double> valueTuple) {

        double valueFirstChild = getValue(operator.getChildren().get(0), valueTuple);
        double valueSecondChild = getValue(operator.getChildren().get(1), valueTuple);
        // Cast to Operator only after confirming it is an instance of Operator
        if (operator instanceof Operator opt) {
            return opt.getType().getFunction().apply(new double[]{valueFirstChild, valueSecondChild});
        } else {
            throw new IllegalArgumentException("Node is not an instance of Operator");
        }
    }

    public double getMax() {
        return calculatedValues.getLast();
    }

    public double getMin() {
        return calculatedValues.getFirst();
    }

    public double getCount() {
        return calculatedValues.size();
    }

    public double getAVG() {
        double sum = 0;
        for (Double value : calculatedValues) {
            sum += value;
        }
        return sum / calculatedValues.size();
    }
}
