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
import imi.character.objects.Goal;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.utils.input.NinjaControlScheme;
import org.jdesktop.mtgame.WorldManager;

public class CharacterTest extends DemoBase
{
    public CharacterTest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        CharacterTest worldTest = new CharacterTest(args);
    }
    
    @Override
    protected void createDemoEntities(WorldManager wm) 
    {
        // Goal point
        // TODO : Place this elsewhere
        //wm.setGoalPoint(createSphereEntity(1.0f, ColorRGBA.blue, new PMatrix(new Vector3f(10.0f, 0.0f, 10.0f)), wm));
        wm.addUserData(Goal.class, new Goal(wm));
        
        // Shadow Blade
        Ninja shadowBlade = new Ninja("Shadow Blade", /*"assets/configurations/ninjaDude.xml",*/ wm);
        NinjaControlScheme control = (NinjaControlScheme)((JSceneAWTEventProcessor)wm.getUserData(JSceneAWTEventProcessor.class)).setDefault(new NinjaControlScheme(shadowBlade));
        shadowBlade.selectForInput();
        control.getNinjaTeam().add(shadowBlade);
        //shadowBlade.setObjectCollection(objs);
        
        Ninja ninja1 = new Ninja("Shadow Blade Slave1", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(5.0f)), null, wm);
        //ninja1.mimic(ninjaMaster);
        control.getNinjaTeam().add(ninja1);
        //ninja1.setObjectCollection(objs);
        
        Ninja ninja2 = new Ninja("Shadow Blade Slave2", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(10.0f)), null, wm);
        //ninja2.mimic(ninja1);
        control.getNinjaTeam().add(ninja2);
        //ninja2.setObjectCollection(objs);
        
        Ninja ninja3 = new Ninja("Shadow Blade Slave3", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(15.0f)), null, wm);
        //ninja3.mimic(ninja2);
        control.getNinjaTeam().add(ninja3);
        //ninja3.setObjectCollection(objs);
        
        Ninja ninja = new Ninja("Shadow Blade Slave", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(20.0f)), null, wm);
        control.getNinjaTeam().add(ninja);
        //ninja.setObjectCollection(objs);
        ninja = new Ninja("Shadow Blade Slave", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(25.0f)), null, wm);
        control.getNinjaTeam().add(ninja);
        //ninja.setObjectCollection(objs);
        ninja = new Ninja("Shadow Blade Slave", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(30.0f)), null, wm);
        control.getNinjaTeam().add(ninja);
        //ninja.setObjectCollection(objs);
        ninja = new Ninja("Shadow Blade Slave", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(35.0f)), null, wm);
        control.getNinjaTeam().add(ninja);
        //ninja.setObjectCollection(objs);
        ninja = new Ninja("Shadow Blade Slave", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(40.0f)), null, wm);
        control.getNinjaTeam().add(ninja);
        //ninja.setObjectCollection(objs);
        ninja = new Ninja("Shadow Blade Slave", new PMatrix().setTranslation(Vector3f.UNIT_X.mult(45.0f)), null, wm);
        control.getNinjaTeam().add(ninja);
        //ninja.setObjectCollection(objs);
    }
}
