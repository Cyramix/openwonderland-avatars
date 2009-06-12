/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ControlPanel.java
 *
 * Created on Feb 23, 2009, 11:30:05 AM
 */

package imi.gui;

import imi.imaging.ImageData;
import java.io.File;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author ptruong
 */
public class ControlPanel extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    private TextureCreator          m_parent    = null;
    private int                     m_prevX     = 0;
    private int                     m_prevY     = 0;
    private String                  m_texName   = null;
    private boolean                 m_bUpdate   = true;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

    /** Creates new form ControlPanel */
    public ControlPanel() {
        initComponents();
        initJFileChooser();
    }

    public ControlPanel(TextureCreator parent) {
        m_parent = parent;
        initComponents();
        initJFileChooser();
    }

    public void updateImage() {

        Object[] data = new Object[10];

        if(!m_bUpdate)
            return;

        data[0] = m_parent.getSelectedLayerIndex();
        data[1] = jSlider_TranslationX.getValue() - m_prevX;
        data[2] = jSlider_TranslationY.getValue() - m_prevY;
        data[3] = ((double) jSlider_ScaleX.getValue()) / 100.0;
        data[4] = ((double) jSlider_ScaleY.getValue()) / 100.0;
        data[5] = Math.toRadians(((double) jSlider_Rotation.getValue()));
        data[6] = ((double) jSlider_Opacity.getValue()) / 100;
        data[7] = jCheckBox_HFlip.isSelected();
        data[8] = jCheckBox_VFlip.isSelected();
        data[9] = jSlider_Repeat.getValue();

        m_prevX     = jSlider_TranslationX.getValue();
        m_prevY     = jSlider_TranslationY.getValue();

        jLabel_TranslationValueX.setText(String.valueOf(m_prevX));
        jLabel_TranslationValueY.setText(String.valueOf(m_prevY));
        jLabel_ScaleXPercent.setText(String.valueOf(data[3]));
        jLabel_ScaleYPercent.setText(String.valueOf(data[4]));
        jLabel_RotationPercent.setText(String.valueOf(jSlider_Rotation.getValue()));
        jLabel_OpacityPercent.setText(String.valueOf(jSlider_Opacity.getValue()) + "%");
        jLabel_RepeatPercent.setText(String.valueOf(jSlider_Repeat.getValue()));

        m_parent.updateImage(data);
    }

    public void updateControls(ImageData data) {
        m_prevX     = data.m_xPos;
        m_prevY     = data.m_yPos;

        jSlider_TranslationX.setValue(data.m_xPos);
        jLabel_TranslationValueX.setText(String.valueOf(m_prevX));

        jSlider_TranslationY.setValue(data.m_yPos);
        jLabel_TranslationValueY.setText(String.valueOf(m_prevY));

        jSlider_ScaleX.setValue((int) (data.m_xScale * 100.0));
        jLabel_ScaleXPercent.setText(String.valueOf(data.m_xScale));

        jSlider_ScaleY.setValue((int) (data.m_yScale * 100.0));
        jLabel_ScaleYPercent.setText(String.valueOf(data.m_yScale));

        jSlider_Rotation.setValue((int) data.m_rotation);
        jLabel_RotationPercent.setText(String.valueOf((int)data.m_rotation));

        jSlider_Opacity.setValue((int) (data.m_opacity * 100.0));
        jLabel_OpacityPercent.setText(String.valueOf((int) (data.m_opacity * 100.0)) + "%");

        jCheckBox_HFlip.setSelected((boolean)data.m_hflip);

        jCheckBox_VFlip.setSelected((boolean)data.m_vflip);

        jSlider_Repeat.setValue(data.m_repeat);
        jLabel_RepeatPercent.setText(String.valueOf(data.m_repeat));

        TitledBorder border = (TitledBorder) jPanel_Options.getBorder();
        border.setTitle("Selected Layer (" + data.m_name + ")");
        jPanel_Options.updateUI();
    }


////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setGUIUpdate(Boolean update) {
        m_bUpdate = update;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////
    public void initJFileChooser() {

        FileFilter textFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".png") || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg") ||
                    f.getName().toLowerCase().endsWith(".gif") || f.getName().toLowerCase().endsWith(".bmp") || f.getName().toLowerCase().endsWith(".tga")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = "Image Files (*.png, *.jpg, *.jpeg, *.gif, *.bmp, *.tga)";
                return szDescription;
            }
        };
        jFileChooser_TextureSelector = new javax.swing.JFileChooser();
        jFileChooser_TextureSelector.setDialogTitle("Load Texture File");
        java.io.File loadDirectory = new java.io.File(System.getProperty("user.dir"));

        jFileChooser_TextureSelector.setCurrentDirectory(loadDirectory);
        jFileChooser_TextureSelector.setDoubleBuffered(true);
        jFileChooser_TextureSelector.setDragEnabled(true);
        jFileChooser_TextureSelector.addChoosableFileFilter((FileFilter)textFilter);
    }

    public void resetGUI() {
        m_bUpdate = false;

        m_prevX     = 0;
        m_prevY     = 0;

        jSlider_TranslationX.setValue(0);
        jLabel_TranslationValueX.setText("0");

        jSlider_TranslationY.setValue(0);
        jLabel_TranslationValueY.setText("0");

        jSlider_ScaleX.setValue(100);
        jLabel_ScaleXPercent.setText("1.0");

        jSlider_ScaleY.setValue(100);
        jLabel_ScaleYPercent.setText("1.0");

        jSlider_Rotation.setValue(0);
        jLabel_RotationPercent.setText("0");

        jSlider_Opacity.setValue(100);
        jLabel_OpacityPercent.setText("100%");

        jCheckBox_HFlip.setSelected(false);

        jCheckBox_VFlip.setSelected(false);

        jSlider_Repeat.setValue(0);
        jLabel_RepeatPercent.setText("0");

        TitledBorder border = (TitledBorder) jPanel_Options.getBorder();
        border.setTitle("Selected Layer (None)");
        jPanel_Options.updateUI();

        m_bUpdate = true;
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

        jFileChooser_TextureSelector = new javax.swing.JFileChooser();
        jPanel_Options = new javax.swing.JPanel();
        jToolBar_Opacity = new javax.swing.JToolBar();
        jLabel_Opacity = new javax.swing.JLabel();
        jSlider_Opacity = new javax.swing.JSlider();
        jLabel_OpacityPercent = new javax.swing.JLabel();
        jToolBar_Repeat = new javax.swing.JToolBar();
        jLabel_Repeat = new javax.swing.JLabel();
        jSlider_Repeat = new javax.swing.JSlider();
        jLabel_RepeatPercent = new javax.swing.JLabel();
        jToolBar_ImageFlip = new javax.swing.JToolBar();
        jLabel_ImageFlip = new javax.swing.JLabel();
        jCheckBox_HFlip = new javax.swing.JCheckBox();
        jCheckBox_VFlip = new javax.swing.JCheckBox();
        jToolBar_Rotation = new javax.swing.JToolBar();
        jLabel_Rotation = new javax.swing.JLabel();
        jSlider_Rotation = new javax.swing.JSlider();
        jLabel_RotationPercent = new javax.swing.JLabel();
        jToolBar_TranslationX = new javax.swing.JToolBar();
        jLabel_TranslationX = new javax.swing.JLabel();
        jSlider_TranslationX = new javax.swing.JSlider();
        jLabel_TranslationValueX = new javax.swing.JLabel();
        jToolBar_TranslationY = new javax.swing.JToolBar();
        jLabel_TranslationY = new javax.swing.JLabel();
        jSlider_TranslationY = new javax.swing.JSlider();
        jLabel_TranslationValueY = new javax.swing.JLabel();
        jToolBar_ScaleX = new javax.swing.JToolBar();
        jLabel_ScaleX = new javax.swing.JLabel();
        jSlider_ScaleX = new javax.swing.JSlider();
        jLabel_ScaleXPercent = new javax.swing.JLabel();
        jToolBar_ScaleY = new javax.swing.JToolBar();
        jLabel_ScaleY = new javax.swing.JLabel();
        jSlider_ScaleY = new javax.swing.JSlider();
        jLabel_ScaleYPercent = new javax.swing.JLabel();

        jFileChooser_TextureSelector.setDialogTitle("Load Texture");

        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        jPanel_Options.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Layer (None)"));
        jPanel_Options.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel_Options.setPreferredSize(new java.awt.Dimension(305, 260));
        jPanel_Options.setLayout(new java.awt.GridBagLayout());

        jToolBar_Opacity.setFloatable(false);
        jToolBar_Opacity.setRollover(true);

        jLabel_Opacity.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Opacity.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel_Opacity.setText("Opacity");
        jLabel_Opacity.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_Opacity.add(jLabel_Opacity);

        jSlider_Opacity.setMinorTickSpacing(1);
        jSlider_Opacity.setPaintTicks(true);
        jSlider_Opacity.setSnapToTicks(true);
        jSlider_Opacity.setValue(100);
        jSlider_Opacity.setPreferredSize(new java.awt.Dimension(165, 29));
        jSlider_Opacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_Opacity.add(jSlider_Opacity);

        jLabel_OpacityPercent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_OpacityPercent.setText("100%");
        jToolBar_Opacity.add(jLabel_OpacityPercent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_Opacity, gridBagConstraints);

        jToolBar_Repeat.setFloatable(false);
        jToolBar_Repeat.setRollover(true);

        jLabel_Repeat.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Repeat.setText("Tex Repeat");
        jLabel_Repeat.setMaximumSize(new java.awt.Dimension(44, 16));
        jLabel_Repeat.setMinimumSize(new java.awt.Dimension(44, 16));
        jLabel_Repeat.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_Repeat.add(jLabel_Repeat);

        jSlider_Repeat.setMaximum(1000);
        jSlider_Repeat.setMinimum(-1000);
        jSlider_Repeat.setSnapToTicks(true);
        jSlider_Repeat.setValue(0);
        jSlider_Repeat.setPreferredSize(new java.awt.Dimension(180, 29));
        jSlider_Repeat.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_Repeat.add(jSlider_Repeat);

        jLabel_RepeatPercent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_RepeatPercent.setText("0");
        jLabel_RepeatPercent.setMaximumSize(new java.awt.Dimension(33, 16));
        jLabel_RepeatPercent.setMinimumSize(new java.awt.Dimension(33, 16));
        jLabel_RepeatPercent.setPreferredSize(new java.awt.Dimension(33, 16));
        jToolBar_Repeat.add(jLabel_RepeatPercent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_Repeat, gridBagConstraints);

        jToolBar_ImageFlip.setFloatable(false);
        jToolBar_ImageFlip.setRollover(true);

        jLabel_ImageFlip.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_ImageFlip.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel_ImageFlip.setText("Image Flip");
        jLabel_ImageFlip.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_ImageFlip.add(jLabel_ImageFlip);

        jCheckBox_HFlip.setText("Horizontal");
        jCheckBox_HFlip.setFocusable(false);
        jCheckBox_HFlip.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jCheckBox_HFlip.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBox_HFlip.setMaximumSize(new java.awt.Dimension(90, 25));
        jCheckBox_HFlip.setMinimumSize(new java.awt.Dimension(100, 25));
        jCheckBox_HFlip.setPreferredSize(new java.awt.Dimension(100, 25));
        jCheckBox_HFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_ImageFlip.add(jCheckBox_HFlip);

        jCheckBox_VFlip.setText("Vertical");
        jCheckBox_VFlip.setFocusable(false);
        jCheckBox_VFlip.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jCheckBox_VFlip.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBox_VFlip.setMaximumSize(new java.awt.Dimension(90, 25));
        jCheckBox_VFlip.setMinimumSize(new java.awt.Dimension(100, 25));
        jCheckBox_VFlip.setPreferredSize(new java.awt.Dimension(100, 25));
        jCheckBox_VFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_ImageFlip.add(jCheckBox_VFlip);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_ImageFlip, gridBagConstraints);

        jToolBar_Rotation.setFloatable(false);
        jToolBar_Rotation.setRollover(true);

        jLabel_Rotation.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Rotation.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel_Rotation.setText("Rotation");
        jLabel_Rotation.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_Rotation.add(jLabel_Rotation);

        jSlider_Rotation.setMaximum(360);
        jSlider_Rotation.setMinorTickSpacing(1);
        jSlider_Rotation.setSnapToTicks(true);
        jSlider_Rotation.setValue(0);
        jSlider_Rotation.setPreferredSize(new java.awt.Dimension(180, 29));
        jSlider_Rotation.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_Rotation.add(jSlider_Rotation);

        jLabel_RotationPercent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_RotationPercent.setText("0");
        jLabel_RotationPercent.setMaximumSize(new java.awt.Dimension(33, 16));
        jLabel_RotationPercent.setMinimumSize(new java.awt.Dimension(33, 16));
        jLabel_RotationPercent.setPreferredSize(new java.awt.Dimension(33, 16));
        jToolBar_Rotation.add(jLabel_RotationPercent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_Rotation, gridBagConstraints);

        jToolBar_TranslationX.setFloatable(false);
        jToolBar_TranslationX.setRollover(true);

        jLabel_TranslationX.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_TranslationX.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel_TranslationX.setText("X Position");
        jLabel_TranslationX.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_TranslationX.add(jLabel_TranslationX);

        jSlider_TranslationX.setMaximum(2000);
        jSlider_TranslationX.setMinimum(-2000);
        jSlider_TranslationX.setMinorTickSpacing(1);
        jSlider_TranslationX.setSnapToTicks(true);
        jSlider_TranslationX.setValue(0);
        jSlider_TranslationX.setPreferredSize(new java.awt.Dimension(180, 29));
        jSlider_TranslationX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_TranslationX.add(jSlider_TranslationX);

        jLabel_TranslationValueX.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_TranslationValueX.setText("0");
        jLabel_TranslationValueX.setMaximumSize(new java.awt.Dimension(33, 16));
        jLabel_TranslationValueX.setMinimumSize(new java.awt.Dimension(33, 16));
        jLabel_TranslationValueX.setPreferredSize(new java.awt.Dimension(33, 16));
        jToolBar_TranslationX.add(jLabel_TranslationValueX);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_TranslationX, gridBagConstraints);

        jToolBar_TranslationY.setFloatable(false);
        jToolBar_TranslationY.setRollover(true);

        jLabel_TranslationY.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_TranslationY.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel_TranslationY.setText("Y Position");
        jLabel_TranslationY.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_TranslationY.add(jLabel_TranslationY);

        jSlider_TranslationY.setMaximum(2000);
        jSlider_TranslationY.setMinimum(-2000);
        jSlider_TranslationY.setMinorTickSpacing(1);
        jSlider_TranslationY.setSnapToTicks(true);
        jSlider_TranslationY.setValue(0);
        jSlider_TranslationY.setPreferredSize(new java.awt.Dimension(180, 29));
        jSlider_TranslationY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_TranslationY.add(jSlider_TranslationY);

        jLabel_TranslationValueY.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_TranslationValueY.setText("0");
        jLabel_TranslationValueY.setMaximumSize(new java.awt.Dimension(33, 16));
        jLabel_TranslationValueY.setMinimumSize(new java.awt.Dimension(33, 16));
        jLabel_TranslationValueY.setPreferredSize(new java.awt.Dimension(33, 16));
        jToolBar_TranslationY.add(jLabel_TranslationValueY);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_TranslationY, gridBagConstraints);

        jToolBar_ScaleX.setFloatable(false);
        jToolBar_ScaleX.setRollover(true);

        jLabel_ScaleX.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_ScaleX.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel_ScaleX.setText("X Scale");
        jLabel_ScaleX.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_ScaleX.add(jLabel_ScaleX);

        jSlider_ScaleX.setMaximum(400);
        jSlider_ScaleX.setMinorTickSpacing(1);
        jSlider_ScaleX.setSnapToTicks(true);
        jSlider_ScaleX.setValue(100);
        jSlider_ScaleX.setPreferredSize(new java.awt.Dimension(180, 29));
        jSlider_ScaleX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_ScaleX.add(jSlider_ScaleX);

        jLabel_ScaleXPercent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_ScaleXPercent.setText("1");
        jLabel_ScaleXPercent.setMaximumSize(new java.awt.Dimension(33, 16));
        jLabel_ScaleXPercent.setMinimumSize(new java.awt.Dimension(33, 16));
        jLabel_ScaleXPercent.setPreferredSize(new java.awt.Dimension(33, 16));
        jToolBar_ScaleX.add(jLabel_ScaleXPercent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_ScaleX, gridBagConstraints);

        jToolBar_ScaleY.setFloatable(false);
        jToolBar_ScaleY.setRollover(true);

        jLabel_ScaleY.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_ScaleY.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel_ScaleY.setText("Y Scale");
        jLabel_ScaleY.setPreferredSize(new java.awt.Dimension(72, 16));
        jToolBar_ScaleY.add(jLabel_ScaleY);

        jSlider_ScaleY.setMaximum(400);
        jSlider_ScaleY.setMinorTickSpacing(1);
        jSlider_ScaleY.setSnapToTicks(true);
        jSlider_ScaleY.setValue(100);
        jSlider_ScaleY.setPreferredSize(new java.awt.Dimension(180, 29));
        jSlider_ScaleY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (m_parent.getSelectedLayerIndex() != -1)
                updateImage();
            }
        });
        jToolBar_ScaleY.add(jSlider_ScaleY);

        jLabel_ScaleYPercent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_ScaleYPercent.setText("1");
        jLabel_ScaleYPercent.setMaximumSize(new java.awt.Dimension(33, 16));
        jLabel_ScaleYPercent.setMinimumSize(new java.awt.Dimension(33, 16));
        jLabel_ScaleYPercent.setPreferredSize(new java.awt.Dimension(33, 16));
        jToolBar_ScaleY.add(jLabel_ScaleYPercent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Options.add(jToolBar_ScaleY, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel_Options, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox_HFlip;
    private javax.swing.JCheckBox jCheckBox_VFlip;
    private javax.swing.JFileChooser jFileChooser_TextureSelector;
    private javax.swing.JLabel jLabel_ImageFlip;
    private javax.swing.JLabel jLabel_Opacity;
    private javax.swing.JLabel jLabel_OpacityPercent;
    private javax.swing.JLabel jLabel_Repeat;
    private javax.swing.JLabel jLabel_RepeatPercent;
    private javax.swing.JLabel jLabel_Rotation;
    private javax.swing.JLabel jLabel_RotationPercent;
    private javax.swing.JLabel jLabel_ScaleX;
    private javax.swing.JLabel jLabel_ScaleXPercent;
    private javax.swing.JLabel jLabel_ScaleY;
    private javax.swing.JLabel jLabel_ScaleYPercent;
    private javax.swing.JLabel jLabel_TranslationValueX;
    private javax.swing.JLabel jLabel_TranslationValueY;
    private javax.swing.JLabel jLabel_TranslationX;
    private javax.swing.JLabel jLabel_TranslationY;
    private javax.swing.JPanel jPanel_Options;
    private javax.swing.JSlider jSlider_Opacity;
    private javax.swing.JSlider jSlider_Repeat;
    private javax.swing.JSlider jSlider_Rotation;
    private javax.swing.JSlider jSlider_ScaleX;
    private javax.swing.JSlider jSlider_ScaleY;
    private javax.swing.JSlider jSlider_TranslationX;
    private javax.swing.JSlider jSlider_TranslationY;
    private javax.swing.JToolBar jToolBar_ImageFlip;
    private javax.swing.JToolBar jToolBar_Opacity;
    private javax.swing.JToolBar jToolBar_Repeat;
    private javax.swing.JToolBar jToolBar_Rotation;
    private javax.swing.JToolBar jToolBar_ScaleX;
    private javax.swing.JToolBar jToolBar_ScaleY;
    private javax.swing.JToolBar jToolBar_TranslationX;
    private javax.swing.JToolBar jToolBar_TranslationY;
    // End of variables declaration//GEN-END:variables

}
