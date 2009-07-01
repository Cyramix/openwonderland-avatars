/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui;

import imi.character.CharacterParams;
import imi.character.FemaleAvatarParams;
import imi.character.MaleAvatarParams;
import imi.gui.configurer.Configurer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author ptruong
 */
public class LoadAvatarDialogue extends JDialog{
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private FileChooser         m_headChooser   = null;
    private FileChooser         m_handChooser   = null;
    private FileChooser         m_torsoChooser  = null;
    private FileChooser         m_legsChooser   = null;
    private FileChooser         m_feetChooser   = null;

    private JPanel              m_confirmPanel  = null;
    private JPanel              m_radioPanel    = null;
    private ButtonGroup         m_groupA        = null;
    private ButtonGroup         m_groupG        = null;
    private JRadioButton        m_default       = null;
    private JRadioButton        m_custom        = null;
    private JRadioButton        m_male          = null;
    private JRadioButton        m_female        = null;

    private JButton             m_load          = null;
    private JButton             m_cancel        = null;
    private Configurer          m_parent        = null;

    private Color               m_colorGray     = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    private Color               m_colorYellow   = new Color(0.5f, 0.5f, 0.0f, 1.0f);
    private Color               m_colorWhite    = new Color(1.0f, 1.0f, 1.0f, 1.0f);

    private CharacterParams     m_attributes    = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public LoadAvatarDialogue() {
        super();
        this.setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public LoadAvatarDialogue(Configurer parent) {
        super(parent, true);
        m_parent = parent;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        LoadAvatarDialogue test = new LoadAvatarDialogue();
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
        m_radioPanel            = new JPanel();
        GridBagConstraints  gbc = new GridBagConstraints();
        gbc.gridx = 0;  gbc.gridy = 0;  gbc.weightx = 1.0f; gbc.weighty = 1.0f;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;

        // Radio Button Panel
//        m_groupA            = new ButtonGroup();
//        m_default           = new JRadioButton("Default");
//        m_default.setSelected(true);
//        m_custom            = new JRadioButton("Custom");
//        m_groupA.add(m_default);
//        m_groupA.add(m_custom);        
//        m_radioPanel.add(m_default, gbc);
//        gbc.gridx = 1;
//        m_radioPanel.add(m_custom, gbc);

        m_groupG            = new ButtonGroup();
        m_male              = new JRadioButton("Male");
        m_male.setSelected(true);
        m_female            = new JRadioButton("Female");
        m_groupG.add(m_male);
        m_groupG.add(m_female);
        gbc.gridx = 0;  gbc.gridy = 0;
        m_radioPanel.add(m_male, gbc);
        gbc.gridx = 1;
        m_radioPanel.add(m_female, gbc);

        // Confirmation Panel
        ImageIcon iconUp    = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/circle.png"));
        ImageIcon iconDown  = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/circleDown.png"));
        m_load              = new JButton(iconUp);
        m_load.setPressedIcon(iconDown);
        iconUp              = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/X.png"));
        iconDown            = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/Xdown.png"));
        m_cancel            = new JButton(iconUp);
        m_cancel.setPressedIcon(iconDown);
        gbc.gridx = 0;  gbc.gridy = 0;
        m_confirmPanel.add(m_load, gbc);
        gbc.gridx = 1;
        m_confirmPanel.add(m_cancel, gbc);

        // Add it all to our content panel
        JPanel contentPanel = (JPanel) this.getContentPane();
        contentPanel.setLayout(new GridBagLayout());
        gbc.gridx = 0;  gbc.gridy = 0;
        contentPanel.add(m_radioPanel, gbc);
        gbc.gridy = 6;  gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(m_confirmPanel, gbc);

        setActionListeners();
        this.pack();
        this.setTitle("Avatar Loading");
    }

    private void setActionListeners() {
//        m_default.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                closeCustomAvatar();
//            }
//        });
//
//        m_custom.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                openCustomAvatar();
//            }
//        });

        m_load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
                createAttributes();
                m_parent.loadAvatar(m_attributes);
            }
        });

        m_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_parent.setLoadingIndicator(false);
                close();
            }
        });
    }

    private void openCustomAvatar() {
        JPanel contentPanel = (JPanel) this.getContentPane();
        GridBagConstraints  gbc = new GridBagConstraints();
        gbc.gridx = 0;  gbc.gridy = 1;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 1;  gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;

        m_headChooser   = new FileChooser("Select Head", "Normal Map", null, m_colorWhite, "imi/gui/data/folderHead.png", "imi/gui/data/folderHeadDown.png");
        contentPanel.add(m_headChooser, gbc);
        m_handChooser   = new FileChooser("Select Hands", null, null, m_colorWhite, "imi/gui/data/folderHand.png", "imi/gui/data/folderHandDown.png");
        gbc.gridy = 2;
        contentPanel.add(m_handChooser, gbc);
        m_torsoChooser  = new FileChooser("Select UpperBody", null, null, m_colorWhite, "imi/gui/data/folderShirt.png", "imi/gui/data/folderShirtDown.png");
        gbc.gridy = 3;
        contentPanel.add(m_torsoChooser, gbc);
        m_legsChooser   = new FileChooser("Select LowerBody", null, null, m_colorWhite, "imi/gui/data/folderPants.png", "imi/gui/data/folderPantsDown.png");
        gbc.gridy = 4;
        contentPanel.add(m_legsChooser, gbc);
        m_feetChooser   = new FileChooser("Select Feet", null, null, m_colorWhite, "imi/gui/data/folderShoes.png", "imi/gui/data/folderShoesDown.png");
        gbc.gridy = 5;
        contentPanel.add(m_feetChooser, gbc);

        this.pack();
    }

    private void closeCustomAvatar() {
        JPanel contentPanel = (JPanel) this.getContentPane();

        contentPanel.remove(m_headChooser);
        m_headChooser.destroy();
        m_headChooser = null;        

        contentPanel.remove(m_handChooser);
        m_handChooser.destroy();
        m_handChooser = null;        

        contentPanel.remove(m_torsoChooser);
        m_torsoChooser.destroy();
        m_torsoChooser = null;

        contentPanel.remove(m_legsChooser);
        m_legsChooser.destroy();
        m_legsChooser = null;        

        contentPanel.remove(m_feetChooser);
        m_feetChooser.destroy();
        m_feetChooser = null;

        this.pack();
    }

    private void createAttributes() {
        if (m_male.isSelected()) {
            //m_attributes = new MaleAvatarParams("TestMale", 0, 0, 2, -1, 0, 12, 0);
            m_attributes = new MaleAvatarParams("TestMale").build();
        } else {
            //m_attributes = new FemaleAvatarParams("TestFemale", 1, 0, 0, 0, 0, 12, 0);
            m_attributes = new FemaleAvatarParams("TestFemale").build();
        }
    }

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
        JTextField      m_mesh      = null;

        public FileChooser(String title, String checkbox, String mesh, Color color, String imageUp, String imageDown) {
            super();
            initComponents(title, checkbox, mesh, color, imageUp, imageDown);
        }

        public void destroy() {
            m_filter    = null;
            m_selector  = null;
            m_fileName  = null;
            m_browse    = null;
            m_file      = null;
            m_checkBox  = null;
            m_mesh      = null;
        }

        public void initComponents(String title, String checkbox, String mesh, Color color, String imageUp, String imageDown) {
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

            if (mesh != null) {
                m_mesh  = new JTextField(mesh);
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
                    loadFile();
                }
            });
        }

        public void loadFile() {
            int returnValue = m_selector.showOpenDialog(m_parent);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File theFile    = m_selector.getSelectedFile();
                m_file = new FileMetrics(theFile);
                m_fileName.setText(m_file.rPath);
            }
        }
    }

    public static class FileMetrics {
        public File    file    = null;
        public String  bPath   = null;
        public String  aPath   = null;
        public String  rPath   = null;
        public String  fName   = null;

        public FileMetrics(File f) {
            file = f;
            setPaths();
            setFileName();
        }

        public void setPaths() {
            bPath = System.getProperty("user.dir");
            aPath = file.getAbsolutePath();
            rPath = getRelativePath(new File(bPath), file);
        }

        public void setFileName() {
            int bIndex  = file.getAbsolutePath().lastIndexOf(File.separatorChar);
            fName       = file.getAbsolutePath().substring(bIndex + 1);
        }

        public List getPathList(File file) {
            List list = new ArrayList();
            File copy;
            try {
                copy = file.getCanonicalFile();
                while (copy != null) {
                    list.add(copy.getName());
                    copy = copy.getParentFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                list = null;
            }
            return list;
        }

        public String matchPathLists(List r, List f) {
            int i;
            int j;
            StringBuilder s = new StringBuilder();
            // start at the beginning of the lists
            // iterate while both lists are equal
            s.append("");
            i = r.size() - 1;
            j = f.size() - 1;

            // first eliminate common root
            while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
                i--;
                j--;
            }

            // for each remaining level in the home path, add a ..
            for (; i >= 0; i--) {
                s.append("..")
                 .append(File.separator);
            }

            // for each level in the file path, add the path
            for (; j >= 1; j--) {
                s.append(f.get(j))
                 .append(File.separator);
            }

            // file name
            s.append(f.get(j));
            return s.toString();
        }

        public String getRelativePath(File home, File f) {
            List homelist;
            List filelist;
            String s;

            homelist = getPathList(home);
            filelist = getPathList(f);
            s = matchPathLists(homelist, filelist);

            return s;
        }
    }
}
