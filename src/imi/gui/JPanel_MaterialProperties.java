/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanel_MaterialProperties.java
 *
 * Created on Feb 27, 2009, 5:28:52 PM
 */

package imi.gui;

import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PMeshMaterial;
import imi.scene.utils.traverser.MeshInstanceSearchProcessor;
import imi.scene.utils.traverser.TreeTraverser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.mtgame.WorldManager;


/**
 *
 * @author ptruong
 */
public class JPanel_MaterialProperties extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////

    private ArrayList<URL>                  m_textureLocations  = new ArrayList<URL>();
    private PScene                          m_pscene            = null;
    private WorldManager                    m_wm                = null;
    private PPolygonMeshInstance            m_mesh              = null; // The mesh that owns the material
    private PMeshMaterial                   m_mat               = null; // The data model
    private JPanel_ShaderProperties         m_shaderPropPanel   = null;
    private Vector<PPolygonMeshInstance>    m_meshInstances     = null;
    private TextureCreator                  m_parent            = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor
     */
    public JPanel_MaterialProperties() {
        initComponents();
    }

    /**
     * Overloaded constructor used for the Texture Creator
     * @param sceneData
     * @param parent
     */
    public JPanel_MaterialProperties(PScene pscene, WorldManager wm, TextureCreator parent) {
        m_pscene    = pscene;
        m_wm        = wm;
        m_parent    = parent;
        initComponents();
        initMeshList();
        initShaderPanel();
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
            if (secondIndex >= jList_Textures.getModel().getSize()) // At the bottom, do nothing
                Toolkit.getDefaultToolkit().beep();
            else
            {
                swap(firstIndex, secondIndex, (DefaultListModel)jList_Textures.getModel());
                jList_Textures.setSelectedIndex(secondIndex);
            }
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

            try {
                m_textureLocations.add(texFile.toURI().toURL());
            } catch (MalformedURLException ex) {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, ex.getMessage());
            }

            ((DefaultListModel)jList_Textures.getModel()).addElement(texName);
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
            m_textureLocations.remove(jList_Textures.getSelectedIndex());
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

    /**
     * Removes all textures from the listbox as well as the Arraylist of textures
     */
    private void removeAllTextures() {
        m_textureLocations.clear();
        ((DefaultListModel)jList_Textures.getModel()).removeAllElements();
    }

    /**
     * Loads the textures that were populated in the Listbox (in actuality the
     * Arraylist of textures) into the material
     */
    private void loadTexturesAndApplyToMesh() {
        if(m_textureLocations.size() > 0) {
            int i = 0;
            for(i = 0; i < m_textureLocations.size(); i++) {
                String relativepath = m_textureLocations.get(i).getPath();
                m_mat.setTexture(new File(relativepath), i);
            }
            m_mesh.setMaterial(m_mat);
            m_mesh.applyMaterial();
        }
    }

    private void applyMaterialRequested(java.awt.event.ActionEvent evt) {
        if (m_mesh == null || m_mat == null)
            return;
        if (m_shaderPropPanel != null && m_shaderPropPanel.getShader() != null)
            m_mat.setDefaultShader(m_shaderPropPanel.getShader());
        loadTexturesAndApplyToMesh();
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public ArrayList<URL> getTextureLocations() {
        return m_textureLocations;
    }

    public PPolygonMeshInstance getMeshInstance() {
        return m_mesh;
    }

    public PMeshMaterial getMeshMaterial() {
        return m_mat;
    }


////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setTextureLocations(ArrayList<URL> textureLocations) {
        m_textureLocations = textureLocations;
    }

    public void setPScene(PScene scene) {
        m_pscene = scene;
    }

    public void setMeshInstance(PPolygonMeshInstance meshInstance) {
        m_mesh = meshInstance;
    }

    public void setMeshMaterial(PMeshMaterial meshMaterial) {
        m_mat = meshMaterial;
    }

    public void setWorldManager(WorldManager wm) {
        m_wm = wm;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser_Texture = new javax.swing.JFileChooser();
        MaterialProperties = new javax.swing.JTabbedPane();
        MaterialPanel = new javax.swing.JPanel();
        jScrollPane_Textures = new javax.swing.JScrollPane();
        jList_Textures = new javax.swing.JList(new DefaultListModel());
        ButtonPanel = new javax.swing.JPanel();
        jButton_MoveUp = new javax.swing.JButton();
        jButton_MoveDown = new javax.swing.JButton();
        jButton_Add = new javax.swing.JButton();
        jButton_Remove = new javax.swing.JButton();
        jButton_RemoveAll = new javax.swing.JButton();
        jButton_Apply = new javax.swing.JButton();
        jComboBox_Meshes = new javax.swing.JComboBox();

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

        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        MaterialProperties.setPreferredSize(new java.awt.Dimension(400, 265));

        MaterialPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        MaterialPanel.setPreferredSize(new java.awt.Dimension(380, 274));
        MaterialPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Textures.setPreferredSize(new java.awt.Dimension(300, 250));
        jScrollPane_Textures.setViewportView(jList_Textures);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        MaterialPanel.add(jScrollPane_Textures, gridBagConstraints);

        ButtonPanel.setPreferredSize(new java.awt.Dimension(120, 174));
        ButtonPanel.setLayout(new java.awt.GridBagLayout());

        jButton_MoveUp.setText("Move Up");
        jButton_MoveUp.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton_MoveUp.setPreferredSize(new java.awt.Dimension(120, 25));
        jButton_MoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveSelectedItemUp();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        ButtonPanel.add(jButton_MoveUp, gridBagConstraints);

        jButton_MoveDown.setText("Move Down");
        jButton_MoveDown.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton_MoveDown.setPreferredSize(new java.awt.Dimension(120, 25));
        jButton_MoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveSelectedItemDown();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        ButtonPanel.add(jButton_MoveDown, gridBagConstraints);

        jButton_Add.setText("Add");
        jButton_Add.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton_Add.setPreferredSize(new java.awt.Dimension(120, 25));
        jButton_Add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTextureToList();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        ButtonPanel.add(jButton_Add, gridBagConstraints);

        jButton_Remove.setText("Remove");
        jButton_Remove.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton_Remove.setPreferredSize(new java.awt.Dimension(120, 25));
        jButton_Remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedTexture();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        ButtonPanel.add(jButton_Remove, gridBagConstraints);

        jButton_RemoveAll.setText("Remove All");
        jButton_RemoveAll.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton_RemoveAll.setPreferredSize(new java.awt.Dimension(120, 25));
        jButton_RemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllTextures();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        ButtonPanel.add(jButton_RemoveAll, gridBagConstraints);

        jButton_Apply.setText("Apply To Material");
        jButton_Apply.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton_Apply.setPreferredSize(new java.awt.Dimension(120, 25));
        jButton_Apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyMaterialRequested(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        ButtonPanel.add(jButton_Apply, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        MaterialPanel.add(ButtonPanel, gridBagConstraints);

        MaterialProperties.addTab("Materials", MaterialPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(MaterialProperties, gridBagConstraints);

        jComboBox_Meshes.setModel(new javax.swing.DefaultComboBoxModel());
        jComboBox_Meshes.setMinimumSize(new java.awt.Dimension(0, 0));
        jComboBox_Meshes.setPreferredSize(new java.awt.Dimension(47, 10));
        jComboBox_Meshes.setRenderer(new JPanel_MaterialProperties.JComboBoxRenderer());

        jComboBox_Meshes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setMeshPanel();
                initShaderPanel();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jComboBox_Meshes, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JPanel MaterialPanel;
    private javax.swing.JTabbedPane MaterialProperties;
    private javax.swing.JButton jButton_Add;
    private javax.swing.JButton jButton_Apply;
    private javax.swing.JButton jButton_MoveDown;
    private javax.swing.JButton jButton_MoveUp;
    private javax.swing.JButton jButton_Remove;
    private javax.swing.JButton jButton_RemoveAll;
    private javax.swing.JComboBox jComboBox_Meshes;
    private javax.swing.JFileChooser jFileChooser_Texture;
    private javax.swing.JList jList_Textures;
    private javax.swing.JScrollPane jScrollPane_Textures;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void initMeshList() {
        if (m_pscene == null)
            return;

        MeshInstanceSearchProcessor proc = new MeshInstanceSearchProcessor();
        proc.setProcessor();
        TreeTraverser.breadthFirst(m_pscene, proc);
        m_meshInstances = proc.getMeshInstances();

        DefaultComboBoxModel model = new DefaultComboBoxModel(m_meshInstances);
        jComboBox_Meshes.setModel(model);
        populateListModel();
    }

    public void initTextureList(PPolygonMeshInstance mesh) {

        if (mesh == null)
            return;

        m_mesh  = mesh;
        m_mat   = mesh.getMaterialRef();

        ((DefaultListModel)jList_Textures.getModel()).removeAllElements();
        if(!m_textureLocations.isEmpty())
            m_textureLocations.clear();

        String texName;
        DefaultListModel newModel = new DefaultListModel();
        for (int i = 0; i < m_mesh.getGeometry().getNumberOfTextures(); ++i)
        {
            if (m_mat.getTextureRef(i) != null) {
                texName = "["+ i + "] " + m_mat.getTextureRef(i).getImageLocation();
                m_textureLocations.add(m_mat.getTextureRef(i).getImageLocation());
                newModel.addElement(texName);
            }
        }

        jList_Textures.setModel(newModel);
    }

    public void populateListModel() {
        m_mesh = null;
        if (jComboBox_Meshes.getSelectedIndex() != -1)
            m_mesh = (PPolygonMeshInstance) jComboBox_Meshes.getSelectedItem();

        ((DefaultListModel)jList_Textures.getModel()).removeAllElements();
        if(!m_textureLocations.isEmpty())
            m_textureLocations.clear();
        
        if (m_mesh == null)
            return;
        
        String texName;
        m_mat = m_mesh.getMaterialRef();
        DefaultListModel newModel = new DefaultListModel();
        for (int i = 0; i < m_mesh.getGeometry().getNumberOfTextures(); ++i)
        {
            if (m_mat.getTextureRef(i) != null) {
                texName = "["+ i + "] " + m_mat.getTextureRef(i).getImageLocation();
                m_textureLocations.add(m_mat.getTextureRef(i).getImageLocation());
                newModel.addElement(texName);
            }            
        }
        jList_Textures.setModel(newModel);

        try {
            m_parent.getLayerPanel().loadBaseImage(new File(m_textureLocations.get(0).toURI()));
            m_parent.getLayerPanel().updatePanels();
        } catch (URISyntaxException ex) {
            Logger.getLogger(JPanel_MaterialProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setMeshPanel() {
        m_mesh = (PPolygonMeshInstance) jComboBox_Meshes.getSelectedItem();
        populateListModel();
    }

    public void initShaderPanel() {
        // add shader stuff in
        if (m_mesh != null)
        {
            if (m_mesh.getMaterialRef().getShader() != null)
            {
                if (m_shaderPropPanel == null)
                {
                    m_shaderPropPanel = new JPanel_ShaderProperties(m_mesh.getMaterialRef().getShader(), m_wm);
                    m_shaderPropPanel.setSize(380, 280);
                    MaterialProperties.addTab("Shader Properties", m_shaderPropPanel);
                }
                m_shaderPropPanel.setShader(m_mesh.getMaterialRef().getShader());
            }
        }
    }

    //Swap two elements in the list.
    private void swap(int a, int b, DefaultListModel listModel) {
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

        URL aFile      = m_textureLocations.get(a);
        URL bFile      = m_textureLocations.get(b);

        listModel.set(a, bObject);
        listModel.set(b, aObject);
        m_textureLocations.set(a, bFile);
        m_textureLocations.set(b, aFile);
    }

    public void addNewlyCreatedTextureToList(File newTexture) {
        int index = ((DefaultListModel)jList_Textures.getModel()).size();
        String texName = "["+ index + "]  " + newTexture.getName();

        try {
            m_textureLocations.add(newTexture.toURI().toURL());
        } catch (MalformedURLException ex) {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, ex.getMessage());
        }

        ((DefaultListModel)jList_Textures.getModel()).addElement(texName);
    }

    public void setComboBoxAvailablity(boolean onOff) {
        jComboBox_Meshes.setEnabled(onOff);
        jComboBox_Meshes.setVisible(onOff);
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Classes
////////////////////////////////////////////////////////////////////////////////

    public static class JComboBoxRenderer extends JLabel implements ListCellRenderer {

        Border m_selectBorder   = null;
        Border m_unselectBorder = null;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                list.setSelectionBackground(new Color(0, 0, 255));
                if (m_selectBorder == null) {
                    m_selectBorder = BorderFactory.createLineBorder(new Color(0, 255, 0), 3);
                }
                this.setBorder(m_selectBorder);
            } else {
                if (m_unselectBorder == null) {
                    m_unselectBorder = BorderFactory.createLineBorder(list.getBackground(), 0);
                }
                this.setBorder(m_unselectBorder);
            }

            if (value instanceof PPolygonMeshInstance) {
                String meshName = ((PPolygonMeshInstance) value).getName();
                setText(meshName);
            }
            return this;
        }
    }
}
