/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.utils;

import imi.scene.PNode;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Paul Vietn Ngueyn Truong (ptruong)
 */
public class JTree_DataDumper {

    private FileOutputStream    m_OutputStream;
    private PrintStream         m_PrintStream;
    private JFileChooser        m_SaveFileChooser;
    private Component           m_Parent;
    private int                 m_tabs;

    /**
     * Default constructor sets up the jFilechooser to set name and location of
     * file to save.  The parent caller of this method still must be set for the
     * jFilechooser to load and accept file name and save location.
     */
    public JTree_DataDumper() {
        initFileChooser();
    }

    /**
     * Overloaded constructor sets up the jFilechooser to set name and location of
     * the output file to save as well as sets the parent caller.
     * @param parentCaller - the component requesting creation of the class
     */
    public JTree_DataDumper(Component parentCaller) {
        m_Parent = parentCaller;
        initFileChooser();
    }

    /**
     * Traverse all the nodes inside a JTree starting with the root node.  Method
     * setsup data and calls the correct function.
     * @param tree - the JTree to traverse through
     */
    public void traverseAllNodes(JTree tree) {
        if (tree == null)
            return;

        DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
        traverseAllNodes(root, m_tabs);
    }

    /**
     * Traverse all the all nodes starting from the node passed in recursively.
     * Processes each node only once. The actual function that does the work
     * @param node - the start node for the traversal
     */
    private void traverseAllNodes(DefaultMutableTreeNode node, int indents) {
        if (node == null)
            return;

        processNode(node, indents);

        if (node.getChildCount() >= 0) {
            int childIndent = new Integer(indents);
            for (int i = 0; i < node.getChildCount(); i++) {
                childIndent++;
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
                traverseAllNodes(childNode, childIndent);
            }
        }
    }

    /**
     * Traverse through all the open node paths.  Ignores node that are collapsed
     * in the treeview.  Method sets up the data and calls the correct function.
     * @param tree - the JTree to traverse through
     */
    public void traverseAllExpandedNodes(JTree tree) {
        if (tree == null)
            return;

        DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
        traverseAllExpandedNodes(tree, new TreePath(root), m_tabs);
    }

    /**
     * Traverse through all the open node paths recursively.  Ignores nodes on paths
     * that are collapsed.  The actual function that does the work.
     * @param tree - the JTree to traverse through
     * @param path - the path to follow/check
     */
    private void traverseAllExpandedNodes(JTree tree, TreePath path, int indents) {
        if (tree == null)
            return;

        // Check to see if the tree is expanded or not
        if (!tree.isExpanded(path))
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        processNode(node, indents);

        if (node.getChildCount() >= 0) {
            int childIndents = new Integer(indents);
            for (int i = 0; i < node.getChildCount(); i++) {
                childIndents++;
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
                TreePath newPath = path.pathByAddingChild(childNode);
                traverseAllExpandedNodes(tree, newPath, childIndents);
            }
        }
    }

    /**
     * Processes the data contained in the node and dumps the data out into file
     * @param node - the current node to process
     */
    private void processNode(DefaultMutableTreeNode node, int tabs) {
        if (node == null)
            return;

        PNode pNode = (PNode)node.getUserObject();
        int index = pNode.getClass().getName().lastIndexOf(".");
        String derivedtype = pNode.getClass().getName().substring(index+1);
        String descriptor = pNode.getName();
        String indents = new String();
        for (int i = 0; i < tabs; i++)
            indents += " ";

        m_PrintStream.println(indents + "[" + derivedtype + "]  " + descriptor);
    }

    public void saveJTreeData(int nodeProcessingType, JTree tree) {
        File saveFile = new File("jtree_datadump.txt");
        m_SaveFileChooser.setSelectedFile(saveFile);

        int retVal = m_SaveFileChooser.showSaveDialog(m_Parent);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            saveFile = m_SaveFileChooser.getSelectedFile();
            try {

                m_OutputStream = new FileOutputStream(saveFile);
                m_PrintStream   = new PrintStream(m_OutputStream);
                m_tabs = 0;

                switch(nodeProcessingType)
                {
                    case 0:
                    {
                        traverseAllNodes(tree);
                        break;
                    }
                    case 1:
                    {
                        traverseAllExpandedNodes(tree);
                        break;
                    }
                }

                m_tabs = 0;
                m_PrintStream.close();
                m_OutputStream.close();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(JTree_DataDumper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(JTree_DataDumper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void initFileChooser() {
        FileFilter saveFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".txt")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Plain Text (*.txt)");
                return szDescription;
            }
        };
        File directory = new File("file:///" + System.getProperty("user.dir"));
        m_SaveFileChooser = new javax.swing.JFileChooser();
        m_SaveFileChooser.setDialogTitle("Save JTree Data");
        m_SaveFileChooser.setCurrentDirectory(directory);
        m_SaveFileChooser.setDoubleBuffered(true);
        m_SaveFileChooser.setDragEnabled(true);
        m_SaveFileChooser.addChoosableFileFilter((FileFilter)saveFilter);
    }
}
