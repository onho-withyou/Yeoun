package com.yeoun.qc.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

import com.yeoun.qc.entity.QcResultDetail;

@Repository
public interface QcResultDetailRepository extends JpaAttributeConverter<QcResultDetail, String> {

}
