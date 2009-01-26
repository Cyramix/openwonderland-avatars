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
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModel;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.utils.PModelUtils;
import java.awt.event.KeyEvent;

/**
 *
 * @author Lou Hayt
 */
public class DemoSchemeSigraph extends InputScheme
{
    PScene  m_pscene      = null;
    PNode   m_tableModel  = null;
    PNode   m_chairsModel = null;
    
    int numberOfStools = 8;
    
    int driverID = -1; // -1 to drive the table
    
    public DemoSchemeSigraph(PNode tableModel, PNode chairsModel, PScene pscene)
    {
        m_pscene      = pscene;
        m_tableModel  = tableModel;
        m_chairsModel = chairsModel;
    }
            
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
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            if (m_tableModel == null || m_tableModel.getChildrenCount() == 0 || !(m_tableModel.getChild(0) instanceof PPolygonMeshInstance))
                return;
            if (m_chairsModel == null || m_chairsModel.getChildrenCount() == 0 || !(m_chairsModel.getChild(0) instanceof PPolygonMeshInstance))
                return;

            PPolygonMeshInstance table = (PPolygonMeshInstance)m_tableModel.getChild(0);
    
            PPolygonMeshInstance driver = null;
            
            if (driverID < -1)
                driverID = numberOfStools-1;
            if (driverID >= numberOfStools)
                driverID = -1;
            
            if (driverID == -1)
                driver = table;
            else if (driverID+1 <= numberOfStools)
                driver = (PPolygonMeshInstance)m_chairsModel.getChild(driverID);
                
            Vector3f pos = new Vector3f(driver.getTransform().getLocalMatrix(false).getTranslation());
            float scalar = 0.5f;
            
            if (ke.getKeyCode() == KeyEvent.VK_UP) 
                driver.getTransform().getLocalMatrix(true).setTranslation(pos.add(Vector3f.UNIT_Z.mult(scalar)));
            
            else if (ke.getKeyCode() == KeyEvent.VK_DOWN) 
                driver.getTransform().getLocalMatrix(true).setTranslation(pos.add(Vector3f.UNIT_Z.mult(-scalar)));
            
            else if (ke.getKeyCode() == KeyEvent.VK_LEFT) 
                driver.getTransform().getLocalMatrix(true).setTranslation(pos.add(Vector3f.UNIT_X.mult(scalar)));
            
            else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) 
                driver.getTransform().getLocalMatrix(true).setTranslation(pos.add(Vector3f.UNIT_X.mult(-scalar)));
         
            else if (ke.getKeyCode() == KeyEvent.VK_ENTER)
            {
                driverID = -1;
                resetStools();
            }
            
            else if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP)
            {
                driverID--;
                if (driverID < -1)
                    driverID = numberOfStools-1;
            }
            
            else if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
            {
                driverID++;
                if (driverID >= numberOfStools)
                    driverID = -1;
            }
            
            else if (ke.getKeyCode() == KeyEvent.VK_0)
            {
                numberOfStools = 0;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_1)
            {
                numberOfStools = 1;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_2)
            {
                numberOfStools = 2;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_3)
            {
                numberOfStools = 3;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_4)
            {
                numberOfStools = 4;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_5)
            {
                numberOfStools = 5;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_6)
            {
                numberOfStools = 6;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_7)
            {
                numberOfStools = 7;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_8)
            {
                numberOfStools = 8;
                resetStools();
            }
            else if (ke.getKeyCode() == KeyEvent.VK_9)
            {
                numberOfStools = 9;
                resetStools();
            }
        }
    }
    
    public void resetStools()
    {
        PPolygonMeshInstance table = (PPolygonMeshInstance)m_tableModel.getChild(0);
            
        // remove chairs
        m_pscene.removeModelInstance(m_chairsModel);
        // add chairs
        if (numberOfStools != 0)
        {
            PPolygonModel chairs = PModelUtils.createChairsAroundATable("BusinessStools", 15, 1, 3, 4, numberOfStools);
            chairs.setMaterial(table.getMaterialCopy(), 1);
            m_chairsModel = m_pscene.addModelInstance(chairs, table.getTransform().getWorldMatrix(false));
        }
    }

    @Override
    public void processMouseEvents(Object[] events) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
