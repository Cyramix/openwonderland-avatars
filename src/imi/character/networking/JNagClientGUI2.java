/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JNagClientGUI2.java
 *
 * Created on Jan 19, 2009, 12:25:50 PM
 */

package imi.character.networking;

import com.sun.sgs.client.ClientChannel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author ptruong
 */
public class JNagClientGUI2 extends javax.swing.JFrame implements ActionListener {
    /** The data model for the channel selector. */
    protected DefaultComboBoxModel channelSelectorModel;

    private boolean firstChannel = true;

    /** The using instance for this client. */
    protected final JNagClient client;

    protected ArrayList<Integer> userIds = new ArrayList<Integer>();

    /** Creates new form JNagClientGUI2 */
    public JNagClientGUI2(JNagClient client) {
        super();
        this.client = client;
        
        initComponents();   // Initialize the GUI components
        TableColumn col1 = jTable_Boards.getColumnModel().getColumn(1);
        col1.setCellRenderer(new customImageCellRender());

        setStatus("Not Started");
        this.setVisible(true);
    }

    public void joinedChannel(ClientChannel channel)
    {
        String channelName = channel.getName();
        appendOutput("Joined to channel " + channelName);
        channelSelectorModel.addElement(channelName);

        if (firstChannel)
        {
            firstChannel = false;
            jComboBox_Selector.setSelectedIndex(1);
        }
    }

    /**
     * Allows subclasses to populate the input panel with
     * additional UI elements.
     *
     * @param panel the panel to populate
     */
    protected void populateInputPanel(javax.swing.JPanel panel) {
        channelSelectorModel = new DefaultComboBoxModel();
        channelSelectorModel.addElement("<DIRECT>");
        jComboBox_Selector = new javax.swing.JComboBox(channelSelectorModel);
        jComboBox_Selector.setFocusable(false);
    }

    /**
     * Appends the given message to the output text pane.
     *
     * @param x the message to append to the output text pane
     */
    protected void appendOutput(String x) {
        jTextArea_OutputWindow.append(x + "\n");
    }

    /**
     * Displays the given string in this client's status bar.
     *
     * @param status the status message to set
     */
    protected void setStatus(String status) {
        appendOutput("Status Set: " + status);
        jLabel_ConnectivityStatus.setText("Status: " + status);
    }

    /**
     * Returns the user-supplied text from the input field, and clears
     * the field to prepare for more input.
     *
     * @return the user-supplied text from the input field
     */
    protected String getInputText() {
        try {
            return jTextField_InputWindow.getText();
        } finally {
            jTextField_InputWindow.setText("");
        }
    }

    public void setEnableInput(boolean enable)
    {
        jPanel_InputPane.setEnabled(enable);
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

        jSplitPane_MainWindow = new javax.swing.JSplitPane();
        jPanel_Main = new javax.swing.JPanel();
        jLabel_ConnectivityStatus = new javax.swing.JLabel();
        jPanel_InputPane = new javax.swing.JPanel();
        channelSelectorModel = new DefaultComboBoxModel();
        channelSelectorModel.addElement("<DIRECT>");
        jComboBox_Selector = new javax.swing.JComboBox();
        jTextField_InputWindow = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea_OutputWindow = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_Boards = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jSplitPane_MainWindow.setDividerLocation(350);
        jSplitPane_MainWindow.setDividerSize(10);
        jSplitPane_MainWindow.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane_MainWindow.setMinimumSize(new java.awt.Dimension(640, 27));
        jSplitPane_MainWindow.setOneTouchExpandable(true);
        jSplitPane_MainWindow.setPreferredSize(new java.awt.Dimension(640, 500));

        jPanel_Main.setMinimumSize(new java.awt.Dimension(380, 0));
        jPanel_Main.setPreferredSize(new java.awt.Dimension(640, 350));
        jPanel_Main.setLayout(new java.awt.GridBagLayout());
        jPanel_Main.setFocusable(false);

        jLabel_ConnectivityStatus.setBackground(new java.awt.Color(102, 204, 255));
        jLabel_ConnectivityStatus.setFont(new java.awt.Font("Lucida Grande", 3, 13));
        jLabel_ConnectivityStatus.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_ConnectivityStatus.setText("jLabel1");
        jLabel_ConnectivityStatus.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_ConnectivityStatus.setMinimumSize(new java.awt.Dimension(54, 25));
        jLabel_ConnectivityStatus.setPreferredSize(new java.awt.Dimension(480, 25));
        jLabel_ConnectivityStatus.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel_Main.add(jLabel_ConnectivityStatus, gridBagConstraints);

        jPanel_InputPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel_InputPane.setPreferredSize(new java.awt.Dimension(480, 32));
        jPanel_InputPane.setLayout(new java.awt.GridBagLayout());

        jComboBox_Selector.setModel((channelSelectorModel));
        jComboBox_Selector.setMinimumSize(new java.awt.Dimension(140, 27));
        jComboBox_Selector.setPreferredSize(new java.awt.Dimension(140, 27));
        jPanel_InputPane.add(jComboBox_Selector, new java.awt.GridBagConstraints());

        jTextField_InputWindow.setBackground(new java.awt.Color(153, 153, 255));
        jTextField_InputWindow.setPreferredSize(new java.awt.Dimension(300, 28));
        jTextField_InputWindow.setFocusable(true);
        jTextField_InputWindow.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel_InputPane.add(jTextField_InputWindow, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel_Main.add(jPanel_InputPane, gridBagConstraints);

        jScrollPane2.setAutoscrolls(true);

        jTextArea_OutputWindow.setBackground(new java.awt.Color(153, 153, 255));
        jTextArea_OutputWindow.setColumns(20);
        jTextArea_OutputWindow.setRows(5);
        jScrollPane2.setViewportView(jTextArea_OutputWindow);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Main.add(jScrollPane2, gridBagConstraints);

        jSplitPane_MainWindow.setLeftComponent(jPanel_Main);

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(640, 200));

        jTable_Boards.setBackground(new java.awt.Color(153, 153, 255));
        jTable_Boards.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Player Name", "Lives Remaining", "Wins", "Loses"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_Boards.setRowHeight(32);
        TableColumn lives = jTable_Boards.getColumnModel().getColumn(1);
        lives.setCellRenderer(new JNagClientGUI2.customImageCellRender());
        lives.setPreferredWidth(180);
        jScrollPane1.setViewportView(jTable_Boards);

        jSplitPane_MainWindow.setRightComponent(jScrollPane1);

        getContentPane().add(jSplitPane_MainWindow, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox_Selector;
    private javax.swing.JLabel jLabel_ConnectivityStatus;
    private javax.swing.JPanel jPanel_InputPane;
    private javax.swing.JPanel jPanel_Main;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane_MainWindow;
    private javax.swing.JTable jTable_Boards;
    private javax.swing.JTextArea jTextArea_OutputWindow;
    private javax.swing.JTextField jTextField_InputWindow;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        if (! client.isConnected())
            return;

        String text = getInputText();
        String channelName = (String) jComboBox_Selector.getSelectedItem();
        if (channelName.equalsIgnoreCase("<DIRECT>"))
            client.getServerProxy().serverCommand(text); // command that will be whispered back
        else // chat channels
        {
            ClientChannel channel = client.getChannel(channelName);
            try {
                channel.send(JNagClient.encodeString(client.getID(), text)); // added user ID
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
    }

    /**
     * Places a newly entered player into the board of players in the game / chat
     * room.  By default it sets the players lives, wins and losses to default.
     * If a playerName must be added.  null may be entered in for lives wins or
     * losses and a default value will be entered
     * @param playerName - name of player to be added
     * @param lives - default number of lives
     * @param wins - default wins so far
     * @param losses - default losses so far
     */
    public void addPlayerToBoards(String playerName, int playerID, int lives, int wins, int losses) {
        Object[] data = new Object[4];
        data[0] = playerName;
        data[1] = lives;
        data[2] = wins;
        data[3] = losses;

        DefaultTableModel table = (DefaultTableModel)jTable_Boards.getModel();
        table.addRow(data);
        table.fireTableDataChanged();
        userIds.add(playerID);
    }

    /**
     * Set the player lives, wins and losses to display on the boards.  Takes in
     * exact value to display.  Ideally should not be used for updating.
     * @param playerName
     * @param lives
     * @param wins
     * @param losses
     */
    public void setPlayerStatsInBoards(String playerName, int lives, int wins, int losses) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                int Lives   = (Integer)model.getValueAt(i, 1);
                int Wins    = (Integer)model.getValueAt(i, 2);
                int Losses  = (Integer)model.getValueAt(i, 3);

                if (lives > -1)
                    Lives   = lives;

                if (wins > -1)
                    Wins    = wins;
                else if (Wins < 0)
                    Wins = 0;

                if (losses > -1)
                    Losses  = losses;
                else if (Losses < 0)
                    Losses = 0;

                model.setValueAt(Lives, i, 1);
                model.setValueAt(Wins, i, 2);
                model.setValueAt(Losses, i, 3);
            }
        }
        model.fireTableDataChanged();
    }
    
    /**
     * Sets the number of lives a player has gotten.  Takes in the exact value
     * of lives to set for that player.
     * @param playerName
     * @param lives
     */
    public void setPlayerLives(String playerName, int lives) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                model.setValueAt(lives, i, 1);
            }
        }
        model.fireTableDataChanged();
    }
    
    /**
     * Set the number of wins a specified player has gotten.  Takes in the exact
     * value of wins to set for that player
     * @param playerName
     * @param wins
     */
    public void setPlayerWins(String playerName, int wins) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                model.setValueAt(wins, i, 2);
            }
        }
        model.fireTableDataChanged();
    }
    
    /**
     * Sets the number of losses a player has gotten.  Takes in the exact value
     * of losses to set for that player
     * @param playerName
     * @param losses
     */
    public void setPlayerLosses(String playerName, int losses) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                model.setValueAt(losses, i, 3);
            }
        }
        model.fireTableDataChanged();
    }

    /**
     * Updates the score board by searching for the player name and then updating
     * the players lives, wins, and loses counter.  Pass in a negative one for
     * decreasing a value, a positive one to increment and zero to do nothing
     * @param playerName - name of the player to find
     * @param livesdeduction - the number of lives to deduct
     * @param winsincrement - the number of wins to increment
     * @param lossesincrement - the number of losses to increment
     */
    public void updatePlayerStatsInBoards(String playerName, int livesdeduction, int winsincrement, int lossesincrement) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                int lives   = (Integer)model.getValueAt(i, 1);
                int wins    = (Integer)model.getValueAt(i, 2);
                int losses  = (Integer)model.getValueAt(i, 3);

                lives   += livesdeduction;
                wins    += winsincrement;
                losses  += lossesincrement;

                model.setValueAt(lives, i, 1);
                model.setValueAt(wins, i, 2);
                model.setValueAt(losses, i, 3);
            }
        }
        model.fireTableDataChanged();
    }

    /**
     * Updates the specified player's number of remaining lives.  Enter in a
     * positive value to subtract from the remaining lives
     * @param playerName
     * @param livesdecrement
     */
    public void updatePlayerLives(String playerName, int livesdecrement) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                int lives   = (Integer)model.getValueAt(i, 1);
                lives      -= livesdecrement;
                model.setValueAt(lives, i, 1);
            }
        }
        model.fireTableDataChanged();
    }

    /**
     * Updates the specified player's number of wins.  Enter in a positive value
     * to add to the number of current wins.
     * @param playerName
     * @param winsincrement
     */
    public void updatePlayerWins(String playerName, int winsincrement) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                int wins    = (Integer)model.getValueAt(i, 2);
                wins       += winsincrement;
                model.setValueAt(wins, i, 2);
            }
        }
        model.fireTableDataChanged();
    }

    /**
     * Updates the specified player's number of losses.  Enter in a positive value
     * to add to the number of current losses
     * @param playerName
     * @param lossesincrement
     */
    public void updatePlayerLosses(String playerName, int lossesincrement) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                int losses  = (Integer)model.getValueAt(i, 3);
                losses     += lossesincrement;
                model.setValueAt(losses, i, 3);
            }
        }
        model.fireTableDataChanged();
    }

    /**
     * Removes the player specified player from the score boards.  Should only be
     * called when a user disconnects/logs off
     * @param playerName
     */
    public void removePlayerFromBoards(String playerName) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(playerName)) {
                model.removeRow(i);
            }
        }
        model.fireTableDataChanged();
    }


    /**
     * Forces the Player boards (table of users) to update due to a change in
     * one or more of the cells
     */
    public void forceTableUpdate(int type) {
        DefaultTableModel model = (DefaultTableModel)jTable_Boards.getModel();
        switch(type) {
            case 1:
            {
                model.fireTableDataChanged();
                break;
            }
            case 2:
            {
                Vector names = new Vector();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    names.add(model.getColumnName(i));
                }
                DefaultTableModel NewTable = new DefaultTableModel(model.getDataVector(), names);
                break;
            }
        }
        
        
    }
    
    /**
     * Custom cell renderer for the JTable to display the lives of the players
     */
    public class customImageCellRender extends JLabel implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                String loc = null;
                switch((Integer)value)
                {
                    case 0:
                    {
                        loc = new String("file://localhost/" + System.getProperty("user.dir") + "/assets/textures/counter_0.png");
                        break;
                    }
                    case 1:
                    {
                        loc = new String("file://localhost/" + System.getProperty("user.dir") + "/assets/textures/counter_1.png");
                        break;
                    }
                    case 2:
                    {
                        loc = new String("file://localhost/" + System.getProperty("user.dir") + "/assets/textures/counter_2.png");
                        break;
                    }
                    case 3:
                    {
                        loc = new String("file://localhost/" + System.getProperty("user.dir") + "/assets/textures/counter_3.png");
                        break;
                    }
                    case 4:
                    {
                        loc = new String("file://localhost/" + System.getProperty("user.dir") + "/assets/textures/counter_4.png");
                        break;
                    }
                    case 5:
                    {
                        loc = new String("file://localhost/" + System.getProperty("user.dir") + "/assets/textures/counter_5.png");
                        break;
                    }
                }
                try {
                    URL Loc = new URL(loc);
                    Icon icon = new ImageIcon(Loc);
                    setIcon(icon);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(JNagClientGUI2.class.getName()).log(Level.SEVERE, null, ex);
                    }

                if (isSelected) {
                    table.setSelectionBackground(new Color(0, 255, 0));
                }
            }
            return this;
        }
    }
}
