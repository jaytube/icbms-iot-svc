package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceBoxInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DeviceBoxInfoMapper {

    @Select("select * from device_box_info where device_box_num = #{deviceBoxNum} AND project_id = #{projectId}")
    List<DeviceBoxInfo> findByDeviceBoxNumAndProjectId(String deviceBoxNum, String projectId);

    @Select({"<script>",
            "select * from device_box_info where project_id in (",
            "<foreach item='item' index='index' collection='list' separator=','>",
            "#{item}",
            "</foreach>",
            ")",
            "</script>"})
    List<DeviceBoxInfo> findByProjectIdList(List<String> projectIds);

    @Delete("delete from device_box_info where project_id = #{projectId}")
    void deleteByProjectId(String projectId);
}
