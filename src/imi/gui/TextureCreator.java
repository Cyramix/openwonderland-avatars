/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TextureCreator.java
 *
 * Created on Feb 26, 2009, 1:22:10 PM
 */

package imi.gui;

import imi.imaging.ImageData;
import java.awt.Dimension;
import java.util.ArrayList;

/**
 *
 * @author ptruong
 */
public class TextureCreator extends javax.swing.JFrame {

    private SceneEssentials             m_sceneData = null;
    private JPanel_MaterialProperties   m_materialProperties = null;

    /** Creates new form TextureCreator */
    public TextureCreator() {
        initComponents();

        m_materialProperties = new JPanel_MaterialProperties();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(m_materialProperties, gridBagConstraints);
    }

    public TextureCreator(SceneEssentials sceneData) {
        m_sceneData = sceneData;
        initComponents();

        m_materialProperties = new JPanel_MaterialProperties(m_sceneData, this);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(m_materialProperties, gridBagConstraints);
    }
    
    public void updateImage(Object[] data) {
        ImageData baseImage = null;
        ImageData blendData = null;

        if (data == null) {
            // Update the base image panel and grab it for the final image
            BaseImageView.repaint();
            if (BaseImageView.getRenderables().size() > 0)
                baseImage = BaseImageView.getRenderables().get(0);

            // Update the blend image panel and grab the final image for the final image panel
            BlendImageView.updateOpacity(0, -1);
            if (BlendImageView.getRenderables().size() > 1)
                blendData = BlendImageView.getFinalImage();
            else if (BlendImageView.getRenderables().size() == 1)
                blendData = BlendImageView.getRenderables().get(0);

            // Check to make sure the images from base panel and blend are not null
            ArrayList<ImageData> renderables = new ArrayList<ImageData>();
            if (baseImage != null)
                renderables.add(baseImage);
            if (blendData != null)
                renderables.add(blendData);

            // Add the renderables to the final image preview panel
            FinalImageView.setRenderables(renderables);

            // Determine what type of update to perform
            if (renderables.size() <= 1)
                FinalImageView.updateOpacity(0, -1);
            else
                FinalImageView.addDecalToBase();

        } else {
            // Update the base image panel and grab it for the final image
            ArrayList<ImageData> renderables = new ArrayList<ImageData>();
            BaseImageView.repaint();
            if (BaseImageView.getRenderables().size() > 0) {
                baseImage = BaseImageView.getRenderables().get(0);
                renderables.add(baseImage);
            }

            // Update the blend image panel and grab the final image for the final image panel
            BlendImageView.updateImage(data);
            if (BlendImageView.getRenderables().size() > 1) {
                blendData = BlendImageView.getFinalImage();
                renderables.add(blendData);
            } else if (BlendImageView.getRenderables().size() == 1) {
                blendData = BlendImageView.getRenderables().get(0);
                renderables.add(blendData);
            }

            FinalImageView.setRenderables(renderables);

            if (renderables.size() <= 1)
                FinalImageView.updateOpacity(0, -1);
            else
                FinalImageView.addDecalToBase();
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    public RenderPanel getBaseImagePanel() {
        return BaseImageView;
    }

    public RenderPanel getBlendImagePanel() {
        return BlendImageView;
    }

    public RenderPanel getFinalImagePanel() {
        return FinalImageView;
    }

    public ControlPanel getControlPanel() {
        return ControlPanel;
    }

    public LayerPanel getLayerPanel() {
        return LayerPanel;
    }

    public JPanel_MaterialProperties getMaterialPropPanel() {
        return m_materialProperties;
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public void setSceneData(SceneEssentials sceneData) {
        m_sceneData = sceneData;
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

        BaseImageView = new imi.gui.RenderPanel();
        BlendImageView = new imi.gui.RenderPanel();
        FinalImageView = new imi.gui.RenderPanel();
        ControlPanel = new imi.gui.ControlPanel(this);
        LayerPanel = new imi.gui.LayerPanel(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(BaseImageView, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(BlendImageView, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(FinalImageView, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(ControlPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(LayerPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TextureCreator().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.RenderPanel BaseImageView;
    private imi.gui.RenderPanel BlendImageView;
    private imi.gui.ControlPanel ControlPanel;
    private imi.gui.RenderPanel FinalImageView;
    private imi.gui.LayerPanel LayerPanel;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void resetGUI () {
        ControlPanel.resetGUI();
    }

    public void toggleGUIUpdate(Boolean update) {
        ControlPanel.setGUIUpdate(update);
    }

    public void updateControls(ImageData data) {
        ControlPanel.updateControls(data);
    }

    public void addBlendLayer(String layerName) {
        LayerPanel.addLayer(layerName);
    }

    public void removeBlendLayer() {
        LayerPanel.removeLayer();
    }

    public void removeBlendLayer(int layerIndex) {
        BlendImageView.getRenderables().remove(layerIndex);
    }

    public int getSelectedLayerIndex() {
        return LayerPanel.getSelectedLayer();
    }

    public void setBaseImageSize() {
        Dimension d = BaseImageView.getBaseImageSize();
        BlendImageView.setBaseImageSize(d);
        FinalImageView.setBaseImageSize(d);
    }
}
