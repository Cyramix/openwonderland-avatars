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
package imi.gui;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSlider;
import javax.swing.Timer;

/**
 * This class allows for simple (yet robust) tweaking of the PMatrix class.
 * @author  Ronald E Dahlgren
 */
public class PMatrixWidget extends javax.swing.JPanel
{
    private PMatrix     m_theMatrix = null; // This is the matrix we are operating on
    
    // Rotation Data caching matrices.
    private PMatrix xRotation = new PMatrix();
    private PMatrix yRotation = new PMatrix();
    private PMatrix zRotation = new PMatrix();
    
    // Refresh timer, periodically causes a document refresh
    private Timer refreshTimer = null;
    
    private boolean bRefreshing = false; // USed to stop incremental refreshing problem
    /** Creates new form PMatrixWidget with a newly generated internal PMatrix (not tied to any existing one)*/
    public PMatrixWidget() 
    {
        m_theMatrix = new PMatrix(); // In case you want it this way...
        initComponents();
        refreshComponents(); // Prime the pump so to speak
        // get our timer started
        refreshTimer = new  Timer(50, new ActionListener() 
                            {
                                public void actionPerformed(ActionEvent e) 
                                {
                                    refreshComponents();
                                }
                            }); 
        refreshTimer.setInitialDelay(300);
        refreshTimer.start();
        setUpFocusTraversalPolicy();
        setVisible(true);
    }
    
    public PMatrixWidget(PMatrix targetMatrix)
    {
        m_theMatrix = targetMatrix;
        initComponents();
        refreshComponents(); // Prime the pump so to speak
        // get our timer started
        refreshTimer = new  Timer(50, new ActionListener() 
                            {
                                public void actionPerformed(ActionEvent e) 
                                {
                                    refreshComponents();
                                }
                            }); 
        refreshTimer.setInitialDelay(300);
        refreshTimer.start();
        setUpFocusTraversalPolicy();
        setVisible(true);
    }
    public PMatrixWidget(PMatrix targetMatrix, String matrixName)
    {
        m_theMatrix = targetMatrix;
        initComponents();
        refreshComponents(); // Prime the pump so to speak
        // get our timer started
        refreshTimer = new  Timer(50, new ActionListener() 
                            {
                                public void actionPerformed(ActionEvent e) 
                                {
                                    refreshComponents();
                                }
                            }); 
        refreshTimer.setInitialDelay(300);
        refreshTimer.start();
        setUpFocusTraversalPolicy();
        
        Label_MatrixName.setText(matrixName);
        
        setVisible(true);
    }
    
    private void setUpFocusTraversalPolicy()
    {
        FocusTraversalPolicy myPolicy = new FocusTraversalPolicy() 
        {
            private Vector focusOrder = new Vector();
            {
                focusOrder.add(XRotationSlider);
                focusOrder.add(YRotationSlider);
                focusOrder.add(ZRotationSlider);
                focusOrder.add(TranslationXTextField);
                focusOrder.add(TranslationYTextField);
                focusOrder.add(TranslationZTextField);
                focusOrder.add(ScalingXComponent);
                focusOrder.add(ScalingYComponent);
                focusOrder.add(ScalingZComponent);
            }
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) 
            {
                int index = focusOrder.indexOf(aComponent) + 1;
                if (index >= focusOrder.size())
                    index = 0;
                return (Component)focusOrder.get(index);
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) 
            {
                int index = focusOrder.indexOf(aComponent) - 1;
                if (index < 0)
                    return (Component)focusOrder.lastElement();
                return (Component)focusOrder.get(index);
            }

            @Override
            public Component getFirstComponent(Container aContainer) 
            {
                return (Component)focusOrder.firstElement();
            }

            @Override
            public Component getLastComponent(Container aContainer) 
            {
                return (Component)focusOrder.lastElement();
            }

            @Override
            public Component getDefaultComponent(Container aContainer) 
            {
                return getFirstComponent(aContainer);
            }
        };
        
        this.setFocusTraversalPolicy(myPolicy);
    }
    
    // Accessors
    public PMatrix getTargetMatrix()
    {
        return m_theMatrix;
    }
    
    // Modifiers
    public void setTargetMatrix(PMatrix target)
    {
        m_theMatrix = target;
    }

    /**
     * Inefficient but simple implementation to keep the view up to date
     * with value changes. Call after making a change to the PMatrix.
     */
    private synchronized void refreshComponents()
    {
        
        if (bRefreshing == true)
            return;
        else
            bRefreshing = true;
        // Only refresh if the component does not have focus!
        // Set the values of translation boxes
        if (!TranslationXTextField.hasFocus())
            TranslationXTextField.setValue(Double.valueOf(m_theMatrix.getTranslation().x));
        if (!TranslationYTextField.hasFocus())
            TranslationYTextField.setValue(Double.valueOf(m_theMatrix.getTranslation().y));
        if (!TranslationZTextField.hasFocus())
            TranslationZTextField.setValue(Double.valueOf(m_theMatrix.getTranslation().z));

        // set the values of the rotation sliders
        if (!XRotationSlider.hasFocus())
            XRotationSlider.setValue((int)(m_theMatrix.getRotation().toAngles(null)[0] * (180.0 / Math.PI)));
        if (!YRotationSlider.hasFocus())
            YRotationSlider.setValue((int)(m_theMatrix.getRotation().toAngles(null)[1] * (180.0 / Math.PI)));
        if (!ZRotationSlider.hasFocus())
            ZRotationSlider.setValue((int)(m_theMatrix.getRotation().toAngles(null)[2] * (180.0 / Math.PI)));
        
        // set the values of the scale boxes
        if (!ScalingXComponent.hasFocus())
            ScalingXComponent.setValue(Double.valueOf(m_theMatrix.getScaleVector().x));
        if (!ScalingYComponent.hasFocus())
            ScalingYComponent.setValue(Double.valueOf(m_theMatrix.getScaleVector().y));
        if (!ScalingZComponent.hasFocus())
            ScalingZComponent.setValue(Double.valueOf(m_theMatrix.getScaleVector().z));
        
        // set the advanced view pane stuffs
        ((PMatrixTableModel)MatrixTable.getModel()).refreshComponents(); 
        
        bRefreshing = false;
    }
    
    /**
     * Thanks Paul!
     * Rotates the model based on the slider (axis) that is used
     * @param axis
     */
    public synchronized void RotateOnAxis(int axis) {
        // TODO: Make this mathematically correct
        if (bRefreshing)
            return;
        PMatrix rotMatrix = new PMatrix();
        int degree = 0;
        float radians = 0;
        switch(axis)
        {
            case 0: // XAxis
            {
                degree = ((Integer)XRotationSlider.getValue()).intValue();
                radians = (float)java.lang.Math.toRadians((double)degree);
                xRotation.buildRotationX(radians);
                break;
            }
            case 1: // YAxis
            {
                degree = ((Integer)YRotationSlider.getValue()).intValue();
                radians = (float)java.lang.Math.toRadians((double)degree);
                yRotation.buildRotationY(radians);
                break;
            }
            case 2: // ZAxis
            {
                degree = ((Integer)ZRotationSlider.getValue()).intValue();
                radians = (float)java.lang.Math.toRadians((double)degree);
                zRotation.buildRotationZ(radians);
                break;
            }
        }
        rotMatrix.mul(yRotation);
        rotMatrix.mul(xRotation);
        rotMatrix.mul(zRotation);
        m_theMatrix.setRotation(rotMatrix.getRotation());
        //refreshComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ViewContainerTabbedPane = new javax.swing.JTabbedPane();
        StandardViewPane = new javax.swing.JPanel();
        jPanel_Rotations = new javax.swing.JPanel();
        jToolBar_XRotation = new javax.swing.JToolBar();
        jLabel_XAxis = new javax.swing.JLabel();
        XRotationSlider = new JSlider(-180, 180, 0);
        jToolBar_YRotation = new javax.swing.JToolBar();
        jLabel_YAxis = new javax.swing.JLabel();
        YRotationSlider = new JSlider(-180, 180, 0);
        jToolBar_ZRotation = new javax.swing.JToolBar();
        jLabel_ZAxis = new javax.swing.JLabel();
        ZRotationSlider = new JSlider(-180, 180, 0);
        Label_MatrixName = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel_TransX = new javax.swing.JLabel();
        TranslationXTextField = new javax.swing.JFormattedTextField();
        jLabel_ScaleX = new javax.swing.JLabel();
        ScalingXComponent = new javax.swing.JFormattedTextField();
        jToolBar2 = new javax.swing.JToolBar();
        jLabel_TransY = new javax.swing.JLabel();
        TranslationYTextField = new javax.swing.JFormattedTextField();
        jLabel_ScaleY = new javax.swing.JLabel();
        ScalingYComponent = new javax.swing.JFormattedTextField();
        jToolBar3 = new javax.swing.JToolBar();
        jLabel_TransZ = new javax.swing.JLabel();
        TranslationZTextField = new javax.swing.JFormattedTextField();
        jLabel_ScaleZ = new javax.swing.JLabel();
        ScalingZComponent = new javax.swing.JFormattedTextField();
        AdvancedViewPane = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        MatrixTable = new javax.swing.JTable();
        JButton_Identity = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(245, 235));
        setMinimumSize(new java.awt.Dimension(245, 235));

        ViewContainerTabbedPane.setToolTipText("Matrix Controls");
        ViewContainerTabbedPane.setMinimumSize(new java.awt.Dimension(245, 345));
        ViewContainerTabbedPane.setName("TabbedPane"); // NOI18N
        ViewContainerTabbedPane.setPreferredSize(new java.awt.Dimension(245, 345));

        StandardViewPane.setToolTipText("Standard Controls");
        StandardViewPane.setMaximumSize(new java.awt.Dimension(240, 600));
        StandardViewPane.setName("StandardViewPane"); // NOI18N
        StandardViewPane.setPreferredSize(new java.awt.Dimension(240, 600));

        jPanel_Rotations.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Rotations", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jToolBar_XRotation.setFloatable(false);
        jToolBar_XRotation.setRollover(true);

        jLabel_XAxis.setText("X");
        jToolBar_XRotation.add(jLabel_XAxis);

        XRotationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                XRotationSliderStateChanged(evt);
            }
        });
        jToolBar_XRotation.add(XRotationSlider);

        jToolBar_YRotation.setFloatable(false);
        jToolBar_YRotation.setRollover(true);

        jLabel_YAxis.setText("Y");
        jToolBar_YRotation.add(jLabel_YAxis);

        YRotationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                YRotationSliderStateChanged(evt);
            }
        });
        jToolBar_YRotation.add(YRotationSlider);

        jToolBar_ZRotation.setFloatable(false);
        jToolBar_ZRotation.setRollover(true);

        jLabel_ZAxis.setText("Z");
        jToolBar_ZRotation.add(jLabel_ZAxis);

        ZRotationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ZRotationSliderStateChanged(evt);
            }
        });
        jToolBar_ZRotation.add(ZRotationSlider);

        org.jdesktop.layout.GroupLayout jPanel_RotationsLayout = new org.jdesktop.layout.GroupLayout(jPanel_Rotations);
        jPanel_Rotations.setLayout(jPanel_RotationsLayout);
        jPanel_RotationsLayout.setHorizontalGroup(
            jPanel_RotationsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_RotationsLayout.createSequentialGroup()
                .add(jPanel_RotationsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar_XRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_YRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_ZRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_RotationsLayout.setVerticalGroup(
            jPanel_RotationsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_RotationsLayout.createSequentialGroup()
                .add(jToolBar_XRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_YRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jToolBar_ZRotation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        Label_MatrixName.setFont(new java.awt.Font("Courier New", 1, 14));
        Label_MatrixName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Label_MatrixName.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Translation--Scaling", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jLabel_TransX.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_TransX.setText("X");
        jToolBar1.add(jLabel_TransX);

        TranslationXTextField.setText("0.00");
        TranslationXTextField.setInputVerifier(new FloatingPointInputVerifier());
        TranslationXTextField.setMaximumSize(new java.awt.Dimension(73, 42));
        TranslationXTextField.setName("XTranslationValue"); // NOI18N
        TranslationXTextField.setPreferredSize(new java.awt.Dimension(73, 28));
        TranslationXTextField.setValue(Float.valueOf(m_theMatrix.getTranslation().getX()));
        TranslationXTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                TranslationXTextFieldFocusLost(evt);
            }
        });
        TranslationXTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TranslationXTextFieldKeyPressed(evt);
            }
        });
        jToolBar1.add(TranslationXTextField);

        jLabel_ScaleX.setText("X");
        jToolBar1.add(jLabel_ScaleX);

        ScalingXComponent.setText("0.00");
        ScalingXComponent.setInputVerifier(new FloatingPointInputVerifier());
        ScalingXComponent.setMaximumSize(new java.awt.Dimension(73, 42));
        ScalingXComponent.setName("XTranslationValue"); // NOI18N
        ScalingXComponent.setPreferredSize(new java.awt.Dimension(73, 28));
        ScalingXComponent.setValue(Float.valueOf(m_theMatrix.getTranslation().getX()));
        ScalingXComponent.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ScalingXComponentFocusLost(evt);
            }
        });
        ScalingXComponent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ScalingXComponentKeyPressed(evt);
            }
        });
        jToolBar1.add(ScalingXComponent);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jLabel_TransY.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_TransY.setText("Y");
        jToolBar2.add(jLabel_TransY);

        TranslationYTextField.setText("0.00");
        TranslationYTextField.setMaximumSize(new java.awt.Dimension(73, 42));
        TranslationYTextField.setName("YTranslationValue"); // NOI18N
        TranslationYTextField.setPreferredSize(new java.awt.Dimension(73, 28));
        TranslationYTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                TranslationYTextFieldFocusLost(evt);
            }
        });
        TranslationYTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TranslationYTextFieldKeyPressed(evt);
            }
        });
        jToolBar2.add(TranslationYTextField);

        jLabel_ScaleY.setText("Y");
        jToolBar2.add(jLabel_ScaleY);

        ScalingYComponent.setText("0.00");
        ScalingYComponent.setMaximumSize(new java.awt.Dimension(73, 42));
        ScalingYComponent.setName("XTranslationValue"); // NOI18N
        ScalingYComponent.setPreferredSize(new java.awt.Dimension(73, 28));
        ScalingYComponent.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ScalingYComponentFocusLost(evt);
            }
        });
        ScalingYComponent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ScalingYComponentKeyPressed(evt);
            }
        });
        jToolBar2.add(ScalingYComponent);

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        jLabel_TransZ.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_TransZ.setText("Z");
        jToolBar3.add(jLabel_TransZ);

        TranslationZTextField.setText("0.00");
        TranslationZTextField.setMaximumSize(new java.awt.Dimension(73, 42));
        TranslationZTextField.setName("ZTranslationValue"); // NOI18N
        TranslationZTextField.setPreferredSize(new java.awt.Dimension(73, 28));
        TranslationZTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                TranslationZTextFieldFocusLost(evt);
            }
        });
        TranslationZTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TranslationZTextFieldKeyPressed(evt);
            }
        });
        jToolBar3.add(TranslationZTextField);

        jLabel_ScaleZ.setText("Z");
        jToolBar3.add(jLabel_ScaleZ);

        ScalingZComponent.setText("0.00");
        ScalingZComponent.setMaximumSize(new java.awt.Dimension(73, 42));
        ScalingZComponent.setName("XTranslationValue"); // NOI18N
        ScalingZComponent.setPreferredSize(new java.awt.Dimension(73, 28));
        ScalingZComponent.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ScalingZComponentFocusLost(evt);
            }
        });
        ScalingZComponent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ScalingZComponentKeyPressed(evt);
            }
        });
        jToolBar3.add(ScalingZComponent);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout StandardViewPaneLayout = new org.jdesktop.layout.GroupLayout(StandardViewPane);
        StandardViewPane.setLayout(StandardViewPaneLayout);
        StandardViewPaneLayout.setHorizontalGroup(
            StandardViewPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(StandardViewPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(StandardViewPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 185, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(StandardViewPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(jPanel_Rotations, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 185, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(Label_MatrixName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        StandardViewPaneLayout.setVerticalGroup(
            StandardViewPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(StandardViewPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel_Rotations, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(Label_MatrixName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ViewContainerTabbedPane.addTab("Standard", null, StandardViewPane, "Easy to use matrix tweaking controls.");
        StandardViewPane.getAccessibleContext().setAccessibleName("StandardViewPane");

        AdvancedViewPane.setToolTipText("Advanced View");
        AdvancedViewPane.setName("AdvancedViewPane"); // NOI18N

        MatrixTable.setModel(new PMatrixTableModel(m_theMatrix));
        MatrixTable.setCellSelectionEnabled(true);
        MatrixTable.setRowHeight(32);
        MatrixTable.setTableHeader(null);
        jScrollPane1.setViewportView(MatrixTable);

        JButton_Identity.setText("Make Identity");
        JButton_Identity.setActionCommand("MakeIdentity");
        JButton_Identity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton_IdentityActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout AdvancedViewPaneLayout = new org.jdesktop.layout.GroupLayout(AdvancedViewPane);
        AdvancedViewPane.setLayout(AdvancedViewPaneLayout);
        AdvancedViewPaneLayout.setHorizontalGroup(
            AdvancedViewPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(AdvancedViewPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(AdvancedViewPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(AdvancedViewPaneLayout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, AdvancedViewPaneLayout.createSequentialGroup()
                        .add(JButton_Identity)
                        .add(48, 48, 48))))
        );
        AdvancedViewPaneLayout.setVerticalGroup(
            AdvancedViewPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(AdvancedViewPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(JButton_Identity)
                .addContainerGap(105, Short.MAX_VALUE))
        );

        ViewContainerTabbedPane.addTab("Advanced", AdvancedViewPane);
        AdvancedViewPane.getAccessibleContext().setAccessibleName("AdvancedViewPane");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ViewContainerTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ViewContainerTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        ViewContainerTabbedPane.getAccessibleContext().setAccessibleName("TabbedPane");
    }// </editor-fold>//GEN-END:initComponents

private void TranslationXTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_TranslationXTextFieldFocusLost
    if (bRefreshing)
        return;
    try 
    {
        // Commit this stuff
        TranslationXTextField.commitEdit();
    } catch (ParseException ex) 
    {
        // Bad text, just ignore it
        Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
        return;
    }
    // validate the entry
    float Xtrans = ((Number)TranslationXTextField.getValue()).floatValue();
    Vector3f translationVector = m_theMatrix.getTranslation();
    translationVector.x = (float)Xtrans;
    // submit this to the matrix
    m_theMatrix.setTranslation(translationVector);
    // update components
    refreshComponents();
}//GEN-LAST:event_TranslationXTextFieldFocusLost

private void TranslationYTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_TranslationYTextFieldFocusLost
                                                  
    if (bRefreshing)
        return;
    try 
    {
        // Commit this stuff
        TranslationYTextField.commitEdit();
    } catch (ParseException ex) 
    {
        // Bad text, just ignore it
        Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
        return;
    }
    // validate the entry
    float Ytrans = ((Number)TranslationYTextField.getValue()).floatValue();
    Vector3f translationVector = m_theMatrix.getTranslation();
    translationVector.y = Ytrans;
    // submit this to the matrix
    m_theMatrix.setTranslation(translationVector);
    // update components
    refreshComponents();
}//GEN-LAST:event_TranslationYTextFieldFocusLost

private void TranslationZTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_TranslationZTextFieldFocusLost
                                                  
    if (bRefreshing)
        return;
    try 
    {
        // Commit this stuff
        TranslationZTextField.commitEdit();
    } catch (ParseException ex) 
    {
        // Bad text, just ignore it
        Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
        return;
    }
    // validate the entry
    float Ztrans = ((Number)TranslationZTextField.getValue()).floatValue();
    Vector3f translationVector = m_theMatrix.getTranslation();
    translationVector.z = Ztrans;
    // submit this to the matrix
    m_theMatrix.setTranslation(translationVector);
    // update components
    refreshComponents();
}//GEN-LAST:event_TranslationZTextFieldFocusLost

private void TranslationXTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TranslationXTextFieldKeyPressed
                                                  
    if (bRefreshing)
        return;
    // if it was the enter key, then submit this stuff!
    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
    {
            try 
            {
                // Commit this stuff
                TranslationXTextField.commitEdit();
            } catch (ParseException ex) 
            {
                // Bad text, just ignore it
                Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        // validate the entry
        float Xtrans = ((Number)TranslationXTextField.getValue()).floatValue();
        Vector3f translationVector = m_theMatrix.getTranslation();
        translationVector.x = Xtrans;
        // submit this to the matrix
        m_theMatrix.setTranslation(translationVector);
        // update components
        refreshComponents();
        
    }
}//GEN-LAST:event_TranslationXTextFieldKeyPressed

private void TranslationYTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TranslationYTextFieldKeyPressed
                                               
    if (bRefreshing)
        return;
    // if it was the enter key, then submit this stuff!
    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
    {
            try 
            {
                // Commit this stuff
                TranslationYTextField.commitEdit();
            } catch (ParseException ex) 
            {
                // Bad text, just ignore it
                Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        // validate the entry
        float Ytrans = ((Number)TranslationYTextField.getValue()).floatValue();
        Vector3f translationVector = m_theMatrix.getTranslation();
        translationVector.y = Ytrans;
        // submit this to the matrix
        m_theMatrix.setTranslation(translationVector);
        // update components
        refreshComponents();
        
    }
}//GEN-LAST:event_TranslationYTextFieldKeyPressed

private void TranslationZTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TranslationZTextFieldKeyPressed
                                               
    if (bRefreshing)
        return;
    // if it was the enter key, then submit this stuff!
    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
    {
            try 
            {
                // Commit this stuff
                TranslationZTextField.commitEdit();
            } catch (ParseException ex) 
            {
                // Bad text, just ignore it
                Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        // validate the entry
        float Ztrans = ((Number)TranslationZTextField.getValue()).floatValue();
        Vector3f translationVector = m_theMatrix.getTranslation();
        translationVector.z = Ztrans;
        // submit this to the matrix
        m_theMatrix.setTranslation(translationVector);
        // update components
        refreshComponents();
        
    }
}//GEN-LAST:event_TranslationZTextFieldKeyPressed

private void ScalingZComponentFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ScalingZComponentFocusLost
                                                  
    if (bRefreshing)
        return;
    try 
    {
        // Commit this stuff
        ScalingZComponent.commitEdit();
    } catch (ParseException ex) 
    {
        // Bad text, just ignore it
        Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
        return;
    }
    // validate the entry
    float Zscale = ((Number)ScalingZComponent.getValue()).floatValue();
    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
    scaleVector.z = Zscale;
    // submit this to the matrix
    m_theMatrix.setScale(scaleVector);
    // update components
    refreshComponents();
}//GEN-LAST:event_ScalingZComponentFocusLost

private void ScalingZComponentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ScalingZComponentKeyPressed
                                                  
    if (bRefreshing)
        return;
    // if it was the enter key, then submit this stuff!
    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
    {
            try 
            {
                // Commit this stuff
                ScalingZComponent.commitEdit();
            } catch (ParseException ex) 
            {
                // Bad text, just ignore it
                Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        // validate the entry
        float Zscale = ((Number)ScalingZComponent.getValue()).floatValue();
        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
        scaleVector.z = Zscale;
        // submit this to the matrix
        m_theMatrix.setScale(scaleVector);
        // update components
        refreshComponents();
        
    }
}//GEN-LAST:event_ScalingZComponentKeyPressed

private void ScalingYComponentFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ScalingYComponentFocusLost
                                                  
    if (bRefreshing)
        return;
    try 
    {
        // Commit this stuff
        ScalingYComponent.commitEdit();
    } catch (ParseException ex) 
    {
        // Bad text, just ignore it
        Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
        return;
    }
    // validate the entry
    float Yscale = ((Number)ScalingYComponent.getValue()).floatValue();
    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
    scaleVector.y = Yscale;
    // submit this to the matrix
    m_theMatrix.setScale(scaleVector);
    // update components
    refreshComponents();
}//GEN-LAST:event_ScalingYComponentFocusLost

private void ScalingYComponentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ScalingYComponentKeyPressed
                                              
    if (bRefreshing)
        return;
    
    // if it was the enter key, then submit this stuff!
    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
    {
            try 
            {
                // Commit this stuff
                ScalingYComponent.commitEdit();
            } catch (ParseException ex) 
            {
                // Bad text, just ignore it
                Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        // validate the entry
        float Yscale = ((Number)ScalingYComponent.getValue()).floatValue();
        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
        scaleVector.y = Yscale;
        // submit this to the matrix
        m_theMatrix.setScale(scaleVector);
        // update components
        refreshComponents();
        
    }
}//GEN-LAST:event_ScalingYComponentKeyPressed

private void ScalingXComponentFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ScalingXComponentFocusLost
                                                  
    if (bRefreshing)
        return;
    try 
    {
        // Commit this stuff
        ScalingXComponent.commitEdit();
    } catch (ParseException ex) 
    {
        // Bad text, just ignore it
        Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
        return;
    }
    // validate the entry
    float Xscale = ((Number)ScalingXComponent.getValue()).floatValue();
    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
    scaleVector.x = Xscale;
    // submit this to the matrix
    m_theMatrix.setScale(scaleVector);
    // update components
    refreshComponents();
}//GEN-LAST:event_ScalingXComponentFocusLost

private void ScalingXComponentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ScalingXComponentKeyPressed
                                              
    if (bRefreshing)
        return;
    
    // if it was the enter key, then submit this stuff!
    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
    {
            try 
            {
                // Commit this stuff
                ScalingXComponent.commitEdit();
            } catch (ParseException ex) 
            {
                // Bad text, just ignore it
                Logger.getLogger(PMatrixWidget.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        // validate the entry
        float Xscale = ((Number)ScalingXComponent.getValue()).floatValue();
        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
        scaleVector.x = Xscale;
        // submit this to the matrix
        m_theMatrix.setScale(scaleVector);
        // update components
        refreshComponents();
        
    }
}//GEN-LAST:event_ScalingXComponentKeyPressed

private void XRotationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_XRotationSliderStateChanged
if (bRefreshing)
        return;
    //    refreshComponents();
        RotateOnAxis(0);
}//GEN-LAST:event_XRotationSliderStateChanged

private void YRotationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_YRotationSliderStateChanged

                                              
    if (bRefreshing)
        return;
        RotateOnAxis(1);
}//GEN-LAST:event_YRotationSliderStateChanged

private void ZRotationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ZRotationSliderStateChanged
                                                
    if (bRefreshing)
        return;
      RotateOnAxis(2);
}//GEN-LAST:event_ZRotationSliderStateChanged

private void JButton_IdentityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JButton_IdentityActionPerformed
                                                
    if (bRefreshing)
        return;
      if (evt.getActionCommand().equals("MakeIdentity"))
    {
        m_theMatrix.setIdentity();
        refreshComponents();
    }
}//GEN-LAST:event_JButton_IdentityActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AdvancedViewPane;
    private javax.swing.JButton JButton_Identity;
    private javax.swing.JLabel Label_MatrixName;
    private javax.swing.JTable MatrixTable;
    private javax.swing.JFormattedTextField ScalingXComponent;
    private javax.swing.JFormattedTextField ScalingYComponent;
    private javax.swing.JFormattedTextField ScalingZComponent;
    private javax.swing.JPanel StandardViewPane;
    private javax.swing.JFormattedTextField TranslationXTextField;
    private javax.swing.JFormattedTextField TranslationYTextField;
    private javax.swing.JFormattedTextField TranslationZTextField;
    private javax.swing.JTabbedPane ViewContainerTabbedPane;
    private javax.swing.JSlider XRotationSlider;
    private javax.swing.JSlider YRotationSlider;
    private javax.swing.JSlider ZRotationSlider;
    private javax.swing.JLabel jLabel_ScaleX;
    private javax.swing.JLabel jLabel_ScaleY;
    private javax.swing.JLabel jLabel_ScaleZ;
    private javax.swing.JLabel jLabel_TransX;
    private javax.swing.JLabel jLabel_TransY;
    private javax.swing.JLabel jLabel_TransZ;
    private javax.swing.JLabel jLabel_XAxis;
    private javax.swing.JLabel jLabel_YAxis;
    private javax.swing.JLabel jLabel_ZAxis;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_Rotations;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar_XRotation;
    private javax.swing.JToolBar jToolBar_YRotation;
    private javax.swing.JToolBar jToolBar_ZRotation;
    // End of variables declaration//GEN-END:variables

}
