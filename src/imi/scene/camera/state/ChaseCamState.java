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

package imi.scene.camera.state;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;

/**
 *
 * @author Lou Hayt
 */
public class ChaseCamState extends CameraState
{
    // Camera State
    PMatrix transform = new PMatrix();

    // Chase State
    Vector3f chasePosition    = new Vector3f();
    PMatrix  chaseOrientation = new PMatrix();

    // Settings
    Vector3f desiredPositionOffset   = new Vector3f();
    Vector3f lookAtOffset = new Vector3f();

    /// Physics coefficient which controls the influence of the camera's position
    /// over the spring force. The stiffer the spring, the closer it will stay to
    /// the chased object.
    float stiffness = 1.0f;

    /// Physics coefficient which approximates internal friction of the spring.
    /// Sufficient damping will prevent the spring from oscillating infinitely.
    float damping = 1.0f;

    /// Mass of the camera body. Heaver objects require stiffer springs with less
    /// damping to move at the same rate as lighter objects.
    float mass = 1.0f;

    float lookAtStiffnes = 5.0f;
    float lookAtDamping  = 2.0f;
    float lookAtMass     = 1.0f;

    // Timed Effects
    Vector3f scratch = new Vector3f();

    Vector3f desiredPositionOffsetModifier = new Vector3f();
    float desiredPositionModifierTimeCounter = 0.0f;
    float desiredPositionModifierTimeLength  = 0.0f;

    float desiredPositionStiffnessScalar = 1.0f;;
    float desiredPositionStiffnessTimeCounter = 0.0f;
    float desiredPositionStiffnessTimeLength  = 0.0f;

    float lookAtStiffnessScalar = 1.0f;;
    float lookAtStiffnessTimeCounter = 0.0f;
    float lookAtStiffnessTimeLength  = 0.0f;

    public ChaseCamState(Vector3f desiredPositionOffset, Vector3f lookAtOffset)
    {
        setType(CameraStateType.Chase);
        this.desiredPositionOffset.set(desiredPositionOffset);
        this.lookAtOffset.set(lookAtOffset);
    }

    /**
     * Local space to the chase target
     */
    public boolean startDesiredPositionModifierEffect(Vector3f desiredPositionModifier, float howLongInSeconds)
    {
        desiredPositionModifierTimeCounter = 0.0f;
        desiredPositionModifierTimeLength = howLongInSeconds;
        this.desiredPositionOffsetModifier.set(desiredPositionModifier);
        return true;
    }

    public boolean startDesiredPositionStiffnessEffect(float tempStiffnessScalar, float howLongInSeconds)
    {
        desiredPositionStiffnessTimeCounter = 0.0f;
        desiredPositionStiffnessTimeLength  = howLongInSeconds;
        desiredPositionStiffnessScalar = tempStiffnessScalar;
        return true;
    }

    public boolean startLookAtStiffnessEffect(float tempStiffnessScalar, float howLongInSeconds)
    {
        lookAtStiffnessTimeCounter = 0.0f;
        lookAtStiffnessTimeLength  = howLongInSeconds;
        lookAtStiffnessScalar = tempStiffnessScalar;
        return true;
    }

    public void update(float deltaTime) {
        desiredPositionModifierTimeCounter += deltaTime;
        desiredPositionStiffnessTimeCounter += deltaTime;
        lookAtStiffnessTimeCounter += deltaTime;
    }

    public void setChasePosition(Vector3f position) {
        chasePosition.set(position);
    }

    public Vector3f getChasePosition() {
        return chasePosition;
    }

    public PMatrix getChaseOrientation() {
        return chaseOrientation;
    }

    public void setChaseOrientation(PMatrix chaseOrientation) {
        this.chaseOrientation.set(chaseOrientation);
    }

    public Vector3f getDesiredPositionOffset() {
        return desiredPositionOffset;
    }

    public void getTransformedAndPositionedDesiredPositionOffset(Vector3f out)
    {
        chaseOrientation.transformNormal(desiredPositionOffset, out);
        if (desiredPositionModifierTimeCounter < desiredPositionModifierTimeLength)
        {
            chaseOrientation.transformNormal(desiredPositionOffsetModifier, scratch);
            out.addLocal(scratch);
        }
        out.addLocal(chasePosition);
    }

    public void setDesiredPositionOffset(Vector3f desiredPositionOffset) {
        this.desiredPositionOffset.set(desiredPositionOffset);
    }

    public Vector3f getLookAtOffset() {
        return lookAtOffset;
    }
    
    public void getTransformedAndPositionedLookAtOffset(Vector3f out) {
        chaseOrientation.transformNormal(lookAtOffset, out);
        out.addLocal(chasePosition);
    }

    public void setLookAtOffset(Vector3f lookAtOffset) {
        this.lookAtOffset.set(lookAtOffset);
    }

    public void getCameraPosition(Vector3f position) {
        transform.getTranslation(position);
    }

    public void addToCameraPosition(Vector3f add) {
        transform.setTranslation(transform.getTranslation().add(add));
    }

    public float getDamping() {
        return damping;
    }

    public void setDamping(float damping) {
        this.damping = damping;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getStiffness()
    {
        if (desiredPositionStiffnessTimeCounter < desiredPositionStiffnessTimeLength)
            return stiffness * desiredPositionStiffnessScalar;
        return stiffness;
    }

    public void setStiffness(float stiffness) {
        this.stiffness = stiffness;
    }

    public float getLookAtDamping() {
        return lookAtDamping;
    }

    public void setLookAtDamping(float lookAtDamping) {
        this.lookAtDamping = lookAtDamping;
    }

    public float getLookAtMass() {
        return lookAtMass;
    }

    public void setLookAtMass(float lookAtMass) {
        this.lookAtMass = lookAtMass;
    }

    public float getLookAtStiffnes() {
        if (lookAtStiffnessTimeCounter < lookAtStiffnessTimeLength)
            return lookAtStiffnes * lookAtStiffnessScalar;
        return lookAtStiffnes;
    }

    public void setLookAtStiffnes(float lookAtStiffnes) {
        this.lookAtStiffnes = lookAtStiffnes;
    }

    @Override
    public void setCameraPosition(Vector3f position) {
        transform.setTranslation(position);
    }

    @Override
    public void setCameraTransform(PMatrix transform) {
        this.transform.set(transform);
    }

    public void setCameraTransform(float[] floats) {
        transform.set(floats);
        transform.normalize();
    }

    @Override
    public PMatrix getCameraTransform() {
        return transform;
    }

}
