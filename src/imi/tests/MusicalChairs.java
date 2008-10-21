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
import imi.character.ninja.Ninja;
import imi.character.ninja.NinjaAvatar;
import imi.character.objects.Goal;
import imi.character.objects.ObjectCollection;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneEventProcessor;
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
        // Create an object collection for the musical chairs game
        ObjectCollection objs = new ObjectCollection("Musical Chairs Game Objects", wm);
        objs.generateChairs(Vector3f.ZERO, 100.0f, 20);
        
        // Create a character (name it "Shadow Blade") using the "Ninja" preset configuration
        Ninja shadowBlade = new Ninja("Shadow Blade", wm);
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(shadowBlade));
        control.getNinjaTeam().add(shadowBlade);
        shadowBlade.selectForInput();
        shadowBlade.setObjectCollection(objs);
        
        Ninja ninja1 = new Ninja("Ninja 1", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(5.0f)), null, wm);
        control.getNinjaTeam().add(ninja1);
        ninja1.setObjectCollection(objs);
        
        Ninja ninja2 = new Ninja("Ninja 2", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(10.0f)), null, wm);
        control.getNinjaTeam().add(ninja2);
        ninja2.setObjectCollection(objs);
        
        // Goal point
        wm.addUserData(Goal.class, new Goal(wm));
        
        // Friends 
        
        Adam adam = new Adam("Adam", wm);
        adam.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(-5.0f));
        control.getNinjaTeam().add(adam);
        adam.setObjectCollection(objs);
        
        NinjaAvatar avatar = new NinjaAvatar("Avatar", wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(-10.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objs);
        
//        Ninja ninja3 = new Ninja("Ninja 3", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(15.0f)), null, wm);
//        control.getNinjaTeam().add(ninja3);
//        ninja3.setObjectCollection(objs);
        
    }
    
}
