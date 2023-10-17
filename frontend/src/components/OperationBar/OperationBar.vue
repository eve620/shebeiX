<template>
    <div class="operation-bar">
        <a-input id="searchInput" v-model:value="searchInput" placeholder="请输入" style="width: 200px" allow-clear>
            <template #prefix>
                <SearchOutlined/>
            </template>
        </a-input>
        <div class="operation">
            <a-button type="primary" @click="onAdd">添加</a-button>
            <a-button class="export-excel" @click="onExport">导出Excel</a-button>
        </div>
    </div>
</template>

<script setup>
import {ref} from "vue";

const emit = defineEmits(['add', 'export']);
const searchInput = ref('');

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