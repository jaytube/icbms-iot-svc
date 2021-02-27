package com.icbms.iot.mapper;

import com.icbms.iot.entity.GatewayInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: Cherry
 * @Date: 2021/1/29
 * @Desc: GateWayInfoMapper
 */
public interface GatewayInfoMapper {

    @Insert({
            "<script>",
            "insert into gateway_info (gateway_id, create_time, update_time, lora_id, name, application_id, application_name, lora_application_id, scene_id, scene_name, lora_scene_id, mac_address, des, mgr_url, create_user_id, update_user_name, update_user_id, is_del, ip_address) values (",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.gatewayId}, #{item.createTime}, #{item.updateTime}, #{item.loraId}, #{item.name}, #{item.applicationId},",
            "#{item.applicationName}, #{item.loraApplicationId}, #{item.sceneId}, #{item.sceneName}, #{item.loraSceneId}, #{item.macAddress}, #{item.des}, #{item.mgrUrl}, #{item.createUserId}, #{item.updateUserName}, #{item.updateUserId}, #{item.isDel}, #{item.ipAddress})",
            "</foreach>",
            ")",
            "</script>"
    })
    void batchInsert(List<GatewayInfo> gatewayInfos);

    @Select("select * from gateway_info")
    List<GatewayInfo> findAll();

    @Select("select * from gateway_info where online = '1'")
    List<GatewayInfo> findAllOnlines();

    @Select({
            "<script>",
            "select * from gateway_info where gateway_id in (",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "#{item}",
            "</foreach>",
            ")",
            "</script>"
    })
    List<GatewayInfo> findByGatewayId(List<String> gatewayAddresses);

    @Select("select * from gateway_info where gateway_id = #{gatewayId}")
    GatewayInfo findById(String gatewayId);
}
