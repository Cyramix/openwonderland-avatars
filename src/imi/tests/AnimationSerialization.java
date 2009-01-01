/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.tests;

import imi.loaders.collada.Instruction;
import imi.loaders.collada.InstructionProcessor;
import imi.scene.animation.AnimationGroup;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * Testing of animation serialization and deserialization
 * @author Ronald E Dahlgren
 */
public class AnimationSerialization
{
    private static final Logger logger = Logger.getLogger(AnimationSerialization.class.getName());
    private static WorldManager wm = new WorldManager("TheWorld");

    public static void main(String[] args)
    {
        AnimationSerialization as = new AnimationSerialization(args);
    }

    private AnimationSerialization(String[] args)
    {
        URL path = null;
        URL output = null;
        logger.info("Creating path URLs...");
        try
        {
            path = new URL("file://localhost/work/IMI/sunSVN/assets/models/collada/Avatars/Male/Male_Anim_FloorSitting.dae");
            output = new URL("file://localhost/work/IMI/sunSVN/assets/models/collada/Avatars/Male/Male_Anim_FloorSitting.baf");
        }
        catch (MalformedURLException ex)
        {
            // blah blah
            logger.severe("URL was messed up! " + ex.getMessage());
            ex.printStackTrace();
        }
        logger.info("Success creating URLs.");

        AnimationGroup group = null;

        logger.info("Loading animation file");
        group = loadAnimation(path);
        if (group == null)
        {
            logger.severe("FAILED TO LOAD ANIMATION! ABORTING!");
            System.exit(-1);
        }
        group.calculateDuration();
        logger.info("Group: " + group.toString());

        /************************************************
         * To create the binary file
         ************************************************/
//        logger.info("Converting to binary.");
//        convertToBinary(group, output);
//        logger.info("Success, exiting");

        /************************************************
         *  to load the binary file and compare the two
         ************************************************/
        logger.info("Deserializing into another group.");
        AnimationGroup restoredGroup = deserializeBinary(output);
        logger.info("Loaded group: " + group.toString());
        logger.info("Deserialized group: " + restoredGroup.toString());

        if (restoredGroup.equals(group))
            logger.info("Success!");
        else
        {
            logger.info("Failure!");
            // Output more detailed info?
        }

        /********************************
         * Kill all those stray threads
         ********************************/
        System.exit(0);
    }

    private AnimationGroup loadAnimation(URL pathToFile)
    {
        AnimationGroup result = null;
        /**
         * Load the old animation file up
         */
        SkeletonNode receivingSkeleton = new SkeletonNode("AnimationCarrier");
        receivingSkeleton.getAnimationComponent().getGroups().add(new AnimationGroup("BufferGroup"));
        Instruction loadAnimationInstruction = new Instruction();
        loadAnimationInstruction.addChildInstruction(Instruction.InstructionType.setSkeleton, receivingSkeleton);
        loadAnimationInstruction.addChildInstruction(Instruction.InstructionType.loadAnimation, pathToFile);
        // Execute these
        InstructionProcessor ip = new InstructionProcessor(wm);
        ip.execute(loadAnimationInstruction);
        // Now receivingSkeleton has the new animation group
        result = receivingSkeleton.getAnimationGroup(); // Index zero by default
        return result;

    }
    private void convertToBinary(AnimationGroup groupToSerialize, URL pathForBinary)
    {
        
        /**
         * Serialize the newly created animation group
         */
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(pathForBinary.getFile());
            out = new ObjectOutputStream(fos);
            out.writeObject(groupToSerialize);
            out.close();
        }
        catch(IOException ex)
        {
            logger.severe("Caught exception while trying to serialize the animation group");
            ex.printStackTrace();
        }
    }

    private AnimationGroup deserializeBinary(URL pathToBinaryFile)
    {
        AnimationGroup result = null;


        FileInputStream fis = null;
        ObjectInputStream in = null;
        try
        {
            fis = new FileInputStream(pathToBinaryFile.getFile());
            in = new ObjectInputStream(fis);
            result = (AnimationGroup)in.readObject();
            in.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        catch(ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
        
        return result;
    }
}


