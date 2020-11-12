package io.datasearch.epidatafuse.core.dengdipipeline;

import io.datasearch.epidatafuse.core.dengdipipeline.datastore.PipelineDataStore;
import io.datasearch.epidatafuse.core.dengdipipeline.datastore.schema.SimpleFeatureTypeSchema;
import io.datasearch.epidatafuse.core.dengdipipeline.fuseengine.FuseEngine;
import io.datasearch.epidatafuse.core.dengdipipeline.fuseengine.Scheduler;
import io.datasearch.epidatafuse.core.dengdipipeline.models.configmodels.AggregationConfig;
import io.datasearch.epidatafuse.core.dengdipipeline.models.configmodels.GranularityRelationConfig;
import io.datasearch.epidatafuse.core.dengdipipeline.models.configmodels.IngestConfig;
import io.datasearch.epidatafuse.core.dengdipipeline.models.configmodels.SchemaConfig;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularityrelationmap.GranularityMap;
import io.datasearch.epidatafuse.core.dengdipipeline.publish.Publisher;
import io.datasearch.epidatafuse.core.dengdipipeline.stream.StreamHandler;
import io.siddhi.core.event.Event;

import org.geotools.data.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * DengDIPipeline is the processing unit for streaming and historical data.
 * PipeLine starts with streaming component which retrieves streaming data and ends with data publisher which publishes
 * aggregated data into specified endpoints.
 */
public class DengDIPipeLine {
    private static final Logger logger = LoggerFactory.getLogger(DengDIPipeLine.class);

    private PipelineDataStore pipelineDataStore;
    //Data store for persisting spatio-temporal data.
    private StreamHandler streamHandler;
    //aggregator and transformer;
    private FuseEngine fuseEngine;
    //publisher for publishing data to relevant endpoints
    private Publisher publisher;

    //store configurations reads from the user inputs
    private HashMap<String, GranularityRelationConfig> granularityRelationConfigs;
    private HashMap<String, AggregationConfig> aggregationConfigs;
    private HashMap<String, IngestConfig> ingestConfigs;
    private HashMap<String, SchemaConfig> schemaConfigs;

    //this stores granularity maps for each feature build according to the configs
    private HashMap<String, GranularityMap> granularityRelationMaps;

    private Timer timer = new Timer();
    private Scheduler scheduler = new Scheduler();

    public DengDIPipeLine(DataStore dataStore, Map<String, SimpleFeatureTypeSchema> schemas,
                          HashMap<String, GranularityRelationConfig> granularityRelationConfigs,
                          HashMap<String, AggregationConfig> aggregationConfigs) {
        this.pipelineDataStore = new PipelineDataStore(dataStore, schemas);
        this.streamHandler = new StreamHandler(this);
        this.fuseEngine = new FuseEngine(pipelineDataStore, granularityRelationConfigs, aggregationConfigs);
        this.granularityRelationConfigs = granularityRelationConfigs;
        this.aggregationConfigs = aggregationConfigs;
    }

    public DataStore getDataStore() {
        return this.pipelineDataStore.getDataStore();
    }

    public SimpleFeatureTypeSchema getSchema(String featureTypeName) {
        return this.pipelineDataStore.getSchema(featureTypeName);
    }

    public void ingest() {
        this.pipelineDataStore.bulkIngest();
    }

    public void mapGranularityRelations() {
//        this.granularityRelationMaps = this.fuseEngine.buildGranularityMap(granularityRelationConfigs);
        this.granularityRelationMaps = this.fuseEngine.setGranularityRelationMaps();
    }

    public void aggregate() throws IOException {
//        String featureType = "precipitation";
//        this.fuseEngine
//                .aggregate(granularityRelationMaps.get(featureType), this.aggregationConfigs.get(featureType));
//        this.fuseEngine.invokeAggregationProcess();

        this.fuseEngine.scheduleTasks(20000);
    }

    public void streamingIngest(Event[] events, String featureType) {
        pipelineDataStore.streamingIngest(events, featureType);
    }
}
