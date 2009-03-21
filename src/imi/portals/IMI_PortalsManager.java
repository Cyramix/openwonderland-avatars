/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.portals;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.state.ZBufferState;
import imi.scene.PMatrix;
import java.util.ArrayList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class IMI_PortalsManager {

    private String              m_name              = null;
    private int                 m_viewportWidth     = 800;
    private int                 m_viewportHeight    = 600;
    private ArrayList<Entity>   m_portals           = new ArrayList<Entity>();

    public IMI_PortalsManager(String name, int width, int height) {
        m_name              = name;
        m_viewportWidth     = width;
        m_viewportHeight    = height;
    }

    public void createPortal(String portalName, PMatrix transform, Vector3f portalDimensions, ZBufferState zBufferState,
           Vector3f portalViewPosition, WorldManager worldManager) {

        IMI_Portals portal = new IMI_Portals(portalName);
        portal.createPortal(portalName, transform, portalDimensions, zBufferState, portalViewPosition, worldManager, m_viewportWidth, m_viewportHeight);
        m_portals.add(portal);
        worldManager.addEntity(portal);
    }
}
