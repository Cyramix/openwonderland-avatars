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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.utils.input;

import com.jme.math.Vector3f;
import imi.character.ninja.Ninja;
import imi.character.objects.ObjectCollection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class NinjaControlScheme extends InputScheme
{
    private boolean bSkeletonMode = false;
  
    private Ninja   ninja = null;
    
    private int currentNinja = 0;
    private ArrayList<Ninja> ninjaTeam = new ArrayList<Ninja>();
    
    private   InputState      inputState     = new InputState();
    
    private boolean bCommandEntireTeam = false;
    private ObjectCollection objects = null;
    
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private int lastMouseX    = 0;
    private int lastMouseY    = 0;
    private boolean mouseDown = false;
    
    
    
    public NinjaControlScheme(Ninja master)
    {
        super();
        ninja = master;
    }
    
    @Override
    public void processEvents(Object[] events) 
    {
        if (m_jscene == null || ninja == null)
            return;
        
        for (int i=0; i<events.length; i++) 
        {
            if (events[i] instanceof KeyEvent) 
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
            
            if (events[i] instanceof MouseEvent)
            {
                if (ninjaTeam.get(currentNinja).getArm() != null &&  ninjaTeam.get(currentNinja).getArm().isEnabled())
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
                        offset.x = deltaX * -0.025f;
                        offset.z = deltaY * -0.025f;
                        
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
                                        
                    ninjaTeam.get(currentNinja).getArm().addInputOffset(offset);
                }
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
                for (int i = 0; i < ninjaTeam.size(); i++)
                {
                    ninjaTeam.get(i).keyReleased(ke.getKeyCode());
                }
            }
            else
                ninja.keyReleased(ke.getKeyCode());
        }
        
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            // Alter the input state for random reference
            inputState.keyPressed(ke.getKeyCode());
            
            // Affect character actions
            if(bCommandEntireTeam)
            {
                for (int i = 0; i < ninjaTeam.size(); i++)
                {
                    ninjaTeam.get(i).keyPressed(ke.getKeyCode());
                }
            }
            else
                ninja.keyPressed(ke.getKeyCode());
            
            /////////////////////////////////////////////////////////////
            
            // Next Ninja
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP) 
            {
                if (!ninjaTeam.isEmpty())
                {
                    currentNinja++;
                    if (currentNinja > ninjaTeam.size()-1)
                        currentNinja = 0;
                    else if (currentNinja < 0)
                        currentNinja = ninjaTeam.size()-1;
                    
                    ninja = ninjaTeam.get(currentNinja);
                    ninja.selectForInput();
                }
            }
            
            // Previouse Ninja
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN) 
            {
                if (!ninjaTeam.isEmpty())
                {
                    currentNinja--;
                    if (currentNinja > ninjaTeam.size()-1)
                        currentNinja = 0;
                    else if (currentNinja < 0)
                        currentNinja = ninjaTeam.size()-1;
                    
                    ninja = ninjaTeam.get(currentNinja);
                    ninja.selectForInput();
                }
            }
            
            // Remove a chair from the object collection
            if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) 
            {
                if(objects != null)
                {
                    objects.removeRandomChair();
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
                m_jscene.toggleRenderPRendererMesh();
                //m_jscene.loadShaders();
            }
            
            if (ke.getKeyCode() == KeyEvent.VK_U)
            {
                if (bSkeletonMode == false)
                {
                    m_jscene.setRenderInternallyBool(false);
                    // turn the prenderer on, turn off the mesh drawing, turn on jme wireframe
                    // This will create the PRenderer if it does not exist
                    m_jscene.setRenderPRendererMesh(false);
                    m_jscene.setRenderBool(true);
                    m_jscene.setRenderInternallyBool(true);
                    m_jscene.setRenderBothBool(true);
                    m_jscene.setWireframe(true);
                }
                else
                {
                    // reset to solid jme only
                    m_jscene.setRenderPRendererMesh(true);
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

    public ArrayList<Ninja> getNinjaTeam() {
        return ninjaTeam;
    }
    
    public void setNinja(Ninja ninjaMaster)
    {
        ninja = ninjaMaster;
    }

    public boolean isCommandEntireTeam() {
        return bCommandEntireTeam;
    }

    public void setCommandEntireTeam(boolean bCommandEntireTeam) {
        this.bCommandEntireTeam = bCommandEntireTeam;
    }
    
    public void setObjectCollection(ObjectCollection objectCollection) {
        objects = objectCollection;
    }
    
}
