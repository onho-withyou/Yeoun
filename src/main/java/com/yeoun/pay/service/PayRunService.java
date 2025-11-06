package com.yeoun.pay.service;

import com.yeoun.pay.dto.PayRunDTO;
import com.yeoun.pay.entity.PayRun;
import com.yeoun.pay.repository.PayRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayRunService {

    private final PayRunRepository repo;

    public Page<PayRun> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Optional<PayRun> findByIdOptional(Long id) {
        return repo.findById(id);
    }

    public PayRun findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("PayRun not found: " + id));
    }

    @Transactional
    public PayRun save(PayRunDTO dto) {
        PayRun entity = toEntity(dto);
        PayRun saved = repo.save(entity);
        log.info("PayRun saved. runId={}", saved.getRunId());
        return saved;
    }

    @Transactional
    public PayRun update(Long id, PayRunDTO dto) {
        PayRun cur = findById(id);

        if (dto.getCalcType() != null)   cur.setCalcType(dto.getCalcType());
        if (dto.getStatus() != null)     cur.setStatus(dto.getStatus());
        if (dto.getReqUser() != null)    cur.setReqUser(dto.getReqUser());
        if (dto.getStartedDate() != null)cur.setStartedDate(dto.getStartedDate());
        if (dto.getEndedDate() != null)  cur.setEndedDate(dto.getEndedDate());

        PayRun updated = repo.save(cur);
        log.info("PayRun updated. runId={}", updated.getRunId());
        return updated;
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("PayRun not found: " + id);
        repo.deleteById(id);
        log.info("PayRun deleted. runId={}", id);
    }

    private PayRun toEntity(PayRunDTO dto) {
        return PayRun.builder()
                .calcType(dto.getCalcType())
                .status(dto.getStatus())
                .reqUser(dto.getReqUser())
                .startedDate(dto.getStartedDate())
                .endedDate(dto.getEndedDate())
                .build();
    }
}
