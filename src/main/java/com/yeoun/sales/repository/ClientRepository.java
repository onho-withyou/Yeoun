package com.yeoun.sales.repository;

import com.yeoun.sales.entity.Client;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, String> {

    /** 거래처명 검색 */
    List<Client> findByClientNameContaining(String keyword);

    /** 거래처구분(CUSTOMER / SUPPLIER) 검색 */
    List<Client> findByClientType(String clientType);

    /** 검색박스: 이름 + 유형 동시필터 */
    @Query("""
    	    SELECT c 
    	    FROM Client c 
    	    WHERE (:type IS NULL OR c.clientType = :type)
    	      AND (
    	            :keyword IS NULL OR 
    	            c.clientName LIKE %:keyword% OR
    	            c.businessNo LIKE %:keyword% OR
    	            c.managerName LIKE %:keyword%
    	      )
    	""")
    	List<Client> search(
    	        @Param("keyword") String keyword,
    	        @Param("type") String type
    	);

    /*코드 생성*/
    @Query(value = """
    	    SELECT MAX(c.clientId)
    	    FROM Client c
    	    WHERE c.clientId LIKE :pattern
    	""")
    	String findMaxClientId(@Param("pattern") String pattern);
    
    /*사업자번호 중복체크*/
    boolean existsByBusinessNo(String businessNo);

}
