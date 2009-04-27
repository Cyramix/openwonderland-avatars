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
package imi.utils;

import imi.cache.DefaultAvatarCache;
import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.ColladaLoadingException;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset;
import imi.scene.PScene;
import imi.scene.animation.AnimationCycle;
import imi.scene.animation.AnimationGroup;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javolution.util.FastList;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.RenderBuffer;


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
public class BinaryTool
{
    /** Logger ref**/
    private final static Logger logger = Logger.getLogger(BinaryTool.class.getName());

    /** The male bind pose location **/
    private static URL MaleSkeletonLocation = null;
    /** The female bind pose location **/
    private static URL FemaleSkeletonLocation = null;

    /** Destination for completed skeletons **/
    private static File MaleOutputFile = new File("src/imi/character/skeleton/Male.bs");
    private static File FemaleOutputFile = new File("src/imi/character/skeleton/Female.bs");

    /** Relative paths to animation files to load onto the skeletons. **/
    private static String[] FemaleAnimationLocations = {
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

    private static String[] FemaleFacialAnimations = {
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleSmile.dae",
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleFrown.dae",
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleScorn.dae",
        "assets/models/collada/Animations/FemaleFacialAnimations/FemaleDefault.dae"
    };

    private static String[] MaleAnimationLocations = {
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

    private static String[] MaleFacialAnimationLocations = {
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
    private static String URLPreamble = "file:///" + System.getProperty("user.dir") + "/";
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

    /** Loading flags **/
    private boolean m_bLoadMale = false;
    private boolean m_bLoadFemale = false;
    private boolean m_bBuildCache = false;
    /** Root of the asset hierarchy **/
    private File    m_assetRoot = new File("assets/");
    /** Repository ref **/
    private Repository repository = null;
    /** used to specify the quality of animations after optimization **/
    private float animationQuality = 0.9f;

    /**
     * Create and run the tool.
     * @param args
     */
    public BinaryTool(String[] args)
    {
        WorldManager wm = new WorldManager("TheWorldManager");
        // create a repository to use
        repository = new Repository(wm, false, null); // do not load skeletons, do not load use cache
        repository.setLoadGeometry(false);
        // Add the repository
        wm.addUserData(Repository.class, repository);
        wm.getRenderManager().setDesiredFrameRate(60);
        createUI(wm);
        processArgs(args);

        if (m_bLoadMale)
            createSerializedSkeleton(wm, true);
        if (m_bLoadFemale)
            createSerializedSkeleton(wm, false);
        if (m_bBuildCache)
        {
            // Behave like a normal repository
            repository.clearCache();
            repository.initCache();
            repository.setLoadGeometry(true);
            loadAllFiles(m_assetRoot);
        }
        System.exit(0);
    }

    /**
     * Run the tool
     * @param args
     */
    public static void main(String[] args)
    {
        BinaryTool worldTest = new BinaryTool(args);
    }

    /**
     * Traverse the asset hierarchy and cache all the encountered collada files
     * @param docRoot
     */
    private void loadAllFiles(File docRoot)
    {
        // Make a file filter to use
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File fileVersion = new File(dir, name);
                if (name.endsWith(".dae"))
                    return true;
                else if (fileVersion.isDirectory() && !fileVersion.getName().equalsIgnoreCase("animations"))
                    return true;
                else
                    return false;
            }
        };
        // Scratch references
        File current = null;
        File[] fileList = null;

        // Our queue!
        FastList<File> queue = new FastList<File>();
        queue.add(docRoot);
        while (!queue.isEmpty())
        {
            current = queue.removeFirst();
            if (current.isFile())
            {
                logger.info("Processing " + current.getName());
                loadColladaFile(current);
            }
            else if (current.isDirectory())
            {
                fileList = current.listFiles(filter);
                for (File file : fileList)
                    queue.add(file);
            }
        }
    }

    /**
     * Load the specified collada file
     * @param fileToLoad
     */
    private void loadColladaFile(File fileToLoad)
    {
        if (repository != null)
        {
            AssetDescriptor asset = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA, fileToLoad);
            SharedAsset assetToLoad = new SharedAsset(repository, asset);
            repository.cacheAsset(assetToLoad);
        }
    }

    /**
     * Generate a serialized skeleton
     * @param wm The manager of the world.
     * @param bLoadMale True to load the male skeleton, false for the female.
     */
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
                                                            false,  false, // show debug output
                                                            4, // max influences per-vertex
                                                            "Skeleton", // 'name'
                                                            null); // existing skeleton (if applicable)
        Collada loader = new Collada(params);
        try {
            loader.load(new PScene(wm), skeletonLocation); // Don't need to hold on to the pscen
        }
        catch (ColladaLoadingException ex)
        {
            logger.severe(ex.getMessage());
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
     * Process the command line arguments.
     * @param args
     */
    private void processArgs(String[] args) {
        if (args.length == 0)
            printUsage();
        for (int i = 0; i < args.length; ++i)
        {
            if (args[i].equalsIgnoreCase("-m"))
                m_bLoadMale = true;
            else if (args[i].equalsIgnoreCase("-f"))
                m_bLoadFemale = true;
            else if (args[i].equalsIgnoreCase("-mf"))
                m_bLoadFemale = m_bLoadMale = true;
            else if (args[i].equalsIgnoreCase("-buildCache"))
                m_bBuildCache = true;
            else if (args[i].equalsIgnoreCase("-skellyRoot"))
            {
                String destinationFolder = args[++i];
                MaleOutputFile = new File(destinationFolder, "Male.bs");
                FemaleOutputFile = new File(destinationFolder, "Female.bs");
            }
            else if (args[i].equalsIgnoreCase("-assetRoot"))
            {
                String assetRootFolder = args[++i];
                m_assetRoot = new File(assetRootFolder);
            }
            else if (args[i].equalsIgnoreCase("-animQuality"))
            {
                animationQuality = Float.parseFloat(args[++i]);
            }
        }
    }

    /**
     * Serialize the provided skeleton to the specified destination
     * @param skeleton
     * @param destination
     */
    private void serializeSkeleton(SkeletonNode skeleton, File destination)
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

    /**
     * Show the usage of this utility
     */
    private void printUsage()
    {
        System.err.println("Usage: <command> (-m | -f) -o outputfile");
        System.err.println("-m : Bake the male skeleton");
        System.err.println("-f : Bake the Female skeleton");
        System.err.println("-skellyOut : Specify output folder for skeletons");
        System.err.println("-buildCache : regenerate the compete cache ");
        System.err.println("-assetRoot : Specify root folder for asset digestion");
        System.err.println("-animQuality : specify a (normalized) float for animation optimizations. ");
    }

    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {
        SwingFrame frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame with focus
        frame.canvas.requestFocusInWindow();

        frame.setVisible(true);

        // Add to the wm to set title string later during debugging
        wm.addUserData(JFrame.class, frame);
    }

    public class SwingFrame extends JFrame implements FrameRateListener, ActionListener {

        JPanel contentPane;
        JPanel canvasPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        Canvas canvas = null;
        JLabel fpsLabel = new JLabel("FPS: ");
        RenderBuffer m_renderBuffer = null;


        // Construct the frame
        public SwingFrame(WorldManager wm) {
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());

            // The Menu Bar
            JMenuBar menuBar = new JMenuBar();

            // File Menu
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);

            // Create Menu
            JMenu createMenu = new JMenu("Create");
            menuBar.add(createMenu);

            // The Rendering Canvas
            m_renderBuffer = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, 1, 1);
            wm.getRenderManager().addRenderBuffer(m_renderBuffer);
            canvas = m_renderBuffer.getCanvas();
            canvas.setVisible(true);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);

            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(fpsLabel);
            contentPane.add(statusPanel, BorderLayout.SOUTH);

            pack();
        }

        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }

        public void actionPerformed(ActionEvent e)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public RenderBuffer getRenderBuffer()
        {
            return m_renderBuffer;
        }
    }
}


