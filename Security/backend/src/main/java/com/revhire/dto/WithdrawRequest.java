package com.revhire.dto;

import jakarta.validation.constraints.Size;

public class WithdrawRequest {

    private Boolean confirm;

    @Size(max = 250)
    private String reason;

    public Boolean getConfirm() {
        return confirm;
    }

    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
