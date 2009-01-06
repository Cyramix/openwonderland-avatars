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

import imi.gui.PNodePropertyPanel;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

/**
 *
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class DefaultScheme extends InputScheme
{
    private boolean m_bSkeletonMode = false;
  
    @Override
    public void processEvents(Object[] events) 
    {
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
        if (m_jscene == null)
            return;
        
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
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
            
            // PRenderer Polygon normals (on\off)
            if (ke.getKeyCode() == KeyEvent.VK_P) 
                m_jscene.toggleRenderPolygonNormals();
            
            // PRenderer Vertex normals (on\off)
            if (ke.getKeyCode() == KeyEvent.VK_V) 
                m_jscene.toggleRenderVertexNormals();
            
            // PRenderer Polygon center points (on\off)
            if (ke.getKeyCode() == KeyEvent.VK_C) 
                m_jscene.toggleRenderPolygonCenters();
            
            // PRenderer Bounding volumes (off, box, sphere)
            if (ke.getKeyCode() == KeyEvent.VK_B) 
                m_jscene.toggleRenderBoundingVolume();
            
            // Flip normals
            if (ke.getKeyCode() == KeyEvent.VK_F) 
                m_jscene.flipNormals();
            
            // Smooth normals toggle
            if (ke.getKeyCode() == KeyEvent.VK_N) 
                m_jscene.toggleSmoothNormals();
            
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
                        
            if (ke.getKeyCode() == KeyEvent.VK_3)
            {
                PPolygonSkinnedMeshInstance ninja = ((PPolygonSkinnedMeshInstance)(m_jscene.getPScene().getInstances().getChild(0).getChild(0)));
                PNodePropertyPanel jointWidget = null;//new PNodePropertyPanel(ninja.getTransformHierarchy().getChild(0).findChild("Joint11"));
                jointWidget.setVisible(true);
                // make and show a new JFrame
                JFrame frame = new JFrame();
                
                frame.add(jointWidget);
                frame.setSize(new Dimension(350, 400));
                
                frame.setVisible(true);
                
            
            }
        }
    }
    
}
