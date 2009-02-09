/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;

/**
 *
 * @author Lou Hayt
 */
public class GameData implements Serializable, ManagedObject
{
    private boolean gameOn = false;
    private int numberOfPlayers = 0;
    
    public GameData()
    {
        
    }

    public boolean isGameOn() {
        return gameOn;
    }

    public void setGameOn(boolean gameOn) {
        this.gameOn = gameOn;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }
    
    public void adjustNumberOfPlayers(int add) {
        this.numberOfPlayers += add;
    }
            
    
}
