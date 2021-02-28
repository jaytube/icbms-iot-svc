package com.icbms.iot.mapper;

import org.apache.ibatis.annotations.Delete;

import java.util.List;

public interface DeviceLocationInfoMapper {

    @Delete({
            "<script>",
            "delete from device_box_location where device_box_id = (",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "#{item}",
            "</foreach>",
            ")",
            "</script>"})
    void deleteByDeviceBoxIds(List<String> deviceBoxIdList);
}
