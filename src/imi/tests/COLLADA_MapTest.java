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

import imi.environments.ColladaEnvironment;
import imi.gui.SceneEssentials;
import imi.scene.JScene;
import imi.scene.PScene;
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
public class COLLADA_MapTest extends BaseDefault
{
    public COLLADA_MapTest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        COLLADA_MapTest worldTest = new COLLADA_MapTest(args);
    }

    @Override
    protected void simpleSceneInit(JScene jscene, WorldManager wm, Entity jsentity, ArrayList<ProcessorComponent> processors)
    {
        PScene pscene = jscene.getPScene();
        Logger.getLogger("com.jme.scene").setLevel(Level.OFF);
        Logger.getLogger("org.collada").setLevel(Level.OFF);
        Logger.getLogger("com.jme.renderer.jogl").setLevel(Level.OFF);

        pscene.setUseRepository(true);
        
        ColladaEnvironment ourEnv = new ColladaEnvironment(wm, "assets/models/collada/Environments/Milan/DSI.dae", "MaldenLabs");

        m_sceneData = new SceneEssentials();
        m_sceneData.setSceneData(jscene, jscene.getPScene(), jsentity, wm, processors);
    }
    
    
}
