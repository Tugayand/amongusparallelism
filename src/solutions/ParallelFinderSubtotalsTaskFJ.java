package solutions;

import data.Image;

import java.util.HashMap;
import java.util.concurrent.RecursiveTask;
import data.Amongus;
import java.util.Map;

class ParallelFinderSubtotalsTaskFJ extends RecursiveTask<HashMap<Integer, Integer>> {
    private final int x1, x2, y1, y2; // Define ranges for both x and y axes
    private final Image img;
    private final int sequential_threshold_x, sequential_threshold_y;

    ParallelFinderSubtotalsTaskFJ(Image img, int x1, int x2, int y1, int y2, int sequential_threshold_x, int sequential_threshold_y) {
        this.img = img;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.sequential_threshold_x = sequential_threshold_x;
        this.sequential_threshold_y = sequential_threshold_y;
    }

    @Override
    protected HashMap<Integer, Integer> compute() {
        if ((x2 - x1) <= sequential_threshold_x && (y2 - y1) <= sequential_threshold_y) {
            // Process the range sequentially
            HashMap<Integer, Integer> colourCounts = new HashMap<>();
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    if (Amongus.detect(x, y, img)) {
                        int colour = Amongus.bodyColor(x, y, img);
                        colourCounts.put(colour, colourCounts.getOrDefault(colour, 0) + 1);
                    }
                }
            }
            return colourCounts;
        } else if ((x2 - x1) > sequential_threshold_x) {
            // Split along the x-axis
            int pivotX = (x1 + x2) / 2;
            ParallelFinderSubtotalsTaskFJ left = new ParallelFinderSubtotalsTaskFJ(img, x1, pivotX, y1, y2, sequential_threshold_x, sequential_threshold_y);
            ParallelFinderSubtotalsTaskFJ right = new ParallelFinderSubtotalsTaskFJ(img, pivotX, x2, y1, y2, sequential_threshold_x, sequential_threshold_y);
            left.fork();
            HashMap<Integer, Integer> rightResult = right.compute();
            HashMap<Integer, Integer> leftResult = left.join();
            return mergeResults(leftResult, rightResult);
        } else {
            // Split along the y-axis
            int pivotY = (y1 + y2) / 2;
            ParallelFinderSubtotalsTaskFJ top = new ParallelFinderSubtotalsTaskFJ(img, x1, x2, y1, pivotY, sequential_threshold_x, sequential_threshold_y);
            ParallelFinderSubtotalsTaskFJ bottom = new ParallelFinderSubtotalsTaskFJ(img, x1, x2, pivotY, y2, sequential_threshold_x, sequential_threshold_y);
            top.fork();
            HashMap<Integer, Integer> bottomResult = bottom.compute();
            HashMap<Integer, Integer> topResult = top.join();
            return mergeResults(topResult, bottomResult);
        }
    }

    private HashMap<Integer, Integer> mergeResults(HashMap<Integer, Integer> map1, HashMap<Integer, Integer> map2) {
        for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
            map1.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        return map1;
    }
}