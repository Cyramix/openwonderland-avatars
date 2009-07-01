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
package imi.character;

import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 * This class provides a character with consistent updates and time deltas.
 * @author Lou Hayt
 */
public class CharacterProcessor extends ProcessorComponent
{
    /** Time caching behaviors! **/
    private double oldTime = 0.0;
    private final Character character;

    /**
     * Construct a new instance and provide updates to the character
     * @param person
     * @throws IllegalArgumentException If {@code person == null}
     */
    public CharacterProcessor(Character person)
    {
        if (person == null)
            throw new IllegalArgumentException("Null character provided!");
        character = person;
    }

    /**
     * {@inheritDoc ProcessorComponent}
     */
    @Override
    public void compute(ProcessorArmingCollection collection) {

    }

    /**
     * {@inheritDoc ProcessorComponent}
     */
    @Override
    public synchronized void commit(ProcessorArmingCollection collection) {
        if (!isEnabled())
            return;
        double newTime = System.nanoTime() / 1000000000.0;
        double deltaTime = (double) (newTime - oldTime);
        oldTime = newTime;
        character.update((float)deltaTime);
    }

    /**
     * {@inheritDoc ProcessorComponent}
     */
    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }
}
