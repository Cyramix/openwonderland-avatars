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

import imi.character.CharacterEyes;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameStateChangeListener;
import imi.character.statemachine.GameStateChangeListenerRegisterar;
import imi.scene.SkeletonNode;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic state for animation that has enter, cycle and exit phases.
 *
 * @author Lou Hayt
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
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
    private static final Logger logger = Logger.getLogger(CycleActionState.class.getName());
    
    /** The animation to get out of the state **/
    private String  exitAnimationName       = null;
    private boolean bExiting                = false;
    private boolean bExitAnimationSet       = false;
    private boolean bExitAnimationReverse   = false;
    private float   exitTransitionDuration  = 0.2f;
    private float   exitAnimationSpeed      = 1.0f;

    private boolean bSimpleAction           = false;
    private boolean tempBlock = false;
    private final HashMap<String, List<String>> backupAnimations = new HashMap<String, List<String>>();
    private String prevAnimName = "";
    private int timer = 0;
    private ScheduledExecutorService winkScheduledService = null;
    private boolean bKeyPressed = false;

    /**
     * Create a state tied to this context
     * @param master
     */
    public CycleActionState(GameContext master) 
    {
        super(master);
        setName("CycleAction");
    }

    @Override
    protected void stateExit(GameContext owner) {
        super.stateExit(owner);
        if (winkScheduledService != null) {
            winkScheduledService.shutdown();
            winkScheduledService = null;
        }

        // call method of listener for all gestures in backup list
        boolean notifyListener = true;
        for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
            for (String anim : backupAnimations.keySet()) {
                lis.changeInState(this, anim, true, "");
                if (anim.equals(animationName)) {
                    notifyListener = false;
                }
            }
        }
        if (notifyListener) {
            for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this, animationName, true, "");
            }
        }
        backupAnimations.clear();
        bKeyPressed = false;
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    protected void stateEnter(GameContext owner) 
    {
        if (context.getTriggerState().isKeyPressed(AvatarContext.TriggerNames.MiscActionInSitting.ordinal())) {
            //release trigger
            context.setGesturePlayingInSitting(true);
        }

        super.stateEnter(owner);

        if (!context.isGesturePlayingInSitting()) {
            context.getCharacter().enableShadow(true);
        } else {
            context.getCharacter().enableShadow(false);
        }

        if (bSimpleAction) 
            return;

        bExiting           = false;
        bCycleAnimationSet = false;
        bExitAnimationSet  = false;

        // If the animation doesn't exist make it possible 
        // to exit the state
        if (context.getSkeleton() != null) 
        {
            if ((owner.getCharacter().getCharacterParams().isUseSimpleStaticModel()
                    || context.getSkeleton().getAnimationComponent().findCycle(getAnimationName(), 0) == -1
                    || context.getSkeleton().getAnimationComponent().findCycle(cycleAnimationName, 0) == -1
                    || context.getSkeleton().getAnimationComponent().findCycle(exitAnimationName, 0) == -1)
                    && !(getAnimationName().equals("Male_Wink") || getAnimationName().equals("Female_Wink"))) 
            {
                bPlayedOnce        = true;
                bCycleAnimationSet = true;
                bExitAnimationSet  = true;
                setAnimationSetBoolean(true);
            }
        }

        if (animationName.equals("Male_Wink") || animationName.equals("Female_Wink")) {
            playOnceCompletedForWink();
        }

        prevAnimName = "";

        //check if there is any movement of Avatar then just exit from the current state
        bKeyPressed = false;
        if (ActionState.isExitForMovements(context)) {
            onKeyPressed();
        }

    }

    private void playOnceCompletedForWink() {
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CycleActionState.class.getName()).log(Level.SEVERE, null, ex);
                }
                notifyAnimationMessage(AnimationMessageType.PlayOnceComplete);
            }
        }).start();
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    public void update(float deltaTime) 
    {
        if (timer != 0) {
            timer--;
            return;
        }

        if (ActionState.isExitForMovements(context)) {
            onKeyPressed();
        }

        if (context.getTriggerState().isKeyPressed(AvatarContext.TriggerNames.MiscAction.ordinal())) {
            context.triggerReleased(TriggerNames.MiscAction.ordinal());
        } else if (context.getTriggerState().isKeyPressed(AvatarContext.TriggerNames.MiscActionInSitting.ordinal())) {
            context.setGesturePlayingInSitting(true);
            context.triggerReleased(TriggerNames.MiscActionInSitting.ordinal());
        }

        if (tempBlock) {
            return;
        }

        if (!prevAnimName.equals(animationName)) {
            List<String> names = new ArrayList<String>();
            names.add(animationName);
            names.add(cycleAnimationName);
            names.add(exitAnimationName);
            names.add(String.valueOf(bExitAnimationReverse));
            
            //decide if we need to notify listener
            // just check if the animation name is already in backup list or not
            boolean notifyListener = true;
            if(backupAnimations.containsKey(animationName)) {
                notifyListener = false;
            }
            
            backupAnimations.put(animationName, names);
            prevAnimName = animationName;
            if (animationName.equals("Male_Wink") || animationName.equals("Female_Wink")) {
                List<String> animsToPlay = getLastAnim();
                if (animsToPlay != null && !animsToPlay.isEmpty()) {
                    setIdleAnimation(animsToPlay.get(1));
                }
                playOnceCompletedForWink();
                cycleAnimationName = animationName;
            }
            
            // call method of listener
            if(notifyListener) {
                for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                    lis.changeInState(this, animationName, false, "");
                }
            }
        }

        if (bSimpleAction) {
            super.update(deltaTime);
            return;
        }

        if (!isAnimationSet())
            setAnimation();

        if (bExiting) 
        {
            if (!bExitAnimationSet) 
            {
                logger.info("setting exit animation..." + exitAnimationName);
                bPlayedOnce = false;
                setExitAnimation();
            }
        } 
        else if (bPlayedOnce && isAnimationSet() && !bCycleAnimationSet) 
        {
            logger.info("setting cycle animation..." + cycleAnimationName);
            setCycleAnimation();
        }

        // Allow an exit
        if ((bPlayedOnce && (ActionState.isExitRepeatWithGoSit(context)))) 
        {
            logger.info("bExiting1 : true");
            bExiting = true;
        }

        // Check for possible transitions
        if (bExitAnimationSet && bPlayedOnce && !context.isTransitioning()) {
            logger.info("transition checking...");
            transitionCheck();
        }
    }

    public void notifyAnimationMessage(AnimationMessageType message) {
        notifyAnimationMessage(message, "");
    }

    /**
     * {@inheritDoc InputClient}
     */
    @Override
    public void notifyAnimationMessage(AnimationMessageType message, String messageString) {
        //for stopping this sate
        if (message == AnimationMessageType.PlayOnceComplete) 
        {
            logger.info("---PlayOnceComplete---" + animationName);
            bPlayedOnce = true;
            //play the repeat animations only
            if (backupAnimations != null && backupAnimations.size() > 0) {
                List<String> animsToPlay = getLastRepeatAnim();
                if (animsToPlay != null) {
                    cycleAnimationName = animsToPlay.get(1);
                }
            }
        } else if (message == AnimationMessageType.EndOfCycle) {
            // the animation has finished
            // so check if wink then shutdown it
            // remove the animation from backup list
            // notify the listener that this anim is finished
            tempBlock = true;
            if (winkScheduledService != null) {
                winkScheduledService.shutdown();
                winkScheduledService = null;
            }
            backupAnimations.remove(animationName);
            logger.info("---EndOfCycle---" + animationName);
            for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this, animationName, true, "");
            }
            bExiting = true;
            tempBlock = false;
        } else if (message == AnimationMessageType.EndOfCycleWithoutExitAnim) {
            // same as above but don't play exit anim
            tempBlock = true;
            backupAnimations.remove(animationName);
            logger.info("---EndOfCycleWithoutExitAnim---" + animationName);
            for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this, animationName, true, "");
            }
            bExiting = true;
            bExitAnimationSet = true;
            tempBlock = false;
        } else if (message == AnimationMessageType.Restart) {
            // restart this state to plat animation
            // so notify that all other animation are finished
            tempBlock = true;
            // call method of listener
            for (String anim : backupAnimations.keySet()) {
                for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                    lis.changeInState(this, anim, true, "");
                }
            }
            backupAnimations.remove(animationName);
            logger.info("---Restart---" + animationName);
            logger.warning("anims.size-Restart : " + backupAnimations.size());
            bPlayedOnce = false;
            bExiting = false;
            bCycleAnimationSet = false;
            bExitAnimationSet = false;
            tempBlock = false;
        } else if (message == AnimationMessageType.RestartAndSave) {
            // restart this state but keep current anim in backup
            // used for combine gestures
            tempBlock = true;
            logger.info("---RestartAndSave---" + animationName);
            logger.info("anims.size-RestartAndSave : " + backupAnimations.size());
            bPlayedOnce = false;
            bExiting = false;
            bCycleAnimationSet = false;
            bExitAnimationSet = false;
            tempBlock = false;
        } else if (message == AnimationMessageType.ExitAnimation) {
            // exit anim and play the backup animation
            // used in combining gesture
            logger.info("---Exit1---" + messageString);
            tempBlock = true;

            String[] splitArray = messageString.split("\\|");
            messageString = splitArray[0];
            String playExit = "";
            String idle = "";
            if (splitArray.length == 3) {
                playExit = splitArray[2];
            }
            if (splitArray.length >= 2) {
                idle = splitArray[1];
            }

            SkeletonNode skeleton = gameContext.getSkeleton();
            if (playExit.equals("true")) {
                skeleton.getAnimationState().setTransitionDuration(exitTransitionDuration);
                skeleton.getAnimationState().setAnimationSpeed(exitAnimationSpeed);
                skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.PlayOnce);
                skeleton.transitionTo(messageString, true);
            }
            backupAnimations.remove(messageString);

            List<String> animsToPlay = getLastRepeatAnim();
            if (animsToPlay == null) {
                animsToPlay = getLastAnim();
            }

            //bPlayedOnce = false;
            bExiting = false;
            bCycleAnimationSet = false;
            bExitAnimationSet = false;
            if (animsToPlay != null) {
                prevAnimName = animsToPlay.get(0);
                animationName = animsToPlay.get(0);
                cycleAnimationName = animsToPlay.get(1);
                exitAnimationName = animsToPlay.get(2);
                bExitAnimationReverse = Boolean.parseBoolean(animsToPlay.get(3));
            }

            if (cycleAnimationName.contains("Wink")) {
                bCycleAnimationSet = true;
            } else {
                if (winkScheduledService != null) {
                    winkScheduledService.shutdown();
                    winkScheduledService = null;
                }
            }

            timer = 100;
            tempBlock = false;

            // call method of listener
            for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this, messageString, true, "");
            }

        }
    }

    private List<String> getLastRepeatAnim() {
        List<String> animsToPlay = null;
        for (int i = 1; i <= backupAnimations.size(); i++) {
            animsToPlay = (List<String>) backupAnimations.values().toArray()[backupAnimations.size() - i];
            if (animsToPlay.get(0).equals(animsToPlay.get(1))) {
                return animsToPlay;
            }
        }
        return null;
    }

    private List<String> getLastAnim() {
        if (backupAnimations == null || backupAnimations.isEmpty()) {
            return null;
        }
        return (List<String>) backupAnimations.values().toArray()[backupAnimations.size() - 1];
    }

    private void setIdleAnimation(String idleAnimName) {
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null)
        {
            skeleton.getAnimationState().setTransitionDuration(cycleTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(cycleAnimationSpeed);
            skeleton.getAnimationState().setReverseAnimation(false);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.PlayOnce);
            skeleton.transitionTo(idleAnimName, false);
            setAnimationSetBoolean(true);
        }
    }

    private void setCycleAnimation() {
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null) {
            skeleton.getAnimationState().setTransitionDuration(cycleTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(cycleAnimationSpeed);
            skeleton.getAnimationState().setReverseAnimation(false);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.Loop);
            if (animationName.equals("Male_Wink") || animationName.equals("Female_Wink")) {
                winkScheduledService = Executors.newSingleThreadScheduledExecutor();
                winkScheduledService.scheduleWithFixedDelay(new WinkRunnable(), 1, 2, TimeUnit.SECONDS);
                bCycleAnimationSet = true;
            } else {
                bCycleAnimationSet = skeleton.transitionTo(cycleAnimationName, false);
            }
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
            if (animationName.equals("Male_Wink") || animationName.equals("Female_Wink")) {
                CharacterEyes eyes = getContext().getCharacter().getEyes();
                eyes.wink(true);
                bExitAnimationSet = true;
                playOnceCompletedForWink();
            } else {
                bExitAnimationSet = skeleton.transitionTo(exitAnimationName, bExitAnimationReverse);
            }
            setAnimationSetBoolean(true);
        }
    }

    private class WinkRunnable implements Runnable {

        public void run() {
            CharacterEyes eyes = getContext().getCharacter().getEyes();
            eyes.wink(true);
        }

    }

    public void onKeyPressed() {
        if (bKeyPressed) {
            return;
        }
        if (context.isGesturePlayingInSitting()) {
            if (context.getCharacter().getCharacterParams().isMale()) {
                setExitAnimationName("Male_Sitting");
            } else {
                setExitAnimationName("Female_Sitting");
            }
        } else {
            if (context.getCharacter().getCharacterParams().isMale()) {
                setExitAnimationName("Male_Idle");
            } else {
                setExitAnimationName("Female_Idle");
            }
        }
        bKeyPressed = true;
        bExiting = true;
        bPlayedOnce = true;
        setExitAnimation();
        //if you want the walking movement after cycle action exits
        //also comment the above setExitAnimation() method
        //transitionCheck();
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
        //Do this otherwise the reverse animation won't be added in backupAnimation list
        this.prevAnimName = "";
    }

    public HashMap<String, List<String>> getBackupAnimations() {
        return backupAnimations;
    }
}
