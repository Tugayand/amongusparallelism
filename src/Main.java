import data.Image;
import solutions.ParallelFinderSubtotals;
import solutions.SequentialFinder;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        //Image img = new Image("images/place_23k_23k.png");
        //Image img = new Image("images/place_20k_20k.png");
        Image img = new Image("images/place_2k_2k.png");
        //HashMap<Integer, Integer> counts = new SequentialFinder().countAmongiByColour(img);
        HashMap<Integer, Integer> counts = new ParallelFinderSubtotals(10, 500, 500).countAmongiByColour(img);
        System.out.println(counts);
    }
}