package com.yeoun.pay.service;

import com.yeoun.pay.entity.PayItemMst;
import com.yeoun.pay.repository.PayItemMstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 급여 항목(PayItemMst) 비즈니스 로직 레이어
 * ----------------------------------------------------------------
 * - Controller와 Repository 사이에서 도메인 규칙/트랜잭션을 담당
 * - 데이터 검증, 중복 체크, 예외 변환, 기본값 보정 등은 이 레이어에서 처리
 * - 트랜잭션 기본값은 클래스 레벨 @Transactional 에서 지정
 * ----------------------------------------------------------------
 */
@Service                      // 스프링 컴포넌트 스캔 대상(서비스 빈 등록)
@RequiredArgsConstructor      // final 필드를 파라미터로 받는 생성자 자동 생성(생성자 주입)
@Transactional                // 기본 트랜잭션: 쓰기 가능(READ_COMMITTED; 구현체 기본)
public class PayItemMstService {

    /** Repository (JPA) 의존성
     *  - 실제 DB 접근은 Repository가 수행
     */
    private final PayItemMstRepository payItemMstRepository;

    /**
     * [C/U] 급여 항목 저장/수정
     * ------------------------------------------------------------
     * - PK(itemCode)가 없으면 insert, 있으면 update 동작(JPA 규칙)
     * - 클래스 레벨 @Transactional로 쓰기 트랜잭션이 열려 있음
     * - 도메인 규칙(예: 코드 중복, 카테고리/타입 검증)을 넣으려면 이 메서드에서 처리
     *
     * @param item 저장할 엔티티
     * @return 저장 후 영속 상태 엔티티(식별자 및 변경 컬럼 반영)
     */
    public PayItemMst save(PayItemMst item) {        
        return payItemMstRepository.save(item);
    }

    /**
     * [R] 전체 목록 조회
     * ------------------------------------------------------------
     * - 읽기 전용 트랜잭션: 플러시/더티체킹 비활성화로 성능/안정성 향상
     * - 대량 데이터일 경우 페이지네이션(findAll(Pageable))로 변경 권장
     *
     * @return 전체 급여 항목 리스트
     */
    @Transactional(readOnly = true)
    public List<PayItemMst> findAll() {
        return payItemMstRepository.findAll();
    }

    /**
     * [R] 단건 조회
     * ------------------------------------------------------------
     * - 존재하지 않으면 null 반환(컨트롤러에서 404 변환 필요)
     * - 도메인적으로 '반드시 존재해야' 한다면 NoSuchElement → Custom 예외로 변환 권장
     *
     * @param itemCode 항목 코드(PK)
     * @return 해당 항목 or null
     */
    @Transactional(readOnly = true)
    public PayItemMst find(String itemCode) {
        return payItemMstRepository.findById(itemCode).orElse(null);
    }

    /**
     * [D] 삭제
     * ------------------------------------------------------------
     * - 존재하지 않는 ID를 삭제해도 JPA는 예외를 던지지 않을 수 있음
     *   (필요하면 existsById로 선확인 후 예외 처리)
     * - 참조 무결성(FK) 제약 위반 시 DataIntegrityViolationException 발생 가능
     *
     * @param itemCode 삭제할 PK
     */
    public void delete(String itemCode) {
        // if (!repo.existsById(itemCode)) { throw new NotFoundException(...); }
    	payItemMstRepository.deleteById(itemCode);
    }

    /* ==================== 확장 팁 ====================
     * - validate(PayItemMst item): 코드 포맷/중복/ENUM 유효성 검사
     * - findPage(Pageable pageable): 페이지네이션 조회
     * - soft delete 전략: STATUS 컬럼 추가 후 비활성 처리
     * - 예외 변환: 스프링 DataAccessException → 도메인 예외로 맵핑
     * ================================================= */
}
