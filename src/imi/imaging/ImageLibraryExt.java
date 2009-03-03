/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.imaging;
////////////////////////////////////////////////////////////////////////////////
// Imports
////////////////////////////////////////////////////////////////////////////////
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Will Braken & Paul Viet Truong
 */
public class ImageLibraryExt {

////////////////////////////////////////////////////////////////////////////////
// Image File I/O
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Loads an image from disk as BufferedImage
     * @param filename File to load
     * @return BufferedImage the image
     */
    public static BufferedImage load(String filename) {
        BufferedImage bufferedImage = null;
        try {
            ImageIcon icon = new ImageIcon(filename);
            Image image = icon.getImage();
            bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bufferedImage.getGraphics();
            g.drawImage(image, 0, 0, null);
            System.out.println("Successfully loaded [" + filename + "]");
        } catch (Exception e) {
            System.err.println("Error loading [" + filename + "] from disk");
            e.printStackTrace();
        }

        return bufferedImage;

        // -------------------------------------------------------------------
        // commented out because i'm not sure this gives me the format i want (ARGB
        // -------------------------------------------------------------------

        // BufferedImage bufferedImage = null;
        // try {
        // File infile = new File(filename);
        // bufferedImage = ImageIO.read(infile);
        // System.out.println("Successfully loaded [" + filename + "]");
        // } catch (IOException ioe) {
        // System.err.println("Error loading [" + filename + "] from disk");
        // ioe.printStackTrace();
        // }
        // return bufferedImage;
    }

    /**
     * Saves a BufferdImage to file
     * @param image The image to save
     * @param filename Filename for image
     * @param type Image Format
     */
    public static void save(BufferedImage image, String filename, String type) {
        try {
            ImageIO.write(image, type, new File(filename));
            System.out.println("Successfully saved [" + filename + "]");
        } catch (IOException ioe) {
            System.err.println("Error writing [" + filename + "] to disk");
            ioe.printStackTrace();
        }
    }

    /**
     * Saves a BufferdImage to file
     * @param image The image to save
     * @param filename Filename for image
     * @param type Image Format
     */
    public static void save(BufferedImage image, File file, String type) {
        try {
            ImageIO.write(image, type, file);
            System.out.println("Successfully saved [" + file.getName() + "]");
        } catch (IOException ioe) {
            System.err.println("Error writing [" + file.getName() + "] to disk");
            ioe.printStackTrace();
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Image Processing Methods
////////////////////////////////////////////////////////////////////////////////

    /**
     * Translates an image
     * @param image BufferedImage to scale
     * @param x offset
     * @param y offset
     * @return BufferedImage translated image
     */
    public static BufferedImage translate(BufferedImage image, int x, int y) {
        AffineTransform tx = new AffineTransform();
        tx.translate(x, y);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    /**
     * Rotates an image
     * @param image BufferedImage to scale
     * @param scaleX - the factor by which coordinates are scaled along the X axis direction
     * @param scaleY - the factor by which coordinates are scaled along the Y axis direction
     * @return BufferedImage rotated image
     */
    public static BufferedImage scale(BufferedImage image, double scaleX, double scaleY) {
        // can't scale to zero
        if (scaleX == 0 && scaleY == 0) {
            scaleX = 0.01;
            scaleY = 0.01;
        }
        AffineTransform tx = new AffineTransform();
        tx.scale(scaleX, scaleY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    /**
     * Rotates an image
     * @param image BufferedImage to scale
     * @param radians angle of rotation
     * @return BufferedImage rotated image
     */
    public static BufferedImage rotate(BufferedImage image, double degrees) {
        AffineTransform tx = new AffineTransform();
        tx.rotate(Math.toRadians(degrees), image.getWidth() / 2, image.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    /**
     * Rotates an image
     * @param image BufferedImage to scale
     * @param radians angle of rotation
     * @param x point of rotation
     * @param y point of rotation
     * @return BufferedImage rotated image
     */
    public static BufferedImage rotate(BufferedImage image, double radians, int x, int y) {
        AffineTransform tx = new AffineTransform();
        tx.rotate(radians, x, y);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    /**
     * Blend the contents of two BufferedImages according to a specified weight.
     * @param bi1 first BufferedImage
     * @param bi2 second BufferedImage
     * @param weight the fractional percentage of the first image to keep
     * @return BufferedImage containing blended results
     */
    public static BufferedImage blend(BufferedImage bi1, BufferedImage bi2, double weight) {
        int width = bi1.getWidth();
        if (width != bi2.getWidth()) {
            throw new IllegalArgumentException("widths not equal");
        }

        int height = bi1.getHeight();
        if (height != bi2.getHeight()) {
            throw new IllegalArgumentException("heights not equal");
        }

        BufferedImage bi3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi3.createGraphics();
        g2d.drawImage(bi1, null, 0, 0);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1.0 - weight)));
        g2d.drawImage(bi2, null, 0, 0);
        g2d.dispose();

        return bi3;
    }

        /**
     * Blend the contents of two BufferedImages according to a specified weight.
     * @param bi1 first BufferedImage
     * @param bi2 second BufferedImage
     * @param weight the fractional percentage of the first image to keep
     * @return BufferedImage containing blended results
     */
    public static BufferedImage blend(ImageData image1, ImageData image2, double weight, int width, int height) {

        BufferedImage bi3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi3.createGraphics();

        g2d.drawImage(image1.m_curImage, null, image1.m_xPos, image1.m_yPos);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (weight)));
        g2d.drawImage(image2.m_curImage, null, image2.m_xPos, image2.m_yPos);
        g2d.dispose();

        return bi3;
    }

    /**
     * Multiply the contents of two BufferedImages
     * @param image1 first BufferedImage
     * @param image2 second BufferedImage
     * @return BufferedImage result
     */
    public static BufferedImage multiplyNewButBroken(BufferedImage image1, BufferedImage image2) {
        int width = image1.getWidth();
        if (width != image2.getWidth()) {
            throw new IllegalArgumentException("widths not equal");
        }

        int height = image1.getHeight();
        if (height != image2.getHeight()) {
            throw new IllegalArgumentException("heights not equal");
        }

        // get pixels from both images
        int[] pixels1 = imageToPixels(image1);
        int[] pixels2 = imageToPixels(image2);
        int[] pixels3 = new int[width * height];

        // create 3rd (output) image
        BufferedImage image3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int rgb1 = pixels1[row * col];
                int a1 = (rgb1 >> 24) & 255;
                int r1 = (rgb1 >> 16) & 255;
                int g1 = (rgb1 >> 8) & 255;
                int b1 = rgb1 & 255;

                int rgb2 = pixels2[row * col];
                int a2 = (rgb2 >> 24) & 255;
                int r2 = (rgb2 >> 16) & 255;
                int g2 = (rgb2 >> 8) & 255;
                int b2 = rgb2 & 255;

                int a3 = (int) ((a1 * a2) / 255);
                int r3 = (int) ((r1 * r2) / 255);
                int g3 = (int) ((g1 * g2) / 255);
                int b3 = (int) ((b1 * b2) / 255);
                pixels3[row * col] = (a3 << 24) | (r3 << 16) | (g3 << 8) | b3;
            }
        }

        image3.setRGB(0, 0, width, height, pixels3, 0, width);
        return image3;
    }

    /**
     * Multiply the contents of two BufferedImages
     * @param bi1 first BufferedImage
     * @param bi2 second BufferedImage
     * @return BufferedImage result
     */
    public static BufferedImage multiply(BufferedImage bi1, BufferedImage bi2) {
        int width = bi1.getWidth();
        int height = bi1.getHeight();

        BufferedImage bi3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] rgbim1 = new int[width];
        int[] rgbim2 = new int[width];
        int[] rgbim3 = new int[width];

        for (int row = 0; row < height; row++) {
            bi1.getRGB(0, row, width, 1, rgbim1, 0, width);
            bi2.getRGB(0, row, width, 1, rgbim2, 0, width);

            for (int col = 0; col < width; col++) {
                int rgb1 = rgbim1[col];
                int a1 = (rgb1 >> 24) & 255;
                int r1 = (rgb1 >> 16) & 255;
                int g1 = (rgb1 >> 8) & 255;
                int b1 = rgb1 & 255;

                int rgb2 = rgbim2[col];
                int a2 = (rgb2 >> 24) & 255;
                int r2 = (rgb2 >> 16) & 255;
                int g2 = (rgb2 >> 8) & 255;
                int b2 = rgb2 & 255;

                int a3 = (int) ((a1 * a2) / 255);
                int r3 = (int) ((r1 * r2) / 255);
                int g3 = (int) ((g1 * g2) / 255);
                int b3 = (int) ((b1 * b2) / 255);
                rgbim3[col] = (a3 << 24) | (r3 << 16) | (g3 << 8) | b3;
            }

            bi3.setRGB(0, row, width, 1, rgbim3, 0, width);
        }

        return bi3;
    }

    /**
     * Returns the brightest pixel for a given area of an image
     * @param image The image to compute pixels for
     * @param x Starting X location
     * @param y Starting Y location
     * @param width The width to test
     * @param height The height to test
     * @return int The brightest pixel found
     */
    public static int computeAverageColor(BufferedImage image, int x, int y, int width, int height) {
        // automatic variables
        int red = 0;
        int green = 0;
        int blue = 0;
        int sum = 0;
        int max = 0;
        int currPixel = 0;
        int retPixel = 0;

        // get pixel array
        int[] pixels = imageToPixels(image, x, y, width, height);

        // iterate thru pixels
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // get current pixel
                currPixel = pixels[i + j * width];
                // get values
                red = (currPixel & 0xff0000) >> 16;
                green = (currPixel & 0xff00) >> 8;
                blue = (currPixel & 0xff);
                sum = red + green + blue;
                // check for max
                if (sum > max) {
                    max = sum;
                    // store pixel
                    retPixel = toPixel(red, green, blue);
                }
            }
        }

        return retPixel;
    }

    /**
     * Normalizes an image in the upper part of the spectrum, leaving the lower intensity values alone
     * @param image The image to compute pixels for
     * @return BufferedImage normalized (upper area anyway) image
     */
    public static BufferedImage normalize(BufferedImage image) {
        // automatic variables
        int width = image.getWidth();
        int height = image.getHeight();
        int red = 0;
        int green = 0;
        int blue = 0;

        // get pixel array
        int[] srcPixels = imageToPixels(image, 0, 0, width, height);

        // make a copy of the pixels array
        int[] normPixels = (int[]) Util.copy(srcPixels);

        // break apart channels
        int size = normPixels.length;
        int[] redPixels = new int[size];
        int[] greenPixels = new int[size];
        int[] bluePixels = new int[size];
        int currPixel = 0;
        for (int i = 0; i < size; i++) {
            // get current pixel
            currPixel = normPixels[i];
            // get values
            red = (currPixel & 0xff0000) >> 16;
            green = (currPixel & 0xff00) >> 8;
            blue = (currPixel & 0xff);
            // assign value to each channel
            redPixels[i] = red;
            greenPixels[i] = green;
            bluePixels[i] = blue;
        }

        // normalize each channel
        redPixels = normalizeChannel(redPixels);
        greenPixels = normalizeChannel(greenPixels);
        bluePixels = normalizeChannel(bluePixels);

        // combine channels back into main image
        BufferedImage normalizedImage = pixelsToImage(redPixels, greenPixels, bluePixels, width, height);

        return normalizedImage;
    }

    /**
     * Normalizes an individual color channel
     * @param pixels The pixels to normalize
     * @return int [] normalized pixels
     */
    public static int[] normalizeChannel(int[] pixels) {
        // automatic variables
        int low = 0;
        int high = 0;

        // find range
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] > high) {
                high = pixels[i];
            }
        }
        int effectiveRange = 255 - low;
        int imageRange = high - low;

        // process pixels
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = pixels[i] * effectiveRange / imageRange;
        }

        return pixels;
    }

    /**
     * Converts a color pixel to grayscale
     * @param pixel The pixel
     * @return int Grayscaled pixel
     */
    public static int RGBToGray(int pixel) {
        int red = (pixel & 0xff0000) >> 16;
        int green = (pixel & 0xff00) >> 8;
        int blue = (pixel & 0xff);
        int gray = (red + green + blue) / 3;
        return (0xff000000 | (gray << 16) | (gray << 8) | gray);
    }

    /**
     * Creates a new buffered image from the source image but flipped vertically
     * @param image - the source image
     * @param width - the width of the image
     * @param height - the height of the image
     * @return BufferedImage
     */
    public static BufferedImage flipVertical(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    /**
     * Creates a new buffered image from the source image but flipped horizontally
     * @param image - the source image
     * @param width - the width of the image
     * @param height - the height of the image
     * @return BufferedImage
     */
    public static BufferedImage flipHorizontal(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    /**
     * Creates a new buffered image from the source image but flipped both vertically
     * and horizontally (Equivalent to rotating the image 180 degrees)
     * @param image - the source image
     * @param width - the width of the image
     * @param height - the height of the image
     * @return BufferedImage
     */
    public static BufferedImage flipVertNHoriz(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
        tx.translate(-image.getWidth(null), -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

////////////////////////////////////////////////////////////////////////////////
// Utility Methods
////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the pixels of a BufferedImage
     * @param image The image to get pixels from
     * @return The pixel array
     */
    public static int[] imageToPixels(BufferedImage image) {
        int[] pixels = (int[]) image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
        return pixels;
    }

    /**
     * Converts a BufferedImage to a pixel array
     * @param image The image to get pixels from
     * @return The pixel array
     */
    public static int[] imageToPixels(BufferedImage image, int x, int y, int width, int height) {
        int[] pixels = (int[]) image.getRaster().getDataElements(x, y, width, height, null);
        return pixels;
    }

    /**
     * Converts a pixel array to a BufferedImage
     * @param pixels The pixel data
     * @param width The width of the image
     * @param height The height of the image
     * @return BufferedImage The image
     */
    public static BufferedImage pixelsToImage(int[] pixels, int width, int height) {
        // TODO: Get this using Raster instead of setRGB (performance is potentially better)
        // Raster raster = new Raster();
        // WritableRaster wr = (WritableRaster) raster;
        // WritableRaster newRaster = wr.createWritableTranslatedChild(0,0);
        // BufferedImage image = new BufferedImage(planarimage.getColorModel(),
        // newRaster,false,new Hashtable());
        // return image;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    /**
     * Converts a pixel array to a BufferedImage given independent RGB channels
     * @param red The red channel
     * @param green The green channel
     * @param blue The blue channel
     * @param width The width of the image
     * @param height The height of the image
     * @return BufferedImage The image
     */
    public static BufferedImage pixelsToImage(int[] red, int[] green, int[] blue, int width, int height) {
        // create new array to combine all color channels into
        int size = red.length;
        int[] pixels = new int[size];

        // combine pixels
        for (int i = 0; i < size; i++) {
            pixels[i] = (0xff000000 | (red[i] << 16) | (green[i] << 8) | blue[i]);
        }

        // create image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        return image;
    }

    /**
     * Creates a pixel from color values
     * @param red The value of the RED channel
     * @param green The value of the GREEN channel
     * @param blue The value of the BLUE channel
     * @return int The pixel
     */
    public static int toPixel(int red, int green, int blue) {
        return (0xff000000 | (red << 16) | (green << 8) | blue);
    }

    /**
     * Returns the alpha value of a pixel
     * @param pixel The pixel
     * @return int value
     */
    public static int getAlpha(int pixel) {
        return ((pixel >> 24) & 0xFF);
    }

    /**
     * Returns the red value of a pixel
     * @param pixel The pixel
     * @return int value
     */
    static public int getRed(int pixel) {
        return ((pixel >> 16) & 0xFF);
    }

    /**
     * Returns the green value of a pixel
     * @param pixel The pixel
     * @return int value
     */
    static public int getGreen(int pixel) {
        return ((pixel >> 8) & 0xFF);
    }

    /**
     * Returns the blue value of a pixel
     * @param pixel The pixel
     * @return int value
     */
    static public int getBlue(int pixel) {
        return (pixel & 0xFF);
    }

}
