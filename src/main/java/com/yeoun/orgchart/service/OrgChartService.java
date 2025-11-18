package com.yeoun.orgchart.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.orgchart.dto.OrgNodeDTO;
import com.yeoun.orgchart.repository.OrgChartRepository;
import com.yeoun.orgchart.repository.OrgNodeProjection;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrgChartService {
	
	private final OrgChartRepository orgChartRepository;
	
	// 조직도 전체 데이터 조회
	public List<OrgNodeDTO> getOrgTree() {
		
		List<OrgNodeProjection> rows = orgChartRepository.findOrgNodes();
		
		rows.sort(Comparator.comparing(
					OrgNodeProjection::getPosOrder,
					Comparator.nullsLast(Integer::compareTo)
				  ).reversed());
		
		return rows.stream()
	                .map(r -> {
	                    OrgNodeDTO orgNodeDTO = new OrgNodeDTO();
	                    orgNodeDTO.setDeptId(r.getDeptId());
	                    orgNodeDTO.setParentDeptId(r.getParentDeptId());
	                    orgNodeDTO.setDeptName(r.getDeptName());
	                    orgNodeDTO.setPosName(r.getPosName());
	                    orgNodeDTO.setEmpName(r.getEmpName());
	                    return orgNodeDTO;
	                })
	                .toList();
	    }
}