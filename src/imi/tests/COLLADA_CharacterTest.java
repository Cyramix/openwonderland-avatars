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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.tests;


import com.jme.math.Vector3f;
import imi.character.ninja.NinjaAvatar;
import imi.character.objects.ObjectCollection;
import imi.scene.processors.FlexibleCameraProcessor;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Lou Hayt
 */
public class COLLADA_CharacterTest extends DemoBase
{
    public COLLADA_CharacterTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        COLLADA_CharacterTest worldTest = new COLLADA_CharacterTest(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        int numberOfAvatars = 1;
        
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Character Test Objects", wm);
        
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        ((FlexibleCameraProcessor)wm.getUserData(FlexibleCameraProcessor.class)).setControl(control);
        
        // Create avatar
        NinjaAvatar avatar = new NinjaAvatar("Avatar", wm);
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);

        // Make some more avatars
        float zStep = 5.0f;
        for (int i = 1; i < numberOfAvatars; i++)
        {
            cloneAvatar(control, objects, wm, 0.0f, 0.0f, zStep);
            zStep += 5.0f;
        }
        
        // Make a chair and let the control the collection so it can delete it
        objects.generateChairs(Vector3f.ZERO, 10.0f, 5);
        control.setObjectCollection(objects);
    }

    private void cloneAvatar(NinjaControlScheme control, ObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset) 
    {   
        NinjaAvatar avatar = new NinjaAvatar("Avatar Clone " + xOffset+yOffset+zOffset, wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }
    
}
