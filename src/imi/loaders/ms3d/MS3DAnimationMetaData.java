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
package imi.loaders.ms3d;

import imi.scene.animation.AnimationCycle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;

/**
 * This class handles the loading and parsing of an MS3D animation meta-data file.
 * This file is used to enumerate the different animation cycles within the animation loops,
 * the animations are presented as a single loop. There is a strong 
 * relationship between a metadata file and the file it is describing, so be sure
 * the files match. Otherwise the behavior is undefined 
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class MS3DAnimationMetaData 
{
    /** The location of the animation meta data. **/
    private URL m_metaDataLocation = null; 
    /** List of animation cycles**/
    private FastList<AnimationCycle> m_AnimLoopCycles = new FastList<AnimationCycle>();
    
    /**
     * Construct a new instance
     * @param fileToRead
     */
    public MS3DAnimationMetaData (URL fileToRead)
    {
        loadFile(fileToRead);
    }
    
    /**
     * This method reads in the data file and populates data members accordingly
     * The file format is assumed to be
     * animLoopName StartTime=0.0 EndTime=0.55
     * Any superfluous whitespace or characters will result in undefined behavior ;)
     */
    private void process()
    {
        if (m_metaDataLocation == null)
            return;
        
        boolean bLoaded = false;
        
        try 
        {
            BufferedReader inFile = new BufferedReader(new InputStreamReader(m_metaDataLocation.openStream()));

            String currentLine;
            while ((currentLine = inFile.readLine()) != null) 
            {
                AnimationCycle currentCycle = new AnimationCycle();
                // Process the line by splitting on whitespace
                String[] fields = currentLine.split(" ");
                // name is now index 0
                currentCycle.setName(new String(fields[0]));
                

                // add this description in
                m_AnimLoopCycles.add(currentCycle);
            }
            inFile.close();
            bLoaded = true;
        } 
        catch (IOException e) 
        {
            Logger.getLogger(MS3DAnimationMetaData.class.getName()).log(Level.SEVERE, "Unable to open" + m_metaDataLocation.toString());
            bLoaded = false;
        }

    }

    /**
     * Retrieve the array of AnimationCycles represented by the loaded file.
     * @return
     */
    public AnimationCycle [] getCycles()
    {
        AnimationCycle[] cycles = new AnimationCycle[m_AnimLoopCycles.size()];
        return m_AnimLoopCycles.toArray(cycles);
    }
    
    public void loadFile(URL fileToRead)
    {
        // clear old data
        m_AnimLoopCycles.clear();
        // Hook it up
        m_metaDataLocation = fileToRead;
        process();
    }
}
