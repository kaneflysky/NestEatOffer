package com.nest.ib.utils.response;

/**
 * ClassName:QueryExtractServiceChargeChains
 * Description:
 */
public class QueryExtractServiceChargeChains {
    private String chain;       // 链名称
    private String depositStatus;   // 充币状态
    private String maxTransactFeeWithdraw;  // 最大单次提币手续费（仅对区间类型和有上限的比例类型有效，withdrawFeeType=circulated or ratio）
    private String maxWithdrawAmt;  // 单次最大提币金额
    private String minDepositAmt;   // 单次最小充币金额
    private String transactFeeWithdraw; // 单次提币手续费（仅对固定类型有效，withdrawFeeType=fixed）
    private String minTransactFeeWithdraw;  // 最小单次提币手续费（仅对区间类型有效，withdrawFeeType=circulated）
    private String transactFeeRateWithdraw; // 单次提币手续费率（仅对比例类型有效，withdrawFeeType=ratio）
    private String minWithdrawAmt;  // 单次最小提币金额
    private String numOfConfirmations;  // 安全上账所需确认次数（达到确认次数后允许提币）
    private String numOfFastConfirmations;  // 快速上账所需确认次数（达到确认次数后允许交易但不允许提币）
    private String withdrawFeeType;     // 提币手续费类型（特定币种在特定链上的提币手续费类型唯一）
    private String withdrawPrecision;   // 提币精度
    private String withdrawQuotaPerDay; // 当日提币额度
    private String withdrawQuotaPerYear;    // 当年提币额度
    private String withdrawQuotaTotal;  // 总提币额度
    private String withdrawStatus;      // 提币状态
    private String instStatus;      // 币种状态

    public String getTransactFeeWithdraw() {
        return transactFeeWithdraw;
    }

    public void setTransactFeeWithdraw(String transactFeeWithdraw) {
        this.transactFeeWithdraw = transactFeeWithdraw;
    }

    public String getTransactFeeRateWithdraw() {
        return transactFeeRateWithdraw;
    }

    public void setTransactFeeRateWithdraw(String transactFeeRateWithdraw) {
        this.transactFeeRateWithdraw = transactFeeRateWithdraw;
    }

    public String getInstStatus() {
        return instStatus;
    }

    public void setInstStatus(String instStatus) {
        this.instStatus = instStatus;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(String depositStatus) {
        this.depositStatus = depositStatus;
    }

    public String getMaxTransactFeeWithdraw() {
        return maxTransactFeeWithdraw;
    }

    public void setMaxTransactFeeWithdraw(String maxTransactFeeWithdraw) {
        this.maxTransactFeeWithdraw = maxTransactFeeWithdraw;
    }

    public String getMaxWithdrawAmt() {
        return maxWithdrawAmt;
    }

    public void setMaxWithdrawAmt(String maxWithdrawAmt) {
        this.maxWithdrawAmt = maxWithdrawAmt;
    }

    public String getMinDepositAmt() {
        return minDepositAmt;
    }

    public void setMinDepositAmt(String minDepositAmt) {
        this.minDepositAmt = minDepositAmt;
    }

    public String getMinTransactFeeWithdraw() {
        return minTransactFeeWithdraw;
    }

    public void setMinTransactFeeWithdraw(String minTransactFeeWithdraw) {
        this.minTransactFeeWithdraw = minTransactFeeWithdraw;
    }

    public String getMinWithdrawAmt() {
        return minWithdrawAmt;
    }

    public void setMinWithdrawAmt(String minWithdrawAmt) {
        this.minWithdrawAmt = minWithdrawAmt;
    }

    public String getNumOfConfirmations() {
        return numOfConfirmations;
    }

    public void setNumOfConfirmations(String numOfConfirmations) {
        this.numOfConfirmations = numOfConfirmations;
    }

    public String getNumOfFastConfirmations() {
        return numOfFastConfirmations;
    }

    public void setNumOfFastConfirmations(String numOfFastConfirmations) {
        this.numOfFastConfirmations = numOfFastConfirmations;
    }

    public String getWithdrawFeeType() {
        return withdrawFeeType;
    }

    public void setWithdrawFeeType(String withdrawFeeType) {
        this.withdrawFeeType = withdrawFeeType;
    }

    public String getWithdrawPrecision() {
        return withdrawPrecision;
    }

    public void setWithdrawPrecision(String withdrawPrecision) {
        this.withdrawPrecision = withdrawPrecision;
    }

    public String getWithdrawQuotaPerDay() {
        return withdrawQuotaPerDay;
    }

    public void setWithdrawQuotaPerDay(String withdrawQuotaPerDay) {
        this.withdrawQuotaPerDay = withdrawQuotaPerDay;
    }

    public String getWithdrawQuotaPerYear() {
        return withdrawQuotaPerYear;
    }

    public void setWithdrawQuotaPerYear(String withdrawQuotaPerYear) {
        this.withdrawQuotaPerYear = withdrawQuotaPerYear;
    }

    public String getWithdrawQuotaTotal() {
        return withdrawQuotaTotal;
    }

    public void setWithdrawQuotaTotal(String withdrawQuotaTotal) {
        this.withdrawQuotaTotal = withdrawQuotaTotal;
    }

    public String getWithdrawStatus() {
        return withdrawStatus;
    }

    public void setWithdrawStatus(String withdrawStatus) {
        this.withdrawStatus = withdrawStatus;
    }
}
