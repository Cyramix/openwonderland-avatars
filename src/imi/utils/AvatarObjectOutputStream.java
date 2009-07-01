/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package imi.utils;

import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationCycle;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.channel.PMatrix_JointChannel;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.HashMap;
import javolution.util.FastCollection;
import javolution.util.FastComparator;
import javolution.util.FastList;
import org.collada.colladaschema.Animation;

/**
 * A specialized ObjectInputStream that reduces the size of serialized core
 * wonderland objects. For known classes this stream stores a (int) id instead
 * of the large serialization class header. For unknown classes it stores
 * the class name in the stream, which again is usually much smaller than 
 * the serialization header. 
 * 
 * @author paulby
 */
public class AvatarObjectOutputStream extends ObjectOutputStream {

    protected static final int UNKNOWN_DESCRIPTOR = Integer.MIN_VALUE;
    protected static final int firstID = UNKNOWN_DESCRIPTOR+1;
    
    private static HashMap<String, Integer> descToId = new HashMap();
        
    // XXX replace with Strings to avoid problems with clients that
    // don't have JME.  This is prone to typos, so should be replaced
    // by a more automatic system XXX
    private static String[] coreClass = new String[] {
        AnimationGroup.class.getName(),
        FastList.class.getName(),
        FastCollection.class.getName(),
        "javolution.util.FastComparator$Default",   // Hidden subclass / Nested class
        FastComparator.class.getName(),
        PMatrix_JointChannel.class.getName(),
        AnimationCycle.class.getName(),
        AnimationComponent.class.getName(),
        PMatrix.class.getName(),
        PNode.class.getName(),
        PJoint.class.getName(),
        SkinnedMeshJoint.class.getName(),
        SkeletonNode.class.getName()
    };
  
    static {
        populateDescToId(descToId);
    }
    
    public AvatarObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        
        // Now send the users class descriptor
        Integer idObj = descToId.get(desc.getName());
        if (idObj == null) {
//            System.err.println("First classDescriptor for " + desc.getName() + "  " + descToId.size());
            writeInt(UNKNOWN_DESCRIPTOR);
            writeUTF(desc.forClass().getName());
        } else {
            writeInt(idObj);
        }
    }
    
    static void populateDescToId(HashMap<String, Integer> map) {
        int id = firstID;
        for(String clazz : coreClass) {
            map.put(clazz, id++);
        }
    }
    
    static void populateIdToDesc(HashMap<Integer, String> map) {
        int id = firstID;
        for(String clazz : coreClass) {
            map.put(id++, clazz);
        }
        
    }
}
