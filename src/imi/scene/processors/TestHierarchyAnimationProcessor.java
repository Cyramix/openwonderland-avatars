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
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.PNode;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.NewFrameCondition;

/**
 * This class was written as a quick test to tweak the transforms of specific
 * nodes. Don't expect much ;)
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class TestHierarchyAnimationProcessor extends ProcessorComponent
{
    
    private PNode m_targetMesh = null;
    //private WorldManager         m_wm         = null;
    
    private float m_angle;
    private Vector3f m_axis;
    private int m_frames = 0;
    
    public TestHierarchyAnimationProcessor(/*WorldManager wm,*/ PNode target, float angle, Vector3f axis)
    {
        //m_wm = wm;
        m_targetMesh    = target;
        m_angle         = angle;
        m_axis          = axis;
        
    }
    
    /**
     * The initialize method
     */
    public void initialize() 
    {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }
    
    /**
     * The Calculate method
     */
    public void compute(ProcessorArmingCollection collection) 
    {
        m_frames++;
        PMatrix rotationMatrix = new PMatrix(m_axis.mult(m_angle), Vector3f.UNIT_XYZ, Vector3f.ZERO);
        
        if (m_targetMesh.getTransform() != null)
        {
            // rotate
            float currentAngle = m_targetMesh.getTransform().getLocalMatrix(false).getRotation().toAngleAxis(m_axis);
            if (m_frames > 240)
            {
                m_angle *= -1.0f;
                m_frames = 0;
            }
            
            PMatrix localMatrix = m_targetMesh.getTransform().getLocalMatrix(true);
            localMatrix.mul(  rotationMatrix  );
            
            m_targetMesh.setDirty(true, true);
        }
    }

    /**
     * The commit method
     */
    public void commit(ProcessorArmingCollection collection) 
    {
        // Updating taken care of on our side
        //m_wm.addToUpdateList(m_targetMesh);
    }
}
