package com.yeoun.messenger.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.messenger.dto.MsgStatusDTO;
import com.yeoun.messenger.mapper.MessengerMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessengerService {
	
	private final MessengerMapper messengerMapper;

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

}
