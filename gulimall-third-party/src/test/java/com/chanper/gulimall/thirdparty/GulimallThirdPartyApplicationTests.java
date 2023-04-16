package com.chanper.gulimall.thirdparty;

import com.chanper.gulimall.thirdparty.component.SmsComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    SmsComponent smsComponent;

    @Test
    void sendSms() {
        String resp = smsComponent.sendSmsCode("17626653349", "963852");
        System.out.println(resp);
    }

}
