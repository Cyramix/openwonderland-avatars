/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
package imi.character.statemachine;

import java.util.LinkedList;

/**
 *
* @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class GameStateChangeListenerRegisterar {

    static final LinkedList<GameStateChangeListener> registeredListeners = new LinkedList<GameStateChangeListener>();

    public static void registerListener(GameStateChangeListener toAdd) {
        registeredListeners.add(toAdd);
    }

    public static void deRegisterListener(GameStateChangeListener toAdd) {
        registeredListeners.remove(toAdd);
    }

    public static void deRegisterListenerAll() {
        registeredListeners.clear();
    }

    public static LinkedList<GameStateChangeListener> getRegisteredListeners() {
        //return clone of linkedlist otherwise it might throw concurrent modification error
        return (LinkedList<GameStateChangeListener>) registeredListeners.clone();
    }

}
