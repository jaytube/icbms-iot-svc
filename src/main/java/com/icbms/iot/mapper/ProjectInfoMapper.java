package com.icbms.iot.mapper;

import com.icbms.iot.entity.ProjectInfo;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface ProjectInfoMapper {

    @Select("select * from project_info where str_to_date(effective_date, '%Y-%m-%d')  <= #{currentDate} and str_to_date(expire_date, '%Y-%m-%d')  >= #{currentDate}")
    List<ProjectInfo> findAllEffectiveProjects(Date currentDate);
}
