package com.freedy.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.freedy.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000117629702";
    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCB8Y+4DjivGWvYVsf0KHLT+dowvj1xH7bufA7TBDt60WKAOIP3mEb6q1CJydfHxZSiyWebWXkrH78HnF24kFTZaSqOCwYwRI1G3mj8RlKU7RtZkaXaj1OJ2li82NBHwO24Ez40OANsO/FdTwDyle6phIWX9iyL3SJ/U3X3eZXxCro3bXDYCY91dZ/gJtiaBBgt0z+g7JweARBuzrIYGvP5ZRTsyhj7RjJ54OC4pFBLkrPX5VQLKPVaUB4j7QCCgvtFOzX1Le/u+UGtOiDAAVORRAVaS0jq+wky6bbVC5K/c7MA4gEZZuvZkaZImRKB/FtBqZi3JR7KIPzA8UPEpEL3AgMBAAECggEAea/60/VVdfyLdwxFsktSxVn8z5rINTDrAcjhpREYlnqRENxcETjkd4iJIpAH58gK2qtbwCp5Az52ia0QQ2++Vd9bf6upPvhjiRNtI5rDaT3mV+nCdLu2EqmMYkV0P03YEILMxOATrH8yGByNGZ/44Xz+EoHx04Th8nT5Syjsls4B3euuUd/4yquIvkcKrSpCDunQPGZlNwv/TsCUfty1OzycJ8gKLjScqPgwDImmvnGMP07H+FRoOqBYgAIwTfxwJr8L6V193PEwvxPCdbl8o30JcXhuF8ojnhm0xxyXL6ovZAGvtDd3iPwex0o7JNE2HPpkZSCPRbfs0RnVDrw6oQKBgQDpi0LIriYb4pRgMCzO8T71s1MVSZkNfNyYwCx4GUFOeXWv2dezicXKg1hOkAyEutGzS3MOS4d2Jt1kUVxZ9pdt6PYWUr2d4LV9eMNE8yq9SSOXAbzfCiYo3031rsJdyfTOTG2Wl5HqdPmV+jYmP/iIA//YRgKHTlj63o4gMCb4MQKBgQCOcCcveKtEPpUZ/R68fJsCNqTTWXrzo0TVrFcKEyJKvzE5KonXOprm+E1+clD2bJ4RywO1hR2PGw1vi1FcAqi1OER2NvhJ50JroMjTLR1BFsUhuykCTUg85bqZ5A3KdEptdzq1X15m8pibYIGSN8K3if6Fk1j9tePAhoN3GiJLpwKBgB41Si0lAX7n3uNWN+WIpOkpCd0eCPMKrIoBiX77XUwmP8fAOLtbXb8lIB4BWe0tMQStBLPbpyjsPATG1Vg5ojqwQGyAxVAPicox/agEgAm91SGqMuDysh7lS3M06KUzAzxP/HjXICOgf8wbcjeeXKpTQhuaaqdPxgilEuMs901xAoGAF9gohybhLNR5K1eXPKBzZoR1RTg2f/C2BjsKexJG7FJjUBmR56jU45NvjZpfeVEniPz+SvXUw+8YabLd1NRRH70ioNOE7wI6uT+6PrfPrOS97W6iZ9M+I4ulmDt4H4smJnsOJeyoJf24lFIsfqi8/PKp2/yVqis3zDP4cAi88wMCgYBJ36emA/yW9iIAJZsQwO2YTvu+rncPNYuM6kKM87JhNZBJ+g4P2ScArYdrmMKTgIolrBHZT48PwgmGuWJn/qBqY2jnCZ65TSq4hwO9yUeErdt4PkbU87abkCa1DaR5lgUc4yGL1QKuOl3+wAaTj4ug6DYGXd0cEPImN+Rjw4V2LQ==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAriba9gtdC21eA2RE1NYrEkACFB4ihrwTu6TLKmqrKO+702wMo0+Ile5mlOzRBjB/y4TjwtgLS1A+PApS0j0iBWBAYtSuz1RJ5X5UgQWszF5DKVwEeZ4QogOhjd9BJ3x7m8+1ZuRVjCN8I88KTU8lROZlRBEXp8cUy4DJURGO+xWC/dJUCr1dBjvBduYrZMwZQTmjGBd9BVipEXu9+A1eisTMJw79UMdZV1IzKSRn/K2so6wMoHIuK+j7IFXo+hEHC3+0Y1KUKeLQ20XjhdCUjqPUzqHZ4+HSP6fOtwcgpMki3MeLDYjVN3QYBUhTcJGlGukdMoDiKPjMYTvDjQqK7wIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://42.194.140.189/payed/notify";
    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.freedymall.com/memberOrder.html";
    // 签名方式
    public  String sign_type = "RSA2";
    // 字符编码格式
    public  String charset = "utf-8";
    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\"1m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
