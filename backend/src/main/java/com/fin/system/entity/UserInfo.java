package com.fin.system.entity;

public class UserInfo {
    public String userAccount;
    public String userName;
    public Integer roleId;

    public UserInfo(String userAccount, String userName, Integer roleId) {
        this.userAccount = userAccount;
        this.userName = userName;
        this.roleId = roleId;
    }

}
