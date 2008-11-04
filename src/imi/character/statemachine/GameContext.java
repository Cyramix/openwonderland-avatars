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

import imi.character.CharacterController;
import imi.character.statemachine.GameState.Action;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import imi.utils.input.InputState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import java.util.HashSet;

/**
 *
 * @author Shawn Kendall
 * @author Lou Hayt
 */
public class GameContext extends NamedUpdatableObject
{
    private boolean initalized = false;
    private imi.character.Character character = null;
    public GameState currentState = null;
    protected HashMap<Class, GameState> gameStates = new HashMap<Class, GameState>();
    protected HashMap<String, GameState> m_registry = new HashMap<String, GameState>();
    
    /**
     * This is a collection of all actions known to this context and thier analog state
     */
    protected float []        actions         = null;
    
    /**
     * Holds the current state of triggers for random reference
     */
    protected InputState      m_triggerState   = new InputState();

    private HashSet<GameContextListener> listeners = new HashSet();
    
    
    /** Creates a new instance of GameContext */
    public GameContext(imi.character.Character master)
    {
        character = master;
    }

    /**
     * This method will atempt a state transition,
     * if the transition object contains a context transition
     * then the character's excecuteContextTransition will be called instead.
     * @param transition
     * @return true if the transition is succesfully validated
     */
    public boolean  excecuteTransition(TransitionObject transition)
    {
        if (transition.getContextMessageName() != null)
        {
            return character.excecuteContextTransition(transition);
        }
        
        GameState state = m_registry.get(transition.getStateMessageName());
        if (state == null)
            return false;
        
        Class stateClass = state.getClass();
        Method method = null;
        
        try {
            method = stateClass.getMethod(transition.getStateMessageName(), Object.class);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (method != null)
        {
            Object bool = null;
            
            try {
                bool = method.invoke(state, transition.getStateMessgeArgs());
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
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
        }
        
        return false;
    }
    
    /**
     * A state may register multiple entry points
     * @param state         -   the state to register
     * @param methodName    -   the entry point validation method of the state
     */
    public void  RegisterStateEntryPoint(GameState state, String methodName)
    {
        m_registry.put(methodName, state);
    }
    
    public HashMap<Class, GameState> getStates() {
        return gameStates;
    }

    public imi.character.Character getCharacter(){
        return character;
    }
    
    public void setStates(HashMap<Class, GameState> states) {
        this.gameStates = states;
    }
    
    // Set the new state
    public void setCurrentState(GameState newState) // protected
    {
        if (currentState == newState)
            return;
        if (currentState != null)
            currentState.stateExit(this);
        currentState = newState;
        if (currentState != null)
            currentState.stateEnter(this);
    }
    
    public GameState getCurrentState()
    {
        return currentState;
    }
        
    @Override
    public void update(float deltaTime)
    {
        if (!initalized)
            initialize();
        
        //System.out.println("GameContext updating...");
        // This method is derived from interface cosmic.Updatable
        // to do: code goes here
        if (currentState != null && currentState.enabledState)
            currentState.update(deltaTime);
    }
    
    @Override
    public void initialize()
    {
        // Skeleton might be null untill loaded
        SkeletonNode skeleton = getSkeleton();
        if (skeleton != null)
        {
            // Set first animation
            skeleton.transitionTo(currentState.getAnimationName(), false);
            initalized = true;
        }
    }
    
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
                //System.out.println(action.action + " pressed: " + m_actions[action.action]);
                notifyTrigger(true, trigger);
            }
            m_triggerState.keyPressed(trigger);
        }
    }
    
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
                //System.out.println(action.action + " released: " + m_actions[action.action]);
                notifyTrigger(false, trigger);
            }
            m_triggerState.keyReleased(trigger);
        }

    }

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
    
    public void resetTriggersAndActions() 
    {
        for (int i = 0; i < actions.length; i++)
            actions[i] = 0.0f;
        
        m_triggerState.clear();
        //System.out.println("actions and triggers cleared");
    }
    
    public float [] getActions()
    {
        return actions;
    }

    public InputState getTriggerState()
    {
        return m_triggerState;
    }
        
    public PPolygonSkinnedMeshInstance getMesh()
    {
        return character.getMesh();
    }
        
    public SkeletonNode getSkeleton() 
    {
        return character.getSkeleton();
    }
    
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
}
