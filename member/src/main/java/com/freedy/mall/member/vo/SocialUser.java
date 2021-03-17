package com.freedy.mall.member.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Freedy
 * @date 2021/3/12 14:24
 */
@Data
public class SocialUser implements Serializable {
    /**
     * access_token : c2ccca13d9d72b2df220f6279a02770b
     * token_type : bearer
     * expires_in : 86400
     * refresh_token : 329bf69ed54fcfd777ae5ba5aa3c655a9b7e7baf14c8401a6c84f1db8a00cd3a
     * scope : user_info projects pull_requests issues notes keys hook groups gists enterprises emails
     * created_at : 1615530191
     */

    private String access_token;
    private String token_type;
    private int expires_in;
    private String refresh_token;
    private String scope;
    private int created_at;
}
