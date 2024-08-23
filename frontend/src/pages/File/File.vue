<template>
  <div>
    <a-modal okText="确定" cancelText="取消" title="新建文件夹" v-model:open="isNewDirOpen" @ok="onCreateDirOk">
      <a-input type="text" placeholder="文件夹名称" v-model:value="newName"/>
    </a-modal>
    <a-modal okText="确定" cancelText="取消" title="修改文件名" v-model:open="isRenameActive" @ok="rename">
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
          <Button v-show="isDeleteShow" text="删除" @click="isDelete = true" icon="trash"/>
          <Button v-show="isRenameShow" text="重命名" @click="isRenameActive=true" icon="modify"/>
          <Button v-show="isDownloadShow" text="下载" @click="download" icon="download"/>
          <Button v-show="isUploadShow" text="上传" @click="isUpload=true" icon="upload"/>
          <Button v-show="isCreateDirShow" text="新建" @click="isNewDirOpen=true" icon="create"/>
        </div>
      </div>
      <Files ref="filesRef" @onChange="fileSelectedChange" @onClick="downloadOrOpen" :data="file"/>
    </div>
  </div>
</template>

<script setup>
import Files from "@/pages/File/Files.vue";
import Button from "@/components/Button/Button.vue";
import {computed, onBeforeMount, ref, watch} from "vue";
import Path from "@/components/Path/Path.vue";
import {useRoute} from "vue-router";
import getInstance from "@/sdk/Instance.js";
import router from "@/router.js";
import Uploader from "@/pages/File/Uploader.vue";
import {message} from "ant-design-vue";

const route = useRoute()
const file = ref([])
const filesRef = ref(null)
const uploaderRef = ref()
const newName = ref("");
const isUpload = ref(false)
const isNewDirOpen = ref(false)
const isDelete = ref(false)
const isRenameActive = ref(false)
const isRenameShow = ref(false)
const isDownloadShow = ref(false)
const isDeleteShow = ref(false)
const isUploadShow = ref(false)
const isCreateDirShow = ref(false)
const instance = getInstance()
const path = computed(() => {
  const decodedPath = decodeURIComponent(route.path);
  if (decodedPath === '/home/file' || decodedPath === '/home/file/') return '';
  return decodedPath.replace('/home/file/', '').replace(/\/+$/, '');
});

function rename() {
  message.info(newName.value)
  isRenameActive.value = false
}

function deleteFileOrDir() {
  message.info("删除成功")
  isDelete.value = false
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
  if (filesRef.value) {
    filesRef.value.active.forEach((item) => {
      if (item.identifier) {
        downloadFile(item.identifier)
      }
    })
  }
}

// 点击下载
function downloadFile(identifier, id) {
  console.log("file:>> ", file);
  window.location.href = `http://localhost:9000/fileStorage/download/${identifier}`;
}

function downloadOrOpen(file) {
  // 大卡文件夹
  if (file.fileType === "dir") {
    const newPath = path.value.length === 0 ? "/home/file/" + file.realName : "/home/file/" + path.value + "/" + file.realName
    router.push(newPath)
  }
}

async function onCreateDirOk() {
  const dirName = getInputValue()
  if (!isValidFilename(dirName)) {
    Pop({message: "文件夹名称不规范"})
    return
  }
  instance.createDir(pathJoin(path, dirName)).then(() => {
    newName.current.reset()
    refreshDir()
  }).catch(errHandler)
  setIsNewDirOpen(false)
}

function refreshDir() {
  instance.getFileList(path.value).then(res => {
    if (res) {
      file.value = res.data.data
    }
  }).catch(error => console.error(error))
}

onBeforeMount(() => {
  refreshDir()
})
watch(path, () => {
  if (route.path.startsWith("/home/file")) {
    refreshDir()
  }
})

function fileSelectedChange(selected) {
  isDownloadShow.value = selected && selected.size > 0
  instance.whoami().then(res => {
    if (!res) {
      isUploadShow.value = false
      isCreateDirShow.value = false
      isDeleteShow.value = false
      isRenameShow.value = false
      return
    }
    isRenameShow.value = selected && selected.size === 1
    isDeleteShow.value = selected && selected.size > 0
    isUploadShow.value = true
    isCreateDirShow.value = true
  })
}

onBeforeMount(() => {
  instance.whoami().then(res => {
    if (res.data.data.roleId) {
      isUploadShow.value = true
      isCreateDirShow.value = true
    }
  })
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