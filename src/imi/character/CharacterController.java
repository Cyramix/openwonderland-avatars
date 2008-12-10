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
package imi.character;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.util.HashSet;
import javax.swing.JFrame;

/**
 * NinjaController contains most concerte code at this point
 * 
 * @author Lou Hayt
 */
public class CharacterController 
{
    protected boolean  bReverseHeading     = false;
    private PMatrix previousOrientation = new PMatrix();
    private Vector3f previousTranslation = new Vector3f();
    

    private HashSet<CharacterMotionListener> listeners = null;
    
    public void stop(){}
    
    public Vector3f getPosition() {
        return null;
    }
    
    public Vector3f getRightVector() {
        return null;
    }
    
    public Vector3f getForwardVector() {
        return null;
    }
    
    public Quaternion getQuaternion() {
        return null;
    }
    
    public boolean isReverseHeading() {
        return bReverseHeading;
    }

    public void setReverseHeading(boolean bReverseHeading) {
        this.bReverseHeading = bReverseHeading;
    }
    
    public JFrame getWindow() {
        return null;
    }

    /**
     * Add a CharacterMotionListener to this character.
     * @param listener to be added
     */
    public void addCharacterMotionListener(CharacterMotionListener listener) {
        if (listeners == null)
            listeners = new HashSet();
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove the CharacterMotionListener from the set of listeners for this
     * character. If the listener was not registered previously this method
     * simply returns.
     * @param listener to be removed
     */
    public void removeCharacterMotionListener(CharacterMotionListener listener) {
        if (listener!=null) {
            synchronized(listeners) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * Notify any motion listeners that the transform has been updated
     * @param location
     * @param orientation
     */
    protected void notifyTransfromUpdate(Vector3f translation, PMatrix orientation) {
        if (listeners==null)
            return;

        if (previousTranslation.equals(translation) &&
                previousOrientation.equals(orientation))
            return;

        synchronized(listeners) {
            for(CharacterMotionListener l : listeners)
                l.transformUpdate(translation, orientation);
        }
        
        previousOrientation.set(orientation);
        previousTranslation.set(translation);
    }
}
