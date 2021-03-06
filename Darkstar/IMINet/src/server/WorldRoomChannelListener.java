/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple example {@link ChannelListener} from the Project Darkstar Server.
 * <p>
 * Logs when a channel receives data.
 * 
 * @author Lou Hayt
 */
public class WorldRoomChannelListener implements Serializable, ChannelListener
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;

    /** The {@link Logger} for this class. */
    private static final Logger logger =
        Logger.getLogger(WorldRoomChannelListener.class.getName());

    /**
     * {@inheritDoc}
     * <p>
     * Logs when data arrives on a channel. A typical listener would 
     * examine the message to decide whether it should be discarded, 
     * modified, or sent unchanged.
     */
    public void receivedMessage(Channel channel, 
                                ClientSession session, 
                                ByteBuffer message)
    {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO,
                "Channel message from {0} on channel {1}",
                new Object[] { session.getName(), channel.getName() }
            );
        }
        channel.send(session, message);
    }
}
