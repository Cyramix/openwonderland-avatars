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
package imi.scene.processors;

import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 *
 * @author Lou Hayt
 */
public class CharacterProcessor extends ProcessorComponent
{
    private float deltaTime = 1.0f / 60.0f;
    
    private imi.character.Character character = null;
    
    public CharacterProcessor(imi.character.Character person)
    {
        character = person;
        setRunInRenderer(true);
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) {
        
    }

    @Override
    public void commit(ProcessorArmingCollection collection) {
        character.update(deltaTime);
    }

    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }

}
