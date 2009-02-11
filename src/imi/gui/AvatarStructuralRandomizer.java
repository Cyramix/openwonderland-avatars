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

/*
 * AvatarStructuralRandomizer.java
 *
 * Created on Feb 06, 2009, 12:01:00 PM
 */

package imi.gui;
////////////////////////////////////////////////////////////////////////////////
// Imports

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

////////////////////////////////////////////////////////////////////////////////

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class AvatarStructuralRandomizer {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private SceneEssentials m_sceneData = null;
    private NumberFormat    m_format    = new DecimalFormat("0.000");

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public double getRandomTranslation() {
        Random rand1    = new Random();
        Random rand     = new Random(rand1.nextLong());

        int iRand       = rand.nextInt(2);
        double dRand    = rand.nextDouble();
        dRand           = Double.valueOf(m_format.format(dRand));
        return dRand;
    }

    public double getRandomScale() {
        Random rand1    = new Random();
        Random rand     = new Random(rand1.nextLong());

        double dRand    = rand.nextDouble();
        dRand           = 1.0 + dRand;
        dRand           = Double.valueOf(m_format.format(dRand));
        return dRand;
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public NumberFormat getNumberFormat() {
        return m_format;
    }

    public SceneEssentials getSceneData() {
        return m_sceneData;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setNumberFormat(NumberFormat format) {
        m_format = format;
    }

    public void setSceneData(SceneEssentials scene) {
        m_sceneData = scene;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////
}
