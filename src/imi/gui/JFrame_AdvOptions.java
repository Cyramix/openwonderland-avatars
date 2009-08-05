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
 * JFrame_AdvOptions.java
 *
 * Created on Dec 17, 2008, 11:37:01 AM
 */

package imi.gui;

import com.jme.math.Vector3f;
import imi.character.Character;
import imi.character.EyeBall;
import imi.character.Manipulator;
import imi.scene.PNode;
import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JFrame_AdvOptions extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private WorldManager                                    worldManager;
    private Character                                       character;
    private Map<GUI_Enums.bodyPart, SkinnedMeshJoint[]>   skeleton;
    private boolean                                         loaeEyePane;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    /**
     * Private constructor that the builder calls on to make the advanceOptions
     * tool window.
     * @param builder   - public builder that sets all the parmaters needed for
     *                    creating the advance options.
     */
    private JFrame_AdvOptions(Builder builder) {
        this.worldManager   = builder.worldManager;
        this.loaeEyePane    = builder.loadEyePane;
        this.character      = builder.character;

        initComponents();
        this.setTitle(builder.windowTitle);

        EyeBall[] eyes = getEyeBallMeshes();
        HeadOptions.setParentFrame(this);
        HeadOptions.setEyeMeshInstances(eyes);
        HeadOptions.setWorldManager(builder.worldManager);
    }

    public static class Builder {
        private String          windowTitle     = "Avatar Joint Manipulator";
        private Character       character       = null;
        private WorldManager    worldManager    = null;
        private boolean         loadEyePane     = false;

        public Builder(WorldManager worldManager) {
            this.worldManager   = worldManager;
        }

        public Builder character(Character character) {
            this.character  = character;
            return this;
        }

        public Builder loadEyePanel(boolean loadEyePane) {
            this.loadEyePane    = loadEyePane;
            return this;
        }

        public Builder windowTitle(String windowTitle) {
            this.windowTitle    = windowTitle;
            return this;
        }

        public JFrame_AdvOptions build() {
            return new JFrame_AdvOptions(this);
        }
    }

    public void avatarCheck() {
        if (this.character == null || this.character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: no character/avatar has been loaded");
        }
    }

    /**
     * Adjusts the joints catagorized under eyes.  Adjusts location in 3D space
     * and scaling.
     * @param type - integer represents the type of eye manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustEyes(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case lefteyeHPos:
            {
                Vector3f displacement   = new Vector3f(mod * -0.05f, 0.0f, 0.0f);
                Manipulator.adjustLeftEyePosition(character, displacement);
                break;
            }
            case righteyeHPos:
            {
                Vector3f displacement   = new Vector3f(mod * -0.05f, 0.0f, 0.0f);
                Manipulator.adjustRightEyePosition(character, displacement);
                break;
            }
            case lefteyeVPos:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod * 0.05f, 0.0f);
                Manipulator.adjustLeftEyePosition(character, displacement);
                break;
            }
            case righteyeVPos:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod * 0.05f, 0.0f);
                Manipulator.adjustRightEyePosition(character, displacement);
                break;
            }
            case lefteyeSize:
            {
                Vector3f scale   = new Vector3f(mod * 3.0f, mod * 3.0f, mod * 3.0f);
                Manipulator.adjustLeftEyeScale(character, scale);
                break;
            }
            case righteyeSize:
            {
                Vector3f scale   = new Vector3f(mod * 3.0f, mod * 3.0f, mod * 3.0f);
                Manipulator.adjustRightEyeScale(character, scale);
                break;
            }
            case lefteyeWidth:
            {
                Vector3f scale   = new Vector3f(mod * 3.0f, 0.0f, 0.0f);
                Manipulator.adjustLeftEyeScale(character, scale);
                break;
            }
            case righteyeWidth:
            {
                Vector3f scale   = new Vector3f(mod * 3.0f, 0.0f, 0.0f);
                Manipulator.adjustRightEyeScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under hands.  Adjusts location in 3D space
     * and scaling.
     * @param type integer represnts the type of hand manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustHands(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case lefthandLength:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustLeftHandLength(character, displacement);
                break;
            }
            case righthandLength:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustRightHandLength(character, displacement);
                break;
            }
            case lefthandThickness:
            {
                Vector3f scale   = new Vector3f(actualval * 3.0f, actualval * 3.0f, actualval * 3.0f);
                Manipulator.adjustLeftHandScale(character, scale);
                break;
            }
            case righthandThickness:
            {
                Vector3f scale   = new Vector3f(actualval * 3.0f, actualval * 3.0f, actualval * 3.0f);
                Manipulator.adjustRightHandScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under forearms.  Adjusts the location in 3D
     * space and scaling
     * @param type - integer represents the type of forearm manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustForearms(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case leftlowerarmLength:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustLeftForearmLength(character, displacement);
                break;
            }
            case rightlowerarmLength:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustRightForearmLength(character, displacement);
                break;
            }
            case leftlowerarmThickness:
            {
                Vector3f scale  = new Vector3f(actualval *3.0f, 0.0f, actualval *3.0f);
                Manipulator.adjustLeftForearmScale(character, scale);
                break;
            }
            case rightlowerarmThickness:
            {
                Vector3f scale  = new Vector3f(actualval *3.0f, 0.0f, actualval *3.0f);
                Manipulator.adjustRightForearmScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under upperarms.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of upperarm manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustUpperarms(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case leftupperarmLength:
            {
                if (actualval <= 0.05f) {
                    Vector3f displacement   = new Vector3f(0.0f, 0.0f, mod * 4.0f);
                    Manipulator.adjustLeftUpperarmLength(character, displacement);
                }
                break;
            }
            case rightupperarmLength:
            {
                if (actualval <= 0.05f) {
                    Vector3f displacement   = new Vector3f(0.0f, 0.0f, mod * 4.0f);
                    Manipulator.adjustRightUpperarmLength(character, displacement);
                }
                break;
            }
            case leftupperarmThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, 0.0f, actualval * 3.0f);
                Manipulator.adjustLeftUpperarmScale(character, scale);
                break;
            }
            case rightupperarmThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, 0.0f, actualval * 3.0f);
                Manipulator.adjustRightUpperarmScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under feet.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of feet manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustFeet(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case leftfootLength:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod, mod);
                Manipulator.adjustLeftFootLength(character, displacement);
                break;
            }
            case rightfootLength:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod, mod);
                Manipulator.adjustRightFootLength(character, displacement);
                break;
            }
            case leftfootThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, actualval * 3.0f, actualval * 3.0f);
                Manipulator.adjustLeftFootScale(character, scale);
                break;
            }
            case rightfootThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, actualval * 3.0f, actualval * 3.0f);
                Manipulator.adjustRightFootScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under calves.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of calves manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustCalves(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case leftlowerlegLength:
            {
                Vector3f displacement  = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustLeftCalfScale(character, displacement);
                break;
            }
            case rightlowerlegLength:
            {
                Vector3f displacement  = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustRightCalfScale(character, displacement);
                break;
            }
            case leftlowerlegThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, 0.0f, actualval * 3.0f);
                Manipulator.adjustLeftCalfScale(character, scale);
                break;
            }
            case rightlowerlegThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, 0.0f, actualval * 3.0f);
                Manipulator.adjustRightCalfScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under thighs.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of thighs manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustThighs(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case leftupperlegLength:
            {
                Vector3f displacement  = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustLeftThighLength(character, displacement);
                break;
            }
            case rightupperlegLength:
            {
                Vector3f displacement  = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustRightThighLength(character, displacement);
                break;
            }
            case leftupperlegThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, 0.0f, actualval * 3.0f);
                Manipulator.adjustLeftThighScale(character, scale);
                break;
            }
            case rightupperlegThickness:
            {
                Vector3f scale  = new Vector3f(actualval * 3.0f, 0.0f, actualval * 3.0f);
                Manipulator.adjustRightThighScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under chest.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of chest manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustChest(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case torsoLength:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod, 0.0f);
                Manipulator.adjustTorsoLength(character, displacement);
                break;
            }
            case torsoThickness:
            {
                Vector3f scale  = new Vector3f(mod * 2.0f, 0.0f, mod * 2.0f);
                Manipulator.adjustTorsoScale(character, scale);
                break;
            }
            case shoulderBroadness:
            {
                Vector3f displacement   = new Vector3f(mod, 0.0f, 0.0f);
                Manipulator.adjustShoulderBroadness(character, displacement);
                break;
            }
            case stomachRoundness:
            {
                Vector3f scale  = new Vector3f(mod, mod, mod);
                Manipulator.adjustStomacheRoundness(character, scale);
                break;
            }
            case glutRoundness:
            {
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized as part of the head.  Adjusts the location
     * in 3D space and scaling.
     * @param type - integer represents the type of the chest manipulation
     * @param mod - delta modification of the seleted slider/scrollbox
     * @param actualval - the actual value of the modification from the selected slider/scrollbox
     */
    private synchronized void adjustHead(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case headHeight:
            {
                Vector3f scale  = new Vector3f(0.0f, mod * 3.0f, 0.0f);
                Manipulator.adjustHeadScale(character, scale);
                break;
            }
            case headWidth:
            {
                Vector3f scale  = new Vector3f(mod * 3.0f, 0.0f, 0.0f);
                Manipulator.adjustHeadScale(character, scale);
                break;
            }
            case headDepth:
            {
                Vector3f scale  = new Vector3f(0.0f, 0.0f, mod * 3.0f);
                Manipulator.adjustHeadScale(character, scale);
                break;
            }
            case headUniform:
            {
                Vector3f scale  = new Vector3f(mod * 3.0f, mod * 3.0f, mod * 3.0f);
                Manipulator.adjustHeadScale(character, scale);
                break;
            }
            case leftearHPos:
            {
                Vector3f displacement  = new Vector3f(mod * 0.03f, 0.0f, 0.0f);
                Manipulator.adjustLeftEarPosition(character, displacement);
                break;
            }
            case leftearSize:
            {
                Vector3f scale  = new Vector3f(mod * 4.0f, mod * 4.0f, mod * 4.0f);
                Manipulator.adjustLeftEarScale(character, scale);
                break;
            }
            case leftearVPos:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod * 0.05f, 0.0f);
                Manipulator.adjustLeftEarPosition(character, displacement);
                break;
            }
            case noseHPos:
            {
                Vector3f displacement   = new Vector3f(0.0f, 0.0f, mod * 0.03f);
                Manipulator.adjustNosePosition(character, displacement);
                break;
            }
            case noseLength:
            {
                Vector3f scale   = new Vector3f(0.0f, mod * 3.0f, 0.0f);
                Manipulator.adjustNoseScale(character, scale);
                break;
            }
            case noseSize:
            {
                Vector3f scale   = new Vector3f(mod * 3.0f, mod * 3.0f, mod * 3.0f);
                Manipulator.adjustNoseScale(character, scale);
                break;
            }
            case noseVPos:
            {
                Vector3f displacement   = new Vector3f(-mod * 0.08f, 0.0f, 0.0f);
                Manipulator.adjustNosePosition(character, displacement);
                break;
            }
            case rightearHPos:
            {
                Vector3f displacement   = new Vector3f(-mod * 0.03f, 0.0f, 0.0f);
                Manipulator.adjustRightEarPosition(character, displacement);
                break;
            }
            case rightearSize:
            {
                Vector3f scale   = new Vector3f(mod * 4.0f, mod * 4.0f, mod * 4.0f);
                Manipulator.adjustRightEarPosition(character, scale);
                break;
            }
            case rightearVPos:
            {
                Vector3f displacement   = new Vector3f(0.0f, mod * 0.05f, 0.0f);
                Manipulator.adjustRightEarPosition(character, displacement);
                break;
            }
        }
    }

    /**
     * Adjusts the joints catagorized under body.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of body manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustBody(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case uniformHeight:
            {
                Vector3f scale  = new Vector3f(0.0f, mod * 3.0f, 0.0f);
                Manipulator.adjustBodyScale(character, scale);
                break;
            }
            case uniformThickness:
            {
                Vector3f scale  = new Vector3f(mod * 3.0f, 0.0f, mod * 3.0f);
                Manipulator.adjustBodyScale(character, scale);
                break;
            }
        }
    }

    /**
     * Adjust the joints catagorizede as mouth joints.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of body manipulaation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustMouth(GUI_Enums.sliderControl type, float mod, float actualval) {
        avatarCheck();
        switch(type)
        {
            case lowerlipSize:
            {
                Vector3f scale  = new Vector3f(mod * 4.0f, 0.0f, mod * 4.0f);
                Manipulator.adjustLowerLipScale(character, scale);
                break;
            }
            case upperlipSize:
            {
                Vector3f scale  = new Vector3f(mod * 4.0f, 0.0f, mod * 4.0f);
                Manipulator.adjustUpperLipScale(character, scale);
                break;
            }
            case mouthWidth:
            {
                Vector3f displacement   = new Vector3f(-mod * 0.04f, 0.0f, 0.0f);
                Manipulator.adjustLipsPosition(character, displacement, true);
                break;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane_Options = new javax.swing.JTabbedPane();
        HeadOptions = new imi.gui.JPanel_HeadOptions(this, loaeEyePane);
        ArmsHandsOptions = new imi.gui.JPanel_ArmsHandsOption(this);
        LegsFeetOptions = new imi.gui.JPanel_LegsFeetOption(this);
        SimpleBodyOptions = new imi.gui.JPanel_SimpBodyOptions(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jTabbedPane_Options.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane_Options.setMinimumSize(new java.awt.Dimension(300, 650));
        jTabbedPane_Options.setPreferredSize(new java.awt.Dimension(300, 650));
        jTabbedPane_Options.addTab("Head", HeadOptions);
        jTabbedPane_Options.addTab("Arms/Hands", ArmsHandsOptions);
        jTabbedPane_Options.addTab("Legs/Feet", LegsFeetOptions);
        jTabbedPane_Options.addTab("Body", SimpleBodyOptions);

        getContentPane().add(jTabbedPane_Options, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.JPanel_ArmsHandsOption ArmsHandsOptions;
    private imi.gui.JPanel_HeadOptions HeadOptions;
    private imi.gui.JPanel_LegsFeetOption LegsFeetOptions;
    private imi.gui.JPanel_SimpBodyOptions SimpleBodyOptions;
    private javax.swing.JTabbedPane jTabbedPane_Options;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the references to the eyeball meshes for quick access
     * @return array of PPolygonSkinnedMeshInstances of the two eyeballs
     */
    public EyeBall[] getEyeBallMeshes() {
        avatarCheck();

        PNode lefteye = null;   PNode righteye = null;
        EyeBall[] eyeballs    = new EyeBall[2];
        lefteye     = character.getModelInst().findChild("leftEyeGeoShape");
        eyeballs[0] = (EyeBall)lefteye;
        righteye    = character.getModelInst().findChild("rightEyeGeoShape");
        eyeballs[1] = (EyeBall)righteye;

        return eyeballs;
    }
    /**
     * Switchboard for all the joint sliders.  Pushes the correct response to the
     * right methods.
     * @param control - which slider control of effect
     * @param mod - delta modification value from the slider
     * @param actualval - actual modification value from the slider
     */
    public void parseModification(GUI_Enums.sliderControl control, float mod, float actualval) {
        switch(control)
        {
            case lefteyeHPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lefteyeSize:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lefteyeVPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lefteyeWidth:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeHPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeSize:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeVPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeWidth:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lowerlipSize:
            {
                adjustMouth(control, mod, actualval);
                break;
            }
            case upperlipSize:
            {
                adjustMouth(control, mod, actualval);
                break;
            }
            case mouthWidth:
            {
                adjustMouth(control, mod, actualval);
                break;
            }
            case lefthandLength:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case lefthandThickness:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case leftlowerarmLength:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case leftlowerarmThickness:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case leftupperarmLength:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case leftupperarmThickness:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case righthandLength:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case righthandThickness:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case rightlowerarmLength:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case rightlowerarmThickness:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case rightupperarmLength:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case rightupperarmThickness:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case leftfootLength:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case leftfootThickness:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case leftlowerlegLength:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case leftlowerlegThickness:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case leftupperlegLength:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case leftupperlegThickness:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case rightfootLength:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case rightfootThickness:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case rightlowerlegLength:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case rightlowerlegThickness:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case rightupperlegLength:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case rightupperlegThickness:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case headDepth:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case headHeight:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case headWidth:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case headUniform:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case torsoLength:
            {
                adjustChest(control, mod, actualval);
                break;
            }
            case torsoThickness:
            {
                adjustChest(control, mod, actualval);
                break;
            }
            case shoulderBroadness:
            {
                adjustChest(control, mod, actualval);
                break;
            }
            case stomachRoundness:
            {
                adjustChest(control, mod, actualval);
                break;
            }
            case glutRoundness:
            {
                adjustChest(control, mod, actualval);
                break;
            }
            case leftearHPos:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case leftearSize:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case leftearVPos:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case noseHPos:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case noseLength:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case noseSize:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case noseVPos:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case rightearHPos:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case rightearSize:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case rightearVPos:
            {
                adjustHead(control, mod, actualval);
                break;
            }
            case uniformHeight:
            {
                adjustBody(control, mod, actualval);
                break;
            }
            case uniformThickness:
            {
                adjustBody(control, mod, actualval);
                break;
            }
        }
    }
}
