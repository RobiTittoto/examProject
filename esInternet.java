import java.util.ArrayList;
import java.util.List;

public class esInternet {

    // Function to find cartesian product of two sets
    static List<List<String>> cartesianProduct(List<List<String>> setA, List<String> setB) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> a : setA) {
            for (String b : setB) {
                List<String> temp = new ArrayList<>(a);
                temp.add(b);
                result.add(temp);
            }
        }
        return result;
    }

    // Function to do cartesian product of N sets
    static void cartesian(List<List<String>> sets) {
        List<List<String>> temp = new ArrayList<>();
        for (String element : sets.getFirst()) {
            List<String> list = new ArrayList<>();
            list.add(element);
            temp.add(list);
        }
        for (int i = 1; i < sets.size(); i++) {
            temp = cartesianProduct(temp, sets.get(i));
        }
        for (List<String> product : temp) {
            for (String element : product) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        List<List<String>> sets = new ArrayList<>();
        sets.add(List.of("1", "2"));
        sets.add(List.of("A"));
        sets.add(List.of("x", "y", "z"));
        cartesian(sets);
    }
}
