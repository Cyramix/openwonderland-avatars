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
 * JPanel_HorizontalSliderT.java
 *
 * Created on Dec 17, 2008, 10:52:18 AM
 */

package imi.gui;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JPanel_HorizontalSliderT extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
    public float                                m_baseSliderVal     =   25.0f;
    private GUI_Enums.sliderControl           m_ObjectRef         =   null;
    private JFrame                              m_ParentFrame       =   null;
    private boolean                             m_SliderInFocus     =   false;
    private boolean                             m_SpinnerInFocus    =   false;
    private float                               m_curr              =   0.0f;
    private float                               m_prev              =   0.0f;
    private NumberFormat                        m_format            =   new DecimalFormat("0.00");
    private String                              m_formattedNumber   =   null;

////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initializes GUI components and sets up an event listener
     * for the JSlider and JTextComponent updates.  Eliminates recursive calls when
     * one object updates the other object.
     */
    public JPanel_HorizontalSliderT() {
        initComponents();

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            public void eventDispatched(AWTEvent event) {
                if (event.getID() == FocusEvent.FOCUS_GAINED) {
                    if (event.getSource() instanceof JSlider) {
                        m_SliderInFocus = true;
                    } else if (event.getSource() instanceof JTextComponent) {
                        m_SpinnerInFocus = true;
                    }
                } else if (event.getID() == FocusEvent.FOCUS_LOST) {
                    if (event.getSource() instanceof JSlider) {
                        m_SliderInFocus = false;
                    } else if (event.getSource() instanceof JTextComponent) {
                        m_SpinnerInFocus = false;
                    }
                }
            }
        }, AWTEvent.FOCUS_EVENT_MASK);
    }

    /**
     * Updates the components based on user input.  After the one component is
     * updated it in turns updates the associated component (ie user controls
     * slider -> slider updates -> scrollbox updates
     * @param evt - changeevent for the object
     */
    public void updateComponents(javax.swing.event.ChangeEvent evt) {
        int     index   = -1;
        float   curVal  = 0.0f;
        float   newVal  = 0.0f;

        if (evt.getSource().equals(jSlider1))
            index = 0;

        if (evt.getSource().equals(jSpinner1))
            index = 1;

        switch(index)
        {
            case 0:
            {
                if (m_SliderInFocus) {
                    curVal = (float)jSlider1.getValue();
                    newVal = (curVal - m_baseSliderVal) / 100;
                    jSpinner1.setValue(newVal);

                    float diff = newVal - m_curr;
                    m_prev = m_curr;
                    m_curr = newVal;
                    m_formattedNumber = m_format.format(diff);

                    if (m_ParentFrame instanceof JFrame_AdvOptions)
                        ((JFrame_AdvOptions)m_ParentFrame).parseModification(m_ObjectRef, Float.valueOf(m_formattedNumber), newVal);
                }
                break;
            }
            case 1:
            {
                if (m_SpinnerInFocus) {
                    curVal = (Float)jSpinner1.getValue();
                    newVal = (curVal * 100) + m_baseSliderVal;
                    jSlider1.setValue((int)newVal);

                    float diff = curVal - m_curr;
                    m_prev = m_curr;
                    m_curr = curVal;
                    m_formattedNumber = m_format.format(diff);

                    if (m_ParentFrame instanceof JFrame_AdvOptions)
                        ((JFrame_AdvOptions)m_ParentFrame).parseModification(m_ObjectRef, Float.valueOf(m_formattedNumber), curVal);
                }
                break;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSlider1 = new javax.swing.JSlider();
        jSpinner1 = new javax.swing.JSpinner();

        setMaximumSize(new java.awt.Dimension(120, 57));
        setMinimumSize(new java.awt.Dimension(120, 57));
        setPreferredSize(new java.awt.Dimension(120, 57));
        setLayout(new java.awt.GridBagLayout());

        jSlider1.setMaximum(50);
        jSlider1.setMinimum(1);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(25);
        jSlider1.setMinimumSize(new java.awt.Dimension(120, 29));
        jSlider1.setPreferredSize(new java.awt.Dimension(120, 29));
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                updateComponents(evt);
            }
        });
        add(jSlider1, new java.awt.GridBagConstraints());

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(-0.25f), Float.valueOf(0.25f), Float.valueOf(0.01f)));
        jSpinner1.setMinimumSize(new java.awt.Dimension(80, 28));
        jSpinner1.setPreferredSize(new java.awt.Dimension(80, 28));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                updateComponents(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jSpinner1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

////////////////////////////////////////////////////////////////////////////////
// ACCESSORS
////////////////////////////////////////////////////////////////////////////////

    public JSlider getSlider() {
        return jSlider1;
    }

    public JSpinner getSpinner() {
        return jSpinner1;
    }

    public GUI_Enums.sliderControl  getObjectRef() {
        return m_ObjectRef;
    }

    public JFrame getParentFrame() {
        return m_ParentFrame;
    }

////////////////////////////////////////////////////////////////////////////////
// MUTATORS
////////////////////////////////////////////////////////////////////////////////

    public void setObjectRef(GUI_Enums.sliderControl object) {
        m_ObjectRef = object;
    }

    public void setParentFrame(JFrame parent) {
        m_ParentFrame = parent;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSpinner jSpinner1;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// HELPER FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

    /**
     * Direct access to the JSlider setup values.  Allows the user to set the
     * minimum and maximum slider values, the tick spacing, wheter or not to show
     * the ticks, wheter or not to snap to ticks and the slider start position
     * @param max - integer maximum range
     * @param min - integer minimum range
     * @param tickspacing - integer spacing between ticks
     * @param paintticks - boolean true to show ticks
     * @param snaptoticks - boolean true for cursor to snap to each tick increment
     * @param initval - integer  initial start value
     */
    public void setJSlider(int max, int min, int tickspacing, boolean paintticks, boolean snaptoticks, int initval) {

        if (max == 0)
            jSlider1.setMaximum(50);
        else
            jSlider1.setMaximum(max);

        if (min >= max)
            jSlider1.setMinimum(1);
        else
            jSlider1.setMinimum(min);

        if (tickspacing <= 0)
            jSlider1.setMinorTickSpacing(1);
        else
            jSlider1.setMinorTickSpacing(tickspacing);

        jSlider1.setPaintTicks(paintticks);

        jSlider1.setSnapToTicks(snaptoticks);

        if (initval < min) {
            m_baseSliderVal = (float)min;
            jSlider1.setValue(min);
        } else {
            m_baseSliderVal = (float)initval;
            jSlider1.setValue(initval);
        }

        jSlider1.setMinimumSize(new java.awt.Dimension(120, 29));

        jSlider1.setPreferredSize(new java.awt.Dimension(120, 29));
    }

    /**
     * Direct access to the JSpinner setup values.  Allows the user to set the
     * minimunm and maximum spinner values, the initial value and the tick increments
     * @param initvalue - float start value for the spinner
     * @param minvalue - float minimum range for the spinner
     * @param maxvalue - float maximum range for the spinner
     * @param tickstep - float increment per tick
     */
    public void setJSpinner(float initvalue, float minvalue, float maxvalue, float tickstep) {
        javax.swing.SpinnerNumberModel spinmodel = new javax.swing.SpinnerNumberModel(initvalue, minvalue, maxvalue, tickstep);
        setJSpinner(spinmodel);
    }

    /**
     * Direct access to the JSpinner setup values.  Allows the user to set the
     * minimunm and maximum spinner values, the initial value and the tick increments
     * @param spinnermodel - model for the spinner to use
     */
    public void setJSpinner(javax.swing.SpinnerModel spinnermodel) {
        jSpinner1.setModel(spinnermodel);
    }
}
