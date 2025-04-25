package solutions;

import data.Amongus;
import data.Image;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

public class ParallelFinderGlobalHashMapTaskFJ extends RecursiveAction {
    private final int x1; private final int x2; private final Image img; private final int sequential_threshold; private final ConcurrentHashMap<Integer, Integer> globalMap;// arguments

    ParallelFinderGlobalHashMapTaskFJ(Image construct_img, int construct_x1, int construct_x2, int construct_sequential_threshold, ConcurrentHashMap<Integer, Integer> construct_globalMap) {
        this.x1 = construct_x1;
        this.x2 = construct_x2;
        this.img = construct_img;
        this.sequential_threshold = construct_sequential_threshold;
        this.globalMap = construct_globalMap;

    }

    @Override
    protected void compute() { // returns no answer
        if ((x2 - x1) <= sequential_threshold) {
            for (int x = x1; x < x2; x++) {
                for (int y = 0; y < img.height; y++) {
                    if (Amongus.detect(x, y, img)) {
                        int colour = Amongus.bodyColor(x, y, img);
                        synchronized (globalMap) { // Make sure that this is "atomic"
                            if (globalMap.containsKey(colour)) {
                                globalMap.put(colour, globalMap.get(colour) + 1);
                            } else {
                                globalMap.put(colour, 1);
                            }
                        }
                    }
                }
            }
            // no return this time
        } else {
            int pivot = (x1 + x2) / 2;
            ParallelFinderGlobalHashMapTaskFJ left = new ParallelFinderGlobalHashMapTaskFJ(img, x1, pivot, sequential_threshold, globalMap);
            ParallelFinderGlobalHashMapTaskFJ right = new ParallelFinderGlobalHashMapTaskFJ(img, pivot, x2, sequential_threshold, globalMap);
            invokeAll(left, right);
        }
    }
}