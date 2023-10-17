<template>
    <div class="loading" v-show="!isShow">
        <a-spin size="large"/>
    </div>
    <a-modal v-model:open="isAddShow" ok-text="确定" cancel-text="取消" @ok="onAddOk" @cancel="onAddCancel"
             title="添加">
        <a-form :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
            <a-form-item label="账号">
                <a-input v-model:value="formData.userAccount" placeholder="请输入账号"/>
            </a-form-item>
            <a-form-item label="用户名">
                <a-input v-model:value="formData.userName" placeholder="请输入用户名"/>
            </a-form-item>
            <a-form-item label="密码">
                <a-input v-model:value="formData.userPassword" placeHolder="请输入密码"/>
            </a-form-item>
        </a-form>
    </a-modal>
    <OperationBar @add="addManager" v-show="isShow"/>
    <a-modal v-model:open="isEditShow" ok-text="确定" cancel-text="取消" @ok="onEditOk"
             @cancel="onEditCancel" title="编辑">
        <a-form :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
            <a-form-item label="账号">
                <a-input v-model:value="formData.userAccount" placeholder="请输入账号"/>
            </a-form-item>
            <a-form-item label="用户名">
                <a-input v-model:value="formData.userName" placeholder="请输入用户名"/>
            </a-form-item>
            <a-form-item label="权限">
                <a-radio-group v-model:value="formData.roleId">
                    <a-radio-button :value="1">管理员</a-radio-button>
                    <a-radio-button :value="0">教师</a-radio-button>
                </a-radio-group>
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

const instance = getInstance()
let user;
const formData = ref({});
const isAdmin = ref(false);
const isAddShow = ref(false);
const addManager = () => {
    formData.value = {};
    isAddShow.value = true;
};
const onAddOk = () => {
    isShow.value = false;
    instance.addManager(formData.value).then(res => {
        if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getManagerList(input.value).then(res => {
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
const isRole = ref(false);
const onEdit = (data) => {
    formData.value = JSON.parse(JSON.stringify(data));
    isEditShow.value = true;
};
const onEditOk = () => {
    isShow.value=false;
    instance.editUser(formData.value).then(res => {
        if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getManagerList(input.value).then(res => {
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

const deleteManager = (userId) => {
    isShow.value = false;
    instance.deleteUserById(userId).then(res => {
        if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getManagerList(input.value).then(res => {
                dataSource.value = res.data.data;
                isShow.value = true;
            })
        } else {
            message.info(res.data.msg)
            isShow.value = true;
        }
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
        title: '用户名',
        dataIndex: 'userName',
        width: '15%',
    },
    {
        title: '权限',
        dataIndex: 'roleId',
        width: '40%',
        customRender(text){
            return text.value===1?'管理员':'教师'
        }
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