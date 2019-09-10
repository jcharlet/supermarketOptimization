package com.globality.supermarketOptimization.storage;

import com.globality.supermarketOptimization.domain.CombinationInfo;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.internal.statistics.DefaultStatisticsService;

import java.io.File;
import java.util.*;

/**
 * Storage of the combinations using a key value store: EhCache
 * <br/><br/>
 *
 * Key: combination of product ids separated by space e.g. "38 40 42"
 * <br/>
 * Value:  {@link CombinationInfo}
 */
public class CombinationsStore {

    public static final String CACHE_NAME = "combinationsCache";
    private final DefaultStatisticsService statisticsService;
    private PersistentCacheManager cacheManager;
    private Cache<String, CombinationInfo> combinationsCache;

    /**
     * Instantiate the embedded EhCache store
     * @param storageFolderPath
     */
    public CombinationsStore(String storageFolderPath) {
        statisticsService = new DefaultStatisticsService();
        cacheManager = CacheManagerBuilder
                .newCacheManagerBuilder()
                .using(statisticsService)
                .with(CacheManagerBuilder.persistence(new File(storageFolderPath)))
                .build(true);

        combinationsCache = cacheManager
                .createCache(CACHE_NAME, CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    String.class, CombinationInfo.class, ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(6000, MemoryUnit.MB)
                                .disk(60, MemoryUnit.GB,false)
                ));
    }
    public void close(){
        cacheManager.close();
    }

    /**
     * @return the number of insertions and updates on combinations
     */
    public long putOperations() {
        return statisticsService.getCacheStatistics("combinationsCache").getCachePuts();
    }

    /**
     * add a new combination with its database line number if it has not been included already
     * @param combination
     * @param indexLine
     */
    public void addIfMissing(String combination, int indexLine) {
        synchronized (combination){
            CombinationInfo combinationInfoContainer = combinationsCache.get(combination);
            if(combinationInfoContainer == null){
                combinationInfoContainer = new CombinationInfo();
                combinationInfoContainer.lines=String.valueOf(indexLine);
                combinationInfoContainer.count+=1;
                combinationsCache.put(combination, combinationInfoContainer);
            }else{
                List<String> lines = new ArrayList<>(Arrays.asList(combinationInfoContainer.lines.split(" ")));
                if(!lines.contains(String.valueOf(indexLine))){
                    lines.add(String.valueOf(indexLine));
                    combinationInfoContainer.lines=String.join(" ", lines);
                    combinationInfoContainer.count+=1;
                    combinationsCache.put(combination, combinationInfoContainer);
                }
            }
        }
    }

    /**
     * Iterator to browse all stored combinations
     * @return
     */
    public Spliterator<Cache.Entry<String, CombinationInfo>> getIterator() {
        return combinationsCache.spliterator();
    }
}