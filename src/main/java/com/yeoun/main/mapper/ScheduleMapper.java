package com.yeoun.main.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper {

	List<Map<String, Object>> getOrganizationList();

}
