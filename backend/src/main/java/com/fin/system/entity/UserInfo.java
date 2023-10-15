package com.fin.system.entity;

public class UserInfo {
    public Integer userId;
    public String userAccount;
    public String userName;
    public Integer roleId;

    public UserInfo(Integer userId,String userAccount, String userName, Integer roleId) {
        this.userId = userId;
        this.userAccount = userAccount;
        this.userName = userName;
        this.roleId = roleId;
    }

}
