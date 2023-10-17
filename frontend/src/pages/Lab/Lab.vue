<template>
    <div class="loading" v-show="!isShow">
        <a-spin size="large"/>
    </div>
    <a-modal v-model:open="isAddShow" ok-text="确定" cancel-text="取消" @ok="onAddOk" @cancel="onAddCancel"
             title="添加">
        <a-form :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
            <a-form-item label="地点">
                <a-input v-model:value="formData.labName" placeholder="请输入地点"/>
            </a-form-item>
            <a-form-item label="管理人">
                <a-select v-model:value="formData.userName" placeholder="请选择">
                    <a-select-option v-for="item in userList" :value="item">{{item}}</a-select-option>
                </a-select>
            </a-form-item>
            <a-form-item label="工资号">
                <a-input v-model:value="formData.userAccount" :disabled="true"/>
            </a-form-item>
        </a-form>
    </a-modal>
    <OperationBar @add="addLab" @export="download" v-show="isShow"/>
    <a-modal v-model:open="isEditShow" ok-text="确定" cancel-text="取消" @ok="onEditOk"
             @cancel="onEditCancel" title="编辑">
        <a-form :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
            <a-form-item label="地点">
                <a-input v-model:value="formData.labName" :disabled="true"/>
            </a-form-item>
            <a-form-item label="管理人">
                <a-select v-model:value="formData.userName" placeholder="请选择">
                    <a-select-option v-for="item in userList" :value="item">{{item}}</a-select-option>
                </a-select>
            </a-form-item>
            <a-form-item label="工资号">
                <a-input v-model:value="formData.userAccount" :disabled="true"/>
            </a-form-item>
        </a-form>
    </a-modal>
    <a-table :columns="columns"
             :data-source="dataSource"
             v-show="isShow"
             row-key="itemId"
             bordered>
        <template v-slot:bodyCell="{ column,record }">
            <span v-if="column.dataIndex==='operation'">
                  <a @click="onEdit(record)">编辑</a>
                  <a class="check-button">查看</a>
                  <Delete @delete="deleteLab" :tagetId="record.labId" v-show="isAdmin"/>
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
import downloadExcel from "@/sdk/exportToExcel.js";

const instance = getInstance()
let user;
let userList;
const formData = ref({});
const isAdmin = ref(false);
const isAddShow = ref(false);
const addLab = async () => {
    formData.value = {};
    if(userList === undefined || labList === undefined){
        const userRes = await instance.getAllUserList()
        userList = userRes.data.data;
    }
    isAddShow.value = true;
};
const onAddOk = () => {
    isShow.value = false;
    instance.addLab(formData.value).then(res => {
        if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getLabList(input.value).then(res => {
                dataSource.value = res.data.data;
                isShow.value = true;
            })
        } else {
            message.info(res.data.msg)
            isShow.value = true;
        }
    })
    isAddShow.value = false;
};
const onAddCancel = () => {
    isAddShow.value = false;
};
const isEditShow = ref(false);
const onEdit = async (data) => {
    formData.value = JSON.parse(JSON.stringify(data));
    if(userList === undefined || labList === undefined){
        const userRes = await instance.getAllUserList()
        userList = userRes.data.data;
    }
    isEditShow.value = true;
};
const onEditOk = () => {
    isShow.value = false;
    instance.editLab(formData.value).then(res => {
        if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getLabList(input.value).then(res => {
                dataSource.value = res.data.data;
                isShow.value = true;
            })
        } else {
            message.info(res.data.msg)
            isShow.value = true;
        }
    })
    isEditShow.value = false;
};
const onEditCancel = () => {
    isEditShow.value = false;
};
const deleteLab = (labId) => {
    isShow.value = false;
    instance.deleteLabById(labId).then(res => {
        if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getLabList(input.value).then(res => {
                dataSource.value = res.data.data;
                isShow.value = true;
            })
        } else message.info(res.data.msg)
    })
}
onBeforeMount(() => {
    instance.whoami().then(res => {
        user = res.data.data;
        isAdmin.value = user.roleId;
    })
    instance.getLabList(input.value).then(res => {
        dataSource.value = res.data.data;
        isShow.value = true;
    })
})
const download = () => {
    downloadExcel("/lab",dataSource.value,"实验室表.xlsx")
}
const input = ref('')
const dataSource = ref();
const isShow = ref(false);
const columns = [
    {
        title: '实验室名称',
        dataIndex: 'labName',
        width: '15%',
    },
    {
        title: '管理员名称',
        dataIndex: 'userName',
        width: '15%',
    },
    {
        title: '管理员工资号',
        dataIndex: 'userAccount',
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