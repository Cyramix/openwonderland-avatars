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

import imi.character.CharacterController;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.statemachine.GameState.Action;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import imi.input.InputState;
import imi.scene.SkeletonNode;
import java.util.HashSet;

/**
 * A character's context contains states and a registry of entry points (validation
 * methods) for its states. Each state has its own list of possible transitions, a
 * transition attempt is always validated at the entry point.
 * @author Shawn Kendall
 * @author Lou Hayt
 */
public class GameContext
{
    private boolean initalized = false;
    /** The character owning this context **/
    private imi.character.Character character = null;
    /** The currently active state **/
    protected GameState currentState = null;
    /** Map of game state types to instances of that type **/
    protected final HashMap<Class, GameState> gameStates = new HashMap<Class, GameState>();
    /** Map of entry point method names to gamestate instances **/
    protected final HashMap<String, GameState> m_registry = new HashMap<String, GameState>();
    
    /**
     * This is a collection of all actions known to this context and their analog state
     */
    protected float []        actions         = null;
    
    /**
     * Holds the current state of triggers for random reference
     */
    protected InputState      m_triggerState   = new InputState();

    /** List of parties interested in the status of this context **/
    private final HashSet<GameContextListener> listeners = new HashSet();
    
    protected String name = "nameless";
    protected boolean isNamed = false;
    protected boolean enabledState = true;

    private static final Logger logger = Logger.getLogger(GameContext.class.getName());

    /** Creates a new instance of GameContext */
    public GameContext(imi.character.Character master)
    {
        character = master;
    }

    /**
     * This method will attempt a state transition,
     * if the transition object contains a context transition
     * then the character's excecuteContextTransition will be called instead.
     * @param transition
     * @return true if the transition is succesfully validated
     */
    public boolean  excecuteTransition(TransitionObject transition)
    {
        if (transition.getContextMessageName() != null) // If a context switch is needed
            return character.executeContextTransition(transition);
        
        GameState state = m_registry.get(transition.getStateMessageName());
        if (state == null) // No state found matching the provided name
            throw new RuntimeException("statemachine transition attempt failed, no state was found with the given name.");

        // Grab a reference to the appropriate transition method
        Class stateClass = state.getClass();
        Method method = null;
        try {
            method = stateClass.getMethod(transition.getStateMessageName(), Object.class);
        } catch (NoSuchMethodException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if (method == null)
            throw new RuntimeException("statemachine transition attempt failed, the validation method was not found in the given state.");
        
        Object bool = null;
        try {
            bool = method.invoke(state, transition.getStateMessageArgs());
        } catch (IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        if (bool instanceof Boolean)
        {
            if ( ((Boolean)bool).booleanValue() )
            {
                // Transition validated! Switch states
                setCurrentState(state);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * A state may register multiple entry points
     * @param state         -   the state to register
     * @param methodName    -   the entry point validation method of the state
     */
    public void  registerStateEntryPoint(GameState state, String methodName)
    {
        m_registry.put(methodName, state);
    }

    /**
     * Get the hash map that maps state types to state instances
     * @return
     */
    public HashMap<Class, GameState> getStateMapping() {
        return gameStates;
    }

    /**
     * Get a state instance
     * @param stateClass
     * @return
     */
    public GameState getState(Class stateClass) {
        return gameStates.get(stateClass);
    }

    /**
     * Get the character that is associated with this context
     * @return
     */
    public imi.character.Character getCharacter(){
        return character;
    }

    /**
     * Sends the provided message to the currentState (if that state is non-null)
     * @param message
     * @param stateID - AnimationState ID, currently 0 for body, 1 for face
     */
    public void notifyAnimationMessage(AnimationMessageType message, int stateID) {
        if (currentState != null && stateID == 0)
            currentState.notifyAnimationMessage(message);
    }

    /**
     * Exit current state and enter the one who's type is provided if it is found
     * @param type
     */
    public void setCurrentStateByType(Class type)
    {
        GameState state = getState(type);
        if (state == null)
            return;
        setCurrentState(getState(type));
    }
    
    /**
     * Exit current state and enter the one provided if it is not null
     * @param newState
     */
    public void setCurrentState(GameState newState)
    {
        if (currentState == newState)
            return;
        if (currentState != null)
            currentState.stateExit(this);
        currentState = newState;
        if (currentState != null)
            currentState.stateEnter(this);
    }

    /**
     * Get the current game state
     * @return
     */
    public GameState getCurrentState()
    {
        return currentState;
    }

    /**
     * If enabled make sure initialize was called and update the current state
     * @param deltaTime
     */
    public void update(float deltaTime)
    {
        if (!enabledState)
            return;
        if (!initalized)
            initialize();
        
        if (currentState != null && currentState.enabledState)
            currentState.update(deltaTime);
    }

    /**
     * Initialized if succesfully transitioned into the current state's animation
     */
    public void initialize()
    {
        // Skeleton might be null until loaded
        SkeletonNode skeleton = getSkeleton();
        if (skeleton != null)
        {
            // Set first animation
            initalized = skeleton.transitionTo(currentState.getAnimationName(), false);
        }
    }

    /**
     * Inform the context of a new trigger event
     * @param trigger The trigger type
     */
    public void triggerPressed(int trigger)
    {
        if (actions == null)
            return;
           
        Action action = currentState.getAction(trigger);
        if (action == null)
        {
            if (!m_triggerState.isKeyPressed(trigger)) {
                triggerAlert(trigger, true);
                notifyTrigger(true, trigger);
            }
            m_triggerState.keyPressed(trigger);
            return;
        }

        if (actions.length-1 >= action.action)
        {
            if (!m_triggerState.isKeyPressed(trigger))
            {
                actions[action.action] += action.modifier;
                triggerAlert(trigger, true);
                notifyTrigger(true, trigger);
            }
            m_triggerState.keyPressed(trigger);
        }
    }

    /**
     * Inform the context of a new trigger event
     * @param trigger The trigger type
     */
    public void triggerReleased(int trigger)
    {
        if (actions == null)
            return;

        Action action = currentState.getAction(trigger);
        if (action == null)
        {
            if (m_triggerState.isKeyPressed(trigger)) {
                triggerAlert(trigger, false);
                notifyTrigger(false, trigger);
            }
            m_triggerState.keyReleased(trigger);
            return;
        }

        if (actions.length-1 >= action.action)
        {
            if (m_triggerState.isKeyPressed(trigger))
            {
                actions[action.action] -= action.modifier;
                triggerAlert(trigger, false);
                notifyTrigger(false, trigger);
            }
            m_triggerState.keyReleased(trigger);
        }

    }

    /**
     * Inform the context of a new trigger event
     * @param pressed
     * @param trigger The trigger type
     */
    private void notifyTrigger(boolean pressed, int trigger) {
        if (listeners==null)
            return;

        CharacterController controller = getController();
        
        synchronized(listeners) {
            for(GameContextListener l : listeners)
                l.trigger(pressed, trigger, controller.getPosition(), controller.getQuaternion());
        }
    }
    
    /**
     * Add a GameContextListener to this character.
     * @param listener to be added
     */
    public void addGameContextListener(GameContextListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove the GameContextListener from the set of listeners for this
     * character. If the listener was not registered previously this method
     * simply returns.
     * @param listener to be removed
     */
    public void removeGameContextListener(GameContextListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
        
    }
    
    /**
     * Removes all GameContextListeners for this character. 
     */
    public void removeAllGameContextListeners() {
        synchronized(listeners) {
            listeners.clear();
        }
        
    }

    /**
     * Override this to hook up centext wide reactions to triggers
     * change in state.
     * 
     * @param trigger - the trigger that changed state
     * @param pressed - true if pressed, false if released
     */
    protected void triggerAlert(int trigger, boolean pressed)
    {
    }
    
    /**
     * Override to define a default action map for states under this context
     * @param actionMap
     */
    public void initDefaultActionMap(Hashtable<Integer, Action> actionMap) 
    {   
    }

    /**
     * Reset all analog actions values to 0.0f and clear the states of triggers.
     */
    public void resetTriggersAndActions() 
    {
        for (int i = 0; i < actions.length; i++)
            actions[i] = 0.0f;
        
        m_triggerState.clear();
        //System.out.println("actions and triggers cleared");
    }

    /**
     * Get the array of actions containing the analog value of each action.
     * @return
     */
    public float [] getActions()
    {
        return actions;
    }

    /**
     * Get the current state of all triggers
     * @return
     */
    public InputState getTriggerState()
    {
        return m_triggerState;
    }

    /**
     * Get the character's skeleton
     * @return
     */
    public SkeletonNode getSkeleton() 
    {
        return character.getSkeleton();
    }

    /**
     * Check if the character is currently transitioning
     * @return
     */
    public boolean isTransitioning()
    {
        return character.isTransitioning();
    }
    
    /**
     * Override to return concerte controller
     * @return
     */
    public CharacterController getController() {
        return null;
    }
    
    /**
     * Override to return concerte behavior
     * @return
     */
    public CharacterBehaviorManager getBehaviorManager() {
        return null;
    }

    /**
     * Instantiate and return a controller, TODO should be abstract
     * @return
     */
    protected CharacterController instantiateController() {
        return null;
    }

    /**
     * Set the name of this context
     * @param name_
     */
    public void setName(String name_)
    {
        name = name_;
        isNamed = true;
    }

    /**
     * Return the name of this context or if it is not named super.toString()
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
