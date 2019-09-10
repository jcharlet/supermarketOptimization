package com.globality.supermarketOptimization.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Information associated to a combination of product ids
 * <br/><br/>
 * Includes:<br/>
 * <ul>
 *     <li>count: Number of occurrences</li>
 *     <li>lines: List separated by space of the transaction database line numbers</li>
 * </ul>
 */
public class CombinationInfo implements Serializable {
    public Integer count = 0;
    public String lines = "";

    @Override
    public String toString() {
        return String.valueOf(count);
    }
}
