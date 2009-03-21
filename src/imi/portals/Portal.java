/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.portals;

import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import imi.scene.PMatrix;
import imi.scene.SkyBox;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.WorldManager;

/**
 * Creates a portal
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class Portal extends Box {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private SkyBox          m_SkyBox            = null;
    private CameraComponent m_cameraComponent   = null;
    private Vector3f        m_portalViewLoc     = new Vector3f();

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////
    
    Portal(String portalName, PMatrix transform, Vector3f portalDimensions, ZBufferState zBufferState,
           Vector3f portalViewPosition, WorldManager worldManager, CameraComponent cameraComponent) {

        super(portalName, new Vector3f(), portalDimensions.x, portalDimensions.y, portalDimensions.z);
        m_cameraComponent   = cameraComponent;
        m_portalViewLoc     = portalViewPosition;
        
        setLocalTranslation(transform.getTranslation());
        setLocalRotation(transform.getRotationJME());
        setLocalScale(transform.getScaleVector());
        setRenderState(zBufferState);
        setPortalView(portalViewPosition, worldManager);
    }

    @Override
    public void draw(Renderer r) {
        // TODO: move skybox
        updateSkyBoxPosition();
        
        // move cam
        m_cameraComponent.getCameraNode().setLocalTranslation(m_portalViewLoc);

        // parent draw
        super.draw(r);
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public CameraComponent getPortalCamera() {
        return m_cameraComponent;
    }

    public Vector3f getPortalViewLoc() {
        return m_portalViewLoc;
    }

    public SkyBox getSkyBox() {
        return m_SkyBox;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setCameraComponent(CameraComponent cameraComponent) {
        m_cameraComponent = cameraComponent;
    }

    public void setPortalViewLocation(Vector3f portalViewLoc) {
        m_portalViewLoc = portalViewLoc;
    }

    public void setSkyBox(SkyBox skyBox) {
        m_SkyBox = skyBox;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Function
////////////////////////////////////////////////////////////////////////////////

    public void setPortalView(Vector3f portalViewPosition, WorldManager worldManager) {
        m_cameraComponent.getCameraNode().setLocalTranslation(portalViewPosition);
        int width       = m_cameraComponent.getViewportWidth();
        int height      = m_cameraComponent.getViewportHeight();

        RenderBuffer rb = worldManager.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, width, height);
        rb.setCameraComponent(m_cameraComponent);
        worldManager.getRenderManager().addRenderBuffer(rb);

        TextureState ts = worldManager.getRenderManager().createTextureState();
        ts.setEnabled(true);
        ts.setTexture(rb.getTexture(), 0);
        this.setRenderState(ts);
    }

    public void updateSkyBoxPosition() {
        // TODO: think about this...
    }
}
