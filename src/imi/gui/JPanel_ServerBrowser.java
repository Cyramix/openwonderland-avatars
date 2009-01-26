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

import imi.character.CharacterAttributes;
import imi.sql.SQLInterface;
import java.awt.Cursor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JPanel_ServerBrowser extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /** SQL Interface */
    private SQLInterface            m_sql;
    private ArrayList<String[]>     m_data, m_anim;
    private Map<Integer, String[]>  m_meshes;
    /** Return Data */
    private String[]                m_modelInfo, m_animInfo, m_meshref;
    private String                  m_region, m_prevAttch;
    private int                     m_loadType;
    private SceneEssentials         m_sceneData;

////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initializes the GUI components and sets up a row
     * selection listener for the JTable
     */
    public JPanel_ServerBrowser() {
        initComponents();

        ListSelectionModel rowSelection = jTable1.getSelectionModel();
        rowSelection.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel rowSelect = (ListSelectionModel) e.getSource();
                int selectedIndex = rowSelect.getMinSelectionIndex();
                if (selectedIndex != -1) {
                    getSelectedData(selectedIndex);
                    jButton_Load.setEnabled(true);
                }
            }
        });
    }

    /**
     * Initializes the JTable with server side information on the assets stored
     * and set in the table for preview
     * @param dataType - integer that represents what type of assets to display
     * in the table
     */
    public void initBrowser(int dataType) {
        String query = new String();
        String[] columnNames = {"Name", "Description"};
        String[] filterTypes = {"Avatar Models", "Clothing", "Accessories", "Textures"};
        DefaultComboBoxModel model = new DefaultComboBoxModel(filterTypes);
        jComboBox_Filter.setModel(model);
        jComboBox_Filter.setSelectedIndex(dataType);

        Object[][] data = null;

        switch(dataType)
        {
            case 0:         // AVATAR (SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, id FROM DefaultAvatars";
                m_data = loadSQLData(query);
                filterResults("0");
                break;
            }
            case 1:         // CLOTHES (SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, type, id FROM Meshes";
                m_data = loadSQLData(query);
                filterResults("2");
                break;
            }
            case 2:         // ACCESSORIES (NON-SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, type, id FROM Meshes";
                m_data = loadSQLData(query);
                filterResults("1");
                break;
            }
            case 3:         // TEXTURES (JPEG, TARGA, PNG, GIF)
            {
                query = "SELECT name, description, bodytype, url, type FROM Textures";
                m_data = loadSQLData(query);
                break;
            }
        }

        data = new Object[m_data.size()][columnNames.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = m_data.get(i)[j];
            }
        }

        DefaultTableModel tabModel = new DefaultTableModel(data, columnNames);
        jTable1.setModel(tabModel);
        m_loadType = dataType;
        jButton_Load.setEnabled(false);
    }

    /**
     * Updates the JTable information displayed based on the combobox filter
     * that have been selected by the user
     */
    public void updateBrowser() {
        int selection = jComboBox_Filter.getSelectedIndex();
        String query = new String();
        String[] columnNames = {"Name", "Description"};
        Object[][] data = null;

        switch(selection)
        {
            case 0:         // AVATAR (SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, id FROM DefaultAvatars";
                m_data = loadSQLData(query);
                filterResults("0");
                break;
            }
            case 1:         // CLOTHES (SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, type, id FROM Meshes";
                m_data = loadSQLData(query);
                filterResults("2");
                break;
            }
            case 2:         // ACCESSORIES (NON-SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, type, id FROM Meshes";
                m_data = loadSQLData(query);
                filterResults("1");
                break;
            }
            case 3:         // TEXTURES (JPEG, TARGA, PNG, GIF)
            {
                query = "SELECT name, description, bodytype, url, type FROM Textures";
                m_data = loadSQLData(query);
                break;
            }
        }

        data = new Object[m_data.size()][columnNames.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = m_data.get(i)[j];
            }
        }

        DefaultTableModel tabModel = new DefaultTableModel(data, columnNames);
        jTable1.removeAll();
        jTable1.setModel(tabModel);
        m_loadType = selection;
        jButton_Load.setEnabled(false);
    }

    /**
     * Filters the displayable results in the browser window
     * @param typeFilter - the type of filtering
     */
    public void filterResults(String typeFilter) {
        ArrayList<String[]> genderFiltered = new ArrayList<String[]>();

        if (jCheckBox_FilterMale.isSelected()) {
            for (int i = 0; i < m_data.size(); i++) {
                if (m_data.get(i)[2].equals("1") || m_data.get(i)[2].equals("3"))
                    genderFiltered.add(m_data.get(i));
            }
        } else {
            for (int i = 0; i < m_data.size(); i++) {
                if (m_data.get(i)[2].equals("2") || m_data.get(i)[2].equals("3"))
                    genderFiltered.add(m_data.get(i));
            }
        }

        m_data.clear();
        m_data = new ArrayList<String[]>(genderFiltered);
        genderFiltered.clear();

        if (typeFilter.equals("1")) {
            for (int i = 0; i < m_data.size(); i++) {
                if (m_data.get(i)[4].equals("0") || m_data.get(i)[4].equals("1") || m_data.get(i)[4].equals("2"))
                    genderFiltered.add(m_data.get(i));
            }
            m_data.clear();
            m_data = new ArrayList<String[]>(genderFiltered);
            genderFiltered.clear();
        } else  if (typeFilter.equals("2")) {
            for (int i = 0; i < m_data.size(); i++) {
                if (!m_data.get(i)[4].equals("0") && !m_data.get(i)[4].equals("1") && !m_data.get(i)[4].equals("2"))
                    genderFiltered.add(m_data.get(i));
            }
            m_data.clear();
            m_data = new ArrayList<String[]>(genderFiltered);
            genderFiltered.clear();
        }
    }

    /**
     * Retrieves the avatar information from the server that the user has selected
     * @param selection - index of selection
     */
    public void getSelectedData(int selection) {
        //int selection = jTable1.getSelectedRow();
        m_modelInfo = m_data.get(selection);
        if (m_loadType == 0) {

        } else {
            String query = "SELECT name, grouping FROM GeometryReferences WHERE referenceid = ";
            query += m_modelInfo[5].toString();
            ArrayList<String[]> ref = loadSQLData(query);
            
            m_meshref = new String[ref.size()];
            for(int i = 0; i < ref.size(); i++)
                m_meshref[i] = ref.get(i)[0];

            if (m_modelInfo[4].equals("0") || m_modelInfo[4].equals("1") || m_modelInfo[4].equals("2")) {
                m_region = "Head";
            } else if (m_modelInfo[4].equals("3")) {
                m_region = "UpperBody";
            } else if (m_modelInfo[4].equals("5") || m_modelInfo[4].equals("6")) {
                m_region = "LowerBody";
            } else if (m_modelInfo[4].equals("10")) {
                m_region = "Feet";
            }
        }
    }

    /**
     * Opens a connection to the mySQL database and retrieves data asked for in
     * the string query in the form of an ArrayList of String arrays.  When query
     * is complete the connection is closed.
     * @param query - string containing a syntax correct query for the database
     * @return ArrayList of string arrays containing the data requested
     */
    public ArrayList<String[]> loadSQLData(String query) {
        m_sql = new SQLInterface();
        boolean connected = m_sql.Connect(null, "jdbc:mysql://zeitgeistgames.com:3306/ColladaShop", "ColladaShopper", "ColladaShopperPassword");
        ArrayList<String[]> data = new ArrayList<String[]>();

        data = m_sql.Retrieve(query);
        int iNumData = m_sql.getNumColumns();
        ArrayList<String> temp = new ArrayList<String>();
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < iNumData; j++) {
                temp.add(data.get(i)[j].toString());
            }
        }

        System.out.println("sql query complete");
        m_sql.Disconnect();
        return data;
    }

    /**
     * Loads the specified model based on filters and selections of use.  If
     * selection is for viewing then only the model is loaded and all other objects
     * in the scene are cleared.  If loaded for view on avatar, the appropriate
     * mesh is replaced on the avatar (avatar must be loaded first).
     */
    public void executeLoad() {
        if (jTable1.getSelectedRow() == -1)
            return;

        if (!jButton_Load.isEnabled())
            return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        switch(m_loadType)
        {
            case 0:         // LOAD AVATAR
            {
                int iGender = -1;
                String headLocation = null;
                String handLocation = null;
                if (isMale()) {
                    iGender = 1;
                    headLocation = "http://www.zeitgeistgames.com/assets/collada/Avatars/Head/MHead6/MaleCHead_Bind.dae";
                    handLocation = "http://www.zeitgeistgames.com/assets/collada/Avatars/Male/Male_Hands.dae";
                } else {
                    iGender = 2;
                }
                if (m_modelInfo[4].equals("1") || m_modelInfo[4].equals("2"))
                    m_sceneData.loadAvatarDAEURL(true, this, m_modelInfo[3], headLocation, handLocation, null, iGender);
                else
                    m_sceneData.loadAvatarHeadDAEURL(true, this, m_modelInfo, m_meshref);
                break;
            }
            case 1:         // LOAD CLOTHES
            {
                if (isViewMode())
                    m_sceneData.loadSMeshDAEURL(true, this, m_modelInfo, m_meshref);
                else if (m_sceneData.getAvatar() != null) {
                    if (m_sceneData.getAvatar().isInitialized() && m_sceneData.getAvatar().getModelInst() != null) {
                        try {
                            URL location = new URL(m_modelInfo[3]);
                            m_sceneData.addSMeshDAEURLToModel(location, m_region);
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(JPanel_ServerBrowser.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                break;
            }
            case 2:         // LOAD ACCESSORIES
            {
                if (isViewMode())
                    m_sceneData.loadMeshDAEURL(true, this, m_modelInfo);
                else if (m_sceneData.getAvatar() != null) {
                    if (m_sceneData.getAvatar().isInitialized() || m_sceneData.getAvatar().getModelInst() != null) {

                        String subgroup = null;
                        if (m_modelInfo[4].equals("0"))
                            subgroup = "Hair";
                        else if (m_modelInfo[4].equals("11"))
                            subgroup = "FacialHair";
                        else if (m_modelInfo[4].equals("1"))
                            subgroup = "Hats";
                        else if (m_modelInfo[4].equals("2"))
                            subgroup = "Glasses";

                        m_sceneData.addMeshDAEURLToModel(m_modelInfo, "Head", subgroup);
                        m_prevAttch = m_modelInfo[0];
                    }
                }
                break;
            }
            case 3:         // LOAD TEXTURES
            {
                // TODO: server table needs to be created with all the textures
                break;
            }
        }
        m_sceneData.setCameraOnModel();
        setCursor(null);
    }

    /** Accessors */
    public ArrayList<String[]> getRawFileData() {
        return m_data;
    }

    public ArrayList<String[]> getRawAnimData() {
        return m_anim;
    }

    public Map<Integer, String[]> getDefaultAvatarMeshes() {
        return m_meshes;
    }

    public String[] getModelInfo() {
        return m_modelInfo;
    }

    public String[] getAnimInfo() {
        return m_animInfo;
    }

    public int getLoadType() {
        return m_loadType;
    }

    public SceneEssentials getSceneData() {
        return m_sceneData;
    }

    public boolean isReplace() {
        return jCheckBox_FilterView.isSelected();
    }

    public boolean isViewMode() {
        return jCheckBox_FilterView.isSelected();
    }
    
    public boolean isMale() {
        return jCheckBox_FilterMale.isSelected();
    }

    /** Mutators */
    public void setSceneEssentials(SceneEssentials sceneInfo) {
        m_sceneData = sceneInfo;
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel_Filter = new javax.swing.JLabel();
        jComboBox_Filter = new javax.swing.JComboBox();
        jCheckBox_FilterMale = new javax.swing.JCheckBox();
        jCheckBox_FilterFemale = new javax.swing.JCheckBox();
        jCheckBox_FilterAdd = new javax.swing.JCheckBox();
        jCheckBox_FilterView = new javax.swing.JCheckBox();
        jButton_Load = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Server Browser", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setMinimumSize(new java.awt.Dimension(320, 480));
        setPreferredSize(new java.awt.Dimension(320, 480));
        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(305, 330));

        DefaultTableModel t = new DefaultTableModel();
        jTable1.setModel(t);
        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jScrollPane1, gridBagConstraints);

        jLabel_Filter.setText("Filter:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel_Filter, gridBagConstraints);

        jComboBox_Filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBrowser();
            }
        });
        jComboBox_Filter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jComboBox_Filter, gridBagConstraints);

        jCheckBox_FilterMale.setSelected(true);
        jCheckBox_FilterMale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FilterMale.setSelected(true);
                jCheckBox_FilterFemale.setSelected(false);
                updateBrowser();
            }
        });
        jCheckBox_FilterMale.setText("Male");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jCheckBox_FilterMale, gridBagConstraints);

        jCheckBox_FilterFemale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FilterFemale.setSelected(true);
                jCheckBox_FilterMale.setSelected(false);
                updateBrowser();
            }
        });
        jCheckBox_FilterFemale.setText("Female");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jCheckBox_FilterFemale, gridBagConstraints);

        jCheckBox_FilterAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FilterAdd.setSelected(true);
                jCheckBox_FilterView.setSelected(false);
            }
        });
        jCheckBox_FilterAdd.setText("Add Mode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 120, 0, 0);
        add(jCheckBox_FilterAdd, gridBagConstraints);

        jCheckBox_FilterView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FilterView.setSelected(true);
                jCheckBox_FilterAdd.setSelected(false);
            }
        });
        jCheckBox_FilterView.setSelected(true);
        jCheckBox_FilterView.setText("View Mode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 120, 0, 0);
        add(jCheckBox_FilterView, gridBagConstraints);

        jButton_Load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeLoad();
            }
        });
        jButton_Load.setText("Load");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButton_Load, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Load;
    private javax.swing.JCheckBox jCheckBox_FilterAdd;
    private javax.swing.JCheckBox jCheckBox_FilterFemale;
    private javax.swing.JCheckBox jCheckBox_FilterMale;
    private javax.swing.JCheckBox jCheckBox_FilterView;
    private javax.swing.JComboBox jComboBox_Filter;
    private javax.swing.JLabel jLabel_Filter;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
