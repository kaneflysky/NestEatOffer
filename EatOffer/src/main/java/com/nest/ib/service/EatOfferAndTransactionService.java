package com.nest.ib.service;


import com.alibaba.fastjson.JSONObject;
import com.nest.ib.utils.response.OrdersDetailResponse;
import com.nest.ib.utils.response.QueryExtractServiceChargeResponse;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * ClassName:EatOfferAndTransactionService
 * Description:
 */
public interface EatOfferAndTransactionService {
    // 吃 ERC20 (打入ERC20获得ETH) (ETH数量: 报价ETH + 吃的ETH*0.002)
    String eatErc20(Web3j web3j, Credentials credentials, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigInteger ETH_AMOUNT, BigInteger TOKEN_AMOUNT, String CONTRACT_ADDRESS, BigInteger TRAN_ETH_AMOUNT, BigInteger TRAN_TOKEN_AMOUNT, String TRAN_TOKEN_ADDRESS) throws ExecutionException, InterruptedException;
    // 吃 ETH (打入ETH获得ERC20) (ETH数量: 报价ETH + 打入ETH*0.002 + 打入的ETH)
    String eatEth(Web3j web3j, Credentials credentials, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigInteger ETH_AMOUNT, BigInteger TOKEN_AMOUNT, String CONTRACT_ADDRESS, BigInteger TRAN_ETH_AMOUNT, BigInteger TRAN_TOKEN_AMOUNT, String TRAN_TOKEN_ADDRESS) throws ExecutionException, InterruptedException;
    // 吃报价合约
    void startEatOffer();
    // 充币  (currency: 如ht,btc,usdt...  value:充值金额)
    void deposit(String currency, BigInteger value);
    // 取回
    void retrieveAssets() throws Exception;
    // 转ERC20
    String transferErc20(String ERC20_CONTRACT_ADDRESS, String address, BigInteger value);
    // 转ETH
    String transferEth(String address, BigInteger value);
    // APIv2 币链参考信息
    QueryExtractServiceChargeResponse apiReferenceCurrencies(String currency, String authorizedUser);
    // 市价卖出订单(例如:交易对htusdt,卖出ht获得usdt)
    Long sendSellMarketOrder(String symbol, String amount);
    // 市价买入订单(例如:交易对htusdt,买入ht卖出usdt)
    Long sendBuyMarketOrder(String symbol, String amount);
    // 交易所的操作
    Long exchangeOperation(BigDecimal rightTokenAmount, String rightTokenName, String eatTokenName, BigDecimal leftTokenAmount);
    // 订单详情
    OrdersDetailResponse ordersDetail(String orderId);
    // 更新用户私钥
    String updatePrivateKey(String privateKey);
    // 更新交易所的API-KEY和API-SECRET
    String updateExchangeApiKey(String apiKey,String apiSecret);
    // 开启/关闭 吃单报价
    boolean updateEatOfferState();
    // 开启/关闭 火币交易所
    boolean updateHuobiExchange();
    // 吃单报价资产交易所买卖
    void exchangeBuyAndSell();
    // 设置允许的价格波动比例
    void updatePriceDeviation(int priceDeviation);
    // 页面需要展示数据
    JSONObject eatOfferData();
    // 更换节点
    void updateNode(String urlNode);
    // 查询提币手续费
    String queryExtractServiceCharge(String currency,QueryExtractServiceChargeResponse queryExtractServiceChargeResponse);
    // 是否开启了用户认证
    void updateAuthorizedUser();

}
