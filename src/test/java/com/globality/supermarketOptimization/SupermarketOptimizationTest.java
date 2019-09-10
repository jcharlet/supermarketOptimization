package com.globality.supermarketOptimization;

import org.junit.jupiter.api.Test;
import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SupermarketOptimizationTest {


    public static final String TEST_FILE_PATH = "retail_10.dat";
    public static final String COMPLETE_TEST_FILE_PATH = "retail_25k.dat";

    @Test
    void testThatICanFindCombinationsOnSmallFile() throws Exception {
        //given class
        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();

        //when I execute function
        String filePath = Paths.get(getClass().getClassLoader().getResource(TEST_FILE_PATH).getFile()).toAbsolutePath().toString();
        String output = supermarketOptimization.analyseFile(filePath,2);

        // then I get expected value
        File file = new File(getClass().getClassLoader().getResource(TEST_FILE_PATH).getFile());
        System.out.println(output);
        assertEquals(  "Item size, Nb occurences, values\n" +
                "3, 2, 36 38 39\n" +
                "3, 3, 38 39 48",output);
    }

    @Test
    void testThatICanFindCombinationsOnCompleteFile() throws Exception {
        //given class
        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();

        //when I execute function
        String filePath = Paths.get(getClass().getClassLoader().getResource(COMPLETE_TEST_FILE_PATH).getFile()).toAbsolutePath().toString();
        String output = supermarketOptimization.analyseFile(filePath,3);

        // then I get expected value
        File file = new File(getClass().getClassLoader().getResource(COMPLETE_TEST_FILE_PATH).getFile());
        System.out.println(output);
    }

//    private ConcurrentHashMap<String,Integer> mapOfCombinations = new ConcurrentHashMap<>();
    private List<String> mapOfCombinations = new ArrayList<>();


}