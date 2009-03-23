/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.portals;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import imi.scene.PMatrix;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionComponent;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class IMI_Portals extends Entity {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private Node                m_portal            = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    IMI_Portals (String id) {
        super(id);
    }

    public void createPortal(String portalName, PMatrix transform, Vector3f portalDimensions, ZBufferState zBufferState,
           Vector3f portalViewPosition, WorldManager worldManager, int camWidth, int camHeight) {
           
        Box portal = createPortalGeometry(portalName, transform, portalDimensions, zBufferState);
        createCameraComponent(worldManager, camWidth, camHeight, portalViewPosition);
        TextureState ts = createRenderToTexture(worldManager);
        portal.setRenderState(ts);
        m_portal = new Node(portalName);
        m_portal.attachChild(portal);

        RenderComponent rc  = worldManager.getRenderManager().createRenderComponent(m_portal);
        rc.setOrtho(false);
        rc.setLightingEnabled(false);
        addComponent(RenderComponent.class, rc);
    }
    
    public Box createPortalGeometry(String portalName, PMatrix transform, Vector3f portalDimensions, ZBufferState zBufferState) {
        Box portal  = new Box(portalName, new Vector3f(), portalDimensions.x, portalDimensions.y, portalDimensions.z);
        portal.setLocalTranslation(transform.getTranslation());
        portal.setLocalRotation(transform.getRotationJME());
        portal.setLocalScale(transform.getScaleVector());
        portal.setRenderState(zBufferState);
        portal.setModelBound(new BoundingBox());
        return portal;
    }

    public void createCameraComponent(WorldManager worldManager, int camWidth, int camHeight, Vector3f cameraPosition) {
        CameraNode cn = new CameraNode(getName() + "Cam", null);
        Node cameraSG = new Node();
        cameraSG.attachChild(cn);
        cameraSG.setLocalTranslation(cameraPosition);
        CameraComponent cc = worldManager.getRenderManager().createCameraComponent(cameraSG, cn,
                camWidth, camHeight, 45.0f, camWidth/camHeight, 1.0f, 1000.0f, false);
        addComponent(CameraComponent.class, cc);
    }

    public TextureState createRenderToTexture(WorldManager worldManager) {
        CameraComponent cc = getComponent(CameraComponent.class);
        int width   = cc.getViewportWidth();
        int height  = cc.getViewportHeight();

        RenderBuffer rb = worldManager.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, width, height);
        rb.setCameraComponent(cc);
        worldManager.getRenderManager().addRenderBuffer(rb);

        TextureState ts = worldManager.getRenderManager().createTextureState();
        ts.setEnabled(true);
        ts.setTexture(rb.getTexture(), 0);
        return ts;
    }

    public void createCollisionComponent(JMECollisionSystem collisionSystem) {
        JMECollisionComponent cc = collisionSystem.createCollisionComponent(m_portal);
        addComponent(CollisionComponent.class, cc);
    }
}
