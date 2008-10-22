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
import imi.character.objects.Goal;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou
 */
public class AdamAndFriends extends DemoBase
{
    public AdamAndFriends(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        AdamAndFriends worldTest = new AdamAndFriends(args);
    }
    
    @Override
    protected void createDemoEntities(WorldManager wm) 
    {
        // Shadow Blade
        Ninja shadowBlade = new Ninja("Shadow Blade", null, 0.22f, wm);
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(shadowBlade));
        shadowBlade.selectForInput();
        control.getNinjaTeam().add(shadowBlade);
     
        // Adam
        Adam adam = new Adam("Adam", wm);
        adam.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(5.0f));
        control.getNinjaTeam().add(adam);
        
        // Goal point 
        wm.addUserData(Goal.class, new Goal(wm));

    }
}
