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
package imi.camera;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/**
 * The base class for all CameraStates. This class provides an API for determining
 * whether a state should use the right, left, or shift-lock mouse looking modes.
 * @author Ronald E Dahlgren
 */
public abstract class AbstractCameraState
{
    /**
     * This enumeration is used to specify which type of state
     * a
     */
    enum CameraStateType
    {
        AzimuthAngle,
        FirstPerson,
        TumbleObject,
        ThirdPerson,
        Chase,
        Default
    }

    private boolean rightMouseButtonLook  = false;
    private boolean middleMouseButtonLook = false;
    private boolean shiftPressedLook      = false;

    /** What type are we? **/
    private final CameraStateType m_type;

    /**
     * Construct a new instance of the provided {@code CameraStateType}
     * @param type
     */
    AbstractCameraState(CameraStateType type) {
        m_type = type;
    }


    /**
     * This method tells the state to respond to the right mouse button for
     * looking.
     *
     * <p>It is the responsibility for subclasses of {@code AbstractCameraState}
     * to honor this contract.</p>
     *
     * @param rightMouseButtonLook
     */
    public void setRightMouseButtonLook(boolean rightMouseButtonLook) {
        this.rightMouseButtonLook = rightMouseButtonLook;
    }

    /**
     * This method tells the state to respond to the middle mouse button for
     * looking.
     *
     * <p>It is the responsibility for subclasses of {@code AbstractCameraState}
     * to honor this contract.</p>
     *
     * @param middleMouseButtonLook
     */
    public void setMiddleMouseButtonLook(boolean middleMouseButtonLook) {
        this.middleMouseButtonLook = middleMouseButtonLook;
    }

    /**
     * This method tells the state to respond to mouse looking only if the shift
     * key is pressed.
     *
     * <p>It is the responsibility for subclasses of {@code AbstractCameraState}
     * to honor this contract.</p>
     *
     * @param shiftPressedLook
     */
    public void setShiftPressedLook(boolean shiftPressedLook) {
        this.shiftPressedLook = shiftPressedLook;
    }

    /**
     * This method determines if the state should respond to mouse looking based
     * on whether the conditions are right.
     *
     * <p>Specifically, if things such as {@code setMiddleMouseButtonLook(true) }
     * have been called, then those conditions must be true for this method to
     * return {@code true}.
     * @param me The MouseEvent to query
     * @param event The AWT MouseEvent event type enumeration
     * @return True if the state should proceed with mouse look behavior
     */
    boolean isLook(MouseEvent me, int event)
    {
        boolean result = true; // default to looking
        if (me.getID() != event)
            result = false;
        else if (shiftPressedLook && !me.isShiftDown())
            result = false;
        else if (rightMouseButtonLook && SwingUtilities.isRightMouseButton(me))
            result = true;
        else if (middleMouseButtonLook && SwingUtilities.isMiddleMouseButton(me))
            result = true;
        else if (rightMouseButtonLook || middleMouseButtonLook)
            result = false;
        return result;
    }
    
    /**
     * Retrieve the type of this AbstractCameraState
     * @return The type
     */
    CameraStateType getType()
    {
        return m_type;
    }
    
    /**
     * Sets the camera position. A state may choose to ignore the parameter if 
     * it does not make sense for the particular model.
     * @param position
     */
    abstract void setCameraPosition(Vector3f position);

    /**
     * Set the camera's transform
     * @param transform
     */
    abstract void setCameraTransform(PMatrix transform);

    /**
     * Retrieve the camera's transform. If a backing PMatrix is used, a reference
     * may be safely returned
     * @return
     */
    abstract PMatrix getCameraTransform();
}
