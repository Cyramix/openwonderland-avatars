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
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.utils.PMathUtils;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 *
 * @author Lou Hayt
 */
public class CharacterProcessor extends ProcessorComponent
{
    private float deltaTime = 1/60.0f;//0.0f;
    private double oldTime = 0.0f;
    
    private imi.character.Character character = null;
    
    public CharacterProcessor(imi.character.Character person)
    {
        character = person;
        setRunInRenderer(true);
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) {
//        double newTime = System.nanoTime() / 1000000000.0;
//        deltaTime = (float) (newTime - oldTime);
//        oldTime = newTime;
        // look at the origin you creepy eyeballs!
        //performEyeballLookAt(Vector3f.ZERO); //<-- not yet functional, need some inversion or something
    }

    @Override
    public void commit(ProcessorArmingCollection collection) {
        // The new sweet way
//        character.update(deltaTime);
        // The old lame way
        character.update(deltaTime);
    }

    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }

    private void performEyeballLookAt(Vector3f targetInWorldSpace)
    {
        // ensure that we have a character, and that the character has a skeleton
        if (character == null || character.getSkeleton() == null)
            return; // try again later
        // grab the appropriate joints to look at
        final String leftEyeballJointName = "leftEye";
        final String rightEyeballJointName = "rightEye";

        SkinnedMeshJoint leftEyeJoint = character.getSkeleton().findSkinnedMeshJoint(leftEyeballJointName);
        SkinnedMeshJoint rightEyeJoint = character.getSkeleton().findSkinnedMeshJoint(rightEyeballJointName);

        // Perform lookAt to target
        // Left eyeball
        PMatrix leftEyeWorldXForm = PMathUtils.lookAt(
                targetInWorldSpace,
                leftEyeJoint.getTransform().getWorldMatrix(false).getTranslation(),
                Vector3f.UNIT_Y);
        leftEyeJoint.getTransform().getWorldMatrix(true).set(leftEyeWorldXForm);
        leftEyeJoint.getTransform().setDirtyWorldMat(false);
        // Right eyeball
        PMatrix rightEyeWorldXForm = PMathUtils.lookAt(
                targetInWorldSpace,
                rightEyeJoint.getTransform().getWorldMatrix(false).getTranslation(),
                Vector3f.UNIT_Y);
        rightEyeJoint.getTransform().getWorldMatrix(true).set(rightEyeWorldXForm);
        rightEyeJoint.getTransform().setDirtyWorldMat(false);
    }

}
