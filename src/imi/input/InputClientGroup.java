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

package imi.input;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javolution.util.FastList;

/**
 * May contain multiple control schemes under a single input client
 *
 * @author Lou Hayt
 */
public class InputClientGroup implements InputClient
{
    private InputClient m_scheme = null;
    private FastList<InputClient> m_schemeList = new FastList<InputClient>();

    /**
     * Switch the control to the next input scheme
     */
    public void nextScheme()
    {
        if (m_schemeList.isEmpty())
            return;
        int index = m_schemeList.indexOf(m_scheme);
        index++;
        if (index > m_schemeList.size()-1)
            m_scheme = m_schemeList.get(0);
        else
            m_scheme = m_schemeList.get(index);
    }

    /**
     * Switch the control to the previous input scheme
     */
    public void previousScheme()
    {
        if (m_schemeList.isEmpty())
            return;
        int index = m_schemeList.indexOf(m_scheme);
        index--;
        if (index < 0)
            m_scheme = m_schemeList.get(m_schemeList.size()-1);
        else
            m_scheme = m_schemeList.get(index);
    }

    /**
     * Clean the control schemes
     */
    public void clearSchemes()
    {
        if (m_schemeList.isEmpty())
            return;
        m_scheme = m_schemeList.get(0);
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
    }

    /**
     * Set a single input scheme
     */
    public InputClient setScheme(InputClient defaultScheme)
    {
        m_scheme = defaultScheme;
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
        return m_scheme;
    }

    /**
     * Add a control scheme
     */
    public void addScheme(InputClient scheme) {
        m_schemeList.add(scheme);
    }

    /**
     * Remove a control scheme if it is not the current one
     * @param scheme
     * @return true if successfull
     */
    public boolean removeScheme(InputClient scheme)
    {
        if (m_scheme != scheme)
            m_schemeList.remove(scheme);
        else
            return false;
        return true;
    }

    /**
     * Get the current input scheme
     * @return
     */
    public InputClient getInputScheme() {
        return m_scheme;
    }

    /**
     * {@inheritDoc InputClient}
     */
    public void processKeyEvent(KeyEvent keyEvent) {
        if (m_scheme != null)
            m_scheme.processKeyEvent(keyEvent);
    }

    /**
     * {@inheritDoc InputClient}
     */
    public void processMouseEvent(MouseEvent mouseEvent) {
        if (m_scheme != null)
            m_scheme.processMouseEvent(mouseEvent);
    }

    public void focusChanged(boolean currentlyInFocus) {
        if (m_scheme != null)
            m_scheme.focusChanged(currentlyInFocus);
    }
}
