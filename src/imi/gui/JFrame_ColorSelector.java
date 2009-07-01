 /**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
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
import imi.character.Character;
import imi.character.Manipulator;
import javax.swing.ButtonModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class JFrame_ColorSelector extends javax.swing.JFrame implements ChangeListener{
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private WorldManager    worldManager    = null;
    private Character       character       = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initualizes the gui components.  If default constructor
     * is the construction choice, remember to set the sceneData before continueing
     * with anything else.
     */
    private JFrame_ColorSelector(Builder builder) {
        this.worldManager   = builder.worldManager;
        this.character      = builder.character;
        
        initComponents();
        this.setTitle(builder.windowTitle);
        jToggleButton_FacialHairColor.setEnabled(false);
    }

    public static class Builder {
        private WorldManager    worldManager    = null;
        private Character       character       = null;
        private String          windowTitle     = "Color Selector";

        public Builder(WorldManager worldManager) {
            this.worldManager   = worldManager;
        }

        public Builder character(Character character) {
            this.character  = character;
            return this;
        }

        public Builder windowTitle(String windowTitle) {
            this.windowTitle    = windowTitle;
            return this;
        }

        public JFrame_ColorSelector build() {
            return new JFrame_ColorSelector(this);
        }
    }

    private void avatarCheck() {
        if (character == null) {
            throw new IllegalArgumentException("SEVERE ERROR: character is null");
        }
        if (character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: character has no SkeletonNode");
        }
        if (!character.isInitialized()) {
            throw new IllegalArgumentException("SEVERE ERROR: character has not been initialized");
        }
    }

    /**
     * Toggle's the hair color for the avatar that is loaded.  If there is no avatar
     * or no hair is found, then it will return and do nothing.
     */
    public void toggleHairColor() {
        avatarCheck();
        Manipulator.setHairColor(character, jColorChooser_Colors.getColor());
    }

    /**
     * Toggle's the facial hair color for the avatar that is loaded.  If there is no
     * avatar or no facial hair is found, then it will return and do nothing
     */
    public void toggleFacialHairColor() {
        avatarCheck();
        Manipulator.setFacialHairColor(character, jColorChooser_Colors.getColor());
    }

    /**
     * Toggle's the skin color for the avatar that is loaded.  If there is no avatar
     * loaded, then the method will return and do nothing
     */
    public void toggleSkinColor() {
        avatarCheck();
        Manipulator.setSkinTone(character, jColorChooser_Colors.getColor());
    }

    /**
     * Toggles the shirt/blouse color for the avatar that is loaded.  If there is
     * no avatar or shirt loaded, the method will return and do nothing;
     */
    public void toggleShirtColor() {
        avatarCheck();
        Manipulator.setShirtColor(character, jColorChooser_Colors.getColor());
    }

    /**
     * Toggles the pants color for the avtar that is loaded.  If there is no avatar
     * or shirt loaded, then the method will return and do nothing;
     */
    public void togglePantsColor() {
        avatarCheck();
        Manipulator.setPantsColor(character, jColorChooser_Colors.getColor());
    }

    /**
     * Toggles the shoes color for the avatar that is loaded.  If there is no avatar
     * or no footwear loaded, then the method will return and do nothing;
     */
    public void toggleShoesColor() {
        avatarCheck();
        Manipulator.setShoesColor(character, jColorChooser_Colors.getColor());
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

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
