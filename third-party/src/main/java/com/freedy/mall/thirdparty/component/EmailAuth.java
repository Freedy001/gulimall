package com.freedy.mall.thirdparty.component;

import lombok.Data;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Freedy
 * @date 2021/3/9 18:47
 */
@Data
@Component
@ConfigurationProperties("freedymall.email")
public class EmailAuth {
    private String hostName;
    private String from;
    private String authentication;

    public void sendMsg(String addTo,String code) throws EmailException {
        HtmlEmail email=new HtmlEmail();
        email.setHostName(hostName);
        email.setCharset("utf-8");
        email.addTo(addTo);
        email.setFrom(from,"Freedy");
        email.setAuthentication(from,authentication);
        email.setSubject("FreedyMall验证码服务");
        String msg="<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Document</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div style=\"background-color:#ffffff;\n" +
                "    display: flex;\n" +
                "    flex-direction: column;\n" +
                "    justify-content: space-between;\n" +
                "    width: 400px;\n" +
                "    height: 200px;\n" +
                "    \">\n" +
                "      <span style=\"\n" +
                "          color: #000000;\n" +
                "          font-size: 30px;\n" +
                "          text-align: center;\n" +
                "        \">你的验证码为</span>\n" +
                "  <span style=\"\n" +
                "          color: #1073fa;\n" +
                "          font-size: 60px;\n" +
                "          text-align: center;\n" +
                "        \">"+code+"</span>\n" +
                "  <span style=\"\n" +
                "          color: #000000;\n" +
                "          font-size: 30px;\n" +
                "          text-align: center;\n" +
                "        \">\n" +
                "          请在三十分钟内使用！\n" +
                "        </span>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
        System.out.println(msg);
        email.setMsg(msg);
        email.send();//进行发送
    }
}
