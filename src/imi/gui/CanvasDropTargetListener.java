/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui;

import imi.gui.configurer.Configurer;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author ptruong
 */
public class CanvasDropTargetListener implements DropTargetListener {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private Component   m_component = null;
    private Logger      m_logger    = Logger.getLogger(CanvasDropTargetListener.class.getName());

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public CanvasDropTargetListener(Component component) {
        m_component = component;
    }

////////////////////////////////////////////////////////////////////////////////
// Class Implementations
////////////////////////////////////////////////////////////////////////////////

    public void dragEnter(DropTargetDragEvent dtde) {
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    public void dragOver(DropTargetDragEvent dtde) {
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        // Do nothing
    }

    public void dragExit(DropTargetEvent dte) {
        // Do nothing
    }

    public void drop(DropTargetDropEvent dtde) {

        DropTargetContext targetContext = dtde.getDropTargetContext();
        boolean outcome                 = false;

        // Check to accept or reject the drop
        if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY) != 0)
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
        else {
            dtde.rejectDrop();
            return;
        }

        // Get all the DataFlavors
        DataFlavor[] dataFlavors        = dtde.getCurrentDataFlavors();
        DataFlavor   transferDataFlavor = null;

        // Check the for a javaFileListFlavor (the object we want)
        for (int i = 0; i < dataFlavors.length; i++) {
            if (DataFlavor.javaFileListFlavor.equals(dataFlavors[i])) {
                m_logger.warning("Flavors Matched...");
                transferDataFlavor = dataFlavors[i];
                break;
            }
        }

        if (transferDataFlavor != null) {
            Transferable transferable   = dtde.getTransferable();
            java.util.List  list        = null;

            try {
                m_logger.warning("Getting list of files");
                list = (java.util.List)transferable.getTransferData(transferDataFlavor);

                File passFile = (File)list.get(0);
                String passName = passFile.getCanonicalPath();
                m_logger.warning("File Path: " + passName );

                // parent load of the texture to the head
                if (m_component instanceof Configurer) {
                    ((Configurer) m_component).loadHeadTexture(passFile);
                    outcome = true;
                } else
                    outcome = false;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                m_logger.severe(ioe.getMessage());

                outcome = false;
            } catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
                m_logger.severe(ufe.getMessage());

                outcome = false;
            }
        }

        targetContext.dropComplete(outcome);
    }
}
