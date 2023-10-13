<template>
    <div class="loading" v-show="!isShow">
        <a-spin size="large"/>
    </div>
    <a-table :columns="columns" :data-source="dataSource" v-show="isShow" row-key="itemId" bordered>
        <template v-slot:bodyCell="{ column,record }">
            <span v-if="column.dataIndex==='operation'">
                  <a @click="edit(record.key)">编辑</a>
            </span>
        </template>
    </a-table>
</template>
<script setup> import {onBeforeMount, reactive, ref} from 'vue';
import getInstance from "@/sdk/Instance.js";

const instance = getInstance()

let user = reactive({roleId: undefined, userAccount: undefined, userName: undefined});
const isAdmin = ref();
const input = ref('')
onBeforeMount(() => {
    instance.whoami().then(res => {
        user = res.data.data;
        isAdmin.value = user.roleId;
    })
    instance.getItemList(input.value).then(res => {
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
        title: '类型',
        dataIndex: 'itemType',
        width: '10%',
    },
    {
        title: '编号',
        dataIndex: 'itemNumber',
        width: '10%',
    },
    {
        title: '名称',
        dataIndex: 'itemName',
        width: '15%',
    }, {
        title: '型号',
        dataIndex: 'itemModel',
        width: '15%',
    }, {
        title: '价值',
        dataIndex: 'itemPrice',
        width: '10%',
    }, {
        title: '领用人',
        dataIndex: 'userName',
        width: '10%',
    }, {
        title: '存放实验室',
        dataIndex: 'labName',
        width: '10%',
    }, {
        title: '领用人',
        dataIndex: 'itemStatus',
        width: '10%',
    }, {
        title: '操作',
        dataIndex: 'operation',
    },
];
</script>
<style scoped>
@import "style.scss";
</style>