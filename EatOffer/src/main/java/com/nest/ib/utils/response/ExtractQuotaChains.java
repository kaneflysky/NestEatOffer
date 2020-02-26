package com.nest.ib.utils.response;

import java.io.Serializable;

/**
 * ClassName:ExtractQuotaChains
 * Description:
 */
public class ExtractQuotaChains implements Serializable {
    private String chain;        // 链名称
    private String maxWithdrawAmt;   // 单次最大提币金额
    private String withdrawQuotaPerDay;  // 当日提币额度
    private String remainWithdrawQuotaPerDay;    // 当日提币剩余额度
    private String withdrawQuotaPerYear;     // 当年提币额度
    private String remainWithdrawQuotaPerYear;   // 当年提币剩余额度
    private String withdrawQuotaTotal;       // 总提币额度
    private String remainWithdrawQuotaTotal; // 总提币剩余额度

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getMaxWithdrawAmt() {
        return maxWithdrawAmt;
    }

    public void setMaxWithdrawAmt(String maxWithdrawAmt) {
        this.maxWithdrawAmt = maxWithdrawAmt;
    }

    public String getWithdrawQuotaPerDay() {
        return withdrawQuotaPerDay;
    }

    public void setWithdrawQuotaPerDay(String withdrawQuotaPerDay) {
        this.withdrawQuotaPerDay = withdrawQuotaPerDay;
    }

    public String getRemainWithdrawQuotaPerDay() {
        return remainWithdrawQuotaPerDay;
    }

    public void setRemainWithdrawQuotaPerDay(String remainWithdrawQuotaPerDay) {
        this.remainWithdrawQuotaPerDay = remainWithdrawQuotaPerDay;
    }

    public String getWithdrawQuotaPerYear() {
        return withdrawQuotaPerYear;
    }

    public void setWithdrawQuotaPerYear(String withdrawQuotaPerYear) {
        this.withdrawQuotaPerYear = withdrawQuotaPerYear;
    }

    public String getRemainWithdrawQuotaPerYear() {
        return remainWithdrawQuotaPerYear;
    }

    public void setRemainWithdrawQuotaPerYear(String remainWithdrawQuotaPerYear) {
        this.remainWithdrawQuotaPerYear = remainWithdrawQuotaPerYear;
    }

    public String getWithdrawQuotaTotal() {
        return withdrawQuotaTotal;
    }

    public void setWithdrawQuotaTotal(String withdrawQuotaTotal) {
        this.withdrawQuotaTotal = withdrawQuotaTotal;
    }

    public String getRemainWithdrawQuotaTotal() {
        return remainWithdrawQuotaTotal;
    }

    public void setRemainWithdrawQuotaTotal(String remainWithdrawQuotaTotal) {
        this.remainWithdrawQuotaTotal = remainWithdrawQuotaTotal;
    }

    @Override
    public String toString() {
        return "ExtractQuotaChains{" +
                "chain='" + chain + '\'' +
                ", maxWithdrawAmt='" + maxWithdrawAmt + '\'' +
                ", withdrawQuotaPerDay='" + withdrawQuotaPerDay + '\'' +
                ", remainWithdrawQuotaPerDay='" + remainWithdrawQuotaPerDay + '\'' +
                ", withdrawQuotaPerYear='" + withdrawQuotaPerYear + '\'' +
                ", remainWithdrawQuotaPerYear='" + remainWithdrawQuotaPerYear + '\'' +
                ", withdrawQuotaTotal='" + withdrawQuotaTotal + '\'' +
                ", remainWithdrawQuotaTotal='" + remainWithdrawQuotaTotal + '\'' +
                '}';
    }
}
