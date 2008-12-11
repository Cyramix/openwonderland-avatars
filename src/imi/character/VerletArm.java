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
    private PPolygonModelInstance modelInst = null;
    private SkinnedMeshJoint shoulderJoint  = null;
    
    private ArrayList<VerletParticle>  particles    = new ArrayList<VerletParticle>();
    private ArrayList<StickConstraint> constraints  = new ArrayList<StickConstraint>();
    
    private Vector3f gravity = new Vector3f(0.0f, -9.8f * 2.0f, 0.0f);
    //private Vector3f gravity = new Vector3f(0.0f, 0.0f, 0.0f);
    private float velocityDampener = 0.8f;
    private boolean enabled = false;;
    private VerletJointManipulator jointManipulator = null; // old prototype
    private VerletSkeletonFlatteningManipulator skeletonManipulator = null;
    
    private Vector3f currentInputOffset = new Vector3f();
    
    private float maxReach = 0.4834519f + 0.0075f;
    
    private final int shoulder  = 0;
    private final int elbow     = 1;
    private final int wrist     = 2;
    
    private boolean manualDriveReachUp = true; // otherwise reach forward
    
    private Vector3f pointAtLocation = null;
    
    public VerletArm(SkinnedMeshJoint shoulderJ, PPolygonModelInstance modelInstance) 
    {
        modelInst     = modelInstance;
        shoulderJoint = shoulderJ;
        
        // Lets make an arm
        Vector3f shoulderPosition = shoulderJoint.getTransform().getWorldMatrix(false).getTranslation();
        particles.add(new VerletParticle(shoulderPosition, false));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.246216f)), true));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.4852301f)), false));
        constraints.add(new StickConstraint(shoulder, elbow, 0.246216f));
        constraints.add(new StickConstraint(elbow, wrist, 0.2390151f));
    }

    public void update(float physicsUpdateTime) 
    {
        // Attach the first particle to the shoulder joint
        particles.get(shoulder).position(shoulderJoint.getTransform().getWorldMatrix(false).getTranslation());
                
        if (pointAtLocation != null)
            pointAt();
        else
        {
            Vector3f shoulderPosition = particles.get(shoulder).getCurrentPosition();
            Vector3f wristPosition    = particles.get(wrist).getCurrentPosition();
            if (wristPosition.distance(shoulderPosition) > maxReach)
                particles.get(wrist).setMoveable(true);
            else
                particles.get(wrist).setMoveable(false);
                
            // Apply current input offset to the wrist particle
            if (currentInputOffset.x != 0.0f || currentInputOffset.y != 0.0f || currentInputOffset.z != 0.0f)
            {
                if (manualDriveReachUp)
                {
                    float temp = currentInputOffset.z;
                    currentInputOffset.z = currentInputOffset.y;
                    currentInputOffset.y = temp;
                }

                PMatrix modelWorld = modelInst.getTransform().getWorldMatrix(false);
                modelWorld.transformNormal(currentInputOffset);

                // Check if it is ok to move the wrist to the new position
                Vector3f newWristPosition = wristPosition.add(currentInputOffset);
                if (newWristPosition.distance(shoulderPosition) < maxReach)
                {
                    float backReach = 0.0f;

                    Vector3f directionFromShoulderToWrist = newWristPosition.subtract(shoulderPosition).normalize();
                    if (modelWorld.getLocalZ().dot(directionFromShoulderToWrist) > backReach)
                        particles.get(wrist).dislocate(currentInputOffset);
                    else
                        particles.get(wrist).dislocate(directionFromShoulderToWrist.mult(-0.1f).add(modelWorld.getLocalZ().mult(0.1f)));
                }

                currentInputOffset.zero();
            }
        }
        
	// Verlet integration step
	for (int i = 1; i < particles.size(); i++)
	{
            if (particles.get(i).isMoveable())
                particles.get(i).setForceAccumulator(gravity);
            else
                particles.get(i).setForceAccumulator(Vector3f.ZERO);
            particles.get(i).scaleVelocity(velocityDampener);
            particles.get(i).verletIntegration(physicsUpdateTime);
	}
        
	// Solving constraints by relaxation
	for(int i = 0; i < 5; i++)
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

            VerletParticle P1 = particles.get(constraint.getParticle1());
            VerletParticle P2 = particles.get(constraint.getParticle2());
            
            if (!P1.isMoveable())
            {
                if (!P2.isMoveable())
                    return;
                
                P2.moveCurrentPosition(	vDelta.mult(fDiff));    
            }
            else if (!P2.isMoveable())
                P1.moveCurrentPosition(	vDelta.mult(-fDiff));    
            else
            {
                P1.moveCurrentPosition(	vDelta.mult(-0.5f * fDiff) );
                P2.moveCurrentPosition(	vDelta.mult( 0.5f * fDiff) );
            }
        }
    }
    
    public void resetArm()
    {
        pointAtLocation = null;
        
        Vector3f shoulderPosition = shoulderJoint.getTransform().getWorldMatrix(false).getTranslation();
        particles.get(shoulder).position(shoulderPosition);
        particles.get(elbow).position(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.246216f)));
        particles.get(wrist).position(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.4852301f)));
        
        PMatrix modelWorld = modelInst.getTransform().getWorldMatrix(false);
        particles.get(wrist).dislocate(modelWorld.getLocalZ().mult(0.1f));
    }
    
    private void pointAt()
    {
        float wristDistanceFromShoulder = 0.475f;
        Vector3f shoulderPosition = particles.get(shoulder).getCurrentPosition();
        Vector3f directionFromShoulderToWrist = pointAtLocation.subtract(shoulderPosition).normalize();
        particles.get(wrist).position(shoulderPosition.add(directionFromShoulderToWrist.mult(wristDistanceFromShoulder)));
    }

    public Vector3f getPointAtLocation() {
        return pointAtLocation;
    }

    /**
     * Set to null to cancel pointing
     * @param pointAtLocation
     */
    public void setPointAtLocation(Vector3f pointAtLocation) {
        this.pointAtLocation = pointAtLocation;
    }
    
    public ArrayList<StickConstraint> getConstraints() {
        return constraints;
    }

    public ArrayList<VerletParticle> getParticles() {
        return particles;
    }

    public PMatrix calculateInverseModelWorldMatrix()
    {
        return modelInst.getTransform().getWorldMatrix(false).inverse();
    }
    
    public Vector3f getElbowPosition() 
    {
        return particles.get(elbow).getCurrentPosition();
    }

    public Vector3f getWristPosition() {
        return particles.get(wrist).getCurrentPosition();
    }

    public boolean isEnabled() 
    {
        return enabled;
    }
    
    public void setEnabled(boolean bEnabled)
    {
        enabled = bEnabled;
        if (jointManipulator != null)
            jointManipulator.setEnabled(enabled);
        if (skeletonManipulator != null)
            skeletonManipulator.setArmEnabled(enabled);
        if (enabled)
            resetArm();
    }
    
    public boolean toggleEnabled()
    {
        enabled = !enabled;
        if (jointManipulator != null)
            jointManipulator.setEnabled(enabled);
        if (skeletonManipulator != null)
            skeletonManipulator.setArmEnabled(enabled);
        if (enabled)
            resetArm();
        return enabled;
    }

    public void setJointManipulator(VerletJointManipulator armJointManipulator) 
    {
        jointManipulator = armJointManipulator;
    }
        
    public void setSkeletonManipulator(VerletSkeletonFlatteningManipulator armSkeletonManipulator) 
    {
        this.skeletonManipulator = armSkeletonManipulator;
    }
    
    public void addInputOffset(Vector3f offset)
    {
        currentInputOffset.addLocal(offset);
    }

    /**
     * If not reach up the arm will reach forward
     * @return
     */
    public boolean isManualDriveReachUp() {
        return manualDriveReachUp;
    }

    /**
     * If not reach up the arm will reach forward
     * @param manualDriveReachUp
     */
    public void setManualDriveReachUp(boolean manualDriveReachUp) {
        this.manualDriveReachUp = manualDriveReachUp;
        if (jointManipulator != null)
            jointManipulator.setManualDriveReachUp(manualDriveReachUp);
        if (skeletonManipulator != null)
            skeletonManipulator.setManualDriveReachUp(manualDriveReachUp);
    }
    
    public void toggleManualDriveReachUp(){
        manualDriveReachUp = !manualDriveReachUp;
        if (jointManipulator != null)
            jointManipulator.setManualDriveReachUp(manualDriveReachUp);
        if (skeletonManipulator != null)
            skeletonManipulator.setManualDriveReachUp(manualDriveReachUp);
    }

    //////////////////////////////////////////////////////////////////////////
    
    public class VerletParticle
    {
        private float       mass                = 1.0f;
        private Vector3f    currentPosition     = new Vector3f();
        private Vector3f    previousPosition    = new Vector3f();
        private Vector3f    forceAccumulator    = new Vector3f();
        
        private Vector3f    integrationHelper   = new Vector3f();
        
        private boolean     moveable            = true;
        
        public VerletParticle(Vector3f position, boolean isMoveable)
        {
            currentPosition.set(position);
            previousPosition.set(position);
            moveable = isMoveable;
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
        
        public void scaleVelocity(float scalar)
        {
            Vector3f reverseVelocity    =   previousPosition.subtract(currentPosition);
            previousPosition.set(currentPosition.add(reverseVelocity.mult(scalar)));
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
            directionNormalized.multLocal(distance);
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
            this.currentPosition.set(currentPosition);
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
            this.previousPosition.set(previousPosition);
        }

        public Vector3f getForceAccumulator() {
            return forceAccumulator;
        }

        public void setForceAccumulator(Vector3f forceAccumulator) {
            this.forceAccumulator.set(forceAccumulator);
        }

        public void addToForceAccumulator(Vector3f force) {
            forceAccumulator.addLocal(force);
        }

        public boolean isMoveable() {
            return moveable;
        }

        public void setMoveable(boolean moveable) {
            this.moveable = moveable;
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
