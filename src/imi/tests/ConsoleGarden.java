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
package imi.tests;

import com.jme.math.Vector3f;
import imi.character.objects.ObjectCollection;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.AvatarControlScheme;
import org.jdesktop.mtgame.WorldManager;

/**
 * Console commands:
 * (required) <optional>
 * 
 * list listAvatars listChairs listLocations
 * remove (ID)
 * selectAvatar (ID)
 * createAvatar (name) (gender) (position: xyz) <select> <direction: xyz>
 * 
 * @author Lou Hayt
 */
public class ConsoleGarden extends DemoBase
{
    public ConsoleGarden(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        // Give ourselves a nice environment
        String[] ourArgs = new String[] { "-env:assets/models/collada/Environments/Garden/Garden.dae" };
        ConsoleGarden worldTest = new ConsoleGarden(ourArgs);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Objects", wm);
        
        // Create avatar input scheme
        AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(null));
        
        // Make a chair and let the control the collection so it can delete it
        objects.generateChairs(Vector3f.ZERO, 5.0f, 4);
        control.setObjectCollection(objects);
        
        // change camera speed and position it
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
        
        objects.getGUI();
        
        // Get the mouse events to be able to control the arm 
        control.getMouseEventsFromCamera();

//        // Construct a tree explorer for analyzing the scene graph
//        TreeExplorer te = new TreeExplorer();
//        SceneEssentials se = new SceneEssentials();
//        se.setSceneData(avatar.getJScene(), avatar.getPScene(), avatar, wm, null);
//        te.setExplorer(se);
//        te.setVisible(true);

//        JFrame_InstrumentationGUI instruments = new JFrame_InstrumentationGUI(wm);
//        instruments.setVisible(true);
        
    }

}
