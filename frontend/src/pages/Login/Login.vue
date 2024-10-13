<template>
  <div class="container">
    <div class="container-header">
      <span>电工电子实验中心设备管理系统</span>
    </div>
    <div class="login-container">
      <div class="login-display"></div>
      <div class="login-form">
        <img src="/logo-mini.png" alt="logo"/>
        <div v-if="useAccount">
          <div class="user">
            <i class="iconfont icon-email"></i>
            <input v-model="loginForm.userAccount" placeholder="账号" autocomplete="off">
          </div>
          <div class="password">
            <i class="iconfont icon-password"></i>
            <input v-model="loginForm.userPassword" type="password" placeholder="密码" autocomplete="off"
                   @keyup.enter="handleLogin">
          </div>
          <button class="login-button" @click="handleLogin">登录</button>
        </div>
        <div v-else>
          <div class="cas-container">
            <h3 style="text-align: center;padding-bottom: 20px">统一认证登录</h3>
            <div class="cas-button" @click="onLoginCas">前往统一认证服务</div>
          </div>
        </div>
        <a-divider style="border-color: #c4c4c4;"><span style="color:#9a9a9a;font-size: small ">或者使用</span>
        </a-divider>
        <div class="change-container">
          <div class="change-button" v-if="useAccount" @click="useAccount = !useAccount">
            <img src="/favicon.ico" alt="cas-logo"/>统一认证登录
          </div>
          <div class="change-button" v-else @click="useAccount = !useAccount">
            <img src="/user.svg" alt="cas-logo"/>账号密码登录
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup> import {onBeforeMount, reactive, ref} from 'vue';
import router from "@/router.js";
import {message} from "ant-design-vue";
import getInstance from "@/sdk/Instance.js";

const instance = getInstance()
const useAccount = ref(true)

onBeforeMount(() => {
  instance.whoami().then(res => {
    if (res.data.code === 1) {
      router.push("/home")
    }
  })
})
const loginForm = reactive({userAccount: '', userPassword: ''});
const onLoginCas = () => {
  window.location.assign("/login/cas")
}
const handleLogin = () => {
  instance.loginApi(loginForm).then(res => {
    if (res.data.code === 1) {
      router.push('/home')
    } else {
      message.info(res.data.msg)
    }
  })
} </script>
<style scoped>
@import "/font/iconfont.css";
@import "style.scss";
</style>