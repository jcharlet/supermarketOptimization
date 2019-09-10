package com.globality.supermarketOptimization;

import org.paukov.combinatorics3.Generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SupermarketOptimization {
    public static final int MIN_NUMBER_OF_IDS_PER_LINE = 3;
    private ConcurrentHashMap<String,Integer> mapOfProducts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Integer> mapOfCombinations = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        String sigma = args[1];

        System.out.print(new SupermarketOptimization().analyseFile(fileName, Integer.valueOf(sigma)));
    }

    public String analyseFile(String fileName, Integer sigma) throws IOException{
        try(Stream<String> linesStream = Files.lines(new File(fileName).toPath())){
            List<String> linesFilteredOnNumberOfElements = filterLinesByNumberOfIdsPerLine(linesStream, MIN_NUMBER_OF_IDS_PER_LINE);

            populateProductsCountMap(linesFilteredOnNumberOfElements.stream());

            List<String> linesWithIdsFilteredByTotalCount = filterIdsWhichOccurLessThanSigma(linesFilteredOnNumberOfElements.stream(), sigma);

            List<String> linesPrepared = filterLinesByNumberOfIdsPerLine(linesWithIdsFilteredByTotalCount.stream(), MIN_NUMBER_OF_IDS_PER_LINE);

            populateCombinationsMap(linesPrepared.stream());

            return createReportOfCombinations(sigma);
        }
    }

    private void log(String s) {
        System.out.println(Calendar.getInstance().getTime().toString() + " " + s );
    }

    private String createReportOfCombinations(Integer sigma) {
        log(">populateCombinationsMap");
        return "Item size, Nb occurences, values" + System.lineSeparator() + mapOfCombinations.entrySet().stream()
//                .sorted(Comparator.comparing()
                .filter(combination -> combination.getValue()>=sigma)

                .map(entry -> {
                    return entry.getKey().toString().split(" ").length + ", " + entry.getValue() + ", " +entry.getKey() ;
                })
                .collect(Collectors.joining( System.lineSeparator()));
    }

    private void populateCombinationsMap(Stream<String> linesStream) {
        log(">populateCombinationsMap");
        linesStream.forEach(line -> {
            String[] ids = line.split(" ");
            Generator.combination(ids)
                    .simple(3)
                    .forEach(combination -> {
                        mapOfCombinations.compute(String.join(" ", combination),
                                (stringCombination, count) -> count == null ? 1 : count + 1);
                    });
        });
    }

    private List<String> filterIdsWhichOccurLessThanSigma(Stream<String> linesStream, Integer sigma) {
        log(">filterIdsWhichOccurLessThanSigma");
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
        log(">filterLinesByNumberOfIdsPerLine");
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

    private void populateProductsCountMap(Stream<String> linesStream) {
        log(">populateProductsCountMap");
        linesStream
            .forEach(line -> {
                String[] products = line.split(" ");
                Stream.of(products).forEach(id -> {
                    mapOfProducts.compute(id, (idKey, count) -> count == null ? 1 : count + 1);
                });
            });
    }

}
