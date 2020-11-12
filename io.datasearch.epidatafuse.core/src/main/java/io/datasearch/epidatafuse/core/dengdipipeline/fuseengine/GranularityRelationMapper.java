package io.datasearch.epidatafuse.core.dengdipipeline.fuseengine;

import io.datasearch.epidatafuse.core.dengdipipeline.datastore.PipelineDataStore;
import io.datasearch.epidatafuse.core.dengdipipeline.datastore.query.QueryManager;
import io.datasearch.epidatafuse.core.dengdipipeline.models.configmodels.GranularityRelationConfig;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularitymappingmethods.ContainMapper;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularitymappingmethods.NearestMapper;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularityrelationmap.SpatialGranularityRelationMap;
import io.datasearch.epidatafuse.core.dengdipipeline.models.granularityrelationmap.TemporalGranularityMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * For granularity Mapping.
 */
public class GranularityRelationMapper {

    private PipelineDataStore pipelineDataStore;
    private DataStore dataStore;
    private QueryManager queryManager;
    private static final Logger logger = LoggerFactory.getLogger(GranularityRelationMapper.class);

    private ArrayList<Map<String, Object>> spatioTemporalGranularityConfigs;

    //private SimpleFeatureCollection targetSpatialGranules;

    public GranularityRelationMapper(PipelineDataStore pipelineDataStore) {
        this.pipelineDataStore = pipelineDataStore;
        this.dataStore = pipelineDataStore.getDataStore();
        this.queryManager = new QueryManager();
    }

    public SpatialGranularityRelationMap buildSpatialGranularityMap(GranularityRelationConfig config) {
        logger.info("spatial " + config.getFeatureTypeName());
        String spatialGranularity = config.getSpatialGranularity();
        String relationMappingMethod = config.getSpatialRelationMappingMethod();
        String targetSpatialGranularity = config.getTargetSpatialGranularity();

        SpatialGranularityRelationMap spatialMap;

        SimpleFeatureCollection targetSpatialGranules = this.getGranuleSet(targetSpatialGranularity);

        SimpleFeatureCollection baseSpatialGranuleSet = this.getGranuleSet(spatialGranularity);

        switch (relationMappingMethod) {
            case "nearest":
                int neighbors = Integer.parseInt(config.getCustomAttribute("neighbors"));
                double maxDistance = Double.parseDouble(config.getCustomAttribute("maxDistance"));

                spatialMap = NearestMapper.buildNearestMap(targetSpatialGranules,
                        baseSpatialGranuleSet, neighbors, maxDistance);
                break;

            case "contain":
                spatialMap = ContainMapper.buildContainMap(targetSpatialGranules, baseSpatialGranuleSet);
                break;

            default:
                spatialMap = new SpatialGranularityRelationMap();
        }

        return spatialMap;
    }

    public SimpleFeatureCollection getGranuleSet(String granularityName) {
        try {

            Query query = new Query(granularityName);
            FeatureReader<SimpleFeatureType, SimpleFeature> reader =
                    dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);

            ArrayList<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
            while (reader.hasNext()) {
                SimpleFeature feature = (SimpleFeature) reader.next();
                featureList.add(feature);
                logger.info(feature.toString());
            }
            reader.close();
            return DataUtilities.collection(featureList);
        } catch (Exception e) {
            logger.info(e.getMessage());
            return null;
        }
    }

    // to implement
    public TemporalGranularityMap buildTemporalMap(GranularityRelationConfig granularityRelationConfig) {
        logger.info("temporal " + granularityRelationConfig.getFeatureTypeName());

        String baseTemporalGranularity = granularityRelationConfig.getTemporalGranularity();
        String targetTemporalGranularity = granularityRelationConfig.getTargetTemporalGranularity();
        String featureTypeName = granularityRelationConfig.getFeatureTypeName();
        String temporalRelationMappingMethod = granularityRelationConfig.getTemporalRelationMappingMethod();

        return new TemporalGranularityMap(
                baseTemporalGranularity, targetTemporalGranularity, featureTypeName,
                temporalRelationMappingMethod);
    }
}
