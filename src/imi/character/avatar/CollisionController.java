/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package imi.character.avatar;

import com.jme.scene.Spatial;
import org.jdesktop.mtgame.JMECollisionSystem;

/**
 *
 * @author paulby
 */
public class CollisionController {

    private boolean collisionEnabled=true;
    private boolean gravityEnabled=true;
    private JMECollisionSystem collisionSystem;
    private Spatial collisionGraph;

    public CollisionController(Spatial collisionGraph, JMECollisionSystem collisionSystem) {
        this.collisionGraph = collisionGraph;
        this.collisionSystem = collisionSystem;
    }

    /**
     * @return the collisionEnabled
     */
    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }

    /**
     * @param collisionEnabled the collisionEnabled to set
     */
    public void setCollisionEnabled(boolean collisionEnabled) {
        this.collisionEnabled = collisionEnabled;
    }

    /**
     * @return the collisionSystem
     */
    public JMECollisionSystem getCollisionSystem() {
        return collisionSystem;
    }

    /**
     * @return the collisionGraph
     */
    public Spatial getCollisionGraph() {
        return collisionGraph;
    }

    /**
     * @return the gravityEnabled
     */
    public boolean isGravityEnabled() {
        return gravityEnabled;
    }

    /**
     * @param gravityEnabled the gravityEnabled to set
     */
    public void setGravityEnabled(boolean gravityEnabled) {
        this.gravityEnabled = gravityEnabled;
    }
}
