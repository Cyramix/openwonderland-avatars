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
package imi.tests;

import imi.scene.shader.dynamic.GLSLCompileException;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.dynamic.GLSLUnsatisfiedDependencyException;
import imi.scene.shader.programs.NormalMapShader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;


/**
 * This class provides the main testing framework
 * for the dynamic shader generation tech.
 * @author Ronald E Dahlgren
 */
public class DynamicShaderGenerationTest 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        GLSLShaderProgram myProgram = new NormalMapShader(new WorldManager("The World!"));
//        myProgram.setUseDefaultInitializers(true);
//        //myProgram.addEffect(new SimpleFTransform_Transform());
//        myProgram.addEffect(new VertexDeformer_Transform());
//        myProgram.addEffect(new UnlitTexturing_Lighting());
//        myProgram.addEffect(new SimpleNdotL_Lighting());
        try
        {

            myProgram.compile();
        } 

        catch (GLSLUnsatisfiedDependencyException ex)
        {
            Logger.getLogger(DynamicShaderGenerationTest.class.getName()).log(Level.SEVERE, "Unsatisfied Dependency: " + ex.getMessage());
        }        catch (GLSLCompileException ex)
        {
            Logger.getLogger(DynamicShaderGenerationTest.class.getName()).log(Level.SEVERE, "Compile Error: " + ex.getMessage());
        }
        
        System.out.println(myProgram.getVertexProgramSource());
        System.out.println(myProgram.getFragmentProgramSource());
    }

}
