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
package imi.tests;

import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.scene.PScene;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;
import org.jdesktop.wonderland.common.comms.WonderlandObjectOutputStream;

/**
 * Serialize the skeleton!
 * @author Ronald E Dahlgren
 */
public class SkeletonNodeSerialization extends DemoBase
{
    private final static Logger logger = Logger.getLogger(SkeletonNodeSerialization.class.getName());

    private static URL MaleSkeletonLocation = null;
    private static URL FemaleSkeletonLocation = null;
    private static File MaleOutputFile = new File("src/imi/character/skeleton/Male.bs");
    private static File FemaleOutputFile = new File("src/imi/character/skeleton/Female.bs");
    /** Relative paths to animation files to load onto the skeletons. **/
    private static String[] FemaleAnimationLocations = {
        "assets/models/collada/Avatars/Female/Female_Anim_Idle.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_Sitting.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_StandToSit.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_Walk.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_Run.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_Wave.dae"
    };
    private static String[] FemaleFacialAnimations = {

    };
    private static String[] MaleAnimationLocations = {
        "assets/models/collada/Avatars/Male/Male_Anim_Bow.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Cheer.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Clap.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_FallFromSitting.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_FloorGetup.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_FloorSitting.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Follow.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Idle.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Jump.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Laugh.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Run.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Sitting.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_StandToSit.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Walk.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Wave.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_No.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Yes.dae",
        "assets/models/collada/Avatars/Male/Male_Anim_Cell.dae",
    };
    private static String[] MaleFacialAnimationLocations = {
        "assets/models/collada/Avatars/MaleFacialAnimation/MaleSmile.dae",
        "assets/models/collada/Avatars/MaleFacialAnimation/MaleFrown.dae",
        "assets/models/collada/Avatars/MaleFacialAnimation/MaleScorn.dae",
        "assets/models/collada/Avatars/MaleFacialAnimation/MaleDefault.dae",
    };

    /** URL preamble **/
    private static String URLPreamble = "file://localhost/" + System.getProperty("user.dir") + "/";
    /** Important state for base skeleton creation **/
    static
    {
        // URL creation
        try {
            MaleSkeletonLocation = new URL(URLPreamble + "assets/models/collada/Avatars/Male/Male_Bind.dae");
            FemaleSkeletonLocation = new URL(URLPreamble + "assets/models/collada/Avatars/Female/Female_Bind.dae");
        }
        catch (MalformedURLException ex)
        {
            logger.severe("Could not initialize static urls to skeletons.");
        }
    }
    
    public SkeletonNodeSerialization(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        SkeletonNodeSerialization worldTest = new SkeletonNodeSerialization(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        createSerializedSkeleton(wm, true); // load the male skeleton
    }

    private void createSerializedSkeleton(WorldManager wm, boolean bLoadMale)
    {
        URL         skeletonLocation = null;
        String[]    animationFiles   = null;
        String[]    facialAnimations = null;
        File        outputFile       = null;

        if (bLoadMale) {
            skeletonLocation = MaleSkeletonLocation;
            animationFiles = MaleAnimationLocations;
            facialAnimations = MaleFacialAnimationLocations;
            outputFile = MaleOutputFile;
        }
        else {
            skeletonLocation = FemaleSkeletonLocation;
            animationFiles = FemaleAnimationLocations;
            facialAnimations = FemaleFacialAnimations;
            outputFile = FemaleOutputFile;
        }

        // Create parameters for the collada loader we will use
        ColladaLoaderParams params = new ColladaLoaderParams(true, false, // load skeleton, load geometry
                                                            false,  false, // load animations, use cache
                                                            false, // show debug output
                                                            4, // max influences per-vertex
                                                            "Skeleton", // 'name'
                                                            null); // existing skeleton (if applicable)
        Collada loader = new Collada(params);
        loader.load(new PScene(wm), skeletonLocation); // Don't need to hold on to the pscen
        SkeletonNode skeleton = loader.getSkeletonNode();
        // Now load it with animations using the InstructionProcessor
        InstructionProcessor processor = new InstructionProcessor(wm);
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
        // now our skeleton is loaded with animation data, time to write it out
        serializeSkeleton(skeleton, outputFile);
    }

    private void serializeSkeleton(SkeletonNode skeleton, File destination)
    {
        FileOutputStream fos = null;
        WonderlandObjectOutputStream out = null;
        try
        {
          fos = new FileOutputStream(destination);
          out = new WonderlandObjectOutputStream(fos);
          out.writeObject(skeleton);
          out.close();
        }
        catch(IOException ex)
        {
            logger.severe("Problem with serializing the skeleton : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private SkeletonNode deserializeSkeleton(URL location)
    {
        SkeletonNode result = null;
        FileInputStream fis = null;
        WonderlandObjectInputStream in = null;

        try
        {
            in = new WonderlandObjectInputStream(location.openStream());
            result = (SkeletonNode)in.readObject();
            in.close();
        }
        catch(Exception ex)
        {
            logger.severe("Uh oh! " + ex.getMessage());
            ex.printStackTrace();
        }

        return result;
    }
}


