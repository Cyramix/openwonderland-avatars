package imi.environments;

import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMeshInstance;

/**
 * This class takes in a root PNode and proceeds to clone and copy the
 * @author Ronald E Dahlgren
 */
public class SceneGraphConvertor 
{
    /** This is the root of the converted jME scene graph **/
    private     Node    m_jmeRoot   = null;
    /** This is the root of the graph we will be converting **/
    private     PNode   m_root      = null;
    
    /**
     * Default constructor 
     */
    public SceneGraphConvertor()
    {
        
    }
    
    /**
     * This method takes the given root node and builds an equivalent jME graph
     * of similar components.
     * @param root
     * @return The root of the jME equivalent graph
     */
    public Node convert(PNode root)
    {
        clear();
        
        m_root = root;
        // do the conversion
        m_jmeRoot = processNode(m_root);
        
        return m_jmeRoot;
    }
    
    /**
     * Clears any state maintained by this object.
     */
    private void clear()
    {
        m_jmeRoot = null;
        m_root = null;
    }
    
     /*****************************
     ** Node processing methods. **
     *****************************/
    
    private Node processNode(PNode node)
    {  
        Node result = null;
        // chain instanceof checks as necessary, currently only mesh instances
        // and regular nodes are relevant
        if (node instanceof PPolygonMeshInstance)
            return processMeshInstance((PPolygonMeshInstance)node);
         
        // just a regular node, be sure to include the transform
        result = new Node(node.getName());
        
        if (node.getTransform() != null)
            applyTransform(node.getTransform().getLocalMatrix(false), result);
        
        for (int i = 0; i < node.getChildrenCount(); i++)
        {
            PNode kid = node.getChild(i);
            result.attachChild(processNode(kid));
        }
        
        return result;
    }
    
    private Node processMeshInstance(PPolygonMeshInstance meshInst)
    {
        SharedMesh mesh  = meshInst.getSharedMesh();//new SharedMesh(meshInst.getName(), meshInst.getGeometry().getGeometry());
        
        // apply transform to a transform node
        Node transformNode = new Node(meshInst.getName() + " transform");
        applyTransform(meshInst.getTransform().getLocalMatrix(false), transformNode);
        // attach shared mesh to that transform node and return it
        transformNode.attachChild(mesh);
        
        return transformNode;
    }
    
    /**
     * Utility method to assign the rotation, translation, and scale components
     * to the jme node provided
     * @param transform Transform to apply
     * @param jmeNode Node to affect
     */
    private void applyTransform(PMatrix transform, Node jmeNode)
    {
        jmeNode.setLocalRotation(transform.getRotationJME());
        jmeNode.setLocalTranslation(transform.getTranslation());
        jmeNode.setLocalScale(transform.getScaleVector());
    }
}
