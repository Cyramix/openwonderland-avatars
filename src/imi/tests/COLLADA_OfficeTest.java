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
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package imi.tests;

import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.objects.ObjectCollection;
import imi.environments.ColladaEnvironment;
import imi.scene.JScene;
import imi.scene.PScene;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * Testing environments
 * @author Ronald E Dahlgren
 */
public class COLLADA_OfficeTest extends DemoBase
{
    public COLLADA_OfficeTest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        COLLADA_OfficeTest worldTest = new COLLADA_OfficeTest(args);
    }

    protected void simpleSceneInit(JScene jscene, WorldManager wm, Entity jsentity, ArrayList<ProcessorComponent> processors)
    {
        PScene pscene = jscene.getPScene();
        Logger.getLogger("com.jme.scene").setLevel(Level.OFF);
        Logger.getLogger("org.collada").setLevel(Level.OFF);
        Logger.getLogger("com.jme.renderer.jogl").setLevel(Level.OFF);

        pscene.setUseRepository(true);
        
        ColladaEnvironment ourEnv = new ColladaEnvironment(wm, "assets/models/collada/Environments/Milan/DSI.dae", "MaldenLabs");
        
        
        ///////////
        
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        
        // Create an object collection for the musical chairs game
        ObjectCollection objs = new ObjectCollection("Musical Chairs Game Objects", wm);
        objs.generateChairs(Vector3f.UNIT_Z.mult(15.0f), 15.0f, 3);
        
        Avatar ColladaOne = new Avatar(new MaleAvatarAttributes("ColladaOne", true), wm);
        ColladaOne.setObjectCollection(objs);
        ColladaOne.selectForInput();
        control.getNinjaTeam().add(ColladaOne);
    }
    
    
}
