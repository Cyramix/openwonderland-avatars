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
package imi.character.ninja;

import imi.character.statemachine.GameState;
import imi.character.statemachine.GameContext;

/**
 *
 * @author Lou
 */
public class PunchState extends GameState 
{
    NinjaContext ninjaContext = null;
            
    private float exitCounter      = 0.0f;
    private float minimumTimeBeforeTransition = 0.5f;
    
    private boolean bReverse = false;
        
    public PunchState(NinjaContext master)
    {
        super(master);
        ninjaContext = master;
        
        setName("Punch");
        setAnimationName("Punch");
        setTransitionDuration(0.2f);
        setAnimationSpeed(1.5f);
    }
    
    /**
     * Entry point method, validates the transition
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toPunch(Object data)
    {
        return true;
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);
        
        if (ninjaContext.getSkeleton() != null)
            ninjaContext.getSkeleton().getAnimationState().setReverseAnimation(false);
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    {       
        super.stateEnter(owner);
        
        exitCounter   = 0.0f;
                      
        // Stop the character
        ninjaContext.getController().stop();
        
        // Set reverse animation
        if (ninjaContext.getSkeleton() != null)
        {
            if (bReverse)
                ninjaContext.getSkeleton().getAnimationState().setReverseAnimation(true);
            else
                ninjaContext.getSkeleton().getAnimationState().setReverseAnimation(false);
        }
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
                
        exitCounter += deltaTime;
                                 
        // Check for possible transitions
        if (exitCounter > minimumTimeBeforeTransition)
            transitionCheck();
    }
    
    public float getMinimumTimeBeforeTransition() {
        return minimumTimeBeforeTransition;
    }

    public void setMinimumTimeBeforeTransition(float minimumTimeBeforeTransition) {
        this.minimumTimeBeforeTransition = minimumTimeBeforeTransition;
    }

    public boolean isReverse() {
        return bReverse;
    }

    public void setReverse(boolean bReverse) {
        this.bReverse = bReverse;
    }
    
}
