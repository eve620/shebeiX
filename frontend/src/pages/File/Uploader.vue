<template>
  <div>
    <div @dragover.prevent @drop.prevent="drop" @click="click" class="drop">
      <UploadOutlined style="font-size: 32px;color: #1296db"/>
      <p>点击上传文件或拖动文件夹到此区域上传</p>
    </div>
    <div class="file-list-container">
      <div
          :style="{
         'background': `linear-gradient(to right,
           rgba(76,172,175,0.2) ${computeProgress(entry) * 100 - 1}%,
           rgba(76,172,175,0.2) ${computeProgress(entry) * 100}%,
           #fff ${computeProgress(entry) * 100}%,
           #fff ${computeProgress(entry) * 100 + 1}%)`
       }"
          :key="index" v-for="(entry,index) of fileList" class="upload-file-item">
        <span style="overflow: hidden;text-overflow: ellipsis; white-space: nowrap;padding-right: 10px">{{
            entry.name
          }}</span>
        <span>{{ formatBytes(entry.size) }}</span>
        <span>{{ entry.type === "file" ? "文件" : "文件夹" }}</span>
        <span>{{ checkUpload(entry) ? "上传完成" : (entry.isPaused ? "等待继续" : "上传中") }}</span>
        <div>
          <CaretRightOutlined @click="resumeUpload(entry)" v-if="!checkUpload(entry) && entry.isPaused"
                              style="font-size: 16px"/>
          <PauseOutlined @click="pauseUpload(entry)" v-if="!checkUpload(entry) && !entry.isPaused"
                         style="font-size: 16px"/>
          <CloseOutlined @click="deleteFile(entry)" v-if="!checkUpload(entry) && entry.isPaused"
                         style="font-size: 14px;margin-left: 5px"/>
          <CheckOutlined v-if="checkUpload(entry)" style="font-size: 16px"/>
        </div>
      </div>
      <button @click="check">测试上传</button>
      <input ref="fileInput" class="file" @change="handleFileChange" type="file" multiple/>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {UploadFile} from "@/sdk/upload.js";
import {message} from "ant-design-vue";
import {UploadOutlined, CheckOutlined, PauseOutlined, CaretRightOutlined, CloseOutlined} from '@ant-design/icons-vue';
import {deletePrefixSlash} from "@/sdk/utils.js";
import getInstance from "@/sdk/Instance.js";

const fileInput = ref(null)
const fileList = reactive(new Set())
const props = defineProps(["path"])
const dirPath = ref("")
const instance = getInstance()

defineExpose({
  fileList,
  clearFilesList
})

function clearFilesList() {
  fileList.clear()
}

function computeProgress(entry) {
  let total = 0
  let uploaded = 0
  for (let [_, value] of entry.children) {
    total += value.totalChunks
    uploaded += value.currentChunk
  }
  return uploaded / total
}

function checkUpload(file) {
  for (let [_, value] of file.children) {
    if (value && !value.isCompleted) {
      return false
    }
  }
  return true
}

function pauseUpload(entry) {
  entry.isPaused = true
  entry.children.forEach((value, _) => {
    if (value) {
      value.pause()
    }
  })
}

function resumeUpload(entry) {
  entry.isPaused = false
  entry.children.forEach((value, _) => {
    if (value) {
      value.resume()
    }
  })
}

function deleteFile(file) {
  // todo 发送请求根据md5删除
  fileList.delete(file)
}

//点击上传
function handleFileChange(event) {
  const files = event.target.files;
  for (let i = 0; i < files.length; i++) {
    const uploadFile = reactive(new UploadFile(files[i]))
    const children = new Map()
    let had = false
    for (let item of fileList) {
      if (item.name === files[i].name) {
        had = true
        break
      }
    }
    if (had) {
      message.info(`${files[i].name}已存在`)
      continue
    }
    children.set(files[i].name, uploadFile)
    const entryInfo = {
      children,
      name: files[i].name,
      size: files[i].size,
      type: "file",
      isPaused: false,
    }
    fileList.add(entryInfo)
    uploadFile.upload(files[i].name, props.path)
  }
}

function formatBytes(byteLen) {
  if (!byteLen) {
    return ""
  }
  const units = ["B", "KB", "MB", "GB", "TB"]
  let index = 0
  while (index < units.length) {
    if (byteLen < 1024) {
      break;
    }
    byteLen = byteLen / 1024
    index++
  }
  return Math.ceil(byteLen * 100) / 100 + " " + units[index];
}

function check() {
  console.log(fileList)
}

async function drop(e) {
  for (let i = 0; i < e.dataTransfer.items.length; i++) {
    const item = e.dataTransfer.items[i];
    if (item.webkitGetAsEntry) {
      const children = new Map()
      const entry = item.webkitGetAsEntry();
      let had = false
      for (let item of fileList) {
        if (item.name === entry.name) {
          had = true
          break
        }
      }
      if (had) {
        message.info(`${entry.name}已存在`)
        continue
      }
      const entryInfo = {
        children,
        name: entry.name,
        size: 0,
        type: entry.isDirectory ? "dir" : "file",
        isPaused: false,
      }
      if (entry.isDirectory) {
        //todo
        await instance.createDir(deletePrefixSlash(props.path + entry.fullPath))
        await readDirectory(entry, entryInfo);
      } else {
        await readFile(entry, entryInfo);
      }
      entryInfo.size = Array.from(entryInfo.children.values())
          .reduce((totalSize, child) => totalSize + child.file.size, 0);
      for (const [key, value] of entryInfo.children) {
        value.upload(deletePrefixSlash(key), props.path);
      }
      fileList.add(entryInfo)
    }
  }
}

async function readDirectory(directoryEntry, entryInfo) {
  const reader = directoryEntry.createReader();
  return new Promise((resolve) => {
    reader.readEntries((entries) => {
      const promises = entries.map((entry) => {
        if (entry.isDirectory) {
          instance.createDir(deletePrefixSlash(props.path + entry.fullPath))
          return readDirectory(entry, entryInfo);
        } else if (entry.isFile) {
          return readFile(entry, entryInfo);
        }
        return Promise.resolve();
      });
      Promise.all(promises).then(() => resolve());
    });
  });
}

function readFile(fileEntry, entryInfo) {
  return new Promise((resolve, reject) => {
    fileEntry.file((file) => {
      const uploadFile = reactive(new UploadFile(file));
      entryInfo.children.set(fileEntry.fullPath, uploadFile);
      resolve();
    }, reject);
  });
}

function click() {
  if (fileInput.value) {
    fileInput.value.value = ''
    fileInput.value.click();
  }
}
</script>

<style lang="scss" scoped>
.upload-file-item {
  padding: 0 10px;
  height: 48px;
  border-bottom: 1px solid #EBEBEB;
  display: grid;
  align-items: center;
  grid-template-columns: minmax(100px, 1fr) 90px 60px 120px 60px;
}

.drop {
  border: 2px dashed #EBEBEB;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 0;
  cursor: pointer;

  p {
    color: #414141;
  }
}

.file {
  display: none;
}

.select-button {
  cursor: pointer;
  margin: 0.5rem 0;
  align-self: end;
}


</style>