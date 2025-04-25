package solutions;

import data.Image;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ParallelFinderGlobalHashMap implements AmongiFinder {
    final int p; // The number of cores used by the Fork/Join Framework.

    final int T; // The sequential threshold for the x-axis.

    final ForkJoinPool forkJoinPool;


    public ParallelFinderGlobalHashMap(int p, int T) {
        this.p = p;
        this.T = T;
        // Hint: initialise the Fork/Join framework here as well.
        this.forkJoinPool = new ForkJoinPool(p);
    }

    public ConcurrentHashMap<Integer, Integer> countAmongiByColour(Image img) {

        ConcurrentHashMap<Integer, Integer> globalMap = new ConcurrentHashMap<>();
        ParallelFinderGlobalHashMapTaskFJ task = new ParallelFinderGlobalHashMapTaskFJ(img, 0, img.width, 100, globalMap);
        forkJoinPool.invoke(task);
        return globalMap;
    }
}
