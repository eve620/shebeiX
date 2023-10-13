<template>
    <div class="top-bar">
        <div>{{ props.title }}</div>
        <div class="top-bar-info">
            <div>修改密码</div>
            <div @click="logout">
                <LogoutOutlined @click="logout"/>
                <span style="padding-left: 2px">退出</span>
            </div>
            <span style="padding-left: 3px;font-weight: bold">{{ props.userInfo.userName }}</span>
        </div>
    </div>
</template>

<script setup>
import getInstance from "@/sdk/Instance.js";
import router from "@/router.js";

const instance = getInstance()
const props = defineProps({
    userInfo: {
        type: Object,
        required: true
    },
    title: {
        type: String,
        required: true
    }
});

const logout = () => {
    instance.logoutApi().then(res => {
        if (res.data.code === 1) {
            router.push('/login')
        }
    })
}
</script>

<style scoped>
@import "style.scss";
</style>