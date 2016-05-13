/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
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
import imi.character.statemachine.GameState;
import imi.character.statemachine.GameContext;
import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationListener.AnimationMessageType;

/**
 * This class represents a character's general action behavior, it may
 * be played once and exit or repeat with oscilation (back and forth) or loop.
 * @author Lou Hayt
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class ActionState extends GameState 
{
    /** The context of this state */
    GameContext context = null;
            
    /** true if the animation played at least once since the state was entered */
    protected boolean bPlayedOnce           = false;
    /** true to keep repeating the animation */
    protected boolean bRepeat               = false;
    /** true to have an oscilating repeat, false to have it loop */
    protected boolean bRepeatWillOscilate   = false;
    
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
    public boolean toAction(Object data)
    {
        return true;
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);
        
        if (context.getSkeleton() != null)
            context.getSkeleton().getAnimationState().setReverseAnimation(false);
        
        // mark gesture playing to false
        context.setGesturePlayingInSitting(false);
        
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    protected void stateEnter(GameContext owner)
    {       
        super.stateEnter(owner);
        
        bPlayedOnce = false;
        
        // If the animation doesn't exist make it possible 
        // to exit the state
        if (context.getSkeleton() != null)
        {
            if ((owner.getCharacter().getCharacterParams().isUseSimpleStaticModel() ||
                    context.getSkeleton().getAnimationComponent().findCycle(getAnimationName(), 0) == -1)
                    && !((getAnimationName()!=null && getAnimationName().equals("Male_Wink")) 
                            || (getAnimationName()!=null && getAnimationName().equals("Female_Wink"))))
                bPlayedOnce = true;
        }
        
        // Stop the character
        context.getController().stop();
        
        if(bRepeat)
            gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(AnimationComponent.PlaybackMode.Loop);
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
                                 
        // Check for possible transitions
        if (bPlayedOnce || bRepeat)
            transitionCheck();
    }
    
    @Override
    public void notifyAnimationMessage(AnimationMessageType message, String messageString) 
    {
        if (message == AnimationMessageType.TransitionComplete)
        {
//            if (bRepeat)
//            {
//                if (bRepeatWillOscilate)
//                    gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Oscillate);
//                else
//                    gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Loop);
//            }
//            else
//                gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
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
    
    /**
     * Return true if the context indicates that the current repeating state
     * should exit
     * @param context
     * @return 
     */
    public static boolean isExitRepeat(GameContext context) {
        return (context.getTriggerState().isKeyPressed(TriggerNames.MiscAction.ordinal())        ||
                context.getTriggerState().isKeyPressed(TriggerNames.MiscActionInSitting.ordinal())        ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Right.ordinal())        ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Left.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Forward.ordinal())      ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Back.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Up.ordinal())           ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Down.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Left.ordinal())  ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Right.ordinal()) ||
                context.getTriggerState().isKeyPressed(TriggerNames.Idle.ordinal()));
    }
    
    public static boolean isExitRepeat1(GameContext context) {
        return (context.getTriggerState().isKeyPressed(TriggerNames.Move_Right.ordinal())        ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Left.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Forward.ordinal())      ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Back.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Up.ordinal())           ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Down.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Left.ordinal())  ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Right.ordinal()) ||
                context.getTriggerState().isKeyPressed(TriggerNames.Idle.ordinal()) ||
                context.getTriggerState().isKeyPressed(TriggerNames.GoSit.ordinal()) ||
                context.getTriggerState().isKeyPressed(TriggerNames.LieDown.ordinal()) ||
                context.getTriggerState().isKeyPressed(TriggerNames.GoSitLieDown.ordinal()));
    }
    
    /**
     * Used in sitting component
     * @param context
     * @return 
     */
    public static boolean isExitRepeatWithGoSit(GameContext context) {
        return (isExitRepeat(context) || 
                context.getTriggerState().isKeyPressed(TriggerNames.GoSit.ordinal()) ||
                context.getTriggerState().isKeyPressed(TriggerNames.LieDown.ordinal()) ||
                context.getTriggerState().isKeyPressed(TriggerNames.GoSitLieDown.ordinal()));
    }
    
    /**
     * Used in sitting component for lie down
     * @param context
     * @return 
     */
    public static boolean lieDownOnLeftClick(GameContext context) {
        return context.getTriggerState().isKeyPressed(TriggerNames.LieDownOnClick.ordinal());
    }
    
    /**
     * just to check if any movement happens or not
     * @param context
     * @return 
     */
    public static boolean isExitForMovements(GameContext context) {
        return (context.getTriggerState().isKeyPressed(TriggerNames.Move_Right.ordinal())        ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Left.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Forward.ordinal())      ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Back.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Up.ordinal())           ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Down.ordinal())         ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Left.ordinal())  ||
                context.getTriggerState().isKeyPressed(TriggerNames.Idle.ordinal())             ||
                context.getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Right.ordinal()));  
    }
}
