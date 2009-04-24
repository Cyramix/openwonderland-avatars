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
package imi.utils.input;

import com.jme.math.Vector3f;
import imi.character.VerletArm;
import imi.character.avatar.Avatar;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.scene.processors.FlexibleCameraProcessor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Ronald E Dahlgren
 */
public class DahlgrensInput extends InputScheme
{
    private WorldManager wm = null;
    private boolean bSkeletonMode = false;
    private Avatar   avatar = null;

    private   InputState      inputState     = new InputState();

    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private int lastMouseX    = 0;
    private int lastMouseY    = 0;

    private boolean mouseDown = false;
    /** Doesn't need to be runtime dynamic, just cleans the code up a bit **/
    private final Map<Integer, Method> keyHandler = new HashMap<Integer, Method>();

    public DahlgrensInput()
    {
    }
    public DahlgrensInput(Avatar target)
    {
        super();
        avatar = target;
    }

    public void setWM(WorldManager wm)
    {
        this.wm = wm;
    }

    public void getMouseEventsFromCamera()
    {
        // Get the hacked mouse events that the camera is stealing from us
        if (avatar != null)
            ((FlexibleCameraProcessor)avatar.getWorldManager().getUserData(FlexibleCameraProcessor.class)).setControl(this);
    }

    public void processMouseEvents(Object[] events)
    {
        if (m_jscene == null || avatar == null)
            return;

        for (int i=0; i<events.length; i++)
        {
            if (events[i] instanceof MouseEvent)
            {
                if (avatar.getSkeletonManipulator() != null && avatar.getSkeletonManipulator().isArmsEnabled())
                {
                    Vector3f offset = new Vector3f();

                    MouseEvent me = (MouseEvent) events[i];
                    if (me.getID() == MouseEvent.MOUSE_PRESSED && me.getButton() == MouseEvent.BUTTON3)
                    {
                        // Mouse pressed, reset initial settings
                        currentMouseX = me.getX();
                        currentMouseY = me.getY();
                        lastMouseX    = me.getX();
                        lastMouseY    = me.getY();
                        mouseDown = !mouseDown;
                    }

                    if (mouseDown)//me.getID() == MouseEvent.MOUSE_DRAGGED)
                    {
                        // Set the current
                        currentMouseX = me.getX();
                        currentMouseY = me.getY();

                        // Calculate delta
                        int deltaX = currentMouseX - lastMouseX;
                        int deltaY = currentMouseY - lastMouseY;

                        // Translate to input offset
                        offset.x = deltaX * -0.0075f;
                        offset.z = deltaY * -0.0075f;

                        // Set the last
                        lastMouseX    = me.getX();
                        lastMouseY    = me.getY();
                    }

                    if (me.getID() == MouseEvent.MOUSE_WHEEL)
                    {
                        if (me instanceof MouseWheelEvent)
                        {
                            int scroll = ((MouseWheelEvent)me).getWheelRotation();
                            offset.y   = scroll * -0.05f;
                        }
                    }

                    VerletArm rightArm = avatar.getRightArm();
                    VerletArm leftArm  = avatar.getLeftArm();

                    if (rightArm != null)
                    {
                        if (me.getID() == MouseEvent.MOUSE_PRESSED && me.getButton() == MouseEvent.BUTTON2)
                        {
                            avatar.getContext().triggerPressed(TriggerNames.ToggleRightArmManualDriveReachMode.ordinal());
                            avatar.getContext().triggerReleased(TriggerNames.ToggleRightArmManualDriveReachMode.ordinal());
                        }

                        rightArm.addInputOffset(offset);
                    }
                    if (leftArm != null)
                    {
                        if (me.getID() == MouseEvent.MOUSE_PRESSED && me.getButton() == MouseEvent.BUTTON2)
                        {
                            avatar.getContext().triggerPressed(TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal());
                            avatar.getContext().triggerReleased(TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal());
                        }

                        leftArm.addInputOffset(offset);
                    }
                }
            }
        }
    }

    public void activateMouseMovement()
    {
        mouseDown = true;
    }

    @Override
    public void processEvents(Object[] events)
    {
        if (m_jscene == null || avatar == null)
            return;

        for (int i=0; i<events.length; i++)
        {
            if (events[i] instanceof KeyEvent)
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
    }

    private void processKeyEvent(KeyEvent ke)
    {
        if (ke.getID() == KeyEvent.KEY_RELEASED)
        {
            // Alter the input state for random reference
            inputState.keyReleased(ke.getKeyCode());
            avatar.keyReleased(ke.getKeyCode());
        }

        if (ke.getID() == KeyEvent.KEY_PRESSED)
        {
            // Alter the input state for random reference
            inputState.keyPressed(ke.getKeyCode());
            avatar.keyPressed(ke.getKeyCode());

            Method keyMethod = keyHandler.get(ke.getID());
            if (keyMethod != null)
            {
                try {
                    keyMethod.invoke(this, (Object[])null);
                }
                catch (Exception ex)
                {

                }
            }

            /////////////////////////////////////////////
            /////// "Default" key bindings //////////////
            /////////////////////////////////////////////
            // Rendering mode (JMonkey, JMonkey and PRenderer, PRenderer)
            if (ke.getKeyCode() == KeyEvent.VK_R)
                m_jscene.renderToggle();

            // PRenderer Bounding volumes (off, box, sphere)
            if (ke.getKeyCode() == KeyEvent.VK_B)
                m_jscene.toggleRenderBoundingVolume();

            // Toggle PRenderer mesh display
//            if (ke.getKeyCode() == KeyEvent.VK_M)
//                m_jscene.toggleRenderPRendererMesh();

            if (ke.getKeyCode() == KeyEvent.VK_U)
            {
                if (bSkeletonMode == false)
                {
                    m_jscene.setRenderInternallyBool(false);
                    // turn the prenderer on, turn off the mesh drawing, turn on jme wireframe
                    // This will create the PRenderer if it does not exist
                    //m_jscene.setRenderPRendererMesh(false);
                    m_jscene.setRenderBool(true);
                    m_jscene.setRenderInternallyBool(true);
                    m_jscene.setRenderBothBool(true);
                    m_jscene.setWireframe(true);
                }
                else
                {
                    // reset to solid jme only
                    //m_jscene.setRenderPRendererMesh(true);
                    m_jscene.setRenderBool(true);
                    m_jscene.setRenderInternallyBool(false);
                    m_jscene.setRenderBothBool(false);
                    m_jscene.setWireframe(false);

                }
                // toggle
                bSkeletonMode = !bSkeletonMode;
            }

        }
    }

    public void setTargetAvatar(Avatar avatar)
    {
        this.avatar = avatar;
    }
}
