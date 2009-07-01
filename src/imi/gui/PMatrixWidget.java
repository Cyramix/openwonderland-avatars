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
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    private PMatrix         m_theMatrix         = null;     // This is the matrix we are operating on
    
    // Rotation Data caching matrices.
    private PMatrix         xRotation           = new PMatrix();
    private PMatrix         yRotation           = new PMatrix();
    private PMatrix         zRotation           = new PMatrix();
    
    // Refresh timer, periodically causes a document refresh
    private Timer           refreshTimer        = null;
    private boolean         bRefreshing         = false;    // USed to stop incremental refreshing problem
    private NumberFormat    m_format            = new DecimalFormat("#,###.######");
    private String          m_formattedNumber   = null;

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
        if (!TranslationXTextField.hasFocus()) {
            m_formattedNumber = m_format.format(m_theMatrix.getTranslation().x);
            TranslationXTextField.setValue(Double.valueOf(Double.valueOf(m_formattedNumber)));
        }
        if (!TranslationYTextField.hasFocus()) {
            m_formattedNumber = m_format.format(m_theMatrix.getTranslation().y);
            TranslationYTextField.setValue(Double.valueOf(Double.valueOf(m_formattedNumber)));
        }
        if (!TranslationZTextField.hasFocus()) {
            m_formattedNumber = m_format.format(m_theMatrix.getTranslation().z);
            TranslationZTextField.setValue(Double.valueOf(Double.valueOf(m_formattedNumber)));
        }

        // set the values of the rotation sliders
        if (!XRotationSlider.hasFocus())
            XRotationSlider.setValue((int)(m_theMatrix.getRotation().toAngles(null)[0] * (180.0 / Math.PI)));
        if (!YRotationSlider.hasFocus())
            YRotationSlider.setValue((int)(m_theMatrix.getRotation().toAngles(null)[1] * (180.0 / Math.PI)));
        if (!ZRotationSlider.hasFocus())
            ZRotationSlider.setValue((int)(m_theMatrix.getRotation().toAngles(null)[2] * (180.0 / Math.PI)));
        
        // set the values of the scale boxes
        if (!ScalingXComponent.hasFocus()) {
            m_formattedNumber = m_format.format(m_theMatrix.getScaleVector().x);
            ScalingXComponent.setValue(Double.valueOf(Double.valueOf(m_formattedNumber)));
        }
        if (!ScalingYComponent.hasFocus()) {
            m_formattedNumber = m_format.format(m_theMatrix.getScaleVector().y);
            ScalingYComponent.setValue(Double.valueOf(Double.valueOf(m_formattedNumber)));
        }
        if (!ScalingZComponent.hasFocus()) {
            m_formattedNumber = m_format.format(m_theMatrix.getScaleVector().z);
            ScalingZComponent.setValue(Double.valueOf(Double.valueOf(m_formattedNumber)));
        }
        
        // set the advanced view pane stuffs
        ((PMatrixTableModel)MatrixTable.getModel()).refreshComponents(); 
        
        bRefreshing = false;
    }
    
    /**
     * Thanks Paul!
     * Rotates the model based on the slider (axis) that is used
     * @param axis
     */
    public synchronized void rotateOnAxis(int axis) {
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
        rotMatrix.fastMul(yRotation);
        rotMatrix.fastMul(xRotation);
        rotMatrix.fastMul(zRotation);
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
        java.awt.GridBagConstraints gridBagConstraints;

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
        jLabel_TransX = new javax.swing.JLabel();
        TranslationXTextField = new javax.swing.JFormattedTextField();
        jLabel_ScaleX = new javax.swing.JLabel();
        ScalingXComponent = new javax.swing.JFormattedTextField();
        jLabel_TransY = new javax.swing.JLabel();
        TranslationYTextField = new javax.swing.JFormattedTextField();
        jLabel_ScaleY = new javax.swing.JLabel();
        ScalingYComponent = new javax.swing.JFormattedTextField();
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
        StandardViewPane.setLayout(new java.awt.GridBagLayout());

        jPanel_Rotations.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Rotations", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanel_Rotations.setMinimumSize(new java.awt.Dimension(207, 200));
        jPanel_Rotations.setPreferredSize(new java.awt.Dimension(207, 130));
        jPanel_Rotations.setLayout(new java.awt.GridBagLayout());

        jToolBar_XRotation.setFloatable(false);
        jToolBar_XRotation.setRollover(true);
        jToolBar_XRotation.setMinimumSize(new java.awt.Dimension(190, 33));
        jToolBar_XRotation.setPreferredSize(new java.awt.Dimension(190, 33));

        jLabel_XAxis.setText("X");
        jToolBar_XRotation.add(jLabel_XAxis);

        XRotationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                XRotationSliderStateChanged(evt);
            }
        });
        jToolBar_XRotation.add(XRotationSlider);

        jPanel_Rotations.add(jToolBar_XRotation, new java.awt.GridBagConstraints());

        jToolBar_YRotation.setFloatable(false);
        jToolBar_YRotation.setRollover(true);
        jToolBar_YRotation.setMinimumSize(new java.awt.Dimension(190, 33));
        jToolBar_YRotation.setPreferredSize(new java.awt.Dimension(190, 33));

        jLabel_YAxis.setText("Y");
        jToolBar_YRotation.add(jLabel_YAxis);

        YRotationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                YRotationSliderStateChanged(evt);
            }
        });
        jToolBar_YRotation.add(YRotationSlider);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Rotations.add(jToolBar_YRotation, gridBagConstraints);

        jToolBar_ZRotation.setFloatable(false);
        jToolBar_ZRotation.setRollover(true);
        jToolBar_ZRotation.setMinimumSize(new java.awt.Dimension(190, 33));
        jToolBar_ZRotation.setPreferredSize(new java.awt.Dimension(190, 33));

        jLabel_ZAxis.setText("Z");
        jToolBar_ZRotation.add(jLabel_ZAxis);

        ZRotationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ZRotationSliderStateChanged(evt);
            }
        });
        jToolBar_ZRotation.add(ZRotationSlider);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel_Rotations.add(jToolBar_ZRotation, gridBagConstraints);

        StandardViewPane.add(jPanel_Rotations, new java.awt.GridBagConstraints());

        Label_MatrixName.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
        Label_MatrixName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Label_MatrixName.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        Label_MatrixName.setMaximumSize(new java.awt.Dimension(207, 25));
        Label_MatrixName.setMinimumSize(new java.awt.Dimension(207, 25));
        Label_MatrixName.setPreferredSize(new java.awt.Dimension(207, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        StandardViewPane.add(Label_MatrixName, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Translation--Scaling", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanel1.setMinimumSize(new java.awt.Dimension(207, 130));
        jPanel1.setPreferredSize(new java.awt.Dimension(207, 130));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel_TransX.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_TransX.setText("X");
        jPanel1.add(jLabel_TransX, new java.awt.GridBagConstraints());

        TranslationXTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.########"))));
        TranslationXTextField.setInputVerifier(new FloatingPointInputVerifier());
        TranslationXTextField.setMaximumSize(new java.awt.Dimension(90, 25));
        TranslationXTextField.setMinimumSize(new java.awt.Dimension(90, 25));
        TranslationXTextField.setName("XTranslationValue"); // NOI18N
        TranslationXTextField.setPreferredSize(new java.awt.Dimension(90, 25));
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
        jPanel1.add(TranslationXTextField, new java.awt.GridBagConstraints());

        jLabel_ScaleX.setText("X");
        jPanel1.add(jLabel_ScaleX, new java.awt.GridBagConstraints());

        ScalingXComponent.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.########"))));
        ScalingXComponent.setInputVerifier(new FloatingPointInputVerifier());
        ScalingXComponent.setMaximumSize(new java.awt.Dimension(90, 25));
        ScalingXComponent.setMinimumSize(new java.awt.Dimension(90, 25));
        ScalingXComponent.setName("XTranslationValue"); // NOI18N
        ScalingXComponent.setPreferredSize(new java.awt.Dimension(90, 25));
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
        jPanel1.add(ScalingXComponent, new java.awt.GridBagConstraints());

        jLabel_TransY.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_TransY.setText("Y");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel_TransY, gridBagConstraints);

        TranslationYTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.########"))));
        TranslationYTextField.setMaximumSize(new java.awt.Dimension(90, 25));
        TranslationYTextField.setMinimumSize(new java.awt.Dimension(90, 25));
        TranslationYTextField.setName("YTranslationValue"); // NOI18N
        TranslationYTextField.setPreferredSize(new java.awt.Dimension(90, 25));
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(TranslationYTextField, gridBagConstraints);

        jLabel_ScaleY.setText("Y");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel_ScaleY, gridBagConstraints);

        ScalingYComponent.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.########"))));
        ScalingYComponent.setMaximumSize(new java.awt.Dimension(90, 25));
        ScalingYComponent.setMinimumSize(new java.awt.Dimension(90, 25));
        ScalingYComponent.setName("XTranslationValue"); // NOI18N
        ScalingYComponent.setPreferredSize(new java.awt.Dimension(90, 25));
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel1.add(ScalingYComponent, gridBagConstraints);

        jLabel_TransZ.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_TransZ.setText("Z");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jLabel_TransZ, gridBagConstraints);

        TranslationZTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.########"))));
        TranslationZTextField.setMaximumSize(new java.awt.Dimension(90, 25));
        TranslationZTextField.setMinimumSize(new java.awt.Dimension(90, 25));
        TranslationZTextField.setName("ZTranslationValue"); // NOI18N
        TranslationZTextField.setPreferredSize(new java.awt.Dimension(90, 25));
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel1.add(TranslationZTextField, gridBagConstraints);

        jLabel_ScaleZ.setText("Z");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jLabel_ScaleZ, gridBagConstraints);

        ScalingZComponent.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.########"))));
        ScalingZComponent.setMaximumSize(new java.awt.Dimension(90, 25));
        ScalingZComponent.setMinimumSize(new java.awt.Dimension(90, 25));
        ScalingZComponent.setName("XTranslationValue"); // NOI18N
        ScalingZComponent.setPreferredSize(new java.awt.Dimension(90, 25));
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        jPanel1.add(ScalingZComponent, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        StandardViewPane.add(jPanel1, gridBagConstraints);

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
//    float Xtrans = ((Number)TranslationXTextField.getValue()).floatValue();
//    Vector3f translationVector = m_theMatrix.getTranslation();
//    translationVector.x = (float)Xtrans;
    m_formattedNumber = m_format.format(((Number)TranslationXTextField.getValue()).doubleValue());
    Vector3f translationVector = m_theMatrix.getTranslation();
    translationVector.x = Float.valueOf(m_formattedNumber);

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
//    float Ytrans = ((Number)TranslationYTextField.getValue()).floatValue();
//    Vector3f translationVector = m_theMatrix.getTranslation();
//    translationVector.y = Ytrans;
    m_formattedNumber = m_format.format(((Number)TranslationYTextField.getValue()).doubleValue());
    Vector3f translationVector = m_theMatrix.getTranslation();
    translationVector.y = Float.valueOf(m_formattedNumber);

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
//    float Ztrans = ((Number)TranslationZTextField.getValue()).floatValue();
//    Vector3f translationVector = m_theMatrix.getTranslation();
//    translationVector.z = Ztrans;
    m_formattedNumber = m_format.format(((Number)TranslationZTextField.getValue()).doubleValue());
    Vector3f translationVector = m_theMatrix.getTranslation();
    translationVector.z = Float.valueOf(m_formattedNumber);

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
//        float Xtrans = ((Number)TranslationXTextField.getValue()).floatValue();
//        Vector3f translationVector = m_theMatrix.getTranslation();
//        translationVector.x = Xtrans;
        m_formattedNumber = m_format.format(((Number)TranslationXTextField.getValue()).doubleValue());
        Vector3f translationVector = m_theMatrix.getTranslation();
        translationVector.x = Float.valueOf(m_formattedNumber);

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
//        float Ytrans = ((Number)TranslationYTextField.getValue()).floatValue();
//        Vector3f translationVector = m_theMatrix.getTranslation();
//        translationVector.y = Ytrans;
        m_formattedNumber = m_format.format(((Number)TranslationYTextField.getValue()).doubleValue());
        Vector3f translationVector = m_theMatrix.getTranslation();
        translationVector.y = Float.valueOf(m_formattedNumber);

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
//        float Ztrans = ((Number)TranslationZTextField.getValue()).floatValue();
//        Vector3f translationVector = m_theMatrix.getTranslation();
//        translationVector.z = Ztrans;
        m_formattedNumber = m_format.format(((Number)TranslationZTextField.getValue()).doubleValue());
        Vector3f translationVector = m_theMatrix.getTranslation();
        translationVector.z = Float.valueOf(m_formattedNumber);

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
//    float Zscale = ((Number)ScalingZComponent.getValue()).floatValue();
//    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
//    scaleVector.z = Zscale;
    m_formattedNumber = m_format.format(((Number)ScalingZComponent.getValue()).doubleValue());
    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
    scaleVector.z = Float.valueOf(m_formattedNumber);

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
//        float Zscale = ((Number)ScalingZComponent.getValue()).floatValue();
//        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
//        scaleVector.z = Zscale;+
        m_formattedNumber = m_format.format(((Number)ScalingZComponent.getValue()).doubleValue());
        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
        scaleVector.z = Float.valueOf(m_formattedNumber);

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
//    float Yscale = ((Number)ScalingYComponent.getValue()).floatValue();
//    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
//    scaleVector.y = Yscale;
    m_formattedNumber = m_format.format(((Number)ScalingYComponent.getValue()).doubleValue());
    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
    scaleVector.y = Float.valueOf(m_formattedNumber);

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
//        float Yscale = ((Number)ScalingYComponent.getValue()).floatValue();
//        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
//        scaleVector.y = Yscale;
        m_formattedNumber = m_format.format(((Number)ScalingYComponent.getValue()).doubleValue());
        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
        scaleVector.y = Float.valueOf(m_formattedNumber);

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
//    float Xscale = ((Number)ScalingXComponent.getValue()).floatValue();
//    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
//    scaleVector.x = Xscale;
    m_formattedNumber = m_format.format(((Number)ScalingXComponent.getValue()).doubleValue());
    Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
    scaleVector.x = Float.valueOf(m_formattedNumber);

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
//        float Xscale = ((Number)ScalingXComponent.getValue()).floatValue();
//        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
//        scaleVector.x = Xscale;
        m_formattedNumber = m_format.format(((Number)ScalingXComponent.getValue()).doubleValue());
        Vector3f scaleVector = new Vector3f(m_theMatrix.getScaleVector());
        scaleVector.x = Float.valueOf(m_formattedNumber);

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
        rotateOnAxis(0);
}//GEN-LAST:event_XRotationSliderStateChanged

private void YRotationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_YRotationSliderStateChanged

                                              
    if (bRefreshing)
        return;
        rotateOnAxis(1);
}//GEN-LAST:event_YRotationSliderStateChanged

private void ZRotationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ZRotationSliderStateChanged
                                                
    if (bRefreshing)
        return;
      rotateOnAxis(2);
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

public void setWidgetName(String name) {
    Label_MatrixName.setText(name);
    Label_MatrixName.setVisible(true);
}
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
    private javax.swing.JToolBar jToolBar_XRotation;
    private javax.swing.JToolBar jToolBar_YRotation;
    private javax.swing.JToolBar jToolBar_ZRotation;
    // End of variables declaration//GEN-END:variables

}
