<template>
    <div>
        <img src="/404.png" alt="404" style="height: 200px;margin-left: 70px"/>
        <br>
        <h1>:( 出错啦</h1>
        <h2>404 Not Found : 页面不存在</h2>
        <h3>
            <router-link to="/login">点击返回登陆界面</router-link>
        </h3>
    </div>
</template>

<script setup>
</script>

<style scoped>
div {
    height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
</style>