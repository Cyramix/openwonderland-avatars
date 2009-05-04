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
public class DemoAvatarControlScheme extends AvatarControlScheme
{
    private boolean bSkeletonMode = false;
    private ObjectCollectionBase objects = null;

    
    public DemoAvatarControlScheme(Avatar master)
    {
        super(master);
    }
    
    public void getMouseEventsFromCamera()
    {
        // Get the hacked mouse events that the camera is stealing from us
        if (avatar != null)
            ((FlexibleCameraProcessor)avatar.getWorldManager().getUserData(FlexibleCameraProcessor.class)).setControl(this);
    }
    


    protected void processKeyEvent(KeyEvent ke)
    {
        super.processKeyEvent(ke);

        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            
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
    
    public void setObjectCollection(ObjectCollectionBase objectCollection) {
        objects = objectCollection;
    }
  }
