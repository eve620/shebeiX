<template>
  <a-modal v-model:open="isAddShow" ok-text="确定" cancel-text="取消" @ok="onAddOk" @cancel="onAddCancel"
           title="添加">
    <a-form ref="formRef" :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
      <a-form-item name="labName" label="地点" :rules="[{ required: true, message: '请输入地点' }]">
        <a-input v-model:value="formData.labName" placeholder="请输入地点"/>
      </a-form-item>
      <a-form-item name="userName" label="管理人" :rules="[{ required: true, message: '请选择管理人' }]">
        <a-select v-model:value="formData.userName" placeholder="请选择">
          <a-select-option v-for="item in userList" :value="item.userName"
                           @click="setAccount(item.userAccount)">{{ item.userName }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="工资号">
        <a-input v-model:value="formData.userAccount" :disabled="true"/>
      </a-form-item>
    </a-form>
  </a-modal>
  <a-modal v-model:open="isEditShow" ok-text="确定" cancel-text="取消" @ok="onEditOk"
           @cancel="onEditCancel" title="编辑">
    <a-form ref="formRef" :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
      <a-form-item label="地点">
        <a-input v-model:value="formData.labName" :disabled="true"/>
      </a-form-item>
      <a-form-item label="管理人">
        <a-select v-model:value="formData.userName" placeholder="请选择">
          <a-select-option v-for="item in userList" :value="item.userName"
                           @click="setAccount(item.userAccount)">{{ item.userName }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="工资号">
        <a-input v-model:value="formData.userAccount" :disabled="true"/>
      </a-form-item>
    </a-form>
  </a-modal>
  <OperationBar :item-list="arr" :user-list="userArr" :addShow="isAdmin" @add="addLab" @export="download"
                @handleSearch="handleSearch"/>
  <div class="loading" v-show="!isShow">
    <a-spin size="large"/>
  </div>
  <a-table :columns="columns"
           :data-source="dataSource"
           v-show="isShow"
           row-key="itemId"
           bordered>
    <template v-slot:bodyCell="{ column,record }">
            <span v-if="column.dataIndex==='operation'">
                  <a @click="onEdit(record)">编辑</a>
                  <a class="check-button" @click="checkDetail(record.labName)">查看</a>
                  <Delete @delete="deleteLab" :tagetId="record.labId" v-show="isAdmin"/>
            </span>
    </template>
  </a-table>
</template>
<script setup>
import {computed, onBeforeMount, ref} from 'vue';
import getInstance from "@/sdk/Instance.js";
import OperationBar from "@/components/OperationBar/OperationBar.vue";
import Delete from "@/components/Delete/Delete.vue";
import {message} from "ant-design-vue";
import downloadExcel from "@/sdk/exportToExcel.js";
import router from "@/router.js";
import {encryptByAES} from "@/sdk/utils.js";
import {useRoute} from "vue-router";

const instance = getInstance()
let user;
let userList;
const formData = ref({});
const isAdmin = ref(false);
const isAddShow = ref(false);
const route = useRoute()

const setAccount = (num) => {
  formData.value.userAccount = num;
}
const addLab = async () => {
  formData.value = {};
  if (userList === undefined) {
    const userRes = await instance.getAllUserList()
    userList = userRes.data.data;
  }
  isAddShow.value = true;
};
//查看实验室详情
const checkDetail = (labName) => {
  // router.push({name: 'labDetail', query: {year: currentYear.value, id: encryptByAES(labName)}})
  router.push({name: 'labDetail', query: {id: encryptByAES(labName)}})
}
const formRef = ref();
const onAddOk = () => {
  formRef.value
      .validateFields()
      .then(values => {
        isShow.value = false;
        instance.addLab(formData.value).then(res => {
          if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getLabList().then(res => {
              dataSourceTemplate.value = res.data.data
              dataSource.value = res.data.data;
              isShow.value = true;
            })
          } else {
            message.info(res.data.msg)
            isShow.value = true;
          }
        })
        isAddShow.value = false;
      }).catch(info => {
        console.log('Validate Failed:', info);
      }
  )
};
const onAddCancel = () => {
  formRef.value.resetFields();
  isAddShow.value = false;
};
const isEditShow = ref(false);
const onEdit = async (data) => {
  formData.value = JSON.parse(JSON.stringify(data));
  if (userList === undefined) {
    const userRes = await instance.getAllUserList()
    userList = userRes.data.data;
  }
  isEditShow.value = true;
};
const onEditOk = () => {
  formRef.value
      .validateFields()
      .then(values => {
        isShow.value = false;
        instance.editLab(formData.value).then(res => {
          if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getLabList().then(res => {
              dataSourceTemplate.value = res.data.data
              dataSource.value = res.data.data;
              isShow.value = true;
            })
          } else {
            message.info(res.data.msg)
            isShow.value = true;
          }
        })
        isEditShow.value = false;
      }).catch(info => {
        console.log('Validate Failed:', info);
      }
  )
};
const onEditCancel = () => {
  formRef.value.resetFields();
  isEditShow.value = false;
};
const deleteLab = (labId) => {
  isShow.value = false;
  instance.deleteLabById(labId).then(res => {
    if (res.data.code === 1) {
      message.info(res.data.data)
      instance.getLabList().then(res => {
        dataSourceTemplate.value = res.data.data
        dataSource.value = res.data.data;
        isShow.value = true;
      })
    } else message.info(res.data.msg)
  })
}
onBeforeMount(() => {
  instance.whoami().then(res => {
    if (res.data.code === 1) {
      user = res.data.data;
      isAdmin.value = user.roleId;
    }
  })
  instance.getLabList().then(res => {
    dataSourceTemplate.value = res.data.data
    dataSource.value = res.data.data;
    isShow.value = true;
  })
})
const download = () => {
  downloadExcel("/lab", dataSource.value, "实验室表.xlsx")
}
const dataSource = ref();
const dataSourceTemplate = ref([])
const isShow = ref(false);
const set = computed(() => {
  return new Set(dataSourceTemplate.value.map(item => (item.labName)))
})
const arr = computed(() => {
  let array = []
  set.value.forEach((item) => {
    array.push({
      value: item,
    })
  })
  return array
})
const userSet = computed(() => {
  return new Set(dataSourceTemplate.value.map(item => (item.userName)))
})
const userArr = computed(() => {
  let array = []
  userSet.value.forEach((item) => {
    array.push({
      value: item,
    })
  })
  return array
})
const handleSearch = (searchParams) => {
  isShow.value = false;
  dataSource.value = dataSourceTemplate.value.filter((item) => {
    const itemSelected = searchParams.itemSelected;
    const userSelected = searchParams.userSelected;
    const itemMatch = itemSelected.length === 0 || itemSelected.some(keyword => item.labName.includes(keyword));
    const userMatch = userSelected.length === 0 || userSelected.some(keyword => item.userName.includes(keyword));
    return itemMatch && userMatch;
  });
  isShow.value = true;
};
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