package org.smarthata.service;

import org.smarthata.model.Measure;

import java.util.List;
import java.util.function.BinaryOperator;

class CommonUtils {
    static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    static BinaryOperator<List<Measure>> joinLists() {
        return (a, b) -> {
            a.addAll(b);
            return a;
        };
    }
}
