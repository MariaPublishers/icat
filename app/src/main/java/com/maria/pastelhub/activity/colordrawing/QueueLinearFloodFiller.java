
package com.maria.pastelhub.activity.colordrawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class QueueLinearFloodFiller {

    protected Bitmap image = null;
    protected int[] tolerance = new int[]{0, 0, 0};
    protected int width = 0;
    protected int height = 0;
    protected int[] pixels = null;
    protected int fillColor = 0;
    protected int[] startColor = new int[]{0, 0, 0};
    protected boolean[] pixelsChecked;
    protected Queue<FloodFillRange> ranges;

    // Construct using an image and a copy will be made to fill into,
    // Construct with BufferedImage and flood fill will write directly to
    // provided BufferedImage
    public QueueLinearFloodFiller(Bitmap img) {
        copyImage(img);
    }

    public QueueLinearFloodFiller(Bitmap img, int targetColor, int newColor) {
        System.gc();
        useImage(img);






        ;
        setTargetColor(targetColor);
    }

    public void setTargetColor(int targetColor) {
        startColor[0] = Color.red(targetColor);
        startColor[1] = Color.green(targetColor);
        startColor[2] = Color.blue(targetColor);
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int value) {
        fillColor = value;
    }

    public int[] getTolerance() {
        return tolerance;
    }

    public void setTolerance(int[] value) {
        tolerance = value;
    }

    public void setTolerance(int value) {
        tolerance = new int[]{value, value, value};
    }

    public Bitmap getImage() {
        return image;
    }

    public void copyImage(Bitmap img) {
        // Copy data from provided Image to a BufferedImage to write flood fill
        // to, use getImage to retrieve
        // cache data in member variables to decrease overhead of property calls
        width = img.getWidth();
        height = img.getHeight();

        image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        canvas.drawBitmap(img, 0, 0, null);

        pixels = new int[width * height];

        image.getPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
    }

    public void useImage(Bitmap img) {
        // Use a pre-existing provided BufferedImage and write directly to it
        // cache data in member variables to decrease overhead of property calls
        try {
            width = img.getWidth();
            height = img.getHeight();
            image = img;

            pixels = new int[width * height];

            image.getPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // Fills the specified point on the bitmap with the currently selected fill
    // color.
    // int x, int y: The starting coords for the fill
    public void floodFill(int x, int y) {

        pixelsChecked = new boolean[pixels.length];
        new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    // Setup
                    ranges = new LinkedList<FloodFillRange>();

                    if (startColor[0] == 0) {
                        // ***Get starting color.
                        int startPixel = pixels[(width * y) + x];
                        startColor[0] = (startPixel >> 16) & 0xff;
                        startColor[1] = (startPixel >> 8) & 0xff;
                        startColor[2] = startPixel & 0xff;
                    }
                    Log.d("Floodfill", "Linearfill");

                    // ***Do first call to floodfill.
                    LinearFill(x, y);

                    // ***Call floodfill routine while floodfill ranges still exist on the
                    // queue
                    FloodFillRange range;

                    while (ranges.size() > 0) {
                        // **Get Next Range Off the Queue
                        try {
                            range = ranges.remove();

                            // **Check Above and Below Each Pixel in the Floodfill Range
                            int downPxIdx = (width * (range.Y + 1)) + range.startX;
                            int upPxIdx = (width * (range.Y - 1)) + range.startX;
                            int upY = range.Y - 1;// so we can pass the y coord by ref
                            int downY = range.Y + 1;

                            for (int i = range.startX; i <= range.endX; i++) {
                                // *Start Fill Upwards
                                // if we're not above the top of the bitmap and the pixel above
                                // this one is within the color tolerance

                                if (range.Y > 0 && (!pixelsChecked[upPxIdx])
                                        && CheckPixel(upPxIdx))

                                    LinearFill(i, upY);


                                // *Start Fill Downwards
                                // if we're not below the bottom of the bitmap and the pixel
                                // below this one is within the color tolerance
                                if (range.Y < (height - 1) && (!pixelsChecked[downPxIdx])
                                        && CheckPixel(downPxIdx))
                                    LinearFill(i, downY);

                                downPxIdx++;
                                upPxIdx++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                    image.setPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Finds the furthermost left and right boundaries of the fill area
    // on a given y coordinate, starting from a given x coordinate, filling as
    // it goes.
    // Adds the resulting horizontal range to the queue of floodfill ranges,
    // to be processed in the main loop.

    // int x, int y: The starting coords
    protected void LinearFill(int x, int y) {
        // Log.d("TAG", "LinearFill: ");
        try {
            // ***Find Left Edge of Color Area
            int lFillLoc = x; // the location to check/fill on the left
            int pxIdx = (width * y) + x;

            while (true) {
                // **fill with the color
                pixels[pxIdx] = fillColor;

                // **indicate that this pixel has already been checked and filled
                pixelsChecked[pxIdx] = true;

                // **de-increment
                lFillLoc--; // de-increment counter
                pxIdx--; // de-increment pixel index

                // **exit loop if we're at edge of bitmap or color area
                if (lFillLoc < 0 || (pixelsChecked[pxIdx]) || !CheckPixel(pxIdx)) {
                    break;
                }
            }

            lFillLoc++;

            // ***Find Right Edge of Color Area
            int rFillLoc = x; // the location to check/fill on the left

            pxIdx = (width * y) + x;

            while (true) {
                // **fill with the color
                pixels[pxIdx] = fillColor;
                // **indicate that this pixel has already been checked and filled
                pixelsChecked[pxIdx] = true;

                // **increment
                rFillLoc++; // increment counter
                pxIdx++; // increment pixel index

                // **exit loop if we're at edge of bitmap or color area
                if (rFillLoc >= width || pixelsChecked[pxIdx] || !CheckPixel(pxIdx)) {
                    break;
                }
            }

            rFillLoc--;

            // add range to queue
            FloodFillRange r = new FloodFillRange(lFillLoc, rFillLoc, y);

            ranges.offer(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sees if a pixel is within the color tolerance range.
    protected boolean CheckPixel(int px) {
        int red = (pixels[px] >>> 16) & 0xff;
        int green = (pixels[px] >>> 8) & 0xff;
        int blue = pixels[px] & 0xff;

        double darkness = 1 - (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        return (darkness < 0.9);
//
//        return (red >= (startColor[0] - tolerance[0])
//                && red <= (startColor[0] + tolerance[0])
//                && green >= (startColor[1] - tolerance[1])
//                && green <= (startColor[1] + tolerance[1])
//                && blue >= (startColor[2] - tolerance[2]) && blue <= (startColor[2] + tolerance[2]));
    }

    // Represents a linear range to be filled and branched from.
    protected class FloodFillRange {
        public int startX;
        public int endX;
        public int Y;

        public FloodFillRange(int startX, int endX, int y) {
            this.startX = startX;
            this.endX = endX;
            this.Y = y;
        }
    }
}