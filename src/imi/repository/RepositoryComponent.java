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

/**
 * This interface provides the repository only functionality
 * @author Ronald E Dahlgren
 */
public interface RepositoryComponent
{
    /**
     * Initialize the component. This method is called by the repository when a
     * component is initially added.
     * @return True to indicate success
     */
    public boolean initialize();

    /**
     * Shutdown the component. This method is called by the repository when a
     * component is removed.
     * @return True to indicate success
     */
    public boolean shutdown();
}
