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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is for specifying the attributes of a unimesh avatar.
 * @author Ronald E Dahlgren
 */
public class UnimeshCharacterAttributes extends CharacterAttributes
{
    private static final Logger logger = Logger.getLogger(UnimeshCharacterAttributes.class.getName());
    private static final String UnimeshGeometryName = "H_DDS_LowResShape";
    public enum Sex {
        Male,
        Female,
        Indeterminate,
        Hybrid,
    }

    /** This is the location of the body hull to use **/
    protected String unimeshBodyLocation   = null;
    /** The sex of this avatar **/
    protected Sex sex                   = Sex.Indeterminate;
    /** Used for configuring the skeleton properly **/
    private UnimeshInitializer    initializer = new UnimeshInitializer();

    /**
     * Construct attributes for a new unimesh avatar!
     * @param name
     * @param unimeshLocation
     * @param sex
     */
    public UnimeshCharacterAttributes(String name, String unimeshLocalPath, Sex sex)
    {
        if (unimeshLocalPath == null)
            throw new IllegalArgumentException("Must provide a valid unimesh location!");
        super.setInitializationObject(initializer);
        setName(name);
        setSex(sex);
        setUnimeshPath(unimeshLocalPath);
    }

    /**
     * Set the gender of this unimesh avatar
     * @param sex
     */
    public void setSex(Sex sex)
    {
        this.sex = sex;
    }

    private final List<String[]> loadInstructions       = new ArrayList<String[]>();
    private final SkinnedMeshParams[] addInstructions   = new SkinnedMeshParams[1];
    /**
     * Set the location of the file to be loaded
     * @param location
     */
    public void setUnimeshPath(String localPath)
    {
        this.unimeshBodyLocation = localPath;

        loadInstructions.clear();
        loadInstructions.add(new String[] { localPath, "FullBody"});
        setLoadInstructions(loadInstructions);
        
        
        addInstructions[0] = new SkinnedMeshParams(UnimeshGeometryName, "FullBody");
        setAddInstructions(addInstructions);

    }

    /**
     * Retrieve the local path to the unimesh.
     * @return
     */
    public String getUnimeshPath()
    {
        return this.unimeshBodyLocation;
    }

    /**
     * Retrieve the initialization object.
     * @return
     */
    @Override
    public InitializationInterface getInitializationObject() {
        return initializer.customInitializer;
    }

    /**
     * Set the initialization object.
     * @param initializationObject
     */
    @Override
    public void setInitializationObject(InitializationInterface initializationObject) {
        initializer.customInitializer = initializationObject;
    }

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


    private class UnimeshInitializer implements InitializationInterface
    {
        /** Provide a slot for user requested initialization **/
        InitializationInterface customInitializer = null;

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
        String urlPrefix = this.getBaseURL();
        // If no base url was provided by the character attributes, then it is
        // assumed that the prefix should be the file protocol to the local machine
        // in the current folder.
        if (urlPrefix == null || urlPrefix.length() == 0)
            urlPrefix = new String("file:///" + System.getProperty("user.dir") + File.separatorChar);
        // TODO :
    }
}
