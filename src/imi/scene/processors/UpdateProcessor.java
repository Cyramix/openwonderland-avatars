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
package imi.scene.processors;

import imi.scene.Updatable;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 * Updates!
 * @author Ronald E Dahlgren
 */
public class UpdateProcessor extends ProcessorComponent
{
    /** Used for calculating the time step **/
    private double oldTime = 0.0;
    /** Target to update **/
    private Updatable target = null;
    /** Used to avoid clobbering **/
    private boolean synchronizer = false;

    /**
     * Construct a new instance and provide updates to the character
     * @param person
     */
    public UpdateProcessor(Updatable target)
    {
        this.target = target;
    }

    @Override
    public void compute(ProcessorArmingCollection collection) {

    }

    @Override
    public void commit(ProcessorArmingCollection collection) {
            if (!synchronizer)
            {
                synchronizer = true;
                double newTime = System.nanoTime() / 1000000000.0;
                double deltaTime = (double) (newTime - oldTime);
                oldTime = newTime;
                if (deltaTime > 1.0f)
                    deltaTime = 0.0f;
                target.update((float)deltaTime);
                synchronizer = false;
            }
    }

    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }
}
