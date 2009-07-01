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

import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import java.io.File;
import java.util.logging.Logger;

/**
 * This class is for specifying the attributes of a unimesh avatar.
 * @author Ronald E Dahlgren
 */
public class UnimeshCharacterParams extends CharacterParams
{
    private static final Logger logger = Logger.getLogger(UnimeshCharacterParams.class.getName());
    private static final String UnimeshGeometryName = "H_DDS_LowResShape";

    /**
     * Enumerated unimesh sexes ftw!
     */
    public enum Sex {
        Male(1),
        Female(2),
        Indeterminate(0),
        Hybrid(3);

        int value;
        Sex(int value) {
            this.value = value;
        }
    }

    /** This is the location of the body hull to use **/
    private final String unimeshBodyLocation;
    /** The sex of this avatar **/
    private Sex sex = Sex.Indeterminate;
    /** Used for configuring the skeleton properly **/
    private final UnimeshInitializer    initializer = new UnimeshInitializer();
    /** The skeleton that the file provided for us. **/
    private SkeletonNode modifiedSkeleton = null;

    /**
     * Construct attributes for a new unimesh avatar!
     * @param name
     * @param unimeshLocation
     * @param sex
     */
    public UnimeshCharacterParams(String name, String unimeshLocalPath, Sex sex)
    {
        if (unimeshLocalPath == null)
            throw new IllegalArgumentException("Must provide a valid unimesh location!");
        if (name == null)
            throw new IllegalArgumentException("Null name provided!");
        if (sex == null)
            throw new IllegalArgumentException("Null sex? Use indeterminate instead!");

        super.setInitializationObject(initializer);
        super.setName(name);
        this.sex = sex;
        unimeshBodyLocation = unimeshLocalPath;
    }

    /**
     * Set the gender of this unimesh avatar
     * @param sex A non-null sex enum, use indeterminate for unspecified.
     * @throws IllegalArgumentException If {@code sex == null}
     */
    public UnimeshCharacterParams setSex(Sex sex)
    {
        if (sex == null)
            throw new IllegalArgumentException("Null sex provided, use indeterminate!");
        this.sex = sex;
        return this;
    }
    
    UnimeshCharacterParams setModifiedSkeleton(SkeletonNode skeleton)
    {
        if (skeleton == null)
            throw new IllegalArgumentException("Null skeleton provided!");
        modifiedSkeleton = skeleton;
        return this;
    }

    /**
     * Set the location of the file to be loaded
     * @param location A non-null path
     */
    public UnimeshCharacterParams setUnimeshPath(String localPath)
    {
        if (localPath == null)
            throw new IllegalArgumentException("Null path provided!");

        clearLoadInstructions();
        clearSkinnedMeshParams();
        this.addLoadInstruction(localPath)
            .addSkinnedMeshParams(new SkinnedMeshParams(UnimeshGeometryName, "FullBody", localPath));
        return this;
  

    }

    /**
     * Retrieve the local path to the unimesh.
     * @return A non-null relative path
     */
    public String getUnimeshPath()
    {
        return this.unimeshBodyLocation;
    }

    /**
     * {@inheritDoc CharacterParams}
     */
    @Override
    public CharacterInitializationInterface getInitializationObject() {
        return super.getInitializationObject();
    }

    /**
     * {@inheritDoc CharacterParams}
     */
    @Override
    public CharacterParams setInitializationObject(CharacterInitializationInterface initializationObject) {
        initializer.customInitializer = initializationObject;
        return this;
    }


    /**
     * {@inheritDoc CharacterParams}
     */
    @Override
    public boolean isMale() {
        boolean result = false;
        switch (sex)
        {
            case Female:
                result = false;
                break;
            case Hybrid:
                result = true; // A little of both
                break;
            case Indeterminate:
                result = false; // neither
                break;
            case Male:
                result = true;
                break;
            default:
                logger.warning("Unidentified sex encountered! (Weird...)");

        }
        return result;
    }

    /**
     * Special initialization codesssss
     */
    private class UnimeshInitializer implements CharacterInitializationInterface
    {
        /** Provide a slot for user requested initialization **/
        CharacterInitializationInterface customInitializer = null;

        @Override
        public void initialize(Character character) {
            // do the config thing
            configureAvatar(character);
            if (customInitializer != null)
                customInitializer.initialize(character);
        }
    }

    private void configureAvatar(Character character)
    {
        SkeletonNode characterSkeleton = character.getSkeleton();

        for (SkinnedMeshJoint smj : modifiedSkeleton.getSkinnedMeshJoints())
        {
            SkinnedMeshJoint originalJoint = characterSkeleton.getSkinnedMeshJoint(smj.getName());
            if (originalJoint == null)
                logger.warning("Could not find a matching joint for " + smj.getName());
            else
                originalJoint.getBindPoseRef().set(smj.getBindPoseRef());
        }
        // refresh the skeleton
        // go through each mesh and nullify the cached inverse bind pose reference
        characterSkeleton.invalidateCachedBindPoses();
        characterSkeleton.refresh();
    }
}
