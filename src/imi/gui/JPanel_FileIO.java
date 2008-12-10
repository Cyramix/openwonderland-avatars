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

import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.utils.tree.MeshInstanceSearchProcessor;
import imi.scene.utils.tree.TreeTraverser;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author  ptruong
 */
public class JPanel_FileIO extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Scene Data **/
    private SceneEssentials sceneData = new SceneEssentials();
    /** File Data */
    private java.io.File fileXML    = null;
    /** Button Actions (HACK) */
    private JPanel_ModelRotation rotPanel   = null;
    private JPanel_Animations animPanel     = null;
    private JPanel_ShaderLoader shaderPanel = null;
    
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form JPanel_FileIO */
    public JPanel_FileIO() {
        initComponents();
    }

    /**
     * Loads the saved avatar configuration file that the user had previously
     * created (xml format)
     */
    public void loadxml(JPanel_ModelRotation rotPanel, JPanel_Animations animPanel, JPanel_ShaderLoader shaderPanel) {
        System.out.println("=================================================");
        System.out.println("User requested to load a saved configuration file");
        System.out.println("=================================================");

        int retValLoad = jFileChooser_LoadXML.showOpenDialog(this);
        if (retValLoad == javax.swing.JFileChooser.APPROVE_OPTION) {
            fileXML = jFileChooser_LoadXML.getSelectedFile();
            sceneData.setfileXML(fileXML);
            
            rotPanel.resetPanel();

            loadSavedData();

            while (sceneData.getPScene().getAssetWaitingList().size() > 0) {
                //System.out.println("Waiting to get assets...");
            }
            System.out.println("=================================================");
            System.out.println("Loading of avatar configuration file is  complete");
            System.out.println("=================================================");
        } else {
            System.out.println("=================================================");
            System.out.println("Loading of avatar configuration file cancelled...");
            System.out.println("=================================================");
        }
    }
    
    public void loadxml(JPanel_ModelRotation rotPanel, JPanel_Animations animPanel) {
        System.out.println("=================================================");
        System.out.println("User requested to load a saved configuration file");
        System.out.println("=================================================");

        int retValLoad = jFileChooser_LoadXML.showOpenDialog(this);
        if (retValLoad == javax.swing.JFileChooser.APPROVE_OPTION) {
            fileXML = jFileChooser_LoadXML.getSelectedFile();
            sceneData.setfileXML(fileXML);
            
            rotPanel.resetPanel();

            loadSavedData();

            while (sceneData.getPScene().getAssetWaitingList().size() > 0) {
                //System.out.println("Waiting to get assets...");
            }
            
            if (animPanel != null)
                animPanel.resetPanel();
            if (rotPanel != null)
                rotPanel.setModelInst(animPanel.getSelectedModelInstanceNode());
            
            System.out.println("=================================================");
            System.out.println("Loading of avatar configuration file is  complete");
            System.out.println("=================================================");
        } else {
            System.out.println("=================================================");
            System.out.println("Loading of avatar configuration file cancelled...");
            System.out.println("=================================================");
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
        imi.scene.polygonmodel.PPolygonModelInstance newModelInst = new imi.scene.polygonmodel.PPolygonModelInstance("this name needs to be set by user... nudge paul nudge...");
        // Switch to Character API when possible
        boolean success = false;//newModelInst.loadModel(sceneData.getFileXML(), sceneData.getPScene());
        sceneData.getPScene().addInstanceNode(newModelInst);
        if (success) {
            System.out.println("<<<<SHOULD HAVE LOADED... CROSS YOUR FINGERS>>>>");
        } else {
            System.out.println("<<<< FAILURE...FAILURE...FAILURE...FAILURE >>>>");
        }
    }
    
    /**
     * Saves the current avatar configuration that the user is working on into
     * an xml formated file
     */
    public void savexml() {
        System.out.println("=================================================");
        System.out.println("User requested to save current avtar configuration");
        System.out.println("=================================================");

        if (fileXML == null) {
            fileXML = new java.io.File("NewAvatarConfig.xml");
            jFileChooser_SaveXML.setSelectedFile(fileXML);
        }

        int retValLoad = jFileChooser_SaveXML.showSaveDialog(this);
        if (retValLoad == javax.swing.JFileChooser.APPROVE_OPTION) {
            fileXML = jFileChooser_SaveXML.getSelectedFile();
            sceneData.setfileXML(fileXML);
            imi.scene.polygonmodel.PPolygonModelInstance modInst = ((imi.scene.polygonmodel.PPolygonModelInstance) sceneData.getPScene().getInstances().getChild(0));
            // Switch to Character API when possible
            //modInst.saveModel(fileXML);
            
            System.out.println("=================================================");
            System.out.println("Saving of current avatar configuration is complete");
            System.out.println("=================================================");
        } else {
            System.out.println("=================================================");
            System.out.println("Saving of current avatar configuration cancelled.");
            System.out.println("=================================================");
        }
    }

    /**
     * Replaces the current model in the scene with a new model selected by the
     * user
     * @param rotPanel (JPanel_Rotation)
     * @param animPanel (JPanel_Animation)
     */
    public void loadModel(JPanel_ModelRotation rotPanel, JPanel_Animations animPanel, JPanel_ShaderLoader shaderPanel) {
//        System.out.println("=================================================");
//        System.out.println("Loading a model file per the user's request.....");
//        System.out.println("=================================================");
//
//        int retValModel = jFileChooser_LoadModels.showOpenDialog(this);
//        if (retValModel == javax.swing.JFileChooser.APPROVE_OPTION) {
//            java.io.File fileModel = jFileChooser_LoadModels.getSelectedFile();
//            sceneData.setfileModel(fileModel);
//
//            rotPanel.resetPanel();
//
//            if (fileModel.getName().endsWith(".ms3d")) {
//                sceneData.loadMS3DFile(0, true, this);
//            } else if (fileModel.getName().endsWith(".dae")) {
//                sceneData.loadDAEFile(true, this);
//            } else {
//                sceneData.loadDAECharacter(true, this);
//            }
//
//            while (sceneData.getPScene().getAssetWaitingList().size() > 0) {
//                //System.out.println("Waiting to get assets...");
//            }
//
//            SkeletonNode skel = ((SkeletonNode)sceneData.getPScene().getInstances().getChild(0).getChild(0));
//            sceneData.setCurrentSkeleton(skel);
//            
//            if (shaderPanel != null)
//                shaderPanel.setPanel(sceneData.getPScene());
//            if (animPanel != null)
//                animPanel.setPanel(sceneData.getPScene());
//            if (rotPanel != null)
//                rotPanel.setModelInst(animPanel.getSelectedModelInstanceNode());
//            
//            System.out.println("=================================================");
//            System.out.println("Loading of model file has been completed.........");
//            System.out.println("=================================================");
//        } else {
//            System.out.println("=================================================");
//            System.out.println("Loading of model file has been cancelled.........");
//            System.out.println("=================================================");
//        }
    }
    
    public void loadSkinnedMeshModel() {
//        System.out.println("=================================================");
//        System.out.println("Loading a model file per the user's request.....");
//        System.out.println("=================================================");
//
//        int retValModel = jFileChooser_LoadModels.showOpenDialog(this);
//        if (retValModel == javax.swing.JFileChooser.APPROVE_OPTION) {
//            java.io.File fileModel = jFileChooser_LoadModels.getSelectedFile();
//            sceneData.setfileModel(fileModel);
//
//            rotPanel.resetPanel();
//
//            sceneData.loadDAESMeshFile(true, this);
//
//            while (sceneData.getPScene().getAssetWaitingList().size() > 0) {
//                //System.out.println("Waiting to get assets...");
//            }
//            
//            if (shaderPanel != null)
//                shaderPanel.setPanel(sceneData.getPScene());
//            if (animPanel != null)
//                animPanel.setPanel(sceneData.getPScene());
//            if (rotPanel != null)
//                rotPanel.setModelInst(animPanel.getSelectedModelInstanceNode());
//            
//            System.out.println("=================================================");
//            System.out.println("Loading of model file has been completed.........");
//            System.out.println("=================================================");
//        } else {
//            System.out.println("=================================================");
//            System.out.println("Loading of model file has been cancelled.........");
//            System.out.println("=================================================");
//        }
    }
    
    public void loadModel(JPanel_ModelRotation rotPanel, JPanel_Animations animPanel) {
//        System.out.println("=================================================");
//        System.out.println("Loading a model file per the user's request.....");
//        System.out.println("=================================================");
//
//        int retValModel = jFileChooser_LoadModels.showOpenDialog(this);
//        if (retValModel == javax.swing.JFileChooser.APPROVE_OPTION) {
//            java.io.File fileModel = jFileChooser_LoadModels.getSelectedFile();
//            sceneData.setfileModel(fileModel);
//
//            rotPanel.resetPanel();
//
//            if (fileModel.getName().endsWith(".ms3d")) {
//                sceneData.loadMS3DFile(0, true, this);
//            } else if (fileModel.getName().endsWith(".dae")) {
//                sceneData.loadDAEFile(true, this);
//            } else {
//                sceneData.loadDAECharacter(true, this);
//            }
//
//            while (sceneData.getPScene().getAssetWaitingList().size() > 0) {
//                //System.out.println("Waiting to get assets...");
//            }
//
//            if (animPanel != null)
//                animPanel.resetPanel();
//            if (rotPanel != null)
//                rotPanel.setModelInst(animPanel.getSelectedModelInstanceNode());
//            
//            System.out.println("=================================================");
//            System.out.println("Loading of model file has been completed.........");
//            System.out.println("=================================================");
//        } else {
//            System.out.println("=================================================");
//            System.out.println("Loading of model file has been cancelled.........");
//            System.out.println("=================================================");
//        }
    }
    
    /**
     * Replaces the current model's texture with a new texture selected by the
     * user
     * @param animPanel (JPanel_Animation)
     */
    public void loadTexture(JPanel_Animations animPanel) {
        System.out.println("=================================================");
        System.out.println("Loading a texture file per the user's request....");
        System.out.println("=================================================");

        sceneData.loadTexture(((PPolygonMeshInstance)jComboBox_Textures.getSelectedItem()), this);

        System.out.println("=================================================");
        System.out.println("Loading of texture file has been completed.......");
        System.out.println("=================================================");
    }
    
    public void loadMeshes() {
        if (animPanel.getSelectedModelInstance() != null) {
            PPolygonModelInstance modInst = animPanel.getSelectedModelInstance();
            imi.scene.PNode node = ((imi.scene.PNode)modInst.findChild("skeletonRoot"));
            if(node != null) {
                imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
                ArrayList<PPolygonSkinnedMeshInstance> aMeshInst = skeleton.getSkinnedMeshInstances();
                Vector<PPolygonSkinnedMeshInstance> vMeshInst = new Vector<PPolygonSkinnedMeshInstance>();
                for(int i = 0; i < aMeshInst.size(); i++) {
                    vMeshInst.add(aMeshInst.get(i));
                }
                jComboBox_Textures.setModel(new DefaultComboBoxModel(vMeshInst));
            } else {
                MeshInstanceSearchProcessor proc = new MeshInstanceSearchProcessor();
                proc.setProcessor();
                TreeTraverser.breadthFirst(modInst, proc);
                jComboBox_Textures.setModel(new DefaultComboBoxModel(proc.getMeshInstances()));
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setRotPanel(JPanel_ModelRotation panel) { rotPanel = panel; }
    public void setAnimPanel(JPanel_Animations panel) { animPanel = panel; }
    public void setShaderPanel (JPanel_ShaderLoader panel) { shaderPanel = panel; }
    public void setSceneEssentials(SceneEssentials sceneEss) {
        sceneData.setSceneData(sceneEss.getJScene(), sceneEss.getPScene(),
                sceneEss.getEntity(), sceneEss.getWM(), sceneEss.getProcessors());
    }
    public void setPanel(SceneEssentials sceneEss, JPanel_ModelRotation rotpanel, JPanel_Animations animpanel, JPanel_ShaderLoader shaderpanel) {
        sceneData.setSceneData(sceneEss.getJScene(), sceneEss.getPScene(),
                sceneEss.getEntity(), sceneEss.getWM(), sceneEss.getProcessors());
        rotPanel = rotpanel;
        animPanel = animpanel;
        shaderPanel = shaderpanel;
        loadMeshes();
    }
    public void setPanel(SceneEssentials sceneEss, JPanel_ModelRotation rotpanel, JPanel_Animations animpanel) {
        sceneData.setSceneData(sceneEss.getJScene(), sceneEss.getPScene(),
                sceneEss.getEntity(), sceneEss.getWM(), sceneEss.getProcessors());
        rotPanel = rotpanel;
        animPanel = animpanel;
        
        if(animpanel.getSelectedModelInstance() != null)
            loadMeshes();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.filechooser.FileFilter modelFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
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
        javax.swing.filechooser.FileFilter assetFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
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
        javax.swing.filechooser.FileFilter xmlFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
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
        javax.swing.filechooser.FileFilter xmlFilterS = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
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
        jButton_LoadSMesh = new javax.swing.JButton();
        jButton_Model = new javax.swing.JButton();
        jButton_New = new javax.swing.JButton();
        jButton_Open = new javax.swing.JButton();
        jButton_Save = new javax.swing.JButton();
        jComboBox_Textures = new javax.swing.JComboBox();
        jButton_Texture = new javax.swing.JButton();

        jFileChooser_LoadModels.setDialogTitle("Load Model File");
        java.io.File modelDirectory = new java.io.File("./assets/models/");

        jFileChooser_LoadModels.setCurrentDirectory(modelDirectory);
        jFileChooser_LoadModels.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        jFileChooser_LoadModels.setDoubleBuffered(true);
        jFileChooser_LoadModels.setDragEnabled(true);
        jFileChooser_LoadModels.addChoosableFileFilter(((javax.swing.filechooser.FileFilter)modelFilter));

        jFileChooser_LoadAssets.setName("Load Texture File");
        jFileChooser_LoadAssets.addChoosableFileFilter(((javax.swing.filechooser.FileFilter)assetFilter));
        java.io.File assetDirectory = new java.io.File("./assets/textures");
        jFileChooser_LoadAssets.setCurrentDirectory(assetDirectory);
        jFileChooser_LoadAssets.setDialogTitle("Load Texture File");

        jFileChooser_LoadXML.addChoosableFileFilter(((javax.swing.filechooser.FileFilter)xmlFilter));
        java.io.File configDirectory = new java.io.File("./assets/configurations/");
        jFileChooser_LoadXML.setCurrentDirectory(configDirectory);
        jFileChooser_LoadXML.setDialogTitle("Load XML File");

        jFileChooser_SaveXML.addChoosableFileFilter(((javax.swing.filechooser.FileFilter)xmlFilterS));
        java.io.File saveDirectory = new java.io.File("./assets/configurations/");
        jFileChooser_SaveXML.setCurrentDirectory(saveDirectory);
        jFileChooser_SaveXML.setDialogTitle("Save XML File");
        jFileChooser_SaveXML.setSelectedFile(fileXML);

        setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "File I/O", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setMaximumSize(new java.awt.Dimension(230, 247));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(230, 247));

        jButton_LoadSMesh.setText("Load Skinned Mesh");
        jButton_LoadSMesh.setPreferredSize(new java.awt.Dimension(214, 29));
        jButton_LoadSMesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSkinnedMeshModel();
            }
        });

        jButton_Model.setText("Load Model");
        jButton_Model.setPreferredSize(new java.awt.Dimension(214, 29));
        jButton_Model.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadModel(rotPanel, animPanel, shaderPanel);
            }
        });

        jButton_New.setText("Reset Scene");
        jButton_New.setPreferredSize(new java.awt.Dimension(214, 29));
        jButton_New.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //TODO: gotta reset all to default
            }
        });

        jButton_Open.setText("Load Config File");
        jButton_Open.setPreferredSize(new java.awt.Dimension(214, 29));
        jButton_Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadxml(rotPanel, animPanel, shaderPanel);
            }
        });

        jButton_Save.setText("Save Config File");
        jButton_Save.setPreferredSize(new java.awt.Dimension(214, 29));
        jButton_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savexml();
            }
        });

        jComboBox_Textures.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_Textures.setMaximumSize(new java.awt.Dimension(214, 29));
        jComboBox_Textures.setMinimumSize(new java.awt.Dimension(85, 27));
        jComboBox_Textures.setPreferredSize(new java.awt.Dimension(214, 29));

        jButton_Texture.setText("Load Texture");
        jButton_Texture.setPreferredSize(new java.awt.Dimension(214, 29));
        jButton_Texture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTexture(animPanel);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButton_Open, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_Save, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_Model, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_LoadSMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_New, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBox_Textures, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 207, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_Texture, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(2, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jButton_Open, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jButton_Save, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jButton_Model, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jButton_LoadSMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jButton_New, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBox_Textures, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton_Texture, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_LoadSMesh;
    private javax.swing.JButton jButton_Model;
    private javax.swing.JButton jButton_New;
    private javax.swing.JButton jButton_Open;
    private javax.swing.JButton jButton_Save;
    private javax.swing.JButton jButton_Texture;
    private javax.swing.JComboBox jComboBox_Textures;
    private javax.swing.JFileChooser jFileChooser_LoadAssets;
    private javax.swing.JFileChooser jFileChooser_LoadModels;
    private javax.swing.JFileChooser jFileChooser_LoadXML;
    private javax.swing.JFileChooser jFileChooser_SaveXML;
    // End of variables declaration//GEN-END:variables
}
