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
import imi.loaders.repository.Repository;
import imi.scene.PScene;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.utils.AvatarObjectInputStream;
import imi.utils.AvatarObjectOutputStream;
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
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.RenderBuffer;

/**
 * Serialize the skeleton!
 * @author Ronald E Dahlgren
 */
public class SkeletonNodeSerialization
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
        "assets/models/collada/Avatars/Female/Female_Anim_StandtoSit.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_Walk.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_Run.dae",
        "assets/models/collada/Avatars/Female/Female_Anim_Wave.dae"
    };
    private static String[] FemaleFacialAnimations = {
        "assets/models/collada/Avatars/FemaleFacialAnimation/FemaleSmile.dae",
        "assets/models/collada/Avatars/FemaleFacialAnimation/FemaleFrown.dae",
        "assets/models/collada/Avatars/FemaleFacialAnimation/FemaleScorn.dae",
        "assets/models/collada/Avatars/FemaleFacialAnimation/FemaleDefault.dae",
        "assets/models/collada/Avatars/FemaleFacialAnimation/FemaleDefault.dae"
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
    private static String URLPreamble = "file:///" + System.getProperty("user.dir") + "/";
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

    private final int MinimumNumberArgs = 1;
    
    public SkeletonNodeSerialization(String[] args)
    {
        if (args.length < MinimumNumberArgs)
            printUsage();
        else
        {
            WorldManager wm = new WorldManager("TheWorldManager");
            Repository  repository = new Repository(wm, false, false); // do not load skeletons, do not load use cache
            repository.setLoadGeometry(false);
            // Add the repository
//                repository.setLoadGeometry(false);
            wm.addUserData(Repository.class, repository);
            wm.getRenderManager().setDesiredFrameRate(60);
            createUI(wm);
            if (args[0].equalsIgnoreCase("-m"))
            {
                if (args.length >= 2)
                    MaleOutputFile = new File(args[1]);
                
                createSerializedSkeleton(wm, true);
            }
            else if (args[0].equalsIgnoreCase("-f"))
            {

                if (args.length >= 2)
                    FemaleOutputFile = new File(args[1]);
                createSerializedSkeleton(wm, false);
            }
            else if (args[0].equalsIgnoreCase("-mf"))
            {
                createSerializedSkeleton(wm, false);
                createSerializedSkeleton(wm, true);
            }
            else
                printUsage();
            System.exit(0);
        }

    }

    public static void main(String[] args)
    {
        SkeletonNodeSerialization worldTest = new SkeletonNodeSerialization(args);
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
        // now our skeleton is loaded with animation data, time to write it out
        serializeSkeleton(skeleton, outputFile);
    }

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

    private SkeletonNode deserializeSkeleton(URL location)
    {
        SkeletonNode result = null;
        AvatarObjectInputStream in = null;

        try
        {
            in = new AvatarObjectInputStream(location.openStream());
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

    private void printUsage()
    {
        System.err.println("Usage: <command> (-m | -f) <outputfile>");
        System.err.println("-m : Bake the male skeleton");
        System.err.println("-f : Bake the Female skeleton");
        System.err.println("outputfile : Optionally provide a path to output the skeleton to.");
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


