package edu.marist.jointstudy.essence.api.parse;

/**
 * How to turn a T into a String
 * @param <T>
 */
@FunctionalInterface
public interface Serialization<T> {

    String serialize(T t);

}
