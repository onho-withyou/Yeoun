package com.yeoun.orgchart.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Emp;

@Repository
public interface OrgChartRepository extends JpaAttributeConverter<Emp, String> {

}
