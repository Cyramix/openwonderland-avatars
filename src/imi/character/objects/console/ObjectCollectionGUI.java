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
import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.PMathUtils;
import imi.utils.input.AvatarControlScheme;
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
    
    /** Additional console commands **/
    protected Hashtable<String, ConsoleCommand> additionalCommands = new Hashtable<String, ConsoleCommand>();
    
    public ObjectCollectionGUI(ObjectCollection master, WorldManager worldManager)
    {
        super();
        this.objs = master;
        this.wm = worldManager;
        
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
        boolean processed = true;
        if (text.startsWith("list"))
            listCommands(text);
        else if (text.startsWith("create"))
            createCommands(text);
        else if (text.startsWith("remove"))
            removeCommands(text);
        else if (text.startsWith("do"))
            doCommands(text);
        else if (text.startsWith("place"))
            placeCommands(text); 
        else if (text.startsWith("select"))
            selectCommands(text); 
        else 
            processed = false;
        
        for (ConsoleCommand command : additionalCommands.values())
        {
            if (text.startsWith(command.getPrefix()) && command.processCommand(text))
                processed = true;
        }
        
        if (!processed)
            appendOutput("unknown command");
    }
    
    public void listCommands(String text) 
    {
        String command = text.substring(4);
        int index = 0;
        if (command.startsWith("Avatars"))
        {
            appendOutput("Avatar List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                if (obj instanceof imi.character.Character)
                    appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }   
            appendOutput("-=-");
        }
        else if (command.startsWith("Chairs"))
        {
            appendOutput("Chair List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                if (obj instanceof imi.character.objects.Chair)
                    appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }      
            appendOutput("-=-");
        }
        else if (command.startsWith("Locations"))
        {
            appendOutput("Location List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                if (obj instanceof imi.character.objects.LocationNode)
                    appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }      
            appendOutput("-=-");
        }
        else
        {
            appendOutput("Object List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }
            appendOutput("-=-");
        }
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
    
    public void createCommands(String text) 
    {
        String command = text.substring(6);
        if (command.startsWith("Avatar"))
        {
            // createAvatar (name:) string (gender:) "female" (position:) float float float
            String genderString;
            String avatar = command.substring(7);
            String [] args = parseArguments(avatar);
            Vector3f pos = new Vector3f();
            String name;
            try {
                name = args[0];
                genderString = args[1];
                if (genderString.equals("f"))
                    genderString = "female";
                else if (!genderString.equals("female"))
                    genderString = "male";
                pos.set(Float.parseFloat(args[2]), Float.parseFloat(args[3]), Float.parseFloat(args[4]));
                appendOutput("creating " + genderString + " avatar named: " + name + " at: " + pos.toString()); 
                
                // optional additional arguments (selectForInput:) boolean (direction:) float float float 
                boolean selectForInput = false;
                Vector3f dir = Vector3f.UNIT_Z; 
                try { 
                    if (args[5].equals("select"))
                        selectForInput = true;
                } catch (Exception ex) { }
                try {
                    if (selectForInput)
                        dir = new Vector3f(Float.parseFloat(args[6]), Float.parseFloat(args[7]), Float.parseFloat(args[8]));
                    else
                        dir = new Vector3f(Float.parseFloat(args[5]), Float.parseFloat(args[6]), Float.parseFloat(args[7]));
                } catch (Exception ex) { }

                Avatar newAvatar;
                if (genderString.equals("female"))
                    newAvatar = new Avatar(new FemaleAvatarAttributes(name, true), wm);    
                else
                    newAvatar = new Avatar(new MaleAvatarAttributes(name, true), wm);    
                
                if (selectForInput)
                {
                    newAvatar.selectForInput();
                    appendOutput("selected " + name);
                }
                
                PMatrix look = PMathUtils.lookAt(pos.add(dir), pos, Vector3f.UNIT_Y);
                newAvatar.getModelInst().getTransform().getLocalMatrix(true).set(look);

                // The event processor provides the linkage between AWT events and input controls
                JSceneEventProcessor eventProcessor = (JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class);
                AvatarControlScheme control = (AvatarControlScheme)eventProcessor.getInputScheme();
                control.getAvatarTeam().add(newAvatar);
                newAvatar.setObjectCollection(objs);
                
            } catch (Exception ex) 
            { 
                appendOutput("Syntax error, (required) <optional>:");
                appendOutput("createAvatar (name) (gender) (position: float float float) <select> <dir: float float float> ");
            }            
        }
        else
            appendOutput("unknown command");
        
        // createChair
        // createLocation
    }
    
    public void removeCommands(String text) 
    {
        String command = text.substring(6);
        // removeAnything (ID)
        String [] args = parseArguments(command);
        boolean done = false;
        try 
        {
            int index = Integer.parseInt(args[0]);
            SpatialObject obj = objs.getObjects().get(index);
            if (obj != null)
            {
                done = true;
                appendOutput("removing Object #" + index);
                obj.destroy();
            }
            else
                appendOutput("can not find Object #" + index);
        } catch (Exception ex)  { }
        if (!done)
        {
            try 
            {
                int index = Integer.parseInt(args[1]);
                SpatialObject obj = objs.getObjects().get(index);
                if (obj != null)
                {
                    appendOutput("removing Object #" + index);
                    obj.destroy();
                }
                else
                    appendOutput("can not find Object #" + index);
            } catch (Exception ex) 
            {
                appendOutput("Syntax error, (required) <optional>:");
                appendOutput("removeAvatar (ID)");
            }
        }
    }

    public void selectCommands(String text) 
    {
        String command = text.substring(6);
        if (command.startsWith("Avatar"))
        {
            // selectAvatar (ID)
            String avatar = command.substring(7);
            String [] args = parseArguments(avatar);
            
            try  {
                int index = Integer.parseInt(args[0]);
                SpatialObject obj = objs.getObjects().get(index);
                if (obj != null && obj instanceof Avatar)
                {
                    Avatar sel = (Avatar)obj;
                    appendOutput("selecting " + sel.getName());
                    sel.selectForInput();
                }
                else
                    appendOutput("can not find Avatar #" + index);
            } catch (Exception ex) {
                appendOutput("Syntax error, (required) <optional>:");
                appendOutput("selectAvatar (ID)");
            }
        }
        else
            appendOutput("unknown command");
    }
    
    public void doCommands(String text) {
    //    throw new UnsupportedOperationException("Not yet implemented");
    }

    public void placeCommands(String text) {
      //  throw new UnsupportedOperationException("Not yet implemented");
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
