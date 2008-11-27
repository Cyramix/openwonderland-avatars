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
package imi.character.ninja;

import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class NinjaAvatarDressShirt extends NinjaAvatar
{
    public class NinjaAvatarDressShirtAttributes extends NinjaAvatarAttributes
    {   
        public NinjaAvatarDressShirtAttributes(String name, boolean bRandomCustomizations) 
        {
            super(name, bRandomCustomizations);
        }
        
        @Override
        protected void customizeTorsoPresets(int preset, ArrayList<String> delete, ArrayList<String> load, ArrayList<String> add, ArrayList<AttachmentParams> attachments) 
        {
            // Dress shirt
            delete.add("TorsoNudeShape");
            load.add("assets/models/collada/Shirts/DressShirt_M/MaleDressShirt.dae");
            add.add("DressShirtShape");
        }
    }
    
    public NinjaAvatarDressShirt(String name, WorldManager wm) 
    {
        super(name, wm);
    }
    
    @Override
    protected Attributes createAttributes(String name)
    {
        return new NinjaAvatarDressShirtAttributes(name, true);
    }
}
