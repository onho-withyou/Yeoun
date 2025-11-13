package com.yeoun.messenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MsgFavoriteId implements Serializable {

    @Column(name = "EMP_ID")
    private String empId;

    @Column(name = "FV_USER")
    private String fvUser;

    public MsgFavoriteId() {}

    public MsgFavoriteId(String empId, String fvUser) {
        this.empId = empId;
        this.fvUser = fvUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MsgFavoriteId)) return false;
        MsgFavoriteId other = (MsgFavoriteId) obj;
        return Objects.equals(empId, other.empId) &&
                Objects.equals(fvUser, other.fvUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empId, fvUser);
    }
}





