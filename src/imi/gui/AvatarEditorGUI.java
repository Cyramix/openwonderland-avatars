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
package imi.gui;

import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.utils.tree.InstanceSearchProcessor;
import imi.scene.utils.tree.KeyProcessor;
import imi.scene.utils.tree.ScaleResetProcessor;
import imi.scene.utils.tree.TreeTraverser;
import java.awt.event.ItemEvent;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 * Main frame/form that contains all the tools used to edit the avatar(s)
 * @author  Paul Viet Nguyen Truong
 * @version 5.0
 */
public class AvatarEditorGUI extends javax.swing.JFrame {

    // Class Data Members
    private final SceneEssentials sceneData = new SceneEssentials();
    // Scaling Data
    private final ArrayList<String>         keys    = new ArrayList<String>();
    private final ArrayList<Vector3f>       values  = new ArrayList<Vector3f>();
    private final HashMap<String, Vector3f> scales  = new HashMap<String, Vector3f>();
    // Rotation Data
    private final PMatrix xRotation = new PMatrix();
    private final PMatrix yRotation = new PMatrix();
    private final PMatrix zRotation = new PMatrix();
    // Save Data
    private File fileXML        = null;
    private String avatarName   = new String("John Doe");
    private String avatarGender = new String("male");
    // Tools
    private TreeExplorer explorer = null;
    private OptionsGUI   options  = null;

    private final static Logger logger = Logger.getLogger(AvatarEditorGUI.class.getName());

    /**
     * Defalt constructor for the AvatarEditorGUI Class
     */
    public AvatarEditorGUI() {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
            logger.log(Level.SEVERE, "Unsupported Look & Feel Exception...");
            logger.log(Level.SEVERE, e.getMessage());
        } catch (ClassNotFoundException e) {
            // handle exception
            logger.log(Level.SEVERE, "Class Not Found Exception...");
            logger.log(Level.SEVERE, e.getMessage());
        } catch (InstantiationException e) {
            // handle exception
            logger.log(Level.SEVERE, "Instantiation Exception...");
            logger.log(Level.SEVERE, e.getMessage());
        } catch (IllegalAccessException e) {
            // handle exception
            logger.log(Level.SEVERE, "Illegal Access Exception...");
            logger.log(Level.SEVERE, e.getMessage());
        }
        initComponents();
        this.setTitle("Avatar Editor GUI");
    }

    // Accessors
    public HashMap<String, Vector3f> getScales() {
        return scales;
    }
    // Mutators
    public void setAvatarName(String name) {
        avatarName = name;
    }

    public void setAvatarGender(String gender) {
        avatarGender = gender;
    }

    // Helper Functions
    /**
     * Method called by other frames to set the set the scene information that
     * the UI uses for manipulation
     * @param jscene (JScene), worldmanager WorldManager,
     *        hiprocessors (ArrayList<ProcessorComponent>), jentity (Entity)
     */
    public void setGUI(JScene jscene, WorldManager worldmanager, ArrayList<ProcessorComponent> hiprocessors, Entity jentity) {
        sceneData.setSceneData(jscene, jscene.getPScene(), jentity, worldmanager, hiprocessors);

        // Wait until the assets are available for use
        while (sceneData.getPScene().getAssetWaitingList().size() > 0) {
            //System.out.println("Waiting to get assets...");
            Thread.yield();
        }

        setDefault();
    }

    /**
     * Resets the AvatarEditor to default values and component positions
     */
    public void setDefault() {
        resetDataMembers();
        resetGUI(0);
    }

    /**
     * Collect scaling data from the model
     */
    public void setLocalScales() {
        scales.clear();
        keys.clear();
        values.clear();
        // Retrieve new scale keys
        if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") != null) {

            KeyProcessor keyProc = new KeyProcessor();
            TreeTraverser.breadthFirst(sceneData.getPScene().getInstances().getChild(0).getChild(0).getChild(0).getChild(0), keyProc);

            keys.addAll(keyProc.getKeys());
            values.addAll(keyProc.getValues());

            // Set the scale data to default values
            for (int i = 0; i < keys.size(); i++) {
                scales.put(keys.get(i), values.get(i));
            }
        }
    }

    /**
     * Resets the speed slider based on the loaded modelinstance
     */
    public void setSpeedSlider() {
        PNode check = ((PNode)jComboBox_ModelInstances.getSelectedItem());
        if(check instanceof PPolygonSkinnedMeshInstance) {
            PPolygonSkinnedMeshInstance smInstance = ((PPolygonSkinnedMeshInstance) ((PNode) jComboBox_ModelInstances.getSelectedItem()).getChild(0));
            // BROKEN --- The animation states are now kept at the skeleton node!
            //jSlider_AnimationsQ.setValue(((int) smInstance.getAnimationState().getAnimationSpeed() * 10));
        }
    }

    /**
     * Retrieves the current model's rotation information and sets the sliders to it
     * TODO: figure out how to get the angles from a rotation matrix... Euler's Method?
     */
    public void setRotationSliders() {
        PNode check = ((PNode)jComboBox_ModelInstances.getSelectedItem());
        Matrix3f result = new Matrix3f();
        Quaternion rot = check.getChild(0).getTransform().getLocalMatrix(true).getRotation();
        rot.toRotationMatrix(result);
        
//        double zAngle = (java.lang.Math.asin(result.m10) * 180.0/java.lang.Math.PI);
//        double xAngle = (java.lang.Math.asin(result.m21) * 180.0/java.lang.Math.PI);
//        double yAngle = (java.lang.Math.asin(result.m12) * 180.0/java.lang.Math.PI);
    }
    
    /**
     * Resets the UI class data members to default values
     */
    public void resetDataMembers() {
        if (sceneData.getPScene() == null) {
            logger.log(Level.WARNING, "==================================================================");
            logger.log(Level.WARNING, "UI has not been initialized... please call setGUI from main window");
            logger.log(Level.WARNING, "==================================================================");
            return;
        }
        // Clear out old scale data
        resetLocalScales();
        // Set rotations to default values
        xRotation.setIdentity();
        yRotation.setIdentity();
        zRotation.setIdentity();
    }

    /**
     * Resets the local scale variables to default 1:1:1 values
     */
    public void resetLocalScales() {

        if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") == null) {
            logger.log(Level.WARNING, "No joints loaded yet");
            return;
        }

        Vector3f normalVector = new Vector3f(1.0f, 1.0f, 1.0f);

        scales.clear();
        keys.clear();
        values.clear();

        // Retrieve new scale keys
        KeyProcessor keyProc = new KeyProcessor();
        if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") != null) {
            TreeTraverser.breadthFirst(sceneData.getPScene().getInstances().findChild("m_TransformHierarchy").getChild(0), keyProc);
            
            keys.addAll(keyProc.getKeys());

            // Set the scale data to default values
            for (int i = 0; i < keys.size(); i++) {
                scales.put(keys.get(i), normalVector);
                values.add(normalVector);
            }
        }
    }

    /**
     * Resets the UI to default positions
     */
    public void resetGUI(int type) {
        if (type == 0) {
            // Closes all tools options
            jCheckBox_AvatarOptions.setSelected(false);
            jCheckBox_Explorer.setSelected(false);
        }

        jSlider_XAxis.setValue(180);
        jSlider_YAxis.setValue(180);
        jSlider_ZAxis.setValue(180);

        resetInstanceCount();
        resetAnimations();
    }

    /**
     * Resets the animation combo box and sets it to the models current animation
     */
    public void resetAnimations() {
        // BROKEN --- The animation states are now kept at the skeleton node!
//        if(sceneData.getPScene().getInstances().getChildrenCount() > 0) {
//            PNode smInstance = ((PNode) jComboBox_ModelInstances.getSelectedItem()).getChild(0);
//            if (smInstance != null && smInstance instanceof PPolygonSkinnedMeshInstance && (((PPolygonSkinnedMeshInstance) smInstance).getAnimationGroup() != null)) {
//                int iNumAnimations = ((PPolygonSkinnedMeshInstance) smInstance).getAnimationGroup().getCycles().length + 1;
//                int iCurrentAnim = ((PPolygonSkinnedMeshInstance) smInstance).getAnimationState().getCurrentCycle();
//                PPolygonMesh ppMesh = ((PPolygonSkinnedMeshInstance) smInstance).getGeometry();
//                String[] szAnimations = new String[iNumAnimations];
//
//                for (int i = 0; i < szAnimations.length; i++) {
//                    if (i == (szAnimations.length - 1)) {
//                        szAnimations[i] = "Stopped";
//                    } else {
//                        szAnimations[i] = ((PPolygonSkinnedMesh) ppMesh).getAnimationGroup().getCycle(i).getName();
//                    }
//                }
//
//                jComboBox_AnimationsQ.setEnabled(true);
//                jSlider_AnimationsQ.setEnabled(true);
//                jComboBox_AnimationsQ.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
//                jComboBox_AnimationsQ.setSelectedIndex(iCurrentAnim);
//            } else {
//                String[] szAnimations = new String[1];
//                szAnimations[0] = "No Animations";
//                jComboBox_AnimationsQ.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
//                jComboBox_AnimationsQ.setEnabled(false);
//                jSlider_AnimationsQ.setEnabled(false);
//                jComboBox_AnimationsQ.setSelectedIndex(0);
//            }
//        }
    }

    /**
     * Checks the PScene for all the different instances in the scene and loads
     * it up in a drop down box
     */
    public void resetInstanceCount() {
        InstanceSearchProcessor proc = new InstanceSearchProcessor();
        proc.setProcessor();

        TreeTraverser.breadthFirst(sceneData.getPScene(), proc);
        java.util.Vector<PNode> instances = proc.getModelInstances();

        jComboBox_ModelInstances.setModel(new javax.swing.DefaultComboBoxModel(instances));
    }

    /**
     * Resets the model scale to default 1:1:1 scaling
     */
    public void resetModelScales() {
        if (sceneData.getPScene().getInstances().getChildrenCount() > 0) {
            sceneData.getPScene().getInstances().getChild(0).getTransform().getLocalMatrix(true).setScale(new Vector3f(1.0f, 1.0f, 1.0f));
            if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") != null) {
                TreeTraverser.depthFirstPre(sceneData.getPScene().getInstances().getChild(0).getChild(0).getChild(0).getChild(0), new ScaleResetProcessor());
            }
        }
    }

    /**
     * Resets the model's rotations to identity
     */
    public void resetModelRotations() {
        PMatrix rotMatrix = new PMatrix();
        rotMatrix.setIdentity();
        if (sceneData.getPScene().getInstances().getChild(0).getChildrenCount() > 0) {
            sceneData.getPScene().getInstances().getChild(0).getChild(0).getTransform().getLocalMatrix(true).setRotation(rotMatrix.getRotation());
        }
    }

    /**
     * Helper function used to load saved avatar configurations
     * TODO: name for the instance????
     */
    public void loadSavedData() {
        //1- Clean out the old instances
        sceneData.getPScene().getInstances().removeAllChildren();
        //2- Get the modelinstance to load the saved file into the scene
        PPolygonModelInstance newModelInst = new PPolygonModelInstance("this name needs to be set by user... nudge paul nudge...");
        // Switch to character API when possible
        boolean success = false; //newModelInst.loadModel(sceneData.getFileXML(), sceneData.getPScene());
        sceneData.getPScene().addInstanceNode(newModelInst);
        if (success) {
            logger.log(Level.INFO, "<<<<SHOULD HAVE LOADED... CROSS YOUR FINGERS>>>>");
        } else {
            logger.log(Level.SEVERE, "<<<< FAILURE...FAILURE...FAILURE...FAILURE >>>>");
        }

    // add an animation processor to the skinned mesh child

    }

    /**
     * Loads the saved avatar configuration file that the user had previously
     * created (xml format)
     */
    public void loadxml() {
        logger.log(Level.INFO, "=================================================");
        logger.log(Level.INFO, "User requested to load a saved configuration file");
        logger.log(Level.INFO, "=================================================");

        int retValLoad = jFileChooser_LoadXML.showOpenDialog(this);

        if (retValLoad == JFileChooser.APPROVE_OPTION) {
            fileXML = jFileChooser_LoadXML.getSelectedFile();
            sceneData.setfileXML(fileXML);

            resetModelScales();
            resetModelRotations();
            resetDataMembers();
            resetGUI(0);

            loadSavedData();

            resetLocalScales();
            resetGUI(0);
        }

        logger.log(Level.INFO, "=================================================");
        logger.log(Level.INFO, "Loading of avatar configuration file is  complete");
        logger.log(Level.INFO, "=================================================");
    }

    /**
     * Saves the current avatar configuration that the user is working on into
     * an xml formated file
     */
    public void savexml() {
        logger.log(Level.INFO, "=================================================");
        logger.log(Level.INFO, "User requested to save current avtar configuration");
        logger.log(Level.INFO, "=================================================");

        if (fileXML == null) {
            fileXML = new File("NewAvatarConfig.xml");
            jFileChooser_SaveXML.setSelectedFile(fileXML);
        }

        int retValLoad = jFileChooser_SaveXML.showSaveDialog(this);
        if (retValLoad == JFileChooser.APPROVE_OPTION) {
            fileXML = jFileChooser_SaveXML.getSelectedFile();
            sceneData.setfileXML(fileXML);
            PPolygonModelInstance modInst = ((PPolygonModelInstance) sceneData.getPScene().getInstances().getChild(0));
            // Switch to Character API when possible
            //modInst.saveModel(fileXML);
        }

        logger.log(Level.INFO, "=================================================");
        logger.log(Level.INFO, "Saving of current avatar configuration is complete");
        logger.log(Level.INFO, "=================================================");
    }

    /**
     * Rotates the model based on the slider (axis) that is used
     * @param axis (Integer)
     * TODO: safe check on second getChild() call
     */
    public void RotateOnAxis(int axis) {
        PMatrix rotMatrix = new PMatrix();
        int degree = 0;
        float radians = 0;
        switch (axis) {
            case 0: // XAxis
            {
                degree = ((Integer) jSlider_XAxis.getValue()).intValue();
                if (degree >= 180) {
                    degree -= 180;
                } else {
                    degree += 180;
                }
                radians = (float) java.lang.Math.toRadians((double) degree);
                xRotation.buildRotationX(radians);
                break;
            }
            case 1: // YAxis
            {
                degree = ((Integer) jSlider_YAxis.getValue()).intValue();
                if (degree >= 180) {
                    degree -= 180;
                } else {
                    degree += 180;
                }
                radians = (float) java.lang.Math.toRadians((double) degree);
                yRotation.buildRotationY(radians);
                break;
            }
            case 2: // ZAxis
            {
                degree = ((Integer) jSlider_ZAxis.getValue()).intValue();
                if (degree >= 180) {
                    degree -= 180;
                } else {
                    degree += 180;
                }
                radians = (float) java.lang.Math.toRadians((double) degree);
                zRotation.buildRotationZ(radians);
                break;
            }
        }
        rotMatrix.mul(yRotation);
        rotMatrix.mul(xRotation);
        rotMatrix.mul(zRotation);
        if (sceneData.getPScene().getInstances().getChildrenCount() > 0 && ((PNode) jComboBox_ModelInstances.getSelectedItem()).getChildrenCount() > 0) {
            ((PNode) jComboBox_ModelInstances.getSelectedItem()).getChild(0).getTransform().getLocalMatrix(true).setRotation(rotMatrix.getRotation());
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

        FileFilter modelFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".ms3d") || f.getName().toLowerCase().endsWith(".dae")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                String szDescription = new String("Models (*.ms3d, *.dae)");
                return szDescription;
            }
        };
        jFileChooser_LoadModels = new javax.swing.JFileChooser();
        FileFilter assetFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".jpg") ||
                    f.getName().toLowerCase().endsWith(".png") ||
                    f.getName().toLowerCase().endsWith(".gif") ||
                    f.getName().toLowerCase().endsWith(".tga")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                String szDescription = new String("Images (*.jpg, *.png, *.gif, *.tga)");
                return szDescription;
            }
        };
        jFileChooser_LoadAssets = new javax.swing.JFileChooser();
        FileFilter xmlFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
                return false;
            }

            public String getDescription() {
                String szDescription = new String("Extensible Markup Language File (*.xml)");
                return szDescription;
            }
        };
        jFileChooser_LoadXML = new javax.swing.JFileChooser();
        FileFilter xmlFilterS = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
                return false;
            }

            public String getDescription() {
                String szDescription = new String("Extensible Markup Language File (*.xml)");
                return szDescription;
            }
        };
        jFileChooser_SaveXML = new javax.swing.JFileChooser();
        jToolBar_HotKeys_FileIO = new javax.swing.JToolBar();
        jButton_New = new javax.swing.JButton();
        jButton_Open = new javax.swing.JButton();
        jButton_Save = new javax.swing.JButton();
        jToolBar_HotKeys_Assets = new javax.swing.JToolBar();
        jButton_Model = new javax.swing.JButton();
        jButton_Texture = new javax.swing.JButton();
        jButton_Reload = new javax.swing.JButton();
        jToolBar_HotKeys_Animations = new javax.swing.JToolBar();
        jComboBox_ModelInstances = new javax.swing.JComboBox();
        jComboBox_AnimationsQ = new javax.swing.JComboBox();
        jLabel_AnimSpeed = new javax.swing.JLabel();
        jSlider_AnimationsQ = new javax.swing.JSlider();
        jToolBar_HotKeys_Rotation = new javax.swing.JToolBar();
        jLabel_RotationsQ = new javax.swing.JLabel();
        jSeparator_Rot = new javax.swing.JToolBar.Separator();
        jLabel_XAxis = new javax.swing.JLabel();
        jSlider_XAxis = new javax.swing.JSlider();
        jSeparator_XY = new javax.swing.JToolBar.Separator();
        jLabel_YAxis = new javax.swing.JLabel();
        jSlider_YAxis = new javax.swing.JSlider();
        jSeparator_YZ = new javax.swing.JToolBar.Separator();
        jLabel_ZAxis = new javax.swing.JLabel();
        jSlider_ZAxis = new javax.swing.JSlider();
        jButton_RotationsReset = new javax.swing.JButton();
        jToolBar_HotKeys_Tools = new javax.swing.JToolBar();
        jLabel_Tools = new javax.swing.JLabel();
        jSeparator_ToolsBegin = new javax.swing.JToolBar.Separator();
        jLabel_AvatarOptions = new javax.swing.JLabel();
        jCheckBox_AvatarOptions = new javax.swing.JCheckBox();
        jLabel_PSceneExplorer = new javax.swing.JLabel();
        jCheckBox_Explorer = new javax.swing.JCheckBox();
        jSeparator_ToolsEnd = new javax.swing.JToolBar.Separator();
        jMenuBar_MainMenu = new javax.swing.JMenuBar();
        jMenu_File = new javax.swing.JMenu();
        jMenuItem_LoadModel = new javax.swing.JMenuItem();
        jMenuItem_LoadTexture = new javax.swing.JMenuItem();
        jMenuItem_LoadXML = new javax.swing.JMenuItem();
        jMenuItem_SaveXML = new javax.swing.JMenuItem();
        jMenu_Help = new javax.swing.JMenu();
        jMenuItem_About = new javax.swing.JMenuItem();

        jFileChooser_LoadModels.setDialogTitle("Load Model File");
        java.io.File modDirectory = new File("./assets/models");
        jFileChooser_LoadModels.setCurrentDirectory(modDirectory);
        jFileChooser_LoadModels.setDoubleBuffered(true);
        jFileChooser_LoadModels.setDragEnabled(true);
        jFileChooser_LoadModels.addChoosableFileFilter(((FileFilter)modelFilter));

        jFileChooser_LoadAssets.setName("Load Texture File");
        jFileChooser_LoadAssets.addChoosableFileFilter(((FileFilter)assetFilter));
        java.io.File assetDirectory = new File("./assets/textures");
        jFileChooser_LoadAssets.setCurrentDirectory(assetDirectory);
        jFileChooser_LoadAssets.setDialogTitle("Load Texture File");

        jFileChooser_LoadXML.addChoosableFileFilter(((FileFilter)xmlFilter));
        java.io.File configDirectory = new File("./assets/configurations/");
        jFileChooser_LoadXML.setCurrentDirectory(configDirectory);
        jFileChooser_LoadXML.setDialogTitle("Load XML File");

        jFileChooser_SaveXML.addChoosableFileFilter(((FileFilter)xmlFilterS));
        java.io.File saveDirectory = new File("./assets/configurations/");
        jFileChooser_SaveXML.setCurrentDirectory(configDirectory);
        jFileChooser_SaveXML.setDialogTitle("Save XML File");
        jFileChooser_SaveXML.setSelectedFile(fileXML);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToolBar_HotKeys_FileIO.setRollover(true);

        jButton_New.setText("NEW");
        jButton_New.setFocusable(false);
        jButton_New.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_New.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_New.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_FileIO.add(jButton_New);

        jButton_Open.setText("OPEN");
        jButton_Open.setFocusable(false);
        jButton_Open.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Open.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OpenActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_FileIO.add(jButton_Open);

        jButton_Save.setText("SAVE");
        jButton_Save.setFocusable(false);
        jButton_Save.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Save.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_FileIO.add(jButton_Save);

        jToolBar_HotKeys_Assets.setRollover(true);

        jButton_Model.setText("Change Model");
        jButton_Model.setFocusable(false);
        jButton_Model.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Model.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Model.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ModelActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_Assets.add(jButton_Model);

        jButton_Texture.setText("Change Texture");
        jButton_Texture.setFocusable(false);
        jButton_Texture.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Texture.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Texture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_TextureActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_Assets.add(jButton_Texture);

        jButton_Reload.setText("Reload PScene");
        jButton_Reload.setFocusable(false);
        jButton_Reload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Reload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Reload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ReloadActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_Assets.add(jButton_Reload);

        jToolBar_HotKeys_Animations.setRollover(true);

        jComboBox_ModelInstances.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_ModelInstances.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAnimations();
                setSpeedSlider();
                //        setRotationSliders();
                if(options != null)
                options.setSelectedInstance(((PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem()));
            }
        });
        jToolBar_HotKeys_Animations.add(jComboBox_ModelInstances);

        jComboBox_AnimationsQ.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_AnimationsQ.setMaximumSize(new java.awt.Dimension(200, 25));
        jComboBox_AnimationsQ.setMinimumSize(new java.awt.Dimension(85, 20));
        jComboBox_AnimationsQ.setPreferredSize(new java.awt.Dimension(150, 27));
        jComboBox_AnimationsQ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_AnimationsQActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_Animations.add(jComboBox_AnimationsQ);

        jLabel_AnimSpeed.setText("Animation Speed");
        jToolBar_HotKeys_Animations.add(jLabel_AnimSpeed);

        jSlider_AnimationsQ.setMaximum(40);
        jSlider_AnimationsQ.setMinimum(1);
        jSlider_AnimationsQ.setValue(10);
        jSlider_AnimationsQ.setMaximumSize(new java.awt.Dimension(200, 29));
        jSlider_AnimationsQ.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                jSlider_AnimationsQStateChanged(e);
            }
        });
        jToolBar_HotKeys_Animations.add(jSlider_AnimationsQ);

        jToolBar_HotKeys_Rotation.setRollover(true);

        jLabel_RotationsQ.setText("Rotate Model");
        jToolBar_HotKeys_Rotation.add(jLabel_RotationsQ);

        jSeparator_Rot.setPreferredSize(new java.awt.Dimension(22, 1));
        jToolBar_HotKeys_Rotation.add(jSeparator_Rot);

        jLabel_XAxis.setText("X Axis");
        jToolBar_HotKeys_Rotation.add(jLabel_XAxis);

        jSlider_XAxis.setMaximum(359);
        jSlider_XAxis.setMinimum(1);
        jSlider_XAxis.setValue(180);
        jSlider_XAxis.setPreferredSize(new java.awt.Dimension(100, 29));
        jSlider_XAxis.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                RotateOnAxis(0);
            }
        });
        jToolBar_HotKeys_Rotation.add(jSlider_XAxis);

        jSeparator_XY.setPreferredSize(new java.awt.Dimension(22, 1));
        jToolBar_HotKeys_Rotation.add(jSeparator_XY);

        jLabel_YAxis.setText("Y Axis");
        jToolBar_HotKeys_Rotation.add(jLabel_YAxis);

        jSlider_YAxis.setMaximum(359);
        jSlider_YAxis.setMinimum(1);
        jSlider_YAxis.setValue(180);
        jSlider_YAxis.setPreferredSize(new java.awt.Dimension(100, 29));
        jSlider_YAxis.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                RotateOnAxis(1);
            }
        });
        jToolBar_HotKeys_Rotation.add(jSlider_YAxis);

        jSeparator_YZ.setMaximumSize(new java.awt.Dimension(22, 2147483647));
        jSeparator_YZ.setMinimumSize(new java.awt.Dimension(22, 1));
        jSeparator_YZ.setPreferredSize(new java.awt.Dimension(22, 1));
        jToolBar_HotKeys_Rotation.add(jSeparator_YZ);

        jLabel_ZAxis.setText("Z Axis");
        jToolBar_HotKeys_Rotation.add(jLabel_ZAxis);

        jSlider_ZAxis.setMaximum(359);
        jSlider_ZAxis.setMinimum(1);
        jSlider_ZAxis.setValue(180);
        jSlider_ZAxis.setPreferredSize(new java.awt.Dimension(100, 29));
        jSlider_ZAxis.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                RotateOnAxis(2);
            }
        });
        jToolBar_HotKeys_Rotation.add(jSlider_ZAxis);

        jButton_RotationsReset.setText("Reset Axes");
        jButton_RotationsReset.setFocusable(false);
        jButton_RotationsReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_RotationsReset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_RotationsReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RotationsResetActionPerformed(evt);
            }
        });
        jToolBar_HotKeys_Rotation.add(jButton_RotationsReset);

        jToolBar_HotKeys_Tools.setRollover(true);

        jLabel_Tools.setText("Tools");
        jToolBar_HotKeys_Tools.add(jLabel_Tools);
        jToolBar_HotKeys_Tools.add(jSeparator_ToolsBegin);

        jLabel_AvatarOptions.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_AvatarOptions.setText("Avatar Options");
        jToolBar_HotKeys_Tools.add(jLabel_AvatarOptions);

        jCheckBox_AvatarOptions.setFocusable(false);
        jCheckBox_AvatarOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCheckBox_AvatarOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox_AvatarOptions.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox_AvatarOptionsItemStateChanged(evt);
            }
        });
        jToolBar_HotKeys_Tools.add(jCheckBox_AvatarOptions);

        jLabel_PSceneExplorer.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_PSceneExplorer.setText("PScene Explorer");
        jToolBar_HotKeys_Tools.add(jLabel_PSceneExplorer);

        jCheckBox_Explorer.setFocusable(false);
        jCheckBox_Explorer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCheckBox_Explorer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox_Explorer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox_ExplorerItemStateChanged(evt);
            }
        });
        jToolBar_HotKeys_Tools.add(jCheckBox_Explorer);
        jToolBar_HotKeys_Tools.add(jSeparator_ToolsEnd);

        jMenuBar_MainMenu.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        jMenu_File.setText("File");
        jMenu_File.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OpenActionPerformed(evt);
            }
        });

        jMenuItem_LoadModel.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadModel.setText("Load Model");
        jMenuItem_LoadModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ModelActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_LoadModel);

        jMenuItem_LoadTexture.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadTexture.setText("Load Texture");
        jMenuItem_LoadTexture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_TextureActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_LoadTexture);

        jMenuItem_LoadXML.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadXML.setText("Load Configuration");
        jMenuItem_LoadXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OpenActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_LoadXML);

        jMenuItem_SaveXML.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_SaveXML.setText("Save Configuration");
        jMenuItem_SaveXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_SaveXML);

        jMenuBar_MainMenu.add(jMenu_File);

        jMenu_Help.setText("Help");

        jMenuItem_About.setText("About");
        jMenu_Help.add(jMenuItem_About);

        jMenuBar_MainMenu.add(jMenu_Help);

        setJMenuBar(jMenuBar_MainMenu);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jToolBar_HotKeys_FileIO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jToolBar_HotKeys_Assets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jToolBar_HotKeys_Animations, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jToolBar_HotKeys_Tools, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jToolBar_HotKeys_Rotation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(jToolBar_HotKeys_FileIO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE)
                        .add(jToolBar_HotKeys_Assets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jToolBar_HotKeys_Animations, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jToolBar_HotKeys_Tools, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_HotKeys_Rotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Opens up a dialog box for the user to choose a previously saved avatar
     * configuration file to load
     * @param evt (ActionEvent)
     */
private void jButton_OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OpenActionPerformed
    loadxml();

    if (explorer != null) {
        explorer.setExplorer(sceneData);
    }
    if (options != null) {
        options.setPScene(sceneData.getPScene());
        options.initValues();
        options.setAvatarName(avatarName);
        options.setAvatarGender(avatarGender);
    }
}//GEN-LAST:event_jButton_OpenActionPerformed

    /**
     * Opens up a dialog box for the user to save the current avatar configuration
     * @param evt (ActionEvent)
     */
private void jButton_SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SaveActionPerformed
    savexml();
}//GEN-LAST:event_jButton_SaveActionPerformed

    /**
     * Sets the animation based on the animation cycle selected from the combo box
     * @param evt (ActionEvent)
     */
private void jComboBox_AnimationsQActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_AnimationsQActionPerformed
// BROKEN --- The animation states are now kept at the skeleton node!
    //    if (jComboBox_AnimationsQ.isEnabled()) {
//        int iAnimID = jComboBox_AnimationsQ.getSelectedIndex();
//        PPolygonSkinnedMeshInstance smInstance = ((PPolygonSkinnedMeshInstance) ((PNode) jComboBox_ModelInstances.getSelectedItem()).getChild(0));
//        if (iAnimID == (jComboBox_AnimationsQ.getItemCount() - 1)) {
//            iAnimID = -1;
//            smInstance.getAnimationState().setPauseAnimation(true);
//        } else {
//            smInstance.getAnimationState().setPauseAnimation(false);
//            smInstance.transitionTo(iAnimID);
//        }
//  }
}//GEN-LAST:event_jComboBox_AnimationsQActionPerformed

    /**
     * Resets the configuration to default settings
     * @param evt (ActionEvent)
     * TODO: make it reset to default
     */
private void jButton_NewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewActionPerformed
    setDefault();
}//GEN-LAST:event_jButton_NewActionPerformed

    /**
     * Opens up a jFileChooser OpenDialog box to permit the user to load a model &
     * subsuqent texture file
     * @param evt (ActionEvent)
     */
private void jButton_ModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ModelActionPerformed
    sceneData.loadAvatarDAEFile(false, false, this);
}//GEN-LAST:event_jButton_ModelActionPerformed

    /**
     * Opens up a jFileChooser OpenDialog box to permit the user to switch out the
     * current texture on the model
     * @param evt (Action Eent)
     */
private void jButton_TextureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_TextureActionPerformed
    logger.log(Level.INFO, "=================================================");
    logger.log(Level.INFO, "Loading a texture file per the user's request....");
    logger.log(Level.INFO, "=================================================");
    
    PPolygonMeshInstance meshInst = (PPolygonMeshInstance)((PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem()).getChild(0);
    sceneData.loadTexture(meshInst, this);
    
    logger.log(Level.INFO, "=================================================");
    logger.log(Level.INFO, "Loading of texture file has been completed.......");
    logger.log(Level.INFO, "=================================================");
}//GEN-LAST:event_jButton_TextureActionPerformed

    /**
     * If the checkbox is selected it opens up the PScene Explorer window otherwise
     * it will dispose of the explorer window if open
     * @param evt (ItemEvent)
     */
private void jCheckBox_ExplorerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox_ExplorerItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        jCheckBox_Explorer.setSelected(true);
        explorer = new TreeExplorer();
        explorer.setExplorer(sceneData);
        explorer.setVisible(true);
        explorer.expandTree();
    } else {
        jCheckBox_Explorer.setSelected(false);
        explorer.nodeUnselect();
        explorer.dispose();
    }
}//GEN-LAST:event_jCheckBox_ExplorerItemStateChanged

    /**
     * If the checkbox is selected it opens up the Options window for editing the
     * avatar otherwise it will dispose of the options window if open
     * @param evt (ItemEvent)
     */
private void jCheckBox_AvatarOptionsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox_AvatarOptionsItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        jCheckBox_AvatarOptions.setSelected(true);
        options = new OptionsGUI();
        options.setPScene(sceneData.getPScene());
        options.setSelectedInstance(((PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem()));
        options.initValues();
        options.setAvatarName(avatarName);
        options.setAvatarGender(avatarGender);
        options.setVisible(true);
    } else {
        jCheckBox_AvatarOptions.setSelected(false);
        options.dispose();
    }
}//GEN-LAST:event_jCheckBox_AvatarOptionsItemStateChanged

    /**
     * Force the controls to recheck the PScene for new data.  Current controls that
     * require this information is the animation data.
     * @param evt (ActionEvent)
     */
private void jButton_ReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ReloadActionPerformed
    resetInstanceCount();
    resetAnimations();
}//GEN-LAST:event_jButton_ReloadActionPerformed

    /**
     * Reset the Avatar rotation and the GUI rotation controls to default
     * @param evt (ActionEvent)
     */
private void jButton_RotationsResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RotationsResetActionPerformed
        jSlider_XAxis.setValue(180);
        jSlider_YAxis.setValue(180);
        jSlider_ZAxis.setValue(180);
        
        xRotation.setIdentity();
        yRotation.setIdentity();
        zRotation.setIdentity();
        
        if (sceneData.getPScene().getInstances().getChildrenCount() > 0 && ((PNode) jComboBox_ModelInstances.getSelectedItem()).getChildrenCount() > 0) {
            ((PNode) jComboBox_ModelInstances.getSelectedItem()).getChild(0).getTransform().getLocalMatrix(true).setRotation(new Matrix3f());
        }
}//GEN-LAST:event_jButton_RotationsResetActionPerformed

/**
 * Change the speed of the animation
 * @param e (ChangeEvent)
 */
private void jSlider_AnimationsQStateChanged(ChangeEvent e) {
    if (jSlider_AnimationsQ.isEnabled()) {
        float fAnimSpeed = (jSlider_AnimationsQ.getValue() * 0.10f);
        PPolygonSkinnedMeshInstance smInstance = ((PPolygonSkinnedMeshInstance)((PNode)jComboBox_ModelInstances.getSelectedItem()).getChild(0));
        // BROKEN --- The animation states are now kept at the skeleton node!
        //smInstance.getAnimationState().setAnimationSpeed(fAnimSpeed);
    }
}

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AvatarEditorGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Model;
    private javax.swing.JButton jButton_New;
    private javax.swing.JButton jButton_Open;
    private javax.swing.JButton jButton_Reload;
    private javax.swing.JButton jButton_RotationsReset;
    private javax.swing.JButton jButton_Save;
    private javax.swing.JButton jButton_Texture;
    private javax.swing.JCheckBox jCheckBox_AvatarOptions;
    private javax.swing.JCheckBox jCheckBox_Explorer;
    private javax.swing.JComboBox jComboBox_AnimationsQ;
    private javax.swing.JComboBox jComboBox_ModelInstances;
    private javax.swing.JFileChooser jFileChooser_LoadAssets;
    private javax.swing.JFileChooser jFileChooser_LoadModels;
    private javax.swing.JFileChooser jFileChooser_LoadXML;
    private javax.swing.JFileChooser jFileChooser_SaveXML;
    private javax.swing.JLabel jLabel_AnimSpeed;
    private javax.swing.JLabel jLabel_AvatarOptions;
    private javax.swing.JLabel jLabel_PSceneExplorer;
    private javax.swing.JLabel jLabel_RotationsQ;
    private javax.swing.JLabel jLabel_Tools;
    private javax.swing.JLabel jLabel_XAxis;
    private javax.swing.JLabel jLabel_YAxis;
    private javax.swing.JLabel jLabel_ZAxis;
    private javax.swing.JMenuBar jMenuBar_MainMenu;
    private javax.swing.JMenuItem jMenuItem_About;
    private javax.swing.JMenuItem jMenuItem_LoadModel;
    private javax.swing.JMenuItem jMenuItem_LoadTexture;
    private javax.swing.JMenuItem jMenuItem_LoadXML;
    private javax.swing.JMenuItem jMenuItem_SaveXML;
    private javax.swing.JMenu jMenu_File;
    private javax.swing.JMenu jMenu_Help;
    private javax.swing.JToolBar.Separator jSeparator_Rot;
    private javax.swing.JToolBar.Separator jSeparator_ToolsBegin;
    private javax.swing.JToolBar.Separator jSeparator_ToolsEnd;
    private javax.swing.JToolBar.Separator jSeparator_XY;
    private javax.swing.JToolBar.Separator jSeparator_YZ;
    private javax.swing.JSlider jSlider_AnimationsQ;
    private javax.swing.JSlider jSlider_XAxis;
    private javax.swing.JSlider jSlider_YAxis;
    private javax.swing.JSlider jSlider_ZAxis;
    private javax.swing.JToolBar jToolBar_HotKeys_Animations;
    private javax.swing.JToolBar jToolBar_HotKeys_Assets;
    private javax.swing.JToolBar jToolBar_HotKeys_FileIO;
    private javax.swing.JToolBar jToolBar_HotKeys_Rotation;
    private javax.swing.JToolBar jToolBar_HotKeys_Tools;
    // End of variables declaration//GEN-END:variables

}
