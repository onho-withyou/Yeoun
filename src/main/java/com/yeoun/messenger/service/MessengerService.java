package com.yeoun.messenger.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.messenger.dto.MsgFavoriteDTO;
import com.yeoun.messenger.dto.MsgStatusDTO;
import com.yeoun.messenger.entity.MsgFavorite;
import com.yeoun.messenger.mapper.MessengerMapper;
import com.yeoun.messenger.repository.MsgFavoriteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class MessengerService {
	
	private final MessengerMapper messengerMapper;
	private final MsgFavoriteRepository msgFavoriteRepository;
	private final EmpRepository empRepository;

	// ====================================================
	// 친구 목록을 불러오는 서비스
	public List<MsgStatusDTO> selectUsers(String username) {

		// 메신저 내 최종 상태를 결정
		List<MsgStatusDTO> dtoList = messengerMapper.selectUsers(username);
		for (int i = 0; i < dtoList.size(); i++) {
			if(dtoList.get(i).getOnlineYn().equals("N")){
				dtoList.get(i).setStatus("OFFLINE");	// 오프라인
			} else if (dtoList.get(i).getAvlbStat().equals("ONLINE")){
				dtoList.get(i).setStatus("ONLINE");		// 온라인
			} else if (dtoList.get(i).getAvlbStat().equals("AWAY")){
				dtoList.get(i).setStatus("AWAY");		// 자리비움
			} else {
				dtoList.get(i).setStatus("BUSY");		// 다른 용무중
			}
		}

		return dtoList;
	}

	// ========================================================
	// 즐겨찾기 추가
	public void insertFavorite(MsgFavoriteDTO msgFavoriteDTO) {
		
		log.info("★★★★★★★★★★★★★★★★★★★★★★★ 즐겨찾기 추가 진입....");
		
		Emp empId = empRepository.findByEmpId(msgFavoriteDTO.getEmpId())
				.orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다"));
		Emp fvUser = empRepository.findByEmpId(msgFavoriteDTO.getFvUser())
				.orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다"));
		
		MsgFavorite msgFavorite = msgFavoriteDTO.toEntity(empId, fvUser);
		msgFavoriteRepository.save(msgFavorite);
	}

	// ========================================================
	// 즐겨찾기 여부 확인
	public boolean searchFavorite(MsgFavoriteDTO msgFavoriteDTO) {
		log.info("★★★★★★★★★★★★★★★★★★★★★★★ 즐겨찾기 확인 진입....");
		return msgFavoriteRepository.existsByEmpId_EmpIdAndFvUser_EmpId(msgFavoriteDTO.getEmpId(), msgFavoriteDTO.getFvUser());
	}

	// ========================================================
	// 즐겨찾기 삭제
	@Transactional
	public boolean deleteFavorite(MsgFavoriteDTO msgFavoriteDTO) {
		log.info("★★★★★★★★★★★★★★★★★★★★★★★ 즐겨찾기 삭제 진입....");
		return msgFavoriteRepository
				.deleteByEmpId_EmpIdAndFvUser_EmpId(msgFavoriteDTO.getEmpId(), msgFavoriteDTO.getFvUser()) > 0;
	}

}
