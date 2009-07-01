/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.imaging;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

/**
 *
 * @author ptruong
 */
public class ImageData {

    public String           m_relFilePath   = null;
    public String           m_name          = null;
    public int              m_xPos          = -1;
    public int              m_yPos          = -1;
    public int              m_repeat        = 0;
    public double           m_xScale        = -1;
    public double           m_yScale        = -1;
    public double           m_rotation      = -1;
    public double           m_opacity       = -1;
    public BufferedImage    m_srcImage      = null;
    public BufferedImage    m_curImage      = null;
    public int[]            m_pixels        = null;
    public Rectangle        m_textureAnchor = new Rectangle();
    public Rectangle        m_imageRect     = new Rectangle();
    public TexturePaint     m_texturePaint  = null;
    public boolean          m_visible       = true;
    public boolean          m_hflip         = false;
    public boolean          m_vflip         = false;

    /**
     * Default Constructor
     */
    public ImageData() {
        // Doesn't do anything so set the data members.
    }

    /**
     * Overloaded Constructor
     * @param relFilePath - string with the relative path to the file
     * @param xPos - integer for the x position of the image
     * @param yPos - integer for the y position of the image
     * @param xScale - float for the x scale of the image
     * @param yScale - float for the y scale of the image
     * @param rotation - float for the radian rotation of the image
     * @param image - BufferedImage of the file.
     */
    public ImageData(String relFilePath, String name, int xPos, int yPos, double xScale, double yScale, double rotation, double opacity, BufferedImage image, int[] pixels ) {
        m_relFilePath   = relFilePath;
        m_name          = name;
        m_xPos          = xPos;
        m_yPos          = yPos;
        m_xScale        = xScale;
        m_yScale        = yScale;
        m_rotation      = rotation;
        m_opacity       = opacity;
        m_srcImage      = image;

        m_pixels    = new int[pixels.length];
        for (int i = 0; i < m_pixels.length; i++) {
            m_pixels[i] = pixels[i];
        }
    }

    public Dimension getCurImageCenter() {
        Dimension center = new Dimension();

        int xOffset = ((int)(m_curImage.getWidth() * m_xScale) / 2);
        int yOffset = ((int)(m_curImage.getHeight() * m_yScale) / 2);

        center.width    = m_xPos + xOffset;
        center.height   = m_yPos + yOffset;

        return center;
    }

    public Dimension getSrcImageCenter() {
        Dimension center = new Dimension();

        int xOffset = m_srcImage.getWidth() / 2;
        int yOffset = m_srcImage.getHeight() / 2;

        center.width    = m_xPos + xOffset;
        center.height   = m_yPos + yOffset;

        return center;
    }

    public Dimension getCurImageDimensions() {
        Dimension dimensions = new Dimension();

        dimensions.width    = m_curImage.getWidth();
        dimensions.height   = m_curImage.getHeight();

        return dimensions;
    }

    public Dimension getSrcImageDimensions() {
        Dimension dimensions = new Dimension();

        dimensions.width    = m_srcImage.getWidth();
        dimensions.height   = m_srcImage.getHeight();

        return dimensions;
    }
}
