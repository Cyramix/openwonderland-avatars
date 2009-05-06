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
package imi.scene.utils;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.PostEventCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class is a temporary workaround for the lighting issues we are suffering
 * from.
 * @author Ronald E Dahlgren
 */
public class LightFixingDeity extends Entity
{
    private final long FIX_THE_LIGHTS = 0xDEADBEEF;
    /** Root of our jME stuffs **/
    private Node root = null;

    /** Pandora's Hack Box **/
    private final Box box = new Box("Magical Box of Hacks!", Vector3f.ZERO, Vector3f.ZERO);

    public LightFixingDeity(WorldManager wm)
    {
        super("Lucifer"); // The light bringer!
        root = new Node("Magical Light Node");

        RenderComponent rc = wm.getRenderManager().createRenderComponent(root);
        // Put in our special processor component
        final WorldManager fwm = wm;
        ProcessorComponent pc = new ProcessorComponent() {

            @Override
            public void compute(ProcessorArmingCollection arg0) {
                root.attachChild(box); // Dont ask why this works...
                fwm.removeEntity(this.getEntity()); // Finished our job, time to leave
            }

            @Override
            public void commit(ProcessorArmingCollection arg0) {

            }

            @Override
            public void initialize() {
                this.setArmingCondition(new PostEventCondition(this, new long[] { FIX_THE_LIGHTS }));
            }

            @Override
            public void compute() {
                root.attachChild(box); // Dont ask why this works...
                fwm.removeEntity(this.getEntity()); // Finished our job, time to leave
            }

            @Override
            public void commit() {

            }
        };
        this.addComponent(RenderComponent.class, rc);
        this.addComponent(ProcessorComponent.class, pc);
        wm.addEntity(this);
        wm.postEvent(FIX_THE_LIGHTS);
    }
}
