package com.tll.commontool.utilstest;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tll on 2017/10/29 21:25
 **/
public class GprsUtil {
    private static double x_pi = 52.35987755982988D;
    private static final double PI = 3.141592653589793D;
    private static final double R = 6371229.0D;
    private static final double X = 0.017453292519943295D;

    public GprsUtil() {
    }

    public static Map<String, Double> Convert_GCJ02_To_BD09(double lat, double lng) {
        double z = Math.sqrt(lng * lng + lat * lat) + 2.0E-5D * Math.sin(lat * x_pi);
        double theta = Math.atan2(lat, lng) + 3.0E-6D * Math.cos(lng * x_pi);
        lng = z * Math.cos(theta) + 0.0065D;
        lat = z * Math.sin(theta) + 0.006D;
        Map<String, Double> map = Maps.newHashMap();
        map.put("lng", lng);
        map.put("lat", lat);
        return map;
    }

    public static Map<String, Double> Convert_BD09_To_GCJ02(double lat, double lng) {
        double x = lng - 0.0065D;
        double y = lat - 0.006D;
        double z = Math.sqrt(x * x + y * y) - 2.0E-5D * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 3.0E-6D * Math.cos(x * x_pi);
        lng = z * Math.cos(theta);
        lat = z * Math.sin(theta);
        Map<String, Double> map = Maps.newHashMap();
        map.put("lng", lng);
        map.put("lat", lat);
        return map;
    }

    public static double getLongt(double longt1, double lat1, double distance) {
        double a = 57.29577951308232D * (distance / 6371229.0D);
        return a;
    }

    public static double getLat(double longt1, double lat1, double distance) {
        double a = 180.0D * distance / (2.0015806220738243E7D * Math.cos(lat1 * 3.141592653589793D / 180.0D));
        return a;
    }

    public static Map<String, Double> getMinAndMaxLat(Double myLng, Double myLat, int dis) {
        double ds = (double)dis / 1000.0D;
        Map<String, Double> map1 = Convert_BD09_To_GCJ02(myLat.doubleValue(), myLng.doubleValue());
        myLat = (Double)map1.get("lat");
        myLng = (Double)map1.get("lng");
        Double range = 57.29577951308232D * (ds / 6372.797D);
        Double lngR = range.doubleValue() / Math.cos(myLat.doubleValue() * 3.141592653589793D / 180.0D);
        Double maxLat = myLat.doubleValue() + range.doubleValue();
        Double minLat = myLat.doubleValue() - range.doubleValue();
        Double maxLng = myLng.doubleValue() + lngR.doubleValue();
        Double minLng = myLng.doubleValue() - lngR.doubleValue();
        Map<String, Double> map = Maps.newHashMap();
        map.put("minPositionX", minLng);
        map.put("maxPositionX", maxLng);
        map.put("minPositionY", minLat);
        map.put("maxPositionY", maxLat);
        return map;
    }

    private static double rad(double d) {
        return d * 0.017453292519943295D;
    }

    public static double getDistanceOfMeter(double long1, double lat1, double long2, double lat2) {
        lat1 = lat1 * 3.141592653589793D / 180.0D;
        lat2 = lat2 * 3.141592653589793D / 180.0D;
        double a = lat1 - lat2;
        double b = (long1 - long2) * 3.141592653589793D / 180.0D;
        double sa2 = Math.sin(a / 2.0D);
        double sb2 = Math.sin(b / 2.0D);
        double d = 1.2742458E7D * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
        return d;
    }

    public static Map<String, Double> getRectangle(double lng, double lat, long distance) {
        float delta = 111000.0F;
        double lng1;
        double lng2;
        double lat1;
        double lat2;
        HashMap map;
        if (lng != 0.0D && lat != 0.0D) {
            lng1 = lng - (double)distance / Math.abs(Math.cos(Math.toRadians(lat)) * (double)delta);
            lng2 = lng + (double)distance / Math.abs(Math.cos(Math.toRadians(lat)) * (double)delta);
            lat1 = lat - (double)((float)distance / delta);
            lat2 = lat + (double)((float)distance / delta);
            map = Maps.newHashMap();
            map.put("minPositionX", lng1);
            map.put("maxPositionX", lng2);
            map.put("minPositionY", lat1);
            map.put("maxPositionY", lat2);
            return map;
        } else {
            lng1 = lng - (double)((float)distance / delta);
            lng2 = lng + (double)((float)distance / delta);
            lat1 = lat - (double)((float)distance / delta);
            lat2 = lat + (double)((float)distance / delta);
            map = Maps.newHashMap();
            map.put("minPositionX", lng1);
            map.put("maxPositionX", lng2);
            map.put("minPositionY", lat1);
            map.put("maxPositionY", lat2);
            return map;
        }
    }

    public static int isPointInGraphical(List<Double[]> list, double lng, double lat) {
        int nCount = list.size();
        int intersection_point = 0;

        for(int i = 0; i < nCount; ++i) {
            Double[] p1 = (Double[])list.get(i);
            Double[] p2 = (Double[])list.get((i + 1) % nCount);
            if (p1[1] != p2[1] && lat < max(p1[1].doubleValue(), p2[1].doubleValue()) && lat >= min(p1[1].doubleValue(), p2[1].doubleValue())) {
                double x = (lat - p1[1].doubleValue()) * (p2[0].doubleValue() - p1[0].doubleValue()) / (p2[1].doubleValue() - p1[1].doubleValue()) + p1[0].doubleValue();
                if (x > lng) {
                    ++intersection_point;
                }
            }
        }

        return intersection_point % 2;
    }

    private static double max(double x, double y) {
        return x > y ? x : y;
    }

    private static double min(double x, double y) {
        return x < y ? x : y;
    }

    public static void main(String[] arg) {
        //int dis = true;
        double d = getDistanceOfMeter(116.350763D, 40.009222D, 116.467686D, 39.948436D);
        System.out.println("计算开始3 = " + d);
    }
}
