/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import net.java.dev.jnag.sgs.common.Remote;

/**
 *
 * @author Lou Hayt
 */
public abstract class WorldPlayerExtension implements Serializable, ManagedObject
{
    public abstract void releaseJNagSession();
    public abstract Remote getClientProxy();
    
    public void trigger(boolean pressed, int trigger) {}
    public void playerRemoved(WorldPlayer player) {}
}
