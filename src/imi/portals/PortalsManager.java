/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.portals;

import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.state.ZBufferState;
import imi.scene.PMatrix;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class PortalsManager extends Entity {

    public Node   m_portals   = null;

    public PortalsManager(String id, WorldManager worldManager, int camWidth, int camHeight) {
        super(id);
        createCameraComponent(worldManager, camWidth, camHeight);
        m_portals = new Node(id);
    }

    public void createPortal(String portalName, PMatrix transform, Vector3f portalDimensions, ZBufferState zBufferState,
                             Vector3f portalViewPosition, WorldManager worldManager) {

        CameraComponent cc  = getComponent(CameraComponent.class);
        Portal portal       = new Portal(portalName, transform, portalDimensions, zBufferState, portalViewPosition, worldManager, cc);
        m_portals.attachChild(portal);
        RenderComponent rc  = worldManager.getRenderManager().createRenderComponent(m_portals);

        rc.setOrtho(false);
        rc.setLightingEnabled(false);
        removeComponent(RenderComponent.class);
        addComponent(RenderComponent.class, rc);
    }

    public void removePortal(String portalName) {
        m_portals.detachChildNamed(portalName);
    }

    public void removePortal(Portal portal) {
        m_portals.detachChild(portal);
    }

    public void removePortal(int index) {
        m_portals.detachChildAt(index);
    }

    public void createCameraComponent(WorldManager worldManager, int camWidth, int camHeight) {
        CameraNode cn = new CameraNode("PortalsCam", null);
        Node cameraSG = new Node();
        cameraSG.attachChild(cn);
        cameraSG.setLocalTranslation(new Vector3f());
        CameraComponent cc = worldManager.getRenderManager().createCameraComponent(cameraSG, cn,
                camWidth, camHeight, 45.0f, camWidth/camHeight, 1.0f, 1000.0f, false);
        addComponent(CameraComponent.class, cc);
    }
}
