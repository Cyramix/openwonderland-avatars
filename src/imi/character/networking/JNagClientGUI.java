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
package imi.character.networking;

import com.sun.sgs.client.ClientChannel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Lou Hayt
 */
public class JNagClientGUI implements ActionListener
{
    /** The frame for the GUI */
    protected JFrame frame;
    
    /** The output area for chat messages. */
    protected final JTextArea outputArea;

    /** The input field for the user to enter a chat message. */
    protected final JTextField inputField;

    /** The panel that wraps the input field and any other UI. */
    protected final JPanel inputPanel;

    /** The status indicator. */
    protected final JLabel statusLabel;
    
    /** The UI selector among direct messaging and different channels. */
    protected JComboBox channelSelector;

    /** The data model for the channel selector. */
    protected DefaultComboBoxModel channelSelectorModel;
    
    private boolean firstChannel = true;
    
    /** The using instance for this client. */
    protected final JNagClient client;
    
    public JNagClientGUI(JNagClient client)
    {
        super();
        this.client = client;
        
        frame = new JFrame(JNagClientGUI.class.getSimpleName());
        Container c = frame.getContentPane();
        JPanel appPanel = new JPanel();
        appPanel.setFocusable(false);
        c.setLayout(new BorderLayout());
        appPanel.setLayout(new BorderLayout());
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFocusable(false);
        appPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        inputField = new JTextField();
        inputField.addActionListener(this);
        inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        populateInputPanel(inputPanel);
        inputPanel.setEnabled(false);
        appPanel.add(inputPanel, BorderLayout.SOUTH);
        c.add(appPanel, BorderLayout.CENTER);
        statusLabel = new JLabel();
        statusLabel.setFocusable(false);
        setStatus("Not Started");
        c.add(statusLabel, BorderLayout.SOUTH);
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent event) 
    {
        if (! client.isConnected())
            return;

        String text = getInputText();
        String channelName = (String) channelSelector.getSelectedItem();
        if (channelName.equalsIgnoreCase("<DIRECT>")) 
            client.getServerProxy().serverCommand(text); // command that will be whispered back
        else // chat channels
        {
            ClientChannel channel = client.getChannel(channelName);
            try {
                channel.send(JNagClient.encodeString(client.getID(), text)); // added user ID
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
    
    public void leftChannel(String channelName) 
    {
  //      String current = (String)channelSelectorModel.getSelectedItem();
        appendOutput("Leaving channel " + channelName);
        channelSelectorModel.removeElement(channelName);
//        int index = channelSelectorModel.getIndexOf(current);
//        channelSelector.setSelectedIndex(index);
    }
    
    public void joinedChannel(ClientChannel channel) 
    {
        String channelName = channel.getName();
        appendOutput("Joined to channel " + channelName);
        channelSelectorModel.addElement(channelName);
     
        if (firstChannel)
        {
            firstChannel = false;
            channelSelector.setSelectedIndex(1);
        }
    }
    
    /**
     * Allows subclasses to populate the input panel with
     * additional UI elements. 
     *
     * @param panel the panel to populate
     */
    protected void populateInputPanel(JPanel panel) {
        panel.add(inputField, BorderLayout.CENTER);

        channelSelectorModel = new DefaultComboBoxModel();
        channelSelectorModel.addElement("<DIRECT>");
        channelSelector = new JComboBox(channelSelectorModel);
        channelSelector.setFocusable(false);
        panel.add(channelSelector, BorderLayout.WEST);
    }
    
    /**
     * Appends the given message to the output text pane.
     *
     * @param x the message to append to the output text pane
     */
    protected void appendOutput(String x) {
        outputArea.append(x + "\n");
    }
    
    /**
     * Displays the given string in this client's status bar.
     *
     * @param status the status message to set
     */
    protected void setStatus(String status) {
        appendOutput("Status Set: " + status);
        statusLabel.setText("Status: " + status);
    }
    
    /**
     * Returns the user-supplied text from the input field, and clears
     * the field to prepare for more input.
     *
     * @return the user-supplied text from the input field
     */
    protected String getInputText() {
        try {
            return inputField.getText();
        } finally {
            inputField.setText("");
        }
    }
    
    public void setEnableInput(boolean enable)
    {
        inputPanel.setEnabled(enable);
    }
}
