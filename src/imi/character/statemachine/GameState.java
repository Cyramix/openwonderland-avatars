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

import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the base state for the extendable state machine.
 * @author Shawn Kendall
 * @author Lou Hayt
 */
public class GameState extends NamedUpdatableObject
{
    /** Reference to the context owning this state **/
    protected GameContext gameContext = null;
    /** The stack of transitions **/
    private final Stack<TransitionObject> transitionStack = new Stack<TransitionObject>();

    boolean bReverseTransitionCheckTraversal = false; // TODO
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
    
    /** Logger convenience method **/
    protected final static Logger logger = Logger.getLogger(GameState.class.getName());

    /**
     * Represents an action for a state. These are mapped to input triggers
     */
    public static class Action
    {
        /** type identifier **/
        public int   action;
        /** support for analog input **/
        public float modifier;

        /**
         * Construct a new instance with all data specified
         * @param action
         * @param modifier
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
     */
    public void setAnimation() 
    {   
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null)
        {
            skeleton.getAnimationState().setTransitionDuration(transitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(animationSpeed);
            skeleton.getAnimationState().setCycleMode(cycleMode);
            bAnimationSet = skeleton.transitionTo(animationName, bTransitionReverseAnimation);
        }
    }
    
    /**
     * Perform any shutting down that needs to be done.
     * @param owner The context that has this state
     */
    protected void stateExit(GameContext owner)
    {
        // Debugging / Dianostic output
        //System.out.println(getName() + " exit");
        if (logger.isLoggable(Level.FINE))
            logger.fine(getName() + " Exit");
    }

    /**
     * Perform any needed initialization
     * @param owner The context owning this state
     */
    protected void stateEnter(GameContext owner)
    {
        // Debugging / Diagnostic Output
        if (logger.isLoggable(Level.FINE))
            logger.fine(getName() + " Enter");
        
//        if (gameContext.getController().getWindow() != null)
//            gameContext.getController().getWindow().setTitle(getName() + " Reverse: " + bReverseAnimation + " " + getAnimationName());
        
        if (facialAnimationName != null)
             gameContext.getCharacter().initiateFacialAnimation(facialAnimationName, facialAnimationTransitionTime, facialAnimationExpressionHoldTime);
        
        bAnimationSet = false;
        
        // Transition to the animation
        setAnimation();
    }
 
    @Override
    public String toString()
    {
        if ( isNamed )
            return objectName;
        else
            return super.toString();
    }
    
    @Override
    public void update(float deltaTime)
    {
        // Set animation
        if (!bAnimationSet)
            setAnimation();
    }

    public String getAnimationName() {
        return animationName;
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public boolean isReverseTransitionCheckTraversal() {
        return bReverseTransitionCheckTraversal;
    }

    public void setReverseTransitionCheckTraversal(boolean bReverseTransitionCheckTraversal) {
        this.bReverseTransitionCheckTraversal = bReverseTransitionCheckTraversal;
    }

    public boolean isAnimationSet() {
        return bAnimationSet;
    }

    public void setAnimationSetBoolean(boolean bAnimationSet) {
        this.bAnimationSet = bAnimationSet;
    }

    public GameContext getContext()
    {
        return gameContext;
    }

    public float getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(float transitionDuration) {
        this.transitionDuration = transitionDuration;
    }

    public boolean isTransitionReverseAnimation() {
        return bTransitionReverseAnimation;
    }

    public void setTransitionReverseAnimation(boolean bTransitionReverseAnimation) {
        this.bTransitionReverseAnimation = bTransitionReverseAnimation;
    }

    public void notifyAnimationMessage(AnimationMessageType message) {
    }

    public PlaybackMode getCycleMode() {
        return cycleMode;
    }

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
}
