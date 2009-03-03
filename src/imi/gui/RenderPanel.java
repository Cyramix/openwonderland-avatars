/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RenderPanel.java
 *
 * Created on Feb 26, 2009, 1:21:31 PM
 */

package imi.gui;

import imi.imaging.ImageData;
import imi.imaging.ImageLibraryExt;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.border.TitledBorder;

/**
 *
 * @author ptruong
 */
public class RenderPanel extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private ArrayList<ImageData>    m_renderables           = new ArrayList<ImageData>();
    private ImageData               m_finalImage            = null;
    private Dimension               m_baseImageSize         = null;

////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    /** Creates new form RenderPanel */
    public RenderPanel() {
        initComponents();
    }

    public void updateImage(Object[] data) {

        if (data == null) {
            updateOpacity(0, -1);
        } else {
            int     index   = (Integer) data[0];
            double  opacity = (Double) data[6];

            updateTransform(data);
            updateOpacity(index, opacity);
        }
    }

    public void updateOpacity(int index, double opacity) {

        if (m_renderables.size() == 1) {
            m_finalImage = null;
            if (opacity > -1)
                m_renderables.get(index).m_opacity = opacity;

            setViewImage(m_renderables.get(0).m_curImage);
            repaint();
            return;
        }

        if (m_renderables.size() == 0) {
            setViewImage(null);
            repaint();
            return;
        }

        if (!isAllVisable()) {
            m_finalImage = null;
            setViewImage(null);
            repaint();
            return;
        }

        if (opacity > -1)
            m_renderables.get(index).m_opacity = opacity;

        int i, j;
        BufferedImage finalImage = null;
        for (i = 0; i < m_renderables.size(); i++) {
            if (m_renderables.get(i) != null && m_renderables.get(i).m_visible) {
                BufferedImage image     = m_renderables.get(i).m_curImage;
                finalImage              = m_renderables.get(i).m_curImage;
                int[] pixels            = ImageLibraryExt.imageToPixels(finalImage);
                m_finalImage            = new ImageData(null, "FinalImage.png", 0, 0, 1.0, 1.0, 0.0, 1.0, image, pixels);
                m_finalImage.m_curImage = finalImage;
                m_finalImage.m_opacity  = m_renderables.get(i).m_opacity;
                break;
            }
        }

        for (j = i + 1; j < m_renderables.size(); j++) {
            if (m_renderables.get(j) != null && m_renderables.get(j).m_visible)
                m_finalImage.m_curImage = ImageLibraryExt.blend(m_finalImage, m_renderables.get(j), m_renderables.get(j).m_opacity, m_baseImageSize.width, m_baseImageSize.height);
        }

        setViewImage(m_finalImage.m_curImage);
    }

    public void updateTransform (Object[] data) {
        int     index   = (Integer) data[0];
        int     xpos    = (Integer) data[1];
        int     ypos    = (Integer) data[2];
        double  scaleX  = (Double)  data[3];
        double  scaleY  = (Double)  data[4];
        double  deltaR  = (Double)  data[5];
        boolean bHFlip  = (Boolean) data[7];
        boolean bVFlip  = (Boolean) data[8];
        int     repeat  = (Integer) data[9];

        // Retrieving selected image data
        BufferedImage image     = m_renderables.get(index).m_srcImage;
        Dimension dimensions    = m_renderables.get(index).getSrcImageDimensions();
        int[] pixels            = m_renderables.get(index).m_pixels;

        m_renderables.get(index).m_curImage  = ImageLibraryExt.pixelsToImage(pixels, dimensions.width, dimensions.height);
        m_renderables.get(index).m_imageRect.setBounds(0, 0, dimensions.width, dimensions.height);
        m_renderables.get(index).m_textureAnchor.setBounds(0, 0, dimensions.width + repeat, dimensions.height + repeat);
        m_renderables.get(index).m_texturePaint = new TexturePaint(image, m_renderables.get(index).m_textureAnchor);

        Graphics2D g2d = m_renderables.get(index).m_curImage.createGraphics();
        g2d.setPaint(m_renderables.get(index).m_texturePaint);
        g2d.fill(m_renderables.get(index).m_imageRect);
        g2d.dispose();

        m_renderables.get(index).m_curImage = ImageLibraryExt.translate(m_renderables.get(index).m_curImage, xpos, ypos);
        Dimension center        = m_renderables.get(index).getCurImageCenter();
        m_renderables.get(index).m_curImage = ImageLibraryExt.rotate(m_renderables.get(index).m_curImage, deltaR, center.width, center.height);
        m_renderables.get(index).m_curImage = ImageLibraryExt.scale(m_renderables.get(index).m_curImage, scaleX, scaleY);
        if (bHFlip)
            m_renderables.get(index).m_curImage = ImageLibraryExt.flipHorizontal(m_renderables.get(index).m_curImage);
        if (bVFlip)
            m_renderables.get(index).m_curImage = ImageLibraryExt.flipVertical(m_renderables.get(index).m_curImage);

        m_renderables.get(index).m_xPos    += xpos;
        m_renderables.get(index).m_yPos    += ypos;
        m_renderables.get(index).m_xScale   = scaleX;
        m_renderables.get(index).m_yScale   = scaleY;
        m_renderables.get(index).m_rotation = Math.toDegrees(deltaR);
        m_renderables.get(index).m_hflip    = bHFlip;
        m_renderables.get(index).m_vflip    = bVFlip;
        m_renderables.get(index).m_repeat   = repeat;
    }

    public void addImage(File imageFile) {

        BufferedImage image     = ImageLibraryExt.load(imageFile.getPath());
        int[] imagePixels       = ImageLibraryExt.imageToPixels(image);
        ImageData data          = new ImageData(null, imageFile.getName(), 0, 0, 1.0, 1.0, 0.0, 1.0, image, imagePixels);
        Dimension dimensions    = data.getSrcImageDimensions();

        data.m_curImage  = ImageLibraryExt.pixelsToImage(imagePixels, dimensions.width, dimensions.height);
        data.m_textureAnchor.setBounds(0, 0, dimensions.width, dimensions.height);
        data.m_texturePaint = new TexturePaint(image, data.m_textureAnchor);

        if (m_baseImageSize == null)
            m_baseImageSize = dimensions;

        data.m_imageRect.setBounds(0, 0, dimensions.width, dimensions.height);

        m_renderables.add(data);
        updateOpacity(0, -1);
    }

    public void addDecalToBase() {
        BufferedImage image         = m_renderables.get(0).m_curImage;
        BufferedImage finalImage    = m_renderables.get(0).m_curImage;
        int[] pixels                = ImageLibraryExt.imageToPixels(finalImage);
        m_finalImage                = new ImageData(null, "FinalImage.png", 0, 0, 1.0, 1.0, 0.0, 1.0, image, pixels);
        m_finalImage.m_curImage     = finalImage;
        m_finalImage.m_opacity      = 1.0;
        m_finalImage.m_curImage     = ImageLibraryExt.multiply(m_finalImage.m_curImage, m_renderables.get(1).m_curImage);

        setViewImage(m_finalImage.m_curImage);
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public ArrayList<ImageData> getRenderables() {
        return m_renderables;
    }

    public Dimension getBaseImageSize() {
        return m_baseImageSize;
    }

    public ImageData getFinalImage() {
        return m_finalImage;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setViewName(String viewName) {
        TitledBorder tBorder = (TitledBorder) View.getBorder();
        tBorder.setTitle(viewName);
    }

    public void setViewImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            View.setIcon(null);
            return;
        }

        ImageIcon i = new ImageIcon(bufferedImage);
        View.setIcon(i);
    }

    public void setRenderables(ArrayList<ImageData> renderables) {
        m_renderables = renderables;
    }

    public void setBaseImageSize(Dimension size) {
        m_baseImageSize = size;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        ViewScroll = new javax.swing.JScrollPane();
        View = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        ViewScroll.setAutoscrolls(true);

        View.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        View.setForeground(new java.awt.Color(255, 102, 0));
        View.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        View.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Image Type", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Lucida Grande", 1, 13), new java.awt.Color(255, 102, 0))); // NOI18N
        View.setDoubleBuffered(true);
        ViewScroll.setViewportView(View);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(ViewScroll, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel View;
    private javax.swing.JScrollPane ViewScroll;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void replaceRenderable(int layerIndex, ImageData newRendrable) {
        m_renderables.set(layerIndex, newRendrable);
    }

    public void removeRenderable(int layerIndex) {
        m_renderables.remove(layerIndex);
    }

    public void removeRenderable(ImageData renderable) {
        m_renderables.remove(renderable);
    }

    public void addRenderable(ImageData renderable) {
        m_renderables.add(renderable);
    }

    public void updateLayerVisibility(int layerIndex, Boolean visibility) {
        m_renderables.get(layerIndex).m_visible = visibility;
        updateImage(null);
    }

    public ImageData getSelectedLayer(int layerIndex) {
        return m_renderables.get(layerIndex);
    }

    public boolean isAllVisable() {
        int counter = 0;
        for (int i = 0; i < m_renderables.size(); i++) {
            if (!m_renderables.get(i).m_visible)
                counter++;
        }

        if (counter == m_renderables.size())
            return false;
        else
            return true;
    }
}
