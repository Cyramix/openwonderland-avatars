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
import imi.scene.PScene;
import java.util.ArrayList;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class NovemberDemo extends DemoBase
{
    /** The name of the world! **/
    protected final String WorldName = "OfficeLand";
    /** Maintain a reference to the environment **/
    private ColladaEnvironment theWorld = null;


    public NovemberDemo(String[] args){
        super(args);
    }

    public static void main(String[] args) {
        NovemberDemo worldTest = new NovemberDemo(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene,
            WorldManager wm,
            ArrayList<ProcessorComponent> processors)
    {
        // create the backdrop
        theWorld = new ColladaEnvironment(wm, "assets/models/collada/Environments/BizObj/BusinessObjectsCenter.dae", WorldName);
    }

}
