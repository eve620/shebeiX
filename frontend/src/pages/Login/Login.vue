<template>
      <div class="container">
          <div class="login-container">
              <div class="login-display"></div>
              <div class="login-form">
                  <img src="/logo-mini.png" alt="logo"/>
                  <div>
                      <div class="user">
                          <i class="iconfont icon-email"></i>
                          <input v-model="loginForm.userAccount" placeholder="账号" autocomplete="off">
                      </div>
                      <div class="password">
                          <i class="iconfont icon-password"></i>
                          <input v-model="loginForm.userPassword" type="password" placeholder="密码" autocomplete="off"
                                 @keyup.enter="handleLogin">
                      </div>
                      <button @click="handleLogin">登录</button>
                  </div>
              </div>
          </div>
      </div>
</template>

<script setup>

 import {onBeforeMount, reactive, ref} from 'vue';
 import router from "@/router.js";
 import {message} from "ant-design-vue";
 import getInstance from "@/sdk/Instance.js";

 const instance = getInstance()

 onBeforeMount(() => {
   instance.whoami().then(res => {
     if (res.data.code === 1) {
       router.push("/home")
     }
   })
 })
 const loginForm = reactive({userAccount: '', userPassword: ''});
 const handleLogin = () => {
   instance.loginApi(loginForm).then(res => {
     if (res.data.code === 1) {
       router.push('/home')
     } else {
       message.info(res.data.msg)
     }
   })
 }
</script>
<style scoped>
@import "/font/iconfont.css";
@import "style.scss";
</style>