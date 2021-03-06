package com.icbms.iot.mapper;

import com.icbms.iot.entity.GatewayDeviceMap;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: Cherry
 * @Date: 2021/1/29
 * @Desc: GatewayDeviceMap
 */
public interface GatewayDeviceMapMapper {

    @Select("select device_sn from gateway_device_map where gym_id = #{gymId} AND project_id = #{projectId} AND gateway_id = #{gatewayId}")
    List<String> findDeviceSns(int gymId, String projectId, String gatewayId);

    @Select("select * from gateway_device_map where gym_id = #{gymId} AND project_id = #{projectId}")
    List<GatewayDeviceMap> findByGmIdAndProjectId(int gymId, String projectId);

    @Select("select * from gateway_device_map")
    List<GatewayDeviceMap> findAll();

    @Delete("delete from gateway_device_map where gateway_id = #{gatewayId}")
    void deleteByGatewayId(Integer gatewayId);

    @Delete("delete from gateway_device_map where gateway_id = #{gatewayId} and project_id = #{projectId}")
    void deleteByGatewayIdAndProjectId(Integer gatewayId, String projectId);
}
