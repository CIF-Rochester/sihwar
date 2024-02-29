package com.vicious.sihwar.util;

/**
 * A consumer but with 4 parameters.
 */
@FunctionalInterface
public interface QuadConsumer<A,B,C,D> {
    void accept(A a, B b, C c, D d);
}
