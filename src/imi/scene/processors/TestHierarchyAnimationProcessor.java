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
    
    private float m_angle;
    private Vector3f m_axis;
    private int m_frames = 0;

    private PMatrix bindMatrix = null;
    private PMatrix rotationMatrix = null;

    public TestHierarchyAnimationProcessor(PNode target, float angle, Vector3f axis)
    {
        this(target, angle, axis, new PMatrix());
    }

    public TestHierarchyAnimationProcessor(PNode target, float angle, Vector3f axis, PMatrix bindMat)
    {
        //m_wm = wm;
        m_targetMesh    = target;
        m_angle         = angle;
        m_axis          = axis;
        
        bindMatrix = new PMatrix(bindMat);

        rotationMatrix = new PMatrix();
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
        rotationMatrix.mul(new PMatrix(m_axis.mult(m_angle), Vector3f.UNIT_XYZ, Vector3f.ZERO));

        // Simulate the bind pose
        //rotationMatrix.setTranslation(new Vector3f(0, 4, 10));
        if (m_targetMesh.getTransform() != null)
        {
            // rotate
            if (m_frames > 240)
            {
                m_angle *= -1.0f;
                m_frames = 0;
            }
            
            PMatrix localMatrix = m_targetMesh.getTransform().getLocalMatrix(true);
            localMatrix.mul(bindMatrix,  rotationMatrix);
            
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
