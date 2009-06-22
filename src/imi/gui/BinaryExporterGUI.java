/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui;

import imi.loaders.repository.Repository;
import imi.utils.BinaryExporterImporter;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.OnscreenRenderBuffer;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class BinaryExporterGUI {

    private static BinaryExporterImporter bhei              = null;
    private static final String     maleRelPath             = "assets/models/collada/Heads/MaleHead/";
    private static final String[]   maleHeads               = new String[] { "midAgeGuy.dae",
                                                                             "MaleCHead.dae",
                                                                             "FG_Obama_HeadMedPoly.dae",
                                                                             "FG_MaleLowPoly_01.dae",
                                                                             "FG_MaleHead02Medium.dae",
                                                                             "FG_Male02MedPoly.dae",
                                                                             "FG_Male02LowPoly.dae",
                                                                             "FaceGenMaleHi.dae",
                                                                             "blackHead.dae",
                                                                             "AsianHeadMale.dae" };
    private static final String     femaleRelPath           = "assets/models/collada/Heads/FemaleHead/";
    private static final String[]   femaleHeads             = new String[] { "FG_FemaleLowPoly_01.dae",
                                                                             "FG_FemaleHead01.dae",
                                                                             "FG_Female02HighPoly.dae",
                                                                             "FG_Female01LowPoly.dae",
                                                                             "FG_Female01HighPoly.dae",
                                                                             "FG_Female_AF_Head02.dae",
                                                                             "FemaleHispanicHead.dae",
                                                                             "FemaleCHead.dae",
                                                                             "FemaleAAHead.dae",
                                                                             "AsianFemaleHead.dae" };

    private static final String[]   maleFGFacialAnim        = new String[] { };
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

    private static String           loadingInfo             = "Ready";
    private static WorldManager     worldManager            = null;
    private static Repository       repository              = null;
    private static boolean          bMakeMaleHeads          = false;
    private static boolean          bMakeFemaleHeads        = false;
    private static String           base                    = "file:///" + System.getProperty("user.dir") + File.separatorChar;

    public BinaryExporterGUI(String[] args) {
        init();
        processArgs(args);
        if (bMakeMaleHeads) {
            createMaleBinaryHeads();
        }
        if (bMakeFemaleHeads) {
            createFemaleBinaryHeads();
        }
    }

    public static void main(String[] args) {
        BinaryExporterGUI test = new BinaryExporterGUI(args);
        System.exit(0);
    }

    private void init() {
        worldManager    = new WorldManager("TheWorldManager");
        repository      = new Repository(worldManager, false, null); // do not load skeletons, do not load use cache
        repository.setLoadGeometry(false);
        worldManager.addUserData(Repository.class, repository);
        worldManager.getRenderManager().setDesiredFrameRate(60);
        createUI(worldManager);
    }

    private void createMaleBinaryHeads() {
//        Runnable createBinary   = new Runnable() {
//
//            @Override
//            public void run() {
//                String saveFile = null;
//                for (String headName : maleHeads) {
//                    int index   = headName.lastIndexOf(".");
//                    saveFile    = headName.substring(0, index);
//                    saveFile   += ".bhf";
//                    File saveMe = new File(base + maleRelPath + saveFile);
//                    if (headName.toLowerCase().contains("obama"))
//                        bhei    = new BinaryExporterImporter(maleRelPath + headName, null, maleFGFacialAnim, saveMe, base);
//                    else
//                        bhei    = new BinaryExporterImporter(maleRelPath + headName, null, maleFacialAnim, saveMe, base);
//                    bhei.serialize(worldManager);
//                    System.out.println(saveFile + " binary conversion complete");
//                }
//            }
//        };
//        Thread binaryCreationThread = new Thread(createBinary);
//        binaryCreationThread.start();
        String saveFile = null;
        for (String headName : maleHeads) {
            int index   = headName.lastIndexOf(".");
            saveFile    = headName.substring(0, index);
            saveFile   += ".bhf";
            File saveMe = new File(maleRelPath + saveFile);
//            if (headName.toLowerCase().contains("obama"))
//                bhei    = new BinaryHeadExporterImporter(maleRelPath + headName, null, maleFGFacialAnim, saveMe, base);
//            else
                bhei    = new BinaryExporterImporter(maleRelPath + headName, null, maleFacialAnim, saveMe, base);
            bhei.serialize(worldManager);
            System.out.println(saveFile + " binary conversion complete");
        }
    }

    private void createFemaleBinaryHeads() {
//        Runnable createBinary   = new Runnable() {
//
//            @Override
//            public void run() {
//                String saveFile = null;
//                for (String headName : femaleHeads) {
//                    int index   = headName.lastIndexOf(".");
//                    saveFile    = headName.substring(0, index);
//                    saveFile   += ".bhf";
//                    File saveMe = new File(base + femaleRelPath + saveFile);
//                    if (headName.toLowerCase().contains("fg_"))
//                        bhei    = new BinaryExporterImporter(femaleRelPath + headName, null, femaleFGFacialAnim, saveMe, base);
//                    else
//                        bhei    = new BinaryExporterImporter(femaleRelPath + headName, null, femaleFacialAnim, saveMe, base);
//                    bhei.serialize(worldManager);
//                    System.out.println(saveFile + " binary conversion complete");
//                }
//            }
//        };
//        Thread binaryCreationThread = new Thread(createBinary);
//        binaryCreationThread.start();
        String saveFile = null;
        for (String headName : femaleHeads) {
            int index   = headName.lastIndexOf(".");
            saveFile    = headName.substring(0, index);
            saveFile   += ".bhf";
            File saveMe = new File(femaleRelPath + saveFile);
            if (headName.toLowerCase().contains("fg_"))
                bhei    = new BinaryExporterImporter(femaleRelPath + headName, null, femaleFGFacialAnim, saveMe, base);
            else
                bhei    = new BinaryExporterImporter(femaleRelPath + headName, null, femaleFacialAnim, saveMe, base);
            bhei.serialize(worldManager);
            System.out.println(saveFile + " binary conversion complete");
        }
    }

    private void processArgs(String[] args) {
        if (args.length == 0)
            printUsage();
        for (int i = 0; i < args.length; ++i)
        {
            if (args[i].equalsIgnoreCase("-m"))
                bMakeMaleHeads = true;
            else if (args[i].equalsIgnoreCase("-f"))
                bMakeFemaleHeads = true;
            else if (args[i].equalsIgnoreCase("-mf"))
                bMakeMaleHeads = bMakeFemaleHeads = true;
        }
    }

    private void printUsage()
    {
        System.err.println("Usage: <command> (-m | -f)");
        System.err.println("-m : Create male binary head files");
        System.err.println("-f : Create female binary head files");
        System.err.println("-mf: Create male & female binary head files");
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

            // The Menu Bar
            JMenuBar menuBar = new JMenuBar();

            // File Menu
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);

            // Create Menu
            JMenu createMenu = new JMenu("Create");
            menuBar.add(createMenu);

            // The Rendering Canvas
            m_renderBuffer = (OnscreenRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, 1, 1);
            wm.getRenderManager().addRenderBuffer(m_renderBuffer);
            canvas = m_renderBuffer.getCanvas();
            canvas.setVisible(true);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);

            JButton button  = new JButton("Make Heads");
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    createMaleBinaryHeads();
                    createFemaleBinaryHeads();
                }
            });

            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(fpsLabel);
            statusPanel.add(button);
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

        public OnscreenRenderBuffer getRenderBuffer()
        {
            return m_renderBuffer;
        }
    }
}
