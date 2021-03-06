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

import com.jme.image.Texture;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * This interface provides a common way to talk to something that exhibits
 * caching behavior.
 * @author Ronald E Dahlgren
 */
public interface CacheBehavior
{
    /**
     * Initialize this cache with the provided parameters.
     * @param params Collection of initialization parameters
     * @return true on success, false otherwise
     */
    public boolean initialize(Object[] params);

    /**
     * Shutdown the repository. 
     * @return True on success, false otherwise.
     */
    public boolean shutdown();

    /**
     * Determine if the file at the specified location is cached.
     * @param location
     * @return True if cached, false otherwise.
     */
    public boolean isFileCached(URL location);

    /**
     * Open a stream to the requested resource.
     * @param location
     * @return InputStream to the resource, or null if no resource located.
     */
    public InputStream getStreamToResource(URL location);

    /**
     * Get an output stream for writing to the cache. The stream will be used
     * @param location
     * @return
     */
    public OutputStream getStreamForWriting(URL location);

    /**
     * Requests the specified texture.
     * @param location The requested texture
     * @return
     */
    public Texture loadTexture(URL location);

    /**
     * This method removes all residue from the cache.
     * @return
     */
    public boolean clearCache();

    /**
     * This method will attempt to create a single file constituting the state of
     * the cache at the specified file. This can later be used to regenerate the
     * cache quickly via loadCachePackage. This method may take a while and should
     * not be called in a performance critical area.
     * @param output Stream that the cache package will be written to
     */
    public void createCachePackage(OutputStream output);

    /**
     * This method will load the specified cache package. This method should be
     * implemented to be as speedy as possible, although it is not intended to be
     * called per frame.
     * @param input Stream the cache package may be read from
     */
    public void loadCachePackage(InputStream input);

}
