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
        } else {
            setDefaultHeadSliders();
            setDefaultBodySliders();
        }
        getGroupings();
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
        
        if (upperArm != null) {
            upperArm.getLocalModifierMatrix().getScale(vScaleArmsU);
            jSlider_ArmLength.setValue((int) (vScaleArmsU.getY() * 10));
            jSlider_UpperArm.setValue((int) (vScaleArmsU.getZ() * 10));
        } else {
            jSlider_ArmLength.setValue(10);
            jSlider_UpperArm.setValue(10);
        }
        
        if (foreArm != null) {
            foreArm.getLocalModifierMatrix().getScale(vScaleArmsL);
            jSlider_Forearms.setValue((int) (vScaleArmsL.getZ() * 10));
        } else {
            jSlider_Forearms.setValue(10);
        }
        
        if (hands != null) {
            hands.getLocalModifierMatrix().getScale(vScaleHands);
            jSlider_HandSize.setValue((int) (vScaleHands.getX() * 10));
        } else {
            jSlider_HandSize.setValue(10);
        }
        
        if (thighs != null) {
            thighs.getLocalModifierMatrix().getScale(vScaleLegsU);
            jSlider_LegLength.setValue((int) (vScaleLegsU.getY() * 10));
            jSlider_Thighs.setValue((int) (vScaleLegsU.getZ() * 10));
        } else {
            jSlider_LegLength.setValue(10);
            jSlider_Thighs.setValue(10);
        }
        
        if (calvs != null) {
            calvs.getLocalModifierMatrix().getScale(vScaleLegsL);
            jSlider_Calfs.setValue((int) (vScaleLegsL.getZ() * 10));
        } else {
            jSlider_Calfs.setValue(10);
        }
        
        if (feet != null) {
            feet.getLocalModifierMatrix().getScale(vScaleFeet);
            jSlider_FootSize.setValue((int) (vScaleFeet.getX() * 10));
        } else {
            jSlider_FootSize.setValue(10);
        }
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
        jPanel_Head = new javax.swing.JPanel();
        jPanel_HeadSize = new javax.swing.JPanel();
        jSlider_HeadHeight = new javax.swing.JSlider();
        jSlider_HeadWidth = new javax.swing.JSlider();
        jSlider_HeadDepth = new javax.swing.JSlider();
        jPanel_Body = new javax.swing.JPanel();
        jPanel_BodyParts = new javax.swing.JPanel();
        jSlider_ArmLength = new javax.swing.JSlider();
        jSlider_UpperArm = new javax.swing.JSlider();
        jSlider_Forearms = new javax.swing.JSlider();
        jSlider_HandSize = new javax.swing.JSlider();
        jSlider_LegLength = new javax.swing.JSlider();
        jSlider_FootSize = new javax.swing.JSlider();
        jSlider_Thighs = new javax.swing.JSlider();
        jSlider_Calfs = new javax.swing.JSlider();
        jSlider_UniformScale = new javax.swing.JSlider();
        jSlider_Height = new javax.swing.JSlider();
        jSlider_Width = new javax.swing.JSlider();
        jPanel_Details = new javax.swing.JPanel();
        jPanel_HeadDetails = new javax.swing.JPanel();
        jSpinner_HairStyle = new javax.swing.JSpinner();
        jSpinner_EarPiercing = new javax.swing.JSpinner();
        jSpinner_FacialHair = new javax.swing.JSpinner();
        jSpinner_NosePiercing = new javax.swing.JSpinner();
        jSpinner_Tattoos = new javax.swing.JSpinner();
        jSpinner_LipPiercing = new javax.swing.JSpinner();
        jSpinner_Scars = new javax.swing.JSpinner();
        jSpinner_EyePiercings = new javax.swing.JSpinner();
        jPanel_BodyDetails = new javax.swing.JPanel();
        jSpinner_BodyScars = new javax.swing.JSpinner();
        jSpinner_BodyTattoos = new javax.swing.JSpinner();
        jSpinner_BodyPiercings = new javax.swing.JSpinner();
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
                .add(jTextField_Name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
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
                .addContainerGap(107, Short.MAX_VALUE))
        );
        jPanel_GenderLayout.setVerticalGroup(
            jPanel_GenderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_GenderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jRadioButton_GenderM)
                .add(jRadioButton_GenderF))
        );

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

        jPanel_SkinColor.setBorder(javax.swing.BorderFactory.createTitledBorder("Skin/Body Color"));

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
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel_ColorsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel_ColorsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jPanel_SkinColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel_HairColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel_EyeColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel_ColorsLayout.setVerticalGroup(
            jPanel_ColorsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_ColorsLayout.createSequentialGroup()
                .add(jPanel_SkinColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_HairColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_EyeColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(372, Short.MAX_VALUE))
        );

        jTabbedPane_Options.addTab("Colors", jPanel_Colors);

        jPanel_HeadSize.setBorder(javax.swing.BorderFactory.createTitledBorder("Head Size - Ninja Only"));

        jSlider_HeadHeight.setMinimum(1);
        jSlider_HeadHeight.setValue(10);
        jSlider_HeadHeight.setBorder(javax.swing.BorderFactory.createTitledBorder("Height"));
        jSlider_HeadHeight.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHead(1);
            }
        });

        jSlider_HeadWidth.setMinimum(1);
        jSlider_HeadWidth.setValue(10);
        jSlider_HeadWidth.setBorder(javax.swing.BorderFactory.createTitledBorder("Width"));
        jSlider_HeadWidth.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHead(0);
            }
        });

        jSlider_HeadDepth.setMinimum(1);
        jSlider_HeadDepth.setValue(10);
        jSlider_HeadDepth.setBorder(javax.swing.BorderFactory.createTitledBorder("Depth"));
        jSlider_HeadDepth.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHead(2);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel_HeadSizeLayout = new org.jdesktop.layout.GroupLayout(jPanel_HeadSize);
        jPanel_HeadSize.setLayout(jPanel_HeadSizeLayout);
        jPanel_HeadSizeLayout.setHorizontalGroup(
            jPanel_HeadSizeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadSizeLayout.createSequentialGroup()
                .add(jPanel_HeadSizeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel_HeadSizeLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel_HeadSizeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSlider_HeadHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jSlider_HeadWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel_HeadSizeLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jSlider_HeadDepth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel_HeadSizeLayout.setVerticalGroup(
            jPanel_HeadSizeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadSizeLayout.createSequentialGroup()
                .add(jSlider_HeadHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSlider_HeadWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSlider_HeadDepth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel_HeadLayout = new org.jdesktop.layout.GroupLayout(jPanel_Head);
        jPanel_Head.setLayout(jPanel_HeadLayout);
        jPanel_HeadLayout.setHorizontalGroup(
            jPanel_HeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel_HeadSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_HeadLayout.setVerticalGroup(
            jPanel_HeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadLayout.createSequentialGroup()
                .add(jPanel_HeadSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(474, Short.MAX_VALUE))
        );

        jTabbedPane_Options.addTab("Scale - Head", jPanel_Head);

        jPanel_BodyParts.setBorder(javax.swing.BorderFactory.createTitledBorder("Body Parts - Ninja Only"));

        jSlider_ArmLength.setMaximum(40);
        jSlider_ArmLength.setMinimum(1);
        jSlider_ArmLength.setValue(10);
        jSlider_ArmLength.setBorder(javax.swing.BorderFactory.createTitledBorder("Arm Length"));
        jSlider_ArmLength.setName("Arm Length"); // NOI18N
        jSlider_ArmLength.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleArm(0);
            }
        });

        jSlider_UpperArm.setMaximum(40);
        jSlider_UpperArm.setMinimum(1);
        jSlider_UpperArm.setValue(10);
        jSlider_UpperArm.setBorder(javax.swing.BorderFactory.createTitledBorder("Upper Arm"));
        jSlider_UpperArm.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleArm(1);
            }
        });

        jSlider_Forearms.setMaximum(40);
        jSlider_Forearms.setMinimum(1);
        jSlider_Forearms.setValue(10);
        jSlider_Forearms.setBorder(javax.swing.BorderFactory.createTitledBorder("Forearms"));
        jSlider_Forearms.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleArm(2);
            }
        });

        jSlider_HandSize.setMaximum(40);
        jSlider_HandSize.setMinimum(1);
        jSlider_HandSize.setValue(10);
        jSlider_HandSize.setBorder(javax.swing.BorderFactory.createTitledBorder("Hand Size"));
        jSlider_HandSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleHands();
            }
        });

        jSlider_LegLength.setMaximum(40);
        jSlider_LegLength.setMinimum(1);
        jSlider_LegLength.setValue(10);
        jSlider_LegLength.setBorder(javax.swing.BorderFactory.createTitledBorder("Leg Length"));
        jSlider_LegLength.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleLeg(0);
            }
        });

        jSlider_FootSize.setMaximum(40);
        jSlider_FootSize.setMinimum(1);
        jSlider_FootSize.setValue(10);
        jSlider_FootSize.setBorder(javax.swing.BorderFactory.createTitledBorder("Foot Size"));
        jSlider_FootSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleFeet();
            }
        });

        jSlider_Thighs.setMaximum(40);
        jSlider_Thighs.setMinimum(1);
        jSlider_Thighs.setValue(10);
        jSlider_Thighs.setBorder(javax.swing.BorderFactory.createTitledBorder("Thighs"));
        jSlider_Thighs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleLeg(1);
            }
        });

        jSlider_Calfs.setMaximum(40);
        jSlider_Calfs.setMinimum(1);
        jSlider_Calfs.setValue(10);
        jSlider_Calfs.setBorder(javax.swing.BorderFactory.createTitledBorder("Calfs"));
        jSlider_Calfs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleLeg(2);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel_BodyPartsLayout = new org.jdesktop.layout.GroupLayout(jPanel_BodyParts);
        jPanel_BodyParts.setLayout(jPanel_BodyPartsLayout);
        jPanel_BodyPartsLayout.setHorizontalGroup(
            jPanel_BodyPartsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyPartsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel_BodyPartsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSlider_ArmLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_UpperArm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_Forearms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_HandSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_LegLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_Thighs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_Calfs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_FootSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel_BodyPartsLayout.setVerticalGroup(
            jPanel_BodyPartsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyPartsLayout.createSequentialGroup()
                .add(jSlider_ArmLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jSlider_UpperArm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jSlider_Forearms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jSlider_HandSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jSlider_LegLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jSlider_Thighs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jSlider_Calfs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSlider_FootSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSlider_UniformScale.setMaximum(40);
        jSlider_UniformScale.setMinimum(1);
        jSlider_UniformScale.setSnapToTicks(true);
        jSlider_UniformScale.setValue(10);
        jSlider_UniformScale.setBorder(javax.swing.BorderFactory.createTitledBorder("Uniform Scale"));
        jSlider_UniformScale.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleBody(2);
            }
        });

        jSlider_Height.setMaximum(40);
        jSlider_Height.setMinimum(1);
        jSlider_Height.setValue(10);
        jSlider_Height.setBorder(javax.swing.BorderFactory.createTitledBorder("Height"));
        jSlider_Height.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleBody(0);
            }
        });

        jSlider_Width.setMaximum(40);
        jSlider_Width.setMinimum(1);
        jSlider_Width.setValue(10);
        jSlider_Width.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Width"));
        jSlider_Width.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                scaleBody(1);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel_BodyLayout = new org.jdesktop.layout.GroupLayout(jPanel_Body);
        jPanel_Body.setLayout(jPanel_BodyLayout);
        jPanel_BodyLayout.setHorizontalGroup(
            jPanel_BodyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel_BodyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel_BodyLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSlider_Width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jSlider_UniformScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSlider_Height, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel_BodyParts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_BodyLayout.setVerticalGroup(
            jPanel_BodyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyLayout.createSequentialGroup()
                .add(jSlider_UniformScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSlider_Height, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSlider_Width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_BodyParts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane_Options.addTab("Scale - Body", jPanel_Body);

        jPanel_HeadDetails.setBorder(javax.swing.BorderFactory.createTitledBorder("Head Details"));
        jPanel_HeadDetails.setAlignmentX(0.0F);
        jPanel_HeadDetails.setAlignmentY(0.0F);

        jSpinner_HairStyle.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hair Style", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_EarPiercing.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ears", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_FacialHair.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Facial Hair", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_NosePiercing.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nose", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_Tattoos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tattoos", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_LipPiercing.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lips", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_Scars.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scars", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_EyePiercings.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Eyes", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        org.jdesktop.layout.GroupLayout jPanel_HeadDetailsLayout = new org.jdesktop.layout.GroupLayout(jPanel_HeadDetails);
        jPanel_HeadDetails.setLayout(jPanel_HeadDetailsLayout);
        jPanel_HeadDetailsLayout.setHorizontalGroup(
            jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                        .add(jSpinner_HairStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSpinner_EarPiercing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                        .add(jSpinner_FacialHair, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSpinner_NosePiercing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                        .add(jSpinner_Tattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSpinner_LipPiercing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                        .add(jSpinner_Scars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSpinner_EyePiercings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_HeadDetailsLayout.setVerticalGroup(
            jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_HeadDetailsLayout.createSequentialGroup()
                .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jSpinner_HairStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSpinner_EarPiercing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jSpinner_FacialHair, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSpinner_NosePiercing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jSpinner_Tattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSpinner_LipPiercing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_HeadDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jSpinner_Scars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSpinner_EyePiercings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jPanel_BodyDetails.setBorder(javax.swing.BorderFactory.createTitledBorder("Body Details"));
        jPanel_BodyDetails.setAlignmentX(0.0F);
        jPanel_BodyDetails.setAlignmentY(0.0F);

        jSpinner_BodyScars.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scars", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_BodyTattoos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tattoos", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jSpinner_BodyPiercings.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Piercings", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        org.jdesktop.layout.GroupLayout jPanel_BodyDetailsLayout = new org.jdesktop.layout.GroupLayout(jPanel_BodyDetails);
        jPanel_BodyDetails.setLayout(jPanel_BodyDetailsLayout);
        jPanel_BodyDetailsLayout.setHorizontalGroup(
            jPanel_BodyDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel_BodyDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSpinner_BodyPiercings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSpinner_BodyTattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSpinner_BodyScars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel_BodyDetailsLayout.setVerticalGroup(
            jPanel_BodyDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BodyDetailsLayout.createSequentialGroup()
                .add(jSpinner_BodyScars, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSpinner_BodyTattoos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSpinner_BodyPiercings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel_DetailsLayout = new org.jdesktop.layout.GroupLayout(jPanel_Details);
        jPanel_Details.setLayout(jPanel_DetailsLayout);
        jPanel_DetailsLayout.setHorizontalGroup(
            jPanel_DetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel_DetailsLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel_DetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel_BodyDetails, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel_HeadDetails, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel_DetailsLayout.setVerticalGroup(
            jPanel_DetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_DetailsLayout.createSequentialGroup()
                .add(jPanel_HeadDetails, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_BodyDetails, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(140, Short.MAX_VALUE))
        );

        jTabbedPane_Options.addTab("Details", jPanel_Details);

        org.jdesktop.layout.GroupLayout jPanel_ShirtsLayout = new org.jdesktop.layout.GroupLayout(jPanel_Shirts);
        jPanel_Shirts.setLayout(jPanel_ShirtsLayout);
        jPanel_ShirtsLayout.setHorizontalGroup(
            jPanel_ShirtsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 259, Short.MAX_VALUE)
        );
        jPanel_ShirtsLayout.setVerticalGroup(
            jPanel_ShirtsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 702, Short.MAX_VALUE)
        );

        jTabbedPane_Options.addTab("Shirts", jPanel_Shirts);

        org.jdesktop.layout.GroupLayout jPanel_PantsLayout = new org.jdesktop.layout.GroupLayout(jPanel_Pants);
        jPanel_Pants.setLayout(jPanel_PantsLayout);
        jPanel_PantsLayout.setHorizontalGroup(
            jPanel_PantsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 259, Short.MAX_VALUE)
        );
        jPanel_PantsLayout.setVerticalGroup(
            jPanel_PantsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 702, Short.MAX_VALUE)
        );

        jTabbedPane_Options.addTab("Pants", jPanel_Pants);

        org.jdesktop.layout.GroupLayout jPanel_DressesLayout = new org.jdesktop.layout.GroupLayout(jPanel_Dresses);
        jPanel_Dresses.setLayout(jPanel_DressesLayout);
        jPanel_DressesLayout.setHorizontalGroup(
            jPanel_DressesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 259, Short.MAX_VALUE)
        );
        jPanel_DressesLayout.setVerticalGroup(
            jPanel_DressesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 702, Short.MAX_VALUE)
        );

        jTabbedPane_Options.addTab("Dresses", jPanel_Dresses);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_Name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jPanel_Gender, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jTabbedPane_Options, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel_Name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_Gender, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane_Options, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 748, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
    private javax.swing.JPanel jPanel_Body;
    private javax.swing.JPanel jPanel_BodyDetails;
    private javax.swing.JPanel jPanel_BodyParts;
    private javax.swing.JPanel jPanel_Colors;
    private javax.swing.JPanel jPanel_Details;
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
    private javax.swing.JPanel jPanel_HeadSize;
    private javax.swing.JPanel jPanel_Name;
    private javax.swing.JPanel jPanel_Pants;
    private javax.swing.JPanel jPanel_Shirts;
    private javax.swing.JPanel jPanel_SkinColor;
    private javax.swing.JPanel jPanel_SkinColorB;
    private javax.swing.JPanel jPanel_SkinColorG;
    private javax.swing.JPanel jPanel_SkinColorR;
    private javax.swing.JRadioButton jRadioButton_GenderF;
    private javax.swing.JRadioButton jRadioButton_GenderM;
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
    private javax.swing.JTabbedPane jTabbedPane_Options;
    private javax.swing.JTextField jTextField_Name;
    private javax.swing.JToolBar jToolBar_EyeB;
    private javax.swing.JToolBar jToolBar_EyeG;
    private javax.swing.JToolBar jToolBar_EyeR;
    private javax.swing.JToolBar jToolBar_HairB;
    private javax.swing.JToolBar jToolBar_HairG;
    private javax.swing.JToolBar jToolBar_HairR;
    private javax.swing.JToolBar jToolBar_SkinB;
    private javax.swing.JToolBar jToolBar_SkinG;
    private javax.swing.JToolBar jToolBar_SkinR;
    // End of variables declaration//GEN-END:variables

}
