<template>
    <div class="loading" v-show="!isShow">
        <a-spin size="large"/>
    </div>
    <a-table :columns="columns" :data-source="dataSource" v-show="isShow" bordered>
        <template v-slot:bodyCell="{ column,record }">
            <span v-if="column.dataIndex==='operation'">
                  <a @click="edit(record.key)">编辑</a>
            </span>
        </template>
    </a-table>
</template>
<script setup>
import {onBeforeMount, ref} from 'vue';
import getInstance from "@/sdk/Instance.js";

const instance = getInstance()
const input = ref('')
onBeforeMount(() => {
    instance.getUserList(input.value).then(res => {
        dataSource.value = res.data.data;
        isShow.value = true;
    })
})
const dataSource = ref();
const edit = key => {
};
const isShow = ref(false);
const columns = [
    {
        title: '工资号',
        dataIndex: 'userAccount',
        width: '25%',
    },
    {
        title: '姓名',
        dataIndex: 'userName',
        width: '15%',
    },
    {
        title: '权限',
        dataIndex: 'roleId',
        width: '40%',
    },
    {
        title: '操作',
        dataIndex: 'operation',
    },
];

</script>
<style scoped>
@import "style.scss";
</style>