<template>
    <div class="loading" v-show="!isShow">
        <a-spin size="large"/>
    </div>
    <a-layout style="height: auto;min-height: 100vh" v-show="isShow" :class={themeRed:!isAdmin}>
        <a-layout-sider
                breakpoint="lg"
                collapsed-width="0"
                @collapse="onCollapse"
                @breakpoint="onBreakpoint"
        >
            <div class="logo">
                <img src="/logo-sider.png" style="padding: 30px 8px 30px 8px;width: 100%" alt="logo"/>
            </div>
            <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline">
                <a-menu-item v-for="item in menuList" :key="item.id" @click="menuClick(item.url)">
                    <component :is="item.icon" style="font-size: 16px;line-height: 40px"/>
                    <span class="nav-text">{{ item.name }}</span>
                </a-menu-item>
            </a-menu>
        </a-layout-sider>
        <a-layout>
            <a-layout-header :style="{ background: '#fff' }">
                <TopBar :userInfo="user" :title="menuList.find(item => item.id === selectedKeys[0]).name"/>
            </a-layout-header>
            <a-layout-content :style="{ margin: '24px 16px 0' }">
                <div :style="{ padding: '24px', background: '#fff', minHeight: '360px' }">
                    <RouterView/>
                </div>
            </a-layout-content>
            <a-layout-footer style="text-align: center">
                开发维护：西安电子科技大电子工程学院
            </a-layout-footer>
        </a-layout>
    </a-layout>
</template>
<script setup> import {onBeforeMount, reactive, ref} from 'vue';
import router from "@/router.js";
import getInstance from "@/sdk/Instance.js";
import {message} from "ant-design-vue";
import TopBar from "@/components/TopBar/TopBar.vue";
import {useRoute} from "vue-router";

const instance = getInstance()
onBeforeMount(() => {
    instance.whoami().then(res => {
        if (res.data.code === 1) {
            user = res.data.data;
            isAdmin.value = user.roleId;
            isShow.value = 1;
            if (isAdmin.value) {
                menuList.push(
                    {
                        id: '3',
                        name: '教师管理',
                        icon: 'UserOutlined',
                        url: '/home/user'
                    },
                    {
                        id: '4',
                        name: '管理员管理',
                        icon: 'ToolOutlined',
                        url: '/home/manager'
                    })
            }
            selectedKeys.value = [menuList.find(item => item.url === url.value).id]
        } else {
            message.info(res.data.msg)
            router.push("/login")
        }
    })
})
let user = reactive({roleId: undefined, userJobnumber: undefined, userName: undefined});
const isAdmin = ref();
const isShow = ref(0)
const url = ref(useRoute().path)
const menuList = reactive([
    {
        id: '1',
        name: '资产管理',
        icon: 'PayCircleOutlined',
        url: '/home/item'
    },
    {
        id: '2',
        name: '实验室管理',
        icon: 'HomeOutlined',
        url: '/home/lab'
    }])
const menuClick = (url) => {
    router.push(url)
}
const onCollapse = (collapsed, type) => {
    console.log(collapsed, type);
};
const onBreakpoint = broken => {
    console.log(broken);
};
const selectedKeys = ref(['1']); </script>
<style scoped>
@import "style.scss";
@import "../../scss/themeRed.scss";

.loading {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>