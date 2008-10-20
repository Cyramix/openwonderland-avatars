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
import imi.character.ninja.PunchState;
import imi.character.ninja.SitState;
import imi.character.ninja.TurnState;
import imi.character.ninja.WalkState;
import imi.character.objects.Goal;
import imi.scene.PMatrix;
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
        Ninja shadowBlade = new Ninja("Shadow Blade", /*"assets/configurations/ninjaDude.xml",*/ wm);
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(shadowBlade));
        shadowBlade.selectForInput();
        control.getNinjaTeam().add(shadowBlade);
     
        // Goal point 
        // TODO : Put the goal point somewhere else in the system
        //wm.setGoalPoint(createSphereEntity(1.0f, ColorRGBA.red, new PMatrix(new Vector3f(10.0f, 0.0f, 10.0f)), wm));
        wm.addUserData(Goal.class, new Goal(wm));
        
        if (true)
        {
            // Adam
            PMatrix origin = new PMatrix();
            origin.buildRotationY(160.0f);
            origin.setTranslation(Vector3f.UNIT_X.mult(5.0f));
            Ninja adam = new Ninja("Adam", origin, "Adam.ms3d", "AdamDiffuse.png", 0.06f, wm);
            control.getNinjaTeam().add(adam);

            // tweak adam
            adam.getController().setReverseHeading(true);
            adam.getContext().getStates().get(PunchState.class).setAnimationSpeed(3.0f);
            ((PunchState)adam.getContext().getStates().get(PunchState.class)).setMinimumTimeBeforeTransition(1.25f);
            adam.getContext().getStates().get(TurnState.class).setAnimationName("StrafeRight");
            adam.getContext().getStates().get(TurnState.class).setAnimationSpeed(5.0f);
            ((WalkState)adam.getContext().getStates().get(WalkState.class)).setWalkSpeedFactor(5.0f);
            ((WalkState)adam.getContext().getStates().get(WalkState.class)).setWalkSpeedMax(5.0f);
            adam.getContext().getStates().get(SitState.class).setAnimationName("CrouchWalk");
            ((SitState)adam.getContext().getStates().get(SitState.class)).setIdleSittingAnimationName("Crawl");
            ((SitState)adam.getContext().getStates().get(SitState.class)).setSittingAnimationTime(2.0f);
            ((SitState)adam.getContext().getStates().get(SitState.class)).setGettingUpAnimationName("CrouchWalk");
            ((SitState)adam.getContext().getStates().get(SitState.class)).setGettingUpAnimationTime(2.0f);
        }
        
    }
}
