package com.freedy.mall.thirdparty.controller;

import com.freedy.common.utils.R;
import com.freedy.mall.thirdparty.component.EmailAuth;
import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Freedy
 * @date 2021/3/9 19:25
 */
@RestController
public class EmailController {
    @Autowired
    EmailAuth emailAuth;

    @GetMapping("/email/{code}")
    public R email(@PathVariable String code, @RequestParam("address") String address) throws EmailException {
        emailAuth.sendMsg(address,code);
        return R.ok();
    }
}
