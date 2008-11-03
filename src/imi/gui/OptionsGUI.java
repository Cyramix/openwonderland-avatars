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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.gui;

import com.jme.math.Vector3f;
import imi.scene.PJoint;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.utils.tree.JointScaleProcessor;
import imi.scene.utils.tree.TreeTraverser;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Main options for user end manipulation of the avatar
 * @author  Viet Nguyen Truong
 */
public class OptionsGUI extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    private ArrayList<PPolygonMeshInstance> meshes  = new ArrayList<PPolygonMeshInstance>();
    private HashMap<String, Vector3f> scales        = new HashMap<String, Vector3f>();
    private HashMap<String, ArrayList<PNode>> group = new HashMap<String, ArrayList<PNode>>();
    
    private PScene currentPScene                    = null;
    private PPolygonModelInstance selectedInstance  = null;
    
    private double dScreenWidth                     = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private double dScreenHeight                    = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    
    private Vector3f vScaleBody                     = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f vScaleArmsU                    = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f vScaleArmsL                    = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f vScaleHands                    = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f vScaleLegsU                    = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f vScaleLegsL                    = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f vScaleFeet                     = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f vScaleHead                     = new Vector3f(1.0f, 1.0f, 1.0f);
    private float prevUniScale                      = 1.0f;
    
////////////////////////////////////////////////////////////////////////////////
// METHODS
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Default Constructor
     * Creates new form OptionsGUI
     */
    public OptionsGUI() {
        initComponents();
        this.setTitle("Avatar Options GUI");
        this.setLocationRelativeTo(this.getParent());
        
        int iYPos = (int) this.getLocation().getY();
        this.setLocation(0, iYPos);
    }
    
    /**
     * Initialize the sliders based on data from the skeleton
     */
    public void initValues() {
        if (currentPScene.getInstances().findChild("skeletonRoot") != null) {
            loadHeadGUIData();
            loadBodyGUIData();
            getGroupings();
        } else {
            setDefaultHeadSliders();
            setDefaultBodySliders();
            deactivateSliders();
        }
    }

    /**
     * Resets the UI head sliders to 1:1:1 values
     */
    public void setDefaultHeadSliders() {
        jSlider_HeadHeight.setValue(10);
        jSlider_HeadWidth.setValue(10);
        jSlider_HeadDepth.setValue(10);
    }
    
    /**
     * Resets the UI body sliders to 1:1:1 values
     */
    public void setDefaultBodySliders() {
        jSlider_Height.setValue(10);
        jSlider_Width.setValue(10);
        jSlider_ArmLength.setValue(10);
        jSlider_UpperArm.setValue(10);
        jSlider_Forearms.setValue(10);
        jSlider_HandSize.setValue(10);
        jSlider_LegLength.setValue(10);
        jSlider_Thighs.setValue(10);
        jSlider_Calfs.setValue(10);
        jSlider_FootSize.setValue(10);
    }
    
    /**
     * Helper function in loading user saved data (avatar configurations)
     */
    public void loadHeadGUIData() {
        PJoint head = ((PJoint) selectedInstance.findChild("Head"));
        if (head != null) {
            head.getLocalModifierMatrix().getScale(vScaleHead);
            scales.put("scaleHead", vScaleHead);
            
            jSlider_HeadHeight.setValue((int) (vScaleHead.getY() * 10));
            jSlider_HeadWidth.setValue((int) (vScaleHead.getX() * 10));
            jSlider_HeadDepth.setValue((int) (vScaleHead.getZ() * 10));
            
            jSlider_HeadHeight.setEnabled(true);
            jSlider_HeadWidth.setEnabled(true);
            jSlider_HeadDepth.setEnabled(true);
            
        } else {
            setDefaultHeadSliders();
        }
    }

    /**
     * Helper function in loading user saved data (avatar configurations)
     * that sets the GUI options in the Body Tab (NINJA ONLY)
     */
    public void loadBodyGUIData() {
        selectedInstance.getTransform().getLocalMatrix(true).getScale(vScaleBody);
        scales.put("scaleBody", vScaleBody);
        
        PJoint upperArm = ((PJoint) selectedInstance.findChild("leftArm"));
        PJoint foreArm = ((PJoint) selectedInstance.findChild("leftForeArm"));
        PJoint hands = ((PJoint) selectedInstance.findChild("leftHand"));
        PJoint thighs = ((PJoint) selectedInstance.findChild("leftUpLeg"));
        PJoint calvs = ((PJoint) selectedInstance.findChild("leftLeg"));
        PJoint feet = ((PJoint) selectedInstance.findChild("leftFoot"));

        jSlider_Height.setValue((int) (vScaleBody.getY() * 10));
        jSlider_Width.setValue((int) (vScaleBody.getZ() * 10));
        jSlider_Height.setEnabled(true);
        jSlider_Width.setEnabled(true);
        
        if (upperArm != null) {
            upperArm.getLocalModifierMatrix().getScale(vScaleArmsU);
            jSlider_ArmLength.setValue((int) (vScaleArmsU.getY() * 10));
            jSlider_UpperArm.setValue((int) (vScaleArmsU.getZ() * 10));
            jSlider_ArmLength.setEnabled(true);
            jSlider_UpperArm.setEnabled(true);
        } else {
            jSlider_ArmLength.setValue(10);
            jSlider_UpperArm.setValue(10);
        }
        
        if (foreArm != null) {
            foreArm.getLocalModifierMatrix().getScale(vScaleArmsL);
            jSlider_Forearms.setValue((int) (vScaleArmsL.getZ() * 10));
            jSlider_Forearms.setEnabled(true);
        } else {
            jSlider_Forearms.setValue(10);
        }
        
        if (hands != null) {
            hands.getLocalModifierMatrix().getScale(vScaleHands);
            jSlider_HandSize.setValue((int) (vScaleHands.getX() * 10));
            jSlider_HandSize.setEnabled(true);
        } else {
            jSlider_HandSize.setValue(10);
        }
        
        if (thighs != null) {
            thighs.getLocalModifierMatrix().getScale(vScaleLegsU);
            jSlider_LegLength.setValue((int) (vScaleLegsU.getY() * 10));
            jSlider_Thighs.setValue((int) (vScaleLegsU.getZ() * 10));
            jSlider_LegLength.setEnabled(true);
            jSlider_Thighs.setEnabled(true);
        } else {
            jSlider_LegLength.setValue(10);
            jSlider_Thighs.setValue(10);
        }
        
        if (calvs != null) {
            calvs.getLocalModifierMatrix().getScale(vScaleLegsL);
            jSlider_Calfs.setValue((int) (vScaleLegsL.getZ() * 10));
            jSlider_Calfs.setEnabled(true);
        } else {
            jSlider_Calfs.setValue(10);
        }
        
        if (feet != null) {
            feet.getLocalModifierMatrix().getScale(vScaleFeet);
            jSlider_FootSize.setValue((int) (vScaleFeet.getX() * 10));
            jSlider_FootSize.setEnabled(true);
            jSlider_FootSize.setValue(10);
        }
    }
    
    public void deactivateSliders() {
        jSlider_UniformScale.setEnabled(false);
        jSlider_HeadHeight.setEnabled(false);
        jSlider_HeadWidth.setEnabled(false);
        jSlider_HeadDepth.setEnabled(false);
        jSlider_Height.setEnabled(false);
        jSlider_Width.setEnabled(false);
        jSlider_ArmLength.setEnabled(false);
        jSlider_UpperArm.setEnabled(false);
        jSlider_Forearms.setEnabled(false);
        jSlider_HandSize.setEnabled(false);
        jSlider_LegLength.setEnabled(false);
        jSlider_Thighs.setEnabled(false);
        jSlider_Calfs.setEnabled(false);
        jSlider_FootSize.setEnabled(false);
    }
    
    public void getGroupings() {
        if(selectedInstance.getChildrenCount() > 0) {
            int i = 0;
            for(i = 0; i < selectedInstance.getChildrenCount(); i++) {
                if(selectedInstance.getChild(i) instanceof SkeletonNode)
                    break;
            }
            SkeletonNode skeleton = ((SkeletonNode)selectedInstance.getChild(i));
            
            for(int j = 0; j < skeleton.getSkinnedMeshInstances().size(); j++) {
                meshes.add(skeleton.getSkinnedMeshInstances().get(j));
                int[] jointIndices = skeleton.getSkinnedMeshInstances().get(j).getInfluenceIndices();
                ArrayList<PNode> joints = new ArrayList<PNode>();
                for(int k = 0; k < jointIndices.length; k++) {
                    joints.add(skeleton.getSkinnedMeshJoint(jointIndices[k]));
                }
                group.put(skeleton.getSkinnedMeshInstances().get(j).getName(), joints);
            }
        }
    }
    
    public HashMap<String, ArrayList<PNode>> getMeshGroups() {
        return group;
    }
    /**
     * Gets head scaling data from slider and affects the model's head
     * @param axis (int) - the axis to scale on
     */
    public void scaleHead(int axis) {
        float scale = 1.0f;
        switch (axis) {
            case 0: {
                scale = (jSlider_HeadWidth.getValue() * 0.10f);
                vScaleHead.setX(scale);
                break;
            }
            case 1: {
                scale = (jSlider_HeadHeight.getValue() * 0.10f);
                vScaleHead.setY(scale);
                break;
            }
            case 2: {
                scale = (jSlider_HeadDepth.getValue() * 0.10f);
                vScaleHead.setZ(scale);
                break;
            }
        }
        // Head (Joint 8)
        if (selectedInstance.findChild("skeletonRoot") != null) {
            PNode headRoot = selectedInstance.findChild("Head");
            
            if (headRoot != null) {
                JointScaleProcessor proc = new JointScaleProcessor();
                proc.setScale(vScaleHead);
                TreeTraverser.breadthFirst(headRoot, proc);
            } else {
                System.out.println("ERROR: a joint/bone was not found");
            }  
        } else {
            System.out.println("ERROR: a joint/bone was not found");
        }
    }

    /**
     * Gets the body scaling data from the slider and applies the scaling
     * affect (NINJA ONLY based on the heirarchy setup)
     * @param type (int) - the type of scale you want
     */
    public void scaleBody(int type) {
        float scale = 1.0f;
        switch (type) {
            case 0: {
                scale = (jSlider_Height.getValue() * 0.10f);
                vScaleBody.setY(scale);
                break;
            }
            case 1: {
                scale = (jSlider_Width.getValue() * 0.10f);
                vScaleBody.setX(scale);
                vScaleBody.setZ(scale);
                break;
            }
            case 2: {
                jSlider_UniformScale.setEnabled(true);
                scale = (jSlider_UniformScale.getValue() * 0.10f);
                float diff = (scale - prevUniScale);
                prevUniScale = scale;
                vScaleBody = vScaleBody.add(diff, diff, diff);
                if (vScaleBody.getY() < 0.1f) {
                    vScaleBody.setY(0.1f);
                }
                if (vScaleBody.getY() > 4.0f) {
                    vScaleBody.setY(4.0f);
                }
                if (vScaleBody.getX() < 0.1f || vScaleBody.getZ() < 0.1f) {
                    vScaleBody.setX(0.1f);
                    vScaleBody.setZ(0.1f);
                }
                if (vScaleBody.getX() > 4.0f || vScaleBody.getZ() > 4.0f) {
                    vScaleBody.setX(4.0f);
                    vScaleBody.setZ(4.0f);
                }
                jSlider_Height.setValue(((int) (vScaleBody.getX() * 10)));
                jSlider_Width.setValue(((int) (vScaleBody.getZ() * 10)));
                break;
            }
        }
        selectedInstance.getTransform().getLocalMatrix(true).setScale(vScaleBody);
        selectedInstance.setDirty(true, true);
    }

    /**
     * Gets the arm scaling data from the slider and applies the scaling
     * affect (NINJA ONLY based on the heirarchy setup)
     * @param type (int) - the type of scale you want
     */
    public void scaleArm(int type) {
        float scale = 1.0f;
        switch (type) {
            case 0: // Length of upper and lower
            {
                scale = (jSlider_ArmLength.getValue() * 0.10f);
                vScaleArmsU.setY(scale);
                vScaleArmsL.setY(scale);
                break;
            }
            case 1: // Upper arm bodyfat
            {
                scale = (jSlider_UpperArm.getValue() * 0.10f);
                vScaleArmsU.setX(scale);
                vScaleArmsU.setZ(scale);
                break;
            }
            case 2: // Lower arm bodyfat
            {
                scale = (jSlider_Forearms.getValue() * 0.10f);
                vScaleArmsL.setX(scale);
                vScaleArmsL.setZ(scale);
                break;
            }
        }
        if (selectedInstance.findChild("skeletonRoot") != null) {
            
            PNode upperArmR = selectedInstance.findChild("rightArm");
            PNode upperArmRr = selectedInstance.findChild("rightArmRoll");
            PNode foreArmR = selectedInstance.findChild("rightForeArm");
            PNode foreArmRr = selectedInstance.findChild("rightForeArmRoll");
            PNode upperArmL = selectedInstance.findChild("leftArm");
            PNode upperArmLr = selectedInstance.findChild("leftArmRoll");
            PNode foreArmL = selectedInstance.findChild("leftForeArm");
            PNode foreArmLr = selectedInstance.findChild("leftForeArmRoll");
            if (upperArmR != null || foreArmR != null || upperArmL != null || foreArmL != null) {
                ((PJoint) upperArmR).getLocalModifierMatrix().setScale(vScaleArmsU);
                ((PJoint) upperArmRr).getLocalModifierMatrix().setScale(vScaleArmsU);
                ((PJoint) foreArmR).getLocalModifierMatrix().setScale(vScaleArmsL);
                ((PJoint) foreArmRr).getLocalModifierMatrix().setScale(vScaleArmsL);
                ((PJoint) upperArmL).getLocalModifierMatrix().setScale(vScaleArmsU);
                ((PJoint) upperArmLr).getLocalModifierMatrix().setScale(vScaleArmsU);
                ((PJoint) foreArmL).getLocalModifierMatrix().setScale(vScaleArmsL);
                ((PJoint) foreArmLr).getLocalModifierMatrix().setScale(vScaleArmsL);
            } else {
                System.out.println("ERROR: a joint/bone was not found");
            }
        } else {
            System.out.println("ERROR: a joint/bone was not found");
        }
    }

    /**
     * Gets the leg scaling data from the slider and applies the scaling
     * affect (NINJA ONLY based on the heirarchy setup)
     * @param type (int) - the type of scale you want
     */
    public void scaleLeg(int type) {
        float scale = 1.0f;
        switch (type) {
            case 0: // Length of upper and lower
            {
                scale = (jSlider_LegLength.getValue() * 0.10f);
                vScaleLegsU.setY(scale);
                vScaleLegsL.setY(scale);
                break;
            }
            case 1: // Upper leg bodyfat
            {
                scale = (jSlider_Thighs.getValue() * 0.10f);
                vScaleLegsU.setX(scale);
                vScaleLegsU.setZ(scale);
                break;
            }
            case 2: // Lower leg bodyfat
            {
                scale = (jSlider_Calfs.getValue() * 0.10f);
                vScaleLegsL.setX(scale);
                vScaleLegsL.setZ(scale);
                break;
            }
        }
        if (selectedInstance.findChild("skeletonRoot") != null) {
            // Right Leg (Joints 18, 19) Left Leg (Joints 23, 24)
            PNode thighR = selectedInstance.findChild("rightUpLeg");
            PNode thighRr = selectedInstance.findChild("rightUpLegRoll");
            PNode calfR = selectedInstance.findChild("rightLeg");
            PNode calfRr = selectedInstance.findChild("rightLegRoll");
            PNode thighL = selectedInstance.findChild("leftUpLeg");
            PNode thighLr = selectedInstance.findChild("leftUpLegRoll");
            PNode calfL = selectedInstance.findChild("leftLeg");
            PNode calfLr = selectedInstance.findChild("leftLegRoll");
            if (thighR != null || calfR != null || thighL != null || calfL != null) {
                ((PJoint) thighR).getLocalModifierMatrix().setScale(vScaleLegsU);
                ((PJoint) thighRr).getLocalModifierMatrix().setScale(vScaleLegsU);
                ((PJoint) calfR).getLocalModifierMatrix().setScale(vScaleLegsL);
                ((PJoint) calfRr).getLocalModifierMatrix().setScale(vScaleLegsL);
                ((PJoint) thighL).getLocalModifierMatrix().setScale(vScaleLegsU);
                ((PJoint) thighLr).getLocalModifierMatrix().setScale(vScaleLegsU);
                ((PJoint) calfL).getLocalModifierMatrix().setScale(vScaleLegsL);
                ((PJoint) calfLr).getLocalModifierMatrix().setScale(vScaleLegsL);
            } else {
                System.out.println("ERROR: a joint/bone was not found");
            }
        } else {
            System.out.println("ERROR: a joint/bone was not found");
        }
    }

    /**
     * Gets hand scaling data from the slider and applies the scaling affect
     * (NINJA ONLY based on the heirarchy setup)
     */
    public void scaleHands() {
        float scale = (jSlider_HandSize.getValue() * 0.10f);
        vScaleHands.setX(scale);
        vScaleHands.setY(scale);
        vScaleHands.setZ(scale);

        if (selectedInstance.findChild("skeletonRoot") != null) {
            // Right Hand (Joints 12) Left Hand (Joints 17)
            PNode handLroot = selectedInstance.findChild("leftHand");
            PNode handRroot = selectedInstance.findChild("rightHand");
            
            if (handLroot != null || handRroot != null) {
                JointScaleProcessor proc = new JointScaleProcessor();
                proc.setScale(vScaleHands);
                TreeTraverser.breadthFirst(handLroot, proc);
                TreeTraverser.breadthFirst(handRroot, proc);
            } else {
                System.out.println("ERROR: a joint/bone was not found");
            }            
        } else {
            System.out.println("ERROR: a joint/bone was not found");
        }
    }

    /**
     * Gets the foot scaling data from the slider and applies the scaling
     * affect (NINJA ONLY based on the heirarchy)
     */
    public void scaleFeet() {
        float scale = (jSlider_FootSize.getValue() * 0.10f);
        vScaleFeet.setX(scale);
        vScaleFeet.setY(scale);
        vScaleFeet.setZ(scale);

        if (selectedInstance.findChild("skeletonRoot") != null) {
            // Right foot (Joints 20, 21) Left foot (Joints 25, 26)
            PNode footLroot = selectedInstance.findChild("leftFoot");
            PNode footRroot = selectedInstance.findChild("rightFoot");
            
            if (footLroot != null || footRroot != null) {
                JointScaleProcessor proc = new JointScaleProcessor();
                proc.setScale(vScaleFeet);
                TreeTraverser.breadthFirst(footLroot, proc);
                TreeTraverser.breadthFirst(footRroot, proc);
            } else {
                System.out.println("ERROR: a joint/bone was not found");
            }  
        } else {
            System.out.println("ERROR: a joint/bone was not found");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Mutators
    ////////////////////////////////////////////////////////////////////////////
    public void setPScene(PScene scene) {
        currentPScene = scene;
    }

    public void setAvatarName(String name) {
        jTextField_Name.setText(name);
    }

    public void setAvatarGender(String gender) {
        if (gender.equals("male")) {
            jRadioButton_GenderM.setSelected(true);
            jRadioButton_GenderF.setSelected(false);
        } else {
            jRadioButton_GenderF.setSelected(true);
            jRadioButton_GenderM.setSelected(false);
        }
    }
    
    public void setSelectedInstance(PPolygonModelInstance instance) {
        if (instance == null)
            selectedInstance = null;
        else
            selectedInstance = instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Accessors
    ////////////////////////////////////////////////////////////////////////////
    public String getAvatarName() {
        return jTextField_Name.getText();
    }

    public String getAvatarGender() {
        if (jRadioButton_GenderM.isSelected()) {
            return "Male";
        } else {
            return "Female";
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

        buttonGroup_Gender = new javax.swing.ButtonGroup();
        jPanel_Name = new javax.swing.JPanel();
        jTextField_Name = new javax.swing.JTextField();
        jPanel_Gender = new javax.swing.JPanel();
        jRadioButton_GenderM = new javax.swing.JRadioButton();
        jRadioButton_GenderF = new javax.swing.JRadioButton();
        jTabbedPane_Options = new javax.swing.JTabbedPane();
        jScrollPanel_Colors = new javax.swing.JScrollPane();
        jPanel_Colors = new javax.swing.JPanel();
        jPanel_HairColor = new javax.swing.JPanel();
        jToolBar_HairR = new javax.swing.JToolBar();
        jPanel_HairColorR = new javax.swing.JPanel();
        jSlider_HairColorR = new javax.swing.JSlider();
        jSpinner_HairR = new javax.swing.JSpinner();
        jToolBar_HairG = new javax.swing.JToolBar();
        jPanel_HairColorG = new javax.swing.JPanel();
        jSlider_HairColorG = new javax.swing.JSlider();
        jSpinner_HairG = new javax.swing.JSpinner();
        jToolBar_HairB = new javax.swing.JToolBar();
        jPanel_HairColorB = new javax.swing.JPanel();
        jSlider_HairColorB = new javax.swing.JSlider();
        jSpinner_HairB = new javax.swing.JSpinner();
        jPanel_EyeColor = new javax.swing.JPanel();
        jToolBar_EyeR = new javax.swing.JToolBar();
        jPanel_EyeColorR = new javax.swing.JPanel();
        jSlider_EyeColorR = new javax.swing.JSlider();
        jSpinner_EyeR = new javax.swing.JSpinner();
        jToolBar_EyeG = new javax.swing.JToolBar();
        jPanel_EyeColorG = new javax.swing.JPanel();
        jSlider_EyeColorG = new javax.swing.JSlider();
        jSpinner_EyeG = new javax.swing.JSpinner();
        jToolBar_EyeB = new javax.swing.JToolBar();
        jPanel_EyeColorB = new javax.swing.JPanel();
        jSlider_EyeColorB = new javax.swing.JSlider();
        jSpinner_EyeB = new javax.swing.JSpinner();
        jPanel_SkinColor = new javax.swing.JPanel();
        jToolBar_SkinR = new javax.swing.JToolBar();
        jPanel_SkinColorR = new javax.swing.JPanel();
        jSlider_SkinColorR = new javax.swing.JSlider();
        jSpinner_SkinR = new javax.swing.JSpinner();
        jToolBar_SkinG = new javax.swing.JToolBar();
        jPanel_SkinColorG = new javax.swing.JPanel();
        jSlider_SkinColorG = new javax.swing.JSlider();
        jSpinner_SkinG = new javax.swing.JSpinner();
        jToolBar_SkinB = new javax.swing.JToolBar();
        jPanel_SkinColorB = new javax.swing.JPanel();
        jSlider_SkinColorB = new javax.swing.JSlider();
        jSpinner_SkinB = new javax.swing.JSpinner();
        jScrollPane_Head = new javax.swing.JScrollPane();
        jPanel_Head = new javax.swing.JPanel();
        jToolBar_HeadHeight = new javax.swing.JToolBar();
        jLabel_HeadHeight = new javax.swing.JLabel();
        jSlider_HeadHeight = new javax.swing.JSlider();
        jToolBar_HeadWidth = new javax.swing.JToolBar();
        jLabel_HeadWidth = new javax.swing.JLabel();
        jSlider_HeadWidth = new javax.swing.JSlider();
        jToolBar_HeadDepth = new javax.swing.JToolBar();
        jLabel_HeadDepth = new javax.swing.JLabel();
        jSlider_HeadDepth = new javax.swing.JSlider();
        jScrollPane_Body = new javax.swing.JScrollPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel_Body = new javax.swing.JPanel();
        jToolBar_UniformScale = new javax.swing.JToolBar();
        jLabel_UniformScale = new javax.swing.JLabel();
        jSlider_UniformScale = new javax.swing.JSlider();
        jToolBar_HeightScale = new javax.swing.JToolBar();
        jLabel_HeightScale = new javax.swing.JLabel();
        jSlider_Height = new javax.swing.JSlider();
        jToolBar_WidthScale = new javax.swing.JToolBar();
        jLabel_WidthScale = new javax.swing.JLabel();
        jSlider_Width = new javax.swing.JSlider();
        jPanel_BodyParts = new javax.swing.JPanel();
        jToolBar_ArmLength = new javax.swing.JToolBar();
        jLabel_ArmLength = new javax.swing.JLabel();
        jSlider_ArmLength = new javax.swing.JSlider();
        jToolBar_UpperArm = new javax.swing.JToolBar();
        jLabel_UpperArm = new javax.swing.JLabel();
        jSlider_UpperArm = new javax.swing.JSlider();
        jToolBar_Forearms = new javax.swing.JToolBar();
        jLabel_Forearms = new javax.swing.JLabel();
        jSlider_Forearms = new javax.swing.JSlider();
        jToolBar_HandSize = new javax.swing.JToolBar();
        jLabel_HandSize = new javax.swing.JLabel();
        jSlider_HandSize = new javax.swing.JSlider();
        jToolBar_LegLength = new javax.swing.JToolBar();
        jLabel_LegLength = new javax.swing.JLabel();
        jSlider_LegLength = new javax.swing.JSlider();
        jToolBar_Thighs = new javax.swing.JToolBar();
        jLabel_Thighs = new javax.swing.JLabel();
        jSlider_Thighs = new javax.swing.JSlider();
        jToolBar_Calfs = new javax.swing.JToolBar();
        jLabel_Calfs = new javax.swing.JLabel();
        jSlider_Calfs = new javax.swing.JSlider();
        jToolBar_FootSize = new javax.swing.JToolBar();
        jLabel_FootSize = new javax.swing.JLabel();
        jSlider_FootSize = new javax.swing.JSlider();
        jScrollPanel_Details = new javax.swing.JScrollPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel_BodyDetails = new javax.swing.JPanel();
        jToolBar_BodyScars = new javax.swing.JToolBar();
        jLabel_BodyScars = new javax.swing.JLabel();
        jSpinner_BodyScars = new javax.swing.JSpinner();
        jToolBar_BodyTattoos = new javax.swing.JToolBar();
        jLabel_BodyTattoos = new javax.swing.JLabel();
        jSpinner_BodyTattoos = new javax.swing.JSpinner();
        jToolBar_BodyPiercings = new javax.swing.JToolBar();
        jLabel_BodyPiercings = new javax.swing.JLabel();
        jSpinner_BodyPiercings = new javax.swing.JSpinner();
        jPanel_HeadDetails = new javax.swing.JPanel();
        jToolBar_HairStyle = new javax.swing.JToolBar();
        jLabel_HairStyle = new javax.swing.JLabel();
        jSpinner_HairStyle = new javax.swing.JSpinner();
        jToolBar_FacialHair = new javax.swing.JToolBar();
        jLabel_FacialHair = new javax.swing.JLabel();
        jSpinner_FacialHair = new javax.swing.JSpinner();
        jToolBar_Ears = new javax.swing.JToolBar();
        jLabel_Ears = new javax.swing.JLabel();
        jSpinner_EarPiercing = new javax.swing.JSpinner();
        jToolBar_Nose = new javax.swing.JToolBar();
        jLabel_Nose = new javax.swing.JLabel();
        jSpinner_NosePiercing = new javax.swing.JSpinner();
        jToolBar_Eyes = new javax.swing.JToolBar();
        jLabel_Eyes = new javax.swing.JLabel();
        jSpinner_EyePiercings = new javax.swing.JSpinner();
        jToolBar_Lips = new javax.swing.JToolBar();
        jLabel_Lips = new javax.swing.JLabel();
        jSpinner_LipPiercing = new javax.swing.JSpinner();
        jToolBar_Tattoos = new javax.swing.JToolBar();
        jLabel_Tattoos = new javax.swing.JLabel();
        jSpinner_Tattoos = new javax.swing.JSpinner();
        jToolBar_Scars = new javax.swing.JToolBar();
        jLabel_Scars = new javax.swing.JLabel();
        jSpinner_Scars = new javax.swing.JSpinner();
        jPanel_Shirts = new javax.swing.JPanel();
        jPanel_Pants = new javax.swing.JPanel();
        jPanel_Dresses = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel_Name.setBorder(javax.swing.BorderFactory.createTitledBorder("Name"));

        jTextField_Name.setText("Robert");

        org.jdesktop.layout.GroupLayout jPanel_NameLayout = new org.jdesktop.layout.GroupLayout(jPanel_Name);
        jPanel_Name.setLayout(jPanel_NameLayout);
        jPanel_NameLayout.setHorizontalGroup(
            jPanel_NameLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_NameLayout.createSequentialGroup()
                .addContainerGap()
                .add(jTextField_Name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel_NameLayout.setVerticalGroup(
            jPanel_NameLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_NameLayout.createSequentialGroup()
                .add(jTextField_Name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel_Gender.setBorder(javax.swing.BorderFactory.createTitledBorder("Gender"));

        buttonGroup_Gender.add(jRadioButton_GenderM);
        jRadioButton_GenderM.setSelected(true);
        jRadioButton_GenderM.setText("Male");

        buttonGroup_Gender.add(jRadioButton_GenderF);
        jRadioButton_GenderF.setText("Female");

        org.jdesktop.layout.GroupLayout jPanel_GenderLayout = new org.jdesktop.layout.GroupLayout(jPanel_Gender);
        jPanel_Gender.setLayout(jPanel_GenderLayout);
        jPanel_GenderLayout.setHorizontalGroup(
            jPanel_GenderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_GenderLayout.createSequentialGroup()
                .addContainerGap()
                .add(jRadioButton_GenderM)
                .add(18, 18, 18)
                .add(jRadioButton_GenderF)
                .addContainerGap(71, Short.MAX_VALUE))
        );
        jPanel_GenderLayout.setVerticalGroup(
            jPanel_GenderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_GenderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jRadioButton_GenderM)
                .add(jRadioButton_GenderF))
        );

        jScrollPanel_Colors.setMaximumSize(new java.awt.Dimension(230, 575));
        jScrollPanel_Colors.setMinimumSize(new java.awt.Dimension(230, 372));

        jPanel_Colors.setMaximumSize(new java.awt.Dimension(253, 353));
        jPanel_Colors.setPreferredSize(new java.awt.Dimension(230, 353));

        jPanel_HairColor.setBorder(javax.swing.BorderFactory.createTitledBorder("Hair Color"));

        jToolBar_HairR.setFloatable(false);
        jToolBar_HairR.setRollover(true);

        jPanel_HairColorR.setBackground(new java.awt.Color(255, 0, 0));
        jPanel_HairColorR.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_HairColorR.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_HairColorR.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_HairColorR.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_HairColorRLayout = new org.jdesktop.layout.GroupLayout(jPanel_HairColorR);
        jPanel_HairColorR.setLayout(jPanel_HairColorRLayout);
        jPanel_HairColorRLayout.setHorizontalGroup(
            jPanel_HairColorRLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_HairColorRLayout.setVerticalGroup(
            jPanel_HairColorRLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_HairR.add(jPanel_HairColorR);

        jSlider_HairColorR.setMaximum(255);
        jSlider_HairColorR.setValue(127);
        jSlider_HairColorR.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_HairR.add(jSlider_HairColorR);
        jToolBar_HairR.add(jSpinner_HairR);

        jToolBar_HairG.setFloatable(false);
        jToolBar_HairG.setRollover(true);

        jPanel_HairColorG.setBackground(new java.awt.Color(0, 255, 0));
        jPanel_HairColorG.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_HairColorG.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_HairColorG.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_HairColorG.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_HairColorGLayout = new org.jdesktop.layout.GroupLayout(jPanel_HairColorG);
        jPanel_HairColorG.setLayout(jPanel_HairColorGLayout);
        jPanel_HairColorGLayout.setHorizontalGroup(
            jPanel_HairColorGLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_HairColorGLayout.setVerticalGroup(
            jPanel_HairColorGLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_HairG.add(jPanel_HairColorG);

        jSlider_HairColorG.setMaximum(255);
        jSlider_HairColorG.setValue(127);
        jSlider_HairColorG.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_HairG.add(jSlider_HairColorG);
        jToolBar_HairG.add(jSpinner_HairG);

        jToolBar_HairB.setFloatable(false);
        jToolBar_HairB.setRollover(true);

        jPanel_HairColorB.setBackground(new java.awt.Color(0, 0, 255));
        jPanel_HairColorB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_HairColorB.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_HairColorB.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_HairColorB.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_HairColorBLayout = new org.jdesktop.layout.GroupLayout(jPanel_HairColorB);
        jPanel_HairColorB.setLayout(jPanel_HairColorBLayout);
        jPanel_HairColorBLayout.setHorizontalGroup(
            jPanel_HairColorBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_HairColorBLayout.setVerticalGroup(
            jPanel_HairColorBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_HairB.add(jPanel_HairColorB);

        jSlider_HairColorB.setMaximum(255);
        jSlider_HairColorB.setValue(127);
        jSlider_HairColorB.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_HairB.add(jSlider_HairColorB);
        jToolBar_HairB.add(jSpinner_HairB);

        org.jdesktop.layout.GroupLayout jPanel_HairColorLayout = new org.jdesktop.layout.GroupLayout(jPanel_HairColor);
        jPanel_HairColor.setLayout(jPanel_HairColorLayout);
        jPanel_HairColorLayout.setHorizontalGroup(
            jPanel_HairColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jToolBar_HairB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_HairG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_HairR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel_HairColorLayout.setVerticalGroup(
            jPanel_HairColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HairColorLayout.createSequentialGroup()
                .add(jToolBar_HairR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_HairG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_HairB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel_EyeColor.setBorder(javax.swing.BorderFactory.createTitledBorder("Eye Color"));

        jToolBar_EyeR.setFloatable(false);
        jToolBar_EyeR.setRollover(true);

        jPanel_EyeColorR.setBackground(new java.awt.Color(255, 0, 0));
        jPanel_EyeColorR.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_EyeColorR.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_EyeColorR.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_EyeColorR.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_EyeColorRLayout = new org.jdesktop.layout.GroupLayout(jPanel_EyeColorR);
        jPanel_EyeColorR.setLayout(jPanel_EyeColorRLayout);
        jPanel_EyeColorRLayout.setHorizontalGroup(
            jPanel_EyeColorRLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_EyeColorRLayout.setVerticalGroup(
            jPanel_EyeColorRLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_EyeR.add(jPanel_EyeColorR);

        jSlider_EyeColorR.setMaximum(255);
        jSlider_EyeColorR.setValue(127);
        jSlider_EyeColorR.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_EyeR.add(jSlider_EyeColorR);
        jToolBar_EyeR.add(jSpinner_EyeR);

        jToolBar_EyeG.setFloatable(false);
        jToolBar_EyeG.setRollover(true);

        jPanel_EyeColorG.setBackground(new java.awt.Color(0, 255, 0));
        jPanel_EyeColorG.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_EyeColorG.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_EyeColorG.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_EyeColorG.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_EyeColorGLayout = new org.jdesktop.layout.GroupLayout(jPanel_EyeColorG);
        jPanel_EyeColorG.setLayout(jPanel_EyeColorGLayout);
        jPanel_EyeColorGLayout.setHorizontalGroup(
            jPanel_EyeColorGLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_EyeColorGLayout.setVerticalGroup(
            jPanel_EyeColorGLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_EyeG.add(jPanel_EyeColorG);

        jSlider_EyeColorG.setMaximum(255);
        jSlider_EyeColorG.setValue(127);
        jSlider_EyeColorG.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_EyeG.add(jSlider_EyeColorG);
        jToolBar_EyeG.add(jSpinner_EyeG);

        jToolBar_EyeB.setFloatable(false);
        jToolBar_EyeB.setRollover(true);

        jPanel_EyeColorB.setBackground(new java.awt.Color(0, 0, 255));
        jPanel_EyeColorB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_EyeColorB.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_EyeColorB.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_EyeColorB.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_EyeColorBLayout = new org.jdesktop.layout.GroupLayout(jPanel_EyeColorB);
        jPanel_EyeColorB.setLayout(jPanel_EyeColorBLayout);
        jPanel_EyeColorBLayout.setHorizontalGroup(
            jPanel_EyeColorBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_EyeColorBLayout.setVerticalGroup(
            jPanel_EyeColorBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_EyeB.add(jPanel_EyeColorB);

        jSlider_EyeColorB.setMaximum(255);
        jSlider_EyeColorB.setValue(127);
        jSlider_EyeColorB.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_EyeB.add(jSlider_EyeColorB);
        jToolBar_EyeB.add(jSpinner_EyeB);

        org.jdesktop.layout.GroupLayout jPanel_EyeColorLayout = new org.jdesktop.layout.GroupLayout(jPanel_EyeColor);
        jPanel_EyeColor.setLayout(jPanel_EyeColorLayout);
        jPanel_EyeColorLayout.setHorizontalGroup(
            jPanel_EyeColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jToolBar_EyeR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_EyeG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_EyeB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel_EyeColorLayout.setVerticalGroup(
            jPanel_EyeColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_EyeColorLayout.createSequentialGroup()
                .add(jToolBar_EyeR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_EyeG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_EyeB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel_SkinColor.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Skin/Body Color"));

        jToolBar_SkinR.setFloatable(false);
        jToolBar_SkinR.setRollover(true);

        jPanel_SkinColorR.setBackground(new java.awt.Color(255, 0, 0));
        jPanel_SkinColorR.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_SkinColorR.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_SkinColorR.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_SkinColorR.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_SkinColorRLayout = new org.jdesktop.layout.GroupLayout(jPanel_SkinColorR);
        jPanel_SkinColorR.setLayout(jPanel_SkinColorRLayout);
        jPanel_SkinColorRLayout.setHorizontalGroup(
            jPanel_SkinColorRLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_SkinColorRLayout.setVerticalGroup(
            jPanel_SkinColorRLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_SkinR.add(jPanel_SkinColorR);

        jSlider_SkinColorR.setMaximum(255);
        jSlider_SkinColorR.setValue(127);
        jSlider_SkinColorR.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_SkinR.add(jSlider_SkinColorR);
        jToolBar_SkinR.add(jSpinner_SkinR);

        jToolBar_SkinG.setFloatable(false);
        jToolBar_SkinG.setRollover(true);

        jPanel_SkinColorG.setBackground(new java.awt.Color(0, 255, 0));
        jPanel_SkinColorG.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_SkinColorG.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_SkinColorG.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_SkinColorG.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_SkinColorGLayout = new org.jdesktop.layout.GroupLayout(jPanel_SkinColorG);
        jPanel_SkinColorG.setLayout(jPanel_SkinColorGLayout);
        jPanel_SkinColorGLayout.setHorizontalGroup(
            jPanel_SkinColorGLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_SkinColorGLayout.setVerticalGroup(
            jPanel_SkinColorGLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_SkinG.add(jPanel_SkinColorG);

        jSlider_SkinColorG.setMaximum(255);
        jSlider_SkinColorG.setValue(127);
        jSlider_SkinColorG.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_SkinG.add(jSlider_SkinColorG);
        jToolBar_SkinG.add(jSpinner_SkinG);

        jToolBar_SkinB.setFloatable(false);
        jToolBar_SkinB.setRollover(true);

        jPanel_SkinColorB.setBackground(new java.awt.Color(0, 0, 255));
        jPanel_SkinColorB.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_SkinColorB.setMaximumSize(new java.awt.Dimension(20, 20));
        jPanel_SkinColorB.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel_SkinColorB.setPreferredSize(new java.awt.Dimension(20, 20));

        org.jdesktop.layout.GroupLayout jPanel_SkinColorBLayout = new org.jdesktop.layout.GroupLayout(jPanel_SkinColorB);
        jPanel_SkinColorB.setLayout(jPanel_SkinColorBLayout);
        jPanel_SkinColorBLayout.setHorizontalGroup(
            jPanel_SkinColorBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        jPanel_SkinColorBLayout.setVerticalGroup(
            jPanel_SkinColorBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jToolBar_SkinB.add(jPanel_SkinColorB);

        jSlider_SkinColorB.setMaximum(255);
        jSlider_SkinColorB.setValue(127);
        jSlider_SkinColorB.setPreferredSize(new java.awt.Dimension(120, 29));
        jToolBar_SkinB.add(jSlider_SkinColorB);
        jToolBar_SkinB.add(jSpinner_SkinB);

        org.jdesktop.layout.GroupLayout jPanel_SkinColorLayout = new org.jdesktop.layout.GroupLayout(jPanel_SkinColor);
        jPanel_SkinColor.setLayout(jPanel_SkinColorLayout);
        jPanel_SkinColorLayout.setHorizontalGroup(
            jPanel_SkinColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_SkinColorLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(jPanel_SkinColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar_SkinR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_SkinG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_SkinB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
        );
        jPanel_SkinColorLayout.setVerticalGroup(
            jPanel_SkinColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_SkinColorLayout.createSequentialGroup()
                .add(jToolBar_SkinR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_SkinG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_SkinB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel_ColorsLayout = new org.jdesktop.layout.GroupLayout(jPanel_Colors);
        jPanel_Colors.setLayout(jPanel_ColorsLayout);
        jPanel_ColorsLayout.setHorizontalGroup(
            jPanel_ColorsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_ColorsLayout.createSequentialGroup()
                .add(jPanel_ColorsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel_EyeColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel_HairColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel_SkinColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_ColorsLayout.setVerticalGroup(
            jPanel_ColorsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_ColorsLayout.createSequentialGroup()
                .add(jPanel_SkinColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_HairColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_EyeColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        jScrollPanel_Colors.setViewportView(jPanel_Colors);

        jTabbedPane_Options.addTab("Color", jScrollPanel_Colors);

        jPanel_Head.setMaximumSize(new java.awt.Dimension(253, 575));
        jPanel_Head.setPreferredSize(new java.awt.Dimension(230, 353));

        jToolBar_HeadHeight.setFloatable(false);
        jToolBar_HeadHeight.setRollover(true);

        jLabel_HeadHeight.setText("Height");
        jToolBar_HeadHeight.add(jLabel_HeadHeight);

        jSlider_HeadHeight.setMinimum(1);
        jSlider_HeadHeight.setValue(10);
        jSlider_HeadHeight.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHead(1);
            }
        });
        jToolBar_HeadHeight.add(jSlider_HeadHeight);

        jToolBar_HeadWidth.setFloatable(false);
        jToolBar_HeadWidth.setRollover(true);

        jLabel_HeadWidth.setText("Width");
        jLabel_HeadWidth.setPreferredSize(new java.awt.Dimension(42, 16));
        jToolBar_HeadWidth.add(jLabel_HeadWidth);

        jSlider_HeadWidth.setMinimum(1);
        jSlider_HeadWidth.setValue(10);
        jSlider_HeadWidth.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHead(0);
            }
        });
        jToolBar_HeadWidth.add(jSlider_HeadWidth);

        jToolBar_HeadDepth.setFloatable(false);
        jToolBar_HeadDepth.setRollover(true);

        jLabel_HeadDepth.setText("Depth");
        jLabel_HeadDepth.setPreferredSize(new java.awt.Dimension(42, 16));
        jToolBar_HeadDepth.add(jLabel_HeadDepth);

        jSlider_HeadDepth.setMinimum(1);
        jSlider_HeadDepth.setValue(10);
        jSlider_HeadDepth.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHead(2);
            }
        });
        jToolBar_HeadDepth.add(jSlider_HeadDepth);

        org.jdesktop.layout.GroupLayout jPanel_HeadLayout = new org.jdesktop.layout.GroupLayout(jPanel_Head);
        jPanel_Head.setLayout(jPanel_HeadLayout);
        jPanel_HeadLayout.setHorizontalGroup(
            jPanel_HeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadLayout.createSequentialGroup()
                .add(jToolBar_HeadHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel_HeadLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(jToolBar_HeadWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
            .add(jPanel_HeadLayout.createSequentialGroup()
                .add(jToolBar_HeadDepth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
        jPanel_HeadLayout.setVerticalGroup(
            jPanel_HeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadLayout.createSequentialGroup()
                .add(jToolBar_HeadHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jToolBar_HeadWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jToolBar_HeadDepth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(450, 450, 450))
        );

        jScrollPane_Head.setViewportView(jPanel_Head);

        jTabbedPane_Options.addTab("Scale - Head", jScrollPane_Head);

        jSplitPane1.setDividerLocation(80);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMaximumSize(new java.awt.Dimension(230, 353));
        jSplitPane1.setPreferredSize(new java.awt.Dimension(230, 353));

        jPanel_Body.setPreferredSize(new java.awt.Dimension(230, 353));

        jToolBar_UniformScale.setFloatable(false);
        jToolBar_UniformScale.setRollover(true);

        jLabel_UniformScale.setText("Uniform");
        jToolBar_UniformScale.add(jLabel_UniformScale);

        jSlider_UniformScale.setMaximum(40);
        jSlider_UniformScale.setMinimum(1);
        jSlider_UniformScale.setSnapToTicks(true);
        jSlider_UniformScale.setValue(10);
        jSlider_UniformScale.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleBody(2);
            }
        });
        jToolBar_UniformScale.add(jSlider_UniformScale);

        jToolBar_HeightScale.setFloatable(false);
        jToolBar_HeightScale.setRollover(true);

        jLabel_HeightScale.setText("Height");
        jLabel_HeightScale.setPreferredSize(new java.awt.Dimension(51, 16));
        jToolBar_HeightScale.add(jLabel_HeightScale);

        jSlider_Height.setMaximum(40);
        jSlider_Height.setMinimum(1);
        jSlider_Height.setValue(10);
        jSlider_Height.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleBody(0);
            }
        });
        jToolBar_HeightScale.add(jSlider_Height);

        jToolBar_WidthScale.setFloatable(false);
        jToolBar_WidthScale.setRollover(true);

        jLabel_WidthScale.setText("Width");
        jLabel_WidthScale.setPreferredSize(new java.awt.Dimension(51, 16));
        jToolBar_WidthScale.add(jLabel_WidthScale);

        jSlider_Width.setMaximum(40);
        jSlider_Width.setMinimum(1);
        jSlider_Width.setValue(10);
        jSlider_Width.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleBody(1);
            }
        });
        jToolBar_WidthScale.add(jSlider_Width);

        org.jdesktop.layout.GroupLayout jPanel_BodyLayout = new org.jdesktop.layout.GroupLayout(jPanel_Body);
        jPanel_Body.setLayout(jPanel_BodyLayout);
        jPanel_BodyLayout.setHorizontalGroup(
            jPanel_BodyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jToolBar_UniformScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_HeightScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_WidthScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel_BodyLayout.setVerticalGroup(
            jPanel_BodyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyLayout.createSequentialGroup()
                .add(jToolBar_UniformScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_HeightScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_WidthScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel_Body);

        jPanel_BodyParts.setPreferredSize(new java.awt.Dimension(230, 200));

        jToolBar_ArmLength.setFloatable(false);
        jToolBar_ArmLength.setRollover(true);

        jLabel_ArmLength.setText("Arm Length");
        jToolBar_ArmLength.add(jLabel_ArmLength);

        jSlider_ArmLength.setMaximum(40);
        jSlider_ArmLength.setMinimum(1);
        jSlider_ArmLength.setValue(10);
        jSlider_ArmLength.setName("Arm Length"); // NOI18N
        jSlider_ArmLength.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleArm(0);
            }
        });
        jToolBar_ArmLength.add(jSlider_ArmLength);

        jToolBar_UpperArm.setFloatable(false);
        jToolBar_UpperArm.setRollover(true);

        jLabel_UpperArm.setText("Upper Arm");
        jLabel_UpperArm.setPreferredSize(new java.awt.Dimension(73, 16));
        jToolBar_UpperArm.add(jLabel_UpperArm);

        jSlider_UpperArm.setMaximum(40);
        jSlider_UpperArm.setMinimum(1);
        jSlider_UpperArm.setValue(10);
        jSlider_UpperArm.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleArm(1);
            }
        });
        jToolBar_UpperArm.add(jSlider_UpperArm);

        jToolBar_Forearms.setFloatable(false);
        jToolBar_Forearms.setRollover(true);

        jLabel_Forearms.setText("Forearms");
        jLabel_Forearms.setPreferredSize(new java.awt.Dimension(73, 16));
        jToolBar_Forearms.add(jLabel_Forearms);

        jSlider_Forearms.setMaximum(40);
        jSlider_Forearms.setMinimum(1);
        jSlider_Forearms.setValue(10);
        jSlider_Forearms.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleArm(2);
            }
        });
        jToolBar_Forearms.add(jSlider_Forearms);

        jToolBar_HandSize.setFloatable(false);
        jToolBar_HandSize.setRollover(true);

        jLabel_HandSize.setText("Hand Size");
        jLabel_HandSize.setPreferredSize(new java.awt.Dimension(73, 16));
        jToolBar_HandSize.add(jLabel_HandSize);

        jSlider_HandSize.setMaximum(40);
        jSlider_HandSize.setMinimum(1);
        jSlider_HandSize.setValue(10);
        jSlider_HandSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHands();
            }
        });
        jToolBar_HandSize.add(jSlider_HandSize);

        jToolBar_LegLength.setFloatable(false);
        jToolBar_LegLength.setRollover(true);

        jLabel_LegLength.setText("Leg Length");
        jLabel_LegLength.setPreferredSize(new java.awt.Dimension(73, 16));
        jToolBar_LegLength.add(jLabel_LegLength);

        jSlider_LegLength.setMaximum(40);
        jSlider_LegLength.setMinimum(1);
        jSlider_LegLength.setValue(10);
        jSlider_LegLength.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleLeg(0);
            }
        });
        jToolBar_LegLength.add(jSlider_LegLength);

        jToolBar_Thighs.setFloatable(false);
        jToolBar_Thighs.setRollover(true);

        jLabel_Thighs.setText("Thighs");
        jLabel_Thighs.setPreferredSize(new java.awt.Dimension(80, 16));
        jToolBar_Thighs.add(jLabel_Thighs);

        jSlider_Thighs.setMaximum(40);
        jSlider_Thighs.setMinimum(1);
        jSlider_Thighs.setValue(10);
        jSlider_Thighs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleLeg(1);
            }
        });
        jToolBar_Thighs.add(jSlider_Thighs);

        jToolBar_Calfs.setFloatable(false);
        jToolBar_Calfs.setRollover(true);

        jLabel_Calfs.setText("Calfs");
        jLabel_Calfs.setPreferredSize(new java.awt.Dimension(80, 16));
        jToolBar_Calfs.add(jLabel_Calfs);

        jSlider_Calfs.setMaximum(40);
        jSlider_Calfs.setMinimum(1);
        jSlider_Calfs.setValue(10);
        jSlider_Calfs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleLeg(2);
            }
        });
        jToolBar_Calfs.add(jSlider_Calfs);

        jToolBar_FootSize.setFloatable(false);
        jToolBar_FootSize.setRollover(true);

        jLabel_FootSize.setText("Foot Size");
        jLabel_FootSize.setPreferredSize(new java.awt.Dimension(73, 16));
        jToolBar_FootSize.add(jLabel_FootSize);

        jSlider_FootSize.setMaximum(40);
        jSlider_FootSize.setMinimum(1);
        jSlider_FootSize.setValue(10);
        jSlider_FootSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleFeet();
            }
        });
        jToolBar_FootSize.add(jSlider_FootSize);

        org.jdesktop.layout.GroupLayout jPanel_BodyPartsLayout = new org.jdesktop.layout.GroupLayout(jPanel_BodyParts);
        jPanel_BodyParts.setLayout(jPanel_BodyPartsLayout);
        jPanel_BodyPartsLayout.setHorizontalGroup(
            jPanel_BodyPartsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyPartsLayout.createSequentialGroup()
                .add(jPanel_BodyPartsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar_Thighs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_ArmLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_UpperArm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_Forearms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_HandSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_LegLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_Calfs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_FootSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_BodyPartsLayout.setVerticalGroup(
            jPanel_BodyPartsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyPartsLayout.createSequentialGroup()
                .add(jToolBar_ArmLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(jToolBar_UpperArm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Forearms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_HandSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_LegLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Thighs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Calfs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_FootSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(323, 323, 323))
        );

        jSplitPane1.setRightComponent(jPanel_BodyParts);

        jScrollPane_Body.setViewportView(jSplitPane1);

        jTabbedPane_Options.addTab("Scale - Body", jScrollPane_Body);

        jScrollPanel_Details.setPreferredSize(new java.awt.Dimension(234, 353));

        jSplitPane2.setDividerLocation(240);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setPreferredSize(new java.awt.Dimension(210, 363));

        jPanel_BodyDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Body Details", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanel_BodyDetails.setAlignmentX(0.0F);
        jPanel_BodyDetails.setAlignmentY(0.0F);
        jPanel_BodyDetails.setPreferredSize(new java.awt.Dimension(230, 353));

        jToolBar_BodyScars.setFloatable(false);
        jToolBar_BodyScars.setRollover(true);

        jLabel_BodyScars.setText("Scars");
        jLabel_BodyScars.setPreferredSize(new java.awt.Dimension(57, 16));
        jToolBar_BodyScars.add(jLabel_BodyScars);
        jToolBar_BodyScars.add(jSpinner_BodyScars);

        jToolBar_BodyTattoos.setFloatable(false);
        jToolBar_BodyTattoos.setRollover(true);

        jLabel_BodyTattoos.setText("Tattoos");
        jLabel_BodyTattoos.setPreferredSize(new java.awt.Dimension(57, 16));
        jToolBar_BodyTattoos.add(jLabel_BodyTattoos);
        jToolBar_BodyTattoos.add(jSpinner_BodyTattoos);

        jToolBar_BodyPiercings.setFloatable(false);
        jToolBar_BodyPiercings.setRollover(true);

        jLabel_BodyPiercings.setText("Piercings");
        jToolBar_BodyPiercings.add(jLabel_BodyPiercings);
        jToolBar_BodyPiercings.add(jSpinner_BodyPiercings);

        org.jdesktop.layout.GroupLayout jPanel_BodyDetailsLayout = new org.jdesktop.layout.GroupLayout(jPanel_BodyDetails);
        jPanel_BodyDetails.setLayout(jPanel_BodyDetailsLayout);
        jPanel_BodyDetailsLayout.setHorizontalGroup(
            jPanel_BodyDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyDetailsLayout.createSequentialGroup()
                .add(jPanel_BodyDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar_BodyTattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_BodyPiercings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel_BodyDetailsLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(jToolBar_BodyScars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
        jPanel_BodyDetailsLayout.setVerticalGroup(
            jPanel_BodyDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyDetailsLayout.createSequentialGroup()
                .add(jToolBar_BodyScars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jToolBar_BodyTattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jToolBar_BodyPiercings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(365, 365, 365))
        );

        jSplitPane2.setRightComponent(jPanel_BodyDetails);

        jPanel_HeadDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Head Details", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanel_HeadDetails.setAlignmentX(0.0F);
        jPanel_HeadDetails.setAlignmentY(0.0F);
        jPanel_HeadDetails.setPreferredSize(new java.awt.Dimension(230, 353));

        jToolBar_HairStyle.setFloatable(false);
        jToolBar_HairStyle.setRollover(true);

        jLabel_HairStyle.setText("Hair Style");
        jLabel_HairStyle.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar_HairStyle.add(jLabel_HairStyle);
        jToolBar_HairStyle.add(jSpinner_HairStyle);

        jToolBar_FacialHair.setFloatable(false);
        jToolBar_FacialHair.setRollover(true);

        jLabel_FacialHair.setText("Facial Hair");
        jToolBar_FacialHair.add(jLabel_FacialHair);
        jToolBar_FacialHair.add(jSpinner_FacialHair);

        jToolBar_Ears.setFloatable(false);
        jToolBar_Ears.setRollover(true);

        jLabel_Ears.setText("Ears");
        jLabel_Ears.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar_Ears.add(jLabel_Ears);
        jToolBar_Ears.add(jSpinner_EarPiercing);

        jToolBar_Nose.setFloatable(false);
        jToolBar_Nose.setRollover(true);

        jLabel_Nose.setText("Nose");
        jLabel_Nose.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar_Nose.add(jLabel_Nose);
        jToolBar_Nose.add(jSpinner_NosePiercing);

        jToolBar_Eyes.setFloatable(false);
        jToolBar_Eyes.setRollover(true);

        jLabel_Eyes.setText("Eyes");
        jLabel_Eyes.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar_Eyes.add(jLabel_Eyes);
        jToolBar_Eyes.add(jSpinner_EyePiercings);

        jToolBar_Lips.setFloatable(false);
        jToolBar_Lips.setRollover(true);

        jLabel_Lips.setText("Lips");
        jLabel_Lips.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar_Lips.add(jLabel_Lips);
        jToolBar_Lips.add(jSpinner_LipPiercing);

        jToolBar_Tattoos.setFloatable(false);
        jToolBar_Tattoos.setRollover(true);

        jLabel_Tattoos.setText("Tattoos");
        jLabel_Tattoos.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar_Tattoos.add(jLabel_Tattoos);
        jToolBar_Tattoos.add(jSpinner_Tattoos);

        jToolBar_Scars.setFloatable(false);
        jToolBar_Scars.setRollover(true);

        jLabel_Scars.setText("Scars");
        jLabel_Scars.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar_Scars.add(jLabel_Scars);
        jToolBar_Scars.add(jSpinner_Scars);

        org.jdesktop.layout.GroupLayout jPanel_HeadDetailsLayout = new org.jdesktop.layout.GroupLayout(jPanel_HeadDetails);
        jPanel_HeadDetails.setLayout(jPanel_HeadDetailsLayout);
        jPanel_HeadDetailsLayout.setHorizontalGroup(
            jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                        .add(0, 0, 0)
                        .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jToolBar_Ears, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jToolBar_FacialHair, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jToolBar_Nose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jToolBar_Eyes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jToolBar_Lips, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jToolBar_Tattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jToolBar_Scars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jToolBar_HairStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_HeadDetailsLayout.setVerticalGroup(
            jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                .add(jToolBar_HairStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jToolBar_FacialHair, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jToolBar_Ears, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Nose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Eyes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Lips, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Tattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Scars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setTopComponent(jPanel_HeadDetails);

        jScrollPanel_Details.setViewportView(jSplitPane2);

        jTabbedPane_Options.addTab("Details", jScrollPanel_Details);

        jPanel_Shirts.setPreferredSize(new java.awt.Dimension(234, 357));

        org.jdesktop.layout.GroupLayout jPanel_ShirtsLayout = new org.jdesktop.layout.GroupLayout(jPanel_Shirts);
        jPanel_Shirts.setLayout(jPanel_ShirtsLayout);
        jPanel_ShirtsLayout.setHorizontalGroup(
            jPanel_ShirtsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 234, Short.MAX_VALUE)
        );
        jPanel_ShirtsLayout.setVerticalGroup(
            jPanel_ShirtsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 372, Short.MAX_VALUE)
        );

        jTabbedPane_Options.addTab("Shirts", jPanel_Shirts);

        jPanel_Pants.setPreferredSize(new java.awt.Dimension(234, 357));

        org.jdesktop.layout.GroupLayout jPanel_PantsLayout = new org.jdesktop.layout.GroupLayout(jPanel_Pants);
        jPanel_Pants.setLayout(jPanel_PantsLayout);
        jPanel_PantsLayout.setHorizontalGroup(
            jPanel_PantsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 234, Short.MAX_VALUE)
        );
        jPanel_PantsLayout.setVerticalGroup(
            jPanel_PantsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 372, Short.MAX_VALUE)
        );

        jTabbedPane_Options.addTab("Pants", jPanel_Pants);

        jPanel_Dresses.setPreferredSize(new java.awt.Dimension(234, 357));

        org.jdesktop.layout.GroupLayout jPanel_DressesLayout = new org.jdesktop.layout.GroupLayout(jPanel_Dresses);
        jPanel_Dresses.setLayout(jPanel_DressesLayout);
        jPanel_DressesLayout.setHorizontalGroup(
            jPanel_DressesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 234, Short.MAX_VALUE)
        );
        jPanel_DressesLayout.setVerticalGroup(
            jPanel_DressesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 372, Short.MAX_VALUE)
        );

        jTabbedPane_Options.addTab("Dresses", jPanel_Dresses);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_Name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel_Gender, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jTabbedPane_Options, 0, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel_Name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_Gender, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane_Options, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 418, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OptionsGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_Gender;
    private javax.swing.JLabel jLabel_ArmLength;
    private javax.swing.JLabel jLabel_BodyPiercings;
    private javax.swing.JLabel jLabel_BodyScars;
    private javax.swing.JLabel jLabel_BodyTattoos;
    private javax.swing.JLabel jLabel_Calfs;
    private javax.swing.JLabel jLabel_Ears;
    private javax.swing.JLabel jLabel_Eyes;
    private javax.swing.JLabel jLabel_FacialHair;
    private javax.swing.JLabel jLabel_FootSize;
    private javax.swing.JLabel jLabel_Forearms;
    private javax.swing.JLabel jLabel_HairStyle;
    private javax.swing.JLabel jLabel_HandSize;
    private javax.swing.JLabel jLabel_HeadDepth;
    private javax.swing.JLabel jLabel_HeadHeight;
    private javax.swing.JLabel jLabel_HeadWidth;
    private javax.swing.JLabel jLabel_HeightScale;
    private javax.swing.JLabel jLabel_LegLength;
    private javax.swing.JLabel jLabel_Lips;
    private javax.swing.JLabel jLabel_Nose;
    private javax.swing.JLabel jLabel_Scars;
    private javax.swing.JLabel jLabel_Tattoos;
    private javax.swing.JLabel jLabel_Thighs;
    private javax.swing.JLabel jLabel_UniformScale;
    private javax.swing.JLabel jLabel_UpperArm;
    private javax.swing.JLabel jLabel_WidthScale;
    private javax.swing.JPanel jPanel_Body;
    private javax.swing.JPanel jPanel_BodyDetails;
    private javax.swing.JPanel jPanel_BodyParts;
    private javax.swing.JPanel jPanel_Colors;
    private javax.swing.JPanel jPanel_Dresses;
    private javax.swing.JPanel jPanel_EyeColor;
    private javax.swing.JPanel jPanel_EyeColorB;
    private javax.swing.JPanel jPanel_EyeColorG;
    private javax.swing.JPanel jPanel_EyeColorR;
    private javax.swing.JPanel jPanel_Gender;
    private javax.swing.JPanel jPanel_HairColor;
    private javax.swing.JPanel jPanel_HairColorB;
    private javax.swing.JPanel jPanel_HairColorG;
    private javax.swing.JPanel jPanel_HairColorR;
    private javax.swing.JPanel jPanel_Head;
    private javax.swing.JPanel jPanel_HeadDetails;
    private javax.swing.JPanel jPanel_Name;
    private javax.swing.JPanel jPanel_Pants;
    private javax.swing.JPanel jPanel_Shirts;
    private javax.swing.JPanel jPanel_SkinColor;
    private javax.swing.JPanel jPanel_SkinColorB;
    private javax.swing.JPanel jPanel_SkinColorG;
    private javax.swing.JPanel jPanel_SkinColorR;
    private javax.swing.JRadioButton jRadioButton_GenderF;
    private javax.swing.JRadioButton jRadioButton_GenderM;
    private javax.swing.JScrollPane jScrollPane_Body;
    private javax.swing.JScrollPane jScrollPane_Head;
    private javax.swing.JScrollPane jScrollPanel_Colors;
    private javax.swing.JScrollPane jScrollPanel_Details;
    private javax.swing.JSlider jSlider_ArmLength;
    private javax.swing.JSlider jSlider_Calfs;
    private javax.swing.JSlider jSlider_EyeColorB;
    private javax.swing.JSlider jSlider_EyeColorG;
    private javax.swing.JSlider jSlider_EyeColorR;
    private javax.swing.JSlider jSlider_FootSize;
    private javax.swing.JSlider jSlider_Forearms;
    private javax.swing.JSlider jSlider_HairColorB;
    private javax.swing.JSlider jSlider_HairColorG;
    private javax.swing.JSlider jSlider_HairColorR;
    private javax.swing.JSlider jSlider_HandSize;
    private javax.swing.JSlider jSlider_HeadDepth;
    private javax.swing.JSlider jSlider_HeadHeight;
    private javax.swing.JSlider jSlider_HeadWidth;
    private javax.swing.JSlider jSlider_Height;
    private javax.swing.JSlider jSlider_LegLength;
    private javax.swing.JSlider jSlider_SkinColorB;
    private javax.swing.JSlider jSlider_SkinColorG;
    private javax.swing.JSlider jSlider_SkinColorR;
    private javax.swing.JSlider jSlider_Thighs;
    private javax.swing.JSlider jSlider_UniformScale;
    private javax.swing.JSlider jSlider_UpperArm;
    private javax.swing.JSlider jSlider_Width;
    private javax.swing.JSpinner jSpinner_BodyPiercings;
    private javax.swing.JSpinner jSpinner_BodyScars;
    private javax.swing.JSpinner jSpinner_BodyTattoos;
    private javax.swing.JSpinner jSpinner_EarPiercing;
    private javax.swing.JSpinner jSpinner_EyeB;
    private javax.swing.JSpinner jSpinner_EyeG;
    private javax.swing.JSpinner jSpinner_EyePiercings;
    private javax.swing.JSpinner jSpinner_EyeR;
    private javax.swing.JSpinner jSpinner_FacialHair;
    private javax.swing.JSpinner jSpinner_HairB;
    private javax.swing.JSpinner jSpinner_HairG;
    private javax.swing.JSpinner jSpinner_HairR;
    private javax.swing.JSpinner jSpinner_HairStyle;
    private javax.swing.JSpinner jSpinner_LipPiercing;
    private javax.swing.JSpinner jSpinner_NosePiercing;
    private javax.swing.JSpinner jSpinner_Scars;
    private javax.swing.JSpinner jSpinner_SkinB;
    private javax.swing.JSpinner jSpinner_SkinG;
    private javax.swing.JSpinner jSpinner_SkinR;
    private javax.swing.JSpinner jSpinner_Tattoos;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane_Options;
    private javax.swing.JTextField jTextField_Name;
    private javax.swing.JToolBar jToolBar_ArmLength;
    private javax.swing.JToolBar jToolBar_BodyPiercings;
    private javax.swing.JToolBar jToolBar_BodyScars;
    private javax.swing.JToolBar jToolBar_BodyTattoos;
    private javax.swing.JToolBar jToolBar_Calfs;
    private javax.swing.JToolBar jToolBar_Ears;
    private javax.swing.JToolBar jToolBar_EyeB;
    private javax.swing.JToolBar jToolBar_EyeG;
    private javax.swing.JToolBar jToolBar_EyeR;
    private javax.swing.JToolBar jToolBar_Eyes;
    private javax.swing.JToolBar jToolBar_FacialHair;
    private javax.swing.JToolBar jToolBar_FootSize;
    private javax.swing.JToolBar jToolBar_Forearms;
    private javax.swing.JToolBar jToolBar_HairB;
    private javax.swing.JToolBar jToolBar_HairG;
    private javax.swing.JToolBar jToolBar_HairR;
    private javax.swing.JToolBar jToolBar_HairStyle;
    private javax.swing.JToolBar jToolBar_HandSize;
    private javax.swing.JToolBar jToolBar_HeadDepth;
    private javax.swing.JToolBar jToolBar_HeadHeight;
    private javax.swing.JToolBar jToolBar_HeadWidth;
    private javax.swing.JToolBar jToolBar_HeightScale;
    private javax.swing.JToolBar jToolBar_LegLength;
    private javax.swing.JToolBar jToolBar_Lips;
    private javax.swing.JToolBar jToolBar_Nose;
    private javax.swing.JToolBar jToolBar_Scars;
    private javax.swing.JToolBar jToolBar_SkinB;
    private javax.swing.JToolBar jToolBar_SkinG;
    private javax.swing.JToolBar jToolBar_SkinR;
    private javax.swing.JToolBar jToolBar_Tattoos;
    private javax.swing.JToolBar jToolBar_Thighs;
    private javax.swing.JToolBar jToolBar_UniformScale;
    private javax.swing.JToolBar jToolBar_UpperArm;
    private javax.swing.JToolBar jToolBar_WidthScale;
    // End of variables declaration//GEN-END:variables

}
