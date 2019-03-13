package edu.marist.jointstudy.essence.api.parse;

/**
 * How to turn a String into a T.
 * @param <T>
 */
@FunctionalInterface
public interface Deserialization<T> {

    T deserialize(String s);

}
