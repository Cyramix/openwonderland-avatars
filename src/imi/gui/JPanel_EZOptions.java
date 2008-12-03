/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanel_EZOptions.java
 *
 * Created on Dec 2, 2008, 10:10:21 AM
 */

package imi.gui;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author ptruong
 */
public class JPanel_EZOptions extends javax.swing.JPanel {

    ArrayList<String[]>                 m_presetLists;
    ArrayList<Map<Integer, String[]>>   m_presets;
    Map<Integer, String[]>              m_addList;

    /** Creates new form JPanel_EZOptions */
    public JPanel_EZOptions() {
        initComponents();
    }

    public void readPresetList(File xmlURL) {

        try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlURL);

            // normalize text representation
            doc.getDocumentElement().normalize();
            System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());


            NodeList listOfAvatars = doc.getElementsByTagName("avatar");
            int totalAvatars = listOfAvatars.getLength();
            System.out.println("Total no of avatar presets : " + totalAvatars);

            m_presets = new ArrayList<Map<Integer, String[]>>();
            m_presetLists = new ArrayList<String[]>();

            for (int s = 0; s < listOfAvatars.getLength(); s++) {

                m_addList = new HashMap<Integer, String[]>();
                String[] list = new String[4];

                Node AvatarNode = listOfAvatars.item(s);
                if (AvatarNode.getNodeType() == Node.ELEMENT_NODE) {


                    Element avatarElement = (Element) AvatarNode;

                    //-------
                    NodeList descList = avatarElement.getElementsByTagName("desc");
                    Element descElement = (Element) descList.item(0);

                    NodeList textDescList = descElement.getChildNodes();
                    System.out.println("Avatar Desc : " + ((Node) textDescList.item(0)).getNodeValue().trim());
                    list[0] = ((Node) textDescList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList imageList = avatarElement.getElementsByTagName("image");
                    Element imageElement = (Element) imageList.item(0);

                    NodeList textImageList = imageElement.getChildNodes();
                    System.out.println("Image loc : " + ((Node) textImageList.item(0)).getNodeValue().trim());
                    list[1] = ((Node) textImageList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList bindList = avatarElement.getElementsByTagName("bind");
                    Element bindElement = (Element) bindList.item(0);

                    NodeList textBindList = bindElement.getChildNodes();
                    System.out.println("Bind loc : " + ((Node) textBindList.item(0)).getNodeValue().trim());
                    list[2] = ((Node) textBindList.item(0)).getNodeValue().trim();

                    //-------
                    NodeList headList = avatarElement.getElementsByTagName("head");
                    Element headElement = (Element) headList.item(0);

                    NodeList textHeadList = headElement.getChildNodes();
                    System.out.println("Head loc : " + ((Node) textHeadList.item(0)).getNodeValue().trim());

                    if (!((Node) textHeadList.item(0)).getNodeValue().trim().equals("null")) {
                        String source = ((Node) textHeadList.item(0)).getNodeValue().trim();
                        NodeList countList = avatarElement.getElementsByTagName("headnum");
                        Element countElement = (Element) countList.item(0);

                        NodeList textCountList = countElement.getChildNodes();
                        String szCount = ((Node) textCountList.item(0)).getNodeValue().trim();
                        int iCount = Integer.parseInt(szCount);
                        ++iCount;

                        NodeList geomList = avatarElement.getElementsByTagName("headgeom");
                        Element geomElement = (Element) geomList.item(0);

                        NodeList textGeomList = geomElement.getChildNodes();
                        String[] szMeshes = new String[iCount];

                        for (int i = 0; i < iCount; i ++) {
                            if (i == 0)
                                szMeshes[i] = source;
                            else {
                                szMeshes[i] = ((Node) textGeomList.item(0)).getNodeValue().trim();
                                if (i < iCount -1) {
                                    geomElement = (Element) geomList.item(i);
                                    textGeomList = geomElement.getChildNodes();
                                }
                            }
                        }
                        m_addList.put(0, szMeshes);
                    }

                    //-------
                    NodeList hairList = avatarElement.getElementsByTagName("hair");
                    Element hairElement = (Element) hairList.item(0);

                    NodeList textHairList = hairElement.getChildNodes();
                    System.out.println("Hair loc : " + ((Node) textHairList.item(0)).getNodeValue().trim());

                    if (!((Node) textHairList.item(0)).getNodeValue().trim().equals("null")) {
                        String source = ((Node) textHairList.item(0)).getNodeValue().trim();
                        NodeList countList = avatarElement.getElementsByTagName("hairnum");
                        Element countElement = (Element) countList.item(0);

                        NodeList textCountList = countElement.getChildNodes();
                        String szCount = ((Node) textCountList.item(0)).getNodeValue().trim();
                        int iCount = Integer.parseInt(szCount);
                        ++iCount;

                        NodeList geomList = avatarElement.getElementsByTagName("hairgeom");
                        Element geomElement = (Element) geomList.item(0);

                        NodeList textGeomList = geomElement.getChildNodes();
                        String[] szMeshes = new String[iCount];

                        for (int i = 0; i < iCount; i ++) {
                            if (i == 0)
                                szMeshes[i] = source;
                            else {
                                szMeshes[i] = ((Node) textGeomList.item(0)).getNodeValue().trim();
                                if (i < iCount -1) {
                                    geomElement = (Element) geomList.item(i);
                                    textGeomList = geomElement.getChildNodes();
                                }
                            }
                        }
                        m_addList.put(5, szMeshes);
                    }

                    //-------
                    NodeList shirtList = avatarElement.getElementsByTagName("shirt");
                    Element shirtElement = (Element) shirtList.item(0);

                    NodeList textShirtList = shirtElement.getChildNodes();
                    System.out.println("Shirt loc : " + ((Node) textShirtList.item(0)).getNodeValue().trim());

                    if (!((Node) textShirtList.item(0)).getNodeValue().trim().equals("null")) {
                        String source = ((Node) textShirtList.item(0)).getNodeValue().trim();
                        NodeList countList = avatarElement.getElementsByTagName("shirtnum");
                        Element countElement = (Element) countList.item(0);

                        NodeList textCountList = countElement.getChildNodes();
                        String szCount = ((Node) textCountList.item(0)).getNodeValue().trim();
                        int iCount = Integer.parseInt(szCount);
                        ++iCount;

                        NodeList geomList = avatarElement.getElementsByTagName("shirtgeom");
                        Element geomElement = (Element) geomList.item(0);

                        NodeList textGeomList = geomElement.getChildNodes();
                        String[] szMeshes = new String[iCount];

                        for (int i = 0; i < iCount; i ++) {
                            if (i == 0)
                                szMeshes[i] = source;
                            else {
                                szMeshes[i] = ((Node) textGeomList.item(0)).getNodeValue().trim();
                                if (i < iCount -1) {
                                    geomElement = (Element) geomList.item(i);
                                    textGeomList = geomElement.getChildNodes();
                                }
                            }
                        }
                        m_addList.put(2, szMeshes);
                    }

                    //-------
                    NodeList pantsList = avatarElement.getElementsByTagName("pants");
                    Element pantsElement = (Element) pantsList.item(0);

                    NodeList textPantsList = pantsElement.getChildNodes();
                    System.out.println("Pants loc : " + ((Node) textPantsList.item(0)).getNodeValue().trim());

                    if (!((Node) textPantsList.item(0)).getNodeValue().trim().equals("null")) {
                        String source = ((Node) textPantsList.item(0)).getNodeValue().trim();
                        NodeList countList = avatarElement.getElementsByTagName("pantsnum");
                        Element countElement = (Element) countList.item(0);

                        NodeList textCountList = countElement.getChildNodes();
                        String szCount = ((Node) textCountList.item(0)).getNodeValue().trim();
                        int iCount = Integer.parseInt(szCount);
                        ++iCount;

                        NodeList geomList = avatarElement.getElementsByTagName("pantsgeom");
                        Element geomElement = (Element) geomList.item(0);

                        NodeList textGeomList = geomElement.getChildNodes();
                        String[] szMeshes = new String[iCount];

                        for (int i = 0; i < iCount; i ++) {
                            if (i == 0)
                                szMeshes[i] = source;
                            else {
                                szMeshes[i] = ((Node) textGeomList.item(0)).getNodeValue().trim();
                                if (i < iCount -1) {
                                    geomElement = (Element) geomList.item(i);
                                    textGeomList = geomElement.getChildNodes();
                                }
                            }
                        }
                        m_addList.put(3, szMeshes);
                    }

                    //-------
                    NodeList shoesList = avatarElement.getElementsByTagName("shoes");
                    Element shoesElement = (Element) shoesList.item(0);

                    NodeList textShoesList = shoesElement.getChildNodes();
                    System.out.println("Shoes loc : " + ((Node) textShoesList.item(0)).getNodeValue().trim());

                    if (!((Node) textPantsList.item(0)).getNodeValue().trim().equals("null")) {
                        String source = ((Node) textShoesList.item(0)).getNodeValue().trim();
                        NodeList countList = avatarElement.getElementsByTagName("shoesnum");
                        Element countElement = (Element) countList.item(0);

                        NodeList textCountList = countElement.getChildNodes();
                        String szCount = ((Node) textCountList.item(0)).getNodeValue().trim();
                        int iCount = Integer.parseInt(szCount);
                        ++iCount;

                        NodeList geomList = avatarElement.getElementsByTagName("shoesgeom");
                        Element geomElement = (Element) geomList.item(0);

                        NodeList textGeomList = geomElement.getChildNodes();
                        String[] szMeshes = new String[iCount];

                        for (int i = 0; i < iCount; i ++) {
                            if (i == 0)
                                szMeshes[i] = source;
                            else {
                                szMeshes[i] = ((Node) textGeomList.item(0)).getNodeValue().trim();
                                if (i < iCount -1) {
                                    geomElement = (Element) geomList.item(i);
                                    textGeomList = geomElement.getChildNodes();
                                }
                            }
                        }
                        m_addList.put(4, szMeshes);
                    }

                    //-------
                    NodeList animList = avatarElement.getElementsByTagName("anim");
                    Element animElement = (Element) animList.item(0);

                    NodeList textAnimList = animElement.getChildNodes();
                    System.out.println("Anim loc : " + ((Node) textAnimList.item(0)).getNodeValue().trim());
                    list[3] = ((Node) textAnimList.item(0)).getNodeValue().trim();

                    //-------
                    m_presets.add(m_addList);
                    m_presetLists.add(list);

                //------


                }//end of if clause


            }//end of for loop with s var


        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
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

        buttonGroup_Gender = new javax.swing.ButtonGroup();
        buttonGroup_Ethnicity = new javax.swing.ButtonGroup();
        jLabel_TitleBar = new javax.swing.JLabel();
        jPanel_AvatarName = new javax.swing.JPanel();
        jLabel_AvatarName = new javax.swing.JLabel();
        jTextField_AvatarName = new javax.swing.JTextField();
        jPanel_AvatarGender = new javax.swing.JPanel();
        jLabel_Gender = new javax.swing.JLabel();
        jRadioButton_GenderMale = new javax.swing.JRadioButton();
        jRadioButton_GenderFemale = new javax.swing.JRadioButton();
        jPanel_AvatarEthnicity = new javax.swing.JPanel();
        jLabel_Ethnicity = new javax.swing.JLabel();
        jRadioButton_EthnicCaucasian = new javax.swing.JRadioButton();
        jRadioButton_EthnicAfrican = new javax.swing.JRadioButton();
        jRadioButton_EthnicAsian = new javax.swing.JRadioButton();
        jTabbedPane_Options = new javax.swing.JTabbedPane();
        jPanel_MainPresets = new javax.swing.JPanel();
        jScrollPane_Presets = new javax.swing.JScrollPane();
        jTable_Presets = new javax.swing.JTable();
        jPanel_MainColors = new javax.swing.JPanel();
        jLabel_ColorHair = new javax.swing.JLabel();
        jPanel_ColorHair = new javax.swing.JPanel();
        jButton_SelectColorH = new javax.swing.JButton();
        jLabel_ColorFacialHair = new javax.swing.JLabel();
        jPanel_ColorFacialHair = new javax.swing.JPanel();
        jButton_SelectColorFH = new javax.swing.JButton();
        jLabel_ColorSkin = new javax.swing.JLabel();
        jPanel_ColorSkin = new javax.swing.JPanel();
        jButton_SelectColorS = new javax.swing.JButton();
        jLabel_ColorShirt = new javax.swing.JLabel();
        jPanel_ColorShirt = new javax.swing.JPanel();
        jButton_SelectColorSh = new javax.swing.JButton();
        jLabel_ColorPants = new javax.swing.JLabel();
        jPanel_ColorPants = new javax.swing.JPanel();
        jButton_SelectColorP = new javax.swing.JButton();
        jLabel_ColorShoes = new javax.swing.JLabel();
        jPanel_ColorShoes = new javax.swing.JPanel();
        jButton_SelectColorShoes = new javax.swing.JButton();
        jButton_Load = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(320, 480));
        setPreferredSize(new java.awt.Dimension(320, 480));
        setLayout(new java.awt.GridBagLayout());

        jLabel_TitleBar.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel_TitleBar.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_TitleBar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_TitleBar.setText("Quick Pick");
        jLabel_TitleBar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel_TitleBar.setMinimumSize(new java.awt.Dimension(320, 25));
        jLabel_TitleBar.setPreferredSize(new java.awt.Dimension(320, 25));
        add(jLabel_TitleBar, new java.awt.GridBagConstraints());

        jPanel_AvatarName.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_AvatarName.setMinimumSize(new java.awt.Dimension(320, 50));
        jPanel_AvatarName.setPreferredSize(new java.awt.Dimension(320, 50));
        jPanel_AvatarName.setLayout(new java.awt.GridBagLayout());

        jLabel_AvatarName.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_AvatarName.setText("Name:");
        jLabel_AvatarName.setMaximumSize(new java.awt.Dimension(100, 25));
        jLabel_AvatarName.setMinimumSize(new java.awt.Dimension(60, 25));
        jLabel_AvatarName.setPreferredSize(new java.awt.Dimension(60, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_AvatarName.add(jLabel_AvatarName, gridBagConstraints);

        jTextField_AvatarName.setText("User");
        jTextField_AvatarName.setMinimumSize(new java.awt.Dimension(240, 25));
        jTextField_AvatarName.setPreferredSize(new java.awt.Dimension(240, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_AvatarName.add(jTextField_AvatarName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jPanel_AvatarName, gridBagConstraints);

        jPanel_AvatarGender.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_AvatarGender.setMinimumSize(new java.awt.Dimension(320, 50));
        jPanel_AvatarGender.setPreferredSize(new java.awt.Dimension(320, 50));
        jPanel_AvatarGender.setLayout(new java.awt.GridBagLayout());

        jLabel_Gender.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Gender.setText("Gender:");
        jLabel_Gender.setMaximumSize(new java.awt.Dimension(100, 25));
        jLabel_Gender.setMinimumSize(new java.awt.Dimension(60, 25));
        jLabel_Gender.setPreferredSize(new java.awt.Dimension(60, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_AvatarGender.add(jLabel_Gender, gridBagConstraints);

        buttonGroup_Gender.add(jRadioButton_GenderMale);
        jRadioButton_GenderMale.setSelected(true);
        jRadioButton_GenderMale.setText("Male");
        jRadioButton_GenderMale.setMinimumSize(new java.awt.Dimension(95, 25));
        jRadioButton_GenderMale.setPreferredSize(new java.awt.Dimension(95, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_AvatarGender.add(jRadioButton_GenderMale, gridBagConstraints);

        buttonGroup_Gender.add(jRadioButton_GenderFemale);
        jRadioButton_GenderFemale.setText("Female");
        jRadioButton_GenderFemale.setMinimumSize(new java.awt.Dimension(145, 25));
        jRadioButton_GenderFemale.setPreferredSize(new java.awt.Dimension(145, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_AvatarGender.add(jRadioButton_GenderFemale, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(jPanel_AvatarGender, gridBagConstraints);

        jPanel_AvatarEthnicity.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_AvatarEthnicity.setMinimumSize(new java.awt.Dimension(320, 50));
        jPanel_AvatarEthnicity.setPreferredSize(new java.awt.Dimension(320, 50));
        jPanel_AvatarEthnicity.setLayout(new java.awt.GridBagLayout());

        jLabel_Ethnicity.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Ethnicity.setText("Ethnicity:");
        jLabel_Ethnicity.setMaximumSize(new java.awt.Dimension(100, 25));
        jLabel_Ethnicity.setMinimumSize(new java.awt.Dimension(60, 25));
        jLabel_Ethnicity.setPreferredSize(new java.awt.Dimension(60, 25));
        jPanel_AvatarEthnicity.add(jLabel_Ethnicity, new java.awt.GridBagConstraints());

        buttonGroup_Ethnicity.add(jRadioButton_EthnicCaucasian);
        jRadioButton_EthnicCaucasian.setSelected(true);
        jRadioButton_EthnicCaucasian.setText("Caucasian");
        jPanel_AvatarEthnicity.add(jRadioButton_EthnicCaucasian, new java.awt.GridBagConstraints());

        buttonGroup_Ethnicity.add(jRadioButton_EthnicAfrican);
        jRadioButton_EthnicAfrican.setText("African");
        jPanel_AvatarEthnicity.add(jRadioButton_EthnicAfrican, new java.awt.GridBagConstraints());

        buttonGroup_Ethnicity.add(jRadioButton_EthnicAsian);
        jRadioButton_EthnicAsian.setText("Asian");
        jPanel_AvatarEthnicity.add(jRadioButton_EthnicAsian, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(jPanel_AvatarEthnicity, gridBagConstraints);

        jTabbedPane_Options.setMinimumSize(new java.awt.Dimension(320, 310));
        jTabbedPane_Options.setPreferredSize(new java.awt.Dimension(320, 275));

        jTable_Presets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane_Presets.setViewportView(jTable_Presets);

        org.jdesktop.layout.GroupLayout jPanel_MainPresetsLayout = new org.jdesktop.layout.GroupLayout(jPanel_MainPresets);
        jPanel_MainPresets.setLayout(jPanel_MainPresetsLayout);
        jPanel_MainPresetsLayout.setHorizontalGroup(
            jPanel_MainPresetsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 299, Short.MAX_VALUE)
            .add(jPanel_MainPresetsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel_MainPresetsLayout.createSequentialGroup()
                    .add(0, 2, Short.MAX_VALUE)
                    .add(jScrollPane_Presets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 295, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 2, Short.MAX_VALUE)))
        );
        jPanel_MainPresetsLayout.setVerticalGroup(
            jPanel_MainPresetsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 260, Short.MAX_VALUE)
            .add(jPanel_MainPresetsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel_MainPresetsLayout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jScrollPane_Presets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 260, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, Short.MAX_VALUE)))
        );

        jTabbedPane_Options.addTab("Avatars", jPanel_MainPresets);

        jPanel_MainColors.setLayout(new java.awt.GridBagLayout());

        jLabel_ColorHair.setText("Hair Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainColors.add(jLabel_ColorHair, gridBagConstraints);

        jPanel_ColorHair.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_ColorHair.setMinimumSize(new java.awt.Dimension(25, 25));
        jPanel_ColorHair.setPreferredSize(new java.awt.Dimension(25, 25));

        org.jdesktop.layout.GroupLayout jPanel_ColorHairLayout = new org.jdesktop.layout.GroupLayout(jPanel_ColorHair);
        jPanel_ColorHair.setLayout(jPanel_ColorHairLayout);
        jPanel_ColorHairLayout.setHorizontalGroup(
            jPanel_ColorHairLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        jPanel_ColorHairLayout.setVerticalGroup(
            jPanel_ColorHairLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainColors.add(jPanel_ColorHair, gridBagConstraints);

        jButton_SelectColorH.setText("Choose Color");
        jPanel_MainColors.add(jButton_SelectColorH, new java.awt.GridBagConstraints());

        jLabel_ColorFacialHair.setText("Facial Hair Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_MainColors.add(jLabel_ColorFacialHair, gridBagConstraints);

        jPanel_ColorFacialHair.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_ColorFacialHair.setMinimumSize(new java.awt.Dimension(25, 25));

        org.jdesktop.layout.GroupLayout jPanel_ColorFacialHairLayout = new org.jdesktop.layout.GroupLayout(jPanel_ColorFacialHair);
        jPanel_ColorFacialHair.setLayout(jPanel_ColorFacialHairLayout);
        jPanel_ColorFacialHairLayout.setHorizontalGroup(
            jPanel_ColorFacialHairLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        jPanel_ColorFacialHairLayout.setVerticalGroup(
            jPanel_ColorFacialHairLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainColors.add(jPanel_ColorFacialHair, gridBagConstraints);

        jButton_SelectColorFH.setText("Choose Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel_MainColors.add(jButton_SelectColorFH, gridBagConstraints);

        jLabel_ColorSkin.setText("Skin Tone");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainColors.add(jLabel_ColorSkin, gridBagConstraints);

        jPanel_ColorSkin.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_ColorSkin.setMinimumSize(new java.awt.Dimension(25, 25));

        org.jdesktop.layout.GroupLayout jPanel_ColorSkinLayout = new org.jdesktop.layout.GroupLayout(jPanel_ColorSkin);
        jPanel_ColorSkin.setLayout(jPanel_ColorSkinLayout);
        jPanel_ColorSkinLayout.setHorizontalGroup(
            jPanel_ColorSkinLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        jPanel_ColorSkinLayout.setVerticalGroup(
            jPanel_ColorSkinLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainColors.add(jPanel_ColorSkin, gridBagConstraints);

        jButton_SelectColorS.setText("Choose Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        jPanel_MainColors.add(jButton_SelectColorS, gridBagConstraints);

        jLabel_ColorShirt.setText("Shirt Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainColors.add(jLabel_ColorShirt, gridBagConstraints);

        jPanel_ColorShirt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_ColorShirt.setMinimumSize(new java.awt.Dimension(25, 25));

        org.jdesktop.layout.GroupLayout jPanel_ColorShirtLayout = new org.jdesktop.layout.GroupLayout(jPanel_ColorShirt);
        jPanel_ColorShirt.setLayout(jPanel_ColorShirtLayout);
        jPanel_ColorShirtLayout.setHorizontalGroup(
            jPanel_ColorShirtLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        jPanel_ColorShirtLayout.setVerticalGroup(
            jPanel_ColorShirtLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainColors.add(jPanel_ColorShirt, gridBagConstraints);

        jButton_SelectColorSh.setText("Choose Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        jPanel_MainColors.add(jButton_SelectColorSh, gridBagConstraints);

        jLabel_ColorPants.setText("Pants Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainColors.add(jLabel_ColorPants, gridBagConstraints);

        jPanel_ColorPants.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_ColorPants.setMinimumSize(new java.awt.Dimension(25, 25));

        org.jdesktop.layout.GroupLayout jPanel_ColorPantsLayout = new org.jdesktop.layout.GroupLayout(jPanel_ColorPants);
        jPanel_ColorPants.setLayout(jPanel_ColorPantsLayout);
        jPanel_ColorPantsLayout.setHorizontalGroup(
            jPanel_ColorPantsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        jPanel_ColorPantsLayout.setVerticalGroup(
            jPanel_ColorPantsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainColors.add(jPanel_ColorPants, gridBagConstraints);

        jButton_SelectColorP.setText("Choose Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        jPanel_MainColors.add(jButton_SelectColorP, gridBagConstraints);

        jLabel_ColorShoes.setText("Shoes Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainColors.add(jLabel_ColorShoes, gridBagConstraints);

        jPanel_ColorShoes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_ColorShoes.setMinimumSize(new java.awt.Dimension(25, 25));

        org.jdesktop.layout.GroupLayout jPanel_ColorShoesLayout = new org.jdesktop.layout.GroupLayout(jPanel_ColorShoes);
        jPanel_ColorShoes.setLayout(jPanel_ColorShoesLayout);
        jPanel_ColorShoesLayout.setHorizontalGroup(
            jPanel_ColorShoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        jPanel_ColorShoesLayout.setVerticalGroup(
            jPanel_ColorShoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainColors.add(jPanel_ColorShoes, gridBagConstraints);

        jButton_SelectColorShoes.setText("Choose Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        jPanel_MainColors.add(jButton_SelectColorShoes, gridBagConstraints);

        jTabbedPane_Options.addTab("Colors", jPanel_MainColors);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        add(jTabbedPane_Options, gridBagConstraints);

        jButton_Load.setText("Load");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton_Load, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_Ethnicity;
    private javax.swing.ButtonGroup buttonGroup_Gender;
    private javax.swing.JButton jButton_Load;
    private javax.swing.JButton jButton_SelectColorFH;
    private javax.swing.JButton jButton_SelectColorH;
    private javax.swing.JButton jButton_SelectColorP;
    private javax.swing.JButton jButton_SelectColorS;
    private javax.swing.JButton jButton_SelectColorSh;
    private javax.swing.JButton jButton_SelectColorShoes;
    private javax.swing.JLabel jLabel_AvatarName;
    private javax.swing.JLabel jLabel_ColorFacialHair;
    private javax.swing.JLabel jLabel_ColorHair;
    private javax.swing.JLabel jLabel_ColorPants;
    private javax.swing.JLabel jLabel_ColorShirt;
    private javax.swing.JLabel jLabel_ColorShoes;
    private javax.swing.JLabel jLabel_ColorSkin;
    private javax.swing.JLabel jLabel_Ethnicity;
    private javax.swing.JLabel jLabel_Gender;
    private javax.swing.JLabel jLabel_TitleBar;
    private javax.swing.JPanel jPanel_AvatarEthnicity;
    private javax.swing.JPanel jPanel_AvatarGender;
    private javax.swing.JPanel jPanel_AvatarName;
    private javax.swing.JPanel jPanel_ColorFacialHair;
    private javax.swing.JPanel jPanel_ColorHair;
    private javax.swing.JPanel jPanel_ColorPants;
    private javax.swing.JPanel jPanel_ColorShirt;
    private javax.swing.JPanel jPanel_ColorShoes;
    private javax.swing.JPanel jPanel_ColorSkin;
    private javax.swing.JPanel jPanel_MainColors;
    private javax.swing.JPanel jPanel_MainPresets;
    private javax.swing.JRadioButton jRadioButton_EthnicAfrican;
    private javax.swing.JRadioButton jRadioButton_EthnicAsian;
    private javax.swing.JRadioButton jRadioButton_EthnicCaucasian;
    private javax.swing.JRadioButton jRadioButton_GenderFemale;
    private javax.swing.JRadioButton jRadioButton_GenderMale;
    private javax.swing.JScrollPane jScrollPane_Presets;
    private javax.swing.JTabbedPane jTabbedPane_Options;
    private javax.swing.JTable jTable_Presets;
    private javax.swing.JTextField jTextField_AvatarName;
    // End of variables declaration//GEN-END:variables

}
