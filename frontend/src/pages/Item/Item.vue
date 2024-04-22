<template>
  <div v-if="currentYear">
    <a-modal v-model:open="isAddShow" ok-text="确定" cancel-text="取消" @ok="onAddOk" @cancel="onAddCancel"
             title="添加">
      <a-form ref="formRef" :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
        <a-form-item name="itemType" label="资产类型" :rules="[{ required: true, message: '请选择'}]">
          <a-radio-group v-model:value="formData.itemType">
            <a-radio-button value="仪器设备">仪器设备</a-radio-button>
            <a-radio-button value="低值设备">低值设备</a-radio-button>
            <a-radio-button value="家具">家具</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="资产编号">
          <a-input v-model:value="formData.itemNumber" placeholder="请输入资产编号"/>
        </a-form-item>
        <a-form-item name="itemName" label="资产名称" :rules="[{ required: true, message: '请输入资产名称'}]">
          <a-input v-model:value="formData.itemName" placeholder="请输入资产名称"/>
        </a-form-item>
        <a-form-item label="型号">
          <a-input v-model:value="formData.itemModel" placeholder="请输入型号"/>
        </a-form-item>
        <a-form-item name="itemPrice" label="价值" :rules="[{ required: true, message: '请输入价值'}]">
          <a-input v-model:value="formData.itemPrice" placeholder="请输入价值"/>
        </a-form-item>
        <a-form-item label="净值">
          <a-input v-model:value="formData.itemNetworth" placeholder="请输入净值"/>
        </a-form-item>
        <a-form-item name="userName" label="领用人" :rules="[{ required: true, message: '请选择'}]">
          <a-select v-model:value="formData.userName" placeholder="请选择">
            <a-select-option v-for="item in userList" :value="item.userName">{{ item.userName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="itemUnit" label="领用单位" :rules="[{ required: true, message: '请选择'}]">
          <a-radio-group v-model:value="formData.itemUnit">
            <a-radio-button value="教育基地">教育基地</a-radio-button>
            <a-radio-button value="实验中心">实验中心</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="实验室">
          <a-select v-model:value="formData.labName" placeholder="请选择">
            <a-select-option v-for="item in labList" :value="item">{{ item }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="itemStatus" label="状态" :rules="[{ required: true, message: '请选择'}]">
          <a-radio-group v-model:value="formData.itemStatus">
            <a-radio-button value="在用">在用</a-radio-button>
            <a-radio-button value="停用">停用</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea
              v-model:value="formData.itemNote"
              placeholder="备注"
              :auto-size="{ minRows: 1, maxRows: 3 }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="isEditShow" ok-text="确定" cancel-text="取消" @ok="onEditOk"
             @cancel="onEditCancel" title="编辑">
      <a-form ref="formRef" :model="formData" :labelCol="{ span: 6 }" :wrapperCol="{ span: 14 }">
        <a-form-item name="itemType" label="资产类型" :rules="[{ required: true, message: '请选择'}]">
          <a-radio-group v-model:value="formData.itemType">
            <a-radio-button value="仪器设备">仪器设备</a-radio-button>
            <a-radio-button value="低值设备">低值设备</a-radio-button>
            <a-radio-button value="家具">家具</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="资产编号">
          <a-input v-model:value="formData.itemNumber" placeholder="请输入资产编号"/>
        </a-form-item>
        <a-form-item name="itemName" label="资产名称" :rules="[{ required: true, message: '请输入资产名称'}]">
          <a-input v-model:value="formData.itemName" placeholder="请输入资产名称"/>
        </a-form-item>
        <a-form-item label="型号">
          <a-input v-model:value="formData.itemModel" placeholder="请输入型号"/>
        </a-form-item>
        <a-form-item name="itemPrice" label="价值" :rules="[{ required: true, message: '请输入价值'}]">
          <a-input v-model:value="formData.itemPrice" placeholder="请输入价值"/>
        </a-form-item>
        <a-form-item label="净值">
          <a-input v-model:value="formData.itemNetworth" placeholder="请输入净值"/>
        </a-form-item>
        <a-form-item name="userName" label="领用人" :rules="[{ required: true, message: '请选择'}]">
          <a-select v-model:value="formData.userName" placeholder="请选择">
            <a-select-option v-for="item in userList" :value="item.userName">{{ item.userName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="itemUnit" label="领用单位" :rules="[{ required: true, message: '请选择'}]">
          <a-radio-group v-model:value="formData.itemUnit">
            <a-radio-button value="教学基地">教学基地</a-radio-button>
            <a-radio-button value="实验中心">实验中心</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="实验室">
          <a-select v-model:value="formData.labName" placeholder="请选择">
            <a-select-option v-for="item in labList" :value="item">{{ item }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="itemStatus" label="状态" :rules="[{ required: true, message: '请选择'}]">
          <a-radio-group v-model:value="formData.itemStatus">
            <a-radio-button value="在用">在用</a-radio-button>
            <a-radio-button value="停用">停用</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea
              v-model:value="formData.itemNote"
              placeholder="备注"
              :auto-size="{ minRows: 1, maxRows: 3 }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <div style="display: flex;justify-content: space-between;padding: 0 10px">
      <div style="cursor: pointer" @click="()=>{router.back()}">
        <LeftOutlined style="padding:0 5px 15px 0;font-size: 15px;color:#707070"/>
        <span style="color:#707070">返回</span>
      </div>
      <div>
        <span style="color:#707070;font-weight: bold">{{ currentYear + "年审查" }}</span>
        <Delete @delete="deleteYear" :tagetId="currentYear" v-show="currentYear"/>
      </div>
    </div>
    <OperationBar :addShow="isAdmin" :itemList="arr" :userList="userArr" @add="addItem" @export="download"
                  @handleSearch="handleSearch" @handleImport="handleImport"/>
    <div class="loading" v-show="!isShow">
      <a-spin size="large"/>
    </div>
    <a-table :columns="columns"
             :data-source="dataSource"
             v-show="isShow"
             @change="handleTableChange"
             row-key="itemId"
             bordered>
      <template v-slot:bodyCell="{ column,record }">
            <span v-if="column.dataIndex==='operation'">
                  <a @click="onEdit(record)">编辑</a>
                  <Delete @delete="deleteItem" :tagetId="record.itemId" v-show="isAdmin"/>
            </span>
      </template>
    </a-table>
  </div>
  <div v-else>
    <NavList/>
  </div>
</template>
<script setup>
import {computed, onBeforeMount, ref, watch} from 'vue';
import getInstance from "@/sdk/Instance.js";
import OperationBar from "@/components/OperationBar/OperationBar.vue";
import Delete from "@/components/Delete/Delete.vue";
import {message} from "ant-design-vue";
import downloadExcel from "@/sdk/exportToExcel.js";
import router from "@/router.js";
import {useRoute} from "vue-router";
import NavList from "@/pages/Item/ItemCheck.vue";

const instance = getInstance()
let user;
const userList = ref([]);
const labList = ref([]);
const formData = ref({});
const isAdmin = ref(false);
const isAddShow = ref(false);
const route = useRoute()
const currentYear = computed(() => route.query.year)
const addItem = async () => {
  formData.value = {};
  isAddShow.value = true;
};
const formRef = ref();
const dataSourceTemplate = ref([])
const dataSource = ref([]);
const isShow = ref(false);
const onAddOk = () => {
  formRef.value
      .validateFields()
      .then(values => {
        isShow.value = false;
        instance.addItem(formData.value).then(res => {
          if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getItemList(currentYear.value, null).then(res => {
              dataSourceTemplate.value = res.data.data;
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
  if (userList === undefined || labList === undefined) {
    const userRes = await instance.getAllUserList()
    const labRes = await instance.getAllLabList()
    userList.value = userRes.data.data;
    labList.value = labRes.data.data;
  }
  isEditShow.value = true;
};
const onEditOk = () => {
  formRef.value
      .validateFields()
      .then(values => {
        isShow.value = false;
        instance.editItem(formData.value).then(res => {
          if (res.data.code === 1) {
            message.info(res.data.data)
            instance.getItemList(currentYear.value, null).then(res => {
              dataSourceTemplate.value = res.data.data;
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
const deleteItem = (itemId) => {
  isShow.value = false;
  instance.deleteItemById(itemId).then(res => {
    if (res.data.code === 1) {
      message.info(res.data.data)
      instance.getItemList(currentYear.value, null).then(res => {
        dataSourceTemplate.value = res.data.data;
        dataSource.value = res.data.data;
        isShow.value = true;
      })
    } else message.info(res.data.msg)
  })
}
const handleImport = (file) => {
  instance.importExcel(file, currentYear.value).then(res => {
    if (res.data.code === 1) {
      message.info(res.data.data)
      instance.getItemList(currentYear.value, null).then(res => {
        dataSourceTemplate.value = res.data.data;
        dataSource.value = res.data.data;
        isShow.value = true;
      })
    } else {
      message.info(res.data.msg)
    }
  })
}
const deleteYear = (year) => {
  instance.deleteYearByName(year).then(res => {
    if (res.data.code === 1) {
      message.info(res.data.data)
      router.push("/home/item")
    } else {
      message.info(res.data.msg)
    }
  })
}
onBeforeMount(() => {
  instance.whoami().then(res => {
    if (res.data.code === 1) {
      user = res.data.data;
      isAdmin.value = user.roleId;
    }
  })
  instance.getAllUserList().then(res => {
    if (res.data.code === 1) {
      userList.value = res.data.data
    }
  })
  instance.getAllLabList().then(res => {
    if (res.data.code === 1) {
      labList.value = res.data.data
    }
  })
  if (currentYear.value) {
    instance.getItemList(currentYear.value, null).then(res => {
      dataSourceTemplate.value = res.data.data;
      dataSource.value = res.data.data;
      isShow.value = true;
    })
  }
})
watch(currentYear, (newValue, oldValue) => {
  if (newValue) {
    isShow.value = false
    instance.getItemList(newValue, null).then(res => {
      dataSourceTemplate.value = res.data.data;
      dataSource.value = res.data.data;
      isShow.value = true;
    })
  }
});
const download = () => {
  downloadExcel("/item", dataSource.value, "资产表.xlsx")
}
const set = computed(() => {
  return new Set(dataSourceTemplate.value.map(item => (item.itemName)))
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
    const itemMatch = itemSelected.length === 0 || itemSelected.some(keyword => item.itemName.includes(keyword));
    const userMatch = userSelected.length === 0 || userSelected.some(keyword => item.userName.includes(keyword));
    return itemMatch && userMatch;
  });
  isShow.value = true;
};
const handleTableChange = (pagination, filters, sorter) => {
  console.log(filters.itemUnit);
  if (!filters.itemUnit) {
    dataSource.value = dataSourceTemplate.value
  } else {
    dataSource.value = dataSourceTemplate.value.filter((item) => {
      return filters.itemUnit.some(keyword => item.itemUnit.includes(keyword));
    });
  }
};
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
  },
  {
    title: '型号',
    dataIndex: 'itemModel',
    width: '10%',
  }, {
    title: '价值',
    dataIndex: 'itemPrice',
    width: '10%',
  }, {
    title: '领用人',
    dataIndex: 'userName',
    width: '10%',
  }, {
    title: '领用单位',
    dataIndex: 'itemUnit',
    width: '8%',
    filters: [
      {text: '实验中心', value: '实验中心'},
      {text: '教学基地', value: '教学基地'},
    ],
  }, {
    title: '存放实验室',
    dataIndex: 'labName',
    width: '10%',
  }, {
    title: '状态',
    dataIndex: 'itemStatus',
    width: '5%',
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