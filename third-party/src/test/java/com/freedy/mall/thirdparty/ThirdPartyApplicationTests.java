package com.freedy.mall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.freedy.mall.thirdparty.component.EmailAuth;
import org.apache.commons.mail.EmailException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ThirdPartyApplicationTests {

    @Autowired
    OSSClient ossClient;

    @Autowired
    EmailAuth emailAuth;

    @Test
    public void testUpload() throws FileNotFoundException {
        // 上传文件流。
        InputStream inputStream = new FileInputStream("D:\\视频图片素材\\图片\\DJI_0015.JPG");
        ossClient.putObject("freedy", "hhhh.JPG", inputStream);
        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传成功");
    }

    @Test
    public void testSendEmailCode() throws  EmailException {
        emailAuth.sendMsg("985948228@qq.com","ZXGFR");
    }

}
