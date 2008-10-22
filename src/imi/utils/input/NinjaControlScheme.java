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

import imi.character.ninja.Ninja;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author Lou Hayt
 */
public class NinjaControlScheme extends InputScheme
{
    private boolean m_bSkeletonMode = false;
  
    private Ninja   ninja = null;
    
    private int currentNinja = 0;
    private ArrayList<Ninja> ninjaTeam = new ArrayList<Ninja>();
    
    private   InputState      inputState     = new InputState();
    
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
        }
    }
       
    private void processKeyEvent(KeyEvent ke) 
    {           
        if (ke.getID() == KeyEvent.KEY_RELEASED) 
        {
            // Alter the input state for random reference
            inputState.keyReleased(ke.getKeyCode());
            
            // Affect character actions
            ninja.keyReleased(ke.getKeyCode());
        }
        
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            // Alter the input state for random reference
            inputState.keyPressed(ke.getKeyCode());
            
            // Affect character actions
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
                if (m_bSkeletonMode == false)
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
                m_bSkeletonMode = !m_bSkeletonMode;

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
}
