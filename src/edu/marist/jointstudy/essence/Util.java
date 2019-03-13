package edu.marist.jointstudy.essence;

import java.time.Duration;
import java.time.Instant;

/**
 * Static Util methods.
 *
 * Created by Thomas.Magnusson1 on 6/5/2017.
 */
public class Util {

    /**
     * <p>Combines the left and right parameters into a single byte[], left first and then right.
     *
     * @param left
     * @param right
     * @return the left and right parameters into a single byte[], left first and then right.
     */
    public static byte[] combine(byte[] left, byte[] right) {
        int combinedLength = left.length + right.length;
        byte[] concatenated = new byte[combinedLength];

        for(int i = 0; i < combinedLength; i++) {
            if(i < left.length) {
                concatenated[i] = left[i];
            } else {
                concatenated[i] = right[i - left.length];
            }
        }
        return concatenated;
    }

    /**
     * True of the string is a number according to {@code Integer.valueOf}, false otherwise.
     *
     * @param s a potential number as a string
     * @return true if s is a number, false otherwise.
     */
    public static boolean isInt(String s) {
        try {
            Integer.valueOf(s); // throws an exception if s isn't a number
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * True if the string is an {@code int} and [0, 65535], false otherwise.
     *
     * @param s a string that might be a valid port number
     * @return True if the string is an {@code int} and [0, 65535], false otherwise.
     */
    public static boolean isPort(String s) {
        if(isInt(s)) {
            int possiblePort = Integer.parseInt(s);
            return possiblePort >= 0 && possiblePort <= 65535;
        }
        return false;
    }

    /**
     * Logs the time a given runnable takes in milliseconds to System.out. Logs when the action starts and when it
     * ends.
     * @param actionName the name of the runnable to be timed, e.g. "blockchain parsing"
     * @param r the runnable to be timed.
     */
    public static void time(String actionName, Runnable r) {
        System.out.println("Starting " + actionName + ".");
        Instant before = Instant.now();
        r.run();
        Instant after = Instant.now();
        System.out.println("Ended " + actionName + ". Took " + Duration.between(before, after).toMillis() + "ms.");
    }
}
