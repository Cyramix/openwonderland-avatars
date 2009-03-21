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
package imi.scene.polygonmodel.morph;

import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.NewFrameCondition;

/**
 * @author Lou Hayt
 */
public class MorphAnimationProcessor extends ProcessorComponent {

    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;
  
    /**
     * The animation target
     */
    private MorphAnimationInstance target = null;
    
    /**
     * A name
     */
    private String name = null;
    
    /**
     * The constructor
     */
    public MorphAnimationProcessor(String name, WorldManager worldManager, MorphAnimationInstance target) {
        this.worldManager = worldManager;
        this.target = target;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return (name);
    }
    
    /**
     * The initialize method
     */
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }
    
    /**
     * The Calculate method
     */
    public void compute(ProcessorArmingCollection collection) {
        
        
        target.updateAnimation(0.1f);                   // TODO : need deltaTime ... frame elapsed time
    }

    /**
     * The commit method
     */
    public void commit(ProcessorArmingCollection collection) {
        
        
        // TODO : am I missing something? - everything is done in compute()
        worldManager.addToUpdateList(target);
    }

    @Override
    public void compute() {

    }

    @Override
    public void commit() {

    }
}
