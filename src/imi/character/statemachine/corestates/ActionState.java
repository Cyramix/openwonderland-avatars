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
package imi.character.statemachine.corestates;

import imi.character.statemachine.GameState;
import imi.character.statemachine.GameContext;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;

/**
 * This class represents a character's general action behavior, it may
 * be played once and exit or repeat with oscilation (back and forth) or loop.
 * @author Lou Hayt
 */
public class ActionState extends GameState 
{
    /** The context of this state */
    GameContext context = null;
            
    /** true if the animation played at least once since the state was entered */
    private boolean bPlayedOnce = false;
    /** true to keep repeating the animation */
    private boolean bRepeat     = false;
    /** true to have an oscilating repeat, false to have it loop */
    private boolean bRepeatWillOscilate = false;
    
    /**
     * Construct a new instance with the provided context.
     * @param master
     */
    public ActionState(GameContext master)
    {
        super(master);
        context = master;
        setName("Action");
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
        
        if (context.getSkeleton() != null)
            context.getSkeleton().getAnimationState().setReverseAnimation(false);
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    {       
        super.stateEnter(owner);
        
        bPlayedOnce = false;
        
        // If the animation doesn't exist make it possible 
        // to exit the state
        if (context.getSkeleton() != null)
        {
            if (context.getSkeleton().getAnimationComponent().findCycle(getAnimationName(), 0) == -1)
                bPlayedOnce = true;
        }
        
        // Stop the character
        context.getController().stop();
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
                                 
        // Check for possible transitions
        if (bPlayedOnce || bRepeat)
            transitionCheck();
    }
    
    @Override
    public void notifyAnimationMessage(AnimationMessageType message) 
    {
        if (message == AnimationMessageType.TransitionComplete)
        {
            if (bRepeat)
            {
                if (bRepeatWillOscilate)
                    gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Oscillate);
                else
                    gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Loop);
            }
            else
                gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
        }
        else if (message == AnimationMessageType.PlayOnceComplete)
        {
            bPlayedOnce = true;
        }
    }

    /**
     * If repeat is on this state will not check for transitions
     * (it will repeat the animation again and again)
     * @return
     */
    public boolean isRepeat() {
        return bRepeat;
    }

    /**
     * Repeat animations for an open (undefined) period of time (e.g. talking on the cell phone)
     * @param bRepeat
     */
    public void setRepeat(boolean bRepeat) {
        this.bRepeat = bRepeat;
    }

    /**
     * if Repeat is turned on it may repeat the animation with a loop or
     * oscilation (back and forth)
     * @return
     */
    public boolean isRepeatWillOscilate() {
        return bRepeatWillOscilate;
    }

    /**
     * The animation repeats will either oscilate or loop
     * @param bRepeatWillOscilate
     */
    public void setRepeatWillOscilate(boolean bRepeatWillOscilate) {
        this.bRepeatWillOscilate = bRepeatWillOscilate;
    }

    /** True if the animation for this state played at least once
     * since the state was entered.
     * @return
     */
    public boolean isPlayedOnce() {
        return bPlayedOnce;
    }
}
