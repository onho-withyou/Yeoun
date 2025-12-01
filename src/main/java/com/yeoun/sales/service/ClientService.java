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

    /** 전체 목록 조회 */
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    /** 검색 조건 조회 */
    public List<Client> search(String name, String type) {
        if ((name == null || name.isBlank()) && (type == null || type.isBlank())) {
            return clientRepository.findAll();
        }
        return clientRepository.search(name, type);
    }

    /** 상세조회 */
    public Client get(String clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }

    /** 신규 등록 */
    /** 신규 등록 */
    public Client create(Client client) {

        // 1) 거래처 ID 생성
        if (client.getClientId() == null || client.getClientId().isBlank()) {
            client.setClientId(generateClientId(client.getClientType()));
        }

        // 2) 기본 상태값 설정
        if (client.getStatusCode() == null || client.getStatusCode().isBlank()) {
            client.setStatusCode("ACTIVE");   // 기본 활성 상태
        }

        // 3) 생성일시
        client.setCreatedAt(LocalDateTime.now());

        // 4) 기본값 보정
        if (client.getAccountName() == null) {
            client.setAccountName(client.getCeoName()); // 또는 빈값 허용
        }

        if (client.getFaxNumber() == null) {
            client.setFaxNumber("");
        }

        if (client.getManagerTel() == null) {
            client.setManagerTel("");
        }

        // 5) 저장
        return clientRepository.save(client);
    }

    /** 수정 */
    public Client update(String clientId, Client updateForm) {
        Client origin = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("거래처 없음"));

        origin.setClientName(updateForm.getClientName());
        origin.setClientType(updateForm.getClientType());
        origin.setBusinessNo(updateForm.getBusinessNo());
        origin.setCeoName(updateForm.getCeoName());
        origin.setManagerName(updateForm.getManagerName());
        origin.setManagerDept(updateForm.getManagerDept());      
        origin.setManagerEmail(updateForm.getManagerEmail());
        origin.setPostCode(updateForm.getPostCode());
        origin.setAddr(updateForm.getAddr());
        origin.setAddrDetail(updateForm.getAddrDetail());
        origin.setFaxNumber(updateForm.getFaxNumber());
        origin.setBankName(updateForm.getBankName());
        origin.setAccountName(updateForm.getAccountName());
        origin.setAccountNumber(updateForm.getAccountNumber());
        origin.setStatusCode(updateForm.getStatusCode());

        return clientRepository.save(origin);
    }

    /** 거래처ID 생성 규칙 */
    private String generateClientId(String type) {

        String prefix = type.equals("SUPPLIER") ? "SUP" : "CUS";

        String date = java.time.LocalDate.now().toString().replace("-", "");

        int seq = (int) (Math.random() * 9000) + 1000;

        return prefix + date + seq;
    }
}
