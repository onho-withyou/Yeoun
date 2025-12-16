package com.yeoun.masterData.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BOM_HDR")
@Getter
@Setter
public class BomHdr {

    @Id
    @Column(name="BOM_HDR_ID", length = 20, nullable = false)
	private String bomHdrId; //BOM Header ID

    @Column(name="BOM_ID", length = 20, nullable = false)
	private String bomId; //BOMid

    @Column(name="BOM_HDR_NAME", length = 20, nullable = false)
	private String bomHdrName; //BOM Header Name
	
    @Column(name="BOM_HDR_TYPE", length = 20, nullable = false)
	private String bomHdrType; //BOM Header Type

    @Column(name="USE_YN", length = 1)
    private String useYn; //사용여부

    @Column(name="BOM_HDR_DATE")
    private LocalDateTime bomHdrDate; //BOM Header Date

    
}
