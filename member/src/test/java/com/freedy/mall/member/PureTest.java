package com.freedy.mall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author Freedy
 * @date 2021/3/10 18:42
 */

public class PureTest {

    @Test
    public void contextTest(){
        //$2a$10$HqE.7e8Us/JeUDepK0n4.OF5GF5IgKArxXV310seSR2Q6/IAMh1le
        String s = DigestUtils.md5Hex("123456");
        //盐值加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("123456");
        boolean matches = bCryptPasswordEncoder.matches("123456", "$2a$10$HqE.7e8Us/JeUDepK0n4.OF5GF5IgKArxXV310seSR2Q6/IAMh1le");
        System.out.println(matches);
        System.out.println(encode);
        System.out.println(s);
    }
}
