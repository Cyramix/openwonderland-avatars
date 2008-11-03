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
package imi.character.statemachine;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shawn Kendall
 * @author Lou Hayt
 */
public class GameState extends NamedUpdatableObject
{
    protected GameContext gameContext = null;
    private Stack<TransitionObject> transitionStack = new Stack<TransitionObject>();
    boolean bReverseTransitionCheckTraversal = false;
    
    protected String animationName = null;
    protected float animationSpeed = 1.0f;
    private boolean bReverseAnimation = false;
    private float transitionDuration = 0.2f;
    
    private boolean bAnimationSet = false;
    
    protected Logger logger = Logger.getLogger(GameState.class.getName());
        
    public static class Action
    {
        public int   action;
        public float modifier;
        
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
     * the context's default action map.
     */
    protected void initActionMap()
    {
        gameContext.initDefaultActionMap(actionMap);
    }
    
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
            if (trans.getClass().equals(transitionObjClass)) // should this be == ?, shuold I use hashCode?
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
            bAnimationSet = skeleton.transitionTo(animationName);
            
            // Set reverse   
            if (bReverseAnimation)
                skeleton.getAnimationState().setReverseAnimation(true);
            else
                skeleton.getAnimationState().setReverseAnimation(false);
        }
    }
    
    /**
     * Perform any shutting down that needs to be done.
     * @param owner The context that has this state
     */
    protected void stateExit(GameContext owner)
    {
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
//        System.out.println(getName() + " enter");
        if (logger.isLoggable(Level.FINE))
            logger.fine(getName() + " Enter");
        
        if (gameContext.getController().getWindow() != null)
            gameContext.getController().getWindow().setTitle(getName() + " Reverse: " + bReverseAnimation + " " + getAnimationName());
        
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
        // This method is derived from interface cosmic.Updatable
        
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

    public boolean isReverseAnimation() {
        return bReverseAnimation;
    }

    public void setReverseAnimation(boolean reverseAnimation) {
        if(reverseAnimation != bReverseAnimation)
            bAnimationSet = false;
        bReverseAnimation = reverseAnimation;
    }
    
    
}
