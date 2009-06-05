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

import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 * This class provides a character with consistent updates and time deltas.
 * @author Lou Hayt
 */
public class CharacterProcessor extends ProcessorComponent
{
    private double oldTime = 0.0;
    private boolean synchronizer = false;
    private boolean enabled = true;
    private imi.character.Character character = null;

    /**
     * Construct a new instance and provide updates to the character
     * @param person
     */
    public CharacterProcessor(imi.character.Character person)
    {
        character = person;
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) {

    }

    @Override
    public void commit(ProcessorArmingCollection collection) {
        synchronized(this)
        {
            if (!enabled)
                return;
            if (!synchronizer)
            {
                synchronizer = true;
                double newTime = System.nanoTime() / 1000000000.0;
                double deltaTime = (double) (newTime - oldTime);
                oldTime = newTime;
                character.update((float)deltaTime);
                synchronizer = false;
            }
        }
    }

    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }

    public void stop()
    {
        synchronized(this) {
            enabled = false;
        }
    }

    public void start()
    {
        synchronized(this) {
            enabled = true;
        }
    }

    
    public void compute() {

    }

    
    public void commit() {

    }
}
