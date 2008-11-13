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
import imi.environments.ColladaEnvironment;
import imi.scene.PScene;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.ArrayList;
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
        NovemberDemo worldTest = new NovemberDemo(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene,
            WorldManager wm,
            ArrayList<ProcessorComponent> processors)
    {
        // create the backdrop
        //theWorld = new ColladaEnvironment(wm, "assets/models/collada/Environments/BizObj/BusinessObjectsCenter.dae", WorldName);


        int numberOfAvatars = 2;
        // make an object collection and a few chairs
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Musical Chairs Objects", wm);
        objects.generateChairs(new Vector3f(20.0f, 0.0f, 20.0f), 20.0f, numberOfAvatars-1);


         // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        control.setCommandEntireTeam(true);
        control.setObjectCollection(objects);

        // Create avatar
        NinjaAvatar avatar = new NinjaAvatar("Avatar", wm);
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);


        // Make some more avatars
        float zStep = 1.0f;
        for (int i = 1; i < numberOfAvatars; i++)
        {
            cloneAvatar(control, objects, wm, 0.0f, 0.0f, zStep);
            zStep += 5.0f;
        }
    }

    private void cloneAvatar(NinjaControlScheme control, ObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset)
    {
        NinjaAvatar avatar = new NinjaAvatar("Avatar Clone " + xOffset+yOffset+zOffset, wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }

}
