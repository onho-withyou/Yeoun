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
		return messengerMapper.selectUsers(username);
	}

}
