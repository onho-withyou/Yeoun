package com.yeoun.qc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.qc.entity.QcResultDetail;

@Repository
public interface QcResultDetailRepository extends JpaRepository<QcResultDetail, String> {

}
