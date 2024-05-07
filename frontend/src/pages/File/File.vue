<template>
  <div>
    <Uploader onSuccess="onUploadSuccess" ref="uploader"/>
    <a-modal okText="确定" cancelText="取消" title="新建文件夹" v-model:open="isNewDirOpen">
      <a-input type="text" placeHolder="文件夹名称" ref="newDirName"/>
    </a-modal>
    <a-modal okText="确定" cancelText="取消" title="修改文件名" v-model:open="isModifyDirNameActive">
      <a-input type="text" placeHolder="新文件名" ref="modifyDirName"/>
    </a-modal>
    <a-modal okText="确定" cancelText="取消" title="删除文件" v-model:open="isDelete">
      确认删除所选文件吗？
    </a-modal>
    <div class="file-container">
      <div class="file-menus">
        <Path/>
        <div class="flex-spacer"></div>
        <div class="button-groups">
          <Button v-if="isDeleteShow" text="删除" @click="" icon="trash"/>
          <Button v-if="isRenameShow" text="重命名" @click="" icon="modify"/>
          <Button v-if="isCutShow" text="剪切" @click="" icon="cut"/>
          <Button v-if="isPasteShow" text="粘贴" @click="" icon="paste"/>
          <Button v-if="isDownloadShow" text="下载" @click="" icon="download"/>
          <Button v-if="isUploadShow" text="上传" @click="" icon="upload"/>
          <Button v-if="isCreateDirShow" text="新建" @click="isNewDirOpen=true" icon="create"/>
        </div>
      </div>
      <Files ref="" @onChange="fileSelectedChange" @onClick="downloadOrOpen" :data="file"/>
    </div>
  </div>
</template>

<script setup>
import Files from "@/pages/File/Files.vue";
import Button from "@/components/Button/Button.vue";
import {computed, onBeforeMount, ref, watch} from "vue";
import Uploader from "@/components/Uploader/Uploader.vue";
import Path from "@/components/Path/Path.vue";
import {useRoute} from "vue-router";
import getInstance from "@/sdk/Instance.js";
import router from "@/router.js";

const route = useRoute()
const file = ref([])
const isNewDirOpen = ref(false)
const isDelete = ref(false)
const isModifyDirNameActive = ref(false)
const isPasteShow = ref(false)
const isRenameShow = ref(false)
const isDownloadShow = ref(false)
const isDeleteShow = ref(false)
const isCutShow = ref(false)
const isUploadShow = ref(false)
const isCreateDirShow = ref(false)
let register = ref(undefined)
const instance = getInstance()
const path = computed(() => {
  if (route.path === "/home/file" || route.path === "/home/file/") return ""
  return route.path.replace("/home/file/", "").endsWith("/") ?
      route.path.replace("/home/file/", "").slice(0, -1) :
      route.path.replace("/home/file/", "")
})
const downloadOrOpen = (file) => {
  console.log(file)
  // 大卡文件夹
  if (file.contentType === "dir") {
    const newPath = path.value.length === 0 ? "/home/file/" + file.name : "/home/file/" + path.value + "/" + file.name
    router.push(newPath)
  }
}
const refreshDir = () => {
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
  refreshDir()
})
const fileSelectedChange = (selected) => {
  isDownloadShow.value = selected && selected.size > 0
  instance.whoami().then(res => {
    if (!res) {
      isUploadShow.value = false
      isCreateDirShow.value = false
      isDeleteShow.value = false
      isRenameShow.value = false
      isPasteShow.value = false
      isCutShow.value = false
      return
    }
    isCutShow.value = selected && selected.size > 0
    isPasteShow.value = register.value !== undefined
    isRenameShow.value = selected && selected.size === 1
    isDeleteShow.value = selected && selected.size > 0
    isUploadShow.value = true
    isCreateDirShow.value = true
  })
}
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
    border: 1px solid $theme-border;
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