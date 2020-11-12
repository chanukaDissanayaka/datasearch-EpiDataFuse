package io.datasearch.epidatafuse.core.dengdipipeline.models.granularitymappingmethods;

import io.datasearch.epidatafuse.core.dengdipipeline.models.granularityrelationmap.SpatialGranularityRelationMap;
import java.util.ArrayList;
import java.util.HashMap;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * containing objects mapping method
 */

public class ContainMapper {
    private static final Logger logger = LoggerFactory.getLogger(ContainMapper.class);

    public static SpatialGranularityRelationMap buildContainMap(SimpleFeatureCollection targetGranuleSet,
                                                                SimpleFeatureCollection baseGranuleSet) {

        SpatialGranularityRelationMap containMap = new SpatialGranularityRelationMap();

        SimpleFeatureIterator featureIt = targetGranuleSet.features();

        try {
            while (featureIt.hasNext()) {
                ArrayList<String> containingGranules = new ArrayList<String>();

                SimpleFeature targetGranule = featureIt.next();
                Geometry targetPolygon = (Geometry) targetGranule.getDefaultGeometry();

                SimpleFeatureIterator iterator2 = baseGranuleSet.features();
                while (iterator2.hasNext()) {
                    SimpleFeature baseGranule = iterator2.next();
                    Geometry basePolygon = (Geometry) baseGranule.getDefaultGeometry();
                    if (targetPolygon.contains(basePolygon.getCentroid())) {
                        containingGranules.add(baseGranule.getID());
                    }
                }
                containMap.addPoint(targetGranule.getID(), containingGranules);
            }
        } finally {
            featureIt.close();
        }
        return containMap;
    }
}
