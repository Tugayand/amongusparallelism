package data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Simple wrapper around a BufferedImage. */
public class Image {
    private BufferedImage img;
    public int width;
    public int height;

    /*
        Image layout:

                 x
           0 ------- width
           |           |
        y  |           |
           |           |
           height -----+
     */

    public Image(String path) {
        try {
            img = ImageIO.read(new File(path));
            width = img.getWidth();
            height = img.getHeight();
        } catch (IOException e) {
            System.out.println("Could not read image " + path);
            System.exit(1);
        }
    }

    /** Returns the colour of a pixel as an integer. */
    public int getColor(int x, int y) {
        return img.getRGB(x, y);
    }

    /** Returns true if a given pixel has a specific color. */
    public boolean matches(int x, int y, int color) {
        return getColor(x, y) == color;
    }
}
