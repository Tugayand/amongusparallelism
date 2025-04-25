package solutions;

import data.Image;

import java.util.AbstractMap;

public interface AmongiFinder {

    /** Counts the number of amongi in a given image. */
    AbstractMap<Integer, Integer> countAmongiByColour(Image img);
}
