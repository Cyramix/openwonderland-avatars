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
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package imi.demos;

import com.jme.math.Vector3f;
import imi.objects.AvatarsNPCsDemo;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class demonstrates some rudimentary avatar behaviors.
 * @author Lou
 */
public class NPCavatarsDemo extends DemoBase
{
    /**
     * Construct and run the test.
     * @param args
     */
    public NPCavatarsDemo(String[] args){
        super(args);
    }

    /**
     * Run the test!
     * @param args
     */
    public static void main(String[] args) {
        new NPCavatarsDemo(args);
    }

    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        new AvatarsNPCsDemo(wm, new Vector3f(0.0f, 0.0f, 0.0f));
    }
}
