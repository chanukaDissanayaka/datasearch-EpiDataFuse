package io.datasearch.epidatafuse.core.dengdipipeline.fuseengine;

import io.datasearch.epidatafuse.core.dengdipipeline.datastore.PipelineDataStore;
import io.datasearch.epidatafuse.core.dengdipipeline.models.configmodels.AggregationConfig;
import io.datasearch.epidatafuse.core.dengdipipeline.models.configmodels.GranularityRelationConfig;
import io.datasearch.epidatafuse.core.dengdipipeline.models.datamodels.SpatioTemporallyAggregatedCollection;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularityrelationmap.GranularityMap;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularityrelationmap.SpatialGranularityRelationMap;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularityrelationmap.TemporalGranularityMap;

import org.geotools.data.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;

/**
 * For data fusion.
 */
public class FuseEngine {

    private static final Logger logger = LoggerFactory.getLogger(FuseEngine.class);
    //aggregating
    private DataFrameBuilder dataFrameBuilder;
    //granularityConvertor
    private GranularityConvertor granularityConvertor;
    //granularityRelationMapper
    private GranularityRelationMapper granularityRelationMapper;

    private PipelineDataStore pipelineDataStore;

    private Scheduler scheduler;

    private HashMap<String, GranularityRelationConfig> granularityRelationConfigs;
    private HashMap<String, AggregationConfig> aggregationConfigs;

    private HashMap<String, GranularityMap> granularityRelationMaps;

    private Timer timer = new Timer();

    public FuseEngine(PipelineDataStore pipelineDataStore,
                      HashMap<String, GranularityRelationConfig> granularityRelationConfigs,
                      HashMap<String, AggregationConfig> aggregationConfigs) {
        this.pipelineDataStore = pipelineDataStore;
        this.granularityRelationMapper = new GranularityRelationMapper(this.pipelineDataStore);
        this.granularityConvertor = new GranularityConvertor(this.pipelineDataStore);
        this.granularityRelationConfigs = granularityRelationConfigs;
        this.aggregationConfigs = aggregationConfigs;
        this.dataFrameBuilder = new DataFrameBuilder();
        this.scheduler = new Scheduler();
        scheduler.setFuseEngine(this);
    }

    public HashMap<String, GranularityMap> setGranularityRelationMaps() {
        HashMap<String, GranularityMap> granularityMaps = this.buildGranularityMap(this.granularityRelationConfigs);
        this.granularityRelationMaps = granularityMaps;
        return granularityMaps;
    }


    public HashMap<String, GranularityMap> buildGranularityMap(
            HashMap<String, GranularityRelationConfig> granularityRelationConfigs) {

        HashMap<String, GranularityMap> granularityMaps = new HashMap<String, GranularityMap>();

        granularityRelationConfigs.forEach((featureType, granularityRelationConfig) -> {

            SpatialGranularityRelationMap spatialMap =
                    this.granularityRelationMapper.buildSpatialGranularityMap(granularityRelationConfig);
            TemporalGranularityMap temporalMap =
                    this.granularityRelationMapper.buildTemporalMap(granularityRelationConfig);

            String baseSpatialGranularity = granularityRelationConfig.getSpatialGranularity();
            String baseTemporalGranularity = granularityRelationConfig.getTemporalGranularity();
            String targetSpatialGranularity = granularityRelationConfig.getTargetSpatialGranularity();
            String targetTemporalGranularity = granularityRelationConfig.getTargetTemporalGranularity();

            GranularityMap granularityMap =
                    new GranularityMap(featureType, spatialMap, temporalMap, baseSpatialGranularity,
                            baseTemporalGranularity, targetSpatialGranularity,
                            targetTemporalGranularity);

            granularityMaps.put(featureType, granularityMap);
        });

        return granularityMaps;
    }

    public void invokeAggregationProcess() {
        if ((granularityRelationMaps != null) && granularityRelationMaps.size() > 0) {
            this.granularityRelationMaps.forEach((String featureTypeName, GranularityMap granularityMap) -> {
                AggregationConfig aggregationConfig = this.aggregationConfigs.get(featureTypeName);
                try {
                    this.aggregate(granularityMap, aggregationConfig);
                } catch (IOException e) {
                    e.getMessage();
                }
            });
        } else {
            logger.info("Cannot aggregate. granularity map is empty");
        }
    }

    public void aggregate(GranularityMap granularityMap, AggregationConfig aggregationConfig) throws IOException {
        SpatioTemporallyAggregatedCollection spatioTemporallyAggregatedCollection =
                this.granularityConvertor.aggregate(granularityMap, aggregationConfig);

        this.dataFrameBuilder.buildDataFrame(spatioTemporallyAggregatedCollection);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void scheduleTasks(long period) {
        this.timer.schedule(scheduler, 0, period);
    }
}

