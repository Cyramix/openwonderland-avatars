/*
 * FileIOPanel.java
 *
 * Created on October 22, 2008, 10:36 AM
 */

package imi.gui;

import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.utils.tree.MeshInstanceSearchProcessor;
import imi.scene.utils.tree.NodeProcessor;
import imi.scene.utils.tree.TreeTraverser;
import imi.sql.SQLInterface;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author  ptruong
 */
public class FileIOPanel extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Scene Information */
    SceneEssentials         m_sceneData;
    
    /** File IO */
    File                    m_fileConfig;
    File                    m_fileModel;
    File                    m_fileMesh;
    File                    m_fileTex;
    URL                     m_urlModel;
    URL                     m_urlMesh;
    URL                     m_urlTex;
    int                     m_iLoadType = -1;
    JFrame                  m_fileChooser;
    
    /** Panel References */
    JPanel_ModelRotation    m_rotPanel;
    JPanel_Animations       m_animPanel;
    
    /** SQL Interface */
    SQLInterface            m_sql;
    ArrayList<String[]>     m_data, m_anim;
    Map<Integer, String[]>  m_meshes;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form FileIOPanel */
    public FileIOPanel() {
        initComponents();
    }
    
    public void loadModelFile() {
        System.out.println("=================================================");
        System.out.println("Loading a model file per the user's request......");
        System.out.println("=================================================");

        int retValModel = jFileChooser_Model.showOpenDialog(this);
        if (retValModel == javax.swing.JFileChooser.APPROVE_OPTION) {
            m_fileModel = jFileChooser_Model.getSelectedFile();
            m_sceneData.setfileModel(m_fileModel);

            m_rotPanel.resetPanel();

            m_sceneData.loadDAECharacter(true, this);    // loads models with skinned animations

            while (m_sceneData.getPScene().getAssetWaitingList().size() > 0) {
                //m_logger.log(Level.INFO, "Waiting to get assets...");
            }

            if (m_animPanel != null)
                m_animPanel.resetPanel();
            if (m_rotPanel != null)
                m_rotPanel.setModelInst(m_animPanel.getSelectedModelInstanceNode());
            
            loadMeshes();
            
            System.out.println("=================================================");
            System.out.println("Loading of model file has been completed.........");
            System.out.println("=================================================");
        }

        System.out.println("=================================================");
        System.out.println("Loading of model file has been cancelled.........");
        System.out.println("=================================================");
    }
    
    public void loadMeshFile() {
        System.out.println("=================================================");
        System.out.println("Loading a model file per the user's request......");
        System.out.println("=================================================");

        int retValModel = jFileChooser_Mesh.showOpenDialog(this);
        if (retValModel == javax.swing.JFileChooser.APPROVE_OPTION) {
            m_fileModel = jFileChooser_Mesh.getSelectedFile();
            m_sceneData.setfileModel(m_fileModel);

            m_rotPanel.resetPanel();

            m_sceneData.loadDAEFile(true, this);    // Not skinned, no animations

            while (m_sceneData.getPScene().getAssetWaitingList().size() > 0) {
                //m_logger.log(Level.INFO, "Waiting to get assets...");
            }

            if (m_animPanel != null)
                m_animPanel.resetPanel();
            if (m_rotPanel != null)
                m_rotPanel.setModelInst(m_animPanel.getSelectedModelInstanceNode());
            
            loadMeshes();
            
            System.out.println("=================================================");
            System.out.println("Loading of model file has been completed.........");
            System.out.println("=================================================");
        }

        System.out.println("=================================================");
        System.out.println("Loading of model file has been cancelled.........");
        System.out.println("=================================================");
    }
    
    public void loadSkinnedMesh() {
        System.out.println("=================================================");
        System.out.println("Loading a model file per the user's request......");
        System.out.println("=================================================");

        int retValModel = jFileChooser_Mesh.showOpenDialog(this);
        if (retValModel == javax.swing.JFileChooser.APPROVE_OPTION) {
            m_fileModel = jFileChooser_Mesh.getSelectedFile();
            m_sceneData.setfileModel(m_fileModel);

            m_rotPanel.resetPanel();

            m_sceneData.loadDAESMeshFile(true, this);    // Not skinned, no animations

            while (m_sceneData.getPScene().getAssetWaitingList().size() > 0) {
                //m_logger.log(Level.INFO, "Waiting to get assets...");
            }

            if (m_animPanel != null)
                m_animPanel.resetPanel();
            if (m_rotPanel != null)
                m_rotPanel.setModelInst(m_animPanel.getSelectedModelInstanceNode());
            
            loadMeshes();
            
            System.out.println("=================================================");
            System.out.println("Loading of model file has been completed.........");
            System.out.println("=================================================");
        }

        System.out.println("=================================================");
        System.out.println("Loading of model file has been cancelled.........");
        System.out.println("=================================================");
    }
    
    public void loadTexFile() {
        System.out.println("=================================================");
        System.out.println("Loading a texture file per the user's request....");
        System.out.println("=================================================");

        m_sceneData.loadTexture(((PPolygonMeshInstance)jComboBox_Meshes.getSelectedItem()), this);

        System.out.println("=================================================");
        System.out.println("Loading of texture file has been completed.......");
        System.out.println("=================================================");
    }
    
    public void loadConfigFile() {
        System.out.println("=================================================");
        System.out.println("User requested to load a saved configuration file");
        System.out.println("=================================================");

        int retValLoad = jFileChooser_Config.showOpenDialog(this);
        if (retValLoad == javax.swing.JFileChooser.APPROVE_OPTION) {
            m_fileConfig = jFileChooser_Config.getSelectedFile();
            m_sceneData.setfileXML(m_fileConfig);
            
            m_rotPanel.resetPanel();

            //loadSavedData();

            while (m_sceneData.getPScene().getAssetWaitingList().size() > 0) {
                //m_logger.log(Level.INFO, "Waiting to get assets...");
            }
            
            if (m_animPanel != null) {
                m_animPanel.resetPanel();
            }
            if (m_rotPanel != null) {
                m_rotPanel.setModelInst(m_animPanel.getSelectedModelInstanceNode());
            }
            
            System.out.println("=================================================");
            System.out.println("Loading of avatar configuration file is complete ");
            System.out.println("=================================================");
        }

        System.out.println("=================================================");
        System.out.println("Loading of avatar configuration file canceled    ");
        System.out.println("=================================================");
    }
    
    public void saveConfigFile() {
        System.out.println("=================================================");
        System.out.println("User requested to save current avtar configuration");
        System.out.println("=================================================");

        if (m_fileConfig == null) {
            m_fileConfig = new java.io.File("NewAvatarConfig.xml");
            jFileChooser_Config.setSelectedFile(m_fileConfig);
        }

        int retValLoad = jFileChooser_Config.showSaveDialog(this);
        if (retValLoad == javax.swing.JFileChooser.APPROVE_OPTION) {
            m_fileConfig = jFileChooser_Config.getSelectedFile();
            m_sceneData.setfileXML(m_fileConfig);
            imi.scene.polygonmodel.PPolygonModelInstance modInst = ((imi.scene.polygonmodel.PPolygonModelInstance) m_sceneData.getPScene().getInstances().getChild(0));
            modInst.saveModel(m_fileConfig);
            
            System.out.println("=================================================");
            System.out.println("Saving of current avatar configuration is complete");
            System.out.println("=================================================");
        }
        System.out.println("=================================================");
        System.out.println("Saving of current avatar configuration is cancelled");
        System.out.println("=================================================");
    }
    
    public void loadURL() {
        switch(m_iLoadType)
        {
            case 0:
            {
                loadModelURL();
                break;
            }
            case 1:
            {
                loadMeshURL();
                break;
            }
            case 2:
            {
                loadTexURL();
                break;
            }
        }
        m_fileChooser.dispose();
    }
    
    public void loadModelURL() {
        System.out.println("=================================================");
        System.out.println("Loading a model file per the user's request......");
        System.out.println("=================================================");

        m_rotPanel.resetPanel();
        String query = "SELECT url FROM Animations WHERE avatarid = ";
        query += m_data.get(jList_ServerFiles.getSelectedIndex())[4].toString();
        m_anim = loadSQLData(query);
        
        String[] animations = new String[m_anim.size()];
        for(int i = 0; i < m_anim.size(); i++) {
            animations[i] = m_anim.get(i)[0].toString();
        }
        m_sceneData.loadDAECharacterURL(true, this, m_data.get(jList_ServerFiles.getSelectedIndex()), animations);   // loads models with skinned animations

        while (m_sceneData.getPScene().getAssetWaitingList().size() > 0) {
            //m_logger.log(Level.INFO, "Waiting to get assets...");
        }
        
        SkeletonNode skel = ((SkeletonNode)m_sceneData.getPScene().getInstances().getChild(0).getChild(0));
        m_sceneData.setCurrentSkeleton(skel);

        if (m_animPanel != null)
            m_animPanel.resetPanel();
        if (m_rotPanel != null)
            m_rotPanel.setModelInst(m_animPanel.getSelectedModelInstanceNode());

        loadMeshes();

        System.out.println("=================================================");
        System.out.println("Loading of model file has been completed.........");
        System.out.println("=================================================");
    }
    
    public void loadMeshURL() {
        System.out.println("=================================================");
        System.out.println("Loading a model file per the user's request......");
        System.out.println("=================================================");

        m_rotPanel.resetPanel();
        String[] data = m_data.get(jList_ServerFiles.getSelectedIndex());
        String query = "Select name, grouping FROM GeometryReferences WHERE referenceid = " + data[5];
        ArrayList<String[]> ref = loadSQLData(query);
        String[] meshref = new String[ref.size()];
        for(int i = 0; i < ref.size(); i++)
            meshref[i] = ref.get(i)[0];
        
        int iType = 0;
        if (ref.get(0)[1].equals("0"))
            iType = 0;          // Head
        else if (ref.get(0)[1].equals("1"))
            iType = 1;          // Hands
        else if (ref.get(0)[1].equals("2"))
            iType = 2;          // Torso
        else if (ref.get(0)[1].equals("3"))
            iType = 3;          // Legs
        else if (ref.get(0)[1].equals("4"))
            iType = 4;
                
        m_sceneData.loadDAEURL(false, this, data, meshref, iType);   // loads models with skinned animations
        
        while (m_sceneData.getPScene().getAssetWaitingList().size() > 0) {
            //m_logger.log(Level.INFO, "Waiting to get assets...");
        }
        
        if (data[4].equals("1") || data[4].equals("2"))
            pruneMeshes(m_sceneData.getPScene(), data[0]);
        
        if (m_animPanel != null)
            m_animPanel.resetPanel();
        if (m_rotPanel != null)
            m_rotPanel.setModelInst(m_animPanel.getSelectedModelInstanceNode());

        loadMeshes();

        System.out.println("=================================================");
        System.out.println("Loading of model file has been completed.........");
        System.out.println("=================================================");
    }
    
    public void pruneMeshes(PNode root, final String name) {
        NodeProcessor proc = new NodeProcessor() {
            PNode check = new PNode();
            
            public boolean processNode(PNode arg0) {
                check = new PNode();
                if(arg0 instanceof PPolygonMeshInstance) {
                    PPolygonMeshInstance meshInst = ((PPolygonMeshInstance)arg0);
                
                    if(!meshInst.getName().equals(name)) {
                        while(check != null)
                            check = m_sceneData.getPScene().getInstances().findAndRemoveChild(arg0);
                    } else {
                        PMatrix matrix = new PMatrix();
                        matrix.setIdentity();
                        meshInst.setTransform(new PTransform(matrix));
                    }
                }
                return true;
            }
        };        
        TreeTraverser.breadthFirst(root, proc);
    }
    
    public void loadTexURL() {

    }
    
    public void loadMeshes() {
        if (m_animPanel.getSelectedModelInstance() != null) {
            PPolygonModelInstance modInst = m_animPanel.getSelectedModelInstance();
            imi.scene.PNode node = ((imi.scene.PNode)modInst.findChild("skeletonRoot"));
            if(node != null) {
                imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
                ArrayList<PPolygonSkinnedMeshInstance> aMeshInst = skeleton.getSkinnedMeshInstances();
                Vector<PPolygonSkinnedMeshInstance> vMeshInst = new Vector<PPolygonSkinnedMeshInstance>();
                for(int i = 0; i < aMeshInst.size(); i++) {
                    vMeshInst.add(aMeshInst.get(i));
                }
                jComboBox_Meshes.setModel(new DefaultComboBoxModel(vMeshInst));
            } else {
                MeshInstanceSearchProcessor proc = new MeshInstanceSearchProcessor();
                proc.setProcessor();
                TreeTraverser.breadthFirst(modInst, proc);
                jComboBox_Meshes.setModel(new DefaultComboBoxModel(proc.getMeshInstances()));
            }
            jComboBox_Meshes.setEnabled(true);
            jButton_LoadTexF.setEnabled(true);
            jButton_LoadTexU.setEnabled(true);
        } else {
            jComboBox_Meshes.setEnabled(false);
            jButton_LoadTexF.setEnabled(false);
            jButton_LoadTexU.setEnabled(false);
        }
    }
    
    public ArrayList<String[]> loadSQLData(String query) {
        m_sql = new SQLInterface();
        boolean connected = m_sql.Connect(null, "jdbc:mysql://zeitgeistgames.com:3306/ColladaShop", "ColladaShopper", "ColladaShopperPassword");
        ArrayList<String[]> data = new ArrayList<String[]>();
        
        data = m_sql.Retrieve(query);
        int iNumData = m_sql.getNumColumns();
        ArrayList<String> temp = new ArrayList<String>();
        int counter = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < iNumData; j++) {
                temp.add(data.get(i)[j].toString());
                System.out.println("retrieved " + temp.get(counter));
                counter++;
            }
        }
        jList_ServerFiles.setSelectedIndex(-1);
        m_sql.Disconnect();
        return data;
    }
    
    public void loadServerFileChooser(int type) {
        DefaultListModel model = new DefaultListModel();
        String queryData = null;
        switch(type)
        {
            case 0: // Model (URL)
            {
                queryData = "SELECT name, description, bodytype, url, id FROM DefaultAvatars";
                m_data = loadSQLData(queryData);
                
                jTextArea_HelpText.setText("When loading avatars from your local folder, \n" +
                                           "remember to have the bind pose and animations \n" +
                                           "in the same folder with the words Bind and \n" +
                                           "Anim in the names respecitvely.");
                m_iLoadType = 0;
                
                // Default setup when you load up a model+
                m_meshes = new HashMap<Integer, String[]>();
                String[] meshNames = new String[2];
                meshNames[0] = "RHandShape";        meshNames[1] = "LHandShape";
                m_meshes.put(1, meshNames);
                
                meshNames = new String[2];
                meshNames[0] = "TorsoNudeShape";    meshNames[1] = "Torso_TorsoNudeShape";
                m_meshes.put(2, meshNames);
                
                meshNames = new String[2];
                meshNames[0] = "RFootNudeShape";    meshNames[1] = "LFootNudeShape";
                m_meshes.put(4, meshNames);
                
                meshNames = new String[1];
                meshNames[0] = "Legs_LegsNudeShape";
                m_meshes.put(3, meshNames);
                
                m_sceneData.setMeshSetup(m_meshes);
                break;
            }
            case 1: // Mesh (URL)
            {
                queryData = "SELECT name, description, bodytype, url, type, id FROM Meshes";
                m_data = loadSQLData(queryData);
                
                jTextArea_HelpText.setText("When loading any collada model remember to have \n" +
                                           "all necessary assets in the same folder.");
                m_iLoadType = 1;
                break;
            }
            case 2: // Tex (URL)
            {
                queryData = "SELECT name, description, bodytype, url, type FROM Textures";
                m_data = loadSQLData(queryData);
                
                jTextArea_HelpText.setText("When loading any texture on a model, make sure \n" +
                                           "you select the mesh you want the texture applied \n" +
                                           "on first");
                m_iLoadType = 2;
                break;
            }
        }
        
        for (int i = 0; i < m_data.size(); i++)
            model.addElement(m_data.get(i)[0].toString());
        
        jList_ServerFiles.setModel(model);
        jList_ServerFiles.setVisible(true);
        jScrollPane_FileSelector.setVisible(true);
        jPanel_ServerFileChooser.setVisible(true);
        m_fileChooser = new JFrame();
        m_fileChooser.add(jPanel_ServerFileChooser);
        m_fileChooser.pack();
        m_fileChooser.setVisible(true);
    }
    
////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    
////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setScene(SceneEssentials s) {
        m_sceneData = s;
    }
    
//    public void setLogger(LoggingModel l) {
//        m_logger = l;
//    }
    
    public void setRotPanel(JPanel_ModelRotation p) { 
        m_rotPanel = p; 
    }
    
    public void setAnimPanel(JPanel_Animations p) { 
        m_animPanel = p;
    }
 
    public void setPanel(SceneEssentials s, JPanel_ModelRotation rotp, JPanel_Animations animp) {
        m_sceneData = s;
        m_rotPanel = rotp;
        m_animPanel = animp;
        if (animp.getSelectedModelInstance() != null) {
            jComboBox_Meshes.setEnabled(true);
            jButton_LoadTexF.setEnabled(true);
            jButton_LoadTexU.setEnabled(true);
            loadMeshes();
        } else {
            jComboBox_Meshes.setEnabled(false);
            jButton_LoadTexF.setEnabled(false);
            jButton_LoadTexU.setEnabled(false);
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

        jFileChooser_Model = new javax.swing.JFileChooser();
        FileFilter configFilter = new FileFilter() {
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
            @Override
            public String getDescription() {
                String szDescription = new String("Extensible Markup Language (*.xml)");
                return szDescription;
            }
        };
        jFileChooser_Config = new javax.swing.JFileChooser();
        FileFilter texFilter = new FileFilter() {
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
                String szDescription = new String("Textures (*.jpg, *.png, *.gif, *.tga)");
                return szDescription;
            }
        };
        jFileChooser_Tex = new javax.swing.JFileChooser();
        FileFilter meshFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".dae")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Collada (*.dae)");
                return szDescription;
            }
        };
        jFileChooser_Mesh = new javax.swing.JFileChooser();
        jPanel_ServerFileChooser = new javax.swing.JPanel();
        jScrollPane_FileSelector = new javax.swing.JScrollPane();
        jList_ServerFiles = new javax.swing.JList();
        jLabel_FileChooserTitle = new javax.swing.JLabel();
        jScrollPane_HelpText = new javax.swing.JScrollPane();
        jTextArea_HelpText = new javax.swing.JTextArea();
        jButton_Cancel = new javax.swing.JButton();
        jButton_Accept = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel_Top = new javax.swing.JPanel();
        jButton_LoadModelURL = new javax.swing.JButton();
        jButton_LoadMeshURL = new javax.swing.JButton();
        jButton_LoadFile = new javax.swing.JButton();
        jButton_LoadFileG = new javax.swing.JButton();
        jButton_LoadConfig = new javax.swing.JButton();
        jButton_SaveConfig = new javax.swing.JButton();
        jButton_LoadSMesh = new javax.swing.JButton();
        jPanel_Bottom = new javax.swing.JPanel();
        jToolBar_SelectMesh = new javax.swing.JToolBar();
        jComboBox_Meshes = new javax.swing.JComboBox();
        jButton_LoadTexU = new javax.swing.JButton();
        jButton_LoadTexF = new javax.swing.JButton();

        jFileChooser_Model.setDialogTitle("Load Collada File");
        java.io.File modelDirectory = new java.io.File("./assets/models");
        jFileChooser_Model.setCurrentDirectory(modelDirectory);
        jFileChooser_Model.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        jFileChooser_Model.setDragEnabled(true);

        jFileChooser_Config.setDialogTitle("Load Extensible Markup Language File");
        java.io.File xmlDirectory = new java.io.File("./assets/");
        jFileChooser_Config.setCurrentDirectory(xmlDirectory);

        jFileChooser_Config.setDragEnabled(true);
        jFileChooser_Config.addChoosableFileFilter((FileFilter)configFilter);

        jFileChooser_Tex.setDialogTitle("Load Texture File");
        java.io.File texDirectory = new java.io.File("./assets/textures");
        jFileChooser_Tex.setCurrentDirectory(texDirectory);

        jFileChooser_Tex.setDragEnabled(true);
        jFileChooser_Tex.addChoosableFileFilter((FileFilter)texFilter);

        jFileChooser_Mesh.setDialogTitle("Load Collada File");
        java.io.File meshDirectory = new java.io.File("./assets/models");
        jFileChooser_Mesh.setCurrentDirectory(meshDirectory);

        jFileChooser_Mesh.setDragEnabled(true);
        jFileChooser_Mesh.addChoosableFileFilter((FileFilter)meshFilter);

        jPanel_ServerFileChooser.setPreferredSize(new java.awt.Dimension(394, 340));

        jList_ServerFiles.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_FileSelector.setViewportView(jList_ServerFiles);

        jLabel_FileChooserTitle.setBackground(new java.awt.Color(0, 0, 255));
        jLabel_FileChooserTitle.setForeground(new java.awt.Color(255, 0, 0));
        jLabel_FileChooserTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_FileChooserTitle.setText("Server File Selecter");
        jLabel_FileChooserTitle.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jTextArea_HelpText.setBackground(new java.awt.Color(204, 204, 204));
        jTextArea_HelpText.setColumns(20);
        jTextArea_HelpText.setEditable(false);
        jTextArea_HelpText.setRows(5);
        jTextArea_HelpText.setBorder(null);
        jScrollPane_HelpText.setViewportView(jTextArea_HelpText);

        jButton_Cancel.setText("Cancel");

        jButton_Accept.setText("Accept");
        jButton_Accept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadURL();
            }
        });

        org.jdesktop.layout.GroupLayout jPanel_ServerFileChooserLayout = new org.jdesktop.layout.GroupLayout(jPanel_ServerFileChooser);
        jPanel_ServerFileChooser.setLayout(jPanel_ServerFileChooserLayout);
        jPanel_ServerFileChooserLayout.setHorizontalGroup(
            jPanel_ServerFileChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel_FileChooserTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
            .add(jPanel_ServerFileChooserLayout.createSequentialGroup()
                .addContainerGap()
                .add(jButton_Cancel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 187, Short.MAX_VALUE)
                .add(jButton_Accept)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel_ServerFileChooserLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane_FileSelector, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                .addContainerGap())
            .add(jPanel_ServerFileChooserLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane_HelpText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel_ServerFileChooserLayout.setVerticalGroup(
            jPanel_ServerFileChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_ServerFileChooserLayout.createSequentialGroup()
                .add(jLabel_FileChooserTitle)
                .add(4, 4, 4)
                .add(jScrollPane_FileSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane_HelpText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_ServerFileChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton_Cancel)
                    .add(jButton_Accept))
                .add(0, 0, 0))
        );

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "File I/O", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setPreferredSize(new java.awt.Dimension(230, 250));

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel_Top.setOpaque(false);
        jPanel_Top.setPreferredSize(new java.awt.Dimension(230, 125));

        jButton_LoadModelURL.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadModelURL.setText("LoadModel (U)");
        jButton_LoadModelURL.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_LoadModelURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadServerFileChooser(0);
            }
        });

        jButton_LoadMeshURL.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadMeshURL.setText("LoadMesh (U)");
        jButton_LoadMeshURL.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_LoadMeshURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadServerFileChooser(1);
            }
        });

        jButton_LoadFile.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadFile.setText("LoadModel (F)");
        jButton_LoadFile.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_LoadFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadModelFile();
            }
        });

        jButton_LoadFileG.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadFileG.setText("LoadMesh (F)");
        jButton_LoadFileG.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_LoadFileG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMeshFile();
            }
        });

        jButton_LoadConfig.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadConfig.setText("LoadConfig (F)");
        jButton_LoadConfig.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_LoadConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadConfigFile();
            }
        });

        jButton_SaveConfig.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_SaveConfig.setText("SaveConfig (F)");
        jButton_SaveConfig.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_SaveConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConfigFile();
            }
        });

        jButton_LoadSMesh.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadSMesh.setText("Load Skinned Mesh (F)");
        jButton_LoadSMesh.setPreferredSize(new java.awt.Dimension(214, 29));
        jButton_LoadSMesh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSkinnedMesh();
            }
        });

        org.jdesktop.layout.GroupLayout jPanel_TopLayout = new org.jdesktop.layout.GroupLayout(jPanel_Top);
        jPanel_Top.setLayout(jPanel_TopLayout);
        jPanel_TopLayout.setHorizontalGroup(
            jPanel_TopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_TopLayout.createSequentialGroup()
                .add(jPanel_TopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel_TopLayout.createSequentialGroup()
                        .add(jButton_LoadModelURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton_LoadFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel_TopLayout.createSequentialGroup()
                        .add(jButton_LoadMeshURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton_LoadFileG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel_TopLayout.createSequentialGroup()
                        .add(jButton_LoadConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton_SaveConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jButton_LoadSMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_TopLayout.setVerticalGroup(
            jPanel_TopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_TopLayout.createSequentialGroup()
                .add(jPanel_TopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton_LoadModelURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_LoadFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_TopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton_LoadMeshURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_LoadFileG, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton_LoadSMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(7, 7, 7)
                .add(jPanel_TopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton_LoadConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_SaveConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setTopComponent(jPanel_Top);

        jPanel_Bottom.setPreferredSize(new java.awt.Dimension(230, 50));

        jToolBar_SelectMesh.setFloatable(false);
        jToolBar_SelectMesh.setRollover(true);

        jComboBox_Meshes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_Meshes.setPreferredSize(new java.awt.Dimension(91, 25));
        jToolBar_SelectMesh.add(jComboBox_Meshes);

        jButton_LoadTexU.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadTexU.setText("LoadTex (U)");
        jButton_LoadTexU.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_LoadTexU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadServerFileChooser(2);
            }
        });

        jButton_LoadTexF.setFont(new java.awt.Font("Arial Narrow", 0, 12)); // NOI18N
        jButton_LoadTexF.setText("LoadTex (F)");
        jButton_LoadTexF.setPreferredSize(new java.awt.Dimension(107, 29));
        jButton_LoadTexF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTexFile();
            }
        });

        org.jdesktop.layout.GroupLayout jPanel_BottomLayout = new org.jdesktop.layout.GroupLayout(jPanel_Bottom);
        jPanel_Bottom.setLayout(jPanel_BottomLayout);
        jPanel_BottomLayout.setHorizontalGroup(
            jPanel_BottomLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BottomLayout.createSequentialGroup()
                .add(jPanel_BottomLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar_SelectMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel_BottomLayout.createSequentialGroup()
                        .add(jButton_LoadTexU, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton_LoadTexF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_BottomLayout.setVerticalGroup(
            jPanel_BottomLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_BottomLayout.createSequentialGroup()
                .add(6, 6, 6)
                .add(jToolBar_SelectMesh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_BottomLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton_LoadTexU, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_LoadTexF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(545, 545, 545))
        );

        jSplitPane1.setRightComponent(jPanel_Bottom);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Accept;
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_LoadConfig;
    private javax.swing.JButton jButton_LoadFile;
    private javax.swing.JButton jButton_LoadFileG;
    private javax.swing.JButton jButton_LoadMeshURL;
    private javax.swing.JButton jButton_LoadModelURL;
    private javax.swing.JButton jButton_LoadSMesh;
    private javax.swing.JButton jButton_LoadTexF;
    private javax.swing.JButton jButton_LoadTexU;
    private javax.swing.JButton jButton_SaveConfig;
    private javax.swing.JComboBox jComboBox_Meshes;
    private javax.swing.JFileChooser jFileChooser_Config;
    private javax.swing.JFileChooser jFileChooser_Mesh;
    private javax.swing.JFileChooser jFileChooser_Model;
    private javax.swing.JFileChooser jFileChooser_Tex;
    private javax.swing.JLabel jLabel_FileChooserTitle;
    private javax.swing.JList jList_ServerFiles;
    private javax.swing.JPanel jPanel_Bottom;
    private javax.swing.JPanel jPanel_ServerFileChooser;
    private javax.swing.JPanel jPanel_Top;
    private javax.swing.JScrollPane jScrollPane_FileSelector;
    private javax.swing.JScrollPane jScrollPane_HelpText;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea_HelpText;
    private javax.swing.JToolBar jToolBar_SelectMesh;
    // End of variables declaration//GEN-END:variables

}
