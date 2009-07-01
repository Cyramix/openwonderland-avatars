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
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.SkeletonNode;
import imi.scene.animation.AnimationCycle;
import imi.scene.animation.AnimationGroup;
import imi.scene.polygonmodel.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class generates binary head file (*.bhf) packs. These packs are composed
 * of facial animations, a unique skeleton for the head, and any attached geometry.
 * @author Paul Viet Nguyen Truong
 * @author Ronald E Dahlgren
 */
public class BinaryHeadExporter {
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(BinaryHeadExporter.class.getName());
    
    /**
     * Create a binary head file using the provided parameters.
     * <p>The provided parameters must be non-null and have valid parameters.</p>
     * @param wm
     * @throws IOException
     */
    public static void createBinaryHeadFile(BinaryHeadExporterParams params) throws IOException
    {
        // Create parameters for the collada loader we will use
        ColladaLoaderParams colladaParams = new ColladaLoaderParams.Builder()
                                                .setLoadSkeleton(true)
                                                .setLoadGeometry(true)
                                                .setName("Skeleton")
                                                .build();
        // Load the skeleton
        Collada loader              = new Collada(colladaParams);
        PScene pscene               = new PScene(params.wm);
        // try loading it
        try {
            loader.load(pscene, params.headSkeletonLocation); // IOException if null or invalid
        }
        catch (ColladaLoadingException ex)
        {
            logger.severe(ex.getMessage());
            throw new RuntimeException("Unable to load collada file: " + params.headSkeletonLocation, ex);
        }
        SkeletonNode loadedSkeleton = loader.getSkeletonNode();
        if (loadedSkeleton == null)
            throw new RuntimeException("No skeleton loaded from file: " + params.headSkeletonLocation);

        loadedSkeleton.setName(params.outputFile.getName()); // NPE on file if null

        // Create animation instructions & processor
        InstructionProcessor processor = new InstructionProcessor(params.wm);
        processor.setUseBinaryFiles(false);
        Instruction animationInstruction = new Instruction(Instruction.InstructionType.grouping);
        animationInstruction.addChildInstruction(Instruction.InstructionType.setSkeleton, loadedSkeleton);    // Set the m_currentSkeleton we just got

        for (String anim : params.facialAnimationFilePaths)
            animationInstruction.addChildInstruction(Instruction.InstructionType.loadFacialAnimation, params.pathPrefix + anim);
        processor.execute(animationInstruction);    // execute the processes

        // Optimize all of the cycles
        for (AnimationGroup group : loadedSkeleton.getAnimationComponent().getGroups())
            for (AnimationCycle cycle : group.getCycles())
                cycle.optimizeChannels(params.animQuality);

        // Get all the SkinnedMeshInstances & place it in the head subgroup
        for (PPolygonSkinnedMeshInstance meshInst : loadedSkeleton.getSkinnedMeshInstances()) {
            meshInst.setAndLinkSkeletonNode(loadedSkeleton);
            loadedSkeleton.addToSubGroup(meshInst, "Head");
        }

        // Now for the skinned meshes
        List<PPolygonSkinnedMesh> ppsmList = loadedSkeleton.getAllSkinnedMeshes();
        for (PPolygonSkinnedMesh skinnedMesh : ppsmList) {
            PPolygonSkinnedMeshInstance meshInst =
                    (PPolygonSkinnedMeshInstance) pscene.addMeshInstance(skinnedMesh, new PMatrix());
            meshInst.setAndLinkSkeletonNode(loadedSkeleton);
            loadedSkeleton.addToSubGroup(meshInst, "Head");
        }

        FileOutputStream fos            = null;
        AvatarObjectOutputStream out    = null;

        fos = new FileOutputStream(params.outputFile);
        out = new AvatarObjectOutputStream(fos);
        out.writeObject(loadedSkeleton);
        out.close();
    }

    /**
     * This class is to ease the burden on the client for parameterizing the exporter.
     */
    public static class BinaryHeadExporterParams {
        // Animation files to load
        private final List<String> facialAnimationFilePaths = new FastList<String>();
        // Location of the COLLADA file with the head skeleton
        private URL headSkeletonLocation = null;
        // WorldManager reference
        private final WorldManager wm;
        // Where the output will be dumped
        private File outputFile = new File("outputHead.bhf");
        // The prefix for all paths
        private String pathPrefix = "file:///" + System.getProperty("user.dir") + File.separatorChar;
        // Animation compression ratio
        private float animQuality = 0.9f;

        /**
         * Construct a new BinaryHeadExporterParams with the provided WorldManager.
         * @param wm A non-null WorldManager reference
         */
        public BinaryHeadExporterParams(WorldManager wm) {
            if (wm == null)
                throw new IllegalArgumentException("Null WorldManager given!");
            this.wm = wm;
        }


        /**
         * Add the specified animation to the list of animations to load.
         * @param animation A non-null relative path to an animation
         * @return this
         * @throws IllegalArgumentException If {@code animation == null}
         */
        public BinaryHeadExporterParams addAnimationFile(String animation) {
            if (animation == null)
                throw new IllegalArgumentException("Null animation provided!");
            facialAnimationFilePaths.add(animation);
            return this;
        }

        /**
         * Clear the existing contents of the internal list and fill it with the
         * provided collection's contents.
         * @param animations A non-null collection of non-null relative animation paths
         * @return this
         * @throws IllegalArgumentException If any element in animations is null
         * @throws NullPointerException If animations == null
         */
        public BinaryHeadExporterParams setAnimationFiles(Iterable<String> animations) {
            facialAnimationFilePaths.clear();
            for (String animation : animations) // NPE if null
            {
                if (animation == null)
                    throw new IllegalArgumentException("Null string encountered!");
                else
                    facialAnimationFilePaths.add(animation);
            }
            return this;

        }

        /**
         * Set the output file to the provided one.
         * @param out The file to output to
         * @return this
         * @throws IllegalArgumentException If file == null
         */
        public BinaryHeadExporterParams setOutputFile(File out) {
            if (out == null)
                throw new IllegalArgumentException("Null output file provided!");
            this.outputFile = out;
            return this;
        }

        /**
         * Set the location of the head file (with the skeleton to be used)
         * @param location A non-null URL
         * @return this
         * @throws IllegalArgumentException If location == null
         */
        public BinaryHeadExporterParams setHeadLocation(URL location) {
            if (location == null)
                throw new IllegalArgumentException("Null URL provided");
            this.headSkeletonLocation = location;
            return this;
        }

        /**
         * Set the path prefix for animation files.
         * <p>The default value for this is "file:///(user.dir)</p>
         * @param prefix A non-null prefix string
         * @return this
         * @throws  IllegalArgumentException If prefix == null
         */
        public BinaryHeadExporterParams setPathPrefix(String prefix) {
            if (prefix == null)
                throw new IllegalArgumentException("Null prefix provided!");
            pathPrefix = prefix;
            return this;
        }

        /**
         * Sets the animation quality control factor.
         * <p>This factor is used when optimizing animation data. It must be
         * a normalized positive float (0.0f - 1.0f). The default value is
         * 0.9</p>
         * @param factor
         * @return
         */
        public BinaryHeadExporterParams setAnimationQualityFactor(float factor) {
            if (    Float.compare(factor, 0f) < 0 ||
                    Float.compare(factor, 1f) > 0)
                throw new IllegalArgumentException(factor + " is not normalized.");
            this.animQuality = factor;
            return this;
        }
    }
}
