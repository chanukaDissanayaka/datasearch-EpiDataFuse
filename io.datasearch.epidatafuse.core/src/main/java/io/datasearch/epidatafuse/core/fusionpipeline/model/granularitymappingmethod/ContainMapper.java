package io.datasearch.epidatafuse.core.fusionpipeline.model.granularitymappingmethod;

import io.datasearch.epidatafuse.core.fusionpipeline.model.granularityrelationmap.SpatialGranularityRelationMap;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * containing objects mapping method
 */

public class ContainMapper {
    private static final Logger logger = LoggerFactory.getLogger(ContainMapper.class);

    public static SpatialGranularityRelationMap buildContainMap(SimpleFeatureCollection targetGranuleSet,
                                                                SimpleFeatureCollection baseGranuleSet) {
        logger.info("here");
        SimpleFeatureIterator featureIt = targetGranuleSet.features();
        int count = 0;

        try {
            while (featureIt.hasNext()) {
//                SimpleFeature targetPolygon = featureIt.next();
//                logger.info(targetPolygon.toString());
//                logger.info("\n\n");
                count = count + 1;
            }
        } finally {
            featureIt.close();
        }
        logger.info(Integer.toString(count));
        return new SpatialGranularityRelationMap();
    }
}