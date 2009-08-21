/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui;

import com.jme.scene.state.CullState.Face;
import imi.character.avatar.Avatar;
import imi.loaders.Collada;
import imi.loaders.ColladaLoadingException;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import imi.scene.utils.PMeshUtils;
import imi.scene.utils.traverser.MeshInstanceSearchProcessor;
import imi.scene.utils.traverser.TreeTraverser;
import imi.shader.programs.HairShader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.mtgame.WorldManager;

/**
 * This UI component is for loading hair collections and previewing them on the
 * loaded head. An ActionListener must be provided to this class for handling
 * avatar hair updates.
 * @author Ronald E Dahlgren
 */
public class AttachmentControlFrame extends JFrame implements ActionListener
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(AttachmentControlFrame.class.getName());

    /** PScene that is used for loading **/
    private PScene pscene = null;

    /** World manager ref **/
    private WorldManager wm = null;

    /** Avatar that we are putting things on **/
    private Avatar targetAvatar = null;

    /** The attachment joint **/
    private PJoint attachJoint = null;
    private String attatchJointNamE = null;

    /** Skeleton joint that we will be attaching to **/
    private SkinnedMeshJoint skeletonAttachPoint = null;

    /** Collada loader we are using **/
    private Collada colladaLoader = new Collada();

    /**
     * Construct and display a new attachment control frame
     * @param wm The world manager
     * @param avatar The avatar to operate on
     * @param folderIconUp
     * @param folderIconDown
     * @param skeletonAttachJoint The joint that the attachments were created for, and will be attached to
     * @param attatchJointName 
     */
    public AttachmentControlFrame(  WorldManager wm,
                                    Avatar avatar,
                                    ImageIcon folderIconUp,
                                    ImageIcon folderIconDown,
                                    SkinnedMeshJoint skeletonAttachJoint,
                                    String attatchJointName,
                                    String frameTitle)
    {
        if (wm == null)
            throw new IllegalArgumentException("Must have a valid WorldManager to proceed");
        if (avatar == null)
            throw new IllegalArgumentException("Must have an avatar to place hair on.");
        if (folderIconUp == null || folderIconDown == null)
            throw new IllegalArgumentException("Need an icon for the folder browsing.");
        if (skeletonAttachJoint == null)
            throw new IllegalArgumentException("Must specify the joint to attach to and unskin from.");

        // Copy the refs we need
        this.wm = wm;
        this.targetAvatar = avatar;
        this.skeletonAttachPoint = skeletonAttachJoint;

        // Create our pscene
        pscene = new PScene("AttachmentController", wm);
        // Set up the collada loader to load all geometry and nothing else.
        colladaLoader.setLoadAnimations(false);
        colladaLoader.setLoadGeometry(true);
        colladaLoader.setLoadRig(false);

        attatchJointNamE    = attatchJointName;
        attachJoint         = (PJoint) skeletonAttachJoint.findChild(attatchJointName);

        // Set up the interface
        initUI(folderIconUp, folderIconDown);

        setTitle(frameTitle);

        this.setResizable(false);
        // show ourselves!
        this.setVisible(true);
    }

    /**
     * Implementation of ActionListener interface.
     * @param action
     */
    public void actionPerformed(ActionEvent action) {
        if (action.getActionCommand().equals("browseButtonPressed"))
            handleBrowseButtonPressed();

    }

    private void handleBrowseButtonPressed() {
        // Put up a JFile chooser
        JFileChooser fileChooser = new JFileChooser();
        // Filter to only dae files
        fileChooser.setFileFilter(new FileNameExtensionFilter("COLLADA File", "dae"));
        java.io.File colladaDirectory   = new java.io.File(System.getProperty("user.dir"));
        fileChooser.setCurrentDirectory(colladaDirectory);

        fileChooser.showOpenDialog(this);
        // if a file is selected:
        if (fileChooser.getSelectedFile() != null)
        {
            try {
                // Load all of the meshes from the file
                attachmentControlPanel.accessorySelectionComboBox.setSelectedIndex(-1);
                pscene.getInstances().removeAllChildren();
                pscene.clearLocalCache();
                
                URL colladaFile = fileChooser.getSelectedFile().toURI().toURL();
                try {
                    colladaLoader.clear();
                    colladaLoader.load(pscene, colladaFile);

                } catch (ColladaLoadingException ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                } catch (IOException ex) {
                    Logger.getLogger(AttachmentControlFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                unskinAllSkinnedMeshes(pscene);
                // populate the combo box with their names
                DefaultComboBoxModel model  = new DefaultComboBoxModel();
                for (PNode kid : pscene.getInstances().getChildren())
                    if (kid instanceof PPolygonMeshInstance)
                        model.addElement(kid.getName());//.addItem(kid.getName());
                attachmentControlPanel.accessorySelectionComboBox.setModel(model);
                // activate the hair control panel
                attachmentControlPanel.setActive(true);
                // set the file text area to show the loaded file path
                accessoryCollectionFile.setText(colladaFile.getFile());
                // attach the top hair
                attachSelectedMesh();
            }
            catch (MalformedURLException ex)
            {
                logger.severe(ex.getMessage());
            }
        }
        else
        // if no file is selected:
        {
            // Clear out mesh list
            pscene.getInstances().removeAllChildren();
            pscene.clearLocalCache();
            // Remove any hair meshes from the avatar
            if (attachJoint != null)
                attachJoint.removeAllChildren();
            // disable the hair controller interface
            attachmentControlPanel.accessorySelectionComboBox.removeAllItems();
            attachmentControlPanel.setActive(false);
            // clear out the text area
            accessoryCollectionFile.setText(null);
        }
    }


    private void initUI(ImageIcon folderIconUp, ImageIcon folderIconDown)
    {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension(290, 176));
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        accessoryCollectionFile = new JTextField("No file selected");
        accessoryCollectionFile.setPreferredSize(new Dimension(200, 18));
        this.add(accessoryCollectionFile, gbc);


        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx = 2;
        gbc.gridy = 0;
        browseButton = new JButton();
        browseButton.setIcon(folderIconUp);
        browseButton.setPressedIcon(folderIconDown);
        browseButton.setActionCommand("browseButtonPressed");
        browseButton.addActionListener(this);
        this.add(browseButton, gbc);

        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        attachmentControlPanel = new AttachmentControlPanel(this);
        attachmentControlPanel.setActive(false);
        this.add(attachmentControlPanel, gbc);

        this.pack();
    }

    public void attachSelectedMesh()
    {
        if (attachmentControlPanel.accessorySelectionComboBox.getSelectedItem() != null &&
            attachmentControlPanel.accessorySelectionComboBox.getSelectedIndex() != -1)
            attachMesh((PPolygonMeshInstance)pscene.findChild(attachmentControlPanel.accessorySelectionComboBox.getSelectedItem().toString()));
        else
            attachMesh(null);
    }

    private void attachMesh(PPolygonMeshInstance mesh)
    {
        if (attachJoint == null)
        {
            attachJoint = new PJoint(attatchJointNamE, new PTransform());
            skeletonAttachPoint.addChild(attachJoint);
        }

        if (attachJoint.getChildrenCount() > 0)
            pscene.getInstances().addChild(attachJoint.getChild(0)); // put the old child back
        if (mesh != null) {
            attachJoint.addChild(mesh);
            mesh.getMaterialRef().setCullFace(Face.None);
            mesh.applyMaterial();
        }
    }

    private void unskinAllSkinnedMeshes(PScene pscene)
    {
        MeshInstanceSearchProcessor misProcessor    = new MeshInstanceSearchProcessor();
        misProcessor.setProcessor();
        TreeTraverser.breadthFirst(pscene, misProcessor);

        Vector<PPolygonMeshInstance> ppsmInstances  = misProcessor.getNodeMeshInstances();
        ArrayList<PPolygonMesh> meshes              = new ArrayList<PPolygonMesh>();

        for (int i = 0; i < ppsmInstances.size(); i++) {
            if (ppsmInstances.get(i) instanceof PPolygonSkinnedMeshInstance) {
                PPolygonSkinnedMeshInstance instance = (PPolygonSkinnedMeshInstance)ppsmInstances.get(i);
                SkeletonNode maleOrFemaleDeafaultSkeleton = null; // TODO get from the repository the default male or female skeleton... the repository returns a copy so better cache it in a static member variable in this class.
                meshes.add(PMeshUtils.unskinMesh(maleOrFemaleDeafaultSkeleton,
                           (PPolygonSkinnedMesh)instance.getGeometry(),
                           "Hair"));
            } else {
                logger.warning("TEST FAILED... NOT PPOLYGONSKINNEDMESHINSTANCE: " + ppsmInstances.get(i).getName());
            }
        }

        pscene.getInstances().removeAllChildren();
        pscene.clearLocalCache();

        for (PPolygonMesh mesh : meshes) {
            PPolygonMeshInstance meshInst = new PPolygonMeshInstance(mesh.getName(), mesh, new PMatrix(), pscene, false);
            // TODO : Parameterize the material
            if (attatchJointNamE.toLowerCase().contains("hair")) {
                meshInst.getMaterialRef().setTexture(0,getClass().getResource("/assets/models/collada/Hair/HairBase.png"));
                meshInst.getMaterialRef().setTexture(1,getClass().getResource("/assets/models/collada/Hair/HairBaseBlack_NRM.png"));
            }
            meshInst.getMaterialRef().setDefaultShader(pscene.getRepository().newShader(HairShader.class));
            meshInst.applyMaterial();
            pscene.getInstances().addChild(meshInst);
        }
    }

    /************************************************
     *                UI Components
     ***********************************************/
    private AttachmentControlPanel attachmentControlPanel = null;
    private JTextField accessoryCollectionFile = null;
    private JButton browseButton = null;
    private class AttachmentControlPanel extends JPanel implements ActionListener
    {
        AttachmentControlFrame owningFrame = null;
        /** The name of the hair **/
        JLabel      accessoryName = new JLabel();
        /** Used to select the accessory to test **/
        JComboBox   accessorySelectionComboBox = new JComboBox();
        /** Left, right, and X buttons **/
        JButton     leftButton  = new JButton();
        JButton     xButton     = new JButton();
        JButton     rightButton = new JButton();

        AttachmentControlPanel(AttachmentControlFrame owner)
        {
            initUI();
        }

        private void initUI()
        {
            // set a border around us.
            this.setBorder(BorderFactory.createRaisedBevelBorder());
            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            // label
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            this.add(accessoryName, gbc);
            // combo box
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.gridx = 0;
            gbc.gridy = 1;
            accessorySelectionComboBox.setPreferredSize(new Dimension(200, 24));
            accessorySelectionComboBox.addActionListener(this);
            this.add(accessorySelectionComboBox, gbc);
            // left arrow
            gbc.gridwidth = 1; // Everything else from this point is 1 unit wide.
            gbc.gridheight = 1;
            gbc.gridx = 1;
            gbc.gridy = 2;
            ImageIcon iconBuffer = new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/data/back.png"));
            leftButton.setIcon(iconBuffer);
            iconBuffer = new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/data/back-down.png"));
            leftButton.setPressedIcon(iconBuffer);
            leftButton.setActionCommand("prevButtonPressed");
            leftButton.addActionListener(this);
            this.add(leftButton, gbc);
            // cancel button
            gbc.gridx = 2;
            gbc.gridy = 2;
            java.awt.Image xImage = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("imi/gui/data/X.png")).getScaledInstance(32, 32, 0);
            iconBuffer = new ImageIcon(xImage);
            xButton.setIcon(iconBuffer);
            xImage = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("imi/gui/data/Xdown.png")).getScaledInstance(32, 32, 0);
            iconBuffer = new ImageIcon(xImage);
            xButton.setPressedIcon(iconBuffer);
            xButton.setActionCommand("xButtonPressed");
            xButton.addActionListener(this);
            this.add(xButton, gbc);
            // right arrow
            gbc.gridx = 3;
            gbc.gridy = 2;
            iconBuffer = new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/data/forward.png"));
            rightButton.setIcon(iconBuffer);
            iconBuffer = new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/data/forward-down.png"));
            rightButton.setPressedIcon(iconBuffer);
            rightButton.setActionCommand("nextButtonPressed");
            rightButton.addActionListener(this);
            this.add(rightButton, gbc);
        }

        /**
         * Implementation of ActionListener interface.
         * @param arg0
         */
        public void actionPerformed(ActionEvent arg0) {
            String actionCommand = arg0.getActionCommand();
            if (actionCommand.equals("nextButtonPressed"))
            {
                int selectedIndex = accessorySelectionComboBox.getSelectedIndex();
                selectedIndex++;
                selectedIndex %= accessorySelectionComboBox.getItemCount();
                accessorySelectionComboBox.setSelectedIndex(selectedIndex);
            }
            else if (actionCommand.equals("prevButtonPressed"))
            {
                int selectedIndex = accessorySelectionComboBox.getSelectedIndex();
                selectedIndex--;
                selectedIndex = selectedIndex < 0 ? selectedIndex = accessorySelectionComboBox.getItemCount() - 1 : selectedIndex;
                accessorySelectionComboBox.setSelectedIndex(selectedIndex);
            }
            else if (actionCommand.equals("xButtonPressed"))
            {
                accessorySelectionComboBox.setSelectedIndex(-1);
            }

            if (arg0.getSource() instanceof JComboBox)
                attachSelectedMesh();

        }

        void setActive(boolean bActive)
        {
            for (Component comp : this.getComponents())
                comp.setEnabled(bActive);
            if (!bActive)
                accessorySelectionComboBox.removeAllItems();
            this.setEnabled(bActive);
        }
    }
}
