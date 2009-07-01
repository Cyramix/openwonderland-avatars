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

import imi.collision.CollisionListener;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.collision.PickingResults;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.util.HashSet;
import javax.swing.JFrame;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The base class for controlling character behaviors at a "state machine"
 * level.
 * 
 * @author Lou Hayt
 */
public abstract class CharacterController implements CollisionListener
{
    /** True to use 'reversed' headings **/
    protected boolean  bReverseHeading     = true;
    /** Cache the last orientation **/
    private final PMatrix previousOrientation = new PMatrix();
    /** Cache the last translation **/
    private final Vector3f previousTranslation = new Vector3f();
    /** List of listeners **/
    private final HashSet<CharacterMotionListener> listeners = new HashSet<CharacterMotionListener>();

    /**
     * Override to implement stopping functionality.
     * <p>This implementation does nothing.<p>
     */
    public void stop(){}

    /**
     * Override to return the character's current position.
     * <p>This implementation does nothing.<p>
     * @return null
     */
    public Vector3f getPosition() {
        return null;
    }

    /**
     * Override to return the character's right vector.
     * <p>This implementation does nothing.<p>
     * @return null
     */
    public Vector3f getRightVector() {
        return null;
    }

    /**
     * Override to return the character's forward vector.
     * <p>This implementation does nothing.<p>
     * @return null
     */
    public Vector3f getForwardVector() {
        return null;
    }

    /**
     * Override to return the character's rotational transform info as a
     * quaternion.
     * <p>This implementation does nothing.<p>
     * @return null
     */
    public Quaternion getQuaternion() {
        return null;
    }

    /**
     * Determine if the heading is being interpreted as it's reverse.
     * @return True if reversed
     */
    public boolean isReverseHeading() {
        return bReverseHeading;
    }

    /**
     * This method will cause the heading to be interpreted in reverse if set.
     * @param bReverseHeading True to reverse interpretation.
     */
    public void setReverseHeading(boolean bReverseHeading) {
        this.bReverseHeading = bReverseHeading;
    }

    /**
     * Add a CharacterMotionListener to this character.
     * @param listener A non-null listener to be added
     * @throws IllegalStateException If {@code listener == null}
     * @see CharacterMotionListener
     */
    public void addCharacterMotionListener(CharacterMotionListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null listener provided!");
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove the CharacterMotionListener from the set of listeners for this
     * character.
     * @param listener A non-null listener to be removed
     * @throws IllegalArgumentException if {@code listener == null}
     */
    public void removeCharacterMotionListener(CharacterMotionListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null listsner provided!");
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Notify any motion listeners that the transform has been updated.
     * @param translation A non-null position vector
     * @param orientation A non-null orientation matrix
     */
    public void notifyTransfromUpdate(Vector3f translation, PMatrix orientation)
    {
        if (listeners.size() == 0) // Any listeners?
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

    /**
     * Implement to provide access to the model being manipulated.
     * @return The model instance
     */
    abstract public PPolygonModelInstance getModelInstance();

    /**
     * Implement this to allow updating of the target model instance.
     * @param newModelInstance
     */
    abstract public void setModelInstance(PPolygonModelInstance newModelInstance);
    
    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @param acc
     */
    public void accelerate(Vector3f acc) {
    }


    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @param scalar
     */
    public void accelerate(float scalar) {
    }

    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @return 0.0f
     */
    public float getVelocityScalar() {
        return 0.0f;
    }

    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @return false
     */
    public boolean isMovingForward() {
        return false;
    }

    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @param direction
     */
    public void turnTo(Vector3f direction) {
    }

    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @return 0.0f
     */
    public float getForwardAcceleration() {
        return 0.0f;
    }

    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @param max
     */
    public void setMaxAcceleration(float max) {
    }

    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @param max
     */
    public void setMaxVelocity(float max) {
    } 
    
    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @param projection
     */
    public void colliding(Vector3f projection) {
    }

    /**
     * Override point.
     * <p>This implementation does nothing.</p>
     * @param source
     * @param messageData
     * @param origin
     * @param direction
     * @param results
     */
    public void picked(Class source, Object messageData, Vector3f origin, Vector3f direction, PickingResults results) {
    }
}
