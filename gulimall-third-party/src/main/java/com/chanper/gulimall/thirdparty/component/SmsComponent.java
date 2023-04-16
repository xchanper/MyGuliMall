package com.chanper.gulimall.thirdparty.component;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>Title: SmsComponent</p>
 * Description：
 * date：2020/6/25 14:23
 */
@Data
// @ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Component
public class SmsComponent {

    @Autowired
    Client smsClient;

    public String sendSmsCode(String phone, String code) {
        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setPhoneNumbers(phone)
                .setTemplateParam("{\"code\":\"" + code + "\"}");
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        SendSmsResponse sendSmsResponse = null;
        try {
            // 复制代码运行请自行打印 API 的返回值
            sendSmsResponse = smsClient.sendSmsWithOptions(sendSmsRequest, runtime);
            return String.valueOf(sendSmsResponse.statusCode);
        } catch (TeaException error) {
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
            return "fail";
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
        }

        return "fail_" + sendSmsResponse.getStatusCode();
    }
}

