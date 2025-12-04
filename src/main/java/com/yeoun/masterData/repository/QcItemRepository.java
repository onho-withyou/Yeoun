package com.yeoun.masterData.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.QcItem;

@Repository
public interface QcItemRepository extends JpaAttributeConverter<QcItem, String> {

}
