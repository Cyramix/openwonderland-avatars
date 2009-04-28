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
        else if (text.startsWith("place"))
            processed = placeCommands(text); 
        else if (text.startsWith("select"))
            processed = selectCommands(text); 
        else
            processed = defaultCommands(text);
        
        return processed;
    }
    
    public boolean defaultCommands(String text) 
    {
        // stop
        if (text.startsWith("stop"))
        {
            if (text.startsWith("stopAll") || text.startsWith("stopall"))
            {
                gui.appendOutput("stop all avatars");
                for (SpatialObject obj : objs.getObjects())
                {
                    if (obj instanceof Avatar)
                        ((Avatar)obj).stop();
                }
            }
            else
            {
                Avatar avatar = getSelectedAvatar();
                gui.appendOutput(avatar.getName() + " stop");
                avatar.stop();
            }
            return true;
        }
        // go
        else if (text.startsWith("go"))
        {
            String command = text.substring(2);
            if (command.startsWith("to"))
            {
                // goto (avatarID:) int (position:) float float float <heading:> float float float
                String string = command.substring(2);
                String [] args = gui.parseArguments(string);
                if (args[0].equals(""))
                    args = gui.parseArguments(string.substring(1));
                int id = -1;
                Vector3f pos = new Vector3f();
                Vector3f dir = null;
                boolean fail = false;
                try {
                    id = Integer.parseInt(args[0]);
                    pos.set(Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]));
                } catch (Exception ex) {
                    fail = true;
                }
                try {
                    dir = new Vector3f(Float.parseFloat(args[4]), Float.parseFloat(args[5]), Float.parseFloat(args[6]));
                } catch (Exception ex) { }
                if (!fail)
                {
                    Avatar avatar = objs.getAvatar(id);
                    if (avatar != null)
                    {
                        avatar.goTo(pos, dir);
                        gui.appendOutput("avatar " + id + " goto " + pos + " heading: " + dir);
                    }
                    else
                        gui.appendOutput("Syntax error, (required), <optional>\ngoto (avatarID) (position) <heading>");
                }
                else
                    gui.appendOutput("Syntax error, (required), <optional>\ngoto (avatarID) (position) <heading>");
                return true;
            }
            else
            {
                // go (position:) float float float <heading:> float float float
                String [] args = gui.parseArguments(command);
                if (args[0].equals(""))
                    args = gui.parseArguments(command.substring(1));
                Vector3f pos = new Vector3f();
                Vector3f dir = null;
                boolean fail = false;
                try {
                    pos.set(Float.parseFloat(args[0]), Float.parseFloat(args[1]), Float.parseFloat(args[2]));
                } catch (Exception ex) {
                    fail = true;
                }
                try {
                    dir = new Vector3f(Float.parseFloat(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]));
                } catch (Exception ex) { }
                if (!fail)
                {
                    Avatar avatar = getSelectedAvatar();
                    avatar.goTo(pos, dir);
                    gui.appendOutput("avatar " + avatar.getName() + " goto " + pos + " heading: " + dir);
                }
                else
                    gui.appendOutput("Syntax error, (required), <optional>\ngo (position) <heading>");
                return true;
            }
        }
        // follow
        else if (text.startsWith("follow"))
        {
            String command = text.substring(6);
            // follow (avatarID:) int (pathName:) string
            String [] args = gui.parseArguments(command);
            if (args[0].equals(""))
                args = gui.parseArguments(command.substring(1));
            String pathName = null;
            Avatar avatar = null;
            try {
                int avatarID = Integer.parseInt(args[0]);
                pathName = args[1];
                avatar = objs.getAvatar(avatarID);
            } 
            catch (Exception ex) 
            { 
                pathName = args[0];
                avatar   = getSelectedAvatar();
            }
            if (avatar != null)
            {
                avatar.followBakedPath(pathName);
                gui.appendOutput(avatar.getName() + " follow path " + pathName);
            }
            else
                gui.appendOutput("Syntax error, (required), <optional>\nfollow (avatarID) int (pathName) string");
            return true;
        }
        // find
        else if (text.startsWith("find"))
        {
            // find <avatarID:> int (locationName:) string
            String command = text.substring(4);
            String [] args = gui.parseArguments(command);
            if (args[0].equals(""))
                args = gui.parseArguments(command.substring(1));
            Avatar avatar       = null;
            String locationName = null;
            try {
                int id = Integer.parseInt(args[0]);
                avatar = objs.getAvatar(id);
                locationName = args[1];
            } 
            catch (Exception ex) {
                avatar = getSelectedAvatar();
                locationName = args[0];
            }
            if (avatar != null)
            {
                gui.appendOutput(avatar.getName() + " will find the path to: " + locationName);
                avatar.findPath(locationName);
            }
            else
                gui.appendOutput("syntax error, (required), <optional>\nfind <avatarID> (pathName)");
            return true;
        }
        // look
        else if (text.startsWith("look"))
        {
            // look <avatarID:> int (heading:) float float float
            String command = text.substring(4);
            String [] args = gui.parseArguments(command);
            if (args[0].equals(""))
                args = gui.parseArguments(command.substring(1));
            Avatar avatar       = null;
            Vector3f dir = null;
            try {
                int id = Integer.parseInt(args[0]);
                avatar = objs.getAvatar(id);
                dir = new Vector3f(Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]));
            } 
            catch (Exception ex) {
                avatar = getSelectedAvatar();
                dir = new Vector3f(Float.parseFloat(args[0]), Float.parseFloat(args[1]), Float.parseFloat(args[2]));
            }
            if (avatar != null && dir != null)
            {
                gui.appendOutput(avatar.getName() + " will look towards: " + dir);
                avatar.goTo(avatar.getPositionRef(), dir);
            }
            else
                gui.appendOutput("syntax error, (required), <optional>\nlook <avatarID> (heading)");
            return true;
        }
        
        return false;
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
                if (obj instanceof imi.character.objects.ChairObject)
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
                boolean male = true;
                name = args[0];
                genderString = args[1];
                if (genderString.startsWith("f"))
                {
                    male = false;
                    genderString = "female";
                }
                else
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
                int index = 8;
                if (selectForInput)
                    index++;
                
                int [] presets = new int[7];
                if (male)
                {
                    presets[0] = (int) (Math.random() * 10000 % 4);
                    presets[1] = (int) (Math.random() * 10000 % 4);
                    presets[2] = (int) (Math.random() * 10000 % 6);
                    presets[3] = (int) (Math.random() * 10000 % 17);
                    presets[4] = (int) (Math.random() * 10000 % 4);
                    presets[5] = (int) (Math.random() * 10000 % 13);
                    presets[6] = (int) (Math.random() * 10000 % 26);
                }
                else
                {
                    presets[0] = (int) (Math.random() * 10000 % 3);
                    presets[1] = (int) (Math.random() * 10000 % 3);
                    presets[2] = (int) (Math.random() * 10000 % 3);
                    presets[3] = (int) (Math.random() * 10000 % 49);
                    presets[4] = (int) (Math.random() * 10000 % 4);
                    presets[5] = (int) (Math.random() * 10000 % 13);
                    presets[6] = (int) (Math.random() * 10000 % 26);
                }
                for (int i = 0; i < 7; i++)
                {
                    try {
                        presets[i] = Integer.parseInt(args[index]);
                        index++;
                    } catch (Exception ex) { }
                }
                
                Avatar newAvatar;
                if (male)
                    newAvatar = new Avatar(new MaleAvatarAttributes(name, presets[0], presets[1], presets[2], presets[3], presets[4], presets[5], presets[6]), wm);    
                else
                    newAvatar = new Avatar(new FemaleAvatarAttributes(name, presets[0], presets[1], presets[2], presets[3], presets[4], presets[5], presets[6]), wm);    
                
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
                objs.getObjects().remove(obj);
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
                    objs.getObjects().remove(obj);
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
    
    public boolean placeCommands(String text) {
        return false;
    }

    public Avatar getSelectedAvatar()
    {
        // The event processor provides the linkage between AWT events and input controls
        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class);
        AvatarControlScheme control = (AvatarControlScheme)eventProcessor.getInputScheme();
        return control.getCurrentlySelectedAvatar();
    }
    
    public String getPrefix() {
        return ""; // these are the core commands...
    }
}
