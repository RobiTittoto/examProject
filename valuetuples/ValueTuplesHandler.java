package valuetuples;

import expression.Node;
import expression.Operator;
import expression.Variable;
import expression.Constant;
import expression.Parser;

import java.lang.reflect.MalformedParametersException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;

public class ValueTuplesHandler extends HashSet<Map<String, Double>> {
    private Node parsedExpression;
    private final NavigableSet<Double> calculatedValues = new TreeSet<>();

    public enum valuesKind {
        GRID,
        LIST
    }

    public ValueTuplesHandler() {
    }

    public ValueTuplesHandler(String stringValuesKind, String variableValuesFunction) throws MalformedParametersException {
        valuesKind vk;
        if (stringValuesKind.equalsIgnoreCase("GRID")) {
            vk = valuesKind.GRID;
        } else if ((stringValuesKind.equalsIgnoreCase("LIST"))) {
            vk = valuesKind.LIST;
        } else {
            throw new MalformedParametersException("Values kind "+ stringValuesKind + "not supported");
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
                if (!Pattern.compile("[a-z][a-z0-9]*").matcher(variableName).find()) {
                    throw new MalformedParametersException(variableName + " is not a valid variable name");
                }


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
        try{
            calculateValues();
        }catch (RuntimeException e){
            throw new MalformedParametersException(e.getMessage()+ " in expression " + expression);
        }
    }

    private void calculateValues() {
        if (this.parsedExpression == null) {
            throw new RuntimeException("Expression is null");
        }
        if(this.isEmpty()){
            throw new RuntimeException("Empty expression");
        }
        this.stream()
                .map(valueTuple -> getValue(parsedExpression, valueTuple))
                .forEach(calculatedValues::add);
    }

    public NavigableSet<Double> getCalculatedValues() {
        return calculatedValues;
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

        if (operator instanceof Operator opt) {
            Double result = opt.getType().getFunction().apply(new double[]{valueFirstChild, valueSecondChild});
            if (Double.isInfinite(result) || Double.isNaN(result)) {
                throw new RuntimeException("Division by zero");
            }
            return result;
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof ValueTuplesHandler) {
            return ((ValueTuplesHandler) o).getCalculatedValues().equals(calculatedValues);
        }
        return  false;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + calculatedValues;
    }
}
