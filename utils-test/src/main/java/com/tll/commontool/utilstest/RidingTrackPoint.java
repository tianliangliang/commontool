package com.tll.commontool.utilstest;

/**
 * Created by tll on 2017/10/29 21:19
 **/
public class RidingTrackPoint {
    // 经度
    private double lng;
    // 纬度
    private double lat;
    // 时间戳(毫秒)
    private long timestamp;

    public RidingTrackPoint() {
        this(0, 0, 0);
    }

    public RidingTrackPoint(double lng, double lat) {
        this(lng, lat, 0);
    }

    public RidingTrackPoint(double lng, double lat, long timestamp) {
        this.lng = lng;
        this.lat = lat;
        this.timestamp = timestamp;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 判断节点是否为有效节点。
     * <p>
     * 有效节点, 具备以下条件:
     * <p>
     * 1. 不为'经纬度零点'(或者近似'经纬度 0 点')
     * 2. 经纬度取值不越界
     * 3. 时间戳为有效的取值
     *
     * @return true 节点有效, false 节点无效
     */
    public boolean isEffective() {
        return !((-0.1 < lng && lng < 0.1) && (-0.1 < lat && lat < 0.1)) && (-180 <= lng && lng <= 180) && (-90 <= lat && lat <= 90) && timestamp > 0;
    }

    /**
     * 判断经纬度是否相同。
     *
     * @param obj 对比节点
     * @return true 节点经纬度相同, false 节点经纬度不同。
     */
    public boolean isSamePosition(RidingTrackPoint obj) {
        // 边界检查
        if (null == obj) {
            return false;
        }
        // 判断经纬度是否相等
        return (Double.compare(this.getLat(), obj.getLat()) == 0) && (Double.compare(this.getLng(), obj.getLng()) == 0);
    }
}
