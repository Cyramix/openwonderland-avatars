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
import imi.character.objects.ObjectCollection;
import imi.character.objects.ObjectCollectionBase;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.tests.AvatarInspectionDemo;
import imi.tests.DemoBase;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class AvatarControlScheme extends InputScheme implements AvatarControls
{
    private boolean bSkeletonMode = false;
  
    private Avatar   avatar = null;
    
    private int currentavatar = 0;
    private ArrayList<Avatar> avatarTeam = new ArrayList<Avatar>();
    
    private   InputState      inputState     = new InputState();
    
    private boolean bCommandEntireTeam = false;
    private ObjectCollectionBase objects = null;
    
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
    
    private void processKeyEvent(KeyEvent ke) 
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
            
            /////////////////////////////////////////////////////////////
            
            // Next avatar
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP)
            {   
                if (!avatarTeam.isEmpty())
                {
                    currentavatar++;
                    if (currentavatar > avatarTeam.size()-1)
                        currentavatar = 0;
                    else if (currentavatar < 0)
                        currentavatar = avatarTeam.size()-1;
                    
                    avatar = avatarTeam.get(currentavatar);
                    avatar.selectForInput();
                    avatar.initiateFacialAnimation("MaleSmile", 0.25f, 3.0f);
                }
            }
            
            // Previouse avatar
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
            {
                if (!avatarTeam.isEmpty())
                {
                    currentavatar--;
                    if (currentavatar > avatarTeam.size()-1)
                        currentavatar = 0;
                    else if (currentavatar < 0)
                        currentavatar = avatarTeam.size()-1;
                    
                    avatar = avatarTeam.get(currentavatar);
                    avatar.selectForInput();
                    avatar.initiateFacialAnimation("MaleSmile", 0.25f, 3.0f);
                }
            }
            
            // Remove a chair from the object collection
            if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) 
            {
                if(objects != null && objects instanceof ObjectCollection)
                {
                    ((ObjectCollection)objects).removeAChair();
                }
            }

            if (ke.getKeyCode() == KeyEvent.VK_N)
            {
                WorldManager wm = DemoBase.getWM();
                FlexibleCameraProcessor p = (FlexibleCameraProcessor) wm.getUserData(FlexibleCameraProcessor.class);
                if (p != null)
                    p.takeSnap();
            }

            // Pop up the console \ chat
            if (ke.getKeyCode() == KeyEvent.VK_BACK_SLASH || ke.getKeyCode() == KeyEvent.VK_BACK_QUOTE) 
            {
                if(objects != null && objects instanceof ObjectCollection)
                {
                    ((ObjectCollection)objects).getGUI().show();
                    //objects.addRandomChair();
                }
            }
            
            // Note: input only affects this JScene
            
            // JMonkey Wireframe (on\off)
            if (ke.getKeyCode() == KeyEvent.VK_T) 
                m_jscene.toggleWireframe();
            
            // JMonkey Lights (on\off)
            if (ke.getKeyCode() == KeyEvent.VK_L) 
                m_jscene.toggleLights();
            
            // Rendering mode (JMonkey, JMonkey and PRenderer, PRenderer)
            if (ke.getKeyCode() == KeyEvent.VK_R) 
                m_jscene.renderToggle();
    
            // PRenderer Bounding volumes (off, box, sphere)
            if (ke.getKeyCode() == KeyEvent.VK_B) 
                m_jscene.toggleRenderBoundingVolume();
            
            // Toggle PRenderer mesh display
            if (ke.getKeyCode() == KeyEvent.VK_M)
            {
                //m_jscene.toggleRenderPRendererMesh();
                //m_jscene.loadShaders();
            }
            
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
    
    public void setObjectCollection(ObjectCollectionBase objectCollection) {
        objects = objectCollection;
    }
    
}
