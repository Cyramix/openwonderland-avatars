/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scene.processors;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import javolution.util.FastList;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 *
 * @author Lou Hayt
 */
public class JmeGraphProcessor extends ProcessorComponent
{
    FastList<GraphCommand> commands = new FastList<GraphCommand>();

    public void attach(Spatial spat, Node node)
    {
        if (spat != null && node != null)
            commands.add(new GraphCommand(spat, true, node));
    }
    
    public void detach(Spatial spat, Node node)
    {
        if (spat != null && node != null)
            commands.add(new GraphCommand(spat, false, node));
    }

    @Override
    public void compute(ProcessorArmingCollection arg0) {
    }

    @Override
    public void commit(ProcessorArmingCollection arg0) {
        while(!commands.isEmpty())
        {
            GraphCommand gc = commands.removeFirst();
            if (gc.attach)
                gc.node.attachChild(gc.spat);
            else
                gc.node.detachChild(gc.spat);
        }
    }

    @Override
    public void initialize() {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection);
    }

    private class GraphCommand
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
