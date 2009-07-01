/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui;

import imi.character.avatar.Avatar;
import imi.gui.configurer.Configurer;
import imi.loaders.Collada;
import imi.loaders.ColladaLoadingException;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.utils.traverser.MeshInstanceSearchProcessor;
import imi.scene.utils.traverser.TreeTraverser;
import imi.utils.FileUtils.FileMetrics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class MeshSwapDialogue extends JDialog{
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private FileChooser     m_headChooser   = null;
    private FileChooser     m_handChooser   = null;
    private FileChooser     m_torsoChooser  = null;
    private FileChooser     m_legsChooser   = null;
    private FileChooser     m_feetChooser   = null;
    private FileChooser     m_hairChooser   = null;
    private FileChooser     m_hatChooser    = null;
    private FileChooser     m_glassChooser  = null;
    private FileChooser     m_jacketChooser = null;

    private JPanel          m_confirmPanel  = null;

    private JButton         m_load          = null;
    private JButton         m_cancel        = null;
    private Configurer      m_parent        = null;

    private Color           m_colorGray     = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    private Color           m_colorYellow   = new Color(0.5f, 0.5f, 0.0f, 1.0f);
    private Color           m_colorWhite    = new Color(1.0f, 1.0f, 1.0f, 1.0f);

    private Avatar          m_avatar        = null;
    private WorldManager    m_worldManager  = null;
    private Collada         m_colladaLoader = null;

    private static String[] subgroups  = new String[] { "Head",    "Hands",    "UpperBody",    "LowerBody",    "Feet",
                                                        "Hair",    "Hats",      "Glasses",      "Jacket"};

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public MeshSwapDialogue() {
        super();
        this.setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public MeshSwapDialogue(Configurer parent, Avatar avatar, WorldManager wm) {
        super(parent, true);
        m_parent        = parent;
        m_avatar        = avatar;
        m_worldManager  = wm;

        m_colladaLoader = new Collada();
        m_colladaLoader.setLoadAnimations(false);
        m_colladaLoader.setLoadGeometry(true);
        m_colladaLoader.setLoadRig(false);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        MeshSwapDialogue test = new MeshSwapDialogue();
        test.initComponents();
        test.setVisible(true);
    }

    public void open() {
        this.initComponents();
        this.setLocationRelativeTo(m_parent);
        this.setVisible(true);
    }

    public void close() {
        dispose();
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void initComponents() {
        m_confirmPanel          = new JPanel();
        GridBagConstraints  gbc = new GridBagConstraints();
        gbc.gridx = 0;  gbc.gridy = 0;  gbc.weightx = 1.0f; gbc.weighty = 1.0f;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;

        // Swapping selector
        m_headChooser   = new FileChooser("Select Head", "Normal Mapped", false, m_colorGray, "imi/gui/data/folderHead.png", "imi/gui/data/folderHeadDown.png");
        m_handChooser   = new FileChooser("Select Hands", null, false, m_colorGray, "imi/gui/data/folderHand.png", "imi/gui/data/folderHandDown.png");
        m_torsoChooser  = new FileChooser("Select UpperBody", null, false, m_colorGray, "imi/gui/data/folderShirt.png", "imi/gui/data/folderShirtDown.png");
        m_legsChooser   = new FileChooser("Select LowerBody", null, false, m_colorGray, "imi/gui/data/folderPants.png", "imi/gui/data/folderPantsDown.png");
        m_feetChooser   = new FileChooser("Select Feet", null, false, m_colorGray, "imi/gui/data/folderShoes.png", "imi/gui/data/folderShoesDown.png");
        m_jacketChooser = new FileChooser("Select Jacket", null, false, m_colorGray, "imi/gui/data/folderJacket.png", "imi/gui/data/folderJacketDown.png");
        
        m_hairChooser   = new FileChooser("Select Hair", null, true, m_colorYellow, "imi/gui/data/folderHair_.png", "imi/gui/data/folderHairDown_.png");
        m_hatChooser    = new FileChooser("Select Hat", null, true, m_colorYellow, "imi/gui/data/folderHat.png", "imi/gui/data/folderHatDown.png");
        m_glassChooser  = new FileChooser("Select Glasses", null, true, m_colorYellow, "imi/gui/data/folderGlasses.png", "imi/gui/data/folderGlassesDown.png");

        // Confirmation Panel
        ImageIcon iconUp    = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/circle.png"));
        ImageIcon iconDown  = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/circleDown.png"));
        m_load              = new JButton(iconUp);
        m_load.setPressedIcon(iconDown);
        iconUp              = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/X.png"));
        iconDown            = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/Xdown.png"));
        m_cancel            = new JButton(iconUp);
        m_cancel.setPressedIcon(iconDown);
        gbc.gridx = 0;
        m_confirmPanel.add(m_load, gbc);
        gbc.gridx = 1;  gbc.anchor = GridBagConstraints.EAST;
        m_confirmPanel.add(m_cancel, gbc);

        // Add it all to our content panel
        JPanel contentPanel = (JPanel) this.getContentPane();
        contentPanel.setLayout(new GridBagLayout());
        gbc.gridx = 0;  gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(m_headChooser, gbc);
        gbc.gridy = 1;
        contentPanel.add(m_handChooser, gbc);
        gbc.gridy = 2;
        contentPanel.add(m_torsoChooser, gbc);
        gbc.gridy = 3;
        contentPanel.add(m_legsChooser, gbc);
        gbc.gridy = 4;
        contentPanel.add(m_feetChooser, gbc);
        gbc.gridy = 5;
        contentPanel.add(m_jacketChooser, gbc);

        gbc.gridx = 2;  gbc.gridy = 1;
        contentPanel.add(m_hairChooser, gbc);
        gbc.gridx = 2;  gbc.gridy = 2;
        contentPanel.add(m_hatChooser, gbc);
        gbc.gridx = 2;  gbc.gridy = 3;
        contentPanel.add(m_glassChooser, gbc);

        gbc.gridy = 11; gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(m_confirmPanel, gbc);

        setActionListeners();
        this.pack();
        this.setTitle("Mesh Swapping");
    }

    private void setActionListeners() {
        m_load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
                List smFiles    = createSkinnedMeshSwapList();
                List aFiles     = createAttatchmentMeshSwapList();
                FileMetrics headMetrics         = null;
                if (m_headChooser.m_file != null)
                    headMetrics = m_headChooser.m_file;
                
                m_parent.swapMesh(smFiles, aFiles, headMetrics, m_headChooser.m_checkBox.isSelected());
            }
        });

        m_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_parent.setLoadingIndicator(false);
                close();
            }
        });
    }

    private List createSkinnedMeshSwapList() {
        List smSwapList = new ArrayList<Object[]>();

        if (m_handChooser.m_file != null) {
            smSwapList.add(new Object[] { m_handChooser.m_file.file, "Hands" });
        }

        if (m_torsoChooser.m_file != null) {
            smSwapList.add(new Object[] { m_torsoChooser.m_file.file, "UpperBody" });
        }

        if (m_jacketChooser.m_file != null) {
            smSwapList.add(new Object[] { m_jacketChooser.m_file.file, "Jacket" });
        }

        if (m_legsChooser.m_file != null) {
            smSwapList.add(new Object[] { m_legsChooser.m_file.file, "LowerBody" });
        }

        if (m_feetChooser.m_file != null) {
            smSwapList.add(new Object[] { m_feetChooser.m_file.file, "Feet" });
        }
        return smSwapList;
    }

    private List createAttatchmentMeshSwapList() {
        List aSwapList  = new ArrayList<Object[]>();

        if (m_hairChooser.m_file != null) {
            aSwapList.add(new Object[] { m_hairChooser.m_file.file, m_hairChooser.m_mesh.getSelectedItem().toString(), "HairAttach" });
        }

        if (m_hatChooser.m_file != null) {
            aSwapList.add(new Object[] { m_hatChooser.m_file.file, m_hatChooser.m_mesh.getSelectedItem().toString(), "Hats" });
        }

        if (m_glassChooser.m_file != null) {
            aSwapList.add(new Object[] { m_glassChooser.m_file.file, m_glassChooser.m_mesh.getSelectedItem().toString(), "Glasses" });
        }
        return aSwapList;
    }

    public void setAvatar(Avatar avatar) {
        m_avatar = avatar;
    }

//    public Instruction createInstruction() {
//        Instruction pRootInstruction = new Instruction();
//        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());
//
//        addDeleteSMInstruct(pRootInstruction);
//        addDeleteMInstruct(pRootInstruction);
//        addLoadSMGeomInstruct(pRootInstruction);
//        addMeshGeomInstruct(pRootInstruction);
//
//        return pRootInstruction;
//    }
//
//    private void addLoadSMGeomInstruct(Instruction pRootInstruction) {
//        try {
//            if (m_handChooser.m_file != null) {
//                pRootInstruction.addLoadGeometryToSubgroupInstruction(m_handChooser.m_file.file.toURI().toURL(), subgroups[1]);
//            }
//
//            if (m_torsoChooser.m_file != null) {
//                pRootInstruction.addLoadGeometryToSubgroupInstruction(m_torsoChooser.m_file.file.toURI().toURL(), subgroups[2]);
//            }
//
//            if (m_jacketChooser.m_file != null) {
//                pRootInstruction.addLoadGeometryToSubgroupInstruction(m_jacketChooser.m_file.file.toURI().toURL(), subgroups[2]);
//            }
//
//            if (m_legsChooser.m_file != null) {
//                pRootInstruction.addLoadGeometryToSubgroupInstruction(m_legsChooser.m_file.file.toURI().toURL(), subgroups[3]);
//            }
//
//            if (m_feetChooser.m_file != null) {
//                pRootInstruction.addLoadGeometryToSubgroupInstruction(m_feetChooser.m_file.file.toURI().toURL(), subgroups[4]);
//            }
//
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(MeshSwapDialogue.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private void addMeshGeomInstruct(Instruction pRootInstruction) {
//        PMatrix tempSolution = new PMatrix();
//        try {
//            if (m_hairChooser.m_file != null) {
//                pRootInstruction.addChildInstruction(InstructionType.loadGeometry, m_hairChooser.m_file.file.toURI().toURL());
//                pRootInstruction.addAttachmentInstruction( m_hairChooser.m_mesh.getSelectedItem().toString(), "Head", tempSolution, subgroups[5] );
//            }
//
//            if (m_hatChooser.m_file != null) {
//                pRootInstruction.addChildInstruction(InstructionType.loadGeometry, m_hatChooser.m_file.file.toURI().toURL());
//                pRootInstruction.addAttachmentInstruction( m_hatChooser.m_mesh.getSelectedItem().toString(), "Head", tempSolution, subgroups[6] );
//            }
//
//            if (m_glassChooser.m_file != null) {
//                pRootInstruction.addChildInstruction(InstructionType.loadGeometry, m_glassChooser.m_file.file.toURI().toURL());
//                pRootInstruction.addAttachmentInstruction( m_glassChooser.m_mesh.getSelectedItem().toString(), "Head", tempSolution, subgroups[7] );
//            }
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(MeshSwapDialogue.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private void addDeleteSMInstruct(Instruction pRootInstruction) {
//        if (m_handChooser.m_file != null) {
//            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroups[1]);
//            m_avatar.getAttributes().deleteAddInstructionsBySubGroup(subgroups[1]);
//            String[] meshestodelete = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroups[1]);
//            for (int i = 0; i < meshestodelete.length; i++)
//                pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshestodelete[i]);
//        }
//
//        if (m_torsoChooser.m_file != null) {
//            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroups[2]);
//            m_avatar.getAttributes().deleteAddInstructionsBySubGroup(subgroups[2]);
//            String[] meshestodelete = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroups[2]);
//            for (int i = 0; i < meshestodelete.length; i++)
//                pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshestodelete[i]);
//        }
//
//        if (m_legsChooser.m_file != null) {
//            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroups[3]);
//            m_avatar.getAttributes().deleteAddInstructionsBySubGroup(subgroups[3]);
//            String[] meshestodelete = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroups[3]);
//            for (int i = 0; i < meshestodelete.length; i++)
//                pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshestodelete[i]);
//        }
//
//        if (m_feetChooser.m_file != null) {
//            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroups[4]);
//            m_avatar.getAttributes().deleteAddInstructionsBySubGroup(subgroups[4]);
//            String[] meshestodelete = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroups[4]);
//            for (int i = 0; i < meshestodelete.length; i++)
//                pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshestodelete[i]);
//        }
//    }
//
//    private void addDeleteMInstruct(Instruction pRootInstruction) {
//        PNode mesh = null;
//
//        if (m_hairChooser.m_file != null) {
//            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroups[5]);
//            m_avatar.getAttributes().deleteAttachmentInstructionsBySubGroup(subgroups[5]);
//            mesh = m_avatar.getSkeleton().findChild("Head");
//            if (mesh != null)
//                m_avatar.getSkeleton().findAndRemoveChild(subgroups[5]);
//        }
//
//        if (m_hatChooser.m_file != null) {
//            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroups[6]);
//            m_avatar.getAttributes().deleteAttachmentInstructionsBySubGroup(subgroups[6]);
//            mesh = m_avatar.getSkeleton().findChild("Head");
//            if (mesh != null)
//                m_avatar.getSkeleton().findAndRemoveChild(subgroups[6]);
//        }
//
//        if (m_glassChooser.m_file != null) {
//            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroups[6]);
//            m_avatar.getAttributes().deleteAttachmentInstructionsBySubGroup(subgroups[6]);
//            mesh = m_avatar.getSkeleton().findChild("Head");
//            if (mesh != null)
//                m_avatar.getSkeleton().findAndRemoveChild(subgroups[7]);
//        }
//    }
//
//    private List<String[]> updateLoadInstructs() {
//        List<String[]> loadInstructs    = new ArrayList<String[]>();
//        String[] instruct               = new String[2];
//
//        if (m_handChooser.m_file != null) {
//            instruct[0] = m_handChooser.m_file.rPath;
//            instruct[1] = subgroups[1];
//            loadInstructs.add(instruct);
//        }
//
//        if (m_torsoChooser.m_file != null) {
//            instruct = new String[2];
//            instruct[0] = m_torsoChooser.m_file.rPath;
//            instruct[1] = subgroups[2];
//            loadInstructs.add(instruct);
//        }
//
//        if (m_jacketChooser.m_file != null) {
//            instruct = new String[2];
//            instruct[0] = m_jacketChooser.m_file.rPath;
//            instruct[1] = subgroups[2];
//            loadInstructs.add(instruct);
//        }
//
//        if (m_legsChooser.m_file != null) {
//            instruct = new String[2];
//            instruct[0] = m_legsChooser.m_file.rPath;
//            instruct[1] = subgroups[3];
//            loadInstructs.add(instruct);
//        }
//
//        if (m_feetChooser.m_file != null) {
//            instruct = new String[2];
//            instruct[0] = m_feetChooser.m_file.rPath;
//            instruct[1] = subgroups[4];
//            loadInstructs.add(instruct);
//        }
//
//        if (m_hairChooser.m_file != null) {
//            instruct = new String[2];
//            instruct[0] = m_hairChooser.m_file.rPath;
//            instruct[1] = subgroups[5];
//            loadInstructs.add(instruct);
//        }
//
//        if (m_hatChooser.m_file != null) {
//            instruct = new String[2];
//            instruct[0] = m_hatChooser.m_file.rPath;
//            instruct[1] = subgroups[6];
//            loadInstructs.add(instruct);
//        }
//
//        if (m_glassChooser.m_file != null) {
//            instruct = new String[2];
//            instruct[0] = m_glassChooser.m_file.rPath;
//            instruct[1] = subgroups[7];
//            loadInstructs.add(instruct);
//        }
//
//        List<String[]> oldLoadInstructs = m_avatar.getAttributes().getLoadInstructions();
//        for (int i = 0; i < oldLoadInstructs.size(); i++) {
//            loadInstructs.add(oldLoadInstructs.get(i));
//        }
//
//        return loadInstructs;
//    }

////////////////////////////////////////////////////////////////////////////////
// Helper Classes
////////////////////////////////////////////////////////////////////////////////

    public class FileChooser extends JPanel{

        FileFilter      m_filter    = null;
        JFileChooser    m_selector  = null;
        JTextField      m_fileName  = null;
        JButton         m_browse    = null;
        FileMetrics     m_file      = null;
        JCheckBox       m_checkBox  = null;
        JComboBox       m_mesh      = null;
        PScene          m_pscene    = null;

        public FileChooser(String title, String checkbox, boolean attatchments, Color color, String imageUp, String imageDown) {
            super();
            initComponents(title, checkbox, attatchments, color, imageUp, imageDown);
            m_pscene    = new PScene(m_worldManager);
        }

        public void destroy() {
            m_filter    = null;
            m_selector  = null;
            m_fileName  = null;
            m_browse    = null;
            m_file      = null;
            m_checkBox  = null;
            m_mesh      = null;
            m_pscene    = null;
        }

        public void initComponents(String title, String checkbox, boolean attatchments, Color color, String imageUp, String imageDown) {
            Color kolor = null;
            if (checkbox != null)
                this.setMinimumSize(new Dimension(232, 32));
            else
                this.setMinimumSize(new Dimension(200, 59));

            if (color != null)
                kolor = color;
            else
                kolor = new Color(0.5f, 0.5f, 0.5f, 1.0f);

            GridBagConstraints gbc  = new GridBagConstraints();

            m_fileName  = new JTextField();
            m_fileName.setPreferredSize(new Dimension(200, 32));
            m_fileName.setBackground(kolor);
            gbc.gridx = 0;  gbc.gridy = 0;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 2;  gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.BOTH; gbc.anchor = GridBagConstraints.WEST;
            this.add(m_fileName, gbc);

            ImageIcon iconUp    = new ImageIcon(getClass().getClassLoader().getResource(imageUp));
            ImageIcon iconDown  = new ImageIcon(getClass().getClassLoader().getResource(imageDown));
            m_browse    = new JButton(iconUp);
            m_browse.setPressedIcon(iconDown);
            gbc.gridx = 1;   gbc.gridwidth = 1;  gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.NONE;
            this.add(m_browse, gbc);

            if (checkbox != null) {
                m_checkBox  = new JCheckBox(checkbox);
                m_checkBox.setPreferredSize(new Dimension(150, 32));
                m_checkBox.setSelected(true);
                gbc.gridx = 2;  gbc.gridy = 0;  gbc.weighty = 1.0f; gbc.gridwidth = 1;  gbc.gridheight = 1;
                this.add(m_checkBox, gbc);
            }

            if (attatchments) {
                m_mesh  = new JComboBox();
                m_mesh.setPreferredSize(new Dimension(150, 32));
                m_mesh.setBackground(kolor);
                gbc.gridx = 2;  gbc.gridy = 0;  gbc.weighty = 1.0f; gbc.gridwidth = 2;  gbc.gridheight = 1;
                this.add(m_mesh, gbc);
            }

            m_filter = new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if(f.isDirectory()) {
                        return true;
                    }

                    if (f.getName().toLowerCase().endsWith(".dae") ||
                        f.getName().toLowerCase().endsWith(".bin") ||
                        f.getName().toLowerCase().endsWith(".bhf")) {
                        return true;
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    String szDescription = "Collada (*.dae) or Binary (*.bin, *bhf)";
                    return szDescription;
                }
            };

            m_selector = new javax.swing.JFileChooser();
            m_selector.setDialogTitle(title);
            java.io.File colladaDirectory   = new java.io.File(System.getProperty("user.dir"));

            m_selector.setCurrentDirectory(colladaDirectory);
            m_selector.setDoubleBuffered(true);
            m_selector.setDragEnabled(true);
            m_selector.addChoosableFileFilter((FileFilter)m_filter);

            setActionListeners();
        }

        private void setActionListeners() {
            m_browse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    boolean bloaded = loadFile();

                    if (bloaded) {
                        if (m_mesh != null) {
                            Runnable populateMeshBox    = new Runnable() {

                                public void run() {
                                    try {
                                        m_pscene.getInstances().removeAllChildren();
                                        URL colladaFile = m_file.file.toURI().toURL();
                                        try {
                                            m_colladaLoader.load(m_pscene, colladaFile);
                                        } catch (ColladaLoadingException ex) {
                                            Logger.getLogger(MeshSwapDialogue.class.toString()).log(Level.SEVERE, ex.getMessage());
                                        } catch (IOException ex) {
                                            Logger.getLogger(MeshSwapDialogue.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                        MeshInstanceSearchProcessor misProc = new MeshInstanceSearchProcessor();
                                        misProc.setProcessor();
                                        TreeTraverser.breadthFirst(m_pscene, misProc);
                                        Vector<PPolygonMeshInstance> ppmInstances = misProc.getMeshInstances();

                                        for (PNode pNode : ppmInstances) {
                                            m_mesh.addItem(pNode.getName());
                                        }

                                    } catch (MalformedURLException ex) {
                                        Logger.getLogger(MeshSwapDialogue.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            };

                            Thread popBoxThread = new Thread(populateMeshBox);
                            popBoxThread.start();
                        }
                    }
                }
            });
        }

        public boolean loadFile() {
            int returnValue = m_selector.showOpenDialog(m_parent);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File theFile    = m_selector.getSelectedFile();
                m_file = new FileMetrics(theFile);
                m_fileName.setText(m_file.rPath);
                return true;
            }
            return false;
        }
    }
}
