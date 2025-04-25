import data.Image;
import org.junit.Test;
import static junit.framework.TestCase.*;

import solutions.ParallelFinderGlobalHashMap;
import solutions.ParallelFinderSubtotals;
import solutions.SequentialFinder;

import java.util.AbstractMap;

/** These tests can be used to test your parallel implementation. Feel free to add more tests. */
public class Tests {

    String[] files = new String[]{
            "images/place_2k_2k.png",
            "images/place_20k_2k.png",
            "images/place_20k_20k.png",
            "images/place_20k_20k_discoloured.png",
            "images/place_23k_23k.png"
    };

    @Test
    public void testSubtotals() {
        SequentialFinder seq = new SequentialFinder();
        ParallelFinderSubtotals p1 = new ParallelFinderSubtotals(1, Integer.MAX_VALUE, Integer.MAX_VALUE); // same as sequential
        ParallelFinderSubtotals p4 = new ParallelFinderSubtotals(4, 1, 1);
        ParallelFinderSubtotals pT = new ParallelFinderSubtotals(4, 43, 43);

        for (String file : files) {
            Image img = new Image(file);

            AbstractMap<Integer, Integer> c_seq = seq.countAmongiByColour(img);
            AbstractMap<Integer, Integer> c_p1 = p1.countAmongiByColour(img);
            AbstractMap<Integer, Integer> c_p4 = p4.countAmongiByColour(img);
            AbstractMap<Integer, Integer> c_pT = pT.countAmongiByColour(img);

            assertEquals(c_seq, c_p1);
            assertEquals(c_seq, c_p4);
            assertEquals(c_seq, c_pT);
        }
    }

    @Test
    public void testGlobalHashMap() {
        SequentialFinder seq = new SequentialFinder();
        ParallelFinderGlobalHashMap p1 = new ParallelFinderGlobalHashMap(1, Integer.MAX_VALUE); // same as sequential
        ParallelFinderGlobalHashMap p4 = new ParallelFinderGlobalHashMap(4, 43); // same as sequential

        for (String file : files) {
            Image img = new Image(file);

            AbstractMap<Integer, Integer> c_seq = seq.countAmongiByColour(img);
            AbstractMap<Integer, Integer> c_p1 = p1.countAmongiByColour(img);
            AbstractMap<Integer, Integer> c_p4 = p4.countAmongiByColour(img);

            assertEquals(c_seq, c_p1);
            assertEquals(c_seq, c_p4);
        }
    }
}
