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

import imi.scene.PScene;
import imi.scene.utils.visualizations.VerletVisualManager;
import java.util.ArrayList;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Ronald E Dahlgren
 */
public class VerletVisualizationTest extends DemoBase
{
    public static void main(final String[] args)
    {
        final VerletVisualizationTest app = new VerletVisualizationTest(args);
    }

    public VerletVisualizationTest(String[] args)
    {
        super(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) {
        super.simpleSceneInit(pscene, wm, processors);
        VerletVisualManager visualizer = new VerletVisualManager("The test renderer", wm);
    }


}
