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

import imi.repository.Repository;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.OnscreenRenderBuffer;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class exports the different sets of available heads. It supports a couple
 * of command line args. First, use "-all" to specify that all types should be
 * created. Additionally, a specific type may be specified by "-type:HeadType" where
 * HeadType is a valid string representation of a member of the HeadTypes enum.
 * @author ptruong
 * @author Ronald E Dahlgren
 */
public final class AvatarBinaryHeadExporter {
    public enum HeadTypes {
        DefaultMale,
        DefaultFemale,
        FaceGenMale,
        FaceGenFemale
        // TODO : FaceGen separation
    }
    // Logger ref
    private static final Logger logger = Logger.getLogger(AvatarBinaryHeadExporter.class.getName());

    ////////////////////////////////////////////////////////
    //////////////// Asset lists        ////////////////////
    ////////////////////////////////////////////////////////

    private static final String     saveFolder              = "assets/models/collada/Heads/Binary/";
    private static final String     maleRelPath             = "assets/models/collada/Heads/MaleHead/";
    private static final String[]   maleFGHeads               = new String[] {
                                                                             "FG_Obama_HeadMedPoly.dae",
                                                                             "FG_MaleLowPoly_01.dae",
                                                                             "FG_MaleHead02Medium.dae",
                                                                             "FG_Male02LowPoly.dae",
                                                                             /**"FaceGenMaleHi.dae"**/ };
    private static final String[] maleHeads = new String[] {
                                                                             "midAgeGuy.dae",
                                                                             "MaleCHead.dae",
                                                                             "blackHead.dae",
                                                                             "AsianHeadMale.dae" };

    private static final String     femaleRelPath           = "assets/models/collada/Heads/FemaleHead/";

    private static final String[]   femaleFGHeads           = new String[] { "FG_Female02HighPoly.dae",
                                                                             "FG_Female01LowPoly.dae",
                                                                             "FG_Female01HighPoly.dae" };
    private static final String[]   femaleHeads = new String[] {             "FemaleHispanicHead.dae",
                                                                             "FemaleCHead.dae",
                                                                             "FemaleAAHead.dae",
                                                                             "AsianFemaleHead.dae" };

    private static final String[]   maleFGFacialAnim        = new String[] { "assets/models/collada/Animations/MaleFacialAnimations/FG_Male01LowPoly-default.dae",
                                                                             "assets/models/collada/Animations/MaleFacialAnimations/FG_Male01LowPoly-smile.dae",
                                                                             "assets/models/collada/Animations/MaleFacialAnimations/FG_Male01LowPoly-scorn.dae",
                                                                             "assets/models/collada/Animations/MaleFacialAnimations/FG_Male01LowPoly-frown.dae",
                                                                             "assets/models/collada/Animations/MaleFacialAnimations/FG_Male01LowPoly-frown-mild.dae" };
    
    private static final String[]   maleFacialAnim          = new String[] { "assets/models/collada/Animations/MaleFacialAnimations/MaleDefault.dae",
                                                                             "assets/models/collada/Animations/MaleFacialAnimations/MaleSmile.dae",
                                                                             "assets/models/collada/Animations/MaleFacialAnimations/MaleFrown.dae",
                                                                             "assets/models/collada/Animations/MaleFacialAnimations/MaleScorn.dae" };

    private static final String[]   femaleFGFacialAnim      = new String[] { "assets/models/collada/Animations/FemaleFacialAnimations/FG_Female_DefaultSmile.dae",
                                                                             "assets/models/collada/Animations/FemaleFacialAnimations/FG_Female_BigSmile.dae",
                                                                             "assets/models/collada/Animations/FemaleFacialAnimations/FG_Female_Frown.dae",
                                                                             "assets/models/collada/Animations/FemaleFacialAnimations/FG_Female_Angry.dae" };
    private static final String[]   femaleFacialAnim        = new String[] { "assets/models/collada/Animations/FemaleFacialAnimations/FemaleDefault.dae",
                                                                             "assets/models/collada/Animations/FemaleFacialAnimations/FemaleSmile.dae",
                                                                             "assets/models/collada/Animations/FemaleFacialAnimations/FemaleFrown.dae",
                                                                             "assets/models/collada/Animations/FemaleFacialAnimations/FemaleScorn.dae" };

    /** The prefix to use for making URLs **/
    private static String           base                    = "file:///" + System.getProperty("user.dir") + File.separatorChar;

    // Disabled
    private AvatarBinaryHeadExporter() {}

    /** Used for relaying status data **/
    private static SwingFrame frame = null;

    /**
     * Run the tool. See the class documentation for supported command line
     * arguments.
     * @param args Command line args
     */
    public static void main(String[] args) {
        // process the command line arguments
        boolean loadAll = true;
        HeadTypes type = null;
        for (String arg : args)
        {
            if (arg.equals("-all"))
                loadAll = true;
            else if (arg.startsWith("-type:"))
                type = Enum.valueOf(HeadTypes.class, arg.split(":")[1]);
        }
        if (args.length == 0)
            loadAll = true;
        if (loadAll == false && type == null) // Nothing to do
            return; // Can just return, because nothing weird has happened
        // Set up the framework
        WorldManager worldManager = new WorldManager("TheWorldManager");
        Repository repository = new Repository(worldManager, false, null); // do not load skeletons, do not load use a cache
        repository.setLoadGeometry(false);
        // Add it as user data
        worldManager.addUserData(Repository.class, repository);
        worldManager.getRenderManager().setDesiredFrameRate(60);
        createUI(worldManager);
        frame.status("Ready!");
        try {
            if (type != null)
                createBinaryHeads(type, worldManager);
            else { // must be loading all types
                for (HeadTypes headType : HeadTypes.values())
                    createBinaryHeads(headType, worldManager);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IOException while processing head!", ex);
        }
        System.exit(0);
    }

    private static void createBinaryHeads(HeadTypes type, WorldManager wm) throws IOException {
        String[] heads = null;
        String[] facialAnimations = null;
        String pathPrefix = null;

        // determine particular data set
        switch (type)
        {
            case DefaultMale:
                heads = maleHeads;
                facialAnimations = maleFacialAnim;
                pathPrefix = maleRelPath;
                break;
            case DefaultFemale:
                heads = femaleHeads;
                facialAnimations = femaleFacialAnim;
                pathPrefix = femaleRelPath;
                break;
            case FaceGenMale:
                heads = maleFGHeads;
                facialAnimations = maleFGFacialAnim;
                pathPrefix = maleRelPath;
                break;
            case FaceGenFemale:
                heads = femaleFGHeads;
                facialAnimations = femaleFGFacialAnim;
                pathPrefix = femaleRelPath;
                break;
            default:
                throw new IllegalArgumentException("Unsupported head type: " + type);
        }


        for (String headName : heads) {
            frame.status("Serializing " + headName);
            // Generate the save file name
            int index   = headName.lastIndexOf(".");
            String saveFile = headName.substring(0, index);
            saveFile += ".bhf";
            BinaryHeadExporter.BinaryHeadExporterParams params =
                    new BinaryHeadExporter.BinaryHeadExporterParams(wm)
                    .setOutputFile(new File(saveFolder + "/" + saveFile))
                    .setHeadLocation(new URL(base + pathPrefix + headName));
            for (String anim : facialAnimations)
                params.addAnimationFile(anim);
            BinaryHeadExporter.createBinaryHeadFile(params);
        }
    }

    /**
     * Create all of the Swing windows - and the 3D window
     */
    private static void createUI(WorldManager wm) {
        frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame with focus
        frame.canvas.requestFocusInWindow();

        frame.setVisible(true);

        // Add to the wm to set title string later during debugging
        wm.addUserData(JFrame.class, frame);
    }

    private static class SwingFrame extends JFrame implements ActionListener {

        private String status = "Ready!";
        JPanel contentPane;
        JPanel canvasPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        Canvas canvas = null;
        JLabel statusLabel = new JLabel(status);
        OnscreenRenderBuffer m_renderBuffer = null;


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

            // The Rendering Canvas
            m_renderBuffer = (OnscreenRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, 1, 1);
            wm.getRenderManager().addRenderBuffer(m_renderBuffer);
            canvas = m_renderBuffer.getCanvas();
            canvas.setVisible(true);
            canvasPanel.setLayout(new GridBagLayout());
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);

            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(statusLabel);
//            statusPanel.add(button);
            contentPane.add(statusPanel, BorderLayout.SOUTH);

            pack();
        }


        /**
         * Listen for frame rate updates
         */
        public void status(String message) {
            this.status = message;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    statusLabel.setText(status);
                    repaint();
                }
            });
        }

        public void actionPerformed(ActionEvent e)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public OnscreenRenderBuffer getRenderBuffer()
        {
            return m_renderBuffer;
        }
    }
}
