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
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.scene.camera.state.FirstPersonCamState;
import org.jdesktop.mtgame.WorldManager;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * This test demonstrates how to instantiate an avatar and provide customization
 * through a CharacterAttributes object.
 * @see DemoBase For information about the freebies that class provides
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class CustomizationExample extends DemoBase
{
    /**
     * Construct a new instance. This method must be defined to subclass
     * DemoBase.
     * @param args Command-line arguments
     */
    public CustomizationExample(String[] args)
    {
        super(args);
    }

    /**
     * Run this file!
     * @param args
     */
    public static void main(String[] args)
    {
        // Construction does all the work
        CustomizationExample worldTest = new CustomizationExample(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) 
    {
        // The event processor provides the linkage between AWT events and input controls
        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class);
        // Set the input scheme that we intend to use
        NinjaControlScheme control = (NinjaControlScheme)eventProcessor.setDefault(new NinjaControlScheme(null));

        // Create an attributes object describing the avatar
        MaleAvatarAttributes attribs = new MaleAvatarAttributes("WeirdGuy", true);
//        NinjaFemaleAvatarAttributes attribs = new NinjaFemaleAvatarAttributes("WeirdChick", 0, 1, 1, 1, 1);
        Avatar avatar = new Avatar(attribs, wm);
        
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);

        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();
 
        // change camera speed
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // give me a tree explorer!
        TreeExplorer te = new TreeExplorer();
        SceneEssentials se = new SceneEssentials();
        se.setSceneData(avatar.getJScene(), avatar.getPScene(), avatar, wm, null);
        te.setExplorer(se);
        te.setVisible(true);
    }

    
}
