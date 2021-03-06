/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.utils;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import javolution.util.FastList;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class JmeGraphProcessor extends ProcessorComponent
{
    private WorldManager wm = null;
    FastList<GraphCommand> commands = new FastList<GraphCommand>();
    FastList<Node> clearChildrenNodes = new FastList<Node>();

    public JmeGraphProcessor(WorldManager wm)
    {
        super();
        this.wm = wm;
    }

    public synchronized void attach(Spatial spat, Node node)
    {
        if (spat != null && node != null)
            commands.add(new GraphCommand(spat, true, node));
    }

    public synchronized void clearChildren(Node node) {
        if (node != null && node.getChildren() != null)
            clearChildrenNodes.add(node);
    }
    
    public synchronized void detach(Spatial spat, Node node)
    {
        if (spat != null && node != null)
            commands.add(new GraphCommand(spat, false, node));
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public void compute(ProcessorArmingCollection arg0) {
    }

    @Override
    public synchronized void commit(ProcessorArmingCollection arg0)
    {
        while(!commands.isEmpty())
        {
            GraphCommand gc = commands.removeFirst();
            if (gc == null)
            {
                System.out.println("recieved null command");
                return;
            }
            if (gc.attach)
            {
                gc.node.attachChild(gc.spat);
                wm.addToUpdateList(gc.spat); // need to inherit the states (lighting!)

            }
            else
                gc.node.detachChild(gc.spat);
        }
        while(!clearChildrenNodes.isEmpty())
        {
            Node node = clearChildrenNodes.removeFirst();
            node.getChildren().clear();
        }
    }

    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }

    private static class GraphCommand
    {
        public Spatial spat;
        public Node    node;
        public boolean attach; // or detach

        public GraphCommand(Spatial spat, boolean attach, Node node)
        {
            this.spat    = spat;
            this.node    = node;
            this.attach = attach;
        }
    }

}
