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
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class AvatarControlScheme extends InputScheme implements AvatarControls
{
    protected Avatar   avatar = null;
    
    protected int currentavatar = 0;
    protected ArrayList<Avatar> avatarTeam = new ArrayList<Avatar>();
    
    protected   InputState      inputState     = new InputState();
    
    private boolean bCommandEntireTeam = false;
    
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private int lastMouseX    = 0;
    private int lastMouseY    = 0;
    private boolean mouseDown = false;

    public AvatarControlScheme(Avatar master)
    {
        super();
        avatar = master;
    }
    
    public void processMouseEvents(Object[] events)
    {
        if (m_jscene == null || avatar == null)
            return;
        
        for (int i=0; i<events.length; i++) 
        {
            if (events[i] instanceof MouseEvent)
            {
                if (avatarTeam.get(currentavatar).getSkeletonManipulator() != null && avatarTeam.get(currentavatar).getSkeletonManipulator().isArmsEnabled())
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

                    VerletArm rightArm = avatarTeam.get(currentavatar).getRightArm();
                    VerletArm leftArm  = avatarTeam.get(currentavatar).getLeftArm();

                    if (rightArm != null)
                    {
                        if (me.getID() == MouseEvent.MOUSE_PRESSED && me.getButton() == MouseEvent.BUTTON2)
                        {
                            avatarTeam.get(currentavatar).getContext().triggerPressed(TriggerNames.ToggleRightArmManualDriveReachMode.ordinal());
                            avatarTeam.get(currentavatar).getContext().triggerReleased(TriggerNames.ToggleRightArmManualDriveReachMode.ordinal());
                        }

                        rightArm.addInputOffset(offset);    
                    }
                    if (leftArm != null)
                    {
                        if (me.getID() == MouseEvent.MOUSE_PRESSED && me.getButton() == MouseEvent.BUTTON2)
                        {
                            avatarTeam.get(currentavatar).getContext().triggerPressed(TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal());
                            avatarTeam.get(currentavatar).getContext().triggerReleased(TriggerNames.ToggleLeftArmManualDriveReachMode.ordinal());
                        }

                        leftArm.addInputOffset(offset);       
                    }
                }
                
                // Picking (under construction)
                if (avatar != null)
                {
                    MouseEvent me = (MouseEvent) events[i];
                    if (me.getID() == MouseEvent.MOUSE_PRESSED && me.getButton() == MouseEvent.BUTTON3)
                    {
                        FlexibleCameraProcessor fcp = (FlexibleCameraProcessor)avatar.getWorldManager().getUserData(FlexibleCameraProcessor.class);
                        //System.out.println("Right Click: mouse cursor ray is shot into the virtual space");
                    }
                }
            }
        }
    }
    
    public Avatar getCurrentlySelectedAvatar()
    {
        return avatarTeam.get(currentavatar);
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
    
    protected void processKeyEvent(KeyEvent ke)
    {
        if (ke.getID() == KeyEvent.KEY_RELEASED) 
        {
            // Alter the input state for random reference
            inputState.keyReleased(ke.getKeyCode());
            
            // Affect character actions
            if(bCommandEntireTeam)
            {
                for (int i = 0; i < avatarTeam.size(); i++)
                {
                    avatarTeam.get(i).keyReleased(ke.getKeyCode());
                }
            }
            else
                avatar.keyReleased(ke.getKeyCode());
        }
        
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            // Alter the input state for random reference
            inputState.keyPressed(ke.getKeyCode());
            
            // Affect character actions
            if(bCommandEntireTeam)
            {
                for (int i = 0; i < avatarTeam.size(); i++)
                {
                    avatarTeam.get(i).keyPressed(ke.getKeyCode());
                }
            }
            else
                avatar.keyPressed(ke.getKeyCode());
            
        }
    }

    public ArrayList<Avatar> getAvatarTeam() {
        return avatarTeam;
    }
    
    public void setAvatar(Avatar avatarMaster)
    {
        avatar = avatarMaster;
    }

    public boolean isCommandEntireTeam() {
        return bCommandEntireTeam;
    }

    public void setCommandEntireTeam(boolean bCommandEntireTeam) {
        this.bCommandEntireTeam = bCommandEntireTeam;
    }
    
  
}
