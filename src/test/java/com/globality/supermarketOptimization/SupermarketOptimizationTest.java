package com.globality.supermarketOptimization;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SupermarketOptimizationTest {


    public static final String TEST_FILE_PATH = "retail_10.dat";
    public static final String COMPLETE_TEST_FILE_PATH = "retail_25k.dat";

    @Test
    void testThatICanFilterTheFile() throws Exception {
        //given class
        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();

        //when I execute function
        String filePath = Paths.get(getClass().getClassLoader().getResource(TEST_FILE_PATH).getFile()).toAbsolutePath().toString();
        String output = supermarketOptimization.analyseFile(filePath,3);

        // then I get expected value
        File file = new File(getClass().getClassLoader().getResource(TEST_FILE_PATH).getFile());
        assertEquals( "38 39 48\n" +
                "38 39 48",output);
    }

//    @Test
//    void testThatICanProcessTheCompleteFile() throws Exception {
//        //given class
//        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();
//
//        //when I execute function
//        String filePath = Paths.get(getClass().getClassLoader().getResource(COMPLETE_TEST_FILE_PATH).getFile()).toAbsolutePath().toString();
//        String output = supermarketOptimization.analyseFile(filePath,3);
//
//        // then I get expected value
//        File file = new File(getClass().getClassLoader().getResource(COMPLETE_TEST_FILE_PATH).getFile());
//        assertEquals( new String (Files.readAllBytes(file.toPath())),output);
//    }



}