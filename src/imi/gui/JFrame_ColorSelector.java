/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JFrame_ColorSelector.java
 *
 * Created on Jan 26, 2009, 10:24:13 AM
 */

package imi.gui;
////////////////////////////////////////////////////////////////////////////////
// Imports
////////////////////////////////////////////////////////////////////////////////
import com.jme.scene.state.CullState;
import imi.loaders.repository.Repository;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.MeshColorModulation;
import imi.scene.shader.programs.ClothingShaderDiffuseAsSpec;
import imi.scene.shader.programs.ClothingShaderSpecColor;
import imi.scene.shader.programs.FleshShader;
import imi.scene.shader.programs.SimpleTNLWithAmbient;
import java.awt.Color;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author ptruong
 */
public class JFrame_ColorSelector extends javax.swing.JFrame implements ChangeListener{
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private SceneEssentials             m_sceneData     = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initualizes the gui components.  If default constructor
     * is the construction choice, remember to set the sceneData before continueing
     * with anything else.
     */
    public JFrame_ColorSelector() {
        initComponents();
    }

    /**
     * Overloaded constructor that initializes the gui and sets the sceneData which
     * is used to grab the skeleton information.
     * @param scene
     */
    public JFrame_ColorSelector(SceneEssentials scene) {
        m_sceneData = scene;
        initComponents();
    }

    /**
     * Toggle's the hair color for the avatar that is loaded.  If there is no avatar
     * or no hair is found, then it will return and do nothing.
     */
    public void toggleHairColor() {
        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        PNode node = m_sceneData.getAvatar().getSkeleton().findChild("Hair");
        PPolygonMeshInstance hair = null;
        if (node != null)
            hair = (PPolygonMeshInstance) node.getChild(0);
        else
            return;

        Color selection = jColorChooser_Colors.getColor();
//        jToggleButton_HairColor.setForeground(selection);
        jToggleButton_HairColor.setBackground(selection);
        float[] color   = new float[3];
        color[0]        = ((float)selection.getRed()/255);
        color[1]        = ((float)selection.getGreen()/255);
        color[2]        = ((float)selection.getBlue()/255);

        setMeshColor(hair, color);
    }

    /**
     * Toggle's the facial hair color for the avatar that is loaded.  If there is no
     * avatar or no facial hair is found, then it will return and do nothing
     */
    public void toggleFacialHairColor() {
        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        PNode node = m_sceneData.getAvatar().getSkeleton().findChild("FacialHair");
        PPolygonMeshInstance facialhair = null;
        if (node != null)
            facialhair = (PPolygonMeshInstance) node.getChild(0);
        else
            return;

        Color selection = jColorChooser_Colors.getColor();
//        jToggleButton_FacialHairColor.setForeground(selection);
        jToggleButton_FacialHairColor.setBackground(selection);
        float[] color   = new float[3];
        color[0]        = ((float)selection.getRed()/255);
        color[1]        = ((float)selection.getGreen()/255);
        color[2]        = ((float)selection.getBlue()/255);

        setMeshColor(facialhair, color);
    }

    /**
     * Toggle's the skin color for the avatar that is loaded.  If there is no avatar
     * loaded, then the method will return and do nothing
     */
    public void toggleSkinColor() {
        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        List<PPolygonSkinnedMeshInstance> lHead, lHands, lUpperBody, lLowerBody, lFeet;
        lHead           = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("Head");
        lHands          = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("Hands");
        lUpperBody      = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("UpperBody");
        lLowerBody      = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("LowerBody");
        lFeet           = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("Feet");

        Color selection = jColorChooser_Colors.getColor();
//        jToggleButton_SkinTone.setForeground(selection);
        jToggleButton_SkinTone.setBackground(selection);
        float[] color   = new float[3];
        color[0]        = ((float)selection.getRed()/255);
        color[1]        = ((float)selection.getGreen()/255);
        color[2]        = ((float)selection.getBlue()/255);

        for (int j = 0; j < lHead.size(); j++) {
            if (lHead.get(j).getName().toLowerCase().contains("head")) {
               PPolygonMeshInstance mesh = (PPolygonMeshInstance)lHead.get(j);
               setSkinnedMeshColor(mesh, color);
            }
        }

        for (int j = 0; j < lHands.size(); j++) {
            PPolygonMeshInstance mesh = (PPolygonMeshInstance)lHands.get(j);
            setSkinnedMeshColor(mesh, color);
        }

        for (int j = 0; j < lUpperBody.size(); j++) {
            if (lUpperBody.get(j).getName().toLowerCase().contains("nude") || lUpperBody.get(j).getName().toLowerCase().contains("arms")) {
               PPolygonMeshInstance mesh = (PPolygonMeshInstance)lUpperBody.get(j);
               setSkinnedMeshColor(mesh, color);
            }
        }

        for (int j = 0; j < lLowerBody.size(); j++) {
            if (lLowerBody.get(j).getName().toLowerCase().contains("nude") || lLowerBody.get(j).getName().toLowerCase().contains("legs")) {
               PPolygonMeshInstance mesh = (PPolygonMeshInstance)lLowerBody.get(j);
               setSkinnedMeshColor(mesh, color);
            }
        }

        for (int j = 0; j < lFeet.size(); j++) {
            if (lFeet.get(j).getName().toLowerCase().contains("nude") || lFeet.get(j).getName().toLowerCase().contains("foot")) {
               PPolygonMeshInstance mesh = (PPolygonMeshInstance)lFeet.get(j);
               setSkinnedMeshColor(mesh, color);
            }
        }
    }

    /**
     * Toggles the shirt/blouse color for the avatar that is loaded.  If there is
     * no avatar or shirt loaded, the method will return and do nothing;
     */
    public void toggleShirtColor() {
        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        List<PPolygonSkinnedMeshInstance> lUpperBody;
        lUpperBody      = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("UpperBody");

        Color selection = jColorChooser_Colors.getColor();
//        jToggleButton_ShirtColor.setForeground(selection);
        jToggleButton_ShirtColor.setBackground(selection);
        float[] color   = new float[3];
        color[0]        = ((float)selection.getRed()/255);
        color[1]        = ((float)selection.getGreen()/255);
        color[2]        = ((float)selection.getBlue()/255);

        for (int j = 0; j < lUpperBody.size(); j++) {
            if (!lUpperBody.get(j).getName().toLowerCase().contains("nude") && !lUpperBody.get(j).getName().toLowerCase().contains("arms")) {
               PPolygonMeshInstance mesh = (PPolygonMeshInstance)lUpperBody.get(j);
               setSkinnedMeshColor(mesh, color);
            }
        }
    }

    /**
     * Toggles the pants color for the avtar that is loaded.  If there is no avatar
     * or shirt loaded, then the method will return and do nothing;
     */
    public void togglePantsColor() {
        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        List<PPolygonSkinnedMeshInstance> lLowerBody;
        lLowerBody      = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("LowerBody");

        Color selection = jColorChooser_Colors.getColor();
//        jToggleButton_PantsColor.setForeground(selection);
        jToggleButton_PantsColor.setBackground(selection);
        float[] color   = new float[3];
        color[0]        = ((float)selection.getRed()/255);
        color[1]        = ((float)selection.getGreen()/255);
        color[2]        = ((float)selection.getBlue()/255);

        for (int j = 0; j < lLowerBody.size(); j++) {
            if (!lLowerBody.get(j).getName().toLowerCase().contains("nude") && !lLowerBody.get(j).getName().toLowerCase().contains("legs")) {
               PPolygonMeshInstance mesh = (PPolygonMeshInstance)lLowerBody.get(j);
               setSkinnedMeshColor(mesh, color);
            }
        }
    }

    /**
     * Toggles the shoes color for the avatar that is loaded.  If there is no avatar
     * or no footwear loaded, then the method will return and do nothing;
     */
    public void toggleShoesColor() {
        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        List<PPolygonSkinnedMeshInstance> lFeet;
        lFeet           = m_sceneData.getAvatar().getSkeleton().retrieveSkinnedMeshes("Feet");

        Color selection = jColorChooser_Colors.getColor();
        jToggleButton_ShoesColor.setForeground(selection);
        jToggleButton_ShoesColor.setBackground(selection);
        float[] color   = new float[3];
        color[0]        = ((float)selection.getRed()/255);
        color[1]        = ((float)selection.getGreen()/255);
        color[2]        = ((float)selection.getBlue()/255);

        for (int j = 0; j < lFeet.size(); j++) {
            if (!lFeet.get(j).getName().toLowerCase().contains("nude") || !lFeet.get(j).getName().toLowerCase().contains("foot")) {
               PPolygonMeshInstance mesh = (PPolygonMeshInstance)lFeet.get(j);
               setSkinnedMeshColor(mesh, color);
            }
        }
    }

    /**
     * Grabs the material and shader set to the selected mesh and checks to see
     * if a color modulation effect is applied.  If not it creates a new shader
     * property and resets the material to the selected mesh instance.
     * @param meshInst - mesh to apply the color modulation to
     * @param fColorArray - the user specified color to apply
     */
    public void setSkinnedMeshColor(PPolygonMeshInstance meshInst, float[] fColorArray) {
        // assign a texture to the mesh instance
        PMeshMaterial material = meshInst.getMaterialRef();
        AbstractShaderProgram shader = material.getShader();

        try {

            // Setting the new color property onto the model here
            if (shader instanceof FleshShader)
                ((FleshShader) shader).setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, fColorArray));
            else if (shader instanceof ClothingShaderSpecColor)
                ((ClothingShaderSpecColor) shader).setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, fColorArray));
            else if (shader instanceof ClothingShaderDiffuseAsSpec)
                ((ClothingShaderDiffuseAsSpec) shader).setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, fColorArray));

        } catch (NoSuchPropertyException ex) {
            System.out.println("SEVER EXCEPTION: " + ex.getMessage());
        }

        meshInst.applyShader();
    }

    /**
     * Sets the shader for non skinned meshes such as hair and accessories.
     * @param meshInst - mesh to apply the color modulation instance to
     * @param fColorArray - the user specified color to apply
     */
    public void setMeshColor(PPolygonMeshInstance meshInst, float[] fColorArray) {
        // assign a texture to the mesh instance
        PMeshMaterial material = meshInst.getMaterialRef();
        GLSLShaderProgram shader = (GLSLShaderProgram)material.getShader();
        Repository repo = (Repository)m_sceneData.getWM().getUserData(Repository.class);
        AbstractShaderProgram accessoryShader = repo.newShader(SimpleTNLWithAmbient.class);
        
        if (meshInst.getParent().getName().toLowerCase().contains("hair"))
        {
            shader.addEffect(new MeshColorModulation());
            try {
                shader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, fColorArray));
            } catch (Exception ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
        } else {
            material.setShader(accessoryShader);
        }
        
        material.setCullFace(CullState.Face.None);
        meshInst.applyShader();
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setScene(SceneEssentials scene) {
        m_sceneData = scene;
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

        jColorChooser_Colors = new javax.swing.JColorChooser();
        jPanel_ToggleSelections = new javax.swing.JPanel();
        jToggleButton_ShirtColor = new javax.swing.JToggleButton();
        jToggleButton_PantsColor = new javax.swing.JToggleButton();
        jToggleButton_ShoesColor = new javax.swing.JToggleButton();
        jToggleButton_HairColor = new javax.swing.JToggleButton();
        jToggleButton_FacialHairColor = new javax.swing.JToggleButton();
        jToggleButton_SkinTone = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jColorChooser_Colors.getSelectionModel().addChangeListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jColorChooser_Colors, gridBagConstraints);

        jPanel_ToggleSelections.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel_ToggleSelections.setMinimumSize(new java.awt.Dimension(362, 50));
        jPanel_ToggleSelections.setPreferredSize(new java.awt.Dimension(405, 50));
        jPanel_ToggleSelections.setLayout(new java.awt.GridBagLayout());

        jToggleButton_ShirtColor.setText("Shirt Color");
        jToggleButton_ShirtColor.setMinimumSize(new java.awt.Dimension(148, 26));
        jToggleButton_ShirtColor.setPreferredSize(new java.awt.Dimension(148, 26));
        jPanel_ToggleSelections.add(jToggleButton_ShirtColor, new java.awt.GridBagConstraints());

        jToggleButton_PantsColor.setText("Pants Color");
        jToggleButton_PantsColor.setMinimumSize(new java.awt.Dimension(148, 26));
        jToggleButton_PantsColor.setPreferredSize(new java.awt.Dimension(148, 26));
        jPanel_ToggleSelections.add(jToggleButton_PantsColor, new java.awt.GridBagConstraints());

        jToggleButton_ShoesColor.setText("Shoes Color");
        jToggleButton_ShoesColor.setMinimumSize(new java.awt.Dimension(148, 26));
        jToggleButton_ShoesColor.setPreferredSize(new java.awt.Dimension(148, 26));
        jPanel_ToggleSelections.add(jToggleButton_ShoesColor, new java.awt.GridBagConstraints());

        jToggleButton_HairColor.setText("Hair Color");
        jToggleButton_HairColor.setMinimumSize(new java.awt.Dimension(148, 26));
        jToggleButton_HairColor.setPreferredSize(new java.awt.Dimension(148, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_ToggleSelections.add(jToggleButton_HairColor, gridBagConstraints);

        jToggleButton_FacialHairColor.setText("Facial Hair Color");
        jToggleButton_FacialHairColor.setMinimumSize(new java.awt.Dimension(148, 26));
        jToggleButton_FacialHairColor.setPreferredSize(new java.awt.Dimension(148, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel_ToggleSelections.add(jToggleButton_FacialHairColor, gridBagConstraints);

        jToggleButton_SkinTone.setText("Skin Tone");
        jToggleButton_SkinTone.setMinimumSize(new java.awt.Dimension(148, 26));
        jToggleButton_SkinTone.setPreferredSize(new java.awt.Dimension(148, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel_ToggleSelections.add(jToggleButton_SkinTone, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel_ToggleSelections, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame_ColorSelector().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JColorChooser jColorChooser_Colors;
    private javax.swing.JPanel jPanel_ToggleSelections;
    private javax.swing.JToggleButton jToggleButton_FacialHairColor;
    private javax.swing.JToggleButton jToggleButton_HairColor;
    private javax.swing.JToggleButton jToggleButton_PantsColor;
    private javax.swing.JToggleButton jToggleButton_ShirtColor;
    private javax.swing.JToggleButton jToggleButton_ShoesColor;
    private javax.swing.JToggleButton jToggleButton_SkinTone;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////
    /**
     * Changelistener for the colorselector.  If the color value changes in the
     * selector window then the selected meshes (toggle buttons) selected will
     * add a color modulation effect to the mesh.
     * @param e
     */
    public void stateChanged(ChangeEvent e) {
        ButtonModel hairColor       = jToggleButton_HairColor.getModel();
        ButtonModel facialHairColor = jToggleButton_FacialHairColor.getModel();
        ButtonModel skinColor       = jToggleButton_SkinTone.getModel();
        ButtonModel shirtColor      = jToggleButton_ShirtColor.getModel();
        ButtonModel pantsColor      = jToggleButton_PantsColor.getModel();
        ButtonModel shoesColor      = jToggleButton_ShoesColor.getModel();

        if (hairColor.isSelected())
            toggleHairColor();
        if (facialHairColor.isSelected())
            toggleFacialHairColor();
        if (skinColor.isSelected())
            toggleSkinColor();
        if (shirtColor.isSelected())
            toggleShirtColor();
        if (pantsColor.isSelected())
            togglePantsColor();
        if (shoesColor.isSelected())
            toggleShoesColor();
    }
}
