package com.nest.ib.service.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nest.ib.contract.ERC20;
import com.nest.ib.contract.OfferContract;
import com.nest.ib.service.EatOfferAndTransactionService;
import com.nest.ib.utils.HttpClientUtil;
import com.nest.ib.utils.api.ApiClient;
import com.nest.ib.utils.api.JsonUtil;
import com.nest.ib.utils.request.CreateOrderRequest;
import com.nest.ib.utils.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * ClassName:EatOfferAndTransactionServiceImpl
 * Description:
 */
@Service
public class EatOfferAndTransactionServiceImpl implements EatOfferAndTransactionService {
    private static final Logger logger = LoggerFactory.getLogger(EatOfferAndTransactionServiceImpl.class);
    private String API_KEY = "";
    private String API_SECRET = "";
    // 离正常价格偏移的百分比
    private BigDecimal UP_PRICE_DEVIATION = new BigDecimal("1.1"); // 110%
    private BigDecimal DOWN_PRICE_DEVIATION = new BigDecimal("0.9"); // 90%
    private static final BigDecimal UNIT_ETH = new BigDecimal("1000000000000000000");
    private static final BigDecimal UNIT_USDT = new BigDecimal("1000000");
    private String NODE = "https://mainnet.infura.io/v3/3f9b5d82819144ad959c992c94bcb107";
    private String USER_PRIVATE_KEY = "";
    // 报价工厂API: 来源于etherscan
    private static String ETHERSCAN_OFFER_CONTRACT_API = "https://api-cn.etherscan.com/api?module=account&action=txlist&address=0x4F391C202a906EED9e2b63fDd387F28E952782E2&startblock=0&endblock=99999999&page=1&offset=10&sort=desc&apikey=YourApiKeyToken";
    // ETH/USDT价格: 来源于火币交易所API
    private static final String URL_ETH_USDT_PRICE = "https://api.huobi.pro/market/history/trade?symbol=ethusdt&size=1";
    // HT/ETH价格
    private static final String URL_HT_ETH_PRICE = "https://api.huobi.pro/market/history/trade?symbol=hteth&size=1";
    // 报价工厂合约地址
    private static final String OFFER_FACTORY_CONTRACT = "0x4F391C202a906EED9e2b63fDd387F28E952782E2";
    // USDT代币合约地址
    private static final String USDT_TOKEN_ADDRESS = "0xdac17f958d2ee523a2206206994597c13d831ec7";
    // HT代币合约地址
    private static final String HT_TOKEN_ADDRESS = "0x6f259637dcd74c767781e37bc6133cd6a68aa161";
    // 报价操作的input数据
    private static final String INPUT_OFFER = "0xf6a4932f";
    // 报价详情日志
    private static final String TRANSACTION_TOPICS_CONTRACT = "0xccacfd869caa3e2e845afe470f00dcb777e77639814c6c96bb320b69885e63ce";
    // 是否开启吃单报价
    private boolean START_EAT_OFFER = false;
    // 是否开启交易所
    private boolean START_HUOBI_EXCHANGE = false;
    // 交易所状态：0无任务，1.有任务，2.充值到交易所对应钱包，3.发送交易对的买卖订单，4. 提现
    private int EXCHANGE_STATE = 0;
    // 买入TOKEN名字
    private String BUY_TOKEN_NAME_EXCHANGE = "";
    // 卖出TOKEN名字
    private String SELL_TOKEN_NAME_EXCHANGE = "";
    // 卖出TOKEN数量
    private BigInteger SELL_TOKEN_VALUE = new BigInteger("0");
    // 订单号
    private long ORDER_ID = 123L;
    // 吃单报价hash
    private String EAT_OFFER_TRANSACTION_HASH = "";
    // 交易所是否进行了用户认证
    private String AUTHORIZED_USER = "true";
    /**
     *  开启吃单报价
     */
    @Override
    public void startEatOffer() {
        // 检测是否开启吃单报价
        if (!START_EAT_OFFER) {
            return;
        }
        // 查看是否设置了私钥
        if (USER_PRIVATE_KEY.equalsIgnoreCase("")) {
            System.out.println("请先设置私钥，再开启吃单报价");
            return;
        }
        // 检查是否授权
        Web3j web3j = Web3j.build(new HttpService(NODE));
        try {
            approveUsdtToOfferFactoryContract(web3j);
        } catch (Exception e) {
            System.out.println("USDT授权失败");
            return;
        }
        try {
            approveHtToOfferFactoryContract(web3j);
        } catch (Exception e) {
            System.out.println("HT授权失败");
        }
        // 开启吃单报价
        try {
            eatOffer(web3j);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    /**
     *  吃单报价(吃ETH或者ERC20)
     */
    private void eatOffer(Web3j web3j) throws Exception {
        /**
        *   通过etherscan Api获取所有的报价合约数据
        */
        String s = HttpClientUtil.sendHttpGet("https://api-cn.etherscan.com/api?module=account&action=txlist&address=0x4F391C202a906EED9e2b63fDd387F28E952782E2&startblock=0&endblock=99999999&page=1&offset=10&sort=desc&apikey=YourApiKeyToken");
        JSONObject jsonObject = JSONObject.parseObject(s);
        JSONArray resultEtherscan = jsonObject.getJSONArray("result");
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(new BigInteger("3"));
        BigInteger gasLimit = new BigInteger("2000000");
        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
        BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        List<String> listContractAddress = new ArrayList();
        for(int i=0; i<resultEtherscan.size(); i++) {
            Object o = resultEtherscan.get(i);
            JSONObject jsonObject1 = JSONObject.parseObject(String.valueOf(o));
            String from = jsonObject1.getString("from");
            String isError = jsonObject1.getString("isError");
            String input = jsonObject1.getString("input");
            String hash = jsonObject1.getString("hash");
            BigInteger offerBlockNumber = jsonObject1.getBigInteger("blockNumber");
            // 如果该报价合约距离当前区块，大于等于25个，说明已经价格生效，无法吃单
            if( (blockNumber.subtract(offerBlockNumber)).compareTo(new BigInteger("24")) > 0){
                return;
            }
            if (input.length() < 10) continue;
            if (input.substring(0, 10).equalsIgnoreCase(INPUT_OFFER) && isError.equalsIgnoreCase("0")) {
                EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(hash).sendAsync().get();
                TransactionReceipt result = ethGetTransactionReceipt.getResult();
                List<Log> logs = result.getLogs();
                if (logs.size() == 0) return;
                // 遍历当前transactionHash下所有的日志记录
                for (Log log : logs) {
                    List<String> topics = log.getTopics();
                    String address = log.getAddress();
                    if (!address.equalsIgnoreCase(OFFER_FACTORY_CONTRACT)) {
                        continue;        // 确定一定要是报价工厂合约地址,才能继续往下执行
                    }
                    // 如果有报价记录
                    if (topics.get(0).equalsIgnoreCase(TRANSACTION_TOPICS_CONTRACT)) {
                        String data = log.getData();
                        // 报价合约地址
                        String contractAddress = "0x" + data.substring(26, 66);
                        // erc20代币合约地址
                        String erc20ContractAddress = "0x" + data.substring(90, 130);
                        // 报价ETH数量
                        BigInteger ethAmount = new BigInteger(data.substring(130, 194), 16);
                        // 报价USDT数量
                        BigInteger erc20Amount = new BigInteger(data.substring(194, 258), 16);
                        // 创建报价合约对象
                        Tuple3<BigInteger, BigInteger, String> send = OfferContract.load(contractAddress, web3j, credentials, gasPrice, gasLimit).checkDealAmount().send();
                        // 剩余可成交ETH数量
                        BigInteger value1 = send.getValue1();
                        // 剩余可成交ERC20数量
                        BigInteger value2 = send.getValue2();
                        // ERC20 TOKEN地址
                        String value3 = send.getValue3();
                        // 吃单同时报价ETH数量
                        BigInteger offerEthAmount = value1.multiply(new BigInteger("2"));
                        // 如果有一边资产剩余资产为0，那么说明该报价合约已经被吃掉了
                        if(value1.compareTo(new BigInteger("0"))==0 || value2.compareTo(new BigInteger("0"))==0)continue;
                        /**
                         *  区分ETH/HT 和 ETH/USDT报价
                         */
                        // erc20的交易所实时价格
                        BigDecimal erc20ExchangePrice;
                        // token的精度
                        BigDecimal UNIT_TOKEN;
                        if(erc20ContractAddress.equalsIgnoreCase(HT_TOKEN_ADDRESS)){
                            try{
                                erc20ExchangePrice = getEthAndHtExchangePrice();
//                                System.out.println("交易所HT/ETH价格： " + erc20ExchangePrice);
                            }catch (Exception e){
                                System.out.println("连接火币API失败，请先开启全局VPN");
                                return;
                            }
                            UNIT_TOKEN = UNIT_ETH;
                        }else if(erc20ContractAddress.equalsIgnoreCase(USDT_TOKEN_ADDRESS)){
                            try{
                                erc20ExchangePrice = getEthAndUsdtExchangePrice();
//                                System.out.println("交易所USDT/ETH价格： " + erc20ExchangePrice);
                            }catch (Exception e){
                                System.out.println("连接火币API失败，请先开启全局VPN");
                                return;
                            }
                            UNIT_TOKEN = UNIT_USDT;
                        }else {
                            System.out.println("TOKEN地址有问题");
                            return;
                        }
                        // 向上偏移后的价格
                        BigDecimal upExchangePrice = erc20ExchangePrice.multiply(UP_PRICE_DEVIATION).setScale(2,BigDecimal.ROUND_DOWN);
                        // 向下偏移后的价格
                        BigDecimal downExchangePrice = erc20ExchangePrice.multiply(DOWN_PRICE_DEVIATION).setScale(2,BigDecimal.ROUND_DOWN);
                        // 报价合约价格
                        BigDecimal offerPrice = (new BigDecimal(erc20Amount).divide(UNIT_TOKEN)).divide(new BigDecimal(ethAmount).divide(UNIT_ETH), 2, BigDecimal.ROUND_DOWN);
                        // 吃单同时报价ERC20数量
                        BigInteger offerErc20Amount = new BigInteger(String.valueOf(new BigDecimal(offerEthAmount).divide(UNIT_ETH).multiply(upExchangePrice).multiply(UNIT_TOKEN).setScale(0, BigDecimal.ROUND_DOWN)));
                        // 如果有一边资产剩余资产为0，那么说明该报价合约已经被吃掉了
                        if(value1.compareTo(new BigInteger("0"))==0 || value2.compareTo(new BigInteger("0"))==0)continue;
                        // 如果价格超过向上偏移价格，那么打入ETH，吃掉ERC20
                        if(offerPrice.compareTo(upExchangePrice) > 0){
                            String eatOfferTransactionHash = eatErc20(web3j, credentials, nonce, gasPrice, gasLimit,
                                    offerEthAmount,
                                    offerErc20Amount,
                                    contractAddress,
                                    value1,
                                    value2,
                                    value3);
                            if(eatOfferTransactionHash != null){
                                System.out.println("吃单报价hash: " + eatOfferTransactionHash);
                                Thread.sleep(1000*60*5);    // 休眠避免一笔报价被重复吃单
                            }
                            return;
                        }
                        // 如果价格超过向下偏移价格，那么打入ERC20，吃掉ETH
                        if(offerPrice.compareTo(downExchangePrice) < 0){
                            String eatOfferTransactionHash = eatEth(web3j, credentials, nonce, gasPrice, gasLimit,
                                    offerEthAmount,
                                    offerErc20Amount,
                                    contractAddress,
                                    value1,
                                    value2,
                                    value3);
                            if(eatOfferTransactionHash != null){
                                System.out.println("吃单报价hash: " + eatOfferTransactionHash);
                                Thread.sleep(1000*60*5);    // 休眠避免一笔报价被重复吃单
                            }
                            return;
                        }
                    }
                }
            }
        }

    }


    /**
     * 充值(ETH或者ERC20)
     *
     */
    @Override
    public void deposit(String currency,BigInteger value) {
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        DepositAddressResponse depositAddress = client.getDepositAddress(currency);
        int code = depositAddress.getCode();
        String address = null; // 充值地址
        // 获取充值地址
        if(code == 200){
            List<DepositAddressBean> data = depositAddress.getData();
            if(data.size() == 1){
                DepositAddressBean depositAddressBean = data.get(0);
                address = depositAddressBean.getAddress();
            }
            if(data.size() > 1){
                for (DepositAddressBean depositAddressBean : data) {
                    if(depositAddressBean.getChain().equalsIgnoreCase("usdterc20")){
                        address = depositAddressBean.getAddress();
                    }
                }
            }
        }
        if(address == null || currency == null)return;
        // 充值
        if(currency.equalsIgnoreCase("ETH")){
            String transactionHash = transferEth(address, value);// 转ETH
            System.out.println("ETH转账hash：" + transactionHash);
        }else {
            String tokenAddress;
            if(currency.equalsIgnoreCase("HT")){
                tokenAddress = HT_TOKEN_ADDRESS;
            }else {
                tokenAddress = USDT_TOKEN_ADDRESS;
            }
            String transactionHash = transferErc20(tokenAddress, address, value);// 转ERC20
            System.out.println("ERC20转账hash：" + transactionHash);
        }
    }
    /**
     *
     *ETH吃单(打入ETH,获取ERC20)
     */
    @Override
    public String eatErc20(Web3j web3j,
                           Credentials credentials,
                           BigInteger nonce,
                           BigInteger gasPrice,
                           BigInteger gasLimit,
                           BigInteger ETH_AMOUNT,
                           BigInteger TOKEN_AMOUNT,
                           String CONTRACT_ADDRESS,
                           BigInteger TRAN_ETH_AMOUNT,
                           BigInteger TRAN_TOKEN_AMOUNT,
                           String TRAN_TOKEN_ADDRESS) throws ExecutionException, InterruptedException {

        // ETH数量: 报价ETH + 吃的ETH*0.002 + 打入的ETH
        BigDecimal multiply = new BigDecimal(TRAN_ETH_AMOUNT).multiply(new BigDecimal("1.002"));
        BigInteger PAYABLE_ETH = new BigInteger(String.valueOf(new BigDecimal(ETH_AMOUNT).add(multiply).setScale(0, BigDecimal.ROUND_DOWN)));
        try {
            // ETH余额
            BigInteger ethBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
            // ERC20余额
            ERC20 erc20 = ERC20.load(TRAN_TOKEN_ADDRESS,web3j,credentials,gasPrice,gasLimit);
            BigInteger ercBalance = erc20.balanceOf(credentials.getAddress()).send();
            /**
             *  如果资产不够全部吃掉，并且账户余额足够吃掉10ETH，那么就吃10ETH的
             */
            if(ethBalance.compareTo(PAYABLE_ETH) <= 0 || ercBalance.compareTo(TOKEN_AMOUNT) < 0){
                // 区分TOKEN，精度不同
                BigDecimal UNIT_TOKEN = new BigDecimal("0");
                if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(HT_TOKEN_ADDRESS)){
                    UNIT_TOKEN = UNIT_ETH;
                }else if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(USDT_TOKEN_ADDRESS)) {
                    UNIT_TOKEN = UNIT_USDT;
                }else {
                    System.out.println("该TOKEN地址暂不支持：" + TRAN_TOKEN_ADDRESS);
                    return null;
                }
                // 当前交易所价格ERC20/ETH(一个ETH相当于多少ERC20)
                BigDecimal exchangePrice = new BigDecimal(TOKEN_AMOUNT).divide(UNIT_TOKEN,18,BigDecimal.ROUND_DOWN).divide(new BigDecimal(ETH_AMOUNT).divide(UNIT_ETH,0,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
                // 报价合约产生的价格(一个ETH相当于多少ERC20)
                BigDecimal offerPrice = new BigDecimal(TRAN_TOKEN_AMOUNT).divide(UNIT_TOKEN,18,BigDecimal.ROUND_DOWN).divide(new BigDecimal(TRAN_ETH_AMOUNT).divide(UNIT_ETH,0,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
                // 报价ETH数量
                ETH_AMOUNT = new BigInteger(String.valueOf(new BigDecimal("20").multiply(UNIT_ETH)));
                // 报价ERC20数量
                TOKEN_AMOUNT = new BigInteger(String.valueOf(exchangePrice.multiply(new BigDecimal("20")).multiply(UNIT_TOKEN).setScale(0,BigDecimal.ROUND_DOWN)));
                // 吃掉ETH数量
                TRAN_ETH_AMOUNT = new BigInteger(String.valueOf(new BigDecimal("10").multiply(UNIT_ETH)));
                // 吃掉ERC20数量
                TRAN_TOKEN_AMOUNT = new BigInteger(String.valueOf(offerPrice.multiply(new BigDecimal("10")).multiply(UNIT_TOKEN).setScale(0,BigDecimal.ROUND_DOWN)));
                // 最小吃单的总支付ETH数量：20.02 * 10的18次方
                multiply = new BigDecimal(TRAN_ETH_AMOUNT).multiply(new BigDecimal("1.002"));
                PAYABLE_ETH = new BigInteger(String.valueOf(new BigDecimal(ETH_AMOUNT).add(multiply).setScale(0, BigDecimal.ROUND_DOWN)));
                if(ethBalance.compareTo(PAYABLE_ETH) <= 0 || ercBalance.compareTo(TOKEN_AMOUNT) < 0){
                    System.out.println("请充值资产，最小吃单金额不够");
                    return null;
                }
            }
            /**
             *   查看钱包资产吃单是否足够，如果不够能吃多少吃多少
             */
            /*if(ethBalance.compareTo(PAYABLE_ETH) <= 0 || ercBalance.compareTo(TOKEN_AMOUNT) < 0){
                logger.info("吃单时账户余额不够");
                // HT和USDT精度不一样，需要区分token并设置精度
                BigDecimal UNIT_TOKEN;
                if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(USDT_TOKEN_ADDRESS)){
                    UNIT_TOKEN = UNIT_USDT;
                }else if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(HT_TOKEN_ADDRESS)){
                    UNIT_TOKEN = UNIT_ETH;
                }else {
                    System.out.println("获取到的代币合约地址有问题");
                    return null;
                }
                // 获取USDT/ETH价格
                BigDecimal price = new BigDecimal(TOKEN_AMOUNT).divide(UNIT_TOKEN,18,BigDecimal.ROUND_DOWN).divide(new BigDecimal(ETH_AMOUNT).divide(UNIT_ETH,0,BigDecimal.ROUND_DOWN), 18, BigDecimal.ROUND_DOWN);
                // 获取账户USDT换算成ETH的数量
                BigDecimal usdtExchangeEth = new BigDecimal(ercBalance).divide(price, 18, BigDecimal.ROUND_DOWN);
                // 如果钱包余额ERC20连最小10ETH吃单都不支持，不需要执行任何操作了
                if(usdtExchangeEth.compareTo(new BigDecimal("20").multiply(UNIT_ETH)) < 0){
                    if(UNIT_TOKEN.compareTo(UNIT_ETH) == 0){
                        System.out.println("账户HT不够，需要补充资产才能吃单");
                    }
                    if(UNIT_TOKEN.compareTo(UNIT_USDT) == 0){
                        System.out.println("账户USDT不够，需要补充资产才能吃单");
                    }
                    return null;
                }
                *//**
                 * 设置吃ETH数量为X，那么完成吃单报价，需要ETH：3.002*X, 需要HT: 价值 2*X 的ETH， 那么 ETH/HT = 3.002/2
                 *//*
                BigInteger eth = new BigInteger("0");
                // 如果账户USDT换算成ETH后乘以3/2，比账户ETH余额多，那么吃单就按照账户ETH的余额数量来算，保持吃单的最大数量
                if( (usdtExchangeEth.multiply(new BigDecimal("3.002")).divide(new BigDecimal("2"),18,BigDecimal.ROUND_DOWN)).compareTo(new BigDecimal(ethBalance)) >= 0 ){
                    // 吃10ETH为一份，求出最多能吃多少份(ETH余额需要减去打包的矿工费)
                    eth = (ethBalance.subtract(new BigInteger("9000000000000000"))).divide(new BigInteger("30" + "020000000000000000"));
                }else {
                    // 吃单不需要ERC20，报价需要双倍，ERC20余额转化为ETH数量，除以20即可得到最多能够报价多少份（10ETH为1份）
                    eth = new BigInteger(String.valueOf(usdtExchangeEth.divide(new BigDecimal("20000000000000000000"), 0, BigDecimal.ROUND_DOWN)));
                }
                // 如果最多能吃单份数为0，说明资产不够
                if(eth.compareTo(new BigInteger("0")) <= 0){
                    System.out.println("账户ETH资产不够，需要补充资产");
                    return null;
                }else {
                    // 报价ETH数量
                    ETH_AMOUNT = eth.multiply(new BigInteger("20")).multiply(new BigInteger(String.valueOf(UNIT_ETH)));
                    // 报价ERC20数量
                    TOKEN_AMOUNT = new BigInteger(String.valueOf(new BigDecimal(eth).multiply(price).multiply(UNIT_TOKEN).multiply(new BigDecimal("20"))));
                    // 吃掉ETH数量
                    TRAN_ETH_AMOUNT = ETH_AMOUNT.divide(new BigInteger("2"));
                    // 吃掉ERC20数量
                    TRAN_TOKEN_AMOUNT = TOKEN_AMOUNT.divide(new BigInteger("2"));
                    // ETH数量: 报价ETH + 吃的ETH*0.002 + 打入的ETH
                    multiply = new BigDecimal(TRAN_ETH_AMOUNT).multiply(new BigDecimal("1.002"));
                    PAYABLE_ETH = new BigInteger(String.valueOf(new BigDecimal(ETH_AMOUNT).add(multiply).setScale(0, BigDecimal.ROUND_DOWN)));
                }
            }*/
        } catch (Exception e) {
            return null;
        }
        logger.info("报价ETH: " + ETH_AMOUNT);
        logger.info("报价TOKEN: " + TOKEN_AMOUNT);
        logger.info("要吃的合约地址: " + CONTRACT_ADDRESS);
        logger.info("吃掉ETH数量: " + TRAN_ETH_AMOUNT);
        logger.info("吃掉ERC20数量: " + TRAN_TOKEN_AMOUNT);
        logger.info("ERC20合约地址: " + TRAN_TOKEN_ADDRESS);
        logger.info("吃单报价总支付ETH: " + PAYABLE_ETH);
        Function function = new Function(
                "ethTran",
                Arrays.<Type>asList(
                        new Uint256(ETH_AMOUNT),
                        new Uint256(TOKEN_AMOUNT),
                        new Address(CONTRACT_ADDRESS),
                        new Uint256(TRAN_ETH_AMOUNT),
                        new Uint256(TRAN_TOKEN_AMOUNT),
                        new Address(TRAN_TOKEN_ADDRESS)
                ),
                Collections.<TypeReference<?>>emptyList());
        String encode = FunctionEncoder.encode(function);
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                OFFER_FACTORY_CONTRACT,
                PAYABLE_ETH,
                encode);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
        if(transactionHash == null){
            return null;
        }
        // 吃单报价后，存储状态，用来交易所进行买卖
        if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(HT_TOKEN_ADDRESS)){
            SELL_TOKEN_NAME_EXCHANGE = "ht";
        }else {
            SELL_TOKEN_NAME_EXCHANGE = "usdt";
        }
        BUY_TOKEN_NAME_EXCHANGE = "eth";
        SELL_TOKEN_VALUE = TRAN_TOKEN_AMOUNT;
        EXCHANGE_STATE = 1;
        EAT_OFFER_TRANSACTION_HASH = transactionHash;
        System.out.println("需要卖出TOKEN：" + SELL_TOKEN_NAME_EXCHANGE);
        System.out.println("需要买入TOKEN：" + BUY_TOKEN_NAME_EXCHANGE);
        System.out.println("卖出TOKEN数量：" + SELL_TOKEN_VALUE);
        System.out.println("吃单hash：" + EAT_OFFER_TRANSACTION_HASH);
        return transactionHash;
    }
    /**
     *  ERC20吃单(打入ERC20,获取ETH)
     *
     */
    @Override
    public String eatEth(Web3j web3j,
                         Credentials credentials,
                         BigInteger nonce,
                         BigInteger gasPrice,
                         BigInteger gasLimit,
                         BigInteger ETH_AMOUNT,
                         BigInteger TOKEN_AMOUNT,
                         String CONTRACT_ADDRESS,
                         BigInteger TRAN_ETH_AMOUNT,
                         BigInteger TRAN_TOKEN_AMOUNT,
                         String TRAN_TOKEN_ADDRESS) throws ExecutionException, InterruptedException {
        // ETH数量: 报价ETH + 吃的ETH*0.002
        BigDecimal multiply = new BigDecimal(TRAN_ETH_AMOUNT).multiply(new BigDecimal("0.002"));
        BigInteger PAYABLE_ETH = new BigInteger(String.valueOf(new BigDecimal(ETH_AMOUNT).add(multiply).setScale(0, BigDecimal.ROUND_DOWN)));
        // 验证ETH和ERC20的余额是否足够
        try {
            // ETH余额
            BigInteger ethBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
            // ERC20余额
            ERC20 erc20 = ERC20.load(TRAN_TOKEN_ADDRESS,web3j,credentials,gasPrice,gasLimit);
            BigInteger ercBalance = erc20.balanceOf(credentials.getAddress()).send();
            if(ethBalance.compareTo(PAYABLE_ETH) <= 0 || ercBalance.compareTo(TOKEN_AMOUNT.add(TRAN_TOKEN_AMOUNT)) < 0){
                logger.info("吃单时账户余额不够全吃掉");
                // 设置token精度
                BigDecimal UNIT_TOKEN;
                if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(USDT_TOKEN_ADDRESS)){
                    UNIT_TOKEN = UNIT_USDT;
                }else if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(HT_TOKEN_ADDRESS)){
                    UNIT_TOKEN = UNIT_ETH;
                }else {
                    System.out.println("获取到的代币合约地址暂不支持：" + TRAN_TOKEN_ADDRESS);
                    return null;
                }
                // 当前交易所价格
                BigDecimal exchangePrice = new BigDecimal(TOKEN_AMOUNT).divide(UNIT_TOKEN,18,BigDecimal.ROUND_DOWN).divide(new BigDecimal(ETH_AMOUNT).divide(UNIT_ETH,0,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
                // 报价合约产生价格
                BigDecimal offerPrice = new BigDecimal(TRAN_TOKEN_AMOUNT).divide(UNIT_TOKEN,18,BigDecimal.ROUND_DOWN).divide(new BigDecimal(TRAN_ETH_AMOUNT).divide(UNIT_ETH,0,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
                /**
                *   如果资产不够，就只吃10ETH的量
                */
                // 报价ETH数量
                ETH_AMOUNT = new BigInteger(String.valueOf(new BigDecimal("20").multiply(UNIT_ETH)));
                // 报价ERC20数量
                TOKEN_AMOUNT = new BigInteger(String.valueOf(exchangePrice.multiply(new BigDecimal("20")).multiply(UNIT_TOKEN).setScale(0,BigDecimal.ROUND_DOWN)));
                // 吃掉ETH数量
                TRAN_ETH_AMOUNT = new BigInteger(String.valueOf(new BigDecimal("10").multiply(UNIT_ETH)));
                // 吃掉ERC20数量
                TRAN_TOKEN_AMOUNT = new BigInteger(String.valueOf(offerPrice.multiply(new BigDecimal("10")).multiply(UNIT_TOKEN).setScale(0,BigDecimal.ROUND_DOWN)));
                // ETH数量: 报价ETH + 吃的ETH*0.002
                multiply = new BigDecimal(TRAN_ETH_AMOUNT).multiply(new BigDecimal("0.002"));
                PAYABLE_ETH = new BigInteger(String.valueOf(new BigDecimal(ETH_AMOUNT).add(multiply).setScale(0, BigDecimal.ROUND_DOWN)));
                if(ethBalance.compareTo(PAYABLE_ETH) <= 0 || ercBalance.compareTo(TRAN_TOKEN_AMOUNT.add(TOKEN_AMOUNT)) < 0){
                    System.out.println("请充值资产，最小吃单金额不够");
                    return null;
                }

                /**
                 *   由于打入ERC20,获取ETH，涉及到两个价格：
                 *                      1. 要吃的报价合约ETH/HT产生的价格
                 *                      2. 当前交易所ETH/HT产生的价格
                 *   计算方式为：
                 *      1. 计算出吃掉最小单位需要的ETH数量m和ERC20数量n
                 *      2. 用账户余额的ETH和ERC20分别除以m和n，可以得到ETH和ERC20最多能支持吃下的份数
                 *      3. 选择份数最小的进行吃单报价
                */
                /*//  吃掉10ETH，总共需要的ETH数量
                BigInteger eatOfferEthAmount =  new BigInteger("20020000000000000000");
                //  钱包ETH余额最大支持份数(1份为吃掉10ETH)
                BigInteger ethCopies = ethBalance.divide(eatOfferEthAmount);
                //  吃掉10ETH，报价需要的ERC20数量
                BigInteger offerErc20Amount = TOKEN_AMOUNT.multiply(new BigInteger("20")).divide(ETH_AMOUNT.divide(new BigInteger(String.valueOf(UNIT_ETH))));
                //  吃掉10ETH，吃需要打入的ERC20数量
                BigDecimal divide = new BigDecimal(TRAN_TOKEN_AMOUNT).divide(UNIT_TOKEN, 18, BigDecimal.ROUND_DOWN).divide(new BigDecimal(TRAN_ETH_AMOUNT).divide(UNIT_ETH, 0, BigDecimal.ROUND_DOWN), 18, BigDecimal.ROUND_DOWN);
                BigInteger eatErc20Amount = new BigInteger(String.valueOf(divide.multiply(new BigDecimal("10"))));
                //  吃掉10ETH,总共需要的ERC20数量
                BigInteger eatOfferErc20Amount = offerErc20Amount.add(eatErc20Amount);
                //  钱包ERC20余额最大支持份数
                BigInteger erc20Copies = ercBalance.divide(eatOfferErc20Amount);
                if( ethCopies.compareTo(new BigInteger("0"))==0){
                    System.out.println("钱包ETH不够，请增加资产");
                    return null;
                }
                if( erc20Copies.compareTo(new BigInteger("0"))==0 ){
                    if (UNIT_TOKEN.compareTo(UNIT_ETH) == 0){
                        System.out.println("钱包HT不够，请增加资产");
                    }
                    if(UNIT_TOKEN.compareTo(UNIT_USDT) == 0){
                        System.out.println("钱包USDT不够，请增加资产");
                    }
                    return null;
                }
                // 吃掉10ETH为一份，总共需要吃掉的份数
                BigInteger copies;
                // 对比ETH和ERC20支持份数，取最小支持份数
                if(ethCopies.compareTo(erc20Copies) < 0){
                    copies = ethCopies;
                }else {
                    copies = erc20Copies;
                }
                // 报价ETH数量
                ETH_AMOUNT = copies.multiply(new BigInteger("20000000000000000000"));
                // 报价ERC20数量
                TOKEN_AMOUNT = copies.multiply(offerErc20Amount);
                // 吃掉ETH数量
                TRAN_ETH_AMOUNT = copies.multiply(new BigInteger("20000000000000000"));
                // 吃掉ERC20数量
                TRAN_TOKEN_AMOUNT = copies.multiply(eatErc20Amount);
                // ETH数量: 报价ETH + 吃的ETH*0.002
                multiply = new BigDecimal(TRAN_ETH_AMOUNT).multiply(new BigDecimal("0.002"));
                PAYABLE_ETH = new BigInteger(String.valueOf(new BigDecimal(ETH_AMOUNT).add(multiply).setScale(0, BigDecimal.ROUND_DOWN)));
                */
            }
        } catch (Exception e) {
            return null;
        }
        logger.info("报价ETH: " + ETH_AMOUNT);
        logger.info("报价TOKEN: " + TOKEN_AMOUNT);
        logger.info("要吃的合约地址: " + CONTRACT_ADDRESS);
        logger.info("吃掉ETH数量: " + TRAN_ETH_AMOUNT);
        logger.info("吃掉ERC20数量: " + TRAN_TOKEN_AMOUNT);
        logger.info("ERC20合约地址: " + TRAN_TOKEN_ADDRESS);
        logger.info("吃单报价总支付ETH: " + PAYABLE_ETH);
        Function function = new Function(
                "ercTran",
                Arrays.<Type>asList(
                        new Uint256(ETH_AMOUNT),
                        new Uint256(TOKEN_AMOUNT),
                        new Address(CONTRACT_ADDRESS),
                        new Uint256(TRAN_ETH_AMOUNT),
                        new Uint256(TRAN_TOKEN_AMOUNT),
                        new Address(TRAN_TOKEN_ADDRESS)
                ),
                Collections.<TypeReference<?>>emptyList());
        String encode = FunctionEncoder.encode(function);
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                OFFER_FACTORY_CONTRACT,
                PAYABLE_ETH,
                encode);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
        if(transactionHash == null){
            return null;
        }
        // 吃单报价后，存储状态，用来交易所进行买卖
        if(TRAN_TOKEN_ADDRESS.equalsIgnoreCase(HT_TOKEN_ADDRESS)){
            BUY_TOKEN_NAME_EXCHANGE = "ht";
        }else {
            BUY_TOKEN_NAME_EXCHANGE = "usdt";
        }
        SELL_TOKEN_NAME_EXCHANGE = "eth";
        SELL_TOKEN_VALUE = TRAN_ETH_AMOUNT;
        EXCHANGE_STATE = 1;
        EAT_OFFER_TRANSACTION_HASH = transactionHash;
        System.out.println("需要卖出TOKEN：" + SELL_TOKEN_NAME_EXCHANGE);
        System.out.println("需要买入TOKEN：" + BUY_TOKEN_NAME_EXCHANGE);
        System.out.println("卖出TOKEN数量：" + SELL_TOKEN_VALUE);
        System.out.println("吃单hash：" + EAT_OFFER_TRANSACTION_HASH);
        return transactionHash;
    }
    /**
     *  取回报价合约资产(发布报价过了25个区块)
     *
     */
    @Override
    public void retrieveAssets() throws Exception {
        if(USER_PRIVATE_KEY.equalsIgnoreCase("")){
            System.out.println("取回操作需要设置私钥");
            return;
        }
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        String s = HttpClientUtil.sendHttpGet("https://api-cn.etherscan.com/api?module=account&action=txlist&address=0x4F391C202a906EED9e2b63fDd387F28E952782E2&startblock=0&endblock=99999999&page=1&offset=20&sort=desc&apikey=YourApiKeyToken");
        JSONObject jsonObject = JSONObject.parseObject(s);
        JSONArray resultEtherscan = jsonObject.getJSONArray("result");
        Web3j web3j = Web3j.build(new HttpService(NODE));
        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigDecimal multiply = new BigDecimal(gasPrice).multiply(new BigDecimal("2"));
        gasPrice = new BigInteger(String.valueOf(multiply.setScale(0,BigDecimal.ROUND_DOWN)));
        for(int i=0; i<resultEtherscan.size(); i++) {
            Object o = resultEtherscan.get(i);
            JSONObject jsonObject1 = JSONObject.parseObject(String.valueOf(o));
            String from = jsonObject1.getString("from");
            String isError = jsonObject1.getString("isError");
            String input = jsonObject1.getString("input");
            String hash = jsonObject1.getString("hash");
            String blockNumber = jsonObject1.getString("blockNumber");
            if (input.length() < 10) continue;
            if ( (input.substring(0, 10).equalsIgnoreCase("0xf6a4932f") || input.substring(0,10).equalsIgnoreCase("0x4139c74c")) && from.equalsIgnoreCase(credentials.getAddress()) && isError.equalsIgnoreCase("0")) {
                EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(hash).sendAsync().get();
                TransactionReceipt result = ethGetTransactionReceipt.getResult();
                List<Log> logs = result.getLogs();
                if (logs.size() == 0) return;
                // 遍历当前transactionHash下所有的日志记录
                for (Log log : logs) {
                    List<String> topics = log.getTopics();
                    String address = log.getAddress();
                    if (!address.equalsIgnoreCase(OFFER_FACTORY_CONTRACT)) {
//                       logger.info("该地址不是报价工厂合约地址: " + address);
                        continue;        // 确定一定要是报价工厂合约地址,才能继续往下执行
                    }
                    // 如果有报价记录
                    if (topics.get(0).equalsIgnoreCase(TRANSACTION_TOPICS_CONTRACT)) {
                        String data = log.getData();
                        String contractAddress = "0x" + data.substring(26, 66);  // 报价合约地址
                        // 检查是否领取过
                        OfferContract offerContract = OfferContract.load(contractAddress, web3j, credentials, gasPrice, new BigInteger("1000000"));
                        Boolean b = offerContract.checkHadReceive().send();
                        // 如果还未领取
                        if (!b) {
                            // 当前区块高度
                            BigInteger nowBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
                            // 如果当前区块高度 - 上一笔报价区块高度 <= 25 ,那么此时是无法取回的
                            if( (nowBlockNumber.subtract(new BigInteger(blockNumber))).compareTo(new BigInteger("25")) <= 0) {
                                continue;
                            }
                            BigInteger gasLimit = new BigInteger("500000");
                            Function function = new Function(
                                    "turnOut",
                                    Arrays.<Type>asList(new Address(contractAddress)),
                                    Collections.<TypeReference<?>>emptyList());
                            String encode = FunctionEncoder.encode(function);
                            RawTransaction rawTransaction = RawTransaction.createTransaction(
                                    nonce,
                                    gasPrice,
                                    gasLimit,
                                    OFFER_FACTORY_CONTRACT,
                                    encode);
                            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                            String hexValue = Numeric.toHexString(signedMessage);
                            String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
                            System.out.println("报价合约取回hash： " + transactionHash);
                            Thread.sleep(1000*60*5);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     *  转账ERC20
     *
     */
    @Override
    public String transferErc20(String ERC20_CONTRACT_ADDRESS, String address, BigInteger value) {
        Web3j web3j = Web3j.build(new HttpService(NODE));
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        try {
            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            gasPrice = gasPrice.multiply(new BigInteger("3"));
            BigInteger gasLimit = new BigInteger("200000");
            if(address != null){
                final Function function = new Function(
                        "transfer",
                        Arrays.<Type>asList(new Address(address),
                                new Uint256(value)),
                        Collections.<TypeReference<?>>emptyList());
                String encode = FunctionEncoder.encode(function);
                RawTransaction rawTransaction = RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        ERC20_CONTRACT_ADDRESS,
                        encode);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,credentials);
                String hexValue = Numeric.toHexString(signedMessage);
                String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
                return transactionHash;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    /**
     *  转账ETH
     *
     */
    @Override
    public String transferEth(String address, BigInteger value) {
        Web3j web3j = Web3j.build(new HttpService(NODE));
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        try{
            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            gasPrice = gasPrice.multiply(new BigInteger("3"));
            BigInteger gasLimit = new BigInteger("200000");
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, address, value);
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            return ethSendTransaction.getTransactionHash();
        }catch (Exception e){
            return null;
        }

    }
    /**
     *  此节点用于查询各币种及其所在区块链的静态参考信息（公共数据）
     *  currency: 币种, authorizedUser: 用户是否认证
     */
    @Override
    public QueryExtractServiceChargeResponse apiReferenceCurrencies(String currency, String authorizedUser) {
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        String s = client.queryExtractServiceCharge(currency, authorizedUser);
        JSONObject jsonObject = JSONObject.parseObject(s);
        int code = jsonObject.getIntValue("code");
        if(code != 200)return null;
        JSONArray data = jsonObject.getJSONArray("data");
        List<QueryExtractServiceChargeData> list = new ArrayList<>();
        data.forEach(da->{
            JSONObject jsonObject1 = JSONObject.parseObject(String.valueOf(da));
            String currency1 = jsonObject1.getString("currency");
            String instStatus = jsonObject1.getString("instStatus");
            JSONArray chains = jsonObject1.getJSONArray("chains");
            List<QueryExtractServiceChargeChains> chainsList = new ArrayList<>();
            chains.forEach(tx->{
                JSONObject jsonObject2 = JSONObject.parseObject(String.valueOf(tx));
                QueryExtractServiceChargeChains queryExtractServiceChargeChains = new QueryExtractServiceChargeChains();
                queryExtractServiceChargeChains.setChain(jsonObject2.getString("chain"));
                queryExtractServiceChargeChains.setDepositStatus(jsonObject2.getString("depositStatus"));
                queryExtractServiceChargeChains.setMaxTransactFeeWithdraw(jsonObject2.getString("maxTransactFeeWithdraw"));
                queryExtractServiceChargeChains.setMaxWithdrawAmt(jsonObject2.getString("maxWithdrawAmt"));
                queryExtractServiceChargeChains.setMinDepositAmt(jsonObject2.getString("minDepositAmt"));
                queryExtractServiceChargeChains.setMinTransactFeeWithdraw(jsonObject2.getString("minTransactFeeWithdraw"));
                queryExtractServiceChargeChains.setMinWithdrawAmt(jsonObject2.getString("minWithdrawAmt"));
                queryExtractServiceChargeChains.setNumOfConfirmations(jsonObject2.getString("numOfConfirmations"));
                queryExtractServiceChargeChains.setNumOfFastConfirmations(jsonObject2.getString("numOfFastConfirmations"));
                queryExtractServiceChargeChains.setWithdrawFeeType(jsonObject2.getString("withdrawFeeType"));
                queryExtractServiceChargeChains.setWithdrawPrecision(jsonObject2.getString("withdrawPrecision"));
                queryExtractServiceChargeChains.setWithdrawQuotaPerDay(jsonObject2.getString("withdrawQuotaPerDay"));
                queryExtractServiceChargeChains.setWithdrawQuotaPerYear(jsonObject2.getString("withdrawQuotaPerYear"));
                queryExtractServiceChargeChains.setWithdrawQuotaTotal(jsonObject2.getString("withdrawQuotaTotal"));
                queryExtractServiceChargeChains.setWithdrawStatus(jsonObject2.getString("withdrawStatus"));
                queryExtractServiceChargeChains.setTransactFeeWithdraw(jsonObject2.getString("transactFeeWithdraw"));
                queryExtractServiceChargeChains.setTransactFeeRateWithdraw(jsonObject2.getString("transactFeeRateWithdraw"));
                queryExtractServiceChargeChains.setInstStatus("instStatus");
                chainsList.add(queryExtractServiceChargeChains);
            });
            QueryExtractServiceChargeData queryExtractServiceChargeData = new QueryExtractServiceChargeData();
            queryExtractServiceChargeData.setChains(chainsList);
            queryExtractServiceChargeData.setCurrency(currency1);
            queryExtractServiceChargeData.setInstStatus(instStatus);
            list.add(queryExtractServiceChargeData);
        });
        QueryExtractServiceChargeResponse queryExtractServiceChargeResponse = new QueryExtractServiceChargeResponse();
        queryExtractServiceChargeResponse.setCode(code);
        queryExtractServiceChargeResponse.setData(list);
        return queryExtractServiceChargeResponse;
    }
    /**
     *  市价卖出订单(例如:交易对htusdt,卖出ht获得usdt)
     */
    @Override
    public Long sendSellMarketOrder(String symbol,String amount) {
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        AccountsResponse accounts = client.accounts();
        Long orderId = 123L;
        List<Accounts> list = (List<Accounts>) accounts.getData();
        Accounts account = list.get(0);
        long accountId = account.getId();
        // create order:
        CreateOrderRequest createOrderReq = new CreateOrderRequest();
        createOrderReq.accountId = String.valueOf(accountId);
//        createOrderReq.amount = "0.1";
        createOrderReq.amount=amount;
//            createOrderReq.price = "0.1";
//        createOrderReq.symbol = "htusdt";
        createOrderReq.symbol = symbol;
        createOrderReq.type = CreateOrderRequest.OrderType.SELL_MARKET; // 市价卖出
        createOrderReq.source = "api";
        //------------------------------------------------------ 创建订单  -------------------------------------------------------
        try {
            orderId = client.createOrder(createOrderReq);
        }catch (Exception e){
            logger.info("发起卖单出现异常");
            return 123L;
        }
        logger.info("订单id: " + orderId);
        return orderId;
    }
    /**
     *  市价买入订单(例如:交易对htusdt,买入ht卖出usdt)
     */
    @Override
    public Long sendBuyMarketOrder(String symbol, String amount) {
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        AccountsResponse accounts = client.accounts();
        Long orderId = 123L;
        List<Accounts> list = (List<Accounts>) accounts.getData();
        Accounts account = list.get(0);
        long accountId = account.getId();
        // create order:
        CreateOrderRequest createOrderReq = new CreateOrderRequest();
        createOrderReq.accountId = String.valueOf(accountId);
//        createOrderReq.amount = "0.1";
        createOrderReq.amount=amount;
//            createOrderReq.price = "0.1";
//        createOrderReq.symbol = "htusdt";
        createOrderReq.symbol = symbol;
        createOrderReq.type = CreateOrderRequest.OrderType.BUY_MARKET; // 市价卖出
        createOrderReq.source = "api";
        //------------------------------------------------------ 创建订单  -------------------------------------------------------
        try{
            orderId = client.createOrder(createOrderReq);
        }catch (Exception e){
            logger.info("买起买单出现异常");
            return 123L;
        }
        logger.info("订单id: " + orderId);
        return orderId;
    }

    /**
     *  火币交易所 -> 进行买卖操作
     * @return
     */
    @Override
    public Long exchangeOperation(BigDecimal rightTokenAmount, String rightTokenName, String eatTokenName, BigDecimal leftTokenAmount) {
        Long orderId = 123L;
        // 如果吃ETH(打入的ETH,获取ERC20), 交易所需要卖出ERC20,获取ETH
        if(eatTokenName.equalsIgnoreCase("ETH")){
            if(rightTokenName.equalsIgnoreCase("HT")){
                orderId = sendSellMarketOrder("hteth", String.valueOf(rightTokenAmount.divide(UNIT_ETH, 2, BigDecimal.ROUND_DOWN)));
            }
            if(rightTokenName.equalsIgnoreCase("USDT")){
                orderId = sendBuyMarketOrder("ethusdt", String.valueOf(rightTokenAmount.divide(UNIT_USDT, 2, BigDecimal.ROUND_DOWN)));
            }
        }
        // 如果吃ERC20(打入的ERC20,获取ETH), 交易所需要卖出ETH,获取ERC20
        // 如果是HT
        if(eatTokenName.equalsIgnoreCase("HT")){
            orderId = sendBuyMarketOrder("hteth", String.valueOf(leftTokenAmount.divide(UNIT_ETH, 2, BigDecimal.ROUND_DOWN)));
        }
        // 如果是USDT
        if(eatTokenName.equalsIgnoreCase("USDT")){
            orderId = sendSellMarketOrder("ethusdt", String.valueOf(leftTokenAmount.divide(UNIT_ETH, 2, BigDecimal.ROUND_DOWN)));
        }
        return orderId;
    }
    /**
     *  订单详情
     */
    @Override
    public OrdersDetailResponse ordersDetail(String orderId) {
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        OrdersDetailResponse ordersDetail = client.ordersDetail(orderId);
        return ordersDetail;
    }
    /**
     *  修改钱包私钥
     */
    @Override
    public String updatePrivateKey(String privateKey) {
        Credentials credentials = Credentials.create(privateKey);
        String address = credentials.getAddress();
        USER_PRIVATE_KEY = privateKey;
        return address;
    }
    /**
    *   修改交易所API-KEY和API-SECRET
    */
    @Override
    public String updateExchangeApiKey(String apiKey, String apiSecret) {
        API_KEY = apiKey;
        API_SECRET = apiSecret;
        return "SUCCESS";
    }
    /**
    *   修改吃单报价状态
    */
    @Override
    public boolean updateEatOfferState() {
        if(START_EAT_OFFER){
            START_EAT_OFFER = false;
        }else {
            START_EAT_OFFER = true;
        }
        return START_EAT_OFFER;
    }
    /**
    *   修改火币交易所交易状态
    */
    @Override
    public boolean updateHuobiExchange() {
        if(START_HUOBI_EXCHANGE){
            START_HUOBI_EXCHANGE = false;
        }else {
            START_HUOBI_EXCHANGE = true;
        }
        return START_HUOBI_EXCHANGE;    }
    /**
    *   吃单报价的资产进行交易所买卖（状态：0无任务，1.有任务，2.已完成充值到交易所对应钱包，3.已完成发送交易对的订单，4. 提现）
    */
    @Override
    public void exchangeBuyAndSell() {

        // 检查是否开启了交易所买卖
        if(!START_HUOBI_EXCHANGE){
            return;
        }
        // 查看是否设置了交易所的API-KEY
        if (API_KEY.equalsIgnoreCase("") || API_SECRET.equalsIgnoreCase("")) {
            System.out.println("如果吃单报价后资产需要去交易所兑换，请设置交易所的API-KEY，并开放充提权限");
            return;
        }
        // 无任务
        if(EXCHANGE_STATE == 0){
            return;
        }
        // 如果有任务
        if(EXCHANGE_STATE == 1){
            logger.info("接到充值到交易所任务");
            // 查看吃单报价是否hash是否已经完成
            Web3j web3j = Web3j.build(new HttpService(NODE));
            TransactionReceipt transactionReceipt = null;
            try {
                transactionReceipt = web3j.ethGetTransactionReceipt(EAT_OFFER_TRANSACTION_HASH).send().getTransactionReceipt().get();
            } catch (IOException e) {
                System.out.println("查询交易hash出现错误");
                return;
            }
            String status = transactionReceipt.getStatus();
            // 如果交易成功,则进行充值
            if(status.equalsIgnoreCase("0x1")){
                try {
                    logger.info("充值前休眠20分钟");
                    // 刚接到任务，资产还没来得及取回，避免跟取回操作互相覆盖
                    Thread.sleep(1000*60*20);
                } catch (InterruptedException e) {
                    return;
                }
                String transactionHash = eatOfferRecharge();
                if(transactionHash != null){
                    System.out.println("已经充值到交易所，交易hash：" + transactionHash);
                    // 充值完成修改状态
                    EAT_OFFER_TRANSACTION_HASH = "";
                    EXCHANGE_STATE = 2;
                    return;
                }
            }
            // 如果交易失败,那么删除该任务
            if(status.equalsIgnoreCase("0x0")){
                BUY_TOKEN_NAME_EXCHANGE = "";
                SELL_TOKEN_NAME_EXCHANGE = "";
                SELL_TOKEN_VALUE = new BigInteger("0");
                EXCHANGE_STATE = 0;
                EAT_OFFER_TRANSACTION_HASH = "";
            }
            return;
        }
        // 如果充值已经完成
        if(EXCHANGE_STATE == 2){
            logger.info("进入买卖");
            // TOKEN精度
            BigDecimal UNIT_TOKEN;
            if(SELL_TOKEN_NAME_EXCHANGE.equalsIgnoreCase("ht") || SELL_TOKEN_NAME_EXCHANGE.equalsIgnoreCase("eth")){
                UNIT_TOKEN = UNIT_ETH;
            }else if(SELL_TOKEN_NAME_EXCHANGE.equalsIgnoreCase("usdt")){
                UNIT_TOKEN = UNIT_USDT;
            }else {
                System.out.println("此TOKEN暂不支持：" + SELL_TOKEN_NAME_EXCHANGE);
                return;
            }
            // 获取交易所所有有资产的token
            HashMap<String, String> exchangeBalance = getExchangeBalance();
            // 要卖的资产余额
            String exchangeToken = exchangeBalance.get(SELL_TOKEN_NAME_EXCHANGE);
            BigDecimal tokenBalance = new BigDecimal(exchangeToken).multiply(UNIT_TOKEN);
            // 交易所资产是否足够
            if(tokenBalance.compareTo(new BigDecimal(SELL_TOKEN_VALUE)) >= 0){
                // 订单号
                long orderId = 123L;
                // 发起买卖订单
                if(SELL_TOKEN_NAME_EXCHANGE.equalsIgnoreCase("eth")){
                    orderId = exchangeOperation(new BigDecimal("0"), SELL_TOKEN_NAME_EXCHANGE, BUY_TOKEN_NAME_EXCHANGE, new BigDecimal(SELL_TOKEN_VALUE));
                }else {
                    orderId = exchangeOperation(new BigDecimal(SELL_TOKEN_VALUE), BUY_TOKEN_NAME_EXCHANGE, SELL_TOKEN_NAME_EXCHANGE, new BigDecimal("0"));
                }
                logger.info("买卖订单ID：" + orderId);
                if(orderId != 123L){
                    ORDER_ID = orderId;
                    EXCHANGE_STATE = 3;
                }
            }
            return;
        }
        // 确认订单是否完成
        if(EXCHANGE_STATE == 3){
            OrdersDetailResponse ordersDetailResponse = ordersDetail(String.valueOf(ORDER_ID));
            if(ordersDetailResponse == null)return;
            String status = ordersDetailResponse.getStatus();
            // 如果订单已经完成
            if(status.equalsIgnoreCase("ok")){
                /**
                *   进行提币操作
                */
                // 查询币链信息
                QueryExtractServiceChargeResponse queryExtractServiceChargeResponse = apiReferenceCurrencies(BUY_TOKEN_NAME_EXCHANGE, AUTHORIZED_USER);
                // 查询提币手续费
                String serviceCharge = queryExtractServiceCharge(BUY_TOKEN_NAME_EXCHANGE, queryExtractServiceChargeResponse);
                // 链名称，默认以太坊
                String chain = null;
                ApiClient client = new ApiClient(API_KEY, API_SECRET);
                // 查询账户要提取的TOKEN余额
                HashMap<String, String> exchangeBalance = getExchangeBalance();
                String balance = exchangeBalance.get(BUY_TOKEN_NAME_EXCHANGE);
                if(new BigDecimal(balance).compareTo(new BigDecimal("1")) <= 0){
                    System.out.println("账户资金还没到账，不提取");
                    return;
                }
                // 提币数量(余额减手续费)
                String amount = String.valueOf(new BigDecimal(balance).subtract(new BigDecimal(serviceCharge)));
                // 发送提币请求
                ExtractERC20Response extractERC20Response = client.extractERC20(
                        Credentials.create(USER_PRIVATE_KEY).getAddress(),
                        amount,
                        BUY_TOKEN_NAME_EXCHANGE,
                        serviceCharge,
                        chain);
                long data = extractERC20Response.getData();
                if(data == 0){
                    System.out.println("请在交易所提币列表地址中设置添加提币钱包地址");
                    try {
                        Thread.sleep(1000*60*60);
                    } catch (InterruptedException e) {
                        return;
                    }
                    return;
                }
                System.out.println("提币交易编号： " + data);
                // 当前交易完成，交易所状态重置
                BUY_TOKEN_NAME_EXCHANGE = "";
                SELL_TOKEN_NAME_EXCHANGE = "";
                SELL_TOKEN_VALUE = new BigInteger("0");
                EXCHANGE_STATE = 0;
                EAT_OFFER_TRANSACTION_HASH = "";
                return;
            }
        }
    }
    /**
    *   设置允许的价格波动比例，超过比例即吃单
    */
    @Override
    public void updatePriceDeviation(int priceDeviation) {
        UP_PRICE_DEVIATION = (new BigDecimal(String.valueOf(priceDeviation)).add(new BigDecimal("1000"))).divide(new BigDecimal("1000"));
        DOWN_PRICE_DEVIATION = (new BigDecimal("1000").subtract(new BigDecimal(String.valueOf(priceDeviation)))).divide(new BigDecimal("1000"));
    }
    /**
    *    用户操作界面需要展示的数据
    */
    @Override
    public JSONObject eatOfferData() {
        JSONObject jsonObject = new JSONObject();
        String address = "";
        try{
            Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
            address = credentials.getAddress();
        }catch (Exception e){
            address = "请填写正确的私钥";
        }
        if(API_KEY.equalsIgnoreCase("")){
            jsonObject.put("apiKey","请填写API-KEY");
        }else {
            jsonObject.put("apiKey","已存在");
        }
        if(API_SECRET.equalsIgnoreCase("")){
            jsonObject.put("apiSecret","请填写API-SECRET");
        }else {
            jsonObject.put("apiSecret","已存在");
        }
        jsonObject.put("eatOfferState",START_EAT_OFFER);
        jsonObject.put("huobiExchangeState",START_HUOBI_EXCHANGE);
        jsonObject.put("walletAddress",address);
        jsonObject.put("priceDeviation",UP_PRICE_DEVIATION.multiply(new BigDecimal("1000")).subtract(new BigDecimal("1000")));
        jsonObject.put("node",NODE);
        jsonObject.put("authorizedUser",AUTHORIZED_USER);
        return jsonObject;
    }

    /**
     *  查询交易所的token余额
     * @return
     */
    private HashMap<String, String> getExchangeBalance(){
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        AccountsResponse accounts = client.accounts();
        List<Accounts> listAccounts = (List<Accounts>) accounts.getData();
        if (!listAccounts.isEmpty()) {
            //------------------------------------------------------ 账户余额  -------------------------------------------------------
            BalanceResponse balance = client.balance(String.valueOf(listAccounts.get(0).getId()));
//            BalanceResponse balance2 = client.balance(String.valueOf(list.get(1).getId()));
//            log.info("sport余额: ");
//            print(balance); //spot
            String s = "";
            try {
                s = JsonUtil.writeValue(balance);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = JSONObject.parseObject(s);
            String data = jsonObject.getString("data");
            JSONObject jsonObject1 = JSONObject.parseObject(data);
            JSONArray list = jsonObject1.getJSONArray("list");
            HashMap<String,String> hashMap = new HashMap();
            list.forEach(li->{
                JSONObject jsonObject2 = JSONObject.parseObject(String.valueOf(li));
                String balanceStr = jsonObject2.getString("balance");
                String currency = jsonObject2.getString("currency");
                if(!balanceStr.equalsIgnoreCase("0")){
                    if(hashMap.containsKey(currency)){
                        // 同一个token有可能会出现多次，选择金额最大的一个
                        if(new BigDecimal(hashMap.get(currency)).compareTo(new BigDecimal(balanceStr)) < 0){
                            hashMap.replace(currency,hashMap.get(currency),balanceStr);
                        }
                    }else {
                        hashMap.put(currency,balanceStr);
                    }
                }
            });
            logger.info("交易所账户所有余额：" + hashMap);
            return hashMap;
        }
        return null;
    }
    /**
    *  吃单报价充值
     * @return
     */
    private String eatOfferRecharge(){
        /**
         *   获取火币交易所token对应的充值钱包地址
         */
        String currency = SELL_TOKEN_NAME_EXCHANGE;
        String tokenAddress = "";
        ApiClient client = new ApiClient(API_KEY, API_SECRET);
        DepositAddressResponse depositAddress = client.getDepositAddress(currency);
        int code = depositAddress.getCode();
        if(code != 200){
            System.out.println("火币交易所查询充值地址失败");
            return null;
        }
        String exchangeTokenAddress =depositAddress.getData().get(0).getAddress();
        System.out.println("交易所充币地址：" + exchangeTokenAddress);
        // 以太坊地址长度为42位，并且是以0x开头（交易所查询的以太坊的默认为第一个，确认防止有改变）
        if(exchangeTokenAddress.length() != 42 || !exchangeTokenAddress.substring(0,2).equalsIgnoreCase("0x")){
            System.out.println("查询交易所地址错误，查询到的地址为：" + exchangeTokenAddress);
            return null;
        }
        // 查询余额
        BigInteger balance = new BigInteger("0");
        try{
            Web3j web3j = Web3j.build(new HttpService(NODE));
            Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(new BigInteger("3"));
            BigInteger gasLimit = new BigInteger("1000000");
            if(currency.equalsIgnoreCase("eth")){
                balance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
            }else if(currency.equalsIgnoreCase("ht")){
                tokenAddress = HT_TOKEN_ADDRESS;
                ERC20 erc20 = ERC20.load(HT_TOKEN_ADDRESS,web3j,credentials,gasPrice,gasLimit);
                balance = erc20.balanceOf(credentials.getAddress()).send();
            }else if(currency.equalsIgnoreCase("usdt")){
                tokenAddress = USDT_TOKEN_ADDRESS;
                ERC20 erc20 = ERC20.load(USDT_TOKEN_ADDRESS,web3j,credentials,gasPrice,gasLimit);
                balance = erc20.balanceOf(credentials.getAddress()).send();
            }else {
                System.out.println("充值到交易所只支持ETH,HT,USDT,暂不支持其他token");
                return null;
            }
        }catch (Exception e) {
            System.out.println("查询ETH余额失败，请检查网络状态");
            return currency;
        }
        // 检测余额是否足够
        if(balance.compareTo(SELL_TOKEN_VALUE) < 0){
            System.out.println("资金不够，还不能转入到交易所账户地址，查看是否有还未取回的报价合约");
            return null;
        }
        String transactionHash;
        // 充值资产到交易所
        if(currency.equalsIgnoreCase("eth")){
            System.out.println("充值：" + exchangeTokenAddress  + "  金额：" + SELL_TOKEN_VALUE);
            transactionHash = transferEth(exchangeTokenAddress, SELL_TOKEN_VALUE);
        }else {
            System.out.println("充值：" + tokenAddress  + "  交易所钱包地址：" + exchangeTokenAddress + "  金额：" + SELL_TOKEN_VALUE);
            transactionHash = transferErc20(tokenAddress, exchangeTokenAddress, SELL_TOKEN_VALUE);
        }
        return transactionHash;
    }
    /**
     *  检测是否对USDT进行了一次性授权,如果没有,即进行一次性授权
     */
    private void approveUsdtToOfferFactoryContract(Web3j web3j) throws Exception {
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        ERC20 load = ERC20.load(USDT_TOKEN_ADDRESS, web3j, credentials, new BigInteger("10000000000"), new BigInteger("500000"));
        BigInteger approveValue = load.allowance(credentials.getAddress(), OFFER_FACTORY_CONTRACT).send();
        if(approveValue.compareTo(new BigInteger("10000000000")) < 0){
            String transactionHash = load.approve(OFFER_FACTORY_CONTRACT, new BigInteger("99999999999999999999")).send().getTransactionHash();
            System.out.println("USDT一次性授权hash：" + transactionHash);
            Thread.sleep(1000*60*2);
        }
    }
    /**
     *  检测是否对HT进行了一次性授权,如果没有,即进行一次性授权
     */
    private void approveHtToOfferFactoryContract(Web3j web3j) throws Exception {
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        ERC20 load = ERC20.load(HT_TOKEN_ADDRESS, web3j, credentials, new BigInteger("10000000000"), new BigInteger("500000"));
        BigInteger approveValue = load.allowance(credentials.getAddress(), OFFER_FACTORY_CONTRACT).send();
        if(approveValue.compareTo(new BigInteger("10000000000000000000000")) < 0){
            String transactionHash = load.approve(OFFER_FACTORY_CONTRACT, new BigInteger("1000000000000000000000000000000")).send().getTransactionHash();
            System.out.println("HT一次性授权hash：" + transactionHash);
            Thread.sleep(1000*60*2);
        }
    }
    /**
     * 获取交易所ETH相对USDT价格
     */
    private static BigDecimal getEthAndUsdtExchangePrice(){
        String s;
        try {
            s = HttpClientUtil.sendHttpGet(URL_ETH_USDT_PRICE);
        }catch (Exception e){
            return null;
        }
        if(s == null)return null;
        JSONObject jsonObject = JSONObject.parseObject(s);
        BigDecimal price = JSONObject.parseObject(
                String.valueOf(
                        JSONObject.parseObject(
                                String.valueOf(
                                        jsonObject.getJSONArray("data").get(0)
                                )
                        ).getJSONArray("data").get(0)
                )
        ).getBigDecimal("price");
        return price == null ? null : price.setScale(6,BigDecimal.ROUND_DOWN);
    }
    /**
     * 获取交易所ETH相对HT价格
     */
    private static BigDecimal getEthAndHtExchangePrice(){
        String s;
        try {
            s = HttpClientUtil.sendHttpGet(URL_HT_ETH_PRICE);
        }catch (Exception e){
            return null;
        }
        if(s == null)return null;
        JSONObject jsonObject = JSONObject.parseObject(s);
        BigDecimal price = JSONObject.parseObject(
                String.valueOf(
                        JSONObject.parseObject(
                                String.valueOf(
                                        jsonObject.getJSONArray("data").get(0)
                                )
                        ).getJSONArray("data").get(0)
                )
        ).getBigDecimal("price");
        return price == null ? null : new BigDecimal("1").divide(price,6,BigDecimal.ROUND_DOWN);
    }
    /**
    *   更换节点
    */
    @Override
    public void updateNode(String urlNode) {
        NODE = urlNode;
    }
    /**
     *  查询提币手续费
     */
    @Override
    public String queryExtractServiceCharge(String currency,QueryExtractServiceChargeResponse queryExtractServiceChargeResponse){
        if(currency==null)return null;
        List<QueryExtractServiceChargeChains> chains = queryExtractServiceChargeResponse.getData().get(0).getChains();
        if(currency.equalsIgnoreCase("usdt")){
            for (QueryExtractServiceChargeChains chain : chains) {
                String chain1 = chain.getChain();
                if(chain1.equalsIgnoreCase("usdterc20")){
                    String withdrawFeeType = chain.getWithdrawFeeType();
                    if(withdrawFeeType.equalsIgnoreCase("fixed")){
                        return chain.getTransactFeeWithdraw();
                    }
                    if(withdrawFeeType.equalsIgnoreCase("circulated")){
                        return chain.getMinTransactFeeWithdraw();
                    }
                    if(withdrawFeeType.equalsIgnoreCase("ratio")){
                        return chain.getTransactFeeRateWithdraw();
                    }
                }
            }
        }else {
            QueryExtractServiceChargeChains queryExtractServiceChargeChains = chains.get(0);
            String withdrawFeeType = queryExtractServiceChargeChains.getWithdrawFeeType();
            if(withdrawFeeType.equalsIgnoreCase("fixed")){
                return queryExtractServiceChargeChains.getTransactFeeWithdraw();
            }
            if(withdrawFeeType.equalsIgnoreCase("circulated")){
                return queryExtractServiceChargeChains.getMinTransactFeeWithdraw();
            }
            if(withdrawFeeType.equalsIgnoreCase("ratio")){
                return queryExtractServiceChargeChains.getTransactFeeRateWithdraw();
            }
        }
        return null;
    }
    /**
    *   设置交易所是否开启了用户认证
    */
    @Override
    public void updateAuthorizedUser() {
        if(AUTHORIZED_USER.equalsIgnoreCase("true")){
            AUTHORIZED_USER = "false";
        }else {
            AUTHORIZED_USER = "true";
        }
    }
}
