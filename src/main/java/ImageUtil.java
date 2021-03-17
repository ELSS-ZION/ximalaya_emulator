import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {

    public static int binaryRectRGBSum(@NotNull BufferedImage bi, int x, int y, int w, int h) {
        int count = 0;
        int sum = 0;
        if (x + w > bi.getWidth() || y + h > bi.getHeight()) {
            return 0;
        }
        for (int xIndex = x; xIndex < x + w; xIndex++) {
            for (int yIndex = y; yIndex < y + h; yIndex++) {
                int color = 0;
                color = bi.getRGB(xIndex, yIndex);

                final int a = (color >> 24) & 0xff;
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
                if (a != 0) {
                    count++;
                }
                if (a != 0 && r == 255) {
                    sum++;
                }
            }
        }
        return sum;
    }

    @NotNull
    public static Map<Point, Integer> getComparedColorPointsFromFgBi(@NotNull BufferedImage fgBi){
        int minX = fgBi.getWidth();
        Map<Point, Integer> colorPoints = new HashMap<Point, Integer>();
        Map<Point, Integer> comparedColorPoints = new HashMap<Point, Integer>();

        for (int x = 0; x < fgBi.getWidth(); x++) {
            for (int y = 0; y < fgBi.getHeight(); y++) {
                int color = fgBi.getRGB(x, y);
                int alpha = color & 0xff000000;

                if (alpha != 0) {
                    colorPoints.put(new Point(x, y), color);
                    if (x < minX) {
                        minX = x;
                    }
                } else {
//                    System.out.format("(%d, %d) alpha == 0\n", x, y);
                }
            }
        }


//        return colorPoints;

        for (Map.Entry<Point, Integer> entry : colorPoints.entrySet()) {
            Point point = entry.getKey();
            point.x -= minX;
            int color = entry.getValue();
            comparedColorPoints.put(point, color);
        }
        return comparedColorPoints;

    }

    public static int binaryRectRGBSumMatch(BufferedImage bgBi, @NotNull Map<Point, Integer> fgOpaqueColorPoints) {
        int mostSimilarX = 0;
        int minDiff = 0x7fffffff;
        int maxXOffset = 0;
        final int blockSize = fgOpaqueColorPoints.size() / 4;
        int[] fgRectRGBSum = new int[4];
        int fgRectRGBSumTotal = 0;
        int i = 0;

        for (Map.Entry<Point, Integer> entry : fgOpaqueColorPoints.entrySet()) {
            Point point = entry.getKey();
            int fgColor = entry.getValue();

            final int r = (fgColor >> 16) & 0xff;
            final int g = (fgColor >> 8) & 0xff;
            final int b = fgColor & 0xff;

            if (r == 255) {
                fgRectRGBSumTotal++;
                if (i <= blockSize) {
                    fgRectRGBSum[0]++;
                } else if (i <= 2 * blockSize) {
                    fgRectRGBSum[1]++;
                } else if (i <= 3 * blockSize) {
                    fgRectRGBSum[2]++;
                } else if (i <= 4 * blockSize) {
                    fgRectRGBSum[3]++;
                }
            }
            i++;
        }


        for (Map.Entry<Point, Integer> entry : fgOpaqueColorPoints.entrySet()) {
            Point point = entry.getKey();
            if (point.x > maxXOffset) {
                maxXOffset = point.x;
            }
        }


        for (int xOffset = 0; maxXOffset + xOffset < bgBi.getWidth(); xOffset++) {
            i = 0;
            int[] bgRectRGBSum = new int[4];
            int bgRectRGBSumTotal = 0;

            for (Map.Entry<Point, Integer> entry : fgOpaqueColorPoints.entrySet()) {
                Point point = entry.getKey();
                int x = point.x + xOffset;
                int y = point.y;

                int bgColor = bgBi.getRGB(x, y);
                final int r = (bgColor >> 16) & 0xff;
                final int g = (bgColor >> 8) & 0xff;
                final int b = bgColor & 0xff;

                if (r == 255) {
                    bgRectRGBSumTotal++;
                    if (i <= blockSize) {
                        bgRectRGBSum[0]++;
                    } else if (i <= 2 * blockSize) {
                        bgRectRGBSum[1]++;
                    } else if (i <= 3 * blockSize) {
                        bgRectRGBSum[2]++;
                    } else if (i <= 4 * blockSize) {
                        bgRectRGBSum[3]++;
                    }
                }
                i++;
            }
            if (bgRectRGBSum[0] != 0
                    || bgRectRGBSum[1] != 0
                    || bgRectRGBSum[2] != 0
                    || bgRectRGBSum[3] != 0) {

                int totalDiff = Math.abs(fgRectRGBSumTotal - bgRectRGBSumTotal);
                int diff0 = Math.abs(fgRectRGBSum[0] - bgRectRGBSum[0]);
                int diff1 = Math.abs(fgRectRGBSum[1] - bgRectRGBSum[1]);
                int diff2 = Math.abs(fgRectRGBSum[2] - bgRectRGBSum[2]);
                int diff3 = Math.abs(fgRectRGBSum[3] - bgRectRGBSum[3]);
                int diff = totalDiff * 40 + diff0 + diff1 * 10 + diff2 * 20 + diff3 * 30;
//                System.out.format("%d,%d--%d,%d,%d,%d\n", xOffset, diff, totalDiff, diff0, diff1, diff2, diff3);
                if (diff < minDiff) {
                    minDiff = diff;
                    mostSimilarX = xOffset;
                }
            }
        }

        return mostSimilarX;
    }

    public static int RectRGBSumMatch(BufferedImage bgBi, @NotNull Map<Point, Integer> fgOpaqueColorPoints, int fgRectRGBSum) {
        int mostSimilarX = 0;
        int minDiff = 0x7fffffff;
        int maxXOffset = 0;

        for (Map.Entry<Point, Integer> entry : fgOpaqueColorPoints.entrySet()) {
            Point point = entry.getKey();
            if (point.x > maxXOffset) {
                maxXOffset = point.x;
            }
        }

        int diff = 0;
        for (int xOffset = 0; maxXOffset + xOffset < bgBi.getWidth(); xOffset++) {
            int bgRGBSum = 0;

            for (Map.Entry<Point, Integer> entry : fgOpaqueColorPoints.entrySet()) {
                Point point = entry.getKey();
                int fgColor = entry.getValue();
                int x = point.x + xOffset;
                int y = point.y;

                final int fgR = (fgColor >> 16) & 0xff;
                final int fgG = (fgColor >> 8) & 0xff;
                final int fgB = fgColor & 0xff;

                int bgColor = bgBi.getRGB(x, y);
                final int bgR = (bgColor >> 16) & 0xff;
                final int bgG = (bgColor >> 8) & 0xff;
                final int bgB = bgColor & 0xff;

                bgRGBSum += bgR + bgG + bgB;
            }
            diff = Math.abs(fgRectRGBSum - bgRGBSum);
            System.out.format("%d--%d\n", xOffset, diff);
            if (diff < minDiff) {
                minDiff = diff;
                mostSimilarX = xOffset;
            }
        }

        return mostSimilarX;
    }

    public static int getMostSimilarXInBg(BufferedImage bgBi, @NotNull Map<Point, Integer> fgOpaqueColorPoints) throws Exception {
        int mostSimilarX = 0;
        int minDiff = 0x7fffffff;
        int maxXOffset = 0;

        for (Map.Entry<Point, Integer> entry : fgOpaqueColorPoints.entrySet()) {
            Point point = entry.getKey();
            if (point.x > maxXOffset) {
                maxXOffset = point.x;
            }
        }

        for (int xOffset = 0; maxXOffset + xOffset < bgBi.getWidth(); xOffset++) {
            int diff = 0;

            for (Map.Entry<Point, Integer> entry : fgOpaqueColorPoints.entrySet()) {
                Point point = entry.getKey();
                int x = point.x + xOffset;
                int y = point.y;

                int fgColor = entry.getValue();
                final int fgR = (fgColor >> 16) & 0xff;
                final int fgG = (fgColor >> 8) & 0xff;
                final int fgB = fgColor & 0xff;

                int bgColor = 0;
                try {
                    bgColor = bgBi.getRGB(x, y);
                } catch (Exception e) {
                    System.out.println("error xOffset: " + x);
                }
                final int bgR = (bgColor >> 16) & 0xff;
                final int bgG = (bgColor >> 8) & 0xff;
                final int bgB = bgColor & 0xff;

                diff += Math.abs(fgR - bgR) + Math.abs(fgG - bgG) + Math.abs(fgB - bgB);
            }
            System.out.format("%d--%d\n", xOffset, diff);
            if (diff < minDiff) {
                minDiff = diff;
                mostSimilarX = xOffset;
            }
        }

        return mostSimilarX;
    }

    public static void printImageRGBA(@NotNull BufferedImage bufferedImage) throws Exception {
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                final int color = bufferedImage.getRGB(i, j);
                final int a = (color >> 24) & 0xff;
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
//                System.out.format("(%03d, %03d, %03d, %03d) ", r, g, b, a);
                if (a != 0) {
                    System.out.format("(%03d, %03d) ", i, j);
                }
            }
            System.out.println();
        }
    }

    @NotNull
    public static BufferedImage grayImage(BufferedImage bufferedImage) throws Exception {

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        BufferedImage grayBufferedImage = new BufferedImage(width, height, bufferedImage.getType());
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                final int color = bufferedImage.getRGB(i, j);
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
                int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
                int newPixel = colorToRGB(255, gray, gray, gray);
                grayBufferedImage.setRGB(i, j, newPixel);
            }
        }

        return grayBufferedImage;
    }

    private static int colorToRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;

    }

    @NotNull
    public static BufferedImage binaryImage(BufferedImage image) throws Exception {
        int w = image.getWidth();
        int h = image.getHeight();
        float[] rgb = new float[3];
        int black = new Color(0, 0, 0).getRGB();
        int white = new Color(255, 255, 255).getRGB();
        BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = image.getRGB(x, y);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                float avg = (rgb[0] + rgb[1] + rgb[2]) / 3;

                double threshold = 192;

                if ((pixel & 0xff000000) == 0) {
                    bi.setRGB(x, y, pixel);
                    continue;
                }

                if (avg < threshold) {
                    bi.setRGB(x, y, black);
                } else {
                    bi.setRGB(x, y, white);
                }
            }
        }

        return bi;
    }

    public static double getGray(double[][] coord, int x, int y, int w, int h) {
        double rs = coord[x][y] + (x == 0 ? 255 : coord[x - 1][y]) + (x == 0 || y == 0 ? 255 : coord[x - 1][y - 1])
                + (x == 0 || y == h - 1 ? 255 : coord[x - 1][y + 1]) + (y == 0 ? 255 : coord[x][y - 1])
                + (y == h - 1 ? 255 : coord[x][y + 1]) + (x == w - 1 ? 255 : coord[x + 1][y])
                + (x == w - 1 || y == 0 ? 255 : coord[x + 1][y - 1])
                + (x == w - 1 || y == h - 1 ? 255 : coord[x + 1][y + 1]);
        return rs / 9;
    }
}
