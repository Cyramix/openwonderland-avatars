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
package imi.character;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 * @author Shawn Kendall
 */
public class VerletArm 
{
    PPolygonModelInstance modelInst = null;
    SkinnedMeshJoint shoulderJoint  = null;
    
    ArrayList<VerletParticle>  particles    = new ArrayList<VerletParticle>();
    ArrayList<StickConstraint> constraints  = new ArrayList<StickConstraint>();
    
    Vector3f gravity = new Vector3f(0.0f, -9.8f, 0.0f);
    
    public VerletArm(SkinnedMeshJoint shoulder, PPolygonModelInstance modelInstance) 
    {
        modelInst     = modelInstance;
        shoulderJoint = shoulder;
        
        // Lets make an arm
        Vector3f shoulderPosition = shoulderJoint.getTransform().getWorldMatrix(false).getTranslation();
        particles.add(new VerletParticle(shoulderPosition));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.246216f))));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.4852301f))));
        constraints.add(new StickConstraint(0, 1, 0.246216f));
        constraints.add(new StickConstraint(1, 2, 0.2390151f));
    }

    public void update(float physicsUpdateTime) 
    {
        // Attach the first particle to the shoulder joint
        particles.get(0).position(shoulderJoint.getTransform().getWorldMatrix(false).getTranslation());
        
	// Verlet integration step
	for (int i = 1; i < particles.size(); i++)
	{
            particles.get(i).setForceAccumulator(gravity);
            particles.get(i).verletIntegration(physicsUpdateTime);
	}
        
	// Solving constraints by relaxation
	for(int i = 0; i < 3; i++)
            satisfyConstraints(); 
    }
    
    public void satisfyConstraints()
    {
        for (StickConstraint constraint : constraints)
        {
            Vector3f vPoint1		= particles.get(constraint.getParticle1()).getCurrentPosition();
            Vector3f vPoint2		= particles.get(constraint.getParticle2()).getCurrentPosition();
            Vector3f vDelta = new Vector3f(vPoint1.subtract(vPoint2));

            float fDeltaLength          =	vDelta.length();
            float fDiff			=	(fDeltaLength - (constraint.getRestDistance())) / fDeltaLength;

            particles.get(constraint.getParticle1()).moveCurrentPosition(	vDelta.mult(-0.5f * fDiff)	);
            particles.get(constraint.getParticle2()).moveCurrentPosition(	vDelta.mult( 0.5f * fDiff)	);
        }
    }

    public ArrayList<StickConstraint> getConstraints() {
        return constraints;
    }

    public ArrayList<VerletParticle> getParticles() {
        return particles;
    }

    public PMatrix getInverseModelWorldMatrix()
    {
        return modelInst.getTransform().getWorldMatrix(false).inverse();
    }
    
    public Vector3f getElbowPosition() 
    {
        return particles.get(1).getCurrentPosition();
    }

    public Vector3f getWristPosition() {
        return particles.get(2).getCurrentPosition();
    }
    
    //////////////////////////////////////////////////////////////////////////
    
    public class VerletParticle
    {
        private float       mass                = 1.0f;
        private Vector3f    currentPosition     = new Vector3f();
        private Vector3f    previousPosition    = new Vector3f();
        private Vector3f    forceAccumulator    = new Vector3f();
        
        private Vector3f    integrationHelper   = new Vector3f();
        
        public VerletParticle(Vector3f position)
        {
            currentPosition.set(position);
            previousPosition.set(position);
        }
        
        public void verletIntegration(float physicsUpdateTime)
        {
            integrationHelper.set(currentPosition);
            currentPosition.addLocal( (currentPosition.subtract(previousPosition)).add(forceAccumulator.mult(physicsUpdateTime * physicsUpdateTime)) );
            previousPosition.set(integrationHelper);
        }
        
        public Vector3f getVelocity()
        {
            return currentPosition.subtract(previousPosition);
        }
        
        public void stop()
        {
            previousPosition.set(currentPosition);
        }
        
        public void position(Vector3f position)
        {
            Vector3f velocity = getVelocity();
            currentPosition.set(position);
            previousPosition.set(currentPosition.subtract(velocity));
        }
        
        public void dislocate(Vector3f offset)
        {
            currentPosition.addLocal(offset);
            previousPosition.addLocal(offset);
        }
        
        public void dislocate(Vector3f directionNormalized, float distance)
        {
            directionNormalized.mult(distance);
            currentPosition.addLocal(directionNormalized);
            previousPosition.addLocal(directionNormalized);
        }
        
        public void moveCurrentPosition(Vector3f move)
        {
            currentPosition.addLocal(move);
        }
        
        public void movePreviousPosition(Vector3f move)
        {
            previousPosition.addLocal(move);
        }
        
        public Vector3f getCurrentPosition() {
            return currentPosition;
        }

        public void setCurrentPosition(Vector3f currentPosition) {
            this.currentPosition = currentPosition;
        }

        public float getMass() {
            return mass;
        }

        public void setMass(float mass) {
            this.mass = mass;
        }

        public Vector3f getPreviousPosition() {
            return previousPosition;
        }

        public void setPreviousPosition(Vector3f previousPosition) {
            this.previousPosition = previousPosition;
        }

        public Vector3f getForceAccumulator() {
            return forceAccumulator;
        }

        public void setForceAccumulator(Vector3f forceAccumulator) {
            this.forceAccumulator = forceAccumulator;
        }

        public void addToForceAccumulator(Vector3f force) {
            forceAccumulator.addLocal(force);
        }        
        
    }
    
    //////////////////////////////////////////////////////////////////////////
    
    public class StickConstraint
    {
        private int	particle1;
        private int	particle2;
        private float   fRestDistance;

        StickConstraint(int particle1Index, int particle2Index, float restDistance)
        {
            particle1 = particle1Index;
            particle2 = particle2Index;
            fRestDistance = restDistance;
        }

        public float getRestDistance() {
            return fRestDistance;
        }

        public void setRestDistance(float fRestDistance) {
            this.fRestDistance = fRestDistance;
        }

        public int getParticle1() {
            return particle1;
        }

        public void setParticle1(int particle1) {
            this.particle1 = particle1;
        }

        public int getParticle2() {
            return particle2;
        }

        public void setParticle2(int particle2) {
            this.particle2 = particle2;
        }
    }

}
