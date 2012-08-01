import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class Main {

    public static BufferedImage toGray(BufferedImage rawImg) {

        for (int x = 0; x < rawImg.getWidth(); x++) {
            for (int y = 0; y < rawImg.getHeight(); y++) {
                final Color color = new Color(rawImg.getRGB(x, y));
                final int grayColorValue = (color.getBlue() + color.getRed() + color.getGreen()) / 3;

                rawImg.setRGB(x, y,
                        new Color(grayColorValue, grayColorValue, grayColorValue).getRGB());
            }
        }

        return rawImg;
    }

    public static int[] histogram(BufferedImage grayImg) {
        final int[] hist = new int[256];
        for (int x = 0; x < grayImg.getWidth(); x++) {
            for (int y = 0; y < grayImg.getHeight(); y++) {
                final Color color = new Color(grayImg.getRGB(x, y));
                hist[color.getBlue()]++;
            }
        }

        return hist;
    }

    public static BufferedImage removedColorImage(BufferedImage img, Set<Integer> reserverColor,
            Color replaceColor) {
        if (null == replaceColor) {
            replaceColor = Color.BLACK;
        }
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (!reserverColor.contains(img.getRGB(x, y))) {
                    img.setRGB(x, y, replaceColor.getRGB());
                }
                else {
                    // img.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return img;
    }

    static class GrayPix {
        int color;
        int volume;
    }

    static class GrayPixComparator implements Comparator<GrayPix> {

        @Override
        public int compare(GrayPix a, GrayPix b) {
            return a.volume - b.volume;
        }

    }

    public static void main(String[] args) throws Exception {
        final File inputFolder = new File(args[0]);

        for (File inputFile : inputFolder.listFiles()) {
            final String filename = inputFile.getName().toLowerCase();
            if(!filename.endsWith(".jpg") || filename.startsWith("out")) {
                continue;
            }
            final BufferedImage rawImg = ImageIO.read(inputFile);
            final BufferedImage grayRawImage = toGray(rawImg);
            final int[] hist = histogram(grayRawImage);
            final GrayPix[] pixHist = new GrayPix[hist.length];
            for (int i = 0; i < hist.length; i++) {
                pixHist[i] = new GrayPix();
                pixHist[i].color = i;
                pixHist[i].volume = hist[i];
            }

            Arrays.sort(pixHist, new GrayPixComparator());
            final Set<Integer> reserverColor = new HashSet<Integer>();
            for (int i = 0; i < pixHist.length; i++) {
                if (pixHist[i].volume < 200) {
                    reserverColor.add(new Color(pixHist[i].color, pixHist[i].color,
                            pixHist[i].color).getRGB());
                }
                System.out.println("color " + pixHist[i].color + " " + pixHist[i].volume);
            }

            // for(int i = hist.length - 2;i >= hist.length - 7;i--) {
            // System.out.println("color removed " + pixHist[i].color + " " +
            // pixHist[i].volume);

            // }

            ImageIO.write(removedColorImage(grayRawImage, reserverColor, Color.BLACK), "JPEG",
                    new File(inputFile.getParent(), "out" + inputFile.getName()));
        }

    }

}
