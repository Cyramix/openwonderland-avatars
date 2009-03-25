/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.scene;

import com.jme.scene.state.RenderState;

/**
 *
 * @author Ronald E Dahlgren
 */
public class GhostNode extends SharedNode
{
    public GhostNode() {
        super();
    }

    /**
     * Constructor creates a new <code>SharedNode</code> object.
     *
     * @param name
     *            the name of this shared mesh.
     * @param target
     *            the Node to share the data.
     */
    public GhostNode(String name, Node target) {
        super(name, target);
    }

    /**
     * Constructor creates a new <code>SharedNode</code> object.
     *
     * @param target
     *            the Node to share the data.
     */
    public GhostNode(Node target) {
        super(target);
    }

    protected void copyNode(Node original, Node copy) {
        copy.setName(original.getName());
        copy.setCullHint(original.getLocalCullHint());
        copy.setLightCombineMode(original.getLocalLightCombineMode());
        /** Copy the references! **/
        copy.localScale = original.localScale;
        copy.localRotation = original.localRotation;
        copy.localTranslation = original.localTranslation;
        copy.setRenderQueueMode(original.getLocalRenderQueueMode());
        copy.setTextureCombineMode(original.getLocalTextureCombineMode());
        copy.setZOrder(original.getZOrder());


        for (RenderState.StateType type : RenderState.StateType.values()) {
            RenderState state = original.getRenderState( type );
            if (state != null) {
                copy.setRenderState(state );
            }
        }
    }

}
