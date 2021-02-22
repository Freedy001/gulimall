package com.freedy.mall.search;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Freedy
 * @date 2021/2/10 0:23
 */
@Data
public class Account implements Serializable {

    /**
     * account_number : 136
     * balance : 45801
     * firstname : Winnie
     * lastname : Holland
     * age : 38
     * gender : M
     * address : 198 Mill Lane
     * employer : Neteria
     * email : winnieholland@neteria.com
     * city : Urie
     * state : IL
     */

    private int account_number;
    private int balance;
    private String firstname;
    private String lastname;
    private int age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;

}
