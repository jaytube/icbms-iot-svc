package com.icbms.iot.mapper;

import org.apache.ibatis.annotations.Delete;

public interface LocationInfoMapper {

    @Delete("delete from location_info where project_id = #{projectId}")
    void deleteByProjectId(String projectId);
}
