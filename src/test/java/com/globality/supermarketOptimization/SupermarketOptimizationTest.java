package com.globality.supermarketOptimization;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupermarketOptimizationTest {


    public static final String TEST_FILE_PATH = "retail_10.dat";
    public static final String COMPLETE_TEST_FILE_PATH = "retail_25k.dat";
    public static final String STORAGE_FOLDER_PATH = "/mnt/da7f9961-d147-4d0b-82c5-9e3594ec7170/tmp/2";
//    public static final String OUTPUT_FILE_PATH = "/tmp/output.txt";
    public static final String OUTPUT_FILE_PATH = null;

    @Test
    void testThatICanFindCombinationsOnSmallFile() throws Exception {
        //given class
        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();

        //when I execute function
        String filePath = Paths.get(getClass().getClassLoader().getResource(TEST_FILE_PATH).getFile()).toAbsolutePath().toString();
        String output = supermarketOptimization.analyseFile(filePath,2, STORAGE_FOLDER_PATH, OUTPUT_FILE_PATH);

        // then I get expected value
        File file = new File(getClass().getClassLoader().getResource(TEST_FILE_PATH).getFile());
        System.out.println(output);
        assertEquals(  "Item size, Nb occurences, values\n" +
                "3, 2, 36 38 39\n" +
                "3, 3, 38 39 48",output);
    }

//    @Test
    void testThatICanFindCombinationsOnCompleteFile() throws Exception {
        //given class
        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();

        //when I execute function
        String filePath = Paths.get(getClass().getClassLoader().getResource(COMPLETE_TEST_FILE_PATH).getFile()).toAbsolutePath().toString();
        String output = supermarketOptimization.analyseFile(filePath,4, STORAGE_FOLDER_PATH, OUTPUT_FILE_PATH);

        // then I get expected value
        File file = new File(getClass().getClassLoader().getResource(COMPLETE_TEST_FILE_PATH).getFile());
        System.out.println(output);
    }

}