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
 */package imi.loaders;

import imi.scene.SkeletonNode;
import imi.utils.AvatarObjectInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used for loading serialized binary head file (bhf) packs.
 *
 * @author Ronald E Dahlgren
 */
public final class BinaryHeadFileImporter
{
    /** Disabled **/
    private BinaryHeadFileImporter() {}

    /**
     * Load the binary head contained in the given InputStream.
     * <p>This InputStream should come from a FileInputStream or a call
     * to URL's openConnection() method on a valid BHF file.</p>
     * @param stream A valid and non-null input stream
     * @return The loaded head skeleton
     * @throws IOException On stream error
     * @throws IllegalArgumentException If stream == null, or if stream is not a valid bhf
     */
    public static SkeletonNode loadHeadFile(InputStream stream) throws IOException {
        if (stream == null)
            throw new IllegalArgumentException("Null stream provided!");

        SkeletonNode result = null;
        AvatarObjectInputStream inStream = new AvatarObjectInputStream(stream);
        try {
            result = (SkeletonNode) inStream.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Stream was not a valid BHF file.");
        }
        return result;
    }
}
