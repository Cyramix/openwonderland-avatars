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
import imi.scene.SkinnedMeshJoint;
import java.util.List;
import javolution.util.FastTable;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * This class encapsulates the data and logic necessary to simulate a verlet arm.
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 * @author Shawn Kendall
 */
@ExperimentalAPI
public class VerletArm 
{
    /**
     * This enumeration represents parts of an arm
     */
    private enum ArmParts {
        Shoulder(0),
        Elbow(1),
        Wrist(2);
        // Used with verlet particle construction
        int particleIndex;

        ArmParts(int index)
        {
            this.particleIndex = index;
        }
    }
    /** true for thisIsRightHand hand and false for left hand **/
    private boolean thisIsRightHand = true;
    /** The owning instance **/
    private final PPolygonModelInstance characterModelInst;
    /** Reference to the joint where the arm attaches **/
    private final SkinnedMeshJoint shoulderJoint;
    /** List of verlet particles that make this arm **/
    private final List<VerletParticle>  particles    = new FastTable<VerletParticle>();
    /** List of constraints applied to the verlet particles **/
    private final List<StickConstraint> constraints  = new FastTable<StickConstraint>();
    /** Gravity vector this particle system uses **/
    private final Vector3f gravity = new Vector3f(0.0f, -9.8f * 2.0f, 0.0f); // gravity is increased for a "better look"
    /** Used to simulate energy lost due to friction, drag, etc **/
    private float velocityDampener = 0.8f;
    /** Switch the behavior on and off **/
    private boolean enabled = false;
    /** Hook to modify the skeleton **/
    private VerletSkeletonFlatteningManipulator skeletonManipulator = null;
    /** Used to track input movement **/
    private final Vector3f currentInputOffset = new Vector3f();
    /** Maximum  distance to allow the hand to travel **/
    private float maxReach = 0.4834519f + 0.0075f;
    /** Switch the driving mode for the hand **/
    private boolean manualDriveReachUp = true; // otherwise reach forward
    /** Used as a pointing target for the arm, null disables pointing **/
    private Vector3f pointAtLocation = null;

    /** Updates occur at a fixed interval **/
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
        thisIsRightHand = rightHand;
        characterModelInst = modelInstance;
        shoulderJoint = shoulderJ;
        
        // Lets make an arm
        Vector3f shoulderPosition = shoulderJoint.getTransform().getWorldMatrix(false).getTranslation();

        // The magic numbers below are taken from the avatar's bind pose data
        particles.add(new VerletParticle(shoulderPosition, false));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.246216f)), true));
        particles.add(new VerletParticle(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.4852301f)), false));
        constraints.add(new StickConstraint(ArmParts.Shoulder.particleIndex, ArmParts.Elbow.particleIndex, 0.246216f));
        constraints.add(new StickConstraint(ArmParts.Elbow.particleIndex, ArmParts.Wrist.particleIndex, 0.2390151f));
    }

    /**
     * Update the simulation.
     * @param deltaTime The timestep
     */
    public void update(float deltaTime) 
    {
        if (!enabled)
            return;
        
        // Updates occur at a fixed interval
        armTimer += deltaTime;
        if (armTimer < armTimeTick)
            return; // Not ready for a tick yet
        armTimer  = 0.0f;
        deltaTime = armTimeTick;
        
        // Attach the first particle to the shoulder joint, apply the difference
        // on all particles so they arm will move with the body movements.
        Vector3f prevPos = new Vector3f(particles.get(ArmParts.Shoulder.particleIndex).getCurrentPosition());
        particles.get(ArmParts.Shoulder.particleIndex).position(shoulderJoint.getTransform().getWorldMatrix(false).getTranslation());
        Vector3f dif = particles.get(ArmParts.Shoulder.particleIndex).getCurrentPosition().subtract(prevPos);
        for (int i = 1; i < particles.size(); i++)
            particles.get(i).dislocate(dif);
        
        // If we are pointing, use the pointAt method, otherwise just run the simulation
        if (pointAtLocation != null)
            pointAt();
        else
        {
            Vector3f shoulderPosition = particles.get(ArmParts.Shoulder.particleIndex).getCurrentPosition();
            Vector3f wristPosition    = particles.get(ArmParts.Wrist.particleIndex).getCurrentPosition();
            if (wristPosition.distance(shoulderPosition) > maxReach)
                particles.get(ArmParts.Wrist.particleIndex).setMoveable(true);
            else
                particles.get(ArmParts.Wrist.particleIndex).setMoveable(false);
                
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
                        particles.get(ArmParts.Wrist.particleIndex).dislocate(currentInputOffset);
                    else
                        particles.get(ArmParts.Wrist.particleIndex).dislocate(modelWorld.getLocalZ(), 0.001f);
                }
                else
                    particles.get(ArmParts.Wrist.particleIndex).dislocate(directionFromShoulderToWrist, -0.005f);

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
                    continue;
                
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

    /**
     * Put the arm back into its starting position for verlet manipulation
     */
    public void resetArm()
    {
        pointAtLocation = null;
        
        Vector3f shoulderPosition = shoulderJoint.getTransform().getWorldMatrix(false).getTranslation();
        particles.get(ArmParts.Shoulder.particleIndex).position(shoulderPosition);
        particles.get(ArmParts.Elbow.particleIndex).position(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.246216f)));
        particles.get(ArmParts.Wrist.particleIndex).position(shoulderPosition.add(Vector3f.UNIT_Y.mult(-0.4852301f)));
        
        // set the hand a bit forwards and up
        PMatrix modelWorld = characterModelInst.getTransform().getWorldMatrix(false);
        particles.get(ArmParts.Wrist.particleIndex).dislocate(modelWorld.getLocalZ().mult(0.25f));
        particles.get(ArmParts.Wrist.particleIndex).dislocate(Vector3f.UNIT_Y.mult(0.25f));
    }

    /**
     * Makes the arm point at the pointAtLocation
     * pointAtLocation must not be null!
     */
    private void pointAt()
    {
        float wristDistanceFromShoulder = 0.475f;
        Vector3f shoulderPosition = particles.get(ArmParts.Shoulder.particleIndex).getCurrentPosition();
        Vector3f directionFromShoulderToWrist = pointAtLocation.subtract(shoulderPosition).normalize();
        particles.get(ArmParts.Wrist.particleIndex).position(shoulderPosition.add(directionFromShoulderToWrist.mult(wristDistanceFromShoulder)));
    }

    /**
     * Retrieve a copy of the position being pointed at, or null if there is
     * no point at location.
     * @return Copy of pointing target, or null if none is set
     */
    public Vector3f getPointAtLocation() {
        Vector3f result = null;
        if (pointAtLocation != null)
            result = new Vector3f(pointAtLocation);
        return result;
    }

    /**
     * Set to null to cancel pointing.
     * @param pointAtLocation A location vector, or null to disable pointing
     */
    public void setPointAtLocation(Vector3f pointAtLocation) {
        if (pointAtLocation == null)
            this.pointAtLocation = null;
        else
        {
            if (this.pointAtLocation != null)
                this.pointAtLocation.set(pointAtLocation);
            else
                this.pointAtLocation = pointAtLocation;
        }
    }
    
    /**
     * Retrieve an immutable list of the stick constraint objects this arm is using.
     * @return List of constraints
     */
    @InternalAPI
    public Iterable<StickConstraint> getConstraints() {
        return constraints;
    }

    /**
     * Retrieve an immutable list of the particle objects this arm is using.
     * @return List of particles
     */
    @InternalAPI
    public Iterable<VerletParticle> getParticles() {
        return particles;
    }

    /**
     * Retrieve a copy of the elbow position.
     * @param vOut A non-null storage object
     * @throws IllegalArgumentException If {@code vOut == null}
     */
    public void getElbowPosition(Vector3f vOut)
    {
        if (vOut == null)
            throw new IllegalArgumentException("Null storage object provided.");
        else 
            vOut.set(particles.get(ArmParts.Elbow.particleIndex).getCurrentPosition());
    }

    /**
     * Retrieve a copy of the wrist position.
     * @param vOut A non-null storage object
     * @throws IllegalArgumentException If {@code vOut == null}
     */
    public void getWristPosition(Vector3f vOut) {
        if (vOut == null)
            throw new IllegalArgumentException("Null storage object provided.");
        else
            vOut.set(particles.get(ArmParts.Wrist.particleIndex).getCurrentPosition());
    }

    /**
     * Retrieve a copy of the wrist velocity.
     * @param vOut A non-null storage object
     * @throws IllegalArgumentException If {@code vOut == null}
     */
    public void getWristVelocity(Vector3f vOut) {
        if (vOut == null)
            throw new IllegalArgumentException("Null storage object provided.");
        else
            vOut.set(particles.get(ArmParts.Wrist.particleIndex).getVelocity());
    }

    /**
     * Is the verlet arm simulation enabled?
     * @return True if enabled, false otherwise
     */
    public boolean isEnabled() 
    {
        return enabled;
    }

    /**
     * Enables / Disables the verlet arm simulation.
     * @param bEnabled True to enable, false to disable
     */
    public void setEnabled(boolean bEnabled)
    {
        enabled = bEnabled;
        if (skeletonManipulator != null)
        {
            if (thisIsRightHand)
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

    /**
     * Sets the reference of the VerletSkeletonFlatteningManipulator used within.
     * @param armSkeletonManipulator The manipulator to use
     */
    public void setSkeletonManipulator(VerletSkeletonFlatteningManipulator armSkeletonManipulator) 
    {
        this.skeletonManipulator = armSkeletonManipulator;
        if (skeletonManipulator != null)
            skeletonManipulator.setManualDriveReachUp(manualDriveReachUp);
    }

    /**
     * Add the provided offset to the current input offset.
     * @param offset A non-null offset vector
     * @throws IllegalArgumentException If {@code offset == null}
     */
    public void addInputOffset(Vector3f offset)
    {
        currentInputOffset.addLocal(offset);
    }

    /**
     * True if the arm is reaching upward (rather than forward)
     * @return True if reaching up, false if reaching out / forward
     */
    public boolean isManualDriveReachUp() {
        return manualDriveReachUp;
    }

    /**
     * If not reach up the arm will reach forward.
     * @param manualDriveReachUp True to reach up, false to reach forward
     */
    public void setManualDriveReachUp(boolean manualDriveReachUp) {
        this.manualDriveReachUp = manualDriveReachUp;
        if (skeletonManipulator != null)
            skeletonManipulator.setManualDriveReachUp(manualDriveReachUp);
    }

    /**
     * Change the arm's input response from reaching upward to reaching forward.
     * <p>This method toggles the current state of reaching.<p>
     */
    public void toggleManualDriveReachUp(){
        manualDriveReachUp = !manualDriveReachUp;
        if (skeletonManipulator != null)
            skeletonManipulator.setManualDriveReachUp(manualDriveReachUp);
    }

    /**
     * Retrieve the values of the positions for the two points used by the
     * provided constraint.
     * @param constraint The constraint in question.
     * @param positionOneOut A non-null storage object
     * @param positionTwoOut A non-null storage object
     * @throws IllegalArgumentException If any param is null.
     */
    public void getPointsForConstraint(StickConstraint constraint,
                                        Vector3f positionOneOut,
                                        Vector3f positionTwoOut)
    {
        if (constraint == null || positionOneOut == null || positionTwoOut == null)
            throw new IllegalArgumentException("Null param encountered!");
        positionOneOut.set(particles.get(constraint.particle1).currentPosition);
        positionTwoOut.set(particles.get(constraint.particle2).currentPosition);
    }

    
    /**
     * The standard Verlet particle
     * @author Lou Hayt
     */
    public static class VerletParticle
    {
        /** The mass of this particular particle **/
        private float mass                = 1.0f;
        /** The current position of this particle **/
        private final Vector3f    currentPosition   = new Vector3f();
        /** History **/
        private final Vector3f    previousPosition  = new Vector3f();
        /** Used during the update process to concatenate forces into one net-force vector**/
        private final Vector3f    forceAccumulator  = new Vector3f();
        /** Used in calculations **/
        private final Vector3f    integrationHelper = new Vector3f();
        /** Determines if this particle is fixed or not **/
        private boolean moveable    = true;


        /**
         * Construct a new particle at the given position
         * @param position A non-null position
         * @param isMoveable True if particle should be moveable
         * @throws IllegalArgumentException if {@code position == null}
         */
        public VerletParticle(Vector3f position, boolean isMoveable)
        {
            if (position == null)
                throw new IllegalArgumentException("Null position provided!");
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
        
        Vector3f getVelocity()
        {
            return currentPosition.subtract(previousPosition);
        }

        /**
         * Multiply the velocity by the provided scale.
         * @param scalar Scale the velocity by this.
         */
        void scaleVelocity(float scalar)
        {
            Vector3f reverseVelocity    =   previousPosition.subtract(currentPosition);
            previousPosition.set(currentPosition.add(reverseVelocity.mult(scalar)));
        }

        /**
         * Sets velocity to the zero vector.
         */
        void stop()
        {
            previousPosition.set(currentPosition);
        }

        /**
         * Moves the particle to the provided position without introducing
         * energy into the system
         * @param vOut A non-null storage object
         * @throws IllegalArgumentException If {@code vOut == null}
         */
        void position(Vector3f vOut)
        {
            Vector3f velocity = getVelocity();
            currentPosition.set(vOut);
            previousPosition.set(currentPosition.subtract(velocity));
        }

        /**
         * Offset the particle's current position by the given offset, does not
         * introduce energy into the system.
         * @param offset A non-null offset
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

        /**
         * Retrieve the current mass value
         * @return A non-negative float
         */
        public float getMass() {
            return mass;
        }

        /**
         * Sets the current mass value
         * @param mass A non-negative mass
         * @throws IllegalArgumentException If <pre>{@code mass < 0}</pre>
         */
        public void setMass(float mass) {
            if (mass < 0)
                throw new IllegalArgumentException("Negative mass provided!");
            this.mass = mass;
        }

        /**
         * Retrieve the previous position.
         * @param vOut A non-null storage object
         * @throws IllegalArgumentException If {@code vOut == null}
         */
        public void getPreviousPosition(Vector3f vOut) {
            if (vOut == null)
                throw new IllegalArgumentException("Null storage object");
            else
                vOut.set(previousPosition);
        }

        /**
         * Sets the previous position to the one specified.
         * @param previousPosition A non-null position vector
         * @throws IllegalArgumentException If {@code previousPosition == null}
         */
        public void setPreviousPosition(Vector3f previousPosition) {
            if (previousPosition == null)
                throw new IllegalArgumentException("Null position provided");
            else
                this.previousPosition.set(previousPosition);
        }

        void setForceAccumulator(Vector3f forceAccumulator) {
            this.forceAccumulator.set(forceAccumulator);
        }

        /**
         * Return true if this particle can change positions during the simulation.
         * @return True if moveable
         */
        public boolean isMoveable() {
            return moveable;
        }

        /**
         * Enable / Disable the moveable flag.
         * @param moveable True to move, false to stay put
         */
        public void setMoveable(boolean moveable) {
            this.moveable = moveable;
        }
    }
    
    
    /**
     * This class represents the basic stick constraint used with the verlet
     * system.
     * @author Lou Hayt
     */
    public static class StickConstraint
    {
        /** Connection point one (index) **/
        private int     particle1;
        /** Connection point two (index) **/
        private int     particle2;
        /** Desired distance between the particles **/
        private float   fRestDistance;

        /**
         * Construct a new constraint between the two particles indicated by
         * <code>particle1Index</code> and <code>particle2Index</code> that will
         * try to maintain <code>restDistance</code> separation between the two.
         * @param particle1Index
         * @param particle2Index
         * @param restDistance The optimal separation distance between the two particles
         * @throws IllegalArgumentException If any param is less than zero.
         */
        StickConstraint(int particle1Index, int particle2Index, float restDistance)
        {
            if (particle1Index < 0 || particle2Index < 0 || restDistance < 0)
                throw new IllegalArgumentException("Negative param, particle1Index: " + particle1Index +
                        ", particle2Index: " + particle2Index + ", restDistance: " + restDistance);
            particle1 = particle1Index;
            particle2 = particle2Index;
            fRestDistance = restDistance;
        }

        float getRestDistance() {
            return fRestDistance;
        }

        void setRestDistance(float fRestDistance) {
            this.fRestDistance = fRestDistance;
        }

        int getParticle1() {
            return particle1;
        }

        void setParticle1(int particle1) {
            this.particle1 = particle1;
        }

        int getParticle2() {
            return particle2;
        }

        void setParticle2(int particle2) {
            this.particle2 = particle2;
        }
    }

}
