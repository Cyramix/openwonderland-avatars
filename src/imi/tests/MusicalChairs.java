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
import com.jme.renderer.ColorRGBA;
import imi.character.ninja.Ninja;
import imi.character.objects.ObjectCollection;
import imi.scene.PMatrix;
import imi.scene.processors.JSceneAWTEventProcessor;
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
        objs.generateChairs(Vector3f.ZERO, 50.0f, 3);
        
        // Create a character (name it "Shadow Blade") using the "Ninja" preset configuration
        Ninja shadowBlade = new Ninja("Shadow Blade", wm, "Ninja");
        NinjaControlScheme control = (NinjaControlScheme)((JSceneAWTEventProcessor)wm.getUserData(JSceneAWTEventProcessor.class)).setDefault(new NinjaControlScheme(shadowBlade));
        control.getNinjaTeam().add(shadowBlade);
        shadowBlade.selectForInput();
        shadowBlade.setObjectCollection(objs);
        
        // Goal point
        //wm.setGoalPoint(createSphereEntity(1.0f, ColorRGBA.blue, new PMatrix(new Vector3f(10.0f, 0.0f, 10.0f)), wm));
    }
    
}
