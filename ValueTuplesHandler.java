import java.lang.reflect.MalformedParametersException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ValueTuplesHandler extends HashSet<Map<String, Double>> {

    private Node parsedExpression;
    private final NavigableSet<Double> calculatedValues = new TreeSet<>();

    public enum valuesKind {
        GRID,
        LIST
    }

    public ValueTuplesHandler() {
        super();
    }

    public ValueTuplesHandler(String stringValuesKind, String variableValuesFunction) throws MalformedParametersException {
        valuesKind vk;
        if (stringValuesKind.equalsIgnoreCase("GRID")) {
            vk = valuesKind.GRID;
        } else if ((stringValuesKind.equalsIgnoreCase("LIST"))) {
            vk = valuesKind.LIST;
        } else {
            throw new MalformedParametersException(stringValuesKind);
        }

        String[] variablesInfo;
        try {
            variablesInfo = variableValuesFunction.split(",");
        } catch (NullPointerException e) {
            throw new MalformedParametersException("Wrong declaration of values");
        }
        for (String variableInfo : variablesInfo) {
            String[] variableInfoParts;
            String variableName;
            BigDecimal firstValue;
            BigDecimal stepValue;
            BigDecimal endValue;
            List<Double> values = new ArrayList<>();
            try {
                variableInfoParts = variableInfo.split(":");
                variableName = variableInfoParts[0];
                firstValue = new BigDecimal(variableInfoParts[1]);
                stepValue = new BigDecimal(variableInfoParts[2]);
                endValue = new BigDecimal(variableInfoParts[3]);
                if (firstValue.compareTo(endValue) > 0) {
                    throw new MalformedParametersException("First value greater then end value");
                }
                if (stepValue.compareTo(BigDecimal.ZERO) == 0) {
                    throw new MalformedParametersException("Invalid step value");
                }

            } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
                throw new MalformedParametersException("Wrong declaration of values");
            }
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
                for (Map<String, Double> value : this) {
                    for (Double aDouble : values) {
                        Map<String, Double> temp = new HashMap<>(value);
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

    public void setExpression(String expression) throws IllegalArgumentException {
       this.parsedExpression = (new Parser(expression)).parse();
        calculateValues();
    }

    private void calculateValues() {
        for (Map<String, Double> valueTuple : this) {
            calculatedValues.add(getValue(parsedExpression, valueTuple));
        }
    }


    private double getValue(Node node, Map<String, Double> valueTuple) throws IllegalArgumentException {
        return switch (node) {
            case Variable variable -> {
                Double value = valueTuple.get(variable.getName());
                if (value == null) {
                    throw new IllegalArgumentException("Unvalued variable " + variable.getName());
                }
                yield value;
            }
            case Constant constant -> constant.getValue();
            case Operator operator -> executeExpression(operator, valueTuple);
            case null, default ->
                    throw new IllegalArgumentException("Unknown Node type");  //questo errore non può essere generato dall'utente, come lo gestisco?
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
        if (parsedExpression == null) {
            throw new RuntimeException("Expression not set");
        }

        return calculatedValues.getLast();
    }

    public double getMin() {
        if (parsedExpression == null) {
            throw new RuntimeException("Expression not set");
        }
        return calculatedValues.getFirst();
    }

    public double getCount() {
        if (parsedExpression == null) {
            throw new RuntimeException("Expression not set");
        }
        return calculatedValues.size();
    }

    public double getAVG() {
        if (parsedExpression == null) {
            throw new RuntimeException("Expression not set");
        }
        double sum = 0;
        for (Double value : calculatedValues) {
            sum += value;
        }
        return sum / calculatedValues.size();
    }
}
