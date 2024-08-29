<template>
  <div>
    <table v-if="props.data.length!==0" class="file-table">
      <thead>
      <tr>
        <th>
          <div style="width: 15px"/>
        </th>
        <th class="sortable">
          <span>文件名称</span>
        </th>
        <th>文件大小</th>
        <th class="sortable">
          <span>文件类型</span>
        </th>
        <th>上传者</th>
        <th class="sortable">
          <span>上传时间</span>
        </th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(item) in props.data" :key="item"
          :class="{ 'clickable': item.fileType === 'dir' }">
        <td>
          <a-checkbox v-model:checked="item.selected" @click="() => emits('select', item.id)"/>
        </td>
        <td @click="emits('onClick', item)">
          <FolderOutlined v-if="item.fileType === 'dir'" style="margin-right: 4px;color: #888888"/>
          <FileOutlined v-else style="margin-right: 4px; color: #888888"/>
          {{ item.realName }}
        </td>
        <td>{{ formatBytes(item.size) }}</td>
        <td>{{ getFileType(item.fileType) }}</td>
        <td>{{ item.createBy }}</td>
        <td>{{ formatDate(new Date(item.createTime)) }}</td>
      </tr>
      </tbody>
    </table>
    <div v-else class="empty">
      <div>
        <a-empty description="什么都没有呢"/>
      </div>
    </div>

  </div>
</template>

<script setup>
import {reactive, ref, watch} from "vue";
import {formatBytes, formatDate, getFileType} from "@/sdk/utils.js";
import {FolderOutlined, FileOutlined} from '@ant-design/icons-vue';

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})
const emits = defineEmits(['onChange', 'onClick', 'select'])
const selected = ref(new Set())
const data = reactive(props.data)
watch(selected.value, () => {
  emits("onChange", selected.value)
})
</script>

<style scoped lang="scss">
@import "../../scss/variable.scss";

.file-table {
  width: 100%;

  th {
    text-align: left;
    color: $theme-subtext;
    font: $theme-font-emphasize;
  }

  tr {
    height: 48px;
  }

  thead {
    th {
      border-bottom: 1px solid $theme-border;
    }

    .sortable {
      cursor: pointer;
    }

    .clickable td:nth-child(2) {
      cursor: pointer;
    }

    .asc::after {
      content: '';
      display: inline-block;
      position: relative;
      margin-left: 5px; /* 调整三角离span标签的距离 */
      width: 0;
      height: 0;
      border-right: 5px solid transparent;
      border-bottom: 8px solid $theme-subtext;
      border-left: 5px solid transparent;
    }

    .desc::after {
      content: '';
      display: inline-block;
      position: relative;
      margin-left: 5px; /* 调整三角离span标签的距离 */
      width: 0;
      height: 0;
      border-right: 5px solid transparent;
      border-top: 8px solid $theme-subtext;
      border-left: 5px solid transparent;
    }

    th:first-child {
      padding-left: 20px;
      padding-right: 20px;
    }

    th:last-child {
      padding-right: 20px;
    }
  }

  tbody {
    td:first-child {
      width: 40px;
    }

    .clickable {
      td:nth-child(2) {
        cursor: pointer;
      }
    }

    tr:not(.active) {
      &:hover {
        background: $theme-hover;
      }
    }

    .active {
      background-color: $theme-hover;
    }

    td {
      &:nth-child(1) {
        padding-left: 20px;
      }

      border-bottom: 1px solid $theme-border;
    }

  }
}

.empty {
  box-sizing: border-box;
  width: 100%;
  display: flex;
  justify-content: center;

  > div {
    display: flex;
    flex-flow: column;
  }
}
</style>