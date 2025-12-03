package com.yeoun.inbound.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.inbound.entity.Inbound;

public interface InboundRepository extends JpaRepository<Inbound, String> {

}
