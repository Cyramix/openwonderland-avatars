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
package imi.character.objects.console;

import imi.character.objects.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class ObjectCollectionGUI implements ActionListener
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
    
    /** The owning collection **/
    protected ObjectCollection objs = null;
    
    /** The world manager **/
    protected WorldManager    wm   = null;
    
    /** Console commands **/
    protected ConsoleCommand coreCommands = null;
    protected Hashtable<String, ConsoleCommand> additionalCommands = new Hashtable<String, ConsoleCommand>();
    
    public ObjectCollectionGUI(ObjectCollection master, WorldManager worldManager)
    {
        super();
        this.objs = master;
        this.wm = worldManager;
        coreCommands = new CoreCommands(this, objs, wm);
        
        frame = new JFrame("IMI Console");
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
        inputPanel.setEnabled(true);
        appPanel.add(inputPanel, BorderLayout.SOUTH);
        c.add(appPanel, BorderLayout.CENTER);
        statusLabel = new JLabel();
        statusLabel.setFocusable(false);
        setStatus("Console ready!");
        c.add(statusLabel, BorderLayout.SOUTH);
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public void consoleCommand(String text)
    {
        boolean processed = coreCommands.processCommand(text);
        
        for (ConsoleCommand command : additionalCommands.values())
        {
            if (text.startsWith(command.getPrefix()) && command.processCommand(text))
                processed = true;
        }
        
        if (!processed)
            appendOutput("unknown command");
    }
    
    private void chatChannel(String channelName, String text)
    {
        
    }
    
    public void addConsoleCommands(ConsoleCommand commandsHandler)
    {
        additionalCommands.put(commandsHandler.getPrefix(), commandsHandler);
    }
    
    public void removeConsoleCOmmands(String commandPrefix)
    {
        additionalCommands.remove(commandPrefix);
    }
    
    public String [] parseArguments(String text)
    {
        ArrayList<String> args = new ArrayList<String>();
        int marker = text.indexOf(" ");
        while (marker != -1)
        {
            args.add(text.substring(0, marker));
            text = text.substring(marker+1);
            marker = text.indexOf(" ");
        }
        args.add(text);
        String [] result = new String [args.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = args.get(i);
        return result;
    }
    
    public void actionPerformed(ActionEvent event) 
    {
        String text = getInputText();
        String channelName = (String) channelSelector.getSelectedItem();
        if (channelName.equalsIgnoreCase("<DIRECT>")) 
            consoleCommand(text);
        else 
            chatChannel(channelName, text);
    }
    
//    public void joinedChannel(ClientChannel channel) 
//    {
//        String channelName = channel.getName();
//        appendOutput("Joined channel: " + channelName);
//        channelSelectorModel.addElement(channelName);
//        if (firstChannel)
//        {
//            firstChannel = false;
//            channelSelector.setSelectedIndex(1);
//        }
//    }
    
    /**
     * Allows subclasses to populate the input panel with
     * additional UI elements. 
     *
     * @param panel the panel to populate
     */
    protected void populateInputPanel(JPanel panel) 
    {
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
        appendOutput("Status: " + status);
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
