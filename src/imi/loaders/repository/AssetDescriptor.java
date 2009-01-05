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
package imi.loaders.repository;

import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.utils.FileUtils;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;

/**
 * This class provides information about an asset that is requested 
 * for loading. It provides a list of URLs to point to the locations
 * of the asset, as well as a type indicator.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class AssetDescriptor 
{
    /** Type indicator **/
    private SharedAssetType m_type  = SharedAssetType.Unknown;
    /** Collection of locations **/
    private final FastList<URL>  m_URLList  = new FastList<URL>();
    
    /**
     * Construct a new instance
     * @param type The type of asset this is
     * @param location The resource location
     */
    public AssetDescriptor(SharedAssetType type, URL location) 
    {
        m_type = type;
        if (location != null)
            m_URLList.add(location);
    }
    
    /**
     * Construct a new instance. The conversion from string to URL should be
     * avoided if possible, as it causes a performance hit as well as throwing
     * possible exceptions
     * @param type The type of asset this is
     * @param relativeFilePath A relative file path. This is internally converted
     * to a file URL with the current working directory glued to the front. Keep
     * this in mind to avoid <code>exception</code>al situations.
     */
    public AssetDescriptor(SharedAssetType type, String relativeFilePath)
    {
        m_type = type;

        URL newURL = FileUtils.convertRelativePathToFileURL(relativeFilePath);

        if (newURL != null)
            m_URLList.add(newURL);
        else
            m_URLList.clear();
    }
    
    /**
     * Construct a new instance pointing to a local file. This specified file
     * is converted into URL form within this method. This conversion is a minor
     * performance hit and should be avoided if possible on the caller's end.
     * @param type The type of asset this is.
     * @param fileLocation The location of the resource on the local machine
     */
    public AssetDescriptor(SharedAssetType type, File fileLocation)
    {
        m_type = type;
        try
        {
            m_URLList.add(fileLocation.toURI().toURL());
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(AssetDescriptor.class.getName()).log(Level.SEVERE,
                    "Malformed URL: " + fileLocation.toString(), ex);
            m_URLList.clear();
        }
    }
    
    /**
     * Construct a new instance
     * @param type The type of asset this is
     * @param locations List of URL locations for resources
     */
    public AssetDescriptor(SharedAssetType type, URL ... locations) 
    {
        m_type = type;
        if (locations != null)
        {
            for (URL location : locations)
                m_URLList.add(location);
        }
    }
        
    /**
     * Retrieve the primary (first) location
     * @return The location
     */
    public URL getLocation() {
        return m_URLList.get(0);
    }
    
    /**
     * Retrieve the specified location URL
     * @param index The index of the location of interest
     * @return URL location
     */
    public URL getLocation(int index) {
        return m_URLList.get(index);
    }

    /**
     * Set the primary (first) location
     * @param location The URL
     */
    public void setLocation(URL location) {
        m_URLList.clear();
        m_URLList.add(location);
    }
    
    /**
     * Clear the internal list of resource locations.
     */
    public void clearLocationList()
    {
        m_URLList.clear();
    }
    
    /**
     * Add the specified URL to the end of the location collection
     * @param location
     */
    public void addLocation(URL location)
    {
        m_URLList.add(location);
    }

    /**
     * Retrieve the type of asset this descriptor refers to.
     * @return
     */
    public SharedAssetType getType() {
        return m_type;
    }

    /**
     * Set the type of asset this is.
     * @param type
     */
    public void setType(SharedAssetType type) {
        m_type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (m_type == SharedAssetType.Unknown)
            return false;
        
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AssetDescriptor other = (AssetDescriptor) obj;
        if (this.m_type != other.m_type) {
            return false;
        }
        if (this.m_URLList != other.m_URLList && (this.m_URLList == null || !this.m_URLList.equals(other.m_URLList))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.m_type != null ? this.m_type.hashCode() : 0);
        hash = 61 * hash + (this.m_URLList != null ? this.m_URLList.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString()
    {
        String location = null;
        if (m_URLList.size() > 0)
            location = new String(m_URLList.get(0).toString());
        else
            location = new String("No location associated.");
        return new String(m_type.toString() + " : " + location);
    }
    
    
}
