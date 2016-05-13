/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
package imi.character.statemachine;

/**
 *
* @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public interface GameStateChangeListener {

    public void enterInState(GameState state);

    public void exitfromState(GameState state);

    public void changeInState(GameState state, String animName, boolean animFinished, String animType);

}
