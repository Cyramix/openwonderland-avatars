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
import imi.character.ninja.Ninja;
import imi.character.ninja.NinjaAvatar;
import imi.character.objects.Goal;
import imi.character.objects.ObjectCollection;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import org.jdesktop.mtgame.WorldManager;
import imi.character.ninja.Adam;

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
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        
        // Goal point
        wm.addUserData(Goal.class, new Goal(wm));
        
        // Create an object collection for the musical chairs game
        ObjectCollection objs = new ObjectCollection("Musical Chairs Game Objects", wm);
        objs.generateChairs(Vector3f.ZERO, 100.0f, 20);
        
        if (false)
        {
            NinjaAvatar avatar = new NinjaAvatar("Avatar", wm);
            control.getNinjaTeam().add(avatar);
            avatar.selectForInput();
            avatar.setObjectCollection(objs);
            
            Adam adam = new Adam("Adam", wm);
            adam.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(-5.0f));
            control.getNinjaTeam().add(adam);
            adam.setObjectCollection(objs);
        }
        else
        {
            Ninja shadowBlade = new Ninja("Shadow Blade", null, 0.22f,wm);
            shadowBlade.selectForInput();
            control.getNinjaTeam().add(shadowBlade);
            shadowBlade.setObjectCollection(objs);
        }
        
        Ninja ninja1 = new Ninja("Ninja 1", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(5.0f)), 0.22f, wm);
        control.getNinjaTeam().add(ninja1);
        ninja1.setObjectCollection(objs);
    }
    
}
