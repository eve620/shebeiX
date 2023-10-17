<template>
    <div class="top-bar">
        <div>{{ props.title }}</div>
        <a-modal v-model:open="isChangePasswordOpen" ok-text="确定" cancel-text="取消" @ok="onChangePasswordOk" @cancel="onChangePasswordCancel" title="修改密码">
            <a-input id=password v-model:value="passwordInput" placeHolder="新密码"/>
        </a-modal>
        <div class="top-bar-info">
            <div @click="onChangeTheme">主题切换</div>
            <div @click="onChangePassword">修改密码</div>
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
import {ref} from "vue";
import {message} from "ant-design-vue";

const instance = getInstance()
const emit = defineEmits(['changeTheme']);
const onChangeTheme = () => {
    emit('changeTheme')
}
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

const passwordInput = ref('');
const isChangePasswordOpen = ref(false);
const onChangePassword = () => {
    isChangePasswordOpen.value = true;
}
const onChangePasswordOk = () => {
    instance.changeUserPassword(props.userInfo.userId,passwordInput.value).then(res => {
        if (res.data.code === 1) {
            message.info(res.data.data)
        } else message.info(res.data.msg)
    })
    passwordInput.value = '';
    isChangePasswordOpen.value = false;
}
const onChangePasswordCancel = () => {
    passwordInput.value = '';
    isChangePasswordOpen.value = false;
}
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