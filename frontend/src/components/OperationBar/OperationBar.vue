<template>
    <div class="operation-bar">
        <a-input id="searchInput" v-model:value="searchInput" placeholder="请输入" style="width: 200px" allow-clear>
            <template #prefix>
                <SearchOutlined/>
            </template>
        </a-input>
        <a-modal v-model:open="isAddShow" ok-text="确定" cancel-text="取消" @ok="onAddOk" @cancel="onAddCancel" title="添加">
        </a-modal>
        <div class="operation">
            <a-button type="primary" @click="onAdd">添加</a-button>
            <a-button class="export-excel">导出Excel</a-button>
        </div>
    </div>
</template>

<script setup>
import {ref} from "vue";

const searchInput = ref('');
const isAddShow = ref(false);
const onAddOk = () => {
    isAddShow.value = false;
};
const onAddCancel = () => {
    isAddShow.value = false;
};
const onAdd = () => {
    isAddShow.value = true;
};
</script>

<style scoped lang="scss">
.operation-bar {
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
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