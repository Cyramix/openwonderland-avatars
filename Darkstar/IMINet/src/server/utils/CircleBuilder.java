/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.utils;

/**
 *
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 */
public class CircleBuilder 
{
        private int     m_points = 0;
        private float   m_radius = 0.0f;
        Vector3 []      m_circle = null;

        public CircleBuilder(int points, float radius)
        {
            m_points = points;
            m_radius = radius;
        }

        public Vector3 [] calculatePoints()
        {
            m_circle = new Vector3 [m_points];

            double angleStep = Math.PI * 2 / m_points;
            double theta = 0.0f;

            for (int i = 0; i < m_points; i++)
            {
                m_circle[i] = new Vector3();
                m_circle[i].x = (float)Math.cos(theta) * -1.0f * m_radius;
                m_circle[i].z = (float)Math.sin(theta) * m_radius;

                theta += angleStep;
            }

            return m_circle;
        }

        public Vector3 [] getCircleRef()
        {
            return m_circle;
        }

        public Vector3 getPoint(int index)
        {
            if (m_circle != null)
                return m_circle[index];
            return null;
        }
}
