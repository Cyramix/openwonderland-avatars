/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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

import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import imi.scene.SkeletonNode;

/**
 * Generic state for animation that has enter, cycle and exit phases.
 * 
 * @author Lou Hayt
 */
public class CycleActionState extends ActionState
{
    /** The inherited animation settings will be used for
     the animation that enteres the state **/
    
    /** The animation that cycles once the enter animation is done **/
    private String  cycleAnimationName      = null;
    private boolean bCycleAnimationSet      = false;
    private float   cycleTransitionDuration = 0.2f;
    private float   cycleAnimationSpeed     = 1.0f;
    
    /** The animation to get out of the state **/
    private String  exitAnimationName       = null;
    private boolean bExiting                = false;
    private boolean bExitAnimationSet       = false;
    private boolean bExitAnimationReverse   = false;
    private float   exitTransitionDuration  = 0.2f;
    private float   exitAnimationSpeed      = 1.0f;
    
    private boolean bSimpleAction           = false;

    /**
     * Create a state tied to this context
     * @param master
     */
    public CycleActionState(GameContext master)
    {
        super(master);
        setName("CycleAction");
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    protected void stateEnter(GameContext owner)
    {       
        super.stateEnter(owner);
        if (bSimpleAction)
            return;

        bExiting           = false;
        bCycleAnimationSet = false;
        bExitAnimationSet  = false;
        
        // If the animation doesn't exist make it possible 
        // to exit the state
        if (context.getSkeleton() != null)
        {
            if ( owner.getCharacter().getCharacterParams().isUseSimpleStaticModel()||
                 context.getSkeleton().getAnimationComponent().findCycle(getAnimationName(), 0) == -1 ||
                 context.getSkeleton().getAnimationComponent().findCycle(cycleAnimationName, 0) == -1 ||
                 context.getSkeleton().getAnimationComponent().findCycle(exitAnimationName,  0) == -1 )
            {
                bPlayedOnce        = true;
                bCycleAnimationSet = true;
                bExitAnimationSet  = true;
                setAnimationSetBoolean(true);
            }
        }
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    public void update(float deltaTime)
    {
        if (bSimpleAction)
        {
            super.update(deltaTime);
            return;
        }

        if (!isAnimationSet())
            setAnimation();

        if (bExiting)
        {
            if (!bExitAnimationSet)
            {
                bPlayedOnce = false;
                setExitAnimation();
            }
        }
        else if (bPlayedOnce && isAnimationSet() && !bCycleAnimationSet)
        {
            setCycleAnimation();
        }

        // Allow an exit
        if ( bPlayedOnce && ActionState.isExitRepeat(context) )
        {
            bExiting = true;
        }
        
        // Check for possible transitions
        if (bExitAnimationSet && bPlayedOnce && !context.isTransitioning())
            transitionCheck();
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    public void notifyAnimationMessage(AnimationMessageType message)
    {
//        int c = gameContext.getSkeleton().getAnimationState().getCurrentCycle();
//        System.out.println("cycle " + gameContext.getSkeleton().getAnimationGroup().getCycle(c).getName() + " " + c);
////        if (gameContext.getSkeleton().getAnimationState().getCurrentCyclePlaybackMode() == PlaybackMode.Loop)
////            System.out.println("loop " + message);
//
//        if (message == AnimationMessageType.TransitionComplete)
//        {
//            if (false)
//            {
//                if (bRepeatWillOscilate)
//                    gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Oscillate);
//                else
//                    gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Loop);
//            }
//            else
//                gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);

        if (message == AnimationMessageType.PlayOnceComplete)
        {
            bPlayedOnce = true;
        }
    }

    private void setCycleAnimation() 
    { 
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null)
        {
            skeleton.getAnimationState().setTransitionDuration(cycleTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(cycleAnimationSpeed);
            skeleton.getAnimationState().setReverseAnimation(false);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.Loop);
            bCycleAnimationSet = skeleton.transitionTo(cycleAnimationName, false);
            setAnimationSetBoolean(true);
        }
    }
    
    private void setExitAnimation() 
    { 
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null)
        {
            skeleton.getAnimationState().setTransitionDuration(exitTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(exitAnimationSpeed);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.PlayOnce);
            bExitAnimationSet = skeleton.transitionTo(exitAnimationName, bExitAnimationReverse);
            setAnimationSetBoolean(true);
        }
    }
    
    public boolean isSimpleAction() {
        return bSimpleAction;
    }

    public void setSimpleAction(boolean bSimpleAction) {
        this.bSimpleAction = bSimpleAction;
    }

    public boolean isCycleAnimationSet() {
        return bCycleAnimationSet;
    }

    public boolean isExitAnimationSet() {
        return bExitAnimationSet;
    }

    public boolean isExiting() {
        return bExiting;
    }
    
    public void exit() {
        bExiting = true;
    }

    public String getCycleAnimationName() {
        return cycleAnimationName;
    }

    public void setCycleAnimationName(String cycleAnimationName) {
        this.cycleAnimationName = cycleAnimationName;
    }

    public float getCycleAnimationSpeed() {
        return cycleAnimationSpeed;
    }

    public void setCycleAnimationSpeed(float cycleAnimationSpeed) {
        this.cycleAnimationSpeed = cycleAnimationSpeed;
    }

    public float getCycleTransitionDuration() {
        return cycleTransitionDuration;
    }

    public void setCycleTransitionDuration(float cycleTransitionDuration) {
        this.cycleTransitionDuration = cycleTransitionDuration;
    }

    public String getExitAnimationName() {
        return exitAnimationName;
    }

    public void setExitAnimationName(String exitAnimationName) {
        this.exitAnimationName = exitAnimationName;
    }

    public float getExitAnimationSpeed() {
        return exitAnimationSpeed;
    }

    public void setExitAnimationSpeed(float exitAnimationSpeed) {
        this.exitAnimationSpeed = exitAnimationSpeed;
    }

    public float getExitTransitionDuration() {
        return exitTransitionDuration;
    }

    public void setExitTransitionDuration(float exitTransitionDuration) {
        this.exitTransitionDuration = exitTransitionDuration;
    }

    public boolean isExitAnimationReverse() {
        return bExitAnimationReverse;
    }

    public void setExitAnimationReverse(boolean bExitAnimationReverse) {
        this.bExitAnimationReverse = bExitAnimationReverse;
    }
}
