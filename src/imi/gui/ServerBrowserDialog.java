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

import imi.sql.SQLInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  ptruong
 */
public class ServerBrowserDialog extends javax.swing.JDialog {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /** SQL Interface */
    private SQLInterface            m_sql;
    private ArrayList<String[]>     m_data, m_anim;
    private Map<Integer, String[]>  m_meshes;
    /** Return Data */
    private String[]                m_modelInfo, m_animInfo, m_meshref;
    private int                     m_loadType, m_region;
    private SceneEssentials         m_sceneData;
 
////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form ServerBrowserDialog */
    public ServerBrowserDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        ListSelectionModel rowSelection = jTable1.getSelectionModel();
        rowSelection.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
            ListSelectionModel rowSelect = (ListSelectionModel)e.getSource();
            int selectedIndex = rowSelect.getMinSelectionIndex();
            getSelectedData(selectedIndex);
            }
        });
    }

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
    }
   
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
        jTable1.setModel(tabModel);
        m_loadType = selection;
    }
    
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
                if (m_data.get(i)[4].equals("1") || m_data.get(i)[4].equals("2"))
                    genderFiltered.add(m_data.get(i));
            }
            m_data.clear();
            m_data = new ArrayList<String[]>(genderFiltered);
            genderFiltered.clear();
        } else  if (typeFilter.equals("2")) {
            for (int i = 0; i < m_data.size(); i++) {
                if (!m_data.get(i)[4].equals("1") && !m_data.get(i)[4].equals("2"))
                    genderFiltered.add(m_data.get(i));
            }
            m_data.clear();
            m_data = new ArrayList<String[]>(genderFiltered);
            genderFiltered.clear();
        }
    }
    
    public void getSelectedData(int selection) {
        //int selection = jTable1.getSelectedRow();
        m_modelInfo = m_data.get(selection);
        if (m_loadType == 0) {
            String query = "SELECT url FROM Animations WHERE avatarid = ";
            query += m_modelInfo[4].toString();
            m_anim = loadSQLData(query);
            
            m_animInfo = new String[m_anim.size()];
            for(int i = 0; i < m_anim.size(); i++) {
                m_animInfo[i] = m_anim.get(i)[0].toString();
            }

            if (m_animInfo.length > 0) {
                String gender = null;
                if (m_modelInfo[2].equals("1"))
                    gender = "\'Male\'";
                else
                    gender = "\'Female\'";

                query = "SELECT name, grouping FROM GeometryReferences WHERE tableref = ";
                query += gender;
                if (m_meshes != null)
                    m_meshes.clear();
                m_meshes = new HashMap<Integer, String[]>();
                ArrayList<String[]> meshes = loadSQLData(query);

                createMeshSwapList("0", meshes);
                createMeshSwapList("1", meshes);
                createMeshSwapList("2", meshes);
                createMeshSwapList("3", meshes);
                createMeshSwapList("4", meshes);
            } else {
                query = "SELECT name, grouping FROM GeometryReferences WHERE referenceid = ";
                query += m_modelInfo[4].toString();
                ArrayList<String[]> ref = loadSQLData(query);

                m_meshref = new String[ref.size()];
                for(int i = 0; i < ref.size(); i++)
                    m_meshref[i] = ref.get(i)[0];

                if (ref.get(0)[1].equals("0")) {
                    m_region = 0;          // Head
                } else if (ref.get(0)[1].equals("1")) {
                    m_region = 1;          // Hands
                } else if (ref.get(0)[1].equals("2")) {
                    m_region = 2;          // Torso
                } else if (ref.get(0)[1].equals("3")) {
                    m_region = 3;          // Legs
                } else if (ref.get(0)[1].equals("4")) {
                    m_region = 4;
                }
                
                m_animInfo = null;
            }
            
            
        } else {
            String query = "SELECT name, grouping FROM GeometryReferences WHERE referenceid = ";
            query += m_modelInfo[5].toString();
            ArrayList<String[]> ref = loadSQLData(query);
            
            m_meshref = new String[ref.size()];
            for(int i = 0; i < ref.size(); i++)
                m_meshref[i] = ref.get(i)[0];
            
            if (ref.get(0)[1].equals("0")) {
                m_region = 0;          // Head
            } else if (ref.get(0)[1].equals("1")) {
                m_region = 1;          // Hands
            } else if (ref.get(0)[1].equals("2")) {
                m_region = 2;          // Torso
            } else if (ref.get(0)[1].equals("3")) {
                m_region = 3;          // Legs
            } else if (ref.get(0)[1].equals("4")) {
                m_region = 4;
            }
        }
    }
    
    public void createMeshSwapList(String region, ArrayList<String[]> meshes) {
        String[] geometry = null;
        ArrayList<String> temp = new ArrayList<String>();
        
        for (int i = 0; i < meshes.size(); i++) {
            if (meshes.get(i)[1].equals(region)) {
                temp.add(meshes.get(i)[0].toString());
            }
        }
        geometry = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            geometry[i] = temp.get(i);
        }
        
        if (region.equals("0"))
            m_region = 0;          // Head
        else if (region.equals("1"))
            m_region = 1;          // Hands
        else if (region.equals("2"))
            m_region = 2;          // Torso
        else if (region.equals("3"))
            m_region = 3;          // Legs
        else if (region.equals("4"))
            m_region = 4;          // Feet
        
        m_meshes.put(m_region, geometry);
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

        m_sql.Disconnect();
        return data;
    }
    
    public void pseudoClose() {
        setVisible(false);
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
                m_sceneData.loadAvatarDAEURL(true, this, m_modelInfo[3], headLocation, handLocation, null, iGender);
                break;
            }
            case 1:         // LOAD CLOTHES
            {
                m_sceneData.loadSMeshDAEURL(true, this, m_modelInfo, m_meshref);
                break;
            }
            case 2:         // LOAD ACCESSORIES
            {
                m_sceneData.loadMeshDAEURL(true, this, m_modelInfo);
                break;
            }
            case 3:         // LOAD TEXTURES
            {
                break;
            }
        }
        dispose();
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
    
    public boolean isMale() {
        return jCheckBox_FilterMale.isSelected();
    }
    
    /** Mutators */
    public void setSceneEssentials(SceneEssentials sceneInfo) {
        m_sceneData = sceneInfo;
    }
////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - END
////////////////////////////////////////////////////////////////////////////////
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel_MainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jComboBox_Filter = new javax.swing.JComboBox();
        jLabel_Filter = new javax.swing.JLabel();
        jLabel_Selection = new javax.swing.JLabel();
        jTextField_Selection = new javax.swing.JTextField();
        jButton_Load = new javax.swing.JButton();
        jButton_Cancel = new javax.swing.JButton();
        jCheckBox_FilterView = new javax.swing.JCheckBox();
        jCheckBox_FilterAdd = new javax.swing.JCheckBox();
        jCheckBox_FilterMale = new javax.swing.JCheckBox();
        jCheckBox_FilterFemale = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel_MainPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_MainPanel.setLayout(new java.awt.GridBagLayout());

        DefaultTableModel t = new DefaultTableModel();
        jTable1.setModel(t);
        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 240;
        gridBagConstraints.ipady = 200;
        jPanel_MainPanel.add(jScrollPane1, gridBagConstraints);

        jComboBox_Filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBrowser();
            }
        });
        jComboBox_Filter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel_MainPanel.add(jComboBox_Filter, gridBagConstraints);

        jLabel_Filter.setText("Filter:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel_MainPanel.add(jLabel_Filter, gridBagConstraints);

        jLabel_Selection.setText("Selection:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel_MainPanel.add(jLabel_Selection, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 240;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel_MainPanel.add(jTextField_Selection, gridBagConstraints);

        jButton_Load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pseudoClose();
            }
        });
        jButton_Load.setText("Load");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 75;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel_MainPanel.add(jButton_Load, gridBagConstraints);

        jButton_Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        jButton_Cancel.setText("Cancel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 68;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel_MainPanel.add(jButton_Cancel, gridBagConstraints);

        jCheckBox_FilterView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FilterView.setSelected(true);
                jCheckBox_FilterAdd.setSelected(false);
            }
        });
        jCheckBox_FilterView.setSelected(true);
        jCheckBox_FilterView.setText("View Mode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainPanel.add(jCheckBox_FilterView, gridBagConstraints);

        jCheckBox_FilterAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FilterAdd.setSelected(true);
                jCheckBox_FilterView.setSelected(false);
            }
        });
        jCheckBox_FilterAdd.setText("Add Mode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainPanel.add(jCheckBox_FilterAdd, gridBagConstraints);

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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = -30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainPanel.add(jCheckBox_FilterMale, gridBagConstraints);

        jCheckBox_FilterFemale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FilterFemale.setSelected(true);
                jCheckBox_FilterMale.setSelected(false);
                updateBrowser();
            }
        });
        jCheckBox_FilterFemale.setText("Female");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = -30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainPanel.add(jCheckBox_FilterFemale, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_MainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_MainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ServerBrowserDialog dialog = new ServerBrowserDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_Load;
    private javax.swing.JCheckBox jCheckBox_FilterAdd;
    private javax.swing.JCheckBox jCheckBox_FilterFemale;
    private javax.swing.JCheckBox jCheckBox_FilterMale;
    private javax.swing.JCheckBox jCheckBox_FilterView;
    private javax.swing.JComboBox jComboBox_Filter;
    private javax.swing.JLabel jLabel_Filter;
    private javax.swing.JLabel jLabel_Selection;
    private javax.swing.JPanel jPanel_MainPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField_Selection;
    // End of variables declaration//GEN-END:variables

}
