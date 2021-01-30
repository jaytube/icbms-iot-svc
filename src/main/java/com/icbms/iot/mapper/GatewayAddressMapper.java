package com.icbms.iot.mapper;

import com.icbms.iot.entity.GatewayAddress;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: Cherry
 * @Date: 2021/1/28
 * @Desc: GateWayInfoMapper
 */
public interface GatewayAddressMapper {

    @Select("select id as id,name as name,ip_address as ipAddress from gateway_address")
    List<GatewayAddress> findAll();

    @Insert({
            "<script>",
            "insert into gateway_address (name,ip_address) values",
            "(#{gateWayAddress.name},#{gateWayAddress.ipAddress})",
            "</script>"
    })
    void insert(GatewayAddress gateWayAddress);
}
