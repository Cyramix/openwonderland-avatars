/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui;

/**
 *
 * @author ptruong
 */
public class GUI_Enums {
////////////////////////////////////////////////////////////////////////////////
// Enumerations for the skeleton and body
////////////////////////////////////////////////////////////////////////////////
    public  enum m_bodyPart { Left_UpperLeg, Left_LowerLeg, Left_Foot, Left_UpperArm, Left_LowerArm, Left_Hand, Left_Shoulder,
                              Right_UpperLeg, Right_LowerLeg, Right_Foot, Right_UpperArm, Right_LowerArm, Right_Hand, Right_Shoulder,
                              Head, Torso, Neck, Eyes, Lips };

    public enum m_sliderControl {
        lefteyeHPos,            lefteyeSize,            lefteyeVPos,            lefteyeWidth,           righteyeHPos,           righteyeSize,           righteyeVPos,
        righteyeWidth,          lowerlipSize,           upperlipSize,           mouthWidth,             lefthandLength,         lefthandThickness,      leftlowerarmLength,
        leftlowerarmThickness,  leftupperarmLength,     leftupperarmThickness,  righthandLength,        righthandThickness,     rightlowerarmLength,    rightlowerarmThickness,
        rightupperarmLength,    rightupperarmThickness, leftfootLength,         leftfootThickness,      leftlowerlegLength,     leftlowerlegThickness,  leftupperlegLength,
        leftupperlegThickness,  rightfootLength,        rightfootThickness,     rightlowerlegLength,    rightlowerlegThickness, rightupperlegLength,    rightupperlegThickness,
        headDepth,              headHeight,             headWidth,              leftlegLength,          leftlegScale,           rightlegLength,         rightlegScale,
        leftarmLength,          leftarmScale,           rightarmLength,         rightarmScale,
    };
}
