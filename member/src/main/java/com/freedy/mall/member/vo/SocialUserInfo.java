package com.freedy.mall.member.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Freedy
 * @date 2021/3/12 20:12
 */
@Data
public class SocialUserInfo implements Serializable {

    /**
     * id : 7609312
     * login : freedyamazing
     * name : Freedyamazing
     * avatar_url : https://gitee.com/assets/no_portrait.png
     * url : https://gitee.com/api/v5/users/freedyamazing
     * html_url : https://gitee.com/freedyamazing
     * followers_url : https://gitee.com/api/v5/users/freedyamazing/followers
     * following_url : https://gitee.com/api/v5/users/freedyamazing/following_url{/other_user}
     * gists_url : https://gitee.com/api/v5/users/freedyamazing/gists{/gist_id}
     * starred_url : https://gitee.com/api/v5/users/freedyamazing/starred{/owner}{/repo}
     * subscriptions_url : https://gitee.com/api/v5/users/freedyamazing/subscriptions
     * organizations_url : https://gitee.com/api/v5/users/freedyamazing/orgs
     * repos_url : https://gitee.com/api/v5/users/freedyamazing/repos
     * events_url : https://gitee.com/api/v5/users/freedyamazing/events{/privacy}
     * received_events_url : https://gitee.com/api/v5/users/freedyamazing/received_events
     * type : User
     * blog : null
     * weibo : null
     * bio : null
     * public_repos : 3
     * public_gists : 0
     * followers : 0
     * following : 1
     * stared : 2
     * watched : 5
     * created_at : 2020-05-26T09:25:57+08:00
     * updated_at : 2021-03-12T13:33:22+08:00
     * email : null
     */

    private int id;
    private String login;
    private String name;
    private String avatar_url;
    private String url;
    private String html_url;
    private String followers_url;
    private String following_url;
    private String gists_url;
    private String starred_url;
    private String subscriptions_url;
    private String organizations_url;
    private String repos_url;
    private String events_url;
    private String received_events_url;
    private String type;
    private String blog;
    private String weibo;
    private String bio;
    private int public_repos;
    private int public_gists;
    private int followers;
    private int following;
    private int stared;
    private int watched;
    private String created_at;
    private String updated_at;
    private String email;
}
