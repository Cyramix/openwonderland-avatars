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

import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.objects.ObjectCollection;
import imi.character.objects.SpatialObject;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.PMathUtils;
import imi.utils.input.AvatarControlScheme;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class CoreCommands implements ConsoleCommand
{
    private ObjectCollection objs;
    private WorldManager wm;
    private ObjectCollectionGUI gui;
            
    CoreCommands(ObjectCollectionGUI gui, ObjectCollection objs, WorldManager wm) 
    {
        this.gui = gui;
        this.objs = objs;
        this.wm   = wm;
    }
    
    public boolean processCommand(String text) 
    {
        boolean processed = false;
        if (text.startsWith("list"))
            processed = listCommands(text);
        else if (text.startsWith("create"))
            processed = createCommands(text);
        else if (text.startsWith("remove"))
            processed = removeCommands(text);
        else if (text.startsWith("do"))
            processed = doCommands(text);
        else if (text.startsWith("place"))
            processed = placeCommands(text); 
        else if (text.startsWith("select"))
            processed = selectCommands(text); 
        
        return processed;
    }
    
    public boolean listCommands(String text) 
    {
        String command = text.substring(4);
        int index = 0;
        if (command.startsWith("Avatars"))
        {
            gui.appendOutput("Avatar List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                if (obj instanceof imi.character.Character)
                    gui.appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }   
            gui.appendOutput("-=-");
        }
        else if (command.startsWith("Chairs"))
        {
            gui.appendOutput("Chair List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                if (obj instanceof imi.character.objects.Chair)
                    gui.appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }      
            gui.appendOutput("-=-");
        }
        else if (command.startsWith("Locations"))
        {
            gui.appendOutput("Location List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                if (obj instanceof imi.character.objects.LocationNode)
                    gui.appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }      
            gui.appendOutput("-=-");
        }
        else
        {
            gui.appendOutput("Object List:");
            for (SpatialObject obj : objs.getObjects()) 
            {
                gui.appendOutput(index + ": " + obj.getClass().toString());
                index++; 
            }
            gui.appendOutput("-=-");
        }
        return true;
    }
    
    public boolean createCommands(String text) 
    {
        String command = text.substring(6);
        if (command.startsWith("Avatar"))
        {
            // createAvatar (name:) string (gender:) "female" (position:) float float float
            String genderString;
            String avatar = command.substring(7);
            String [] args = gui.parseArguments(avatar);
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
                gui.appendOutput("creating " + genderString + " avatar named: " + name + " at: " + pos.toString()); 
                
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
                    gui.appendOutput("selected " + name);
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
                gui.appendOutput("Syntax error, (required) <optional>:");
                gui.appendOutput("createAvatar (name) (gender) (position: float float float) <select> <dir: float float float> ");
            }
            return true;
        }
        // createChair
        // createLocation
        return false;
    }
    
    public boolean removeCommands(String text) 
    {
        String command = text.substring(6);
        // removeAnything (ID)
        String [] args = gui.parseArguments(command);
        boolean done = false;
        try 
        {
            int index = Integer.parseInt(args[0]);
            SpatialObject obj = objs.getObjects().get(index);
            if (obj != null)
            {
                done = true;
                gui.appendOutput("removing Object #" + index);
                obj.destroy();
            }
            else
                gui.appendOutput("can not find Object #" + index);
        } catch (Exception ex)  { }
        if (!done)
        {
            try 
            {
                int index = Integer.parseInt(args[1]);
                SpatialObject obj = objs.getObjects().get(index);
                if (obj != null)
                {
                    gui.appendOutput("removing Object #" + index);
                    obj.destroy();
                }
                else
                    gui.appendOutput("can not find Object #" + index);
            } catch (Exception ex) 
            {
                gui.appendOutput("Syntax error, (required) <optional>:");
                gui.appendOutput("removeAvatar (ID)");
            }
        }
        return true;
    }

    public boolean selectCommands(String text) 
    {
        String command = text.substring(6);
        if (command.startsWith("Avatar"))
        {
            // selectAvatar (ID)
            String avatar = command.substring(7);
            String [] args = gui.parseArguments(avatar);
            
            try  {
                int index = Integer.parseInt(args[0]);
                SpatialObject obj = objs.getObjects().get(index);
                if (obj != null && obj instanceof Avatar)
                {
                    Avatar sel = (Avatar)obj;
                    gui.appendOutput("selecting " + sel.getName());
                    sel.selectForInput();
                }
                else
                    gui.appendOutput("can not find Avatar #" + index);
            } catch (Exception ex) {
                gui.appendOutput("Syntax error, (required) <optional>:");
                gui.appendOutput("selectAvatar (ID)");
            }
            return true;
        }
        return false;
    }
    
    public boolean doCommands(String text) {
        return false;
    }

    public boolean placeCommands(String text) {
        return false;
    }

    public String getPrefix() {
        return ""; // these are the core commands...
    }
}
