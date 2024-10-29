<template>
  <div>
    <a-modal okText="确定" cancelText="取消" title="新建文件夹" v-model:open="isNewDirOpen" @ok="onCreateDirOk" @cancel="newName=''">
      <a-input type="text" placeholder="文件夹名称" v-model:value="newName"/>
    </a-modal>
    <a-modal okText="确定" cancelText="取消" title="修改文件名" v-model:open="isRenameActive" @ok="rename" @cancel="newName=''">
      <a-input type="text" placeholder="新文件名" v-model:value="newName"/>
    </a-modal>
    <a-modal okText="确定" cancelText="取消" title="删除文件" @ok="deleteFileOrDir" v-model:open="isDelete">
      确认删除所选文件吗？
    </a-modal>
    <a-modal v-model:open="isUpload" ok-text="确定" cancel-text="取消" title="文件上传" width="80vw"
             @cancel="handleCancel"
             :footer="[]">
      <Uploader ref="uploaderRef" @refreshDir="refreshDir" :isUpload="isUpload" :path="path"/>
    </a-modal>
    <div class="file-container">
      <div class="file-menus">
        <Path/>
        <div class="flex-spacer"></div>
        <div class="button-groups">
          <Button v-show="selected&&selected.length>0" text="删除" @click="isDelete = true" icon="trash"/>
          <Button v-show="selected&&selected.length===1" text="重命名" @click="isRenameActive=true" icon="modify"/>
          <Button v-show="selected&&selected.length>0" text="下载" @click="download" icon="download"/>
          <Button text="上传" @click="isUpload=true" icon="upload"/>
          <Button text="新建" @click="isNewDirOpen=true" icon="create"/>
        </div>
      </div>
      <Files ref="filesRef" @onClick="downloadOrOpen" @select="select" :data="file"/>
    </div>
  </div>
</template>

<script setup>
import Files from "@/pages/File/Files.vue";
import Button from "@/components/Button/Button.vue";
import {computed, onBeforeMount, ref, watch} from "vue";
import Path from "@/components/Path/Path.vue";
import {useRoute} from "vue-router";
import router from "@/router.js";
import Uploader from "@/pages/File/Uploader.vue";
import {message} from "ant-design-vue";
import {validateDirOrFileName} from "@/sdk/utils.js";
import getInstance from "@/sdk/Instance.js";

const route = useRoute()
const file = ref([])
const filesRef = ref(null)
const uploaderRef = ref()
const newName = ref("");
const isUpload = ref(false)
const isNewDirOpen = ref(false)
const isDelete = ref(false)
const isRenameActive = ref(false)
const instance = getInstance()
const path = computed(() => {
  const decodedPath = decodeURIComponent(route.path);
  if (decodedPath === '/home/file' || decodedPath === '/home/file/') return '';
  return decodedPath.replace('/home/file/', '').replace(/\/+$/, '');
});
const selected = computed(() => {
  return file.value.filter(item => item.selected);
});
watch(path, () => {
  refreshDir()
})

function select(id) {
  const selectedItem = file.value.find(item => item.id === id);
  if (selectedItem) {
    selectedItem.selected = !selectedItem.selected; // 切换选中状态
  }
}

function rename() {
  if (validateDirOrFileName(newName.value)) {
    if (selected.value.length === 1) {
      selected.value.forEach((item) => {
        instance.renameFileOrDir(item.id, newName.value).then(res => {
          if (res.data.code === 1) {
            message.success("修改成功")
            newName.value = ""
            refreshDir()
          } else {
            message.error("修改失败")
          }
        })
      })
      isRenameActive.value = false
    }
  } else {
    message.warn("文件名不合法")
  }
}

async function deleteFileOrDir() {
  if (selected.value) {
    const deletePromise = []
    for (let item of selected.value) {
      deletePromise.push(instance.deleteFileOrDir(item.id))
    }
    isDelete.value = false
    try {
      const res = await Promise.all(deletePromise); // 使用 `await` 等待所有的删除操作完成
      message.success("删除成功")
      refreshDir()
    } catch (err) {
      message.error("删除失败")
    }
  }
}

function handleCancel() {
  if (uploaderRef.value.fileList.size) {
    for (let file of uploaderRef.value.fileList) {
      if ((file.currentChunk !== file.totalChunks) && file.isPaused) {
        message.info("上传中，请暂停或等待上传完成...")
        isUpload.value = true
        return
      }
    }
    uploaderRef.value.clearFilesList()
    refreshDir()
  }
  isUpload.value = false
}

function download() {
  if (selected.value) {
    if (selected.value.length === 1 && selected.value[0].fileType !== "dir") {
      //处理下载单独文件逻辑
      window.open("/fileStorage/download?id=" + selected.value[0].id);
      // window.location.href = baseURL + "/fileStorage/download?id=" + selected.value[0].id;
    } else {
      //处理下载多个文件压缩包逻辑
      const downloadIds = []
      selected.value.forEach(item => downloadIds.push(item.id))
      // // 排除所有文件夹
      const query = new URLSearchParams();
      downloadIds.forEach(id => query.append("id", id))
      window.open("/fileStorage/downloadZip?" + query);
      // window.location.href = baseURL + "/fileStorage/downloadZip?" + query
    }
  }
  refreshDir()
}

function downloadOrOpen(file) {
  // 大卡文件夹
  if (file.fileType === "dir") {
    const newPath = path.value.length === 0 ? "/home/file/" + file.realName : "/home/file/" + path.value + "/" + file.realName
    router.push(newPath)
  }
}

async function onCreateDirOk() {
  if (newName.value && validateDirOrFileName(newName.value)) {
    instance.createDir(path.value ? path.value + "/" + newName.value.trim() : newName.value.trim()).then(res => {
      if (res.data.code === 1) {
        message.success("创建成功")
        refreshDir()
      } else message.error("创建失败")
      newName.value = ""
      isNewDirOpen.value = false
    })
  } else {
    message.warn("文件夹不合法")
  }
}

function refreshDir() {
  instance.getFileList(path.value).then(res => {
    if (res) {
      file.value = res.data.data.map(file => {
        return {
          ...file,      // 展开原始文件对象
          selected: false // 添加 selected 属性并设置为 false
        };
      });
    }
  }).catch(error => console.error(error))
}

onBeforeMount(() => {
  refreshDir()
})
</script>

<style scoped lang="scss">
@import "../../scss/variable.scss";

.file-container {
  height: 100%;
  padding: 0 0 60px 0;
  width: 100%;
  border: 1px solid $theme-border;
  border-radius: 4px;

  > div {
    padding: 8px 12px;
    border-top: 1px solid $theme-border;
  }
}

.file-menus {
  display: flex;

  > .button-groups {
    display: flex;
    gap: 2px;
  }
}

.flex-spacer {
  flex-grow: 3;
}
</style>