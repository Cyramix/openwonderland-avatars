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
package org.collada.xml_walker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import imi.loaders.collada.Collada;



/**
 * Creates walker objects from collada schema objects
 *
 * @author paulby
 */
public class ProcessorFactory {
    
    private static Logger logger = Logger.getLogger("org.collada.xml_walker");
    
    private static final String walkerPackage = "org.collada.xml_walker.";
    
    /**
     * Create a procesor to handle this schemaObject
     * @param collada
     * @param schemaObj
     * @param parentProcessor
     * @return Processor or null
     */
    public static Processor createProcessor(Collada collada, Object schemaObj, Processor parentProcessor)
    {
        if (schemaObj==null)
            return null;
        
        Class colladaClass = collada.getClass();
        Class schemaClass = schemaObj.getClass();
        String schemaClassName = schemaClass.getName();
        String schemaObjName = schemaClassName.substring(schemaClassName.lastIndexOf('.')+1);
        if (schemaObjName.indexOf('$')!=0)
            schemaObjName = schemaObjName.substring(schemaObjName.lastIndexOf('$')+1);
        try {            
//            System.out.println("Looking for "+walkerPackage+schemaObjName+"Processor");
            Class walkerClass = Class.forName(walkerPackage+schemaObjName+"Processor");
//            System.out.println("   Found class " + walkerClass);

            //  Get pointer to the Processor constructor.
            Constructor con = walkerClass.getConstructor(new Class[] {colladaClass, schemaClass, Processor.class} );

            //  Create the Processor.
            return (Processor) con.newInstance(collada, schemaObj, parentProcessor);
        } catch (ClassNotFoundException ex) {
            logger.warning("No Handler for "+schemaClass+"  looking for "+schemaObjName);
        } catch (NoSuchMethodException ex) {
            logger.warning("No constructor "+schemaObjName+"("+schemaClassName+")");
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.getCause().printStackTrace();
            ex.printStackTrace();
        }

        return null;
    }
}

