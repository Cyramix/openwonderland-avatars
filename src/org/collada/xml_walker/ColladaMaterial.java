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
package org.collada.xml_walker;

import java.util.ArrayList;
import java.util.List;
import org.collada.colladaschema.Asset;
import org.collada.colladaschema.Extra;
import org.collada.colladaschema.InstanceEffect;

/**
 * This class represents a COLLADA material
 * @author Ronald E Dahlgren
 */
public class ColladaMaterial
{
    private String          m_ID                = null;
    private String          m_name              = null;
    private Asset           m_asset             = null;
    private InstanceEffect  m_instanceEffect    = null;

    private ArrayList<Extra> m_extra = null;

    public ColladaMaterial()
    {

    }

    public ColladaMaterial(String name, String id, Asset asset, InstanceEffect instanceEffect, List<Extra> extras)
    {
        m_name = name;
        m_ID = id;
        m_asset = asset;
        m_instanceEffect = instanceEffect;
        if (extras != null)
        {
            m_extra = new ArrayList<Extra>();
            for (Extra extra : extras)
                m_extra.add(extra);
        }
    }

    public Asset getAsset() {
        return m_asset;
    }

    public void setAsset(Asset asset) {
        this.m_asset = asset;
    }

    public List<Extra> getExtra() {
        return m_extra;
    }

    public void setExtra(List<Extra> extras) {
        if (extras != null)
        {
            extras = new ArrayList<Extra>();
            for (Extra extra : extras)
                m_extra.add(extra);
        }
    }

    public InstanceEffect getInstanceEffect() {
        return m_instanceEffect;
    }

    public void setInstanceEffect(InstanceEffect instanceEffect) {
        this.m_instanceEffect = instanceEffect;
    }

    public String getID() {
        return m_ID;
    }

    public void setID(String ID) {
        this.m_ID = ID;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        this.m_name = name;
    }

    public String getInstanceEffectTargetURL()
    {
        String result = m_instanceEffect.getUrl();
        if (result.startsWith("#"))
            result = result.substring(1);
        return result;
    }
}
