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
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.processors.TestHierarchyAnimationProcessor;
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;

public class Demo extends DemoBase
{
    public Demo(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        Demo worldTest = new Demo(args);
    }
    
    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        // Create Robert
        PPolygonModelInstance modelInst = pscene.addModelInstance(createArticulatedModel(1.3f, 1.0f, 2.0f, 10.0f, 3.0f, new PMatrix()), new PMatrix());
        
        // Grab the head, the hardcoded way!                                 
        PPolygonMeshInstance meshInstance = (PPolygonMeshInstance)modelInst.getChild(0).getChild(0).getChild(0).getChild(0);
        
        // Make Robert animate
        processors.add(new TestHierarchyAnimationProcessor(meshInstance, 0.01f, Vector3f.UNIT_X));
    }
}
