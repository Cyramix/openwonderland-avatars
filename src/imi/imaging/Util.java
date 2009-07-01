package imi.imaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Util {

    /**
     * Returns a copy of the object, or null if the object cannot be serialized.
     * @param orig Object to copy
     * @return Object copy of Object
     */
    public static Object copy(Object orig) {
        Object obj = null;

        try {
            // serialize
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // deserialize
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }

        return obj;
    }

    /**
     * Returns all powers of 2 dimensions
     * @param depth how many numbers to calculate
     * @return results int[] values
     */
    public static int[] powersOfTwo(int depth) {
        int two = 2;
        int[] values = new int[depth];
        for (int i = 0; i < depth; i++) {
            int number = 2;
            for (int j = 0; j < i; j++) {
                number *= two;
            }
            values[i] = number;
        }
        return values;
    }

    public static double toPercent(double range, double value) {
        return value / range;
    }

    public static void printClassInfo(Object obj) {
        System.out.println(obj);
        System.out.println(obj.getClass());
        System.out.println(obj.getClass().getName());
        System.out.println(obj.getClass().getSimpleName());
        System.out.println(obj.getClass().getCanonicalName());
        System.out.println(obj.getClass().getClassLoader());
    }

}
