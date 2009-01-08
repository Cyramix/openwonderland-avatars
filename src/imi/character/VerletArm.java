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
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import java.util.ArrayList;

/**
 * This class encapsulates the data and logic necessary to simulate a verlet arm.
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 * @author Shawn Kendall
 */
public class VerletArm 
{
    /** true for right hand and false for left hand **/
    private boolean right = true;
    
    /** The owning instance **/
    private PPolygonModelInstance characterModelInst = null;
    /** Reference to the joint where the arm attaches **/
    private SkinnedMeshJoint shoulderJoint  = null;

    /** List of verlet particles that make this arm **/
    private ArrayList<VerletParticle>  particles    = new ArrayList<VerletParticle>();
    /** List of constraints applied to the verlet particles **/
    private ArrayList<StickConstraint> constraints  = new ArrayList<StickConstraint>();
    
    private Vector3f gravity = new Vector3f(0.0f, -9.8f * 2.0f, 0.0f); // gravity is increaced for a "better look"
    /** Used to simulate energy lost due to friction, drag, etc **/
    private float velocityDampener = 0.8f;
    /** Switch the behavior on and off **/
    private boolean enabled = false;
    private VerletSkeletonFlatteningManipulator skeletonManipulator = null;
    
    private Vector3f currentInputOffset = new Vector3f();
    
    private float maxReach = 0.4834519f + 0.0075f;
    /** Enumerations for clarity **/
    private final int shoulder  = 0;
    private final int elbow     = 1;
    private final int wrist     = 2;
    /** Switch the driving mode for the hand **/
    private boolean manualDriveReachUp = true; // otherwise reach forward
    /** Used as a pointing target for the arm, null disables pointing **/
    private Vector3f pointAtLocation = null;

    // Updates occure at a fixed interval
    private float    armTimer         = 0.0f;
    private float    armTimeTick      = 1.0f / 60.0f;
    
    /**
     * Construct a new verlet arm attached to the provided joint as the shoulder,
     * belonging to the provided model instance.
     * @param shoulderJ The joint to attach to as a shoulder
     * @param modelInstance The owner
     */
    public VerletArm(SkinnedMeshJoint shoulderJ, PPolygonModelInstance modelInstance, boolean rightHand) 
    {
        right = rightHand;
        characterModelInst = modelInstance;
        shoulderJoint = shoulderJ;
        
        // Lets make an arm
        Vector3f shoulderPosition = shoulderJoint.getTransform().getWorldMatrix(false).getTranslation();
        // The magic numbers below are taken from the avatar's bind pose data
        particles.add(new VerletParticle(shoulderPosition, false));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.246216f)), true));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.4852301f)), false));
        constraints.add(new StickConstraint(shoulder, elbow, 0.246216f));
        constraints.add(new StickConstraint(elbow, wrist, 0.2390151f));
    }

    /**
     * Update the simulation
     * @param deltaTime
     */
    public void update(float deltaTime) 
    {
        if (!enabled)
            return;
        
        // Updates occure at a fixed interval
        armTimer += deltaTime;
        if (armTimer < armTimeTick)
            return;
        armTimer  = 0.0f;
        deltaTime = armTimeTick;
        
        // Attach the first particle to the shoulder joint, apply the difference
        // on all particles so they arm will move with the body movements.
        Vector3f prevPos = new Vector3f(particles.get(shoulder).getCurrentPosition());
        particles.get(shoulder).position(shoulderJoint.getTransform().getWorldMatrix(false).getTranslation());
        Vector3f dif = particles.get(shoulder).getCurrentPosition().subtract(prevPos);
        for (int i = 1; i < particles.size(); i++)
            particles.get(i).dislocate(dif);
        
        // If we are pointing, use the pointAt method, otherwise just run the simulation
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

                PMatrix modelWorld = characterModelInst.getTransform().getWorldMatrix(false);
                modelWorld.transformNormal(currentInputOffset);

                // Check if it is ok to offset the wrist to the new position
                Vector3f newWristPosition = wristPosition.add(currentInputOffset);
                Vector3f directionFromShoulderToWrist = newWristPosition.subtract(shoulderPosition).normalize();
                if (newWristPosition.distance(shoulderPosition) < maxReach)
                {
                    float backReach = 0.0f;

                    if (modelWorld.getLocalZ().dot(directionFromShoulderToWrist) > backReach)
                        particles.get(wrist).dislocate(currentInputOffset);
                    else
                        particles.get(wrist).dislocate(modelWorld.getLocalZ(), 0.001f);
                }
                else
                    particles.get(wrist).dislocate(directionFromShoulderToWrist, -0.005f);

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
            particles.get(i).verletIntegration(deltaTime);
	}
        
	// Solving constraints by relaxation
	for(int i = 0; i < 5; i++)
            satisfyConstraints(); 
    }

    /**
     * Move towards a state of equilibrium.
     */
    private void satisfyConstraints()
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
        
        // set the hand a bit forwards and up
        PMatrix modelWorld = characterModelInst.getTransform().getWorldMatrix(false);
        particles.get(wrist).dislocate(modelWorld.getLocalZ().mult(0.25f));
        particles.get(wrist).dislocate(Vector3f.UNIT_Y.mult(0.25f));
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

    private PMatrix calculateInverseModelWorldMatrix()
    {
        return characterModelInst.getTransform().getWorldMatrix(false).inverse();
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
        if (skeletonManipulator != null)
        {
            if (right)
                skeletonManipulator.setRightArmEnabled(bEnabled);
            else
                skeletonManipulator.setLeftArmEnabled(bEnabled);
        }
        if (enabled)
            resetArm();
    }

    /**
     * <code>enabled = !enabled</code>
     * @return The new state of <code>enabled</code>
     */
    public boolean toggleEnabled()
    {
        setEnabled(!enabled);
        return enabled;
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
        if (skeletonManipulator != null)
            skeletonManipulator.setManualDriveReachUp(manualDriveReachUp);
    }
    
    public void toggleManualDriveReachUp(){
        manualDriveReachUp = !manualDriveReachUp;
        if (skeletonManipulator != null)
            skeletonManipulator.setManualDriveReachUp(manualDriveReachUp);
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * The standard Verlet particle
     * @author Lou Hayt
     */
    public class VerletParticle
    {
        /** The mass of this particular particle **/
        private float       mass                = 1.0f;

        private Vector3f    currentPosition     = new Vector3f();
        private Vector3f    previousPosition    = new Vector3f();
        /** Used during the update process to concatenate forces into one net-force vector**/
        private Vector3f    forceAccumulator    = new Vector3f();
        
        private Vector3f    integrationHelper   = new Vector3f();
        /** Determines if this particle is fixed or not **/
        private boolean     moveable            = true;

        /**
         * Construct a new particle at the given position
         * @param position
         * @param isMoveable
         */
        public VerletParticle(Vector3f position, boolean isMoveable)
        {
            currentPosition.set(position);
            previousPosition.set(position);
            moveable = isMoveable;
        }

        /**
         * Package-private helper method to aid in integration.
         * @param physicsUpdateTime The time slice.
         */
        void verletIntegration(float physicsUpdateTime)
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

        /**
         * Sets velocity to the zero vector.
         */
        public void stop()
        {
            previousPosition.set(currentPosition);
        }

        /**
         * Moves the particle to the provided position without introducing
         * energy into the system
         * @param position
         */
        public void position(Vector3f position)
        {
            Vector3f velocity = getVelocity();
            currentPosition.set(position);
            previousPosition.set(currentPosition.subtract(velocity));
        }

        /**
         * Offset the particle's current position by the given offset, does not
         * introduce energy into the system.
         * @param offset
         */
        public void dislocate(Vector3f offset)
        {
            currentPosition.addLocal(offset);
            previousPosition.addLocal(offset);
        }

        /**
         * Use the polar coordinates provided to offset the particle from its
         * current position, does not introduce energy into the system.
         * @param directionNormalized
         * @param distance
         */
        public void dislocate(Vector3f directionNormalized, float distance)
        {
            directionNormalized.multLocal(distance);
            currentPosition.addLocal(directionNormalized);
            previousPosition.addLocal(directionNormalized);
        }

        /**
         * Change position by the provided offset vector. This does introduce
         * energy into the system.
         * @param offset
         */
        public void moveCurrentPosition(Vector3f offset)
        {
            currentPosition.addLocal(offset);
        }

        /**
         * Change the history of the particle by moving its previous position
         * by the provided offset. This will introduce energy into the system.
         * @param offset
         */
        public void movePreviousPosition(Vector3f offset)
        {
            previousPosition.addLocal(offset);
        }

        /**
         * Retrieve a reference to the current position
         * @return
         */
        public Vector3f getCurrentPosition() {
            return currentPosition;
        }
        /**
         * Sets the current position; will introduce energy into the system
         * @param currentPosition
         */
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
    /**
     * This class represents the basic stick constraint used with the verlet
     * system.
     * @author Lou Hayt
     */
    public class StickConstraint
    {
        private int	particle1;
        private int	particle2;
        private float   fRestDistance;

        /**
         * Construct a new constraint between the two particles indicated by
         * <code>particle1Index</code> and <code>particle2Index</code> that will
         * try to maintain <code>restDistance</code> separation between the two.
         * @param particle1Index
         * @param particle2Index
         * @param restDistance The optimal separation distance between the two particles
         */
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
