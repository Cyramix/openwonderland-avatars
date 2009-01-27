/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JFrame_HeadCustomizer.java
 *
 * Created on Jan 26, 2009, 4:51:21 PM
 */

package imi.gui;

import imi.loaders.repository.Repository;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.programs.EyeballShader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JFrame_HeadCustomizer extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private int                             m_numCol        = 1;
    private int                             m_colWidth      = 64;
    private PPolygonSkinnedMeshInstance[]   m_Eyes          = null;
    private SceneEssentials                 m_sceneData     = null;
    private File                            m_HeadsDir      = null;
    private File                            m_EyesDir       = null;
    private File                            m_HairDir       = null;
    private Vector                          m_HeadModels    = null;
    private Vector                          m_HairModels    = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initializes the GUI components.  Before the tool is
     * usable the scene data must be set, the eye meshes must be set, and the
     * relative path (path from the user directory) to the head, eyes, and hair
     * assets must be set.  Finally set the selection listener on the tables
     */
    public JFrame_HeadCustomizer() {
        initComponents();
    }

    /**
     * Overloaded constructor initializes the GUI compoents as well as sets the
     * scene data, eye meshes and the relative directory paths for the directory
     * of assets for head, hair and eyes.  If null is passed in for the directories
     * or eyes, then the directories will default to a set default directory and
     * a search to see if there is an avatar with eyes is available.  Scene data
     * must NOT be null.
     * @param scene - the data of the entire scene
     * @param headDirec - relative string path to the head assets
     * @param eyesDirec - relative string path to the eyes assets
     * @param hairDirec - relative string path to the hair assets
     * @param eyes - an array of eye mesh instnaces for the avatar (should be 2 only ie human)
     */
    public JFrame_HeadCustomizer(SceneEssentials scene, String headDirec, String eyesDirec, String hairDirec, PPolygonSkinnedMeshInstance[] eyes) {
        initComponents();
        m_sceneData = scene;

        if (eyes == null)
            findEyes();
        else
            m_Eyes = eyes;

        if (headDirec == null) {
            headDirec = "/assets/models/collada/Heads/";
            if (m_sceneData.getAvatar() != null && m_sceneData.getAvatar().isInitialized()) {
                if (m_sceneData.getAvatar().getAttributes().getGender() == 1) {
                    headDirec += "MaleHead/Thumbnails";
                } else {
                    headDirec += "FemaleHead/Thumbnails";
                }
            }
        }

        if (hairDirec == null) {
            hairDirec = "/assets/models/collada/Hair/";
            if (m_sceneData.getAvatar() != null && m_sceneData.getAvatar().isInitialized()) {
                if (m_sceneData.getAvatar().getAttributes().getGender() == 1) {
                    hairDirec += "MaleHair/Thumbnails";
                } else {
                    hairDirec += "FemaleHair/Thumbnails";
                }
            }
        }

        if (eyesDirec == null)
            eyesDirec = "/assets/models/collada/Heads/EyeTextures";

        setDirectories(headDirec, hairDirec, eyesDirec);
        setTable(jTable_Eyes, m_EyesDir);
        setTable(jTable_Heads, m_HeadsDir);
        setTable(jTable_HairStyles, m_HairDir);
        setListenersOnTables();
        getHeads();
    }

    /**
     * Grabs the selected texture and applys the texture to the mesh material and
     * applies the eyeball shader to the mesh
     * @param row - the selected row
     * @param col - the selected coloumn
     */
    public void setTextureOnEyeball(int row, int col) {
        if (m_Eyes == null)
            return;

        // Create a material to use
        String temp = jTable_Eyes.getValueAt(row, col).toString();
        String location = temp.substring(1, temp.length() - 1);
        File loc = new File(location);
        String szName = loc.getName();
        try {
            URL urlFile = loc.toURI().toURL();
            PMeshMaterial material = new PMeshMaterial(szName + "Material", urlFile);
            Repository repo = (Repository)m_sceneData.getWM().getUserData(Repository.class);
            AbstractShaderProgram eyeballShader = repo.newShader(EyeballShader.class);

            for (int i = 0; i < m_Eyes.length; i++) {
                material.setShader(eyeballShader);
                m_Eyes[i].setMaterial(material);
                m_Eyes[i].applyMaterial();
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Grabs the selected head file (*.dae) from the arraylist of heads and adds
     * the model to the avatar.  Avatar must be loaded already.  Checks for an
     * avatar before it continues;
     * @param row - the selected row
     * @param col - the selected coloumn
     */
    public void addHead(int row, int col) {
        if (m_sceneData == null)
            return;

        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        String protocol = "file:///";
        String temp     = ((Vector)m_HeadModels.get(row)).get(col).toString();
        String location = temp.substring(1, temp.length() - 1);
        File tempFile   = new File(location);
        String url      = protocol + location;
        int index       = tempFile.toString().indexOf(".");
        String relPath  = tempFile.toString().substring(index + 1);

        m_sceneData.addAvatarHeadDAEURL(true, this, url, relPath);
    }

    /**
     * Grabs the selected hair (*.dae) from the arraylist of hair and adds
     * the model to the avatar's head.  Avatar must be loaded already.  Checks for
     * an avatar before it continues;
     * @param row - the selected row
     * @param col - the selected column
     */
    public void addHair(int row, int col) {

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

        jTabbedPane_Main = new javax.swing.JTabbedPane();
        jPanel_HeadHairMain = new javax.swing.JPanel();
        jTabbedPane_HeadHair = new javax.swing.JTabbedPane();
        jPanel_Head = new javax.swing.JPanel();
        jScrollPane_Heads = new javax.swing.JScrollPane();
        jTable_Heads = new javax.swing.JTable();
        jPanel_Hair = new javax.swing.JPanel();
        jScrollPane_HairStyles = new javax.swing.JScrollPane();
        jTable_HairStyles = new javax.swing.JTable();
        jPanel_Eyes = new javax.swing.JPanel();
        jScrollPane_Eyes = new javax.swing.JScrollPane();
        jTable_Eyes = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTabbedPane_HeadHair.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel_Head.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Heads.setMinimumSize(new java.awt.Dimension(128, 27));
        jScrollPane_Heads.setPreferredSize(new java.awt.Dimension(128, 404));

        jTable_Heads.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "    "
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_Heads.setRowHeight(100);
        jScrollPane_Heads.setViewportView(jTable_Heads);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        jPanel_Head.add(jScrollPane_Heads, gridBagConstraints);

        jTabbedPane_HeadHair.addTab("Heads", jPanel_Head);

        jPanel_Hair.setLayout(new java.awt.GridBagLayout());

        jScrollPane_HairStyles.setMinimumSize(new java.awt.Dimension(128, 23));
        jScrollPane_HairStyles.setPreferredSize(new java.awt.Dimension(128, 404));

        jTable_HairStyles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "    "
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_HairStyles.setRowHeight(100);
        jScrollPane_HairStyles.setViewportView(jTable_HairStyles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        jPanel_Hair.add(jScrollPane_HairStyles, gridBagConstraints);

        jTabbedPane_HeadHair.addTab("Hair Styles", jPanel_Hair);

        javax.swing.GroupLayout jPanel_HeadHairMainLayout = new javax.swing.GroupLayout(jPanel_HeadHairMain);
        jPanel_HeadHairMain.setLayout(jPanel_HeadHairMainLayout);
        jPanel_HeadHairMainLayout.setHorizontalGroup(
            jPanel_HeadHairMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 212, Short.MAX_VALUE)
            .addGroup(jPanel_HeadHairMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane_HeadHair, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
        );
        jPanel_HeadHairMainLayout.setVerticalGroup(
            jPanel_HeadHairMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 502, Short.MAX_VALUE)
            .addGroup(jPanel_HeadHairMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane_HeadHair, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE))
        );

        jTabbedPane_Main.addTab("Head & Hair", jPanel_HeadHairMain);

        jPanel_Eyes.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Eyes.setMinimumSize(new java.awt.Dimension(128, 27));
        jScrollPane_Eyes.setPreferredSize(new java.awt.Dimension(128, 404));

        jTable_Eyes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "    "
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_Eyes.setRowHeight(100);
        jScrollPane_Eyes.setViewportView(jTable_Eyes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        jPanel_Eyes.add(jScrollPane_Eyes, gridBagConstraints);

        jTabbedPane_Main.addTab("Eyes", jPanel_Eyes);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane_Main)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane_Main, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame_HeadCustomizer().setVisible(true);
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    
////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setHeadsDirectory(String relativePath) {
        String completePath = System.getProperty("user.dir") + relativePath;
        m_HeadsDir = new File(completePath);
    }

    public void setHairDirectory(String relativePath) {
        String completePath = System.getProperty("user.dir") + relativePath;
        m_HairDir = new File(completePath);
    }

    public void setEyesDirectory(String relativePath) {
        String completePath = System.getProperty("user.dir") + relativePath;
        m_EyesDir = new File(completePath);
    }

    public void setDirectories(String headRelPath, String hairRelPath, String eyesPath) {
        setHeadsDirectory(headRelPath);
        setHairDirectory(hairRelPath);
        setEyesDirectory(eyesPath);
    }

    public void setSceneData(SceneEssentials scene) {
        m_sceneData = scene;
    }

    public void setEyeMeshInstances(PPolygonSkinnedMeshInstance[] eyes) {
        m_Eyes = eyes;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel_Eyes;
    private javax.swing.JPanel jPanel_Hair;
    private javax.swing.JPanel jPanel_Head;
    private javax.swing.JPanel jPanel_HeadHairMain;
    private javax.swing.JScrollPane jScrollPane_Eyes;
    private javax.swing.JScrollPane jScrollPane_HairStyles;
    private javax.swing.JScrollPane jScrollPane_Heads;
    private javax.swing.JTabbedPane jTabbedPane_HeadHair;
    private javax.swing.JTabbedPane jTabbedPane_Main;
    private javax.swing.JTable jTable_Eyes;
    private javax.swing.JTable jTable_HairStyles;
    private javax.swing.JTable jTable_Heads;
    // End of variables declaration//GEN-END:variables
////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    /**
     * Parses the JFile containing the directory location to the assets to
     * be used to set the JTable with the selctions
     * @return string double array
     */
    public Vector formatTableData(File dir) {
        if (dir == null)
            return null;

        FilenameFilter images = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") ||
                    name.toLowerCase().endsWith(".bmp") || name.toLowerCase().endsWith(".gif") || name.toLowerCase().endsWith(".tga"))
                    return true;
                return false;
            }
        };

        File[] textures = dir.listFiles(images);
        Vector data = new Vector();

        int i = 0;
        while (i < textures.length) {
            Vector rowData = new Vector();
            for (int j = 0; j < m_numCol; j++) {
                if (i >= textures.length)
                    break;

                rowData.add(Arrays.asList(textures[i]));
                i++;
            }
            while (rowData.size() < m_numCol) {
                rowData.add(null);
            }
            data.add(rowData);
        }

        return data;
    }

    /**
     * Sets the JTable to display the folder full of assets
     */
    public void setTable(JTable table, File dir) {
        if (dir == null)
            return;

        Vector data = formatTableData(dir);
        Vector colNames = new Vector();
        String[] colName  = new String[m_numCol];
        for (int i = 0; i < m_numCol; i++ ) {
            colName[i] = "    ";
        }
        colNames = new Vector(Arrays.asList(colName));

        DefaultTableModel model = new DefaultTableModel(data, colNames);
        table.setModel(model);

        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setCellRenderer(new customImageCellRender());
            col.setPreferredWidth(m_colWidth);
        }

        table.setVisible(true);
    }

    /**
     * Custom cell renderer for the JTable to display preview images of the avatar
     */
    public class customImageCellRender extends JLabel implements TableCellRenderer {

        private Border m_selectBorder   = null;
        private Border m_unselectBorder = null;
        private float  m_resizeFactor   = 0.45f;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (value != null) {

                String location = value.toString().substring(1, value.toString().length() - 1);
                File file = new File(location);

                try {
                    URL loc = file.toURI().toURL();
                    ImageIcon icon = new ImageIcon(loc);
                    Image image = icon.getImage();

                    int width   = (int)(m_resizeFactor * image.getWidth(null));
                    int height  = (int)(m_resizeFactor * image.getHeight(null));

                    Image newImage  = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    ImageIcon newImageIcon = new ImageIcon(newImage);

                    setIcon(newImageIcon);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(JPanel_HeadOptions.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (isSelected) {
                table.setSelectionBackground(new Color(0, 255, 0));
                if (m_selectBorder == null) {
                    m_selectBorder = BorderFactory.createLineBorder(new Color(0, 255, 0), 3);
                }

                this.setBorder(m_selectBorder);
            } else {
                if (m_unselectBorder == null) {
                    m_unselectBorder = BorderFactory.createLineBorder(table.getBackground(), 0);
                }
                this.setBorder(m_unselectBorder);
            }

            return this;
        }
    }

    /**
     * Custom selection listener for selecting objects from the table
     */

    public class SelectionListener implements ListSelectionListener {
        JTable  table;
        int     type;

        public SelectionListener(JTable table, int type) {
            this.table  = table;
            this.type   = type;
        }

        public void valueChanged(ListSelectionEvent e) {

            if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
                // Column selection changed
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (type == 0)
                    setTextureOnEyeball(row, col);
                else if (type == 1)
                    addHead(row, col);
                else if (type == 2)
                    addHair(row, col);

            } else if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed() ){
                // Row selection changed
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (type == 0)
                    setTextureOnEyeball(row, col);
                else if (type == 1)
                    addHead(row, col);
                else if (type == 2)
                    addHair(row, col);
            }

            if (e.getValueIsAdjusting()) {
                return;
            }
        }
    }

    /**
     * Sets a selection listener on the tables
     */
    public void setListenersOnTables() {
        jTable_Eyes.getSelectionModel().addListSelectionListener(new SelectionListener(jTable_Eyes, 0));
        jTable_Heads.getSelectionModel().addListSelectionListener(new SelectionListener(jTable_Heads, 1));
        jTable_HairStyles.getSelectionModel().addListSelectionListener(new SelectionListener(jTable_HairStyles, 2));
    }

    /**
     * Finds the eyes of the avatar in the scene (2 eyes max + only 1 avatar in
     * the scene).  If there is no scene datar, or there is no avatar then it will
     * return without doing anything.
     */
    public void findEyes() {
        if (m_sceneData == null)
            return;

        if (m_sceneData.getAvatar() == null || !m_sceneData.getAvatar().isInitialized())
            return;

        if (m_sceneData.getAvatar().getSkeleton().getMeshesBySubGroup("Head") != null) {
            PPolygonSkinnedMeshInstance[] meshes = m_sceneData.getAvatar().getSkeleton().getMeshesBySubGroup("Head");

            if (meshes != null) {
                ArrayList<PPolygonSkinnedMeshInstance> ppm = new ArrayList<PPolygonSkinnedMeshInstance>();
                for (int i = 0; i < meshes.length; i++) {
                    if (meshes[i].getName().toLowerCase().contains("eye")) {
                        ppm.add(meshes[i]);
                    }
                }
                
                m_Eyes = new PPolygonSkinnedMeshInstance[ppm.size()];
                for (int i = 0; i < ppm.size(); i++)
                    m_Eyes[i] = ppm.get(i);
            }
        }
    }

    public void getHeads() {

        FilenameFilter asset = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".dae"))
                    return true;
                return false;
            }
        };

        int indes       = m_HeadsDir.getPath().lastIndexOf("/");
        String newPath  = m_HeadsDir.getPath().substring(0, indes);
        File newFile    = new File(newPath);
        File[] textures = newFile.listFiles(asset);

        if (textures == null)
            return;

        m_HeadModels = new Vector();

        int i = 0;
        while (i < textures.length) {
            Vector rowData = new Vector();
            for (int j = 0; j < m_numCol; j++) {
                if (i >= textures.length)
                    break;

                rowData.add(Arrays.asList(textures[i]));
                i++;
            }
            while (rowData.size() < m_numCol) {
                rowData.add(null);
            }
            m_HeadModels.add(rowData);
        }
    }

    public void getHair() {

    }
}
