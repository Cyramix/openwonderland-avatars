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
import imi.character.ninja.NinjaAvatarAttributes;
import imi.character.objects.LocationNode;
import imi.character.objects.ObjectCollection;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.graph.Connection;
import imi.utils.graph.Connection.ConnectionDirection;
import imi.utils.input.NinjaControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou
 */
public class MusicalChairs extends DemoBase
{
    public MusicalChairs(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        MusicalChairs worldTest = new MusicalChairs(args);
    }
    
    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        int numberOfAvatars = 5;
        float block = 2.0f * numberOfAvatars;
        float halfBlock = 1.0f * numberOfAvatars;
        
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Musical Chairs Objects", wm);
        objects.generateChairs(new Vector3f(halfBlock, 0.0f, halfBlock), halfBlock, numberOfAvatars-1);
        
        // Create locations for the game
        LocationNode chairGame1 = new LocationNode("Location 1", Vector3f.ZERO, halfBlock, wm, objects);
        LocationNode chairGame2 = new LocationNode("Location 2", Vector3f.UNIT_X.mult(block),  halfBlock, wm, objects);
        LocationNode chairGame3 = new LocationNode("Location 3", new Vector3f(block, 0.0f, block),  halfBlock, wm, objects);
        LocationNode chairGame4 = new LocationNode("Location 4", Vector3f.UNIT_Z.mult(block),  halfBlock, wm, objects);
        
        // Create paths
        chairGame1.addConnection(new Connection("yellowRoom", chairGame1, chairGame2, ConnectionDirection.OneWay));
        chairGame2.addConnection(new Connection("yellowRoom", chairGame2, chairGame3, ConnectionDirection.OneWay));
        chairGame3.addConnection(new Connection("yellowRoom", chairGame3, chairGame4, ConnectionDirection.OneWay));
        chairGame4.addConnection(new Connection("yellowRoom", chairGame4, chairGame1, ConnectionDirection.OneWay));
        
        chairGame1.addConnection(new Connection("lobbyCenter", chairGame1, chairGame4, ConnectionDirection.OneWay));
        chairGame2.addConnection(new Connection("lobbyCenter", chairGame4, chairGame3, ConnectionDirection.OneWay));
        chairGame3.addConnection(new Connection("lobbyCenter", chairGame3, chairGame2, ConnectionDirection.OneWay));
        chairGame4.addConnection(new Connection("lobbyCenter", chairGame2, chairGame1, ConnectionDirection.OneWay));
     
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        control.setCommandEntireTeam(true);
        control.setObjectCollection(objects);
        
        // Create avatar
        NinjaAvatar avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar", true, false), wm);
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
        
//        NinjaAvatar bigBaby = new NinjaAvatar("Big Baby", wm);
//        bigBaby.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-5.0f));
//        control.getNinjaTeam().add(bigBaby);
//        bigBaby.setObjectCollection(objects);
        
//        Ninja shadowBlade = new Ninja("Shadow Blade", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(5.0f)), 0.22f, wm);
//        //shadowBlade.selectForInput();
//        control.getNinjaTeam().add(shadowBlade);
//        shadowBlade.setObjectCollection(objects);

//        Adam adam = new Adam("Adam", wm);
//        adam.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(-5.0f));
//        control.getNinjaTeam().add(adam);
//        adam.setObjectCollection(objects);
    }

    private void cloneAvatar(NinjaControlScheme control, ObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset) 
    {   
        NinjaAvatar avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar Clone " + xOffset+yOffset+zOffset, true, false), wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }
    
}
