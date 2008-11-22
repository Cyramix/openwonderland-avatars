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

package imi.gui;
////////////////////////////////////////////////////////////////////////////////
// Imports - BEGIN
////////////////////////////////////////////////////////////////////////////////
import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
////////////////////////////////////////////////////////////////////////////////
// Imports - END
////////////////////////////////////////////////////////////////////////////////

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JPanel_BasicOptions extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /** Scene Info */
    private SceneEssentials         m_sceneData;
    /** Data Containers */
    private Map<Integer, String[]>  m_meshes;
    /** Others */
    private int                     m_gender;
    private boolean                 m_isViewMode;
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - END
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form JPanel_BasicOptions */
    public JPanel_BasicOptions() {
        initComponents();
    }
    ////////////////////////////////////////////////////////////////////////////
    // Inits
    ////////////////////////////////////////////////////////////////////////////
    public void InitHeadList(boolean isViewMode) {
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT name, description FROM DefaultAvatars WHERE name like '%Head%'";
        else
            query = "SELECT name, description FROM DefaultAvatars WHERE name like '%Head%' and bodytype = " + m_gender;

        data = m_sceneData.loadSQLData(query);
        
        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_Heads.setListData(list);
            jList_Heads1.setListData(list);
            return;
        }
        
        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[1];
        }

        jList_Heads.setListData(list);
        jList_Heads1.setListData(list);
    }

    public void InitUpperBodyList(boolean isViewMode) {
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT description FROM Meshes WHERE type = 3";
        else
            query = "SELECT description FROM Meshes WHERE type = 3 and bodyType = " + m_gender;

        data = m_sceneData.loadSQLData(query);
        
        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_UpperBody.setListData(list);
            return;
        }
        
        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[0];
        }

        jList_UpperBody.setListData(list);
    }

    public void InitLowerBodyList(boolean isViewMode) {
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT description FROM Meshes WHERE type in (5,6)";
        else
            query = "SELECT description FROM Meshes WHERE type in (5,6) and bodyType = " + m_gender;

        data = m_sceneData.loadSQLData(query);

        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_LowerBody.setListData(list);
            return;
        }
        
        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[0];
        }

        jList_LowerBody.setListData(list);
    }

    public void InitShoesList(boolean isViewMode) {
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT description FROM Meshes WHERE type = 10";
        else
            query = "SELECT description FROM Meshes WHERE type = 10 and bodyType = " + m_gender;
        data = m_sceneData.loadSQLData(query);

        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_Shoes.setListData(list);
            return;
        }

        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[0];
        }

        jList_Shoes.setListData(list);
    }

    public void InitHairList(boolean isViewMode) {
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT description FROM Meshes WHERE type = 0";
        else
            query = "SELECT description FROM Meshes WHERE type = 0 and bodyType = " + m_gender;

        data = m_sceneData.loadSQLData(query);

        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_Hair.setListData(list);
            return;
        }

        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[0];
        }

        jList_Hair.setListData(list);
    }

    public void InitFacialHairList(boolean isViewMode) {
        // TODO: Once we get some facial hair stuff
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT description FROM Meshes WHERE type = 11";
        else
            query = "SELECT description FROM Meshes WHERE type = 11 and bodyType = " + m_gender;

        data = m_sceneData.loadSQLData(query);

        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_FacialHair.setListData(list);
            return;
        }

        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[0];
        }

        jList_FacialHair.setListData(list);
    }

    public void InitHatsList(boolean isViewMode) {
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT description FROM Meshes WHERE type = 1";
        else
            query = "SELECT description FROM Meshes WHERE type = 1 and bodyType = " + m_gender;

        data = m_sceneData.loadSQLData(query);
        
        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_Hats.setListData(list);
            return;
        }

        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[0];
        }

        jList_Hats.setListData(list);
    }

    public void InitSpecsList(boolean isViewMode) {
        ArrayList<String[]> data;
        String[] list;
        String query = new String();

        if (isViewMode)
            query = "SELECT description FROM Meshes WHERE type = 2";
        else
            query = "SELECT description FROM Meshes WHERE type = 2 and bodyType = " + m_gender;

        data = m_sceneData.loadSQLData(query);
        
        if (data.size() <= 0) {
            list = new String[1];
            list[0] = "N/A";
            jList_Specs.setListData(list);
            return;
        }

        list = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            list[i] = data.get(i)[0];
        }

        jList_Specs.setListData(list);
    }

    public void InitListBoxes(boolean isViewMode) {
        InitHeadList(isViewMode);
        InitUpperBodyList(isViewMode);
        InitLowerBodyList(isViewMode);
        InitShoesList(isViewMode);
        InitHairList(isViewMode);
        InitFacialHairList(isViewMode);
        InitHatsList(isViewMode);
        InitSpecsList(isViewMode);
    }
    ////////////////////////////////////////////////////////////////////////////
    // Loading
    ////////////////////////////////////////////////////////////////////////////
    public void loadHead(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_Heads.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyHead.isEnabled())
            return;

        jButton_ApplyHead.setEnabled(false);
        String selection = jList_Heads.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, id FROM DefaultAvatars WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[4];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode) {
            String[] anim = null;
            m_sceneData.loadAvatarDAEURL(true, true, this, data.get(0), anim, meshes, 0);
        }
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 0);
            m_meshes.put(0, meshes);
        }

        jButton_ApplyHead.setEnabled(true);
        jLabel_CurrHead.setText(selection);
        jLabel_CurrHead1.setText(selection);
    }

    public void loadHead1(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_Heads1.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyHead1.isEnabled())
            return;

        jButton_ApplyHead1.setEnabled(false);
        String selection = jList_Heads1.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, id FROM DefaultAvatars WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[4];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode) {
            String[] anim = null;
            m_sceneData.loadAvatarDAEURL(true, true, this, data.get(0), anim, meshes, 0);
        }
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 0);
            m_meshes.put(0, meshes);
        }

        jButton_ApplyHead1.setEnabled(true);
        jLabel_CurrHead.setText(selection);
        jLabel_CurrHead1.setText(selection);
    }

    public void loadUpperBody(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_UpperBody.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyBody.isEnabled())
            return;

        jButton_ApplyBody.setEnabled(false);
        String selection = jList_UpperBody.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, type, id FROM Meshes WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[5];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode)
            m_sceneData.loadMeshDAEURL(true, true, this, data.get(0), meshes, 2);
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 2);
            m_meshes.put(2, meshes);
        }

        jButton_ApplyBody.setEnabled(true);
        jLabel_CurrUpperBody.setText(selection);
    }

    public void loadLowerBody(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_LowerBody.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyLegs.isEnabled())
            return;

        jButton_ApplyLegs.setEnabled(false);
        String selection = jList_LowerBody.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, type, id FROM Meshes WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[5];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode)
            m_sceneData.loadMeshDAEURL(true, true, this, data.get(0), meshes, 3);
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 3);
            m_meshes.put(3, meshes);
        }

        jButton_ApplyLegs.setEnabled(true);
        jLabel_CurrLowerBody.setText(selection);
    }

    public void loadShoes(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_Shoes.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyShoes.isEnabled())
            return;

        jButton_ApplyShoes.setEnabled(false);
        String selection = jList_Shoes.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, type, id FROM Meshes WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[5];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode)
            m_sceneData.loadMeshDAEURL(true, true, this, data.get(0), meshes, 4);
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 4);
            m_meshes.put(4, meshes);
        }

        jButton_ApplyShoes.setEnabled(true);
        jLabel_CurrShoes.setText(selection);
    }

    public void loadHair(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_Hair.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyHair.isEnabled())
            return;

        jButton_ApplyHair.setEnabled(false);
        String selection = jList_Hair.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, type, id FROM Meshes WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[5];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode)
            m_sceneData.loadMeshDAEURL(true, true, this, data.get(0), meshes, 5);
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 5);
            String[] hair = new String[] {data.get(0)[0]};
            m_meshes.put(5, hair);
            m_sceneData.setMeshSetup(m_meshes);
        }

        jButton_ApplyHair.setEnabled(true);
        jLabel_CurrHair.setText(selection);
    }

    public void loadFacialHair(boolean isViewmode) {
        if (!isViewmode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_FacialHair.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyFacialHair.isEnabled())
            return;

        jButton_ApplyFacialHair.setEnabled(false);
        String selection = jList_FacialHair.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, type, id FROM Meshes WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[5];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewmode)
            m_sceneData.loadMeshDAEURL(true, true, this, data.get(0), meshes, 6);
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 6);
            String[] facialHair = new String[] {};
            m_meshes.put(6, facialHair);
            m_sceneData.setMeshSetup(m_meshes);
        }

        jButton_ApplyFacialHair.setEnabled(true);
        jLabel_CurrFacialHair.setText(selection);
    }

    public void loadHats(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_Hats.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplyHat.isEnabled())
            return;

        jButton_ApplyHat.setEnabled(false);
        String selection = jList_Hats.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, type, id FROM Meshes WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[5];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode)
            m_sceneData.loadMeshDAEURL(true, true, this, data.get(0), meshes, 7);
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 7);
            String[] hair = new String[] {data.get(0)[0]};
            m_meshes.put(7, hair);
            m_sceneData.setMeshSetup(m_meshes);
        }

        jButton_ApplyHat.setEnabled(true);
        jLabel_CurrHat.setText(selection);
    }

    public void loadSpecs(boolean isViewMode) {
        if (!isViewMode) {
            if (m_sceneData.getCurrentSkeleton() == null)
                return;
        }

        if (jList_Specs.getSelectedValues().length == 0)
            return;

        if (!jButton_ApplySpecs.isEnabled())
            return;

        jButton_ApplySpecs.setEnabled(false);
        String selection = jList_Specs.getSelectedValues()[0].toString();

        if (selection.equals("N/A"))
            return;

        String query = new String();
        ArrayList<String[]> data, meshref;
        String[] meshes;

        query = "SELECT name, description, bodytype, url, type, id FROM Meshes WHERE description = '" + selection + "'";
        data = m_sceneData.loadSQLData(query);

        query = "SELECT name FROM GeometryReferences WHERE referenceid = " + data.get(0)[5];
        meshref = m_sceneData.loadSQLData(query);

        meshes = new String[meshref.size()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = meshref.get(i)[0];
        }

        if (isViewMode)
            m_sceneData.loadMeshDAEURL(true, true, this, data.get(0), meshes, 8);
        else {
            m_sceneData.loadMeshDAEURL(false, true, this, data.get(0), meshes, 8);
            String[] hair = new String[] {data.get(0)[0]};
            m_meshes.put(8, hair);
            m_sceneData.setMeshSetup(m_meshes);
        }

        jButton_ApplySpecs.setEnabled(true);
        jLabel_CurrSpecs.setText(selection);
    }

    public void applyAllBody(boolean isViewMode) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (!isViewMode) {
            loadHead(isViewMode);
            loadUpperBody(isViewMode);
            loadLowerBody(isViewMode);
            loadShoes(isViewMode);
        }

        setCursor(null);
    }

    public void applyAllHair(boolean isViewMode) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (!isViewMode) {
            loadHead1(isViewMode);
            loadHair(isViewMode);
            loadFacialHair(isViewMode);
        }

        setCursor(null);
    }

    public void applyAllAcc(boolean isViewMode) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (!isViewMode) {
            loadHats(m_isViewMode);
            loadSpecs(m_isViewMode);
        }

        setCursor(null);
    }

    public void loadDefaultAvatar() {
        if (!jButton_Male.isEnabled())
            return;

        if (!jButton_Female.isEnabled())
            return;

        jButton_Male.setEnabled(false);
        jButton_Female.setEnabled(false);
        String query = new String();
        ArrayList<String[]> data, anim, meshref;

        if (m_gender == 1) {
            query = "SELECT name, description, bodytype, url, id FROM DefaultAvatars WHERE id = 1";
            data = m_sceneData.loadSQLData(query);

            query = "SELECT url FROM Animations WHERE avatarid = 1 and name like '%Idle%'";
            anim = m_sceneData.loadSQLData(query);

            query = "SELECT name, grouping FROM GeometryReferences WHERE tableref = 'Male'";
            meshref = m_sceneData.loadSQLData(query);
        }
        else {
            query = "SELECT name, description, bodytype, url, id FROM DefaultAvatars WHERE id = 2";
            data = m_sceneData.loadSQLData(query);

            query = "SELECT url FROM Animations WHERE avatarid = 2 and name like '%Idle%'";
            anim = m_sceneData.loadSQLData(query);

            query = "SELECT name, grouping FROM GeometryReferences WHERE tableref = 'Female'";
            meshref = m_sceneData.loadSQLData(query);
        }

        if (m_meshes != null)
            m_meshes.clear();
        m_meshes = new HashMap<Integer, String[]>();

        createMeshSwapList("0", meshref);
        createMeshSwapList("1", meshref);
        createMeshSwapList("2", meshref);
        createMeshSwapList("3", meshref);
        createMeshSwapList("4", meshref);
        createMeshSwapList("5", meshref);
        createMeshSwapList("6", meshref);
        createMeshSwapList("7", meshref);
        createMeshSwapList("8", meshref);

        m_sceneData.setMeshSetup(m_meshes);

        // NOTE: the last 2 params not important since we are adding in a new avatar
        m_sceneData.loadAvatarDAEURL(true, true, this, data.get(0), anim.get(0), anim.get(0), 0);

        if (m_gender == 1) {
            m_meshes = m_sceneData.getMeshSetup();
            String geom = null;
            String query2 = null;
            String[] meshies = new String[3];
            ArrayList<String[]> data2;

            for (int i = 0; i < 3; i++) {
                geom = m_meshes.get(i+2)[0];
                query = "SELECT referenceid FROM GeometryReferences WHERE tableref = 'Meshes' and name = '" + geom + "'";
                data2 = m_sceneData.loadSQLData(query);
                query = "SELECT description FROM Meshes WHERE id = " + data2.get(0)[0];
                data2.clear();
                data2 = m_sceneData.loadSQLData(query);
                meshies[i] = data2.get(0)[0];
            }

            jLabel_CurrUpperBody.setText(meshies[0]);
            jLabel_CurrLowerBody.setText(meshies[1]);
            jLabel_CurrShoes.setText(meshies[2]);
        }

        jButton_Male.setEnabled(true);
        jButton_Female.setEnabled(true);
    }

    public void loadMaleAvatar(File path) {
        if (!jButton_Male.isEnabled())
            return;

        jButton_Male.setEnabled(false);
        jButton_Female.setEnabled(false);
        String query = new String();
        ArrayList<String[]> data, anim, meshref;
        String[] nada = null;

        query = "SELECT name, grouping FROM GeometryReferences WHERE tableref = 'Male'";
        meshref = m_sceneData.loadSQLData(query);

        if (m_meshes != null)
            m_meshes.clear();
        m_meshes = new HashMap<Integer, String[]>();

        createMeshSwapList("0", meshref);
        createMeshSwapList("1", meshref);
        createMeshSwapList("2", meshref);
        createMeshSwapList("3", meshref);
        createMeshSwapList("4", meshref);
        createMeshSwapList("5", meshref);
        createMeshSwapList("6", meshref);
        createMeshSwapList("7", meshref);
        createMeshSwapList("8", meshref);

        m_sceneData.setMeshSetup(m_meshes);

        // NOTE: the last 2 params not important since we are adding in a new avatar
        m_sceneData.loadUnZippedAvatar(true, true, this, path, nada, 0);

//        if (m_gender == 1) {
//            m_meshes = m_sceneData.getMeshSetup();
//            String geom = null;
//            String query2 = null;
//            String[] meshies = new String[3];
//            ArrayList<String[]> data2;
//
//            for (int i = 0; i < 3; i++) {
//                geom = m_meshes.get(i+2)[0];
//                query = "SELECT referenceid FROM GeometryReferences WHERE tableref = 'Meshes' and name = '" + geom + "'";
//                data2 = m_sceneData.loadSQLData(query);
//                query = "SELECT description FROM Meshes WHERE id = " + data2.get(0)[0];
//                data2.clear();
//                data2 = m_sceneData.loadSQLData(query);
//                meshies[i] = data2.get(0)[0];
//            }
//
//            jLabel_CurrUpperBody.setText(meshies[0]);
//            jLabel_CurrLowerBody.setText(meshies[1]);
//            jLabel_CurrShoes.setText(meshies[2]);
//        }

        jButton_Male.setEnabled(true);
        jButton_Female.setEnabled(true);
    }

    public void loadFemaleAvatar(File path) {
        if (!jButton_Male.isEnabled())
            return;

        jButton_Male.setEnabled(false);
        jButton_Female.setEnabled(false);
        String query = new String();
        ArrayList<String[]> data, anim, meshref;
        String[] nada = null;

        query = "SELECT name, grouping FROM GeometryReferences WHERE tableref = 'Female'";
        meshref = m_sceneData.loadSQLData(query);

        if (m_meshes != null)
            m_meshes.clear();
        m_meshes = new HashMap<Integer, String[]>();

        createMeshSwapList("0", meshref);
        createMeshSwapList("1", meshref);
        createMeshSwapList("2", meshref);
        createMeshSwapList("3", meshref);
        createMeshSwapList("4", meshref);
        createMeshSwapList("5", meshref);
        createMeshSwapList("6", meshref);
        createMeshSwapList("7", meshref);
        createMeshSwapList("8", meshref);

        m_sceneData.setMeshSetup(m_meshes);

        // NOTE: the last 2 params not important since we are adding in a new avatar
        m_sceneData.loadUnZippedAvatar(true, true, this, path, nada, 0);

        jButton_Male.setEnabled(true);
        jButton_Female.setEnabled(true);
    }

    public void createMeshSwapList(String region, ArrayList<String[]> meshes) {
        String[] geometry = null;
        int iRegion = 0;

        ArrayList<String> temp = new ArrayList<String>();

        for (int i = 0; i < meshes.size(); i++) {
            if (meshes.get(i)[1].equals(region)) {
                temp.add(meshes.get(i)[0].toString());
            }
        }

        if (temp.size() == 0)
            return;

        geometry = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            geometry[i] = temp.get(i);
        }

        if (region.equals("0"))
            iRegion = 0;          // Head
        else if (region.equals("1"))
            iRegion = 1;          // Hands
        else if (region.equals("2"))
            iRegion = 2;          // Torso
        else if (region.equals("3"))
            iRegion = 3;          // Legs
        else if (region.equals("4"))
            iRegion = 4;          // Feet
        else if (region.equals("5"))
            iRegion = 5;          // Hair
        else if (region.equals("6"))
            iRegion = 6;          // Facial Hair
        else if (region.equals("7"))
            iRegion = 7;          // Hats
        else if (region.equals("8"))
            iRegion = 8;          // Glasses

        m_meshes.put(iRegion, geometry);
    }

    public void maleAvatarMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        m_gender = 1;
        m_isViewMode = false;
        jButton_ApplyAllHead.setEnabled(true);
        jButton_ApplyAllBody.setEnabled(true);
        jButton_ApplyAllAcc.setEnabled(true);

        InitListBoxes(m_isViewMode);
        m_sceneData.setDefaultLoad(false);
        
        ////////////////////////////////////////////////////////////////////////
        // ZIP Stream TEST
        ////////////////////////////////////////////////////////////////////////
        String query = "SELECT url FROM DefaultAvatars WHERE id = 5";
        ArrayList<String[]> data = m_sceneData.loadSQLData(query);

        String destination = System.getProperty("user.dir");
        destination += "/temp/";
        File dest = new File(destination);
        int index = data.get(0)[0].lastIndexOf("/");
        int indez = data.get(0)[0].lastIndexOf(".");
        String name = data.get(0)[0].substring(index+1);
        String fold = data.get(0)[0].substring(index+1, indez);
        fold += "/";
        dest = new File(dest, name);
        m_sceneData.downloadZipStream(data.get(0)[0], dest);
        dest = new File(dest.getParent(), fold);
        loadMaleAvatar(dest);
        dest.getParentFile().deleteOnExit();
        ////////////////////////////////////////////////////////////////////////
        // ZIP Stream TEST
        ////////////////////////////////////////////////////////////////////////

        //loadDefaultAvatar();

        jPanel_MainBody.setVisible(true);
        jPanel_MainHead.setVisible(true);
        jPanel_MainAcc.setVisible(true);
        setCursor(null);
    }

    public void femaleAvatarMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        m_gender = 2;
        m_isViewMode = false;
        jButton_ApplyAllHead.setEnabled(true);
        jButton_ApplyAllBody.setEnabled(true);
        jButton_ApplyAllAcc.setEnabled(true);

        InitListBoxes(m_isViewMode);
        m_sceneData.setDefaultLoad(false);
        ////////////////////////////////////////////////////////////////////////
        // ZIP Stream TEST
        ////////////////////////////////////////////////////////////////////////
        String query = "SELECT url FROM DefaultAvatars WHERE id = 6";
        ArrayList<String[]> data = m_sceneData.loadSQLData(query);

        String destination = System.getProperty("user.dir");
        destination += "/temp/";
        File dest = new File(destination);
        int index = data.get(0)[0].lastIndexOf("/");
        int indez = data.get(0)[0].lastIndexOf(".");
        String name = data.get(0)[0].substring(index+1);
        String fold = data.get(0)[0].substring(index+1, indez);
        fold += "/";
        dest = new File(dest, name);
        m_sceneData.downloadZipStream(data.get(0)[0], dest);
        dest = new File(dest.getParent(), fold);
        loadMaleAvatar(dest);
        dest.getParentFile().deleteOnExit();
        ////////////////////////////////////////////////////////////////////////
        // ZIP Stream TEST
        ////////////////////////////////////////////////////////////////////////

        //loadDefaultAvatar();

        jPanel_MainBody.setVisible(true);
        jPanel_MainHead.setVisible(true);
        jPanel_MainAcc.setVisible(true);
        setCursor(null);
    }

    public void viewMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        m_isViewMode = true;
        jButton_ApplyAllHead.setEnabled(false);
        jButton_ApplyAllBody.setEnabled(false);
        jButton_ApplyAllAcc.setEnabled(false);

        InitListBoxes(m_isViewMode);

        jPanel_MainBody.setVisible(true);
        jPanel_MainHead.setVisible(true);
        jPanel_MainAcc.setVisible(true);
        setCursor(null);
    }

    /** Accessors */
    public SceneEssentials getSceneData() {
        return m_sceneData;
    }

    public Map<Integer, String[]> getAvatarMeshList() {
        return m_meshes;
    }

    /** Mutators */
    public void setSceneData(SceneEssentials sEss) {
        m_sceneData = sEss;
    }

    public void setAvatarMeshList(Map<Integer, String[]> meshes) {
        m_meshes = meshes;
    }
////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - END
////////////////////////////////////////////////////////////////////////////////

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton_Male = new javax.swing.JButton();
        jButton_ModelViewer = new javax.swing.JButton();
        jButton_Female = new javax.swing.JButton();
        jTabbedPane_Options = new javax.swing.JTabbedPane();
        jPanel_MainBody = new javax.swing.JPanel();
        jLabel_Head = new javax.swing.JLabel();
        jLabel_CurrHead = new javax.swing.JLabel();
        jButton_ApplyHead = new javax.swing.JButton();
        jScrollPane_Heads = new javax.swing.JScrollPane();
        jList_Heads = new javax.swing.JList();
        jLabel_UpperBody = new javax.swing.JLabel();
        jLabel_CurrUpperBody = new javax.swing.JLabel();
        jButton_ApplyBody = new javax.swing.JButton();
        jScrollPane_UpperBody = new javax.swing.JScrollPane();
        jList_UpperBody = new javax.swing.JList();
        jLabel_LowerBody = new javax.swing.JLabel();
        jLabel_CurrLowerBody = new javax.swing.JLabel();
        jButton_ApplyLegs = new javax.swing.JButton();
        jScrollPane_LowerBody = new javax.swing.JScrollPane();
        jList_LowerBody = new javax.swing.JList();
        jLabel_Shoes = new javax.swing.JLabel();
        jLabel_CurrShoes = new javax.swing.JLabel();
        jButton_ApplyShoes = new javax.swing.JButton();
        jScrollPane_Shoes = new javax.swing.JScrollPane();
        jList_Shoes = new javax.swing.JList();
        jButton_ApplyAllBody = new javax.swing.JButton();
        jPanel_MainHead = new javax.swing.JPanel();
        jLabel_Head1 = new javax.swing.JLabel();
        jLabel_CurrHead1 = new javax.swing.JLabel();
        jButton_ApplyHead1 = new javax.swing.JButton();
        jScrollPane_Heads1 = new javax.swing.JScrollPane();
        jList_Heads1 = new javax.swing.JList();
        jLabel_Hair = new javax.swing.JLabel();
        jLabel_CurrHair = new javax.swing.JLabel();
        jButton_ApplyHair = new javax.swing.JButton();
        jButton_ColorH = new javax.swing.JButton();
        jLabel_CurrColorH = new javax.swing.JLabel();
        jScrollPane_Hair = new javax.swing.JScrollPane();
        jList_Hair = new javax.swing.JList();
        jLabel_FacialHair = new javax.swing.JLabel();
        jLabel_CurrFacialHair = new javax.swing.JLabel();
        jButton_ApplyFacialHair = new javax.swing.JButton();
        jScrollPane_FacialHair = new javax.swing.JScrollPane();
        jList_FacialHair = new javax.swing.JList();
        jButton_ColorFH = new javax.swing.JButton();
        jLabel_CurrColorFH = new javax.swing.JLabel();
        jLabel_SkinTone = new javax.swing.JLabel();
        jButton_ColorST = new javax.swing.JButton();
        jLabel_CurrColorST = new javax.swing.JLabel();
        jButton_ApplyAllHead = new javax.swing.JButton();
        jPanel_MainAcc = new javax.swing.JPanel();
        jLabel_Hats = new javax.swing.JLabel();
        jLabel_CurrHat = new javax.swing.JLabel();
        jButton_ApplyHat = new javax.swing.JButton();
        jScrollPane_Hats = new javax.swing.JScrollPane();
        jList_Hats = new javax.swing.JList();
        jLabel_Specs = new javax.swing.JLabel();
        jLabel_CurrSpecs = new javax.swing.JLabel();
        jButton_ApplySpecs = new javax.swing.JButton();
        jScrollPane_Specs = new javax.swing.JScrollPane();
        jList_Specs = new javax.swing.JList();
        jButton_ApplyAllAcc = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(320, 2222222));
        setMinimumSize(new java.awt.Dimension(320, 480));
        setPreferredSize(new java.awt.Dimension(320, 480));
        setLayout(new java.awt.GridBagLayout());

        jButton_Male.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maleAvatarMode();
            }
        });
        jButton_Male.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jButton_Male.setText("Male Avatar");
        jButton_Male.setMaximumSize(new java.awt.Dimension(100, 25));
        jButton_Male.setMinimumSize(new java.awt.Dimension(100, 25));
        jButton_Male.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton_Male, gridBagConstraints);

        jButton_ModelViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMode();
            }
        });
        jButton_ModelViewer.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jButton_ModelViewer.setText("Model View");
        jButton_ModelViewer.setMaximumSize(new java.awt.Dimension(100, 25));
        jButton_ModelViewer.setMinimumSize(new java.awt.Dimension(100, 25));
        jButton_ModelViewer.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton_ModelViewer, gridBagConstraints);

        jButton_Female.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                femaleAvatarMode();
            }
        });
        jButton_Female.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jButton_Female.setText("Female Avatar");
        jButton_Female.setMaximumSize(new java.awt.Dimension(100, 25));
        jButton_Female.setMinimumSize(new java.awt.Dimension(100, 25));
        jButton_Female.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButton_Female, gridBagConstraints);

        jTabbedPane_Options.setMaximumSize(new java.awt.Dimension(320, 32767));
        jTabbedPane_Options.setPreferredSize(new java.awt.Dimension(320, 450));

        jPanel_MainBody.setMaximumSize(new java.awt.Dimension(320, 450));
        jPanel_MainBody.setPreferredSize(new java.awt.Dimension(320, 450));
        jPanel_MainBody.setLayout(new java.awt.GridBagLayout());

        jLabel_Head.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_Head.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Head.setText("Head");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainBody.add(jLabel_Head, gridBagConstraints);

        jLabel_CurrHead.setText("Default");
        jLabel_CurrHead.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrHead.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrHead.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jLabel_CurrHead, gridBagConstraints);

        jButton_ApplyHead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadHead(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyHead.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyHead.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel_MainBody.add(jButton_ApplyHead, gridBagConstraints);

        jScrollPane_Heads.setPreferredSize(new java.awt.Dimension(200, 55));

        jList_Heads.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Please choose Male Avatar, Female Avatar or Model View" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Heads.setViewportView(jList_Heads);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jScrollPane_Heads, gridBagConstraints);

        jLabel_UpperBody.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_UpperBody.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_UpperBody.setText("Upper Body");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainBody.add(jLabel_UpperBody, gridBagConstraints);

        jLabel_CurrUpperBody.setText("Default");
        jLabel_CurrUpperBody.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrUpperBody.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrUpperBody.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jLabel_CurrUpperBody, gridBagConstraints);

        jButton_ApplyBody.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadUpperBody(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyBody.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyBody.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel_MainBody.add(jButton_ApplyBody, gridBagConstraints);

        jScrollPane_UpperBody.setPreferredSize(new java.awt.Dimension(200, 55));

        jList_UpperBody.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_UpperBody.setViewportView(jList_UpperBody);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jScrollPane_UpperBody, gridBagConstraints);

        jLabel_LowerBody.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_LowerBody.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_LowerBody.setText("Lower Body");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainBody.add(jLabel_LowerBody, gridBagConstraints);

        jLabel_CurrLowerBody.setText("Default");
        jLabel_CurrLowerBody.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrLowerBody.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrLowerBody.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jLabel_CurrLowerBody, gridBagConstraints);

        jButton_ApplyLegs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadLowerBody(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyLegs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyLegs.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel_MainBody.add(jButton_ApplyLegs, gridBagConstraints);

        jScrollPane_LowerBody.setPreferredSize(new java.awt.Dimension(200, 55));

        jList_LowerBody.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_LowerBody.setViewportView(jList_LowerBody);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jScrollPane_LowerBody, gridBagConstraints);

        jLabel_Shoes.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_Shoes.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Shoes.setText("Shoes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainBody.add(jLabel_Shoes, gridBagConstraints);

        jLabel_CurrShoes.setText("Default");
        jLabel_CurrShoes.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrShoes.setMaximumSize(new java.awt.Dimension(45, 25));
        jLabel_CurrShoes.setPreferredSize(new java.awt.Dimension(45, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jLabel_CurrShoes, gridBagConstraints);

        jButton_ApplyShoes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadShoes(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyShoes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyShoes.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel_MainBody.add(jButton_ApplyShoes, gridBagConstraints);

        jScrollPane_Shoes.setPreferredSize(new java.awt.Dimension(200, 55));

        jList_Shoes.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Shoes.setViewportView(jList_Shoes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainBody.add(jScrollPane_Shoes, gridBagConstraints);

        jButton_ApplyAllBody.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyAllBody(m_isViewMode);
            }
        });
        jButton_ApplyAllBody.setText("Apply All");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel_MainBody.add(jButton_ApplyAllBody, gridBagConstraints);

        jPanel_MainBody.setVisible(false);

        jTabbedPane_Options.addTab("Body", jPanel_MainBody);

        jPanel_MainHead.setLayout(new java.awt.GridBagLayout());

        jLabel_Head1.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_Head1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Head1.setText("Head");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jLabel_Head1, gridBagConstraints);

        jLabel_CurrHead1.setText("Default");
        jLabel_CurrHead1.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrHead1.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrHead1.setMinimumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrHead1.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 5, 0);
        jPanel_MainHead.add(jLabel_CurrHead1, gridBagConstraints);

        jButton_ApplyHead1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadHead1(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyHead1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyHead1.setMinimumSize(new java.awt.Dimension(55, 55));
        jButton_ApplyHead1.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel_MainHead.add(jButton_ApplyHead1, gridBagConstraints);

        jScrollPane_Heads1.setMinimumSize(new java.awt.Dimension(230, 55));
        jScrollPane_Heads1.setPreferredSize(new java.awt.Dimension(230, 55));

        jList_Heads1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Heads1.setViewportView(jList_Heads1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainHead.add(jScrollPane_Heads1, gridBagConstraints);

        jLabel_Hair.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_Hair.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Hair.setText("Hair");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jLabel_Hair, gridBagConstraints);

        jLabel_CurrHair.setText("Default");
        jLabel_CurrHair.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrHair.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrHair.setMinimumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrHair.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainHead.add(jLabel_CurrHair, gridBagConstraints);

        jButton_ApplyHair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadHair(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyHair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyHair.setMinimumSize(new java.awt.Dimension(55, 55));
        jButton_ApplyHair.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainHead.add(jButton_ApplyHair, gridBagConstraints);

        jButton_ColorH.setText("Select Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainHead.add(jButton_ColorH, gridBagConstraints);

        jLabel_CurrColorH.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel_CurrColorH.setPreferredSize(new java.awt.Dimension(25, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainHead.add(jLabel_CurrColorH, gridBagConstraints);

        jScrollPane_Hair.setMinimumSize(new java.awt.Dimension(230, 55));
        jScrollPane_Hair.setPreferredSize(new java.awt.Dimension(230, 55));

        jList_Hair.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Hair.setViewportView(jList_Hair);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jScrollPane_Hair, gridBagConstraints);

        jLabel_FacialHair.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_FacialHair.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_FacialHair.setText("Facial Hair");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jLabel_FacialHair, gridBagConstraints);

        jLabel_CurrFacialHair.setText("Default");
        jLabel_CurrFacialHair.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrFacialHair.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrFacialHair.setMinimumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrFacialHair.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainHead.add(jLabel_CurrFacialHair, gridBagConstraints);

        jButton_ApplyFacialHair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadFacialHair(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyFacialHair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyFacialHair.setMinimumSize(new java.awt.Dimension(55, 55));
        jButton_ApplyFacialHair.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel_MainHead.add(jButton_ApplyFacialHair, gridBagConstraints);

        jScrollPane_FacialHair.setMinimumSize(new java.awt.Dimension(230, 55));
        jScrollPane_FacialHair.setPreferredSize(new java.awt.Dimension(230, 55));

        jList_FacialHair.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_FacialHair.setViewportView(jList_FacialHair);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jScrollPane_FacialHair, gridBagConstraints);

        jButton_ColorFH.setText("Select Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jButton_ColorFH, gridBagConstraints);

        jLabel_CurrColorFH.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel_CurrColorFH.setPreferredSize(new java.awt.Dimension(25, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jLabel_CurrColorFH, gridBagConstraints);

        jLabel_SkinTone.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_SkinTone.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_SkinTone.setText("Skin Tone (Body)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jLabel_SkinTone, gridBagConstraints);

        jButton_ColorST.setText("Select Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jButton_ColorST, gridBagConstraints);

        jLabel_CurrColorST.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel_CurrColorST.setPreferredSize(new java.awt.Dimension(25, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainHead.add(jLabel_CurrColorST, gridBagConstraints);

        jButton_ApplyAllHead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyAllHair(m_isViewMode);
            }
        });
        jButton_ApplyAllHead.setText("Apply All");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel_MainHead.add(jButton_ApplyAllHead, gridBagConstraints);

        jPanel_MainHead.setVisible(false);

        jTabbedPane_Options.addTab("Head", jPanel_MainHead);

        jPanel_MainAcc.setLayout(new java.awt.GridBagLayout());

        jLabel_Hats.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_Hats.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Hats.setText("Hats");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainAcc.add(jLabel_Hats, gridBagConstraints);

        jLabel_CurrHat.setText("NONE");
        jLabel_CurrHat.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrHat.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrHat.setMinimumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrHat.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainAcc.add(jLabel_CurrHat, gridBagConstraints);

        jButton_ApplyHat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadHats(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplyHat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplyHat.setMinimumSize(new java.awt.Dimension(55, 55));
        jButton_ApplyHat.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel_MainAcc.add(jButton_ApplyHat, gridBagConstraints);

        jScrollPane_Hats.setMinimumSize(new java.awt.Dimension(230, 55));
        jScrollPane_Hats.setPreferredSize(new java.awt.Dimension(230, 55));

        jList_Hats.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Hats.setViewportView(jList_Hats);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainAcc.add(jScrollPane_Hats, gridBagConstraints);

        jLabel_Specs.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel_Specs.setForeground(new java.awt.Color(0, 0, 255));
        jLabel_Specs.setText("Specs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainAcc.add(jLabel_Specs, gridBagConstraints);

        jLabel_CurrSpecs.setText("NONE");
        jLabel_CurrSpecs.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jLabel_CurrSpecs.setMaximumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrSpecs.setMinimumSize(new java.awt.Dimension(50, 25));
        jLabel_CurrSpecs.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainAcc.add(jLabel_CurrSpecs, gridBagConstraints);

        jButton_ApplySpecs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                loadSpecs(m_isViewMode);
                setCursor(null);
            }
        });
        jButton_ApplySpecs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imi/icons/Apply.png"))); // NOI18N
        jButton_ApplySpecs.setMinimumSize(new java.awt.Dimension(55, 55));
        jButton_ApplySpecs.setPreferredSize(new java.awt.Dimension(55, 55));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel_MainAcc.add(jButton_ApplySpecs, gridBagConstraints);

        jScrollPane_Specs.setMinimumSize(new java.awt.Dimension(230, 55));
        jScrollPane_Specs.setPreferredSize(new java.awt.Dimension(230, 55));

        jList_Specs.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "N/A" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Specs.setViewportView(jList_Specs);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel_MainAcc.add(jScrollPane_Specs, gridBagConstraints);

        jButton_ApplyAllAcc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyAllAcc(m_isViewMode);
            }
        });
        jButton_ApplyAllAcc.setText("Apply All");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel_MainAcc.add(jButton_ApplyAllAcc, gridBagConstraints);

        jPanel_MainAcc.setVisible(false);

        jTabbedPane_Options.addTab("Acc", jPanel_MainAcc);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(jTabbedPane_Options, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_ApplyAllAcc;
    private javax.swing.JButton jButton_ApplyAllBody;
    private javax.swing.JButton jButton_ApplyAllHead;
    private javax.swing.JButton jButton_ApplyBody;
    private javax.swing.JButton jButton_ApplyFacialHair;
    private javax.swing.JButton jButton_ApplyHair;
    private javax.swing.JButton jButton_ApplyHat;
    private javax.swing.JButton jButton_ApplyHead;
    private javax.swing.JButton jButton_ApplyHead1;
    private javax.swing.JButton jButton_ApplyLegs;
    private javax.swing.JButton jButton_ApplyShoes;
    private javax.swing.JButton jButton_ApplySpecs;
    private javax.swing.JButton jButton_ColorFH;
    private javax.swing.JButton jButton_ColorH;
    private javax.swing.JButton jButton_ColorST;
    private javax.swing.JButton jButton_Female;
    private javax.swing.JButton jButton_Male;
    private javax.swing.JButton jButton_ModelViewer;
    private javax.swing.JLabel jLabel_CurrColorFH;
    private javax.swing.JLabel jLabel_CurrColorH;
    private javax.swing.JLabel jLabel_CurrColorST;
    private javax.swing.JLabel jLabel_CurrFacialHair;
    private javax.swing.JLabel jLabel_CurrHair;
    private javax.swing.JLabel jLabel_CurrHat;
    private javax.swing.JLabel jLabel_CurrHead;
    private javax.swing.JLabel jLabel_CurrHead1;
    private javax.swing.JLabel jLabel_CurrLowerBody;
    private javax.swing.JLabel jLabel_CurrShoes;
    private javax.swing.JLabel jLabel_CurrSpecs;
    private javax.swing.JLabel jLabel_CurrUpperBody;
    private javax.swing.JLabel jLabel_FacialHair;
    private javax.swing.JLabel jLabel_Hair;
    private javax.swing.JLabel jLabel_Hats;
    private javax.swing.JLabel jLabel_Head;
    private javax.swing.JLabel jLabel_Head1;
    private javax.swing.JLabel jLabel_LowerBody;
    private javax.swing.JLabel jLabel_Shoes;
    private javax.swing.JLabel jLabel_SkinTone;
    private javax.swing.JLabel jLabel_Specs;
    private javax.swing.JLabel jLabel_UpperBody;
    private javax.swing.JList jList_FacialHair;
    private javax.swing.JList jList_Hair;
    private javax.swing.JList jList_Hats;
    private javax.swing.JList jList_Heads;
    private javax.swing.JList jList_Heads1;
    private javax.swing.JList jList_LowerBody;
    private javax.swing.JList jList_Shoes;
    private javax.swing.JList jList_Specs;
    private javax.swing.JList jList_UpperBody;
    private javax.swing.JPanel jPanel_MainAcc;
    private javax.swing.JPanel jPanel_MainBody;
    private javax.swing.JPanel jPanel_MainHead;
    private javax.swing.JScrollPane jScrollPane_FacialHair;
    private javax.swing.JScrollPane jScrollPane_Hair;
    private javax.swing.JScrollPane jScrollPane_Hats;
    private javax.swing.JScrollPane jScrollPane_Heads;
    private javax.swing.JScrollPane jScrollPane_Heads1;
    private javax.swing.JScrollPane jScrollPane_LowerBody;
    private javax.swing.JScrollPane jScrollPane_Shoes;
    private javax.swing.JScrollPane jScrollPane_Specs;
    private javax.swing.JScrollPane jScrollPane_UpperBody;
    private javax.swing.JTabbedPane jTabbedPane_Options;
    // End of variables declaration//GEN-END:variables
////////////////////////////////////////////////////////////////////////////////
// Helper Functions - Begin
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Helper Functions - End
////////////////////////////////////////////////////////////////////////////////
}
