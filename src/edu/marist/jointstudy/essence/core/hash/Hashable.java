package edu.marist.jointstudy.essence.core.hash;

/**
 * <p>Defines a class that is able to be hashed.
 *
 * <p>{@code toByteArray()} allows an implementing class to specify which of its instance variables
 * should be included in its hash.
 *
 * <p>Created by Thomas.Magnusson1 on 6/5/2017.
 */
public interface Hashable {

    /**
     * <p>Implementing classes should:
     * <ol>
     *  <li>Decide which instance variables should be part of the class's hash.
     *  <li>Make sure that all those instance variables can be converted into byte[]
     *  <li>Combine all the byte[] of this class's instance variables into one big byte[]
     *  <li>Pay attention to order; it matters, so make sure the order is decisive
     *  (for verification by a hash later, to reproducability, etc).
     * </ol>
     *
     * @return an array of bytes representing the bytes that the implementing class wishes to have hashed.
     */
    byte[] toByteArray();

}
