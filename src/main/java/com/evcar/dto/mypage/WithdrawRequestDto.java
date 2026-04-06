package com.evcar.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawRequestDto {

    private String password;
    private String withdrawReason;
    private String withdrawReasonDetail;
    private Boolean agreeWithdraw;

    public boolean isInvalid() {
        if (password == null || password.isBlank()) {
            return true;
        }

        if (withdrawReason == null || withdrawReason.isBlank()) {
            return true;
        }

        if (isOtherReason() && (withdrawReasonDetail == null || withdrawReasonDetail.isBlank())) {
            return true;
        }

        return !Boolean.TRUE.equals(agreeWithdraw);
    }

    public boolean isOtherReason() {
        return "기타".equals(withdrawReason);
    }

    public String getFinalWithdrawReason() {
        if (isOtherReason()) {
            return withdrawReasonDetail != null ? withdrawReasonDetail.trim() : "";
        }
        return withdrawReason != null ? withdrawReason.trim() : "";
    }
}