package solutions;

import data.Amongus;
import data.Image;

import java.util.HashMap;

public class SequentialFinder implements AmongiFinder {

    public HashMap<Integer, Integer> countAmongiByColour(Image img) {
        HashMap<Integer, Integer> colourCounts = new HashMap<>();
        for (int x = 0; x < img.width; x++) {
            for (int y = 0; y < img.height; y++) {
                if (Amongus.detect(x, y, img)) {
                    int colour = Amongus.bodyColor(x, y, img);
                    if (colourCounts.containsKey(colour)) {
                        int currentCount = colourCounts.get(colour);
                        colourCounts.put(colour, currentCount + 1);
                    } else {
                        colourCounts.put(colour, 1);
                    }
                }
            }
        }
        return colourCounts;
    }
}
