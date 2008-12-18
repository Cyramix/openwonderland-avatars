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
package imi.character.ninja;

import imi.character.statemachine.GameState;
import imi.character.statemachine.GameContext;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;

/**
 * This class represents a character's punching behavior
 * @author Lou
 */
public class PunchState extends GameState 
{
    GameContext ninjaContext = null;
            
    private boolean bPlayedOnce = false;
    
    /** The facial animation will play when entering the state if not null **/
    private String facialAnimationName    = null;
    private float  facialAnimationTimeIn  = 1.0f;
    private float  facialAnimationTimeOut = 2.0f;

    /**
     * Construct a new instance with the provided context.
     * @param master
     */
    public PunchState(GameContext master)
    {
        super(master);
        ninjaContext = master;
        
        setName("Punch");
        setAnimationName("Punch");
        setTransitionDuration(0.2f);
        setAnimationSpeed(1.0f);
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
        
        bPlayedOnce = false;
                      
        // Stop the character
        ninjaContext.getController().stop();
        
        if (facialAnimationName != null)
             ninjaContext.getCharacter().initiateFacialAnimation(facialAnimationName, facialAnimationTimeIn, facialAnimationTimeOut);
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
                                 
        // Check for possible transitions
        if (bPlayedOnce)
            transitionCheck();
    }
    
    @Override
    public void notifyAnimationMessage(AnimationMessageType message) 
    {
        if (message == AnimationMessageType.TransitionComplete)
            gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
        else if (message == AnimationMessageType.PlayOnceComplete)
            bPlayedOnce = true;
    }

    public String getFacialAnimationName() {
        return facialAnimationName;
    }

    /**
     * The animation will play when entering the state.
     * Set to null to disable facial animation.
     * @param facialAnimationName
     */
    public void setFacialAnimationName(String facialAnimationName) {
        this.facialAnimationName = facialAnimationName;
    }

    public float getFacialAnimationTimeIn() {
        return facialAnimationTimeIn;
    }

    public void setFacialAnimationTimeIn(float facialAnimationTimeIn) {
        this.facialAnimationTimeIn = facialAnimationTimeIn;
    }

    public float getFacialAnimationTimeOut() {
        return facialAnimationTimeOut;
    }

    public void setFacialAnimationTimeOut(float facialAnimationTimeOut) {
        this.facialAnimationTimeOut = facialAnimationTimeOut;
    }
}
