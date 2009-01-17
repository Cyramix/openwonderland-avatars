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
                              Head, Torso, Neck, Eyes, Lips, Left_Arm, Right_Arm, Left_Leg, Right_Leg};

    public enum m_sliderControl {
        lefteyeHPos,            lefteyeSize,            lefteyeVPos,            lefteyeWidth,           righteyeHPos,           righteyeSize,           righteyeVPos,
        righteyeWidth,          lowerlipSize,           upperlipSize,           mouthWidth,             lefthandLength,         lefthandThickness,      leftlowerarmLength,
        leftlowerarmThickness,  leftupperarmLength,     leftupperarmThickness,  righthandLength,        righthandThickness,     rightlowerarmLength,    rightlowerarmThickness,
        rightupperarmLength,    rightupperarmThickness, leftfootLength,         leftfootThickness,      leftlowerlegLength,     leftlowerlegThickness,  leftupperlegLength,
        leftupperlegThickness,  rightfootLength,        rightfootThickness,     rightlowerlegLength,    rightlowerlegThickness, rightupperlegLength,    rightupperlegThickness,
        headDepth,              headHeight,             headWidth,              leftlegLength,          leftlegScale,           rightlegLength,         rightlegScale,
        leftarmLength,          leftarmScale,           rightarmLength,         rightarmScale,          uniformHeight,          uniformThickness,       torsoLength,
        torsoThickness,         leftearHPos,            leftearSize,            leftearVPos,            noseHPos,               noseLength,             noseSize,
        noseVPos,               rightearHPos,           rightearSize,           rightearVPos
    };
}
