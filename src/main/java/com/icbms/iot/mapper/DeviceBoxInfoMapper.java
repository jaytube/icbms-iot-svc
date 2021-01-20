package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceBoxInfo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DeviceBoxInfoMapper {

    @Select("select * from device_box_info where device_box_num = #{deviceBoxNum} AND project_id = #{projectId}")
    List<DeviceBoxInfo> findByDeviceBoxNumAndProjectId(String deviceBoxNum, String projectId);

    @Select("<script>" +
            "select * from device_box_info where project_id in " +
            "<foreach item='item' index='index' collection='projectIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<DeviceBoxInfo> findByProjectIdList(List<String> projectIds);
}
