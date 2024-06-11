import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class provaCreazioneCartesian {

    static List<Map<String, Double>> addInGrid(List<Map<String, Double>> setA, String VariableName, List<Double> values){
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




















}
