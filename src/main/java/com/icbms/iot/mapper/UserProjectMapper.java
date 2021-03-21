package com.icbms.iot.mapper;

import org.apache.ibatis.annotations.Delete;

import java.util.List;

public interface UserProjectMapper {

    @Delete({
            "<script>",
            "delete from sys_user_project where project_id in (",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "#{item}",
            "</foreach>",
            ")",
            "</script>"})
    void deleteByProjectIdList(List<String> projectIdList);

}
