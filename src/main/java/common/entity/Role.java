package common.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ROLE")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Role {
	
	// 역할코드
	@Id
	@Column(name = "ROLE_CODE", length = 20, nullable = false)
	private String roleCode;
	
	// 역할명
	@Column(name = "ROLE_NAME", length = 50, nullable = false)
	private String roleName;
	
	// 역할설명
	@Column(name = "ROLE_DESC", length = 200)
	private String roleDesc;
	
	// 사용여부
	@Column(name = "USE_YN", length = 1, nullable = false)
	private String useYn = "Y";
	
	// 등록일시
	@CreatedDate
	@Column(name = "CREATED_DATE", nullable = false, updatable = false)
	private LocalDateTime createdDate;
	
}
