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


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Chris Nagle
 */
public class FileUtils
{
    public static final File rootPath = new File(System.getProperty("user.dir"));
 
    //  Returns a string containing just the short filename.
    public static final String getShortFilename(String fullFilename)
    {
        if (fullFilename == null)
            return null;

        int lastDirectoryDividerIndex = fullFilename.lastIndexOf('/');
        if (lastDirectoryDividerIndex == -1)
            lastDirectoryDividerIndex = fullFilename.lastIndexOf('\\');

        if (lastDirectoryDividerIndex == -1)
            return(fullFilename);

        String shortFilename = null;
        if (lastDirectoryDividerIndex != -1)
            shortFilename = fullFilename.substring(lastDirectoryDividerIndex+1, fullFilename.length());
        else
            shortFilename = new String(shortFilename);

        return(shortFilename);
    }

    public static URL convertRelativePathToFileURL(String relativePath)
    {
       File newFile = new File(System.getProperty("user.dir"), relativePath);
       URL result = null;
       try 
       {
           result = newFile.toURI().toURL();
       }
       catch (MalformedURLException ex)
       {
           Logger.getLogger(FileUtils.class.toString()).log(Level.SEVERE, "Unable to convert to URL: " + ex.getMessage());
       }
       return result;
//       String currentDirectory = System.getProperty("user.dir");
//       // determine if we are on a non-windows system
//       if (currentDirectory.startsWith("/") == false)
//           currentDirectory = "/" + currentDirectory; // Add on the initial slash
//       // now generate the url
//       String urlString = new String("file://localhost" + currentDirectory + "/" +  relativePath);
//       URL result = null;
//       try 
//       {
//           result = new URL(urlString);
//       }
//       catch (MalformedURLException ex)
//       {
//           Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE,
//                    "Malformed URL: " + urlString);
//       }
//       return result;
    }

    //  Returns a string containing the directory path of the file.
    public static final String getDirectoryPath(String fullFilename)
    {
        if (fullFilename == null)
            return("");

        int lastDirectoryDividerIndex = fullFilename.lastIndexOf('/');
        if (lastDirectoryDividerIndex == -1)
            lastDirectoryDividerIndex = fullFilename.lastIndexOf('\\');

        if (lastDirectoryDividerIndex == -1)
            return("");

        return(fullFilename.substring(0, lastDirectoryDividerIndex));
    }


    //  Builds of list of filenames in the specified directory.
    public static final ArrayList getAllChildFilenames(String baseDirectory)
    {
        File rootDirectory = new File(baseDirectory);

        ArrayList childFilenames = new ArrayList();

        File [] allChildren = rootDirectory.listFiles();
        for (int i=0; i<allChildren.length; i++)
        {
            if (!allChildren[i].isDirectory() && !allChildren[i].isHidden())
            {
                String filePath = allChildren[i].getPath();
                childFilenames.add(filePath);
            }
        }

        return(childFilenames);
    }

    //  Builds of list of directories in the specified directory.
    public static final ArrayList getAllChildDirectories(String baseDirectory)
    {
        File rootDirectory = new File(baseDirectory);

        ArrayList childDirectories = new ArrayList();

        File [] allChildren = rootDirectory.listFiles();
        for (int i=0; i<allChildren.length; i++)
        {
            if (allChildren[i].isDirectory() && !allChildren[i].isHidden())
            {
                String filePath = allChildren[i].getPath();
                childDirectories.add(filePath);
            }
        }

        return(childDirectories);
    }
    
    //  Attempts to find a file in a directory and it's sub directories.
    public static final String findFile(String baseDirectory, String shortFilename)
    {
        String fullFilename = baseDirectory + File.separator + shortFilename;
        File physicalFile = new File(fullFilename);
        
        //  Does the file exist in this directory.
        if (physicalFile.exists())
            return(fullFilename);

        
        //  Otherwise, search through all subdirectories.
        ArrayList subDirectories = FileUtils.getAllChildDirectories(baseDirectory);
        
        
        for (int i=0; i<subDirectories.size(); i++)
        {
            String subBaseDirectory = (String)subDirectories.get(i);
            
            fullFilename = FileUtils.findFile(subBaseDirectory, shortFilename);
            if (fullFilename.length() != 0)
                return(fullFilename);
        }

        return("");
    }
    
    //  Attempts to find the Texture file.
    public static final String findTextureFile(String shortTextureFilename)
    {
        String fullFilename = FileUtils.findFile("assets/", shortTextureFilename);
        if (fullFilename.length() == 0)
            return("");

//        fullFilename = fullFilename.substring(4, fullFilename.length());

        return(fullFilename);
    }

}
