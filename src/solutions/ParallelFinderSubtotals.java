package solutions;

import data.Image;

import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;

public class ParallelFinderSubtotals implements AmongiFinder {
    final int p; // Number of threads
    final int T; // Sequential threshold for x-axis
    final int Ty; // Sequential threshold for y-axis
    final ForkJoinPool forkJoinPool;

    public ParallelFinderSubtotals(int p, int T, int Ty) {
        this.p = p;
        this.T = T;
        this.Ty = Ty;
        this.forkJoinPool = new ForkJoinPool(p);
    }

    public HashMap<Integer, Integer> countAmongiByColour(Image img) {
        return forkJoinPool.invoke(new ParallelFinderSubtotalsTaskFJ(img, 0, img.width, 0, img.height, T, Ty));
    }
}