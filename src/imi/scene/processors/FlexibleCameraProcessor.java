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
package imi.scene.processors;

import com.jme.scene.Node;
import imi.scene.PMatrix;
import imi.scene.camera.behaviors.CameraModel;
import imi.scene.camera.behaviors.WrongStateTypeException;
import imi.scene.camera.state.CameraState;
import imi.tests.SkyBox;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.AwtEventCondition;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;

/**
 *
 * @author Ronald E Dahlgren
 */
public class FlexibleCameraProcessor extends AWTEventProcessorComponent
{
    private PMatrix m_transform = new PMatrix();
    
    private Node    m_jmeNode   = null;
    private Node    m_skyNode   = null;
    
    private WorldManager m_WM   = null;
    
    private ProcessorArmingCollection m_armingConditions = null;
    
    // camera behavior specifics
    private CameraState m_state = null;
    private CameraModel m_model = null;
    
    /**
     * The default constructor
     */
    public FlexibleCameraProcessor(AWTInputComponent listener, Node cameraNode,
            WorldManager wm, Entity myEntity, SkyBox skyboxNode) {
        
        super(listener);
        setEntity(myEntity);
        
        m_jmeNode = cameraNode;
        m_WM = wm;
        
        m_skyNode = skyboxNode;
        
        m_armingConditions = new ProcessorArmingCollection(this);
        m_armingConditions.addCondition(new AwtEventCondition(this));
        m_armingConditions.addCondition(new NewFrameCondition(this));
    }
    
    public void setCameraBehavior(CameraModel newModel, CameraState newState)
    {
        m_model = newModel;
        m_state = newState;
    }

    public CameraModel getModel()
    {
        return m_model;
    }
    
    public CameraState getState()
    {
        return m_state;
    }
    
    public void setState(CameraState stateToMatchModel)
    {
        m_state = stateToMatchModel;
    }

    public PMatrix getTransform()
    {
        return m_transform;
    }
    
    @Override
    public void compute(ProcessorArmingCollection arg0)
    {
        //float fCurrentTime = System.currentTimeMillis() / 1000.0f;
        float delta = 0.00356f;//fCurrentTime - m_lastFrameTime;
        if (delta > 1.0f)
            delta = 0.0f;

        if (m_model != null && m_state != null)
        {
            try
            {
                m_model.update(m_state, delta);
                m_model.handleInputEvents(m_state, getEvents());
                m_model.determineTransform(m_state, m_transform);
            } catch (WrongStateTypeException ex)
            {
                Logger.getLogger(FlexibleCameraProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //m_lastFrameTime = fCurrentTime;
    }

    @Override
    public void commit(ProcessorArmingCollection arg0)
    {
        m_jmeNode.setLocalRotation(m_transform.getRotation());
        m_jmeNode.setLocalTranslation(m_transform.getTranslation());
        
        if (m_skyNode != null)
        {
            m_skyNode.setLocalTranslation(m_transform.getTranslation());
            m_WM.addToUpdateList(m_skyNode);
        }
        m_WM.addToUpdateList(m_jmeNode);
    }

    @Override
    public void initialize()
    {
        setArmingCondition(m_armingConditions);
    }
}
