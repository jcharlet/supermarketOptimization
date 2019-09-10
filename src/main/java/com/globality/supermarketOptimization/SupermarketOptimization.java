package com.globality.supermarketOptimization;

import com.globality.supermarketOptimization.domain.CombinationInfo;
import com.globality.supermarketOptimization.storage.CombinationsStore;
import org.paukov.combinatorics3.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SupermarketOptimization {

    final private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int MIN_NUMBER_OF_IDS_PER_LINE = 3;
    private ConcurrentHashMap<String,Integer> mapOfProducts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CombinationInfo> mapOfCombinations = new ConcurrentHashMap<>();
    private CombinationsStore combinationsCache;

    /**
     * Command Line Interface to run the application on a transaction database
     * @param args: filePath, sigma, storageFolderPath, outputFilepath
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if(args.length!=4){
            System.out.println("To run this command line interface, please provide all the following arguments:\n"
            +"java -jar java-interview-exercises-1.0-SNAPSHOT.jar <transactions database file> <sigma> <parent directory for store> <output filepath>\n"
            +"e.g. java -Xmx4g -jar java-interview-exercises-1.0-SNAPSHOT.jar retail_25k.dat 4 \"/tmp/globalityCache\" \"/tmp/output.txt\"\n");
            System.exit(1);
        }
        String filePath = args[0];
        String sigma = args[1];
        String storageFolderPath = args[2];
        String outputFilepath = args[3];

        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();
        supermarketOptimization.analyseFile(filePath, Integer.valueOf(sigma), storageFolderPath, outputFilepath);

        supermarketOptimization.combinationsCache.close();
        System.exit(0);
    }

    /**
     * Main method to process a file and create a report on its combinations
     * @param fileName
     * @param sigma
     * @param storageFolderPath
     * @param outputFilepath
     * @return
     * @throws IOException
     */
    public String analyseFile(String fileName, Integer sigma, String storageFolderPath, String outputFilepath) throws IOException{
        combinationsCache = new CombinationsStore(storageFolderPath);
        try(Stream<String> linesStream = Files.lines(new File(fileName).toPath())){
            List<String> linesPrepared = cleanDataset(sigma, linesStream);

            findAndStoreAllRecurringCombinations(linesPrepared);

            return createReportOfCombinations(sigma, outputFilepath);
        }
    }

    private List<String> cleanDataset(Integer sigma, Stream<String> linesStream) {
        List<String> linesFilteredOnNumberOfElements = filterLinesByNumberOfIdsPerLine(linesStream, MIN_NUMBER_OF_IDS_PER_LINE);

        countProductIdsOccurrences(linesFilteredOnNumberOfElements.stream());

        List<String> linesWithIdsFilteredByTotalCount = filterProductIdsWhichOccurLessThanSigma(linesFilteredOnNumberOfElements.stream(), sigma);

        return filterLinesByNumberOfIdsPerLine(linesWithIdsFilteredByTotalCount.stream(), MIN_NUMBER_OF_IDS_PER_LINE);
    }

    /**
     * to create a report of combinations <br/>
     * outputFilepath required for large files, otherwise creates out of memory error
     * @param sigma
     * @param outputFilepath report file path. if null log the combinations found
     * @return the report if outputFilePath is null, otherwise return nothing
     * @throws IOException
     */
    private String createReportOfCombinations(Integer sigma, String outputFilepath) throws IOException {
        logger.info(">createReportOfCombinations");

        StringBuilder stringBuilder = new StringBuilder();

        String headerLine = "Item size, Nb occurences, values";
        if(outputFilepath==null) {
            logger.info(headerLine);
            stringBuilder.append(headerLine);
        }else{
            Files.write(Paths.get(outputFilepath),headerLine.getBytes());
        }

        combinationsCache.getIterator().forEachRemaining(
            combination -> {
                if(combination.getValue().count<sigma){
                    return;
                }

                String line = combination.getKey().split(" ").length + ", " + combination.getValue() + ", " + combination.getKey();

                if(outputFilepath==null) {
                    logger.info(line);
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(line);
                }else{
                    try {
                        Files.write(Paths.get(outputFilepath),(System.lineSeparator()+line).getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        logger.error("writing to file failed");
                    }
                }

            }
        );
        logger.info("the combinations can be found in the output file you provided: " + outputFilepath);
        return stringBuilder.toString();
    }

    private List<Object> findAndStoreAllRecurringCombinations(List<String> lines) {
        logger.info(">populateCombinationsMap");
//        for (int i = 0; i < 500; i++) {
        for (int i = 0; i < lines.size(); i++) {
            if(i%100==0){
                logger.info("index: " + i + " , combinations cache put operations: " + combinationsCache.putOperations());
            }
            String line1 = lines.get(i);
            int indexLine1 = i;
            AtomicInteger atomicInteger = new AtomicInteger(0);
            IntStream.range(0, lines.size())
                    .filter(indexLine2 -> indexLine2 != indexLine1)
                    .mapToObj(indexLine2 -> {
                        String line2 = lines.get(indexLine2);
                        List<String> line1Ids = Arrays.asList(line1.split(" "));
                        List<String> line2Ids = Arrays.asList(line2.split(" "));
                        List<String> commonCombinations = line1Ids.stream().filter(line2Ids::contains).collect(Collectors.toList());
                        for (int k = 3; k <= commonCombinations.size(); k++) {
                            //        for (int i = 3; i <4 ; i++) {
                            Generator.combination(commonCombinations)
                                    .simple(k)
                                    .forEach(combination -> {
                                        combinationsCache.addIfMissing(String.join(" ", combination),indexLine2);
                                    });
                        }
                        return null;
                    })
                    .count(); //this .count() method is unused and only serves to execute the processing of the stream
        }
        return null;
    }

    private List<String> filterProductIdsWhichOccurLessThanSigma(Stream<String> linesStream, Integer sigma) {
        logger.info(">filterIdsWhichOccurLessThanSigma");
        return linesStream
                .map(line -> {
                    List<String> ids = Stream.of(line.split(" "))
                            .filter(id -> id != null && mapOfProducts.get(id) != null && mapOfProducts.get(id) >= sigma)
                            .collect(Collectors.toList());
                    if(ids.size()==0){
                        return null;
                    }
                    return ids.stream().collect(Collectors.joining(" "));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> filterLinesByNumberOfIdsPerLine(Stream<String> linesStream, int minNumberOfIdsPerLine) {
        logger.info(">filterLinesByNumberOfIdsPerLine");
        return linesStream
                .map(line -> {
                    String[] products = line.split(" ");
                    if(products.length< minNumberOfIdsPerLine){
                        return null;
                    }
                    return line;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void countProductIdsOccurrences(Stream<String> linesStream) {
        logger.info(">countProductIdsOccurrences");
        linesStream
            .forEach(line -> {
                String[] products = line.split(" ");
                Stream.of(products).forEach(id -> {
                    mapOfProducts.compute(id, (idKey, count) -> count == null ? 1 : count + 1);
                });
            });
    }

}
