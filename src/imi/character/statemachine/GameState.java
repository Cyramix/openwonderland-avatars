/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
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
package imi.character.statemachine;

import imi.character.CharacterEyes;
import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import imi.scene.SkeletonNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds game state for a character, it is used to drive its animations
 * and game logic.
 * @author Shawn Kendall
 * @author Lou Hayt
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class GameState
{
    /** Reference to the context owning this state **/
    protected GameContext gameContext = null;
    /** The stack of transitions **/
    private final Stack<TransitionObject> transitionStack = new Stack<TransitionObject>();

    //boolean bReverseTransitionCheckTraversal = false; // TODO
    /** Name of the animation associated with this state **/
    protected String    animationName       = null;
    /** Default playback speed for any animations used by this state **/
    protected float     animationSpeed      = 1.0f;
    /** Length of time to transition **/
    private float       transitionDuration  = 0.2f;
    private boolean     bTransitionReverseAnimation = false;
    /** The cycle mode to be applied after transion **/
    private AnimationComponent.PlaybackMode cycleMode = AnimationComponent.PlaybackMode.Loop;
    /** Indicates when the appropriate animation has been set **/
    private boolean bAnimationSet = false;
    
    /** The facial animation will play when entering the state if not null **/
    private String facialAnimationName    = null;
    private float  facialAnimationTransitionTime  = 0.3f;
    private float  facialAnimationExpressionHoldTime = 2.5f;

    protected String name = "nameless";
    protected boolean isNamed = false;
    protected boolean enabledState = true;

    /** Logger convenience method **/
    protected final static Logger logger = Logger.getLogger(GameState.class.getName());

    /**
     * Holds a floating point modifier to be applied on an action (indexed in a GameCotnext)
     * as a result of an input trigger.
     */
    public static class Action
    {
        /** type identifier **/
        public int   action;
        /** support for analog input **/
        public float modifier;

        /**
         * Construct a new instance with all data specified
         * @param action   - the action identifier
         * @param modifier - the floating point modifier value
         */
        public Action(int action, float modifier)
        {
            this.action   = action;
            this.modifier = modifier;
        }
    }
        
    /**
     * Maps game triggers to an action with a modifier
     */
    protected Hashtable<Integer, Action> actionMap = new Hashtable<Integer, Action>();
        
    /** Creates a new instance of GameState */
    public GameState(GameContext context)
    {
        gameContext = context;
        initActionMap();
    }
    
    /**
     * Override this method to initialize the state's action map.
     * By default the call will be forwarded to the context to initialize
     * with the context's default action map.
     */
    protected void initActionMap()
    {
        gameContext.initDefaultActionMap(actionMap);
    }

    /**
     * Retrieve the action associated with the provided trigger.
     * @param trigger The trigger type in question
     * @return The associated action
     */
    public Action getAction(int trigger)
    {
        return actionMap.get(trigger);
    }

    /**
     * Called at the end of every update, checks for every possible transition.
     * (checks for the exit conditions)
     */
    protected void transitionCheck()
    {
        Iterator it = transitionStack.iterator();
        
        while(it.hasNext())
        {
            TransitionObject trans = (TransitionObject)it.next();
            
            if (trans.transition(this))
                return;
        }
    }
    
    /**
     * Use this to explicitly initiate a possible transition 
     * @param transitionObjClass
     * @return true if the transition was validated
     */
    protected boolean transition(Class transitionObjClass)
    {
        Iterator it = transitionStack.iterator();
        
        while(it.hasNext())
        {
            TransitionObject trans = (TransitionObject)it.next();
            if (trans.getClass().equals(transitionObjClass)) // should this be == ?, should I use hashCode?
            {
                return trans.transition(this);
            }
        }
        return false;
    }
    
    /**
     * Add a transition on top of the stack
     * @param transition
     */
    public void addTransition(TransitionObject transition)
    {
        transitionStack.add(transition);
    }
    
    /**
     * Transitions to the states animation and sets the speed and 
     * transition duration.
     * If the animation is successfully set then bAnimationSet will be true
     */
    public void setAnimation() 
    {   
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null)
        {
            skeleton.getAnimationState().setTransitionDuration(transitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(animationSpeed);
            skeleton.getAnimationState().setTransitionCycleMode(cycleMode);
            if (animationName != null && animationName.equals("Male_Wink")) {
                CharacterEyes eyes = getContext().getCharacter().getEyes();
                eyes.wink(true);
                bAnimationSet = true;
            } else if (animationName != null && animationName.equals("Female_Wink")) {
                CharacterEyes eyes = getContext().getCharacter().getEyes();
                eyes.wink(true);
                bAnimationSet = true;
            } else {
                bAnimationSet = skeleton.transitionTo(animationName, bTransitionReverseAnimation);
            }
        }
    }
    
    /**
     * Perform any shutting down that needs to be done.
     * @param owner The context that has this state
     */
    protected void stateExit(GameContext owner)
    {
        // Debugging / Dianostic output
        if (logger.isLoggable(Level.FINE))
            logger.fine(getName() + " Exit, animation:"+getAnimationName());
        
        // call method of listener
        for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
            lis.exitfromState(this);
        }
    }

    /**
     * Perform any needed initialization
     * @param owner The context owning this state
     */
    protected void stateEnter(GameContext owner)
    {
        // Debugging / Diagnostic Output
        if (logger.isLoggable(Level.FINE))
            logger.fine(getName() + " Enter, animation:"+getAnimationName());
        
//        if (gameContext.getController().getWindow() != null)
//            gameContext.getController().getWindow().setTitle(getName() + " Reverse: " + bReverseAnimation + " " + getAnimationName());
        
        if (facialAnimationName != null)
             gameContext.getCharacter().initiateFacialAnimation(facialAnimationName, facialAnimationTransitionTime, facialAnimationExpressionHoldTime);
        
        bAnimationSet = false;

        // Transition to the animation
        setAnimation();
      
        // call listener's method
        for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
            lis.enterInState(this);
        }
    }
 
    @Override
    public String toString()
    {
        if ( isNamed )
            return name;
        else
            return super.toString();
    }

    /**
     * If the animation is not set it will be set now
     * @param deltaTime
     */
    public void update(float deltaTime)
    {
        // Set animation
        if (!bAnimationSet)
            setAnimation();
    }

    /**
     * Get the name of the animation
     * @return
     */
    public String getAnimationName() {
        return animationName;
    }

    /**
     * Set the animation name
     * @param animationName
     */
    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    /**
     * Get the animation speed
     * @return
     */
    public float getAnimationSpeed() {
        return animationSpeed;
    }

    /**
     * Set the animation speed
     * @param animationSpeed
     */
    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    /**
     * Check if the animation is set
     * @return
     */
    public boolean isAnimationSet() {
        return bAnimationSet;
    }

    /**
     * Confirm that the animation has been set
     * @param bAnimationSet
     */
    public void setAnimationSetBoolean(boolean bAnimationSet) {
        this.bAnimationSet = bAnimationSet;
    }

    /**
     * Get the game context
     * @return
     */
    public GameContext getContext()
    {
        return gameContext;
    }

    /**
     * Get the animation transition duration
     * @return
     */
    public float getTransitionDuration() {
        return transitionDuration;
    }

    /**
     * Set the animation transition duration
     * @param transitionDuration
     */
    public void setTransitionDuration(float transitionDuration) {
        this.transitionDuration = transitionDuration;
    }

    /**
     * Check is transitioning with a reversed animation
     * @return
     */
    public boolean isTransitionReverseAnimation() {
        return bTransitionReverseAnimation;
    }

    /**
     * Set if to transition with a reversed animation
     * @param bTransitionReverseAnimation
     */
    public void setTransitionReverseAnimation(boolean bTransitionReverseAnimation) {
        this.bTransitionReverseAnimation = bTransitionReverseAnimation;
    }

    /**
     * Notify an animation event
     * @param message
     */
    public void notifyAnimationMessage(AnimationMessageType message) {
    }
    
    /**
     * Notify an animation event with message
     *
     * @param message
     * @param messageString
     */
    public void notifyAnimationMessage(AnimationMessageType message, String messageString) {
    }

    /**
     * Get the playback mode
     * @return
     */
    public PlaybackMode getCycleMode() {
        return cycleMode;
    }

    /**
     * Set the playback mode
     * @param cycleMode
     */
    public void setCycleMode(PlaybackMode cycleMode) {
        this.cycleMode = cycleMode;
    }
    
    /**
     * The animation will play when entering the state if not null.
     * @param facialAnimationName
     */
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

    /**
     * The time it takes to reach the facial pose
     * The animation will play when entering the state if the name 
     * is not null.
     * @return
     */
    public float getFacialAnimationTransitionTime() {
        return facialAnimationTransitionTime;
    }

    /**
     * The time it takes to reach the facial pose
     * The animation will play when entering the state if the name 
     * is not null.
     * @return
     */
    public void setFacialAnimationTransitionTime(float facialAnimationTransition) {
        this.facialAnimationTransitionTime = facialAnimationTransition;
    }

    /**
     * The time it takes to relax the facial pose
     * The animation will play when entering the state if the name 
     * is not null.
     * @return
     */
    public float getFacialAnimationExpressionHoldTime() {
        return facialAnimationExpressionHoldTime;
    }

    /**
     * The time it takes to relax the facial pose
     * The animation will play when entering the state if the name 
     * is not null.
     * @return
     */
    public void setFacialAnimationExpressionHoldTime(float facialAnimationTime) {
        this.facialAnimationExpressionHoldTime = facialAnimationTime;
    }

    /**
     * Set the name of this game state
     * @param name_
     */
    public void setName(String name_)
    {
        name = name_;
        isNamed = true;
    }

    /**
     * Get the name of this state or if not named get super.toString()
     * @return
     */
    public String getName()
    {
        if ( isNamed )
            return name;
        else
            return super.toString();
    }

    /**
     * Set enabled to true
     */
    public void start() {
        enabledState = true;
    }

    /**
     * Set enabled to false
     */
    public void stop() {
        enabledState = false;
    }

    /**
     * Set the enabled state
     * @param state
     */
    public void setEnable( boolean state ) {
        enabledState = state;
    }

    /**
     * Check if enabled
     * @return
     */
    public boolean isEnabled() {
        return enabledState;
    }
}
