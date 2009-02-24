/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import net.java.dev.jnag.sgs.common.LogMessageReception;
import net.java.dev.jnag.sgs.common.LogMethodInvocation;
import net.java.dev.jnag.sgs.common.NoLog;
import net.java.dev.jnag.sgs.common.Remote;

@LogMessageReception // Log when the server receives any message of this class.
@LogMethodInvocation // Log when the server calls any message of this class.
public interface ServerSideUser extends Remote 
{   
    public void setClientInfo(String info);
    public void serverCommand(String string);
    public void enterWorld(String worldName);
    public void requestWorldList();
}