<template>
    <div class="loading" v-show="!isShow">
        <a-spin size="large"/>
    </div>
    <OperationBar v-show="isShow"/>
    <a-modal v-model:open="isEditShow" ok-text="确定" cancel-text="取消" @ok="onEditOk"
             @cancel="onEditCancel" title="编辑">
    </a-modal>
    <a-table :columns="columns"
             :data-source="dataSource"
             v-show="isShow"
             row-key="itemId"
             bordered>
        <template v-slot:bodyCell="{ column,record }">
            <span v-if="column.dataIndex==='operation'">
                  <a @click="onEdit()">编辑</a>
                  <Delete @delete="deleteItem" :tagetId="record.itemId"/>
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

const instance = getInstance()
let user = reactive({roleId: undefined, userAccount: undefined, userName: undefined});
const isAdmin = ref();
const isEditShow = ref(false);
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
const deleteItem = (itemId) => {
    isShow.value = false;
    instance.deleteItemById(itemId).then(res => {
        if(res.data.code === 1){
            message.info(res.data.data)
            instance.getItemList(input.value).then(res => {
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
    instance.getItemList(input.value).then(res => {
        dataSource.value = res.data.data;
        isShow.value = true;
    })
})
const input = ref('')
const dataSource = ref();
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
        title: '状态',
        dataIndex: 'itemStatus',
        width: '8%',
    }, {
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