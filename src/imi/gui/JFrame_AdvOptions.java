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
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
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
    }

    /**
     * Overloaded constructor to create an AdvOptions window.  It sets the scene
     * information as well as the information of the joints from the currently
     * loaded avatar
     * @param scene - reference to the main window's scene information
     */
    public JFrame_AdvOptions(SceneEssentials scene) {
        m_sceneData = scene;
        initComponents();
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
//                for (int i = 0; i < 5; i++) {
//                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
//                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
//                    curr.x = start.x;
//                    if (eyes[i].getName().contains("leftEye")) {
//                        ladjust.x = (actualval / 15);
//                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
//                    }
//                    else {
//                        ladjust.x = (actualval / 4);
//                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
//                    }
//                }
//
//                float y = scale.y += actualval * 4;
//                formattedNumber = m_format.format(y);
//                scale.x = Float.valueOf(formattedNumber);
//                eyes[1].getLocalModifierMatrix().setScale(scale);

                break;
            }
            case righteyeHPos:
            {
                radjust.x = mod * 0.01f;   radjust.y = 0.0f;    ladjust.z = 0.0f;
                skelnode.displaceJoint(eyes[6].getName(), radjust);
//                for (int i = 5; i < eyes.length; i++) {
//                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
//                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
//                    curr.x = start.x;
//                    if (eyes[i].getName().contains("rightEye")) {
//                        radjust.x = (actualval / 15);
//                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
//                    }
//                    else {
//                        radjust.x = (actualval / 4);
//                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
//                    }
//                }
//
//                float y = scale.y += actualval * 4;
//                formattedNumber = m_format.format(y);
//                scale.x = Float.valueOf(formattedNumber);
//                eyes[6].getLocalModifierMatrix().setScale(scale);

                break;
            }
            case lefteyeVPos:
            {
                ladjust.x = 0.0f;   ladjust.y = mod * 0.01f;    ladjust.z = 0.0f;
                skelnode.displaceJoint(eyes[0].getName(), ladjust);
//                for (int i = 0; i < 5; i++) {
//                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
//                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
//                    curr.y = start.y;
//                    if (eyes[i].getName().contains("leftEye")) {
//                        ladjust.y = (actualval / 15);
//                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
//                    }
//                    else {
//                        ladjust.y = (actualval / 4);
//                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
//                    }
//                }

                break;
            }
            case righteyeVPos:
            {
                radjust.x = 0.0f;   radjust.y = -mod * 0.01f;    ladjust.z = 0.0f;
                skelnode.displaceJoint(eyes[6].getName(), radjust);
//                for (int i = 5; i < eyes.length; i++) {
//                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
//                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
//                    curr.y = start.y;
//                    if (eyes[i].getName().contains("rightEye")) {
//                        radjust.y = (actualval / 15);
//                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
//                    }
//                    else {
//                        radjust.y = (actualval / 4);
//                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
//                    }
//                }

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
    // TODO: write functionality
    private synchronized void adjustChest(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  shoulders    = new SkinnedMeshJoint[] { m_skeleton.get(GUI_Enums.m_bodyPart.Left_Shoulder)[0],
                                                                    m_skeleton.get(GUI_Enums.m_bodyPart.Right_Shoulder)[0] };
        SkinnedMeshJoint[]  chest        = m_skeleton.get(GUI_Enums.m_bodyPart.Torso);
        Vector3f            adjust       = new Vector3f(0.0f, mod, 0.0f);
        m_format                         = new DecimalFormat("0.00");

        switch(type)
        {
            case torsoLength:
            {
                break;
            }
            case torsoThickness:
            {
                break;
            }
            case shoulderBroadness:
            {
                break;
            }
        }
    }

    private synchronized void adjustHead(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  head    = m_skeleton.get(GUI_Enums.m_bodyPart.Head);
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
        }
    }

    /**
     * Adjusts the joints catagorized under stomach.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of stomach manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    // TODO: write functionality
    private synchronized void adjustStomach(GUI_Enums.m_sliderControl type, float mod, float actualval) {

    }

    /**
     * Adjusts the joints catagorized under butt.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of butt manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    // TODO: write functionality
    private synchronized void adjustGluts(GUI_Enums.m_sliderControl type, float mod, float actualval) {

    }

    /**
     * Adjusts the joints catagorized under body.  Adjusts the location in
     * 3D space and scaling
     * @param type - integer represents the type of body manipulation
     * @param mod - delta modification of the selected slider/scrollbox
     * @param actualval - the actual value of modification from the selected slider/scrollbox
     */
    // TODO: write functionality
    private synchronized void adjustBody(GUI_Enums.m_sliderControl type, float mod, float actualval) {

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
        HeadOptions = new imi.gui.JPanel_HeadOptions(this);
        ArmsHandsOptions = new imi.gui.JPanel_ArmsHandsOption(this);
        LegsFeetOptions = new imi.gui.JPanel_LegsFeetOption(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jTabbedPane_Options.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane_Options.setMinimumSize(new java.awt.Dimension(300, 650));
        jTabbedPane_Options.setPreferredSize(new java.awt.Dimension(300, 650));
        jTabbedPane_Options.addTab("Head", HeadOptions);
        jTabbedPane_Options.addTab("Arms/Hands", ArmsHandsOptions);
        jTabbedPane_Options.addTab("Legs/Feet", LegsFeetOptions);

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
    private javax.swing.JTabbedPane jTabbedPane_Options;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

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
                // TODO: adjustMouth(control, mod, actualval);
                break;
            }
            case upperlipSize:
            {
                // TODO: adjustMouth(control, mod, actualval);
                break;
            }
            case mouthWidth:
            {
                // TODO: adjustMouth(control, mod, actualval);
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
        }
    }
}
