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

package imi.camera;

import com.jme.math.Vector3f;
import imi.character.CharacterMotionListener;
import imi.scene.PMatrix;

/**
 * This class provides the state necessary for performing the chase camera
 * behavior.
 * @author Lou Hayt
 */
public final class ChaseCamState extends AbstractCameraState implements CharacterMotionListener
{
    // Character target (if targeting a character)
    imi.character.Character targetCharacter = null;

    // Camera State
    private final PMatrix transform = new PMatrix();

    // Chase State
    private final Vector3f chasePosition    = new Vector3f();
    private final PMatrix  chaseOrientation = new PMatrix();

    // Settings
    private boolean zoomEnabled = true;
    private final Vector3f desiredPositionOffset   = new Vector3f();
    private final Vector3f lookAtOffset = new Vector3f();

    /// Physics coefficient which controls the influence of the camera's position
    /// over the spring force. The stiffer the spring, the closer it will stay to
    /// the chased object.
    private float stiffness = 1.0f;

    /// Physics coefficient which approximates internal friction of the spring.
    /// Sufficient damping will prevent the spring from oscillating infinitely.
    private float damping = 1.0f;

    /// Mass of the camera body. Heaver objects require stiffer springs with less
    /// damping to move at the same rate as lighter objects.
    private float mass = 1.0f;

    private float lookAtStiffnes = 5.0f;
    private float lookAtDamping  = 2.0f;
    private float lookAtMass     = 1.0f;
    private boolean lookAtSpringEnabled = true;
    private float lookAtLinearTimeCounter = 0.0f;
    private float lookAtLinearTimeLength = 100.0f;

    private boolean hardAttachMode = false;

    // Timed Effects
    private final Vector3f scratch = new Vector3f();

    private final Vector3f desiredPositionOffsetModifier = new Vector3f();
    private float desiredPositionModifierTimeCounter = 0.0f;
    private float desiredPositionModifierTimeLength  = 0.0f;

    private float desiredPositionStiffnessScalar = 1.0f;;
    private float desiredPositionStiffnessTimeCounter = 0.0f;
    private float desiredPositionStiffnessTimeLength  = 0.0f;

    private float lookAtStiffnessScalar = 1.0f;;
    private float lookAtStiffnessTimeCounter = 0.0f;
    private float lookAtStiffnessTimeLength  = 0.0f;

    int lastMouseX;
    int lastMouseY;

    float pitchModifier = 0;

    /**
     * Construct a new instance with the specified offset vectors.
     *
     * <p>The desiredPostionOffset vector is used to tell the camera the position
     * it should seek to relative to the target. For instance, if the target is
     * at location L, then the camera will seek to location L + desiredPositionOffset.
     * The lookAtOffset specifies the difference between the target's transform origin
     * and where the camera should be looking. For instance, if the target is at
     * location L, the camera will seek to looking at L + lookAtOffset. Bother of
     * the provided parameters should be non-null.
     * @param desiredPositionOffset A non-null offset vector
     * @param lookAtOffset A non-null offset vector
     * @throws IllegalArgumentException If either parameter is null
     */
    public ChaseCamState(Vector3f desiredPositionOffset, Vector3f lookAtOffset)
    {
        super(CameraStateType.Chase);
        if (desiredPositionOffset == null || lookAtOffset == null)
            throw new IllegalArgumentException("Null param(s), position offset: " + desiredPositionOffset + ", lookAtOffse: " + lookAtOffset);
        this.desiredPositionOffset.set(desiredPositionOffset);
        this.lookAtOffset.set(lookAtOffset);
    }

    ////////////////////////////////////////
    //////////////// Public API
    ////////////////////////////////////////

    /**
     * Sets the character this camera will follow
     * @param character A character to target, or null to unset.
     */
    public void setTargetCharacter(imi.character.Character character) {
        if (targetCharacter != null && targetCharacter.getContext() != null)
            targetCharacter.getController().removeCharacterMotionListener(this);
        targetCharacter = character;
        if (character != null)
            character.getController().addCharacterMotionListener(this);
    }

    /**
     * Determine if the camera is performing hard attach behavior
     * @return True if in hard attach mode
     */
    public boolean isHardAttached() {
        return hardAttachMode;
    }

    /**
     * Sets hard attach mode.
     *
     * <p>If this mode is set, the camera will always be at <i>precisely</i> the
     * specified offsets from the targets, producing a hard attach effect. If this
     * mode is not armed, the camera will behave as if attached by springs.
     * @param hardAttachMode True to arm hard attach mode
     */
    public void setHardAttachMode(boolean hardAttachMode) {
        this.hardAttachMode = hardAttachMode;
    }

    /**
     * Enabled / Disables the look at spring.
     *
     * <p>If this flag is enabled, the the camera will smoothly seek between
     * look at points (which change via updates as a target moves). If this
     * flag is disabled, then the camera will perform a "hard look at" algorithm.
     * This method also has the side affect of resetting the lerp time for the
     * spring behavior.</p>
     *
     * @param enabled True to enable
     */
    public void setLookAtSpringEnabled(boolean enabled) {
        lookAtSpringEnabled = enabled;
        lookAtLinearTimeCounter = 0.0f;
    }

    /**
     * Determine if the look at spring is enabled.
     *
     * <p>If this flag is enabled, the the camera will smoothly seek between
     * look at points (which change via updates as a target moves). If this
     * flag is disabled, then the camera will perform a "hard look at" algorithm.
     * </p>
     * @return True if enabled
     */
    public boolean isLookAtSpringEnabled() {
        return lookAtSpringEnabled;
    }
    
    /**
     * This method initiates a smooth alteration of the position offset vector.
     *
     * @param desiredPositionModifier The new offset to transition to.
     * @param howLongInSeconds Length or transition
     * @return
     */
    public void startDesiredPositionModifierEffect(Vector3f desiredPositionModifier, float howLongInSeconds)
    {
        desiredPositionModifierTimeCounter = 0.0f;
        desiredPositionModifierTimeLength = howLongInSeconds;
        this.desiredPositionOffsetModifier.set(desiredPositionModifier);
    }

    /**
     * This method initiates a smooth alteration of the position spring stiffness.
     * @param tempStiffnessScalar New stiffness coefficient for the position spring
     * @param howLongInSeconds Length of transition
     */
    public void startDesiredPositionStiffnessEffect(float tempStiffnessScalar, float howLongInSeconds)
    {
        desiredPositionStiffnessTimeCounter = 0.0f;
        desiredPositionStiffnessTimeLength  = howLongInSeconds;
        desiredPositionStiffnessScalar = tempStiffnessScalar;
    }

    /**
     * This method initiates a smooth alteration of the lookAt spring stiffness.
     * @param tempStiffnessScalar New stiffness coefficient for the position spring
     * @param howLongInSeconds Length of transition
     */
    public void startLookAtStiffnessEffect(float tempStiffnessScalar, float howLongInSeconds)
    {
        lookAtStiffnessTimeCounter = 0.0f;
        lookAtStiffnessTimeLength  = howLongInSeconds;
        lookAtStiffnessScalar = tempStiffnessScalar;
    }

    /**
     * Retrieve the current value of the position offset vector.
     * @param vOut A non-null storage object
     */
    public void getDesiredPositionOffset(Vector3f vOut) {
        vOut.set(desiredPositionOffset);
    }

    /**
     * Sets the desired position offset.
     * @param desiredPositionOffset A non-null position vector
     * @throws IllegalArgumentException If desiredPositionOffset == null
     */
    public void setDesiredPositionOffset(Vector3f desiredPositionOffset) {
        if (desiredPositionOffset == null)
            throw new IllegalArgumentException("Position was null");
        this.desiredPositionOffset.set(desiredPositionOffset);
    }

    /**
     * Retrieve the current value of the look at offset vector
     * @param vOut a non-null storage object
     */
    public void getLookAtOffset(Vector3f vOut) {
        vOut.set(lookAtOffset);
    }

    /**
     * Sets the look at offset vector to the specified value.
     * @param lookAtOffset A non-null offset vector
     * @throws IllegalArgumentException If lookAtOffset == null
     */
    public void setLookAtOffset(Vector3f lookAtOffset) {
        if (lookAtOffset == null)
            throw new IllegalArgumentException("Null offset.");
        this.lookAtOffset.set(lookAtOffset);
    }

    /**
     * Gets the current value of the camera translation
     * @param position A non-null storage object
     */
    public void getCameraPosition(Vector3f vOut) {
        transform.getTranslation(vOut);
    }

    /**
     * Retrieve the damping coefficient for the position spring.
     * @return
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Sets the damping coefficient for the position spring.
     *
     * <p>If the provided coefficient is a small number (<pre>{@code damping < 1}</pre>) then
     * the damping effect is greater. If the value is larger than 1, then the damping
     * is in fact a multiplier (inverse damping)<p>
     * @param damping
     */
    public void setDamping(float damping) {
        this.damping = damping;
    }

    /**
     * Retrieve the current mass of the camera
     * @return Camera mass
     */
    public float getMass() {
        return mass;
    }

    /**
     * Set the mass of the camera
     * @param mass Camera mass
     */
    public void setMass(float mass) {
        this.mass = mass;
    }

    /**
     * Retrieve the stiffness of the position spring
     * @return Position spring stiffness
     */
    public float getStiffness()
    {
        if (desiredPositionStiffnessTimeCounter < desiredPositionStiffnessTimeLength)
            return stiffness * desiredPositionStiffnessScalar;
        return stiffness;
    }

    /**
     * Set the position spring stiffness
     * @param stiffness
     */
    public void setStiffness(float stiffness) {
        this.stiffness = stiffness;
    }

    /**
     * Retrieve the damping coefficient for the look at spring.
     *
     * <p></p>
     * @return
     */
    public float getLookAtDamping() {
        return lookAtDamping;
    }

    /**
     * Sets the damping coefficient for the look at spring.
     *
     * <p>If the provided coefficient is a small number (<pre>{@code damping < 1}</pre>) then
     * the damping effect is greater. If the value is larger than 1, then the damping
     * is in fact a multiplier (inverse damping)<p>
     * @param lookAtDamping
     */
    public void setLookAtDamping(float lookAtDamping) {
        this.lookAtDamping = lookAtDamping;
    }

    /**
     * Retrieve the mass of the camera as used in look at operations
     * @return camera mass
     */
    public float getLookAtMass() {
        return lookAtMass;
    }

    /**
     * Set the mass of the camera as used in look at operations.
     * @param lookAtMass  The camera mass.
     */
    public void setLookAtMass(float lookAtMass) {
        this.lookAtMass = lookAtMass;
    }

    /**
     * Retrieve the stiffness coefficient of the look at spring
     * @return Stiffness coefficient
     */
    public float getLookAtStiffness() {
        float result = 0;
        if (lookAtStiffnessTimeCounter < lookAtStiffnessTimeLength)
            result = lookAtStiffnes * lookAtStiffnessScalar;
        else
            result = lookAtStiffnes;
        return result;
    }

    /**
     * Set the stiffness coefficient of the look at spring.
     * @param lookAtStiffnes stiffness coefficient
     */
    public void setLookAtStiffness(float lookAtStiffnes) {
        this.lookAtStiffnes = lookAtStiffnes;
    }

    /**
     * Determine if the zoom mode is enabled.
     *
     * <p>If this mode is enabled, then the mouse wheel can be used to move the
     * desire position offset further or closer to the target.</p>
     * @return True if enabled
     */
    public boolean isZoomEnabled() {
        return zoomEnabled;
    }

    /**
     * Enables or Disables zoom mode.
     *
     * <p>If this mode is enabled, then the mouse wheel can be used to move the
     * desire position offset further or closer to the target.</p>
     * @param zoomEnabled true to enable
     */
    public void setZoomEnabled(boolean zoomEnabled) {
        this.zoomEnabled = zoomEnabled;
    }

    ////////////////////////////////////
    ///////// Package API
    ////////////////////////////////////

    Vector3f getLookAtOffsetRef() {
        return lookAtOffset;
    }

    Vector3f getDesiredPositionOffsetRef() {
        return desiredPositionOffset;
    }

    public void getChasePosition(Vector3f out) {
        out.set(chasePosition);
    }    

    float getLookAtLinearTimeCounter() {
        return lookAtLinearTimeCounter;
    }

    float getLookAtLinearTimeLength() {
        return lookAtLinearTimeLength;
    }

    void setLookAtLinearTimeLength(float lookAtLinearTimeLength) {
        this.lookAtLinearTimeLength = lookAtLinearTimeLength;
    }

    void update(float deltaTime) {
        desiredPositionModifierTimeCounter += deltaTime;
        desiredPositionStiffnessTimeCounter += deltaTime;
        lookAtStiffnessTimeCounter += deltaTime;
        lookAtLinearTimeCounter += deltaTime;
    }

    public void setChasePosition(Vector3f position) {
        chasePosition.set(position);
    }

    Vector3f getChasePosition() {
        return chasePosition;
    }

    PMatrix getChaseOrientation() {
        return chaseOrientation;
    }

    public void setChaseOrientation(PMatrix chaseOrientation) {
        this.chaseOrientation.set(chaseOrientation);
    }

    void getTransformedAndPositionedDesiredPositionOffset(Vector3f out)
    {
        chaseOrientation.transformNormal(desiredPositionOffset, out);
        if (desiredPositionModifierTimeCounter < desiredPositionModifierTimeLength)
        {
            chaseOrientation.transformNormal(desiredPositionOffsetModifier, scratch);
            out.addLocal(scratch);
        }
        out.addLocal(chasePosition);
    }

    
    void getTransformedAndPositionedLookAtOffset(Vector3f out) {
        chaseOrientation.transformNormal(lookAtOffset, out);
        out.addLocal(chasePosition);
    }


    void addToCameraPosition(Vector3f add) {
        transform.setTranslation(transform.getTranslation().add(add));
    }

    @Override
    public void setCameraPosition(Vector3f position) {
        transform.setTranslation(position);
    }

    @Override
    public void setCameraTransform(PMatrix transform) {
        this.transform.set(transform);
    }

    void setCameraTransform(float[] floats) {
        transform.set(floats);
        transform.normalize();
    }

   // @Override
    public PMatrix getCameraTransform() {
        return transform;
    }

    public void transformUpdate(Vector3f translation, PMatrix rotation) {
        setChasePosition(translation);
        setChaseOrientation(rotation);
    }

}
