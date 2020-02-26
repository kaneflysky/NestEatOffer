package com.nest.ib.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nest.ib.contract.ERC20;
import com.nest.ib.service.EatOfferAndTransactionService;
import com.nest.ib.utils.api.ApiClient;
import com.nest.ib.utils.api.JsonUtil;
import com.nest.ib.utils.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName:MiningController
 * Description:
 */
@RestController
@RequestMapping("/eatOffer")
public class MiningController {


    @Autowired
    private EatOfferAndTransactionService eatOfferAndTransactionService;

    /**
     *  关闭/开启 吃单报价
     */
    @PostMapping("/updateEatOfferState")
    public boolean updateEatOfferState(){
        return eatOfferAndTransactionService.updateEatOfferState();
    }
    /**
    *   关闭/开启  火币交易所交易（将吃单后获取的资产去交易所进行买卖）
    */
    @PostMapping("/updateHuobiExchangeState")
    public boolean updateHuobiExchangeState(){
        return eatOfferAndTransactionService.updateHuobiExchange();
    }
    /**
    *   设置是否开启用户认证
    */
    @PostMapping("/updateAuthorizedUser")
    public void updateAuthorizedUser(){
        eatOfferAndTransactionService.updateAuthorizedUser();
    }
    /**
    *   更换节点
    */
    @PostMapping("/updateNode")
    public void updateNode(@RequestParam(name = "node") String node){
        Web3j web3j = Web3j.build(new HttpService(node));
        try {
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            eatOfferAndTransactionService.updateNode(node);
        } catch (IOException e) {
            System.out.println("请填写正确的节点");
        }
    }
    /**
    *   更新用户私钥
    */
    @PostMapping("/updatePrivateKey")
    public String updatePrivateKey(@RequestParam(name = "privateKey") String privateKey){
        return eatOfferAndTransactionService.updatePrivateKey(privateKey);
    }
    /**
    *   设置允许的价格波动比例，超过则进行吃单报价:  1则是允许上下波动 1‰
    */
    @PostMapping("/updatePriceDeviation")
    public void updatePriceDeviation(@RequestParam(name = "priceDeviation") int priceDeviation){
        if(priceDeviation <= 0 || priceDeviation >= 1000){
            System.out.println("设置价格波动比例取值范围为：1 ~ 999");
            return;
        }
        eatOfferAndTransactionService.updatePriceDeviation(priceDeviation);
    }
    /**
    *   设置交易所的API-KEY和API-SECRET
     * @return
     */
    @PostMapping("/updateExchangeApiKey")
    public List updateExchangeApiKey(@RequestParam(name = "apiKey")String apiKey, @RequestParam(name = "apiSecret")String apiSecret){
        System.out.println("apiKey: " + apiKey);
        System.out.println("apiSecret: " + apiSecret);
        ApiClient client = new ApiClient(apiKey,apiSecret);
        AccountsResponse accounts = client.accounts();
        List<Accounts> listAccounts = (List<Accounts>) accounts.getData();
        if (!listAccounts.isEmpty()) {
            //------------------------------------------------------ 账户余额  -------------------------------------------------------
            BalanceResponse balance = client.balance(String.valueOf(listAccounts.get(0).getId()));
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
            List balanceList = new ArrayList<>();
            list.forEach(li->{
                JSONObject jsonObject2 = JSONObject.parseObject(String.valueOf(li));
                String balanceStr = jsonObject2.getString("balance");
                String currency = jsonObject2.getString("currency");
                // 找到有余额的token
                /*if(!balanceStr.equalsIgnoreCase("0")){
                    HashMap hashMap = new HashMap();
                    hashMap.put("balance",balanceStr);
                    hashMap.put("currency",currency);
                    balanceList.add(hashMap);
                }*/
                // 找到HT,ETH,USDT的余额
                if(currency.equalsIgnoreCase("ht") || currency.equalsIgnoreCase("eth") || currency.equalsIgnoreCase("usdt")){
                    HashMap hashMap = new HashMap();
                    hashMap.put("balance",balanceStr);
                    hashMap.put("currency",currency);
                    balanceList.add(hashMap);
                }
            });
            String result = eatOfferAndTransactionService.updateExchangeApiKey(apiKey, apiSecret);
            if(result.equalsIgnoreCase("SUCCESS")){
                return balanceList;
            }
        }
        return null;
    }
    /**
     * 查看矿机详情
     */
    @GetMapping("/miningData")
    public ModelAndView miningData(){
        JSONObject jsonObject = eatOfferAndTransactionService.eatOfferData();
        ModelAndView mav = new ModelAndView("miningData");
        mav.addObject("address",jsonObject.getString("walletAddress"));
        mav.addObject("huobiExchangeState",jsonObject.getBooleanValue("huobiExchangeState") == true ? "交易所状态：开启买卖" : "交易所状态：关闭买卖");
        mav.addObject("eatOfferState",jsonObject.getBooleanValue("eatOfferState") == true ? "吃单报价状态: 开启" : "吃单报价状态: 关闭");
        mav.addObject("apiKey",jsonObject.getString("apiKey"));
        mav.addObject("apiSecret",jsonObject.getString("apiSecret"));
        mav.addObject("priceDeviation",jsonObject.getBigDecimal("priceDeviation"));
        mav.addObject("node",jsonObject.getString("node"));
        mav.addObject("authorizedUser",jsonObject.getString("authorizedUser").equalsIgnoreCase("true") ? "交易所认证：开启" : "交易所认证：关闭");
        return mav;
    }

}
