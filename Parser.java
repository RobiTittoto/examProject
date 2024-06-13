import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    //BNF
    //  <e> ::= <n> | <v> | (<e> <o> <e>)

    private final String string;
    private int cursor = 0;

    public Parser(String string) {
        this.string = string.replace(" ", "");
    }

    public enum TokenType {
        CONSTANT("[0-9]+(\\.[0-9]+)?"),
        VARIABLE("[a-z][a-z0-9]*"),
        OPERATOR("[+\\-\\*/\\^]"),
        OPEN_BRACKET("\\("),
        CLOSED_BRACKET("\\)");
        private final String regex;

        TokenType(String regex) {
            this.regex = regex;
        }

        public Token next(String s, int i) {
            Matcher matcher = Pattern.compile(regex).matcher(s);
            if (!matcher.find(i)) {
                return null;
            }
            return new Token(matcher.start(), matcher.end());
        }

        public String getRegex() {
            return regex;
        }
    }

    private static class Token {
        private final int start;
        private final int end;

        public Token(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public Node parse() throws IllegalArgumentException {
        Token token;
        token = TokenType.CONSTANT.next(string, cursor);
        if (token != null && token.start == cursor) {
            cursor = token.end;
            return new Constant(Double.parseDouble(string.substring(token.start, token.end)));
        }
        token = TokenType.VARIABLE.next(string, cursor);
        if (token != null && token.start == cursor) {
            cursor = token.end;
            return new Variable(string.substring(token.start, token.end));
        }
        token = TokenType.OPEN_BRACKET.next(string, cursor);
        if (token != null && token.start == cursor) {
            cursor = token.end;
            Node child1 = parse();
            Token operatorToken = TokenType.OPERATOR.next(string, cursor);
            if (operatorToken != null && operatorToken.start == cursor) {
                cursor = operatorToken.end;
            } else {
                throw new IllegalArgumentException(String.format(
                        "Unexpected char at %d instead of operator: '%s'",
                        cursor,
                        string.charAt(cursor)
                ));
            }
            Node child2 = parse();
            Token closedBracketToken = TokenType.CLOSED_BRACKET.next(string, cursor);
            if (closedBracketToken != null && closedBracketToken.start == cursor) {
                cursor = closedBracketToken.end;
            } else {                                                                           //deve esserci una parentesi chiusa
                throw new IllegalArgumentException(String.format(
                        "Unexpected char at %d instead of closed bracket: '%s'",
                        cursor,
                        string.charAt(cursor)
                ));
            }
            Operator.Type operatorType = null;
            String operatorString = string.substring(operatorToken.start, operatorToken.end);
            for (Operator.Type type : Operator.Type.values()) {
                if (operatorString.equals(Character.toString(type.getSymbol()))) {
                    operatorType = type;
                    break;
                }
            }
            if (operatorType == null) {
                throw new IllegalArgumentException(String.format(
                        "Unknown operator at %d: '%s'",
                        operatorToken.start,
                        operatorString
                ));
            }
            return new Operator(operatorType, Arrays.asList(child1, child2));
        }
        throw new IllegalArgumentException(String.format(
                "Unexpected char at %d: '%s'",
                cursor,
                string.charAt(cursor)
        ));
    }

    public static void main(String[] args) {
        Parser parser = new Parser("(x0*x1)");
        ValueTuplesHandler vtp = ValueTuplesHandler.getTuplesHandler();
        vtp.setValueTuples("x0:-1:0.1:-0.8,x1:1:1:3","GRID");
        Set<Map<String,Double>> prova = vtp.getValueTuples();
        double max=-100;
        for(Map<String,Double> aa : prova){
            double val = parser.executeExpression(parser.parse(),aa);
            if(val>max){
                max=val;
            }
        }
        System.out.println(max);
    }

    public double executeExpression(Node operator, Map<String, Double> valueTuple) {
        cursor=0;
        double valueFirstChild;
        if (operator.getChildren().getFirst() instanceof Variable) {
            valueFirstChild = valueTuple.get(((Variable) operator.getChildren().getFirst()).getName());
        } else if (operator.getChildren().getFirst() instanceof Constant) {
            valueFirstChild = ((Constant) operator.getChildren().getFirst()).getValue();
        } else {
            valueFirstChild = executeExpression(operator.getChildren().getFirst(), valueTuple);
        }
        double valueSecondChild;
        if (operator.getChildren().getFirst() instanceof Variable) {
            valueSecondChild = valueTuple.get(((Variable) operator.getChildren().get(1)).getName());
        } else if (operator.getChildren().getFirst() instanceof Constant) {
            valueSecondChild = ((Constant) operator.getChildren().get(1)).getValue();
        } else {

            valueSecondChild = executeExpression(operator.getChildren().get(1), valueTuple);
        }
        //System.out.println(valueFirstChild +" "+ operator.getType().getSymbol()+" "+ valueSecondChild);
        Operator opt = (Operator) operator;
        return opt.getType().getFunction().apply(new double[]{valueFirstChild, valueSecondChild});
    }

    public static double performOperation(Operator.Type type, double[] operands) {  //quando ho due numeri
        return type.getFunction().apply(operands);
    }

}
