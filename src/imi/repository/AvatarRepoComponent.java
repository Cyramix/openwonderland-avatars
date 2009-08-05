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
package imi.repository;

import imi.loaders.BinaryHeadFileImporter;
import imi.scene.PScene;
import imi.scene.SkeletonNode;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;

/**
 * The specialized repository component for the avatar system. This class is
 * MT safe.
 * @author Ronald E Dahlgren
 */
public class AvatarRepoComponent implements RepositoryComponent
{
    /** Logger ref */
    private static final Logger logger = Logger.getLogger(AvatarRepoComponent.class.getName());
    /** My lovely collection of heads... muwahahahha **/
    // Note, strings are "toExternalForm"
    private final Map<String, SkeletonNode> heads = new FastMap();

    /**
     * Retrieve a copy of the head at the specified location.
     * <p>
     * The sceneToAssociateWith is the PScene that the newly created skeleton node
     * will have its meshes associated with. This should be the requesting character's
     * pscene.
     * </p>
     * @param location
     * @param sceneToAssociateWith  A Non-null pscene
     * @return The head, or null if not found
     */
    public SkeletonNode getBinaryHead(URL location, PScene sceneToAssociateWith)
    {
        if (sceneToAssociateWith == null)
            throw new NullPointerException("sceneToAssociateWith was null.");
        
        SkeletonNode result = heads.get(location.toExternalForm());
        if (result == null) {
            try {
                result = BinaryHeadFileImporter.loadHeadFile(location.openStream());
                heads.put(location.toString(), result);
                result = duplicateSkeleton(result, sceneToAssociateWith);
            } catch (IOException ex) {
                logger.severe("IOE while loading " + location + ", ex: " + ex.getMessage());
            }
        }
        else
            result = duplicateSkeleton(result, sceneToAssociateWith);
        
        return result;
    }

    public boolean initialize() {
        logger.info("Initialize...");
        return true;
    }

    public boolean shutdown() {
        logger.info("Shutdown...");
        heads.clear();
        return true;
    }

    private SkeletonNode duplicateSkeleton(SkeletonNode input, PScene pscene) {
        SkeletonNode result = null;
        result = input.deepCopy(pscene);
        // Copy animations
//        for (int i = 0; i < input.getAnimationGroupCount(); ++i)
//            result.getAnimationComponent().addGroup(input.getAnimationGroup(i));
        return result;
    }

}
