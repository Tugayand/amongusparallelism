package data;

public class Amongus {
/*
     There are several patterns (P = point (x,y) if not top-left color, A = color 1, B = color 2):

     Normal right | Small right | Normal left | Small left
       PAAA           PAAA        AAA           AAA
       AABB           AABB        BBAA          BBAA
       AAAA           AAAA        AAAA          AAAA
        AAA            A A        AAA           A A
        A A                       A A
*/
    /** Detects whether the point (x, y) is the top-left pixel of an amongus in the image img.
     * Simple manual implementation. Requires pixels next to the amongus to be of a different colour. */
    public static boolean detect(int x, int y, Image img) {
        // First, check bounds. If these are not true, no amongi can be found anyway.
        if (x > img.width - 4 || y > img.height - 4) return false;
        // Then, check the 3rd row (identical in all patterns).
        int a = bodyColor(x, y, img);
        if (!detectRow(x, y + 2, img, a, 4)) return false;
        // Check that the colours above the first row differ.
        if (y > 0)
            for (int z = 0; z < 4; z++) {
                if (img.matches(x + z, y - 1, a))
                    return false;
            }
        return detectRight(x, y, img, a) || detectLeft(x, y, img, a);
    }

    /** Returns the body colour of an amongus detected at position (x, y). */
    public static int bodyColor(int x, int y, Image img) {
        return img.getColor(x, y + 2);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean detectRight(int x, int y, Image img, int a) {
        int b = img.getColor(x + 2, y + 1);
        return detectRow(x + 1, y, img, a, 3) &&
                detectRow(x, y + 1, img, a, 2) &&
                detectRow(x + 2, y + 1, img, b, 2) &&
                (detectRow(x + 1, y + 3, img, a, 3) &&
                        detectLegs(x + 1, y + 4, img, a) || detectLegs(x + 1, y + 3, img, a));
    }

    private static boolean detectLeft(int x, int y, Image img, int a) {
        int b = img.getColor(x, y + 1);
        return detectRow(x, y, img, a, 3) &&
                detectRow(x, y + 1, img, b, 2) &&
                detectRow(x + 2, y + 1, img, a, 2) &&
                (detectRow(x, y + 3, img, a, 3) &&
                        detectLegs(x, y + 4, img, a) || detectLegs(x, y + 3, img, a));
    }

    // Detects whether there are n pixels of colour a starting from (x,y) in image img and surrounded by a different colour.
    private static boolean detectRow(int x, int y, Image img, int a, int n) {
        for (int z = 0; z < n; z++) {
            if (!img.matches(x + z, y, a)) return false;
        }
        return (x == 0 || !img.matches(x - 1, y, a)) && (x + n >= img.width || !img.matches(x + n, y, a));
    }

    // Detects whether legs are present in a given row and column.
    private static boolean detectLegs(int x, int y, Image img, int a) {
        // Colour of the legs. Check the y coordinate for normal amongi.
        if (y == img.height || !img.matches(x, y, a) || !img.matches(x + 2, y, a)) return false;
        // Different colour next to, under, and in between the legs.
        return (x == 0 || !img.matches(x - 1, y, a)) &&
                !img.matches(x + 1, y, a) &&
                (x + 3 >= img.width || !img.matches(x + 3, y, a)) &&
                (y + 1 >= img.height || !img.matches(x, y + 1, a)) &&
                (y + 1 >= img.height || !img.matches(x + 2, y + 1, a));
    }
}