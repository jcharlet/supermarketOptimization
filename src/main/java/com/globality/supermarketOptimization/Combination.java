package com.globality.supermarketOptimization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Combination implements Serializable {
    Integer count = 0;
    String lines = "";

    @Override
    public String toString() {
        return String.valueOf(count);
    }
}
