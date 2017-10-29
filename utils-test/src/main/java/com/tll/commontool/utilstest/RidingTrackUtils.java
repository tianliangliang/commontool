package com.tll.commontool.utilstest;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 骑行轨迹相关的工具类
 */
public class RidingTrackUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RidingTrackUtils.class);

    // 自行车的最大速度 (单位: m/s)
    private static final int BIKE_MAX_SPEED_METERS_PER_SECOND = 30;

    /**
     * 根据'原始的骑行轨迹信息', 抽取'有效骑行轨迹', 同时, 计算'骑行距离':
     * <p>
     * 1. 根据'订单的起止时间', 从'原始的骑行轨迹信息'中, 截取出 '有效的骑行轨迹';
     * 2. 过滤异常点: 过滤经纬度 0 点和特殊点;
     * 3. 轨迹校正: 重复坐标去重、偏移坐标纠正
     *
     * @param oriTrackTime   原始的骑行轨迹数据 (格式: '#lng,lat;timestamp#lng,lat;timestamp' )
     * @param orderStartTime 订单的起始时间 (单位: ms)
     * @param orderEndTime   订单的结束时间 (单位: ms)
     * @return 有效的骑行轨迹数据
     */
    public static List<RidingTrackPoint> extractEffectivePointList(String oriTrackTime, long orderStartTime, long orderEndTime) {
        // 边界检查
        if (StringUtils.isBlank(oriTrackTime)) {
            return Collections.emptyList();
        }

        // 全量的 point 列表(简单过滤).
        List<RidingTrackPoint> totalPointList = Lists.newArrayList();
        String[] tracks = StringUtils.split(oriTrackTime, "#");
        for (String singlePointStr : tracks) {
            RidingTrackPoint currPoint = extractSinglePoint(singlePointStr);
            if (currPoint == null) {
                continue;
            }
            if (!currPoint.isEffective()) {
                continue;
            }
            totalPointList.add(currPoint);
        }
        Collections.sort(totalPointList, (former, latter) -> {
            if (null == former && null == latter) {
                return 0;
            } else if (null == former) {
                return -1;
            } else if (null == latter) {
                return 1;
            }
            return (former.getTimestamp() == latter.getTimestamp() ? 0 : (former.getTimestamp() > latter.getTimestamp() ? 1 : -1));
        });

        // 有效的 point 列表(去重和修偏)
        List<RidingTrackPoint> effectivePointList = Lists.newArrayList();
        for (RidingTrackPoint currPoint : totalPointList) {
            // 1. 窗口截取: 在订单的时间窗口内, 才会被截取。
            long timeOfCurrPoint = currPoint.getTimestamp();
            if (!(orderStartTime <= timeOfCurrPoint && timeOfCurrPoint <= orderEndTime)) {
                continue;
            }

            // 第一个节点
            if (CollectionUtils.isEmpty(effectivePointList)) {
                effectivePointList.add(currPoint);
                continue;
            }

            // 2. 轨迹修正:
            // a. 去重: 忽略相同'经纬度'的节点
            // b. 修偏: 2 个节点之间, 平均速度 > 25m/s (90km/h), 则, 忽略后一个节点
            RidingTrackPoint lastPoint = effectivePointList.get(effectivePointList.size() - 1);
            if (lastPoint.isSamePosition(currPoint) || !isSpeedLimited(lastPoint, currPoint)) {
                continue;
            }
            effectivePointList.add(currPoint);
        }

        return effectivePointList;
    }

    /**
     * 根据原始字符串, 抽取经纬度节点。
     *
     * @param oriString 原始字符串,要求为: 'lng,lat;timestamp' 格式
     * @return 结构化的经纬度节点信息。
     */
    public static RidingTrackPoint extractSinglePoint(String oriString) {
        if (StringUtils.isBlank(oriString)) {
            return null;
        }
        String[] lngLatAndTimestampArray = StringUtils.split(oriString, ";");
        if (lngLatAndTimestampArray.length < 2) {
            return null;
        }
        String lngLatStr = lngLatAndTimestampArray[0];
        String timestampStr = lngLatAndTimestampArray[1];
        if (StringUtils.isBlank(lngLatStr) || StringUtils.isBlank(timestampStr)) {
            return null;
        }
        String[] lngLatArray = StringUtils.split(lngLatStr, ",");
        if (lngLatArray.length < 2) {
            return null;
        }
        try {
            double lng = Double.parseDouble(lngLatArray[0].trim());
            double lat = Double.parseDouble(lngLatArray[1].trim());
            long timestamp = Long.parseLong(timestampStr.trim());
            return new RidingTrackPoint(lng, lat, timestamp);
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.error("[RidingTrack] extract the RidingTrackPoint Object from original string ({}), exception happens, skip this string.", oriString, e);
            return null;
        }

    }

    /**
     * 判断节点之间, 平均速度是否超过限制: <= 25 m/s.
     *
     * @param former 前一个节点
     * @param latter 后一个节点
     * @return true 没有超过限制, false 超过限制。
     */
    public static boolean isSpeedLimited(RidingTrackPoint former, RidingTrackPoint latter) {
        // 无效节点: 直接返回 false, 判断为超速。
        if (null == former || null == latter || former.getTimestamp() <= 0 || latter.getTimestamp() <= 0) {
            return false;
        }
        double distance = 0;
        try {
            distance = GprsUtil.getDistanceOfMeter(former.getLng(), former.getLat(), latter.getLng(), latter.getLat());
        } catch (RuntimeException e) {
            LOGGER.error("[RidingTrack] revise the original points, the exception happens, while checking the speed whether exceed the MAX_SPEED. Current strategy: skip the latter point (B), go on the next point. Point A({}), Point B({})", JSON.toJSON(former), JSON.toJSON(latter), e);
            return false;
        }
        // 计算速度: 单位 m/s
        double speed = distance / (Math.abs(former.getTimestamp() - latter.getTimestamp()) / 1000.0);
        return speed <= BIKE_MAX_SPEED_METERS_PER_SECOND;
    }
}
