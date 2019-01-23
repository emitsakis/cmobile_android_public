package certh.hit.cmobile.utils

/**
 * Created by anmpout on 23/01/2019
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays

/**
 * Copyright (C) 2009, 2010
 * State of California,
 * Department of Water Resources.
 * This file is part of DSM2 Grid Map
 * The DSM2 Grid Map is free software:
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * DSM2 Grid Map is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. [http://www.gnu.org/licenses]
 *
 * @author Nicky Sandhu
 */

/**
 * TMS Global Mercator OmsProfile ---------------------------
 *
 * Functions necessary for generation of tiles in Spherical Mercator projection,
 * EPSG:900913 (EPSG:gOOglE, Google Maps Global Mercator), EPSG:3785,
 * OSGEO:41001.
 *
 * Such tiles are compatible with Google Maps, Microsoft Virtual Earth, Yahoo
 * Maps, UK Ordnance Survey OpenSpace API, ... and you can overlay them on top
 * of base maps of those web mapping applications.
 *
 * Pixel and tile coordinates are in TMS notation (origin [0,0] in bottom-left).
 *
 * What coordinate conversions do we need for TMS Global Mercator tiles::
 *
 * LatLon <-> Meters <-> Pixels <-> Tile
 *
 * WGS84 coordinates Spherical Mercator Pixels in pyramid Tiles in pyramid
 * lat/lon XY in metres XY pixels Z zoom XYZ from TMS EPSG:4326 EPSG:900913
 * .----. --------- -- TMS / \ <-> | | <-> /----/ <-> Google \ / | | /--------/
 * QuadTree ----- --------- /------------/ KML, public WebMapService Web Clients
 * TileMapService
 *
 * What is the coordinate extent of Earth in EPSG:900913?
 *
 * [-20037508.342789244, -20037508.342789244, 20037508.342789244,
 * 20037508.342789244] Constant 20037508.342789244 comes from the circumference
 * of the Earth in meters, which is 40 thousand kilometers, the coordinate
 * origin is in the middle of extent. In fact you can calculate the constant as:
 * 2 * Math.PI * 6378137 / 2.0 $ echo 180 85 | gdaltransform -s_srs EPSG:4326
 * -t_srs EPSG:900913 Polar areas with abs(latitude) bigger then 85.05112878 are
 * clipped off.
 *
 * What are zoom level constants (pixels/meter) for pyramid with EPSG:900913?
 *
 * whole region is on top of pyramid (zoom=0) covered by 256x256 pixels tile,
 * every lower zoom level resolution is always divided by two initialResolution
 * = 20037508.342789244 * 2 / 256 = 156543.03392804062
 *
 * What is the difference between TMS and Google Maps/QuadTree tile name
 * convention?
 *
 * The tile raster itself is the same (equal extent, projection, pixel size),
 * there is just different identification of the same raster tile. Tiles in TMS
 * are counted from [0,0] in the bottom-left corner, id is XYZ. Google placed
 * the origin [0,0] to the top-left corner, reference is XYZ. Microsoft is
 * referencing tiles by a QuadTree name, defined on the website:
 * http://msdn2.microsoft.com/en-us/library/bb259689.aspx
 *
 * The lat/lon coordinates are using WGS84 datum, yeh?
 *
 * Yes, all lat/lon we are mentioning should use WGS84 Geodetic Datum. Well, the
 * web clients like Google Maps are projecting those coordinates by Spherical
 * Mercator, so in fact lat/lon coordinates on sphere are treated as if the were
 * on the WGS84 ellipsoid.
 *
 * From MSDN documentation: To simplify the calculations, we use the spherical
 * form of projection, not the ellipsoidal form. Since the projection is used
 * only for map display, and not for displaying numeric coordinates, we don't
 * need the extra precision of an ellipsoidal projection. The spherical
 * projection causes approximately 0.33 percent scale distortion in the Y
 * direction, which is not visually noticable.
 *
 * How do I create a raster in EPSG:900913 and convert coordinates with PROJ.4?
 *
 * You can use standard GIS tools like gdalwarp, cs2cs or gdaltransform. All of
 * the tools supports -t_srs 'epsg:900913'.
 *
 * For other GIS programs check the exact definition of the projection: More
 * info at http://spatialreference.org/ref/user/google-projection/ The same
 * projection is degined as EPSG:3785. WKT definition is in the official EPSG
 * database.
 *
 * Proj4 Text: +proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0
 * +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs
 *
 * Human readable WKT format of EPGS:900913:
 * PROJCS["Google Maps Global Mercator", GEOGCS["WGS 84", DATUM["WGS_1984",
 * SPHEROID["WGS 84",6378137,298.2572235630016, AUTHORITY["EPSG","7030"]],
 * AUTHORITY["EPSG","6326"]], PRIMEM["Greenwich",0],
 * UNIT["degree",0.0174532925199433], AUTHORITY["EPSG","4326"]],
 * PROJECTION["Mercator_1SP"], PARAMETER["central_meridian",0],
 * PARAMETER["scale_factor",1], PARAMETER["false_easting",0],
 * PARAMETER["false_northing",0], UNIT["metre",1, AUTHORITY["EPSG","9001"]]]
 *
 *
 */
class GlobalMercator {
    private val tileSize: Int
    private val initialResolution: Double
    private val originShift: Double

    init {
        tileSize = TILE_SIZE
        initialResolution = 2.0 * Math.PI * 6378137.0 / tileSize
        // 156543.03392804062 for tileSize 256 pixels
        originShift = 2.0 * Math.PI * 6378137.0 / 2.0
        // 20037508.342789244
    }

    /**
     * Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator
     * EPSG:900913
     *
     * @param lat
     * @param lon
     * @return
     */
    fun LatLonToMeters(lat: Double, lon: Double): DoubleArray {

        val mx = lon * originShift / 180.0
        var my = Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0)
        my = my * originShift / 180.0
        return doubleArrayOf(mx, my)
    }

    /**
     * Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84
     * Datum
     *
     * @return
     */
    fun MetersToLatLon(mx: Double, my: Double): DoubleArray {

        val lon = mx / originShift * 180.0
        var lat = my / originShift * 180.0

        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0)
        return doubleArrayOf(lat, lon)
    }

    /**
     * Converts pixel coordinates in given zoom level of pyramid to EPSG:900913
     *
     * @return
     */
    fun PixelsToMeters(px: Double, py: Double, zoom: Int): DoubleArray {
        val res = Resolution(zoom)
        val mx = px * res - originShift
        val my = py * res - originShift
        return doubleArrayOf(mx, my)
    }

    /**
     * Converts EPSG:900913 to pyramid pixel coordinates in given zoom level
     *
     * @param mx
     * @param my
     * @param zoom
     * @return
     */
    fun MetersToPixels(mx: Double, my: Double, zoom: Int): IntArray {
        val res = Resolution(zoom)
        val px = Math.round((mx + originShift) / res).toInt()
        val py = Math.round((my + originShift) / res).toInt()
        return intArrayOf(px, py)
    }

    /**
     * Returns a tile covering region in given pixel coordinates
     *
     * @param px
     * @param py
     * @return
     */
    fun PixelsToTile(px: Int, py: Int): IntArray {
        val tx = Math.ceil(px / tileSize.toDouble() - 1).toInt()
        val ty = Math.ceil(py / tileSize.toDouble() - 1).toInt()
        return intArrayOf(tx, ty)
    }

    /**
     * Move the origin of pixel coordinates to top-left corner
     *
     * @param px
     * @param py
     * @param zoom
     * @return
     */
    fun PixelsToRaster(px: Int, py: Int, zoom: Int): IntArray {
        val mapSize = tileSize shl zoom
        return intArrayOf(px, mapSize - py)
    }

    /**
     * Returns tile for given mercator coordinates
     *
     * @return
     */
    fun MetersToTile(mx: Double, my: Double, zoom: Int): IntArray {
        val p = MetersToPixels(mx, my, zoom)
        return PixelsToTile(p[0], p[1])
    }

    fun metersToTileUp(mx: Double, my: Double, zoom: Int): IntArray {
        val p = metersToPixelsUp(mx, my, zoom)
        return pixelsToTileUp(p[0], p[1])
    }

    fun metersToPixelsUp(mx: Double, my: Double, zoom: Int): IntArray {
        val res = Resolution(zoom)
        val px = Math.ceil((mx + originShift) / res).toInt()
        val py = Math.ceil((my + originShift) / res).toInt()
        return intArrayOf(px, py)
    }

    fun pixelsToTileUp(px: Int, py: Int): IntArray {
        val tx = Math.ceil(px / tileSize.toDouble() - 1).toInt()
        val ty = Math.ceil(py / tileSize.toDouble() - 1).toInt()
        return intArrayOf(tx, ty)
    }

    fun metersToTileDown(mx: Double, my: Double, zoom: Int): IntArray {
        val p = metersToPixelsDown(mx, my, zoom)
        return pixelsToTileDown(p[0], p[1])
    }

    fun metersToPixelsDown(mx: Double, my: Double, zoom: Int): IntArray {
        val res = Resolution(zoom)
        val px = Math.floor((mx + originShift) / res).toInt()
        val py = Math.floor((my + originShift) / res).toInt()
        return intArrayOf(px, py)
    }

    fun pixelsToTileDown(px: Int, py: Int): IntArray {
        val tx = Math.floor(px / tileSize.toDouble() - 1).toInt()
        val ty = Math.floor(py / tileSize.toDouble() - 1).toInt()
        return intArrayOf(tx, ty)
    }

    /**
     * Returns bounds of the given tile in EPSG:900913 coordinates
     *
     * @param tx
     * @param ty
     * @param zoom
     * @return
     */
    fun TileBounds(tx: Int, ty: Int, zoom: Int): DoubleArray {
        val min = PixelsToMeters((tx * tileSize).toDouble(), (ty * tileSize).toDouble(), zoom)
        val minx = min[0]
        val miny = min[1]
        val max = PixelsToMeters(((tx + 1) * tileSize).toDouble(), ((ty + 1) * tileSize).toDouble(), zoom)
        val maxx = max[0]
        val maxy = max[1]
        return doubleArrayOf(minx, miny, maxx, maxy)
    }

    /**
     * Returns bounds of the given tile in latitude/longitude using WGS84 datum
     *
     */
    fun TileLatLonBounds(tx: Int, ty: Int, zoom: Int): DoubleArray {
        val bounds = TileBounds(tx, ty, zoom)
        val mins = MetersToLatLon(bounds[0], bounds[1])
        val maxs = MetersToLatLon(bounds[2], bounds[3])
        return doubleArrayOf(mins[0], mins[1], maxs[0], maxs[1])
    }

    /**
     * Resolution (meters/pixel) for given zoom level (measured at Equator)
     *
     * @return
     */
    fun Resolution(zoom: Int): Double {
        // return (2 * Math.PI * 6378137) / (this.tileSize * 2**zoom)
        return initialResolution / Math.pow(2.0, zoom.toDouble())
    }

    /**
     * Maximal scaledown zoom of the pyramid closest to the pixelSize
     *
     * @param pixelSize
     * @return
     */
    fun ZoomForPixelSize(pixelSize: Int): Int {
        for (i in 0..29) {
            if (pixelSize > Resolution(i)) {
                return if (i != 0) {
                    i - 1
                } else {
                    0 // We don't want to scale up
                }
            }
        }
        return 0
    }

    /**
     * Converts TMS tile coordinates to Google Tile coordinates
     *
     * @param tx
     * @param ty
     * @param zoom
     * @return
     */
    fun GoogleTile(tx: Int, ty: Int, zoom: Int): IntArray {
        // coordinate origin is moved from bottom-left to top-left corner of the
        // extent
        return intArrayOf(tx, (Math.pow(2.0, zoom.toDouble()) - 1 - ty).toInt())
    }

    fun TMSTileFromGoogleTile(tx: Int, ty: Int, zoom: Int): IntArray {
        // coordinate origin is moved from bottom-left to top-left corner of the
        // extent
        return intArrayOf(tx, (Math.pow(2.0, zoom.toDouble()) - 1 - ty).toInt())
    }

    /**
     * Converts a lat long coordinates to Google Tile Coordinates
     *
     * @param lat
     * @param lon
     * @param zoom
     * @return
     */
    fun GoogleTile(lat: Double, lon: Double, zoom: Int): IntArray {
        val meters = LatLonToMeters(lat, lon)
        val tile = MetersToTile(meters[0], meters[1], zoom)
        return this.GoogleTile(tile[0], tile[1], zoom)
    }

    /**
     * Converts TMS tile coordinates to Microsoft QuadTree
     *
     * @return
     */
    fun QuadTree(tx: Int, ty: Int, zoom: Int): String {
        var ty = ty
        var quadKey = ""
        ty = (Math.pow(2.0, zoom.toDouble()) - 1 - ty).toInt()
        for (i in zoom downTo 1) {
            var digit = 0
            val mask = 1 shl i - 1
            if (tx and mask != 0) {
                digit += 1
            }
            if (ty and mask != 0) {
                digit += 2
            }
            quadKey += digit.toString() + ""
        }
        return quadKey
    }

    companion object {
        val TILE_SIZE = 256
    }


}

