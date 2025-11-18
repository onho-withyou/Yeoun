package com.yeoun.orgchart.repository;

// JPA에서 엔티티 전체가 아닌 특정 컬럼만 골라서 조회하고 싶을 때 사용하는 기능
public interface OrgNodeProjection {
	
	String getDeptId();
	String getParentDeptId();
	String getDeptName();
	String getPosName();
	String getEmpName();

}
