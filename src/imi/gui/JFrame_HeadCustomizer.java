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
 * JFrame_HeadCustomizer.java
 *
 * Created on Jan 26, 2009, 4:51:21 PM
 */

package imi.gui;

import imi.character.Character;
import imi.character.EyeBall;
import imi.character.Manipulator;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import imi.utils.MaterialMeshUtils.ShaderType;
import imi.utils.MaterialMeshUtils.TextureType;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
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
    private WorldManager    worldManager    = null;
    private Character       character       = null;
    private int             numCol          = 1;
    private int             colWidth        = 64;
    private EyeBall[]       eyes            = null;
    private File            headsDir        = null;
    private File            eyesDir         = null;
    private File            hairDir         = null;
    private String          headsRelPath    = null;
    private String          eyesRelPath     = null;
    private String          hairRelPath     = null;
    private Vector          headModels      = null;
    private Vector          hairModels      = null;
    private Vector          meshNames       = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initializes the GUI components.  Before the tool is
     * usable the scene data must be set, the eye meshes must be set, and the
     * relative path (path from the user directory) to the head, eyes, and hair
     * assets must be set.  Finally set the selection listener on the tables
     */
    private JFrame_HeadCustomizer(Builder builder) {
        this.worldManager   = builder.worldManager;
        this.character      = builder.character;
        avatarCheck();

        initComponents();
        this.setTitle(builder.windowTitle);

        if (builder.eyes == null)
            findEyes();
        else {
            this.eyes = new EyeBall[builder.eyes.length];
            for (int i = 0; i < eyes.length; i++) {
                this.eyes[i] = builder.eyes[i];
            }
        }

        if (character.getCharacterParams().getGender() == 1) {
            builder.headsDir += "MaleHead/Thumbnails";
        } else {
            builder.headsDir += "FemaleHead/Thumbnails";
        }

        
        if (character.getCharacterParams().getGender() == 1) {
            builder.hairDir += "MaleHair/Thumbnails";
        } else {
            builder.hairDir += "FemaleHair/Thumbnails";
        }

        setDirectories(builder.headsDir, builder.hairDir, builder.eyesDir);
        setTable(jTable_Eyes, eyesDir);
        setTable(jTable_Heads, headsDir);
        setTable(jTable_HairStyles, hairDir);
        setListenersOnTables();
        getHeads();
        getHair();
    }

    public static class Builder {
        private Character       character       = null;
        private WorldManager    worldManager    = null;
        private EyeBall[]       eyes            = null;
        private String          windowTitle     = "Head Customizer";
        private String          headsDir        = "/assets/models/collada/Heads/";
        private String          eyesDir         = "/assets/models/collada/Heads/EyeTextures";
        private String          hairDir         = "/assets/models/collada/Hair/";

        public Builder (WorldManager worldManager, Character character) {
            this.worldManager   = worldManager;
            this.character      = character;
        }

        public Builder windowTitle(String windowTitle) {
            this.windowTitle    = windowTitle;
            return this;
        }
        
        public Builder headsDirectory(String headDir) {
            this.headsDir   = headDir;
            return this;
        }
        
        public Builder eyesDirectory(String eyesDir) {
            this.eyesDir    = eyesDir;
            return this;
        }
        
        public Builder hairDirectory(String hairDir) {
            this.hairDir    = hairDir;
            return this;
        }

        public Builder eyes(EyeBall[] eyes) {
            this.eyes = new EyeBall[eyes.length];
            for (int i = 0; i < eyes.length; i++) {
                this.eyes[i] = eyes[i];
            }
            return this;
        }

        public JFrame_HeadCustomizer build() {
            return new JFrame_HeadCustomizer(this);
        }
    }

    private void avatarCheck() {
        if (character == null) {
            throw new IllegalArgumentException("SEVERE ERROR: character is null");
        }
        if (character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: character has no SkeletonNode");
        }
        if (!character.isInitialized()) {
            throw new IllegalArgumentException("SEVERE ERROR: character has not been initialized");
        }
    }

    /**
     * Grabs the selected texture and applys the texture to the mesh material and
     * applies the eyeball shader to the mesh
     * @param row - the selected row
     * @param col - the selected coloumn
     */
    public void setTextureOnEyeball(int row, int col) {
        avatarCheck();
        String temp = jTable_Eyes.getValueAt(row, col).toString();
        String location = temp.substring(1, temp.length() - 1);
        Manipulator.setEyesTexture(character, location, TextureType.Color, Manipulator.Eyes.allEyes);
    }

    /**
     * Grabs the selected head file (*.dae) from the arraylist of heads and adds
     * the model to the avatar.  Avatar must be loaded already.  Checks for an
     * avatar before it continues;
     * @param row - the selected row
     * @param col - the selected coloumn
     */
    public void addHead(int row, int col) {
        avatarCheck();
        String temp     = ((Vector)headModels.get(row)).get(col).toString();
        String location = temp.substring(1, temp.length() - 1);
        Manipulator.swapHeadMesh(character, true, new File(location), ShaderType.FleshShader);
    }

    /**
     * Grabs the selected hair (*.dae) from the arraylist of hair and adds
     * the model to the avatar's head.  Avatar must be loaded already.  Checks for
     * an avatar before it continues;
     * @param row - the selected row
     * @param col - the selected column
     */
    public void addHair(int row, int col) {
        avatarCheck();
        Vector data     = (Vector) meshNames.get(row);
        Manipulator.swapHairMesh(character, true, new File(data.get(1).toString()), data.get(0).toString());
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
    
////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setHeadsDirectory(String relativePath) {
        String completePath = System.getProperty("user.dir") + relativePath;
        headsDir = new File(completePath);
    }

    public void setHairDirectory(String relativePath) {
        String completePath = System.getProperty("user.dir") + relativePath;
        hairDir = new File(completePath);
    }

    public void setEyesDirectory(String relativePath) {
        String completePath = System.getProperty("user.dir") + relativePath;
        eyesDir = new File(completePath);
    }

    public void setDirectories(String headRelPath, String hairRelPath, String eyesPath) {
        this.headsRelPath  = headRelPath;
        this.hairRelPath   = hairRelPath;
        this.eyesRelPath   = eyesPath;
        setHeadsDirectory(headRelPath);
        setHairDirectory(hairRelPath);
        setEyesDirectory(eyesPath);
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
            for (int j = 0; j < numCol; j++) {
                if (i >= textures.length)
                    break;

                rowData.add(Arrays.asList(textures[i]));
                i++;
            }
            while (rowData.size() < numCol) {
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
        Vector colNames;
        String[] colName  = new String[numCol];
        for (int i = 0; i < numCol; i++ ) {
            colName[i] = "    ";
        }
        colNames = new Vector(Arrays.asList(colName));

        DefaultTableModel model = new DefaultTableModel(data, colNames);
        table.setModel(model);

        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setCellRenderer(new CustomImageCellRender());
            col.setPreferredWidth(colWidth);
        }

        table.setVisible(true);
    }

    /**
     * Custom cell renderer for the JTable to display preview images of the avatar
     */
    public static class CustomImageCellRender extends JLabel implements TableCellRenderer {

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
                if (type == 0) {
                    setTextureOnEyeball(row, col);
                } else if (type == 1) {
                    addHead(row, col);
                    findEyes();
                } else if (type == 2)
                    addHair(row, col);

            } else if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed() ){
                // Row selection changed
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (type == 0) {
                    setTextureOnEyeball(row, col);
                } else if (type == 1) {
                    addHead(row, col);
                    findEyes();
                } else if (type == 2)
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
        avatarCheck();
        if (character.getSkeleton().getMeshesBySubGroup("Head") != null) {
            PPolygonSkinnedMeshInstance[] meshes = character.getSkeleton().getMeshesBySubGroup("Head");

            if (meshes != null) {
                ArrayList<PPolygonSkinnedMeshInstance> ppm = new ArrayList<PPolygonSkinnedMeshInstance>();
                for (int i = 0; i < meshes.length; i++) {
                    if (meshes[i].getName().toLowerCase().contains("eye")) {
                        ppm.add(meshes[i]);
                    }
                }
                
                this.eyes = new EyeBall[ppm.size()];
                for (int i = 0; i < ppm.size(); i++)
                    this.eyes[i] = (EyeBall) ppm.get(i);
            }
        }
    }

    /**
     * Creates a vector of the available heads in the heads directory to use as
     * reference when selecting heads to load in realtime using the heads table
     */
    public void getHeads() {

        FilenameFilter asset = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".dae"))
                    return true;
                return false;
            }
        };

        int indes       = headsDir.getPath().lastIndexOf("/");
        String newPath  = headsDir.getPath().substring(0, indes);
        File newFile    = new File(newPath);
        File[] textures = newFile.listFiles(asset);

        if (textures == null)
            return;

        headModels = new Vector();

        int i = 0;
        while (i < textures.length) {
            Vector rowData = new Vector();
            for (int j = 0; j < numCol; j++) {
                if (i >= textures.length)
                    break;

                rowData.add(Arrays.asList(textures[i]));
                i++;
            }
            while (rowData.size() < numCol) {
                rowData.add(null);
            }
            headModels.add(rowData);
        }
    }

    /**
     * Creates a vector of the available hair names and files to use when the
     * add hair method is called.
     */
    public void getHair() {

        FilenameFilter asset = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".dae"))
                    return true;
                return false;
            }
        };

        FilenameFilter meshes = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".txt"))
                    return true;
                return false;
            }
        };

        int indes       = hairDir.getPath().lastIndexOf("/");
        String newPath  = hairDir.getPath().substring(0, indes);
        File newFile    = new File(newPath);
        File[] textures = newFile.listFiles(asset);
        File[] meshData = newFile.listFiles(meshes);

        if (textures == null || meshData == null)   // Missing collada file and datafile
            return;

        if (textures.length != meshData.length) // Missing a file should be 1:1
            return;

        for (int i = 0; i < meshData.length; i++) {
            readInMeshNames(meshData[i], textures[i]);
        }

        hairModels = new Vector();

        int i = 0;
        while (i < textures.length) {
            Vector rowData = new Vector();
            for (int j = 0; j < numCol; j++) {
                if (i >= textures.length)
                    break;

                rowData.add(Arrays.asList(textures[i]));
                i++;
            }
            while (rowData.size() < numCol) {
                rowData.add(null);
            }
            hairModels.add(rowData);
        }
    }

    /**
     * Reads in the name of the meshes for the hair from an external file.
     * @param meshNames - file location for the mesh names data
     * @param actualFile - the actual collada file to associate with the file.
     */
    public void readInMeshNames(File meshNamesFolder, File actualFile) {
        if (meshNames == null) {
            meshNames = new Vector();
        }

        try {

            BufferedReader input = new BufferedReader(new FileReader(meshNamesFolder));
            try {
                String meshName = null; //not declared within while loop
                /**
                 * Reads till new lint but won't  contain new line and returns null
                 * when there is nothing else to read.
                 */
                while ((meshName = input.readLine()) != null) {
                    Vector meshInfo = new Vector();
                    meshInfo.add(meshName);
                    meshInfo.add(actualFile);
                    meshNames.add(meshInfo);
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
