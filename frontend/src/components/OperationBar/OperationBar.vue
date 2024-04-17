<template>
  <div class="operation-bar">
    <div style="flex: 1">
      <div style="display: inline-block;width: 50%;padding-right: 10px">
        <a-select
            v-model:value="itemSelected"
            mode="multiple"
            style="min-width: 80%"
            placeholder="请输入名称"
            :options="itemList"
            @change="onSearch"
        ></a-select>
      </div>
      <div style="display: inline-block;width: 50%;padding-right: 10px">
        <a-select
            v-model:value="userSelected"
            mode="multiple"
            style="min-width: 80%"
            placeholder="请输入姓名"
            :options="userList"
            @change="onSearch"
        ></a-select>
      </div>
    </div>
    <div class="operation">
      <a-button type="primary" @click="onAdd" v-show="addShow">添加</a-button>
      <a-button class="import-excel" v-show="addShow && useRoute().path.startsWith('/home/item')">导入Excel
      </a-button>
      <a-button class="export-excel" @click="onExport">导出Excel</a-button>
    </div>
  </div>
</template>

<script setup>
import {ref} from "vue";
import {useRoute} from "vue-router";

const emit = defineEmits(['add', 'export', 'handleSearch']);
const props = defineProps(['addShow', 'itemList', 'userList']);
const searchInput = ref('');
const itemSelected = ref([])
const userSelected = ref([])
const onSearch = () => {
  emit('handleSearch', {itemSelected: itemSelected.value, userSelected: userSelected.value});
};
const onAdd = () => {
  emit('add');
};
const onExport = () => {
  emit('export');
};
</script>

<style scoped lang="scss">
.operation-bar {
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
}

.import-excel {
  background-color: #28ad67;
  color: #fff;
  border-style: none;
  margin-left: 8px;

  &:hover {
    background-color: #17a25a;
    color: #fff;
  }

  &:active {
    background-color: #0d6534;
    color: #fff;
  }
}

.export-excel {
  background-color: #107C41;
  color: #fff;
  border-style: none;
  margin-left: 8px;

  &:hover {
    background-color: #128c4b;
    color: #fff;
  }

  &:active {
    background-color: #0d6534;
    color: #fff;
  }
}

</style>