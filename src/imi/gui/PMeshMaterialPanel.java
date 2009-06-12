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
import imi.scene.polygonmodel.parts.PMeshMaterial;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.mtgame.WorldManager;

/**
 * This widget is for viewing and potentially tweaking the state of a 
 * PMeshMaterial object.
 * @author  Ronald E Dahlgren
 * @author  Paul Viet Nguyen Truong
 */
public class PMeshMaterialPanel extends javax.swing.JPanel 
{
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Texture Data */
    private ArrayList<URL> textureLocations = new ArrayList<URL>();
    /** Scene Data */
    private WorldManager wm             = null;
    private PPolygonMeshInstance m_mesh = null; // The mesh that owns the material   
    private PMeshMaterial m_mat         = null; // The data model
    /** Shader Panel */    
    private JPanel_ShaderProperties m_shaderPropPanel = null;
    
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form PMeshMaterialPanel */
    public PMeshMaterialPanel() 
    {
        initComponents();
        jList_Textures.setModel(new DefaultListModel());
        loadShaderPanel();
    }
    
    public PMeshMaterialPanel(WorldManager wm)
    {
        this.wm = wm;
        initComponents();
    }
    
    /**
     * Create a new instance of this panel operating on the specified mesh instance
     * @param meshInst
     */
    public PMeshMaterialPanel(PPolygonMeshInstance meshInst)
    {
        m_mesh = meshInst;
        m_mat = m_mesh.getMaterialRef();
        initComponents();
    }

    /**
     * Change the mesh whose material will be operated on to the one specified.
     * @param meshInst
     */
    public void setOwningMesh(PPolygonMeshInstance meshInst)
    {
        m_mesh = meshInst;
        m_mat = m_mesh.getMaterialRef();
        loadShaderPanel();
    }
    
    /**
     * Re-populates the listbox that contains the textures of the current mesh
     * NOTE: any textures added into this listbox will be applied to the mesh
     * when the apply material button is used
     */
    public void populateListModel()
    {
        // out with the old
        ((DefaultListModel)jList_Textures.getModel()).removeAllElements();
        if(!textureLocations.isEmpty())
            textureLocations.clear();
        String texName;
        DefaultListModel newModel = new DefaultListModel();
        for (int i = 0; i < m_mesh.getGeometry().getNumberOfTextures(); ++i) 
        {
            if (m_mat.getTexture(i) != null) {
                texName = "["+ i + "] " + m_mat.getTexture(i).getImageLocation();
                textureLocations.add(m_mat.getTexture(i).getImageLocation());
            }
            else
                texName = "[" + i + "] is unset";
            
            newModel.addElement(texName);
        }
        jList_Textures.setModel(newModel);
    }
    
    /**
     * moves the listed texture up a slot in the ArrayList and the listbox view
     * NOTE: this manipulates the listbox's model as well as the ArrayList of
     * of Files.
     */
    private void moveSelectedItemUp() {
        if (jList_Textures.getSelectedIndex() == -1)
        {
            Toolkit.getDefaultToolkit().beep();
        }
        else
        {
            int firstIndex = jList_Textures.getSelectedIndex();
            int secondIndex = firstIndex - 1;
            if (secondIndex < 0) // At the top, do nothing
                Toolkit.getDefaultToolkit().beep();
            else
            {
                swap(firstIndex, secondIndex, (DefaultListModel)jList_Textures.getModel());
                jList_Textures.setSelectedIndex(secondIndex);
            }
        }
    }                                   

    /**
     * moves the listed texture down a slot in the ArrayList and the listbox view
     * NOTE: this manipulates the listbox's model as well as the ArrayList of
     * of Files.
     */
    private void moveSelectedItemDown() {
        if (jList_Textures.getSelectedIndex() == -1)
        {
            Toolkit.getDefaultToolkit().beep();
        }
        else
        {
            int firstIndex = jList_Textures.getSelectedIndex();
            int secondIndex = firstIndex + 1;
            if (secondIndex >= jList_Textures.getModel().getSize()) // At the top, do nothing
                Toolkit.getDefaultToolkit().beep();
            else
            {
                swap(firstIndex, secondIndex, (DefaultListModel)jList_Textures.getModel());
                jList_Textures.setSelectedIndex(secondIndex);
            }
        }
    }                                     

    /**
     * Removes the selected texture from the listbox as well as the Arraylist of
     * textures.
     * NOTE: after removal of the selected texture from the list the listbox's
     * model is renamed to reflect the ordering change.
     */
    private void removeSelectedTexture() {
        if (jList_Textures.getSelectedIndex() == -1)
            Toolkit.getDefaultToolkit().beep();
        else
        {
            textureLocations.remove(jList_Textures.getSelectedIndex());
            ((DefaultListModel)jList_Textures.getModel()).remove(jList_Textures.getSelectedIndex());
            DefaultListModel curModel = ((DefaultListModel)jList_Textures.getModel());
            DefaultListModel newModel = new DefaultListModel();
            for(int i = 0; i < curModel.size(); i++) {
                String temp = curModel.get(i).toString();
                String name = temp.substring(temp.lastIndexOf(" "));
                String texName = "[" + i + "]" + name;
                newModel.addElement(texName);
            }
            jList_Textures.setModel(newModel);
        }
    }
    
    private void removeAllTextures() {
        for(int i = 0; i < textureLocations.size(); i++) {
            textureLocations.remove(i);
            ((DefaultListModel)jList_Textures.getModel()).remove(i);
        }
    }

    /**
     * Adds the selected texture from the JFileChooser to the listbox and
     * Arraylist of textures.
     */
    private void addTextureToList() {                                 
        int retValLoad = jFileChooser_Texture.showOpenDialog(this);
        if (retValLoad == javax.swing.JFileChooser.APPROVE_OPTION) {
            File texFile = jFileChooser_Texture.getSelectedFile();
            int index = ((DefaultListModel)jList_Textures.getModel()).size();
            String texName = "["+ index + "]  " + texFile.getName();
            try
            {
                textureLocations.add(texFile.toURI().toURL());
            } catch (MalformedURLException ex)
            {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, ex.getMessage());
            }
            
            ((DefaultListModel)jList_Textures.getModel()).addElement(texName);
        }
    }
    
    /**
     * Loads the textures that were populated in the Listbox (in actuality the
     * Arraylist of textures) into the material
     */
    private void loadTexturesAndApplyToMesh() {
        if(textureLocations.size() > 0) {
            m_mat = m_mesh.getMaterialRef();
            int i = 0;
            for(i = 0; i < textureLocations.size(); i++) {
                String relativepath = textureLocations.get(i).getPath();
                int index = relativepath.lastIndexOf("assets");
                relativepath = relativepath.substring(index);
                m_mat.setTexture(new File(relativepath), i);
            }
            m_mesh.setMaterial(m_mat);
            m_mesh.applyMaterial();
            populateListModel();
        }
    }
    
    private void applyMaterialRequested(java.awt.event.ActionEvent evt) {
        if (m_mesh == null || m_mat == null)
            return;
        if (m_shaderPropPanel != null && m_shaderPropPanel.getShader() != null)
            m_mat.setShader(m_shaderPropPanel.getShader());
        loadTexturesAndApplyToMesh();
    }

    public void setWM(WorldManager worldm) { wm = worldm; }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser_Texture = new javax.swing.JFileChooser();
        jTabbedPane_MaterialProp = new javax.swing.JTabbedPane();
        jPanel_MaterialPanel = new javax.swing.JPanel();
        jScrollPane_Textures = new javax.swing.JScrollPane();
        jList_Textures = new javax.swing.JList();
        jPanel_Buttons = new javax.swing.JPanel();
        jButton_MoveUp = new javax.swing.JButton();
        jButton_MoveDown = new javax.swing.JButton();
        jButton_Add = new javax.swing.JButton();
        jButton_Remove = new javax.swing.JButton();
        jButton_RemoveAll = new javax.swing.JButton();
        jButton_Apply = new javax.swing.JButton();

        jFileChooser_Texture.setCurrentDirectory(new File("assets/textures"));
        jFileChooser_Texture.setDialogTitle("Choose a Texture");
        jFileChooser_Texture.setFileFilter(new FileFilter() {
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
                String szDescription = "Images (*.jpg, *.png, *.gif, *.tga)";
                return szDescription;
            }
        });
        jFileChooser_Texture.setToolTipText("");

        setMaximumSize(new java.awt.Dimension(32767, 345));
        setMinimumSize(new java.awt.Dimension(420, 345));
        setPreferredSize(new java.awt.Dimension(420, 345));

        jPanel_MaterialPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Textures.setMinimumSize(new java.awt.Dimension(255, 300));
        jScrollPane_Textures.setPreferredSize(new java.awt.Dimension(255, 300));

        jList_Textures.setModel(new javax.swing.DefaultListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Textures.setViewportView(jList_Textures);

        jPanel_MaterialPanel.add(jScrollPane_Textures, new java.awt.GridBagConstraints());

        jPanel_Buttons.setLayout(new java.awt.GridBagLayout());

        jButton_MoveUp.setText("Move Up");
        jButton_MoveUp.setFocusable(false);
        jButton_MoveUp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_MoveUp.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_MoveUp.setMinimumSize(new java.awt.Dimension(150, 25));
        jButton_MoveUp.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton_MoveUp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_MoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveSelectedItemUp();
            }
        });
        jPanel_Buttons.add(jButton_MoveUp, new java.awt.GridBagConstraints());

        jButton_MoveDown.setText("Move Down");
        jButton_MoveDown.setFocusable(false);
        jButton_MoveDown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_MoveDown.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_MoveDown.setMinimumSize(new java.awt.Dimension(150, 25));
        jButton_MoveDown.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton_MoveDown.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_MoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveSelectedItemDown();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Buttons.add(jButton_MoveDown, gridBagConstraints);

        jButton_Add.setText("Add");
        jButton_Add.setFocusable(false);
        jButton_Add.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Add.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_Add.setMinimumSize(new java.awt.Dimension(150, 25));
        jButton_Add.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton_Add.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTextureToList();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel_Buttons.add(jButton_Add, gridBagConstraints);

        jButton_Remove.setText("Remove");
        jButton_Remove.setFocusable(false);
        jButton_Remove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Remove.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_Remove.setMinimumSize(new java.awt.Dimension(150, 25));
        jButton_Remove.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton_Remove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedTexture();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel_Buttons.add(jButton_Remove, gridBagConstraints);

        jButton_RemoveAll.setText("Remove All");
        jButton_RemoveAll.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_RemoveAll.setMinimumSize(new java.awt.Dimension(150, 25));
        jButton_RemoveAll.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton_RemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedTexture();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        jPanel_Buttons.add(jButton_RemoveAll, gridBagConstraints);

        jButton_Apply.setText("Apply Material");
        jButton_Apply.setMaximumSize(new java.awt.Dimension(150, 25));
        jButton_Apply.setMinimumSize(new java.awt.Dimension(150, 25));
        jButton_Apply.setPreferredSize(new java.awt.Dimension(150, 25));
        jButton_Apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyMaterialRequested(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        jPanel_Buttons.add(jButton_Apply, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel_MaterialPanel.add(jPanel_Buttons, gridBagConstraints);

        jTabbedPane_MaterialProp.addTab("Material Properties", jPanel_MaterialPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane_MaterialProp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 420, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane_MaterialProp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>                        


    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton_Add;
    private javax.swing.JButton jButton_Apply;
    private javax.swing.JButton jButton_MoveDown;
    private javax.swing.JButton jButton_MoveUp;
    private javax.swing.JButton jButton_Remove;
    private javax.swing.JButton jButton_RemoveAll;
    private javax.swing.JFileChooser jFileChooser_Texture;
    private javax.swing.JList jList_Textures;
    private javax.swing.JPanel jPanel_Buttons;
    private javax.swing.JPanel jPanel_MaterialPanel;
    private javax.swing.JScrollPane jScrollPane_Textures;
    private javax.swing.JTabbedPane jTabbedPane_MaterialProp;
    // End of variables declaration                   

    public void setTargetMaterial(PMeshMaterial newMaterial)
    {
        m_mat = newMaterial;
        populateListModel();
    }

    public PMeshMaterial getTargetMaterial()
    {
        return m_mat;
    }

    //Swap two elements in the list.
    private void swap(int a, int b, DefaultListModel listModel) 
    {
        String aString  = (String)listModel.getElementAt(a);
        String bString  = (String)listModel.getElementAt(b);
        String aPre     = aString.substring(0, aString.indexOf(" "));
        String bPre     = bString.substring(0, bString.indexOf(" "));
        String aEnd     = aString.substring(aString.lastIndexOf(" "));
        String bEnd     = bString.substring(bString.lastIndexOf(" "));
        
        aString         = bPre + aEnd;
        bString         = aPre + bEnd;
        
        Object aObject  = aString;
        Object bObject  = bString;
        
        URL aFile      = textureLocations.get(a);
        URL bFile      = textureLocations.get(b);
        
        listModel.set(a, bObject);
        listModel.set(b, aObject);
        textureLocations.set(a, bFile);
        textureLocations.set(b, aFile);
    }
    
    private void loadShaderPanel()
    {
        // add shader stuff in
        if (m_mesh != null)
        {
            if (m_mesh.getMaterialRef().getShader() != null)
            {
                if (m_shaderPropPanel == null)
                {
                    m_shaderPropPanel = new JPanel_ShaderProperties(m_mesh.getMaterialRef().getShader(), wm);
                    m_shaderPropPanel.setSize(500, 400);
                    jTabbedPane_MaterialProp.addTab("Shader Properties", m_shaderPropPanel);
                }
                m_shaderPropPanel.setShader(m_mesh.getMaterialRef().getShader());
            }
        }
    }
}
