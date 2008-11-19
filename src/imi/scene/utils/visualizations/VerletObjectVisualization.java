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
package imi.scene.utils.visualizations;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jme.util.geom.BufferUtils;
import imi.character.VerletArm;
import imi.character.VerletArm.VerletParticle;
import java.nio.FloatBuffer;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * This class manages all the necessary data and functionality for representing
 * a verlet object's particles and constraints.
 * @author Ronald E Dahlgren
 */
class VerletObjectVisualization
{
    /** Root of the object **/
    Node m_objectRoot = null;
    /** The constraint line (single object) **/
    Line m_constraintLine = new Line("ConstraintLine");
    /** Reference to the owning object **/
    VerletArm m_verletObject = null;
    /** Reference to the overall object's position **/
    Vector3f m_objectPosition = null;
    /** Maintain a convenient mapping of all the particle positions to their jME Nodes **/
    FastMap<Vector3f, Sphere> m_particlePositionToNodeMapping = new FastMap<Vector3f, Sphere>();
    /** Maintain some representation of the constraint representation **/
    FastMap<Vector3f, Vector3f> m_constraintMap = new FastMap<Vector3f, Vector3f>();

    /**
     * Construct a new visualization object.
     * @param verletObject
     */
    public VerletObjectVisualization(VerletArm verletObject)
    {
        m_objectRoot = new Node(verletObject.toString());
        m_objectRoot.attachChild(m_constraintLine);
        // configure the line's behavior / appearance
        m_constraintLine.setLineWidth(1.2f);

        // grab reference from the verlet object and map it
        m_objectPosition = new Vector3f();
        m_objectRoot.setLocalTranslation(m_objectPosition);

        m_verletObject = verletObject;
    }

    /**
     * Iterate over each particle in the object and create the mapping entries for it
     */
    public void mapParticles()
    {
        // clear out the old
        m_objectRoot.detachAllChildren();
        m_particlePositionToNodeMapping.clear();

        // parse the new
        int index = 0;
        for (VerletParticle particle : m_verletObject.getParticles())
        {
            // add a new sphere and map it
            Vector3f particlePosition = particle.getCurrentPosition(); // This should actually come from the particle
            Sphere particleSphere = new Sphere("Particle#" + index, particlePosition, 10, 10, 0.05f);
            particleSphere.setDefaultColor(ColorRGBA.cyan);

            // add this particle to the mapping
            m_particlePositionToNodeMapping.put(particlePosition, particleSphere);
            // Attach the sphere to the scene root
            m_objectRoot.attachChild(particleSphere);
        }
        // re-attach our constraint line
        m_objectRoot.attachChild(m_constraintLine);
    }

    /**
     * Iterate of the collection of constraints and add their geometry
     */
    public void mapConstraints()
    {
        // clear out the old
        m_constraintMap.clear();

        // parse the new
        // For each constraint pair
        for (VerletArm.StickConstraint constraint : m_verletObject.getConstraints())
        {
            int indexOne = constraint.getParticle1();
            int indexTwo = constraint.getParticle2();
            // Grab these particles from the object and use their current positions
            m_constraintMap.put(m_verletObject.getParticles().get(indexOne).getCurrentPosition(),
                    m_verletObject.getParticles().get(indexTwo).getCurrentPosition());
        }
    }

    /**
     * Update all the local translation components of the nodes representing the particles
     */
    public void updateParticlePositions()
    {
        // Use a fast map entry to iterate over our map's contents
        for (FastMap.Entry<Vector3f, Sphere> iter = m_particlePositionToNodeMapping.head(),
                                         end = m_particlePositionToNodeMapping.tail();
                 (iter = iter.getNext()) != end;)
             {
                  Vector3f position = iter.getKey();
                  Sphere   jmeNode  = iter.getValue();
                  // Update the position
                  jmeNode.setLocalTranslation(position);
             }
    }

    /**
     * Updates all the constraint objects to fit the current particle positions
     */
    public void updateConstraintVisuals()
    {
        // clear out the old line
        m_constraintLine.clearBuffers();
        FastList<Vector3f> positionBuffer = new FastList<Vector3f>();
        int index = -1;
        // Use a fast map entry to iterate over our map's contents
        for (FastMap.Entry<Vector3f, Vector3f> iter = m_constraintMap.head(),
                                         end = m_constraintMap.tail();
                 (iter = iter.getNext()) != end;)
        {
            // Grab the next two positions
            Vector3f position1 = iter.getKey();
            Vector3f position2 = iter.getValue();
            positionBuffer.add(position1);
            positionBuffer.add(position2);
        }
        // At this point, the index and position buffers are ready for reconstruction
        Vector3f[] positionArray = new Vector3f[positionBuffer.size()];
        FloatBuffer positionFB = null;
        index = 0;
        for (Vector3f vec : positionBuffer)
        {
            positionArray[index] = vec;
            index++;
        }
        positionFB = BufferUtils.createFloatBuffer(positionArray);

        m_constraintLine.reconstruct(positionFB, null, null, null);
        m_constraintLine.setDefaultColor(ColorRGBA.blue);
        m_constraintLine.updateRenderState();
    }

    ////////////////////////////////////////////////////////////////////////////
    /// Equality checking is agnostic of all state except the verlet object  ///
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VerletObjectVisualization other = (VerletObjectVisualization) obj;
        if (this.m_verletObject != other.m_verletObject && (this.m_verletObject == null || !this.m_verletObject.equals(other.m_verletObject))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.m_verletObject != null ? this.m_verletObject.hashCode() : 0);
        return hash;
    }


}
