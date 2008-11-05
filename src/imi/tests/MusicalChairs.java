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
import imi.character.ninja.Adam;
import imi.character.ninja.NinjaAvatar;
import imi.character.objects.LocationNode;
import imi.character.objects.ObjectCollection;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.graph.Connection;
import imi.utils.graph.Connection.ConnectionDirection;
import imi.utils.input.NinjaControlScheme;
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
        MusicalChairs worldTest = new MusicalChairs(args);
    }
    
    @Override
    protected void createDemoEntities(WorldManager wm) 
    {
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Musical Chairs Objects", wm);
        
        // Create locations for the game
        LocationNode chairGame1 = new LocationNode("Location 1", Vector3f.ZERO, 15.0f, wm, objects);
        chairGame1.generateChairs(3);
        LocationNode chairGame2 = new LocationNode("Location 2", Vector3f.UNIT_X.mult(30.0f),  15.0f, wm, objects);
        chairGame2.generateChairs(3);
        LocationNode chairGame3 = new LocationNode("Location 3", Vector3f.UNIT_Z.mult(30.0f),  15.0f, wm, objects);
        chairGame3.generateChairs(3);
        
        // Create paths
        chairGame1.addConnection(new Connection("Location 3", chairGame1, chairGame2, ConnectionDirection.OneWay));
        chairGame1.addConnection(new Connection("Location 2", chairGame1, chairGame2, ConnectionDirection.OneWay));
        chairGame2.addConnection(new Connection("Location 3", chairGame2, chairGame3, ConnectionDirection.OneWay));
        chairGame2.addConnection(new Connection("Location 1", chairGame2, chairGame1, ConnectionDirection.OneWay));
        chairGame3.addConnection(new Connection("Location 1", chairGame3, chairGame2, ConnectionDirection.OneWay));
        chairGame3.addConnection(new Connection("Location 2", chairGame3, chairGame2, ConnectionDirection.OneWay));
     
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        
        // Create avatar
        NinjaAvatar avatar = new NinjaAvatar("Avatar", wm);
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);


        // Make some avatars
        //cloneAvatars(control, objects, wm);
        
        
        
        
        
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

    private void cloneAvatars(NinjaControlScheme control, ObjectCollection objects, WorldManager wm) 
    {   
        NinjaAvatar avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-5.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-10.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-15.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-20.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-25.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-30.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-35.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-40.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        avatar = new NinjaAvatar("Avatar Clone", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-45.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }
    
}
