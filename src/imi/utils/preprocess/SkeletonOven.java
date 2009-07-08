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
package imi.utils.preprocess;

import imi.utils.*;
import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.loaders.Collada;
import imi.loaders.ColladaLoaderParams;
import imi.loaders.ColladaLoadingException;
import imi.repository.Repository;
import imi.scene.PScene;
import imi.scene.animation.AnimationCycle;
import imi.scene.SkeletonNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;


/**
 * This class is used to generate various binary files. The following command
 * line arguments are used:
 * -m : load the male binary skeleton
 * -f : load the female binary skeleton
 * -skellyOut : Specify a directory to output the skeleton files
 * -buildCache : regenerate the complete binary cache
 * -assetRoot : specify a non-standard asset root folder
 * -animQuality : specify a quality coefficient for animation compression (normalized float)
 * @author Ronald E Dahlgren
 */
public class SkeletonOven
{
    /**
     * Differentiate the different types of skeletons.
     */
    public enum SkeletonType {
        HumanMaleAvatar,
        HumanFemaleAvatar
    }

    /** Logger ref**/
    private static final Logger logger = Logger.getLogger(SkeletonOven.class.getName());

    /** The male bind pose location **/
    private static URL MaleSkeletonLocation = null;
    /** The female bind pose location **/
    private static URL FemaleSkeletonLocation = null;

    /** Destination for completed skeletons (Defaults)**/
    private static final File DefaultMaleOutputFile = new File("src/imi/character/data/Male.bs");
    private static final File DefaultFemaleOutputFile = new File("src/imi/character/data/Female.bs");

    /** Destination variables**/
    private static File MaleOutputFile = DefaultMaleOutputFile;
    private static File FemaleOutputFile = DefaultFemaleOutputFile;

    /** Relative paths to animation files to load onto the skeletons. **/
    private static final String[] FemaleAnimationLocations = {
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_AnswerCell.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Bow.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Cell.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Cheer.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Clap.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_FallFromSitting.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_FloorGetup.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_FloorSitting.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Follow.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Idle.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Laugh.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_No.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_PublicSpeaking.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_RaiseHand.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_RaiseHandIdle.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Run.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Sitting.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_StandtoSit.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Walk.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Wave.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Yes.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_SittingRaiseHand.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_SittingRaiseHandIdle.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_Rotate.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_StrafeLeft.dae",
        "assets/models/collada/Animations/FemaleBodyAnimations/Female_Anim_StrafeRight.dae",
    };

    private static final String[] FemaleFacialAnimations = {
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleSmile.dae",
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleFrown.dae",
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleScorn.dae",
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleDefault.dae"
    };

    private static final String[] MaleAnimationLocations = {
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_ActiveIdle.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_AnswerCell.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Cell.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Bow.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Cheer.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Clap.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_FallFromSitting.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_FloorGetup.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_FloorSitting.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Follow.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Idle.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Jump.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Laugh.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_No.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Yes.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_PublicSpeaking.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Run.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_ShakeHands.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Sitting.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_StandToSit.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_TakeDamage.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Walk.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Wave.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_RaiseHand.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_RaiseHandIdle.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_SittingRaiseHand.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_SittingRaiseHandIdle.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_Rotate.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_StrafeLeft.dae",
        "assets/models/collada/Animations/MaleBodyAnimations/Male_Anim_StrafeRight.dae",
    };

    private static final String[] MaleFacialAnimationLocations = {
        "assets/models/collada/Animations/MaleFacialAnimations/MaleSmile.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/MaleFrown.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/MaleScorn.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/MaleDefault.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_AI.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_Cons.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_E.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_FandV.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_L.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_MBP.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_O.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_U.dae",
        "assets/models/collada/Animations/MaleFacialAnimations/Phonemes/Male_Pho_WQ.dae",
    };

    /** URL preamble **/
    private static final String URLPreamble = "file:///" + System.getProperty("user.dir") + "/";

    /** Important state for base skeleton creation **/
    static
    {
        // URL creation
        try {
            MaleSkeletonLocation = new URL(URLPreamble + "assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae");
            FemaleSkeletonLocation = new URL(URLPreamble + "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae");
        }
        catch (MalformedURLException ex)
        {
            logger.severe("Could not initialize static urls to skeletons.");
        }
    }

    /** Disabled **/
    private SkeletonOven(){}

    /**
     * Bake the skeletons for the male and female avatars.
     * <p>To build the male skeleton, specify a '-m' command line argument.
     * To build the female skeleton, specify a '-f' command line argument. 
     * Additionally, '-mf' may be used to build both skeletons. Finally,
     * '-quality:float' is allowed where float is a normalized position
     * float. This will specify the compression ratio to use.</p>
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        boolean bLoadMale = false;
        boolean bLoadFemale = false;
        float animationQuality = 0.9f;

        // Parse the command line args
        for (String arg : args) {
            if (arg.equals("-m"))
                bLoadMale = true;
            else if (arg.equals("-f"))
                bLoadFemale = true;
            else if (arg.equals("-mf") || arg.equals("-all"))
                bLoadFemale = bLoadMale = true;
            else if (arg.startsWith("-quality"))
                animationQuality = Float.parseFloat(arg.split(":")[1]);
        }

        WorldManager wm = new WorldManager("TheWorldManager");
        // create a repository to use
        Repository repository = new Repository(wm, false, null); // do not load skeletons, do not load use cache
        repository.setToolMode(true);
        // Add the repository
        wm.addUserData(Repository.class, repository);

        if (bLoadMale)
            createSerializedSkeleton(wm, SkeletonType.HumanMaleAvatar, animationQuality);
        if (bLoadFemale)
            createSerializedSkeleton(wm, SkeletonType.HumanFemaleAvatar, animationQuality);
        System.exit(0);
    }




    /**
     * Create a new serialized skeleton of the specified type with the  specified
     * animation quality.
     * @param wm A world manager
     * @param type The type of skeleton to make
     * @param animationQuality Compression quality
     */
    public static void createSerializedSkeleton(WorldManager wm, SkeletonType type, float animationQuality)
    {
        if (Float.compare(animationQuality, 0f) < 0 || Float.compare(animationQuality, 1f) > 0)
            throw new IllegalArgumentException(animationQuality + " is not normalized.");
        if (wm == null || type == null)
            throw new IllegalArgumentException("Null param!");
        // Get the repository and tell it not to load geometry
        Repository repo = (Repository) wm.getUserData(Repository.class);
        repo.setLoadGeometry(false); // Must be so!
        if (repo == null)
            throw new IllegalStateException("The WorldManager must have a Repository " +
                                            "(as user data) to continue.");

        URL         skeletonLocation;
        String[]    animationFiles;
        String[]    facialAnimations;
        File        outputFile;

        switch (type)
        {
            case HumanMaleAvatar:
                skeletonLocation = MaleSkeletonLocation;
                animationFiles = MaleAnimationLocations;
                facialAnimations = MaleFacialAnimationLocations;
                outputFile = MaleOutputFile;
                break;
            case HumanFemaleAvatar:
                skeletonLocation = FemaleSkeletonLocation;
                animationFiles = FemaleAnimationLocations;
                facialAnimations = FemaleFacialAnimations;
                outputFile = FemaleOutputFile;
                break;
            default:
                throw new IllegalArgumentException("Unsupported SkeletonType: " + type);
        }

        // Create parameters for the collada loader we will use
        ColladaLoaderParams params = new ColladaLoaderParams.Builder()
                                            .setLoadGeometry(false)
                                            .setLoadSkeleton(true)
                                            .setName(type.name() + "Skeleton")
                                            .build();
        Collada loader = new Collada(params);
        try {
            loader.load(new PScene(wm), skeletonLocation); // Don't need to hold on to the pscene
        } catch (ColladaLoadingException ex) {
            logger.severe(ex.getMessage());
            throw new RuntimeException("Unable to load skeleton.", ex);
        } catch (IOException ex) {
            logger.severe("Unable to load skeleton (IOException!): " + ex.getMessage());
            throw new RuntimeException("Unable to load skeleton.", ex);
        }
        SkeletonNode skeleton = loader.getSkeletonNode();
        skeleton.refresh();
        // Now load it with animations using the InstructionProcessor
        InstructionProcessor processor = new InstructionProcessor(wm);
        processor.setUseBinaryFiles(false); // Reduce complexity
        Instruction animationInstruction = new Instruction(); // Grouping instruction node
        // Load in the skeleton
        animationInstruction.addChildInstruction(Instruction.InstructionType.setSkeleton, skeleton);
        // Body animations
        for (String filePath : animationFiles)
            animationInstruction.addChildInstruction(Instruction.InstructionType.loadAnimation, URLPreamble + filePath);
        // Facial animations
        for (String filePath : facialAnimations)
            animationInstruction.addChildInstruction(Instruction.InstructionType.loadFacialAnimation, URLPreamble + filePath);
        // Execute it
        processor.execute(animationInstruction);
        
        // optimize all of those cycles
        for (AnimationCycle cycle : skeleton.getAnimationGroup(0).getCycles())
            cycle.optimizeChannels(animationQuality);
        for (AnimationCycle cycle : skeleton.getAnimationGroup(1).getCycles())
            cycle.optimizeChannels(animationQuality);
        // now our skeleton is loaded with animation data, time to write it out
        serializeSkeleton(skeleton, outputFile);
    }

    /**
     * Serialize the provided skeleton to the specified destination
     * @param skeleton
     * @param destination
     */
    private static void serializeSkeleton(SkeletonNode skeleton, File destination)
    {
        FileOutputStream fos = null;
        AvatarObjectOutputStream out = null;
        try
        {
          fos = new FileOutputStream(destination);
          out = new AvatarObjectOutputStream(fos);
          out.writeObject(skeleton);
          out.close();
        }
        catch(IOException ex)
        {
            logger.severe("Problem with serializing the skeleton : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}


