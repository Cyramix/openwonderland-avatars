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
import imi.scene.PNode;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JFrame_AdvOptions extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private Map<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>   m_skeleton;
    private SceneEssentials                                 m_sceneData;
    private NumberFormat                                    m_format;
    private boolean                                         m_bEyeTextures;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor to create an AdvOptions window.  Using this method
     * requires manually setting the SceneEssentials using the setSceneData() method
     * as well as calling the public function createJointCatalog() to setup joint
     * informaiton
     */
    public JFrame_AdvOptions() {
        initComponents();
        
        HeadOptions.setParentFrame(this);
        PPolygonSkinnedMeshInstance[] eyes = getEyeBallMeshes();
        HeadOptions.setEyeMeshInstances(eyes);
        HeadOptions.setWorldManager(m_sceneData.getWM());
    }

    /**
     * Overloaded constructor to create an AdvOptions window.  It sets the scene
     * information as well as the information of the joints from the currently
     * loaded avatar
     * @param scene - reference to the main window's scene information
     */
    public JFrame_AdvOptions(SceneEssentials scene, boolean displayEyeTextures) {
        m_sceneData = scene;
        m_bEyeTextures = displayEyeTextures;
        
        initComponents();

        HeadOptions.setParentFrame(this);
        PPolygonSkinnedMeshInstance[] eyes = getEyeBallMeshes();
        HeadOptions.setEyeMeshInstances(eyes);
        HeadOptions.setWorldManager(m_sceneData.getWM());

        createJointCatalog();
    }

    /**
     * Adjusts the joints catagorized under eyes.  Adjusts location in 3D space
     * and scaling.
     * @param type - integer represents the type of eye manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    private synchronized void adjustEyes(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode        skelnode        = m_sceneData.getAvatar().getSkeleton();
        String              formattedNumber = null;
        if (skelnode == null) { return; }

        // [0] = Left eye joints && [6] = Right eye joints
        SkinnedMeshJoint[]  eyes            = m_skeleton.get(GUI_Enums.m_bodyPart.Eyes);
        Vector3f            ladjust         = new Vector3f();
        Vector3f            radjust         = new Vector3f();
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case lefteyeHPos:
            {
                ladjust.x = mod * -0.05f;   ladjust.y = 0.0f;    ladjust.z = 0.0f;
                skelnode.displaceJoint(eyes[0].getName(), ladjust);

                break;
            }
            case righteyeHPos:
            {
                radjust.x = mod * -0.05f;   radjust.y = 0.0f;    ladjust.z = 0.0f;
                skelnode.displaceJoint(eyes[6].getName(), radjust);

                break;
            }
            case lefteyeVPos:
            {
                ladjust.x = 0.0f;   ladjust.y = mod * 0.01f;    ladjust.z = 0.0f;
                skelnode.displaceJoint(eyes[0].getName(), ladjust);

                break;
            }
            case righteyeVPos:
            {
                radjust.x = 0.0f;   radjust.y = mod * 0.01f;    ladjust.z = 0.0f;
                skelnode.displaceJoint(eyes[6].getName(), radjust);

                break;
            }
            case lefteyeSize:
            {
                scale = eyes[0].getBindPose().getScaleVector();
                scale.x += mod * 3; scale.y += mod * 3; scale.z += mod * 3;
                eyes[0].getBindPose().setScale(scale);

                break;
            }
            case righteyeSize:
            {
                scale = eyes[6].getBindPose().getScaleVector();
                scale.x += mod * 3; scale.y += mod * 3; scale.z += mod * 3;
                eyes[6].getBindPose().setScale(scale);

                break;
            }
            case lefteyeWidth:
            {
                scale = eyes[0].getBindPose().getScaleVector();
                scale.x += mod * 3;
                eyes[0].getBindPose().setScale(scale);

                break;
            }
            case righteyeWidth:
            {
                scale = eyes[6].getBindPose().getScaleVector();
                scale.x += mod * 3;
                eyes[6].getBindPose().setScale(scale);

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
    private synchronized void adjustHands(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skelnode           = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[] lefthand     = m_skeleton.get(GUI_Enums.m_bodyPart.Left_Hand);
        SkinnedMeshJoint[] righthand    = m_skeleton.get(GUI_Enums.m_bodyPart.Right_Hand);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);

        switch(type)
        {
            case lefthandLength:
            {
                skelnode.displaceJoint(lefthand[0].getName(), ladjust);
                break;
            }
            case righthandLength:
            {
                skelnode.displaceJoint(righthand[0].getName(), radjust);
                break;
            }
            case lefthandThickness:
            {
                lefthand[0].getBindPose().setScale(1.0f + (actualval * 3));
                break;
            }
            case righthandThickness:
            {
                righthand[0].getBindPose().setScale(1.0f + (actualval * 3));
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
    private synchronized void adjustForearms(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        String          formattedNumber = null;
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftforearm     = m_skeleton.get(GUI_Enums.m_bodyPart.Left_LowerArm);
        SkinnedMeshJoint[]  rightforearm    = m_skeleton.get(GUI_Enums.m_bodyPart.Right_LowerArm);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case leftlowerarmLength:
            {
                skelnode.displaceJoint(leftforearm[1].getName(), ladjust);

                if (actualval <= 0.05f) {
                    for (int i = 0; i < leftforearm.length; i++) {
                        float y = scale.y += actualval * 3;
                        formattedNumber = m_format.format(y);
                        scale.y = Float.valueOf(formattedNumber);
                        leftforearm[i].getLocalModifierMatrix().setScale(scale);
                    }
                }

                break;
            }
            case rightlowerarmLength:
            {
                skelnode.displaceJoint(rightforearm[1].getName(), radjust);

                if (actualval <= 0.05f) {
                    for (int i = 0; i < rightforearm.length; i++) {
                        float y = scale.y += actualval * 3;
                        formattedNumber = m_format.format(y);
                        scale.y = Float.valueOf(formattedNumber);
                        rightforearm[i].getLocalModifierMatrix().setScale(scale);
                    }
                }

                break;
            }
            case leftlowerarmThickness:
            {
                for (int i = 0; i < leftforearm.length; i++) {
                    ladjust = new Vector3f(leftforearm[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += actualval * 3;
                    ladjust.z = 1.0f;   ladjust.z += actualval * 3;
                    leftforearm[i].getLocalModifierMatrix().setScale(ladjust);
                }
//                leftforearm[0].getBindPose().setScale(1.0f + (actualval * 3));
                break;
            }
            case rightlowerarmThickness:
            {
                for (int i = 0; i < rightforearm.length; i++) {
                    radjust = new Vector3f(rightforearm[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += actualval * 3;
                    radjust.z = 1.0f;   radjust.z += actualval * 3;
                    rightforearm[i].getLocalModifierMatrix().setScale(radjust);
                }
//                rightforearm[0].getBindPose().setScale(1.0f + (actualval * 3));
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
    private synchronized void adjustUpperarms(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode        = m_sceneData.getAvatar().getSkeleton();
        String          formattedNumber = null;
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftupperarm    = m_skeleton.get(GUI_Enums.m_bodyPart.Left_UpperArm);
        SkinnedMeshJoint[]  rightupperarm   = m_skeleton.get(GUI_Enums.m_bodyPart.Right_UpperArm);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case leftupperarmLength:
            {
                skelnode.displaceJoint(leftupperarm[1].getName(), ladjust);

                if (actualval <= 0.05f) {
                    for (int i = 0; i < leftupperarm.length; i++) {
                        float y = scale.y += actualval * 4;
                        formattedNumber = m_format.format(y);
                        scale.y = Float.valueOf(formattedNumber);
                        leftupperarm[i].getLocalModifierMatrix().setScale(scale);
                    }
                }

                break;
            }
            case rightupperarmLength:
            {
                skelnode.displaceJoint(rightupperarm[1].getName(), radjust);

                if (actualval <= 0.05f) {
                    for (int i = 0; i < rightupperarm.length; i++) {
                        float y = scale.y += actualval * 4;
                        formattedNumber = m_format.format(y);
                        scale.y = Float.valueOf(formattedNumber);
                        rightupperarm[i].getLocalModifierMatrix().setScale(scale);
                    }
                }

                break;
            }
            case leftupperarmThickness:
            {
                for (int i = 0; i < leftupperarm.length; i++) {
                    ladjust = new Vector3f(leftupperarm[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += actualval * 3;
                    ladjust.z = 1.0f;   ladjust.z += actualval * 3;
                    leftupperarm[i].getLocalModifierMatrix().setScale(ladjust);
                }
//                leftupperarm[0].getBindPose().setScale(1.0f + (actualval * 3));
                break;
            }
            case rightupperarmThickness:
            {
                for (int i = 0; i < rightupperarm.length; i++) {
                    radjust = new Vector3f(rightupperarm[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += actualval * 3;
                    radjust.z = 1.0f;   radjust.z += actualval * 3;
                    rightupperarm[i].getLocalModifierMatrix().setScale(radjust);
                }
//                rightupperarm[0].getBindPose().setScale(1.0f + (actualval * 3));
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
    private synchronized void adjustFeet(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftfoot    = m_skeleton.get(GUI_Enums.m_bodyPart.Left_Foot);
        SkinnedMeshJoint[]  rightfoot   = m_skeleton.get(GUI_Enums.m_bodyPart.Right_Foot);
        Vector3f            ladjust     = new Vector3f(0.0f, mod, mod);
        Vector3f            radjust     = new Vector3f(0.0f, mod, mod);

        switch(type)
        {
            case leftfootLength:
            {
                skelnode.displaceJoint(leftfoot[1].getName(), ladjust);
                break;
            }
            case rightfootLength:
            {
                skelnode.displaceJoint(rightfoot[1].getName(), radjust);
                break;
            }
            case leftfootThickness:
            {
                leftfoot[0].getBindPose().setScale(1.0f + (actualval * 3));
                break;
            }
            case rightfootThickness:
            {
                rightfoot[0].getBindPose().setScale(1.0f + (actualval * 3));
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
    private synchronized void adjustCalves(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        String          formattedNumber = null;
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftlowerleg    = m_skeleton.get(GUI_Enums.m_bodyPart.Left_LowerLeg);
        SkinnedMeshJoint[]  rightlowerleg   = m_skeleton.get(GUI_Enums.m_bodyPart.Right_LowerLeg);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case leftlowerlegLength:
            {
                skelnode.displaceJoint(leftlowerleg[1].getName(), ladjust);

                if (actualval <= 0.05f) {
                    for (int i = 0; i < leftlowerleg.length; i++) {
                        float y = scale.y += actualval * 2;
                        formattedNumber = m_format.format(y);
                        scale.y = Float.valueOf(formattedNumber);
                        leftlowerleg[i].getLocalModifierMatrix().setScale(scale);
                    }
                }

                break;
            }
            case rightlowerlegLength:
            {
                skelnode.displaceJoint(rightlowerleg[1].getName(), radjust);

                if (actualval <= 0.05f) {
                    for (int i = 0; i < rightlowerleg.length; i++) {
                        float y = scale.y += actualval * 2;
                        formattedNumber = m_format.format(y);
                        scale.y = Float.valueOf(formattedNumber);
                        rightlowerleg[i].getLocalModifierMatrix().setScale(scale);
                    }
                }

                break;
            }
            case leftlowerlegThickness:
            {
                for (int i = 0; i < leftlowerleg.length; i++) {
                    ladjust = new Vector3f(leftlowerleg[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += actualval * 3;
                    ladjust.z = 1.0f;   ladjust.z += actualval * 3;
                    leftlowerleg[i].getLocalModifierMatrix().setScale(ladjust);
                }
//                leftlowerleg[0].getBindPose().setScale(1.0f + (actualval * 3));
                break;
            }
            case rightlowerlegThickness:
            {
                for (int i = 0; i < rightlowerleg.length; i++) {
                    radjust = new Vector3f(rightlowerleg[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += actualval * 3;
                    radjust.z = 1.0f;   radjust.z += actualval * 3;
                    rightlowerleg[i].getLocalModifierMatrix().setScale(radjust);
                }
//                rightlowerleg[0].getBindPose().setScale(1.0f + (actualval * 3));
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
    private synchronized void adjustThighs(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftupperleg    = m_skeleton.get(GUI_Enums.m_bodyPart.Left_UpperLeg);
        SkinnedMeshJoint[]  rightupperleg   = m_skeleton.get(GUI_Enums.m_bodyPart.Right_UpperLeg);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case leftupperlegLength:
            {
                skelnode.displaceJoint(leftupperleg[0].getName(), ladjust);
                break;
            }
            case rightupperlegLength:
            {
                skelnode.displaceJoint(rightupperleg[0].getName(), radjust);
                break;
            }
            case leftupperlegThickness:
            {
                for (int i = 0; i < leftupperleg.length; i++) {
                    ladjust = new Vector3f(leftupperleg[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += actualval * 3;
                    ladjust.z = 1.0f;   ladjust.z += actualval * 3;
                    leftupperleg[i].getLocalModifierMatrix().setScale(ladjust);
                }
//                leftupperleg[0].getBindPose().setScale(1.0f + (actualval * 3));
                break;
            }
            case rightupperlegThickness:
            {
                for (int i = 0; i < rightupperleg.length; i++) {
                    radjust = new Vector3f(rightupperleg[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += actualval * 3;
                    radjust.z = 1.0f;   radjust.z += actualval * 3;
                    rightupperleg[i].getLocalModifierMatrix().setScale(radjust);
                }
//                rightupperleg[0].getBindPose().setScale(1.0f + (actualval * 3));
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
    private synchronized void adjustChest(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  shoulders    = new SkinnedMeshJoint[] { m_skeleton.get(GUI_Enums.m_bodyPart.Left_Shoulder)[0],
                                                                    m_skeleton.get(GUI_Enums.m_bodyPart.Right_Shoulder)[0] };
        SkinnedMeshJoint[]  chest        = m_skeleton.get(GUI_Enums.m_bodyPart.Torso);
        SkinnedMeshJoint    hip          = (SkinnedMeshJoint) skelnode.findChild("Hips");
        Vector3f            adjust       = new Vector3f(mod, 0.0f, 0.0f);
        m_format                         = new DecimalFormat("0.00");

        switch(type)
        {
            case torsoLength:
            {
                Vector3f upAdjust = new Vector3f(0.0f, mod, 0.0f);
                skelnode.displaceJoint(chest[1].getName(), upAdjust);
                break;
            }
            case torsoThickness:
            {
//                Vector3f scale1 = chest[1].getLocalModifierMatrix().getScaleVector();
//                scale1.x += mod *3;  scale1.z += mod *3;
//                chest[1].getLocalModifierMatrix().setScale(scale1);
//                Vector3f scale2 = chest[2].getLocalModifierMatrix().getScaleVector();
//                scale2.x += mod *3;  scale2.z += mod *3;
//                chest[2].getLocalModifierMatrix().setScale(scale2);
                Vector3f scale = chest[1].getBindPose().getScaleVector();
                scale.x += mod *2;  scale.z += mod *2;
                chest[1].getBindPose().setScale(scale);
                break;
            }
            case shoulderBroadness:
            {
                skelnode.displaceJoint(shoulders[0].getName(), adjust);
                skelnode.displaceJoint(shoulders[1].getName(), adjust.mult(-1));
                break;
            }
            case stomachRoundness:
            {
                Vector3f translation = chest[0].getLocalModifierMatrix().getTranslation();
                translation.z += mod * 0.06f;
                Vector3f scale = chest[0].getLocalModifierMatrix().getScaleVector();
                scale.x += mod *4;  scale.y += mod *4;  scale.z += mod *4;
                chest[0].getLocalModifierMatrix().setTranslation(translation);
                chest[0].getLocalModifierMatrix().setScale(scale);
                Vector3f hiptrans = hip.getLocalModifierMatrix().getTranslation();
                hiptrans.z += mod * 0.06f;
                hip.getLocalModifierMatrix().setTranslation(hiptrans);
                hip.getLocalModifierMatrix().setScale(scale);
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
    private synchronized void adjustHead(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  head    = m_skeleton.get(GUI_Enums.m_bodyPart.Head);
        SkinnedMeshJoint[]  ears    = m_skeleton.get(GUI_Enums.m_bodyPart.Ears);
        SkinnedMeshJoint[]  nose    = m_skeleton.get(GUI_Enums.m_bodyPart.Nose);

        Vector3f            scale   = head[0].getBindPose().getScaleVector();
        m_format                    = new DecimalFormat("0.00");

        switch(type)
        {
            case headHeight:
            {
                scale.y += mod *3;
                head[0].getBindPose().setScale(scale);
                break;
            }
            case headWidth:
            {
                scale.x += mod * 3;
                head[0].getBindPose().setScale(scale);
                break;
            }
            case headDepth:
            {
                scale.z += mod * 3;
                head[0].getBindPose().setScale(scale);
                break;
            }
            case headUniform:
            {
                scale.x += mod *3;  scale.y += mod *3;  scale.z += mod *3;
                head[0].getBindPose().setScale(scale);
                break;
            }
            case leftearHPos:
            {
                Vector3f transX  = new Vector3f((mod * 0.03f), 0.0f, 0.0f);
                skelnode.displaceJoint(ears[0].getName(), transX);
                break;
            }
            case leftearSize:
            {
                scale = ears[0].getBindPose().getScaleVector();
                scale.x += mod *4;  scale.y += mod *4;  scale.z += mod *4;
                ears[0].getBindPose().setScale(scale);
                break;
            }
            case leftearVPos:
            {
                Vector3f transY  = new Vector3f(0.0f, (mod * 0.05f), 0.0f);
                skelnode.displaceJoint(ears[0].getName(), transY);
                break;
            }
            case noseHPos:
            {
                Vector3f transX  = new Vector3f(0.0f, 0.0f, (mod * 0.03f));
                skelnode.displaceJoint(nose[0].getName(), transX);
                break;
            }
            case noseLength:
            {
                scale = nose[0].getBindPose().getScaleVector();
                scale.y += mod * 3;
                nose[0].getBindPose().setScale(scale);
                break;
            }
            case noseSize:
            {
                scale = nose[0].getBindPose().getScaleVector();
                scale.x += mod *3;  scale.y += mod *3;  scale.z += mod *3;
                nose[0].getBindPose().setScale(scale);
                break;
            }
            case noseVPos:
            {
                Vector3f transY  = new Vector3f((-mod * 0.08f), 0.0f, 0.0f);
                skelnode.displaceJoint(nose[0].getName(), transY);
                break;
            }
            case rightearHPos:
            {
                Vector3f transX  = new Vector3f((-mod * 0.03f), 0.0f, 0.0f);
                skelnode.displaceJoint(ears[1].getName(), transX);
                break;
            }
            case rightearSize:
            {
                scale = ears[1].getBindPose().getScaleVector();
                scale.x += mod *4;  scale.y += mod *4;  scale.z += mod *4;
                ears[1].getBindPose().setScale(scale);
                break;
            }
            case rightearVPos:
            {
                Vector3f transY  = new Vector3f(0.0f, (mod * 0.05f), 0.0f);
                skelnode.displaceJoint(ears[1].getName(), transY);
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
    private synchronized void adjustBody(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        Vector3f scale  = ((SkinnedMeshJoint)skelnode.findChild("Hips")).getBindPose().getScaleVector();  //skelnode.getTransform().getLocalMatrix(true).getScaleVector();
        Vector3f move   = ((SkinnedMeshJoint)skelnode.findChild("Hips")).getBindPose().getTranslation();
        Vector3f scaleL = ((SkinnedMeshJoint)skelnode.findChild("leftShoulder")).getBindPose().getScaleVector();
        Vector3f scaleR = ((SkinnedMeshJoint)skelnode.findChild("rightShoulder")).getBindPose().getScaleVector();
        switch(type)
        {
            case uniformHeight:
            {
                scale.y += mod * 3;
                move.y  += mod * 3;
                scaleL.y += mod *3;
                scaleR.y += mod *3;
//                skelnode.getTransform().getLocalMatrix(true).setScale(scale);
                ((SkinnedMeshJoint)skelnode.findChild("leftShoulder")).getBindPose().setScale(scaleL);
                ((SkinnedMeshJoint)skelnode.findChild("rightShoulder")).getBindPose().setScale(scaleR);
                break;
            }
            case uniformThickness:
            {
                scale.x += mod * 3; scale.z += mod * 3;
                move.x  += mod * 3; move.z  += mod * 3;
                scaleL.x += mod *3; scaleL.z += mod *3;
                scaleR.x += mod *3; scaleR.z += mod *3;
//                skelnode.getTransform().getLocalMatrix(true).setScale(scale);
                ((SkinnedMeshJoint)skelnode.findChild("leftShoulder")).getBindPose().setScale(scaleL);
                ((SkinnedMeshJoint)skelnode.findChild("rightShoulder")).getBindPose().setScale(scaleR);
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
    private synchronized void adjustMouth(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  mouth       = m_skeleton.get(GUI_Enums.m_bodyPart.Lips);
        SkinnedMeshJoint    upperLip    = (SkinnedMeshJoint) skelnode.findChild("UpperLip_Adjust");
        SkinnedMeshJoint    lowerLip    = (SkinnedMeshJoint) skelnode.findChild("LowerLip_Adjust");

        Vector3f            ladjust     = new Vector3f(-mod * 0.04f, 0.0f, 0.0f);
        Vector3f            radjust     = new Vector3f(mod * 0.04f, 0.0f, 0.0f);

        switch(type)
        {
            case lowerlipSize:
            {
                Vector3f scale = lowerLip.getBindPose().getScaleVector();
                scale.x += mod * 4; scale.z += mod * 4;
                lowerLip.getBindPose().setScale(scale);
                break;
            }
            case upperlipSize:
            {
                Vector3f scale = upperLip.getBindPose().getScaleVector();
                scale.x += mod * 4; scale.z += mod * 4;
                upperLip.getBindPose().setScale(scale);
                break;
            }
            case mouthWidth:
            {
                skelnode.displaceJoint(mouth[6].getName(), ladjust);
                skelnode.displaceJoint(mouth[7].getName(), radjust);
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
        HeadOptions = new imi.gui.JPanel_HeadOptions(this, m_bEyeTextures);
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
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame_AdvOptions().setVisible(true);
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////
// ACCESSORS
////////////////////////////////////////////////////////////////////////////////

    public SceneEssentials getSceneData() {
        return m_sceneData;
    }

    public Map<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]> getSkeletonJoints() {
        return m_skeleton;
    }

    public SkinnedMeshJoint[] getSkeletonJointsBySection(int section) {
        return m_skeleton.get(section);
    }

////////////////////////////////////////////////////////////////////////////////
// MUTATORS
////////////////////////////////////////////////////////////////////////////////

    public void setSceneData(SceneEssentials scene) {
        m_sceneData = scene;
    }

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
    public PPolygonSkinnedMeshInstance[] getEyeBallMeshes() {
        PPolygonSkinnedMeshInstance[] eyeballs = null;
        if (m_sceneData.getAvatar() == null)
            return eyeballs;

        PNode lefteye = null;   PNode righteye = null;
        eyeballs    = new PPolygonSkinnedMeshInstance[2];
        lefteye     = m_sceneData.getAvatar().getModelInst().findChild("leftEyeGeoShape");
        eyeballs[0] = (PPolygonSkinnedMeshInstance)lefteye;
        righteye    = m_sceneData.getAvatar().getModelInst().findChild("rightEyeGeoShape");
        eyeballs[1] = (PPolygonSkinnedMeshInstance)righteye;

        return eyeballs;
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the hands for quick access
     */
    public void catalogHands() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftHand = new String[] { "leftHand",    "leftHandThumb1",   "leftHandThumb2",   "leftHandThumb3",   "leftHandThumb4",
                                             "leftPalm",    "leftHandIndex1",   "leftHandIndex2",   "leftHandIndex3",   "leftHandIndex4",
                                                            "leftHandMiddle1",  "leftHandMiddle2",  "leftHandMiddle3",  "leftHandMiddle4",
                                                            "leftHandRing1",    "leftHandRing2",    "leftHandRing3",    "leftHandRing4",
                                                            "leftHandPinky1",   "leftHandPinky2",   "leftHandPinky3",   "leftHandPinky4" };
        String[] szRightHand = new String[] { "rightHand",  "rightHandThumb1",  "rightHandThumb2",  "rightHandThumb3",  "rightHandThumb4",
                                              "rightPalm",  "rightHandIndex1",  "rightHandIndex2",  "rightHandIndex3",  "rightHandIndex4",
                                                            "rightHandMiddle1", "rightHandMiddle2", "rightHandMiddle3", "rightHandMiddle4",
                                                            "rightHandRing1",   "rightHandRing2",   "rightHandRing3",   "rightHandRing4",
                                                            "rightHandPinky1",  "rightHandPinky2",  "rightHandPinky3",  "rightHandPinky4"};

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftHand.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightHand.length];
        int                 iSize    = szLeftHand.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftHand[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightHand[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Hand, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Hand, right);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the lowerarms for quick access
     */
    public void catalogLowerArms() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftLowerArm     = new String[] { "leftForeArm",     "leftForeArmRoll" };
        String[] szRightLowerArm    = new String[] { "rightForeArm",    "rightForeArmRoll"};

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftLowerArm.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightLowerArm.length];
        int                 iSize    = szLeftLowerArm.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftLowerArm[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightLowerArm[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_LowerArm, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_LowerArm, right);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the upperarms for quick access
     */
    public void catalogUpperArms() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftUpperArm     = new String[] { "leftArm",     "leftArmRoll" };
        String[] szRightUpperArm    = new String[] { "rightArm",    "rightArmRoll" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftUpperArm.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightUpperArm.length];
        int                 iSize    = szLeftUpperArm.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftUpperArm[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightUpperArm[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_UpperArm, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_UpperArm, right);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the shoulders for quick access
     */
    public void catalogShoulders() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftShoulder     = new String[] { "leftShoulder" };
        String[] szRightShoulder    = new String[] { "rightShoulder" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftShoulder.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightShoulder.length];
        int                 iSize    = szLeftShoulder.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftShoulder[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightShoulder[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Shoulder, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Shoulder, right);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the torso for quick access
     */
    public void catalogTorso() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szTorso     = new String[] { "Spine", "Spine1", "Spine2" };

        SkinnedMeshJoint[]  torso     = new SkinnedMeshJoint[szTorso.length];
        int                 iSize    = szTorso.length;

        for (int i = 0; i < iSize; i++) {
            torso[i]     = (SkinnedMeshJoint) skeleton.findChild(szTorso[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Torso, torso);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the upperlegs for quick access
     */
    public void catalogUpperLegs() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftUpperLeg     = new String[] { "leftUpLeg",   "leftUpLegRoll" };
        String[] szRightUpperLeg    = new String[] { "rightUpLeg",  "rightUpLegRoll" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftUpperLeg.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightUpperLeg.length];
        int                 iSize    = szLeftUpperLeg.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftUpperLeg[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightUpperLeg[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_UpperLeg, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_UpperLeg, right);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the lowerlegs for quick access
     */
    public void catalogLowerLegs() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftLowerLeg     = new String[] { "leftLeg",     "leftLegRoll" };
        String[] szRightLowerLeg    = new String[] { "rightLeg",    "rightLegRoll" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftLowerLeg.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightLowerLeg.length];
        int                 iSize    = szLeftLowerLeg.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftLowerLeg[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightLowerLeg[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_LowerLeg, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_LowerLeg, right);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the feet for quick access
     */
    public void catalogFeet() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftFoot     = new String[] { "leftFoot",    "leftFootBall" };
        String[] szRightFoot    = new String[] { "rightFoot",   "rightFootBall" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftFoot.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightFoot.length];
        int                 iSize    = szLeftFoot.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftFoot[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightFoot[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Foot, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Foot, right);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the head for quick access
     */
    public void catalogHead() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szHead     = new String[] { "Head",            "Jaw",              "Tongue",           "Tongue1",
                                             "leftLowerLip",    "rightLowerLip",    "leftInnerBrow",    "leftEyeLid",
                                             "leftOuterBrow",   "leftCheek",        "leftUpperLip",     "leftOuterLip",
                                             "rightInnerBrow",  "rightOuterBrow",   "rightCheek",       "rightOuterLip",
                                             "rightUpperLip",   "rightEyeLid",      "leftEye",          "rightEye"};

        SkinnedMeshJoint[]  head     = new SkinnedMeshJoint[szHead.length];
        int                 iSize    = szHead.length;

        for (int i = 0; i < iSize; i++) {
            head[i]     = (SkinnedMeshJoint) skeleton.findChild(szHead[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Head, head);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the neck for quick access
     */
    public void catalogNeck() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szNeck     = new String[] { "Neck" };

        SkinnedMeshJoint[]  neck     = new SkinnedMeshJoint[szNeck.length];
        int                 iSize    = szNeck.length;

        for (int i = 0; i < iSize; i++) {
            neck[i]     = (SkinnedMeshJoint) skeleton.findChild(szNeck[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Neck, neck);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the eyes for quick access
     */
    public void catalogEyes() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szEyes     = new String[] { "EyeL_Adjust",     "leftEye",     "leftEyeLid",       "leftInnerBrow",    "leftOuterBrow",    "leftCheek",
                                             "EyeR_Adjust",     "rightEye",    "rightEyeLid",      "rightInnerBrow",   "rightOuterBrow",   "rightCheek" };

        SkinnedMeshJoint[]  eyes     = new SkinnedMeshJoint[szEyes.length];
        int                 iSize    = szEyes.length;

        for (int i = 0; i < iSize; i++) {
            eyes[i]     = (SkinnedMeshJoint) skeleton.findChild(szEyes[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Eyes, eyes);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the eyes for quick access
     */
    public void catalogEyeBalls() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szEyes     = new String[] { "leftEye",     "rightEye" };

        SkinnedMeshJoint[]  eyes     = new SkinnedMeshJoint[szEyes.length];
        int                 iSize    = szEyes.length;

        for (int i = 0; i < iSize; i++) {
            eyes[i]     = (SkinnedMeshJoint) skeleton.findChild(szEyes[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.EyeBalls, eyes);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the lips for quick access
     */
    public void catalogLips() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLips     = new String[] { "leftLowerLip",    "rightLowerLip",    "leftUpperLip",     "rightUpperLip",
                                             "leftCheek",       "rightCheek",       "leftOuterLip",     "rightOuterLip" };

        SkinnedMeshJoint[]  lips     = new SkinnedMeshJoint[szLips.length];
        int                 iSize    = szLips.length;

        for (int i = 0; i < iSize; i++) {
            lips[i]     = (SkinnedMeshJoint) skeleton.findChild(szLips[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Lips, lips);
    }

    /**
     * Sets up a listing of SkinnedMeshJoints for the eyes for quick access
     */
    public void catalogEars() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szEars     = new String[] { "LeftEar_Adjust", "RightEar_Adjust" };

        SkinnedMeshJoint[]  ears     = new SkinnedMeshJoint[szEars.length];
        int                 iSize    = szEars.length;

        for (int i = 0; i < iSize; i++) {
            ears[i]     = (SkinnedMeshJoint) skeleton.findChild(szEars[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Ears, ears);
    }
    
    /**
     * Sets up a listing of SkinnedMeshJoints for the nose for quick access
     */
    public void catalogNose() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szNose     = new String[] { "Nose_Adjust" };

        SkinnedMeshJoint[]  nose     = new SkinnedMeshJoint[szNose.length];
        int                 iSize    = szNose.length;

        for (int i = 0; i < iSize; i++) {
            nose[i]     = (SkinnedMeshJoint) skeleton.findChild(szNose[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Nose, nose);
    }

    /**
     * Creates a complete listing of all the SkinnedMeshJoints for the entire
     * avatar body
     */
    public void createJointCatalog() {
        if (m_sceneData.getAvatar() == null)
            return;

        catalogHands();
        catalogLowerArms();
        catalogUpperArms();
        catalogShoulders();
        catalogTorso();
        catalogUpperLegs();
        catalogLowerLegs();
        catalogFeet();
        catalogHead();
        catalogNeck();
        catalogEyes();
        catalogLips();
        catalogEars();
        catalogNose();
    }

    /**
     * Switchboard for all the joint sliders.  Pushes the correct response to the
     * right methods.
     * @param control - which slider control of effect
     * @param mod - delta modification value from the slider
     * @param actualval - actual modification value from the slider
     */
    public void parseModification(GUI_Enums.m_sliderControl control, float mod, float actualval) {
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
