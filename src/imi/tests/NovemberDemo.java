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
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.objects.LocationNode;
import imi.character.objects.ObjectCollection;
import imi.environments.ColladaEnvironment;
import imi.scene.PScene;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.graph.Connection;
import imi.utils.graph.Connection.ConnectionDirection;
import imi.utils.input.NinjaControlScheme;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class NovemberDemo extends DemoBase
{
    /** The name of the world! **/
    protected final String WorldName = "OfficeLand";
    /** Maintain a reference to the environment **/
    private ColladaEnvironment theWorld = null;


    public NovemberDemo(String[] args){
        super(args);
    }

    public static void main(String[] args) {
        Logger.getLogger("com.jme.renderer.jogl.JOGLRenderer").setLevel(Level.OFF);
        NovemberDemo worldTest = new NovemberDemo(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene,
            WorldManager wm,
            ArrayList<ProcessorComponent> processors)
    {
        // create the backdrop
        //theWorld = new ColladaEnvironment(wm, "assets/models/collada/Environments/BizObj/BusinessObjectsCenter.dae", WorldName);
        theWorld = new ColladaEnvironment(wm, "assets/models/collada/Environments/MPK20/MPK20.dae", WorldName);
        
        ObjectCollection objects = new ObjectCollection("Objects", wm);
        
        LocationNode lobbyCenter        = new LocationNode("lobbyCenter", new Vector3f(3.905138f, 0.0f, 18.265793f), 7.0f, wm, objects);
        LocationNode hallEntry          = new LocationNode("hallEntry", new Vector3f(12.296166f, 0.0f, 3.6152136f), 2.0f, wm, objects);
        LocationNode yellowHallEntry    = new LocationNode("yellowHallEntry", new Vector3f(17.967321f, 0.0f, 0.85017294f), 1.0f, wm, objects);
        LocationNode redHallEntry       = new LocationNode("redHallEntry", new Vector3f(18.030338f, 0.0f, 5.632542f), 1.0f, wm, objects);
        LocationNode blueHallEntry      = new LocationNode("blueHallEntry", new Vector3f(18.00801f, 0.0f, 3.6330051f), 1.0f, wm, objects);
        
        LocationNode yellowEntryOut     = new LocationNode("yellowEntryOut", new Vector3f(29.747492f, 0.0f, 1.1190792f), 1.0f, wm, objects);
        LocationNode yellowEntryDoor    = new LocationNode("yellowEntryDoor", new Vector3f(29.771223f, 0.0f, -1.7147286f), 0.5f, wm, objects);
        LocationNode yellowEntryIn      = new LocationNode("yellowEntryIn", new Vector3f(31.348663f, 0.0f, -6.361941f), 1.0f, wm, objects);
        LocationNode yellowRoom         = new LocationNode("yellowRoom", new Vector3f(45.822628f, 0.0f, -10.891217f), 5.0f, wm, objects);
        LocationNode yellowExitIn       = new LocationNode("yellowExitIn", new Vector3f(26.164415f, 0.0f, -21.734386f), 1.0f, wm, objects);
        LocationNode yellowExitDoor     = new LocationNode("yellowExitDoor", new Vector3f(22.415726f, 0.0f, -21.85774f), 0.5f, wm, objects);
        LocationNode yellowExitOut      = new LocationNode("yellowExitOut", new Vector3f(18.572618f, 0.0f, -21.50969f), 1.0f, wm, objects);
  
        LocationNode redEntryOut        = new LocationNode("redEntryOut", new Vector3f(23.176514f, 0.0f, 5.167732f), 1.0f, wm, objects);
        LocationNode redEntryDoor       = new LocationNode("redEntryDoor", new Vector3f(23.561344f, 0.0f, 6.9372473f), 0.5f, wm, objects);
        LocationNode redEntryIn         = new LocationNode("redEntryIn", new Vector3f(25.37261f, 0.0f, 11.449364f), 1.0f, wm, objects);
        LocationNode redRoom            = new LocationNode("redRoom", new Vector3f(31.717348f, 0.0f, 16.338402f), 5.0f, wm, objects);
        LocationNode redExitIn          = new LocationNode("redExitIn", new Vector3f(36.38373f, 0.0f, 9.23577f), 1.0f, wm, objects);
        LocationNode redExitDoor        = new LocationNode("redExitDoor", new Vector3f(38.145355f, 0.0f, 7.2389126f), 0.5f, wm, objects);
        LocationNode redExitOut         = new LocationNode("redExitOut", new Vector3f(38.63156f, 0.0f, 5.106365f), 1.0f, wm, objects);
  
        LocationNode blueEntryOut       = new LocationNode("blueEntryOut", new Vector3f(40.834335f, 0.0f, 3.309015f), 1.0f, wm, objects);
        LocationNode blueEntryDoor      = new LocationNode("blueEntryDoor", new Vector3f(41.546036f, 0.0f, 7.028772f), 0.5f, wm, objects);
        LocationNode blueEntryIn        = new LocationNode("blueEntryIn", new Vector3f(42.251007f, 0.0f, 10.075825f), 1.0f, wm, objects);
        LocationNode blueRoom           = new LocationNode("blueRoom", new Vector3f(46.890636f, 0.0f, 16.11433f), 5.0f, wm, objects);
        LocationNode blueExitIn         = new LocationNode("blueExitIn", new Vector3f(54.813347f, 0.0f, 9.560907f), 1.0f, wm, objects);
        LocationNode blueExitDoor       = new LocationNode("blueExitDoor", new Vector3f(56.226494f, 0.0f, 7.079925f), 0.5f, wm, objects);
        LocationNode blueExitOut        = new LocationNode("blueExitOut", new Vector3f(40.834335f, 0.0f, 3.4001088f), 1.0f, wm, objects);

        lobbyCenter.addConnection(new Connection("yellowRoom", lobbyCenter, hallEntry, ConnectionDirection.OneWay));
        lobbyCenter.addConnection(new Connection("blueRoom", lobbyCenter, hallEntry, ConnectionDirection.OneWay));
        lobbyCenter.addConnection(new Connection("redRoom", lobbyCenter, hallEntry, ConnectionDirection.OneWay));
        
        hallEntry.addConnection(new Connection("yellowRoom", hallEntry, yellowHallEntry, ConnectionDirection.OneWay));
        hallEntry.addConnection(new Connection("blueRoom", hallEntry, blueHallEntry, ConnectionDirection.OneWay));
        hallEntry.addConnection(new Connection("redRoom", hallEntry, redHallEntry, ConnectionDirection.OneWay));
        hallEntry.addConnection(new Connection("lobbyCenter", hallEntry, lobbyCenter, ConnectionDirection.OneWay));
        
        yellowHallEntry.addConnection(new Connection("yellowRoom", yellowHallEntry, yellowEntryOut, ConnectionDirection.OneWay));
        yellowHallEntry.addConnection(new Connection("blueRoom", yellowHallEntry, hallEntry, ConnectionDirection.OneWay));
        yellowHallEntry.addConnection(new Connection("redRoom", yellowHallEntry, hallEntry, ConnectionDirection.OneWay));
        yellowHallEntry.addConnection(new Connection("lobbyCenter", yellowHallEntry, hallEntry, ConnectionDirection.OneWay));
        
        redHallEntry.addConnection(new Connection("yellowRoom", redHallEntry, hallEntry, ConnectionDirection.OneWay));
        redHallEntry.addConnection(new Connection("blueRoom", redHallEntry, hallEntry, ConnectionDirection.OneWay));
        redHallEntry.addConnection(new Connection("redRoom", redHallEntry, redEntryOut, ConnectionDirection.OneWay));
        redHallEntry.addConnection(new Connection("lobbyCenter", redHallEntry, hallEntry, ConnectionDirection.OneWay));
        
        blueHallEntry.addConnection(new Connection("yellowRoom", blueHallEntry, hallEntry, ConnectionDirection.OneWay));
        blueHallEntry.addConnection(new Connection("blueRoom", blueHallEntry, blueEntryOut, ConnectionDirection.OneWay));
        blueHallEntry.addConnection(new Connection("redRoom", blueHallEntry, hallEntry, ConnectionDirection.OneWay));
        blueHallEntry.addConnection(new Connection("lobbyCenter", blueHallEntry, hallEntry, ConnectionDirection.OneWay));
        
        yellowEntryOut.addConnection(new Connection("yellowRoom", yellowEntryOut, yellowEntryDoor, ConnectionDirection.OneWay));
        yellowEntryOut.addConnection(new Connection("blueRoom", yellowEntryOut, yellowHallEntry, ConnectionDirection.OneWay));
        yellowEntryOut.addConnection(new Connection("redRoom", yellowEntryOut, yellowHallEntry, ConnectionDirection.OneWay));
        yellowEntryOut.addConnection(new Connection("lobbyCenter", yellowEntryOut, yellowHallEntry, ConnectionDirection.OneWay));
        
        yellowEntryDoor.addConnection(new Connection("yellowRoom", yellowEntryDoor, yellowEntryIn, ConnectionDirection.OneWay));
        yellowEntryDoor.addConnection(new Connection("blueRoom", yellowEntryDoor, yellowEntryOut, ConnectionDirection.OneWay));
        yellowEntryDoor.addConnection(new Connection("redRoom", yellowEntryDoor, yellowEntryOut, ConnectionDirection.OneWay));
        yellowEntryDoor.addConnection(new Connection("lobbyCenter", yellowEntryDoor, yellowEntryOut, ConnectionDirection.OneWay));
        
        yellowEntryIn.addConnection(new Connection("yellowRoom", yellowEntryIn, yellowRoom, ConnectionDirection.OneWay));
        yellowEntryIn.addConnection(new Connection("blueRoom", yellowEntryIn, yellowEntryDoor, ConnectionDirection.OneWay));
        yellowEntryIn.addConnection(new Connection("redRoom", yellowEntryIn, yellowEntryDoor, ConnectionDirection.OneWay));
        yellowEntryIn.addConnection(new Connection("lobbyCenter", yellowEntryIn, yellowEntryDoor, ConnectionDirection.OneWay));
        
        yellowRoom.addConnection(new Connection("blueRoom", yellowRoom, yellowExitIn, ConnectionDirection.OneWay));
        yellowRoom.addConnection(new Connection("redRoom", yellowRoom, yellowExitIn, ConnectionDirection.OneWay));
        yellowRoom.addConnection(new Connection("lobbyCenter", yellowRoom, yellowExitIn, ConnectionDirection.OneWay));
        
        yellowExitIn.addConnection(new Connection("yellowRoom", yellowExitIn, yellowRoom, ConnectionDirection.OneWay));
        yellowExitIn.addConnection(new Connection("blueRoom", yellowExitIn, yellowExitDoor, ConnectionDirection.OneWay));
        yellowExitIn.addConnection(new Connection("redRoom", yellowExitIn, yellowExitDoor, ConnectionDirection.OneWay));
        yellowExitIn.addConnection(new Connection("lobbyCenter", yellowExitIn, yellowExitDoor, ConnectionDirection.OneWay));
        
        yellowExitDoor.addConnection(new Connection("yellowRoom", yellowExitDoor, yellowExitIn, ConnectionDirection.OneWay));
        yellowExitDoor.addConnection(new Connection("blueRoom", yellowExitDoor, yellowExitOut, ConnectionDirection.OneWay));
        yellowExitDoor.addConnection(new Connection("redRoom", yellowExitDoor, yellowExitOut, ConnectionDirection.OneWay));
        yellowExitDoor.addConnection(new Connection("lobbyCenter", yellowExitDoor, yellowExitOut, ConnectionDirection.OneWay));
        
        yellowExitOut.addConnection(new Connection("yellowRoom", yellowExitOut, yellowExitDoor, ConnectionDirection.OneWay));
        yellowExitOut.addConnection(new Connection("blueRoom", yellowExitOut, yellowHallEntry, ConnectionDirection.OneWay));
        yellowExitOut.addConnection(new Connection("redRoom", yellowExitOut, yellowHallEntry, ConnectionDirection.OneWay));
        yellowExitOut.addConnection(new Connection("lobbyCenter", yellowExitOut, yellowHallEntry, ConnectionDirection.OneWay));
        

        int numberOfAvatars = 3;
        
        objects.generateChairs(lobbyCenter.getPosition(), 7.0f, numberOfAvatars);
        objects.generateChairs(yellowRoom.getPosition(), 7.0f, numberOfAvatars);

         // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        control.setCommandEntireTeam(false);
        control.setObjectCollection(objects);

        // Create avatar
        Avatar avatar = new Avatar(new MaleAvatarAttributes("Avatar", true), wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(lobbyCenter.getPosition());
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);

        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();
        
        // Make some more avatars
        float zStep = 1.0f;
        for (int i = 1; i < numberOfAvatars; i++)
        {
            cloneAvatar(control, objects, wm, 0.0f, 0.0f, zStep, lobbyCenter.getPosition());
            zStep += 5.0f;
        }

        // change the initial camera position
        FirstPersonCamState fpsState = (FirstPersonCamState)m_cameraProcessor.getState();
        fpsState.setCameraPosition(Vector3f.UNIT_Y.mult(2.3f));
    }

    private void cloneAvatar(NinjaControlScheme control, ObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset, Vector3f origin)
    {
        Avatar avatar = new Avatar(new MaleAvatarAttributes("Avatar Clone " + xOffset+yOffset+zOffset, true), wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset).add(origin));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }

}
