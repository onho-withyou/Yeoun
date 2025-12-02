package com.yeoun.sales.service;

import com.yeoun.sales.entity.Client;
import com.yeoun.sales.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    /** 전체 목록 */
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    /** 검색 */
    public List<Client> search(String name, String type) {
        if ((name == null || name.isBlank()) && (type == null || type.isBlank()))
            return clientRepository.findAll();
        return clientRepository.search(name, type);
    }

    /** 상세조회 */
    public Client get(String clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }

    /** ===============================
     *   신규 등록 (필수값+중복검증 포함)
     * =============================== */
    public Client create(Client client) {

        /* ✅ 1. 필수값 검증 */
        if (client.getClientType() == null || client.getClientType().isBlank())
            throw new IllegalArgumentException("유형은 필수입니다.");

        if (client.getClientName() == null || client.getClientName().isBlank())
            throw new IllegalArgumentException("거래처명은 필수입니다.");

        if (client.getBusinessNo() == null || client.getBusinessNo().isBlank())
            throw new IllegalArgumentException("사업자번호는 필수입니다.");

        /* ✅ 2. 사업자번호 중복 확인 */
        if (clientRepository.existsByBusinessNo(client.getBusinessNo()))
            throw new IllegalArgumentException("이미 등록된 사업자번호입니다.");

        /* ✅ 3. 거래처 ID 자동 생성 */
        client.setClientId(generateClientId(client.getClientType()));

        /* ✅ 4. 기본 상태값 설정 */
        client.setStatusCode("ACTIVE");

        /* ✅ 5. 생성자/일시 */
        client.setCreatedAt(LocalDateTime.now());
        client.setCreatedBy("SYSTEM");  // 로그인 사용자로 변경 가능

        /* 기본값 보정 */
        if (client.getFaxNumber() == null) client.setFaxNumber("");
        if (client.getManagerTel() == null) client.setManagerTel("");
        if (client.getAccountName() == null) client.setAccountName("");

        return clientRepository.save(client);
    }


    /** ===========================
     *   수정
     * =========================== */
    public Client update(String clientId, Client updateForm) {

        Client origin = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("거래처 없음"));

        // 필드 갱신
        origin.setClientName(updateForm.getClientName());
        origin.setClientType(updateForm.getClientType());
        origin.setBusinessNo(updateForm.getBusinessNo());
        origin.setCeoName(updateForm.getCeoName());
        origin.setManagerName(updateForm.getManagerName());
        origin.setManagerDept(updateForm.getManagerDept());
        origin.setManagerTel(updateForm.getManagerTel());
        origin.setManagerEmail(updateForm.getManagerEmail());
        origin.setPostCode(updateForm.getPostCode());
        origin.setAddr(updateForm.getAddr());
        origin.setAddrDetail(updateForm.getAddrDetail());
        origin.setFaxNumber(updateForm.getFaxNumber());
        origin.setBankName(updateForm.getBankName());
        origin.setAccountName(updateForm.getAccountName());
        origin.setAccountNumber(updateForm.getAccountNumber());
        origin.setStatusCode(updateForm.getStatusCode());

        // 수정일/수정자
        origin.setUpdatedAt(LocalDateTime.now());
        origin.setUpdatedBy("SYSTEM");

        return clientRepository.save(origin);
    }


    /* ===========================
        ID 생성 규칙
        CUS20251202-0001
        VEN20251202-0001
       ===========================*/
    public String generateClientId(String type) {

        String prefix = type.equals("SUPPLIER") ? "VEN" : "CUS";  
        String date = java.time.LocalDate.now().toString().replace("-", "");
        String pattern = prefix + date + "-%";

        /* 오늘 날짜의 최대 seq 조회 */
        String maxId = clientRepository.findMaxClientId(pattern);

        int nextSeq = 1;

        if (maxId != null) {
            String seqStr = maxId.substring(maxId.lastIndexOf("-") + 1); // 0001
            nextSeq = Integer.parseInt(seqStr) + 1;
        }

        String seqStr = String.format("%04d", nextSeq);

        return prefix + date + "-" + seqStr;
    }

    /* 사업자번호 중복 체크 API용 */
    public boolean existsBusinessNo(String businessNo) {
        return clientRepository.existsByBusinessNo(businessNo);
    }
}
