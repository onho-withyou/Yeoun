package com.yeoun.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yeoun.equipment.entity.ProdEquip;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdEquipRepository extends JpaRepository<ProdEquip, Long> {

    @Query("""
        SELECT e
        FROM ProdEquip e
        WHERE (:equipment IS NULL OR e.equipment.equipId = :equipment)
          AND (:line IS NULL OR e.line.lineId = :line)
          AND (:status IS NULL OR e.status = :status)
        ORDER BY e.createdDate DESC
    """)
    List<ProdEquip> loadProdEquip(
            @Param("equipment") String equipment,
            @Param("line") String line,
            @Param("status") String status
    );
}
