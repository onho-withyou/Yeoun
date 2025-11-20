package com.yeoun.approval.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApproverId implements Serializable {
    private Long approvalId;
    private String empId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApproverId that = (ApproverId) o;
        return approvalId.equals(that.approvalId) && empId.equals(that.empId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(approvalId, empId);
    }
    
}
