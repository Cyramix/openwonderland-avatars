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
package imi.scene.processors;

import imi.scene.JScene;
import imi.utils.input.InputScheme;

/**
 *
 * Interface for EventProcessors which provide the link between raw events
 * and the InputSchemes.
 *
 * @author paulby
 */
public interface JSceneEventProcessor {

    void addScheme(InputScheme scheme);

    void clearSchemes();

    InputScheme getInputScheme();

    JScene getJScene();

    InputScheme setDefault(InputScheme defaultScheme);

    void setJScene(JScene jscene);

}
