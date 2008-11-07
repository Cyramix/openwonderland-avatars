/*
 * ServerBrowser.java
 *
 * Created on November 6, 2008, 12:20 PM
 */

package imi.gui;

import imi.sql.SQLInterface;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  ptruong
 */
public class ServerBrowser extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /** SQL Interface */
    private SQLInterface            m_sql;
    private ArrayList<String[]>     m_data, m_anim;
    private Map<Integer, String[]>  m_meshes;
    /** Return Data */
    private String[]                m_modelInfo, m_animInfo;
    private int                     m_loadType;
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - END
////////////////////////////////////////////////////////////////////////////////    
////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form ServerBrowser */
    public ServerBrowser() {
        initComponents();
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
                break;
            }
            case 1:         // CLOTHES (SKINNED MESHES) / ACCESSORIES (NON-SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, type, id FROM Meshes";
                break;
            }
            case 2:         // TEXTURES (JPEG, TARGA, PNG, GIF)
            {
                query = "SELECT name, description, bodytype, url, type FROM Textures";
                break;
            }
        }
        
        m_data = loadSQLData(query);
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
                break;
            }
            case 1:         // CLOTHES (SKINNED MESHES) / ACCESSORIES (NON-SKINNED MESHES)
            {
                query = "SELECT name, description, bodytype, url, type, id FROM Meshes";
                break;
            }
            case 2:         // TEXTURES (JPEG, TARGA, PNG, GIF)
            {
                query = "SELECT name, description, bodytype, url, type FROM Textures";
                break;
            }
        }
        
        m_data = loadSQLData(query);
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
    
    public void getSelectedData() {
        int selection = jTable1.getSelectedRow();
        m_modelInfo = m_data.get(selection);
        if (m_loadType == 0) {
            String query = "SELECT url FROM Animations WHERE avatarid = ";
            query += m_modelInfo[4].toString();
            m_anim = loadSQLData(query);
            m_animInfo = new String[m_anim.size()];
            for(int i = 0; i < m_anim.size(); i++) {
                m_animInfo[i] = m_anim.get(i)[0].toString();
            }

            String gender = null;
                        
            if (m_modelInfo[2].equals("1"))
                gender = "Male";
            else
                gender = "Female";
            
            query = "SELECT name, grouping FROM GeometryReferences WHERE tableref = ";
            query += gender;
            ArrayList<String[]> meshes = loadSQLData(query);
            for (int i = 0; i < meshes.size(); i++) {
                int iType = 0;
                if (meshes.get(i)[1].equals("0"))
                    iType = 0;          // Head
                else if (meshes.get(i)[1].equals("1"))
                    iType = 1;          // Hands
                else if (meshes.get(i)[1].equals("2"))
                    iType = 2;          // Torso
                else if (meshes.get(i)[1].equals("3"))
                    iType = 3;          // Legs
                else if (meshes.get(i)[1].equals("4"))
                    iType = 4;          // Feet
                
                String[] geometry = new String[1];
                geometry[0] = meshes.get(i)[0].toString();
                m_meshes.put(iType, geometry);
            }
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

        m_sql.Disconnect();
        return data;
    }
    
    public void pseudoClose() {
        this.setVisible(false);
        
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
    
    public boolean isReplace() {
        return jCheckBox_FilterAdd.isSelected();
    }
    
    public boolean isMale() {
        return jCheckBox_FilterMale.isSelected();
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
        jLabel_TitleBar = new javax.swing.JLabel();
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

        jPanel_MainPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_MainPanel.setLayout(new java.awt.GridBagLayout());

        jLabel_TitleBar.setBackground(new java.awt.Color(204, 204, 204));
        jLabel_TitleBar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_TitleBar.setText("File Browser");
        jLabel_TitleBar.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 240;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel_MainPanel.add(jLabel_TitleBar, gridBagConstraints);

        DefaultTableModel t = new DefaultTableModel();
        jTable1.setModel(t);
        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
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
                pseudoClose();
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
        jCheckBox_FilterView.setText("View Mode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainPanel.add(jCheckBox_FilterView, gridBagConstraints);

        jCheckBox_FilterAdd.setSelected(true);
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
            .add(jPanel_MainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_MainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServerBrowser().setVisible(true);
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
    private javax.swing.JLabel jLabel_TitleBar;
    private javax.swing.JPanel jPanel_MainPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField_Selection;
    // End of variables declaration//GEN-END:variables

}
