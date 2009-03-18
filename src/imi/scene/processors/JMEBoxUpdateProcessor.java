/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scene.processors;

import com.jme.image.Texture2D;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.TextureRenderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.util.Timer;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 *
 * @author ptruong
 */
public class JMEBoxUpdateProcessor extends ProcessorComponent {
    /** Used for calculating the time step **/
    private double          m_oldTime       = 0.0;
    private Box             m_realBox       = null;
    private Box             m_monkeyBox     = null;
    private Node            m_fakeNode      = null;
    private boolean         m_synchronizer  = false;

    private TextureRenderer m_Renderer      = null;
    private Texture2D       m_fakeTex       = null;
    private Quaternion      m_rotQuat       = new Quaternion();
    private Quaternion      m_rotMBQuat     = new Quaternion();
    private Vector3f        m_axis          = new Vector3f(1, 1, 0.5f);
    private float           m_angle         = 0;
    private float           m_angle2        = 0;
    private Timer           m_timer         = Timer.getTimer();
    private float           m_tpf           = 0.0f;

    /**
     * Construct a new instance and provide updates to the character
     * @param box1
     * @param box2
     * @param fakeNode
     * @param renderer
     * @param fakeTex
     */
    public JMEBoxUpdateProcessor(Box box1, Box box2, Node fakeNode, TextureRenderer renderer, Texture2D fakeTex)
    {
        m_realBox   = box1;
        m_monkeyBox = box2;
        m_fakeNode  = fakeNode;
        m_Renderer  = renderer;
        m_fakeTex   = fakeTex;
    }

    @Override
    public void compute(ProcessorArmingCollection collection) {

        if (!m_synchronizer) {
                m_synchronizer = true;
                double newTime = System.nanoTime() / 1000000000.0;
                double deltaTime = (double) (newTime - m_oldTime);
                m_oldTime = newTime;
                if (deltaTime > 1.0f)
                    deltaTime = 0.0f;
                // Add code to update the box here
                simpleUpdate();
                m_synchronizer = false;
            }
    }

    @Override
    public void commit(ProcessorArmingCollection collection) {
        // can't do this here because its not part of the ogl thread
        // setting this to run in the renderer as true does not make it run
        // in the renderer.
        //renderMe();
    }

    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }

    public void simpleUpdate() {
        m_timer.update();
        m_tpf   = m_timer.getTimePerFrame();

        if (m_tpf < 1) {
          m_angle = m_angle + (m_tpf * -.25f);
          m_angle2 = m_angle2 + (m_tpf * 1);
          if (m_angle < 0) {
            m_angle = 360 - .25f;
          }
          if (m_angle2 >= 360) {
            m_angle2 = 0;
          }
        }

        m_rotQuat.fromAngleAxis(m_angle, m_axis);
        m_rotMBQuat.fromAngleAxis(m_angle2, m_axis);

        m_realBox.setLocalRotation(m_rotQuat);
        m_monkeyBox.setLocalRotation(m_rotMBQuat);
        m_fakeNode.updateGeometricState(0.0f, true);
    }

    public void renderMe() {
        m_Renderer.render(m_fakeNode, m_fakeTex);
    }
}
