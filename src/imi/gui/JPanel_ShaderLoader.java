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

import imi.scene.shader.NoSuchPropertyException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  ptruong
 */
public class JPanel_ShaderLoader extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Shader Data */
    private java.io.File shaders = new java.io.File("src/imi/scene/shader/programs");
    private String       shaderPackage = new String("imi.scene.shader.programs");
    private java.util.ArrayList<imi.scene.shader.ShaderProperty> selectedShaderProperties = new java.util.ArrayList<imi.scene.shader.ShaderProperty>();
    /** Scene Data */
    private org.jdesktop.mtgame.WorldManager wm = null;
    private imi.scene.PScene  pScene = null;
    
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form JPanel_ShaderLoader */
    public JPanel_ShaderLoader() {
        initComponents();
    }

    /**
     * Scans the list of shader classes and populates the selection box
     */
    public void setShaderList() {
        java.io.File[] fList = shaders.listFiles();
        java.util.ArrayList<String> sList = new java.util.ArrayList<String>();
        String name, extension;
        int index = 0;
        int iSize = 0;
        
        // Filter out the abstact classes and folders
        for(int i = 0; i < fList.length; i++) {
            index = fList[i].getName().lastIndexOf(".");
            extension = fList[i].getName().substring(index+1, fList[i].getName().length());
            name = fList[i].getName().substring(0, index);
            if(extension.equals("java") && !name.equals("AbstractShader")) {
                sList.add(name);
                iSize++;
            }
        }
        
        // Create the list of classes of shaders
        String[] list = new String[iSize];
        for(int i = 0; i < iSize; i++) {
            list[i] = sList.get(i);
        }
        
        // Add the list to the combo box
        jComboBox_ShaderClasses.setModel(new javax.swing.DefaultComboBoxModel(list));
        try {
            setShaderProperties();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setShaderProperties() throws ClassNotFoundException {
        Class temp = getClass().getClassLoader().loadClass(shaderPackage + "."+jComboBox_ShaderClasses.getSelectedItem().toString());
        java.lang.reflect.Constructor[] construct = temp.getConstructors();
        imi.scene.shader.AbstractShaderProgram aShader;
        try {
            aShader = (imi.scene.shader.AbstractShaderProgram) construct[0].newInstance(wm);
            imi.scene.shader.ShaderProperty[] shaderProps = aShader.getProperties();
            String[] name = new String[shaderProps.length];
            selectedShaderProperties.clear();
            for(int i = 0; i < shaderProps.length; i++) {
                selectedShaderProperties.add(shaderProps[i]);
                name[i] = shaderProps[i].name;
            }
            jComboBox_PropName.setModel(new javax.swing.DefaultComboBoxModel(name));
            loadShaderProperties();            
        } catch (InstantiationException ex) {
            Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadShaderProperties() {
        if(selectedShaderProperties.size() > 0) {
            imi.scene.shader.ShaderProperty prop = selectedShaderProperties.get(jComboBox_PropName.getSelectedIndex());
            jFormattedTextField_PropClass.setText(prop.type.getJavaType().getName());
            //jFormattedTextField_PropValue.setText(prop.getValue().toString());            
            jFormattedTextField_PropValue.setValue(prop.getValue());
            
            jComboBox_PropName.setEnabled(true);
            jFormattedTextField_PropClass.setEnabled(true);
            jFormattedTextField_PropValue.setEnabled(true);
        } else {
            jFormattedTextField_PropClass.setText("");
            //jFormattedTextField_PropValue.setText("");
            jComboBox_PropName.setEnabled(false);
            jFormattedTextField_PropClass.setEnabled(false);
            jFormattedTextField_PropValue.setEnabled(false);
        }        
    }
    
    public void commitShaderChange() {
        try {
            jFormattedTextField_PropValue.commitEdit();
            selectedShaderProperties.get(jComboBox_PropName.getSelectedIndex()).setValue(jFormattedTextField_PropValue.getValue()); 
        } catch (ParseException ex) {
            Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
    public void reloadModelInstances() {
        imi.scene.utils.tree.InstanceSearchProcessor proc = new imi.scene.utils.tree.InstanceSearchProcessor();
        proc.setProcessor();
        imi.scene.utils.tree.TreeTraverser.breadthFirst(pScene, proc);
        java.util.Vector<imi.scene.PNode> instances = proc.getModelInstances();
        jComboBox_AffectedModel.setModel(new javax.swing.DefaultComboBoxModel(instances));
    }
    
    public void reloadMeshInstances() {
        if(jComboBox_AffectedModel.getSelectedIndex() >= 0) {
            imi.scene.PNode node = (imi.scene.PNode)jComboBox_AffectedModel.getSelectedItem();
            if(node.getChildrenCount() > 0 && node.getChild(0) instanceof imi.scene.polygonmodel.parts.skinned.SkeletonNode) {
                imi.scene.polygonmodel.parts.skinned.SkeletonNode skeletonnode = (imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getChild(0);
                java.util.ArrayList<imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance> arraylistofSMInstances = skeletonnode.getSkinnedMeshInstances();
                java.util.Vector<imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance> instance = new java.util.Vector<imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance>();
                for(int i = 0; i < arraylistofSMInstances.size(); i++) {
                    instance.add(arraylistofSMInstances.get(i));
                }
                jComboBox_AffectedMesh.setModel(new javax.swing.DefaultComboBoxModel(instance));
            } else if(node.getChildrenCount() > 0 && node.getChild(0) instanceof imi.scene.polygonmodel.PPolygonMeshInstance) {
                imi.scene.utils.tree.MeshInstanceSearchProcessor proc = new imi.scene.utils.tree.MeshInstanceSearchProcessor();
                proc.setProcessor();
                imi.scene.utils.tree.TreeTraverser.breadthFirst(node, proc);
                java.util.Vector<imi.scene.polygonmodel.PPolygonMeshInstance> instances = proc.getMeshInstances();
                jComboBox_AffectedMesh.setModel(new javax.swing.DefaultComboBoxModel(instances));
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    public java.io.File getShaderDirectory() { return shaders; }
    public java.util.ArrayList<imi.scene.shader.ShaderProperty> getShaderList() { return selectedShaderProperties; }
    
////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setShaderDirectory(java.io.File file) { shaders = file; }
    public void setShaderPropertyList(java.util.ArrayList<imi.scene.shader.ShaderProperty> list) { selectedShaderProperties = list; }
    public void setWM(org.jdesktop.mtgame.WorldManager worldm) { wm = worldm; }
    public void setPScene(imi.scene.PScene pscene) { pScene = pscene; }
    public void setPanel(imi.scene.PScene pscene) {
        pScene = pscene;
        wm = pscene.getWorldManager();
//        setShaderList();
        reloadModelInstances();
        reloadMeshInstances();
    }
    public void resetPanel() {
        setShaderList();
        reloadModelInstances();
        reloadMeshInstances();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar_ShaderClasses = new javax.swing.JToolBar();
        jLabel_ShaderType = new javax.swing.JLabel();
        jComboBox_ShaderClasses = new javax.swing.JComboBox();
        jToolBar_ShaderProperty = new javax.swing.JToolBar();
        jLabel_ShaderProp = new javax.swing.JLabel();
        jComboBox_PropName = new javax.swing.JComboBox();
        jToolBar_ValueTypeData = new javax.swing.JToolBar();
        jFormattedTextField_PropClass = new javax.swing.JFormattedTextField();
        jFormattedTextField_PropValue = new javax.swing.JFormattedTextField();
        jToolBar_CommitScan = new javax.swing.JToolBar();
        jButton_Load = new javax.swing.JButton();
        jButton_ReloadShaderList = new javax.swing.JButton();
        jToolBar_AffectedModel = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jComboBox_AffectedModel = new javax.swing.JComboBox();
        jToolBar_AffectedMesh = new javax.swing.JToolBar();
        jLabel2 = new javax.swing.JLabel();
        jComboBox_AffectedMesh = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Shader Loader", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setMaximumSize(new java.awt.Dimension(650, 120));
        setMinimumSize(new java.awt.Dimension(650, 120));
        setPreferredSize(new java.awt.Dimension(650, 120));

        jToolBar_ShaderClasses.setFloatable(false);
        jToolBar_ShaderClasses.setRollover(true);

        jLabel_ShaderType.setText("Shader Type  ");
        jToolBar_ShaderClasses.add(jLabel_ShaderType);

        jComboBox_ShaderClasses.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_ShaderClasses.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_ShaderClasses.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_ShaderClasses.setPreferredSize(new java.awt.Dimension(85, 25));
        jComboBox_ShaderClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    setShaderProperties();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jToolBar_ShaderClasses.add(jComboBox_ShaderClasses);

        jToolBar_ShaderProperty.setFloatable(false);
        jToolBar_ShaderProperty.setRollover(true);

        jLabel_ShaderProp.setText("Shader Prop  ");
        jToolBar_ShaderProperty.add(jLabel_ShaderProp);

        jComboBox_PropName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_PropName.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_PropName.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_PropName.setPreferredSize(new java.awt.Dimension(100, 25));
        jComboBox_PropName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadShaderProperties();
            }
        });
        jToolBar_ShaderProperty.add(jComboBox_PropName);

        jToolBar_ValueTypeData.setFloatable(false);
        jToolBar_ValueTypeData.setRollover(true);

        jFormattedTextField_PropClass.setEditable(false);
        jFormattedTextField_PropClass.setText("Value Type");
        jFormattedTextField_PropClass.setMaximumSize(new java.awt.Dimension(180, 25));
        jFormattedTextField_PropClass.setMinimumSize(new java.awt.Dimension(14, 25));
        jFormattedTextField_PropClass.setPreferredSize(new java.awt.Dimension(180, 25));
        jToolBar_ValueTypeData.add(jFormattedTextField_PropClass);

        jFormattedTextField_PropValue.setText("Value");
        jFormattedTextField_PropValue.setMaximumSize(new java.awt.Dimension(120, 25));
        jFormattedTextField_PropValue.setMinimumSize(new java.awt.Dimension(14, 25));
        jFormattedTextField_PropValue.setPreferredSize(new java.awt.Dimension(120, 25));
        jFormattedTextField_PropValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commitShaderChange();
            }
        });
        jFormattedTextField_PropValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
                commitShaderChange();
            }
        });
        jToolBar_ValueTypeData.add(jFormattedTextField_PropValue);

        jToolBar_CommitScan.setFloatable(false);
        jToolBar_CommitScan.setRollover(true);

        jButton_Load.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jButton_Load.setText("Load Shader");
        jButton_Load.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Load.setFocusable(false);
        jButton_Load.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Load.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_Load.setMinimumSize(new java.awt.Dimension(130, 25));
        jButton_Load.setPreferredSize(new java.awt.Dimension(160, 25));
        jButton_Load.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_LoadActionPerformed(evt);
            }
        });
        jToolBar_CommitScan.add(jButton_Load);

        jButton_ReloadShaderList.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jButton_ReloadShaderList.setText("Scan Shader List");
        jButton_ReloadShaderList.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_ReloadShaderList.setFocusable(false);
        jButton_ReloadShaderList.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ReloadShaderList.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_ReloadShaderList.setMinimumSize(new java.awt.Dimension(130, 25));
        jButton_ReloadShaderList.setPreferredSize(new java.awt.Dimension(160, 25));
        jButton_ReloadShaderList.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ReloadShaderList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setShaderList();
            }
        });
        jToolBar_CommitScan.add(jButton_ReloadShaderList);

        jToolBar_AffectedModel.setFloatable(false);
        jToolBar_AffectedModel.setRollover(true);

        jLabel1.setText("Affected Model  ");
        jToolBar_AffectedModel.add(jLabel1);

        jComboBox_AffectedModel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_AffectedModel.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_AffectedModel.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_AffectedModel.setPreferredSize(new java.awt.Dimension(85, 25));
        jToolBar_AffectedModel.add(jComboBox_AffectedModel);

        jToolBar_AffectedMesh.setFloatable(false);
        jToolBar_AffectedMesh.setRollover(true);

        jLabel2.setText("Affected Mesh   ");
        jToolBar_AffectedMesh.add(jLabel2);

        jComboBox_AffectedMesh.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_AffectedMesh.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_AffectedMesh.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_AffectedMesh.setPreferredSize(new java.awt.Dimension(85, 25));
        jToolBar_AffectedMesh.add(jComboBox_AffectedMesh);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jToolBar_ShaderClasses, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 315, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_AffectedModel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 320, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(jToolBar_ShaderProperty, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 315, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_AffectedMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 320, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(jToolBar_ValueTypeData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 315, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_CommitScan, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 315, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(jToolBar_ShaderClasses, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jToolBar_ShaderProperty, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jToolBar_AffectedModel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jToolBar_AffectedMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jToolBar_ValueTypeData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_CommitScan, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(11, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton_LoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_LoadActionPerformed
    try {
        if (jComboBox_AffectedMesh.getSelectedIndex() >= 0) {
            imi.scene.PNode instance = (imi.scene.PNode)jComboBox_AffectedMesh.getSelectedItem();
            if (instance != null && instance instanceof imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance) {

                imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance inst = (imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance) instance;
                Class temp = getClass().getClassLoader().loadClass(shaderPackage + "."+jComboBox_ShaderClasses.getSelectedItem().toString());
                java.lang.reflect.Constructor[] construct = temp.getConstructors();
                imi.scene.polygonmodel.parts.PMeshMaterial meshMat = inst.getMaterialRef();
                try {
                    for(int i = 0; i < construct.length; i++) {
                            imi.scene.shader.AbstractShaderProgram aShader = (imi.scene.shader.AbstractShaderProgram) construct[i].newInstance(wm);

                        if(jComboBox_PropName.getSelectedIndex() >= 0) {
                            int index = jComboBox_PropName.getSelectedIndex();
                            imi.scene.shader.ShaderProperty prop = new imi.scene.shader.ShaderProperty(selectedShaderProperties.get(index).name,
                                    selectedShaderProperties.get(index).type, selectedShaderProperties.get(index).getValue());
                            try {
                                aShader.setProperty(prop);
                            } catch (NoSuchPropertyException ex) {
                                Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        meshMat.setShader(aShader);
                        inst.setMaterial(meshMat);
                        break;
                    }
                } catch (InstantiationException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                }            
            } else if(instance != null && instance instanceof imi.scene.polygonmodel.PPolygonMeshInstance) {
                
                imi.scene.polygonmodel.PPolygonMeshInstance inst = (imi.scene.polygonmodel.PPolygonMeshInstance) instance;
                Class temp = getClass().getClassLoader().loadClass("imi.scene.shader.effects."+jComboBox_ShaderClasses.getSelectedItem().toString());
                java.lang.reflect.Constructor[] construct = temp.getConstructors();
                imi.scene.polygonmodel.parts.PMeshMaterial meshMat = inst.getMaterialRef();
                try {
                    for(int i = 0; i < construct.length; i++) {
                            imi.scene.shader.AbstractShaderProgram aShader = (imi.scene.shader.AbstractShaderProgram) construct[i].newInstance(wm);

                        if(jComboBox_PropName.getSelectedIndex() >= 0) {
                            int index = jComboBox_PropName.getSelectedIndex();
                            imi.scene.shader.ShaderProperty prop = new imi.scene.shader.ShaderProperty(selectedShaderProperties.get(index).name,
                                    selectedShaderProperties.get(index).type, selectedShaderProperties.get(index).getValue());
                            try {
                                aShader.setProperty(prop);
                            } catch (NoSuchPropertyException ex) {
                                Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        meshMat.setShader(aShader);
                        inst.setMaterial(meshMat);
                        break;
                    }
                } catch (InstantiationException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    } catch (ClassNotFoundException ex) {
        Logger.getLogger(JPanel_ShaderLoader.class.getName()).log(Level.SEVERE, null, ex);
    }
}//GEN-LAST:event_jButton_LoadActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Load;
    private javax.swing.JButton jButton_ReloadShaderList;
    private javax.swing.JComboBox jComboBox_AffectedMesh;
    private javax.swing.JComboBox jComboBox_AffectedModel;
    private javax.swing.JComboBox jComboBox_PropName;
    private javax.swing.JComboBox jComboBox_ShaderClasses;
    private javax.swing.JFormattedTextField jFormattedTextField_PropClass;
    private javax.swing.JFormattedTextField jFormattedTextField_PropValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel_ShaderProp;
    private javax.swing.JLabel jLabel_ShaderType;
    private javax.swing.JToolBar jToolBar_AffectedMesh;
    private javax.swing.JToolBar jToolBar_AffectedModel;
    private javax.swing.JToolBar jToolBar_CommitScan;
    private javax.swing.JToolBar jToolBar_ShaderClasses;
    private javax.swing.JToolBar jToolBar_ShaderProperty;
    private javax.swing.JToolBar jToolBar_ValueTypeData;
    // End of variables declaration//GEN-END:variables

}
