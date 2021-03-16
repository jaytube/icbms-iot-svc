package com.icbms.iot.mapper;

import com.icbms.iot.entity.ProjectInfo;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface ProjectInfoMapper {

    @Select("select * from project_info p join gateway_info g on p.gateway_address = g.gateway_id where str_to_date(p.effective_date, '%Y-%m-%d')  <= #{currentDate} and str_to_date(p.expire_date, '%Y-%m-%d')  >= #{currentDate} and p.gym_id = 2")
    List<ProjectInfo> findAllEffectiveProjects(Date currentDate);
}
