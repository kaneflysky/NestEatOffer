package com.nest.ib.vo;

import com.nest.ib.service.EatOfferAndTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * ClassName:applicationBonusStorage
 * Description:
 */
@Component
public class applicationBonusStorage {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(50);
        return taskScheduler;
    }

    @Autowired
    private EatOfferAndTransactionService eatOfferAndTransactionService;

    /**
    *   吃单报价
    */
    @Scheduled(fixedDelay = 10000)
    public void offer(){
        eatOfferAndTransactionService.startEatOffer();
    }
    /**
     *  查找报价后未取回的合约，并取回资产
     */
    @Scheduled(fixedDelay = 60000)
    public void turnOut(){
        try {
            eatOfferAndTransactionService.retrieveAssets();
        } catch (Exception e) {
            System.out.println("取回资产出现异常");
            return;
        }
    }
    /**
     *   将吃单报价的资产进行交易所买卖
     */
    @Scheduled(fixedDelay = 60000)
    public void exchangeBuyAndSell(){
        eatOfferAndTransactionService.exchangeBuyAndSell();
    }

}
