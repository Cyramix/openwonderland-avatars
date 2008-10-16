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
package imi.scene.utils;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModel;
import imi.utils.CircleUtil;


public class PModelUtils 
{

    //  Builds a PolygonModel that contains a Triangle.
    static public PPolygonModel createTriangle(
                                       String modelName,
                                       Vector3f Point1,
                                       Vector3f Point2,
                                       Vector3f Point3,
                                       ColorRGBA Diffuse,
                                       Vector2f TexCoord1,
                                       Vector2f TexCoord2,
                                       Vector2f TexCoord3)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();

            pPolygonMesh = PMeshUtils.createTriangle(modelName,
                                          Point1, Point2, Point3,
                                          Diffuse,
                                          TexCoord1, TexCoord2, TexCoord3);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();


            return pPolygonModel;
    }

    //  Builds a PolygonModel that contains a Quad.
    static public PPolygonModel createQuad(
                                   String modelName,
                                   Vector3f Point1,
                                   Vector3f Point2,
                                   Vector3f Point3,
                                   Vector3f Point4,
                                   ColorRGBA Diffuse,
                                   Vector2f TexCoord1,
                                   Vector2f TexCoord2,
                                   Vector2f TexCoord3,
                                   Vector2f TexCoord4)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();

            pPolygonMesh = PMeshUtils.createQuad(modelName,
                                                  Point1, Point2, Point3, Point4,
                                                  Diffuse,
                                                  TexCoord1, TexCoord2, TexCoord3, TexCoord4);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();
            
            return pPolygonModel;
    }


    //  Builds a PolygonModel that contains a Grid.
    static public PPolygonModel createGrid(
                                   String modelName,
                                   Vector3f Center,
                                   float fWidth,
                                   float fDepth,
                                   int NumberOfDivisionsAlongWidth,
                                   int NumberOfDivisionsAlongDepth,
                                   ColorRGBA Diffuse,
                                   Vector2f MinTexCoord,
                                   Vector2f MaxTexCoord)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();

            pPolygonMesh = PMeshUtils.createGrid(modelName,
                                                  Center,
                                                  fWidth,
                                                  fDepth,
                                                  NumberOfDivisionsAlongWidth,
                                                  NumberOfDivisionsAlongDepth,
                                                  Diffuse,
                                                  MinTexCoord,
                                                  MaxTexCoord);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();


            return pPolygonModel;
    }

    static public PPolygonModel createTable(
                                                String name,
                                                float fTableRadius,
                                                float fTableHeight)
    {
        PPolygonModel result = new PPolygonModel(name);
        // create the table
        PPolygonMesh tableMesh = PMeshUtils.createTable("Table", fTableRadius, fTableHeight);
        result.addChild(tableMesh);
        return result;
    }

    static public PPolygonModel createChairsAroundATable(
                                                String name,
                                                float fTableRadius,
                                                float fDistanceFromTable,
                                                float fStoolRadius,
                                                float fStoolHeight,
                                                int   numStools)
    {
        PPolygonModel result = new PPolygonModel(name);
        
        // create the chairs using circle utils
        CircleUtil cu = new CircleUtil(numStools, fTableRadius + fDistanceFromTable);
        Vector2f[] vPositions = cu.calculatePoints();
        
        for (int i = 0; i < vPositions.length; ++i)
        {
            PPolygonMesh stool = PMeshUtils.createTable("Stool" + i, fStoolRadius, fStoolHeight);
            stool.getTransform().getLocalMatrix(true).set(new PMatrix(new Vector3f(vPositions[i].x, 0.0f, vPositions[i].y)));
            result.addChild(stool);
        }
        return result;
    }
    
    //  Builds a PolygonModel that contains a Box.
    static public PPolygonModel createBox(
                                  String modelName,
                                  Vector3f Center,
                                  float fWidth,
                                  float fHeight,
                                  float fDepth,
                                  ColorRGBA Diffuse)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();

            pPolygonMesh = PMeshUtils.createBox(modelName,
                                                                                     Center,
                                                                                     fWidth,
                                                                                     fHeight,
                                                                                     fDepth,
                                                                                     Diffuse);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();


            return pPolygonModel;
    }


    //  Builds a PolygonModel that contains a Box.
    static public PPolygonModel createCubeBox(
                                      String modelName,
                                      Vector3f Center,
                                      float fWidth,
                                      float fHeight,
                                      float fDepth,
                                      ColorRGBA Diffuse)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();

            pPolygonMesh = PMeshUtils.createCubeBox(modelName,
                                                 Center,
                                                 fWidth,
                                                 fHeight,
                                                 fDepth,
                                                 Diffuse);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();


            return pPolygonModel;
    }


    //  Builds a PolygonModel that contains a Sphere.
    static public PPolygonModel createSphere(
                                     String modelName,
                                     Vector3f Center,
                                     float fRadius,
                                     int NumberOfSlices,
                                     int NumberOfStacks,
                                     ColorRGBA Diffuse)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();

            pPolygonMesh = PMeshUtils.createSphere(modelName,
                                                    Center,
                                                    fRadius,
                                                    NumberOfSlices,
                                                    NumberOfStacks,
                                                    Diffuse);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();


             return pPolygonModel;
    }


    //  Builds a PolygonModel that contains a Cone.
    static public PPolygonModel createCone(
                                           String modelName,
                                           Vector3f Bottom,
                                           float fHeight,
                                           int NumberOfSlices,
                                           int NumberOfStacks,
                                           float fBottomRadius,
                                           boolean bCapBottom,
                                           ColorRGBA Diffuse)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();


            pPolygonMesh = PMeshUtils.createCone(modelName,
                                                                                      Bottom,
                                                                                      fHeight,
                                                                                      NumberOfSlices,
                                                                                      NumberOfStacks,
                                                                                      fBottomRadius,
                                                                                      bCapBottom,
                                                                                      Diffuse);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();


            return pPolygonModel;
    }


    //  Creates an PolygonModel containing a Cylinder.
    static public PPolygonModel createCylinder(
                                       String modelName,
                                       Vector3f Bottom,
                                       float fHeight,
                                       int NumberOfSlices,
                                       int NumberOfStacks,
                                       float fTopRadius,
                                       float fBottomRadius,
                                       boolean bCapTop,
                                       boolean bCapBottom,
                                       ColorRGBA Diffuse)
    {
            PPolygonModel pPolygonModel = new PPolygonModel();
            
            PPolygonMesh	pPolygonMesh	= null;

            //  Set the name of the PolygonModel.
            pPolygonModel.setName(modelName);


            //  Begin the Batch.
            pPolygonModel.beginBatch();


            pPolygonMesh = PMeshUtils.createCylinder(modelName,
                                                      Bottom,
                                                      fHeight,
                                                      NumberOfSlices,
                                                      NumberOfStacks,
                                                      fTopRadius,
                                                      fBottomRadius,
                                                      bCapTop,
                                                      bCapBottom,
                                                      Diffuse);

            pPolygonModel.addChild(pPolygonMesh);

            //  End the Batch.
            pPolygonModel.endBatch();


            return pPolygonModel;
    }
    
    static  public PPolygonModel createStructureWithDoor(String name,
                                                float fWidth,
                                                float fHeight,
                                                float fDepth,
                                                float fDoorHeight,
                                                float fDoorWidth)
    {
        PPolygonModel result = new PPolygonModel(name);
        result.addChild(PMeshUtils.createRoomWithDoor("RoomInterior", Vector3f.ZERO,
                fWidth * 0.99f, fHeight * 0.99f, fDepth * 0.99f, fDoorHeight, fDoorWidth));
        PPolygonMesh outerMesh = PMeshUtils.createRoomWithDoor("RoomExterior", Vector3f.ZERO,
                fWidth, fHeight, fDepth, fDoorHeight, fDoorWidth);
        outerMesh.flipNormals();
        result.addChild(outerMesh);
        return result;
    }
    
    

}
