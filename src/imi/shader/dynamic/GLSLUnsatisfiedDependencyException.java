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
package imi.shader.dynamic;

/**
 * This exception represents a dependency in the code that has not been
 * satisfied and for whatever reason, cannot be automatically resolved.
 * @author Ronald E Dahlgren
 */
public class GLSLUnsatisfiedDependencyException extends GLSLCompileException
{
    public GLSLUnsatisfiedDependencyException(String message)
    {
        super(message);
    }
}
