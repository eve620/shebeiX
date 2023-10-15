<template>
    <div class="loading" v-show="!isShow">
        <a-spin size="large"/>
    </div>
    <OperationBar v-show="isShow"/>
    <a-table :columns="columns"
             :data-source="dataSource"
             v-show="isShow"
             row-key="itemId"
             bordered>
        <template v-slot:bodyCell="{ column,record }">
            <span v-if="column.dataIndex==='operation'">
                  <a-modal v-model:open="isEditShow" ok-text="确定" cancel-text="取消" @ok="onEditOk"
                           @cancel="onEditCancel" title="编辑">
                  </a-modal>
                  <a @click="onEdit()">编辑</a>
                  <Delete @delete="deleteManager" :tagetId="record.userId"/>
            </span>
        </template>
    </a-table>
</template>
<script setup>
import {onBeforeMount, reactive, ref} from 'vue';
import getInstance from "@/sdk/Instance.js";
import OperationBar from "@/components/OperationBar/OperationBar.vue";
import Delete from "@/components/Delete/Delete.vue";
import {message} from "ant-design-vue";
const isEditShow = ref(false);
const instance = getInstance()
let user = reactive({roleId: undefined, userAccount: undefined, userName: undefined});
const isAdmin = ref();
const onEdit = () => {
    isEditShow.value = true;
};
const onEditOk = () => {
    //编辑逻辑
    isEditShow.value = false;
};
const onEditCancel = () => {
    isEditShow.value = false;
};

const deleteManager = (userId) => {
    isShow.value = false;
    instance.deleteUserById(userId).then(res => {
        if(res.data.code === 1){
            message.info(res.data.data)
            instance.getManagerList(input.value).then(res => {
                dataSource.value = res.data.data;
                isShow.value = true;
            })
        }
        else message.info(res.data.msg)
    })
}
onBeforeMount(() => {
    instance.whoami().then(res => {
        user = res.data.data;
        isAdmin.value = user.roleId;
    })
    instance.getManagerList(input.value).then(res => {
        dataSource.value = res.data.data;
        isShow.value = true;
    })
})
const input = ref('')
const dataSource = ref();
const isShow = ref(false);
const columns = [
    {
        title: '账号',
        dataIndex: 'userAccount',
        width: '15%',
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
<style scoped lang="scss">
@import "../../scss/content";

:deep(.ant-input-clear-icon) {
    line-height: normal;
}

</style>