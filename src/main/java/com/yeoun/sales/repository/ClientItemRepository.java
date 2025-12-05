package com.yeoun.sales.repository;

import com.yeoun.sales.entity.ClientItem;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientItemRepository extends JpaRepository<ClientItem, Long> {

	// itemId로 ClientItem 조회
	Optional<ClientItem> findByItemId(Long itemId);
}
