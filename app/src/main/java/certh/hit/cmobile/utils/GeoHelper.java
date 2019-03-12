package certh.hit.cmobile.utils;

import android.util.Log;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.shape.impl.BufferedLineString;
import com.spatial4j.core.shape.impl.PointImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anmpout on 12/03/2019
 */
public class GeoHelper {


    public static boolean isLocationInsideLineBox(Double startLat, Double startLon, Double stopLat, Double stopLon, double currentLat, double currentLon){
        PointImpl start = new PointImpl(startLat,startLon, SpatialContext.GEO);
        PointImpl stop = new PointImpl(stopLat,stopLon, SpatialContext.GEO);
        PointImpl currentPoint = new PointImpl(currentLat,currentLon, SpatialContext.GEO);
        BufferedLineString bls = createPolyline(start,stop);

        SpatialRelation relation = bls.relate(currentPoint);
        if(relation.equals(SpatialRelation.CONTAINS)){
            Log.d("Debug","CONTAINS");
            Log.d("Debug",start.toString());
            Log.d("Debug",stop.toString());
            Log.d("Debug",currentPoint.toString());
            Log.d("Debug",bls.toString());
           return true;

        }else{
            Log.d("Debug","NOT CONTAINS");
            Log.d("Debug",start.toString());
            Log.d("Debug",stop.toString());
            Log.d("Debug",currentPoint.toString());
            Log.d("Debug",bls.toString());
            return false;
        }


    }

    private static BufferedLineString createPolyline(PointImpl start,PointImpl stop) {
        BufferedLineString ls = null;
        List<Point> pointsList = new ArrayList<>();


        pointsList.add(start);
        pointsList.add(stop);

        ls = new BufferedLineString(pointsList, 0.001, SpatialContext.GEO);
        return ls;

    }
}
