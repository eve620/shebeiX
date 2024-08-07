<template>
  <div class="container">
    <div @dragover.prevent @drop.prevent="drop" @click="click" class="drop">
      拖拽空间
    </div>
    <input ref="fileInput" class="file" @change="handleFileChange" type="file" multiple/>
    <button class="select-button" @click="check">上传</button>
    <div class="file-list-container">
      文件列表
      <ul class="file-list">
        <li class="file-list-item" v-for="file of fileList">
          {{ file.file.name }}
          {{ file.currentChunk }}
          <button @click="file.resume()">start</button>
          <button @click="file.pause()">stop</button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import {nextTick, reactive, ref} from "vue";
import {UploadFile} from "@/sdk/upload.js";

const fileInput = ref(null)
const fileList = reactive([])

function handleFileChange(event) {
  const files = event.target.files;
  for (let i = 0; i < files.length; i++) {
    fileList.push(files[i]);
  }
  uploadFile()
}

function check() {
  console.log(fileList)
}

function drop(e) {
  for (let i = 0; i < e.dataTransfer.items.length; i++) {
    const item = e.dataTransfer.items[i];
    if (item.webkitGetAsEntry) {
      const entry = item.webkitGetAsEntry();
      if (entry.isDirectory) {
        readDirectory(entry);
      } else if (entry.isFile) {
        readFile(entry);
      }
    }
  }
}

function readDirectory(directoryEntry) {
  const reader = directoryEntry.createReader();
  reader.readEntries((entries) => {
    entries.forEach((entry) => {
      if (entry.isDirectory) {
        readDirectory(entry);
      } else if (entry.isFile) {
        readFile(entry);
      }
    });
  });
}

function readFile(fileEntry) {
  fileEntry.file((file) => {
    const uploadFile = reactive(new UploadFile(file))
    // 在这里可以处理文件，比如上传到服务器
    fileList.push(uploadFile)
    uploadFile.upload()
  });
}

function click() {
  if (fileInput.value) {
    fileInput.value.click();
  }
}

const uploadChunk = async (chunk, chunkNumber, totalChunks, md5) => {
  const formData = new FormData();
  formData.append('file', chunk);
  formData.append('chunkNumber', chunkNumber);
  formData.append('totalChunks', totalChunks);
  formData.append('identifier', md5);
  // const response = await fetch('/upload', {
  //   method: 'POST',
  //   body: formData,
  // });
  //
  // if (!response.ok) {
  //   throw new Error('上传分片失败');
  // }
};
const CHUNK_SIZE = 1024 * 1024;
const uploadFile = async () => {
  if (!fileList.length) return;
  const file = fileList[0]
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
  let currentChunk = 0;
  const md5Hash = await computeMD5(file);
  while (currentChunk < totalChunks) {
    const start = currentChunk * CHUNK_SIZE;
    const end = Math.min(file.size, start + CHUNK_SIZE);
    const chunk = file.slice(start, end);
    try {
      await uploadChunk(chunk, currentChunk + 1, totalChunks, md5Hash);
      currentChunk++;
      // progress.value = Math.round((currentChunk / totalChunks) * 100);
    } catch (error) {
      console.error(error);
      break;
    }
  }
  await uploadFile()
};

function computeMD5(file) {
  return new Promise((resolve, reject) => {
    const chunks = Math.ceil(file.size / CHUNK_SIZE);
    let currentChunk = 0;
    let spark = new SparkMD5.ArrayBuffer();
    const fileReader = new FileReader();

    fileReader.onload = function (e) {
      spark.append(e.target.result); // Append array buffer
      currentChunk++;

      if (currentChunk < chunks) {
        loadNext();
      } else {
        // Convert the final hash to a hexadecimal string
        resolve(spark.end());
      }
    };

    fileReader.onerror = reject;

    function loadNext() {
      const start = currentChunk * CHUNK_SIZE;
      const end = ((start + CHUNK_SIZE) >= file.size) ? file.size : start + CHUNK_SIZE;
      fileReader.readAsArrayBuffer(file.slice(start, end));
    }

    loadNext(); // Start loading the file
  });
}

</script>

<style lang="scss" scoped>
.container {
  display: flex;
  width: 30em;
  margin: auto;
  flex-direction: column;
  align-items: center;

  .drop {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border: #4d3b3b dotted;
    background-color: #817373;
    width: 100%;
    height: 10rem;
  }

  .file {
    display: none;
  }

  .select-button {
    cursor: pointer;
    margin: 0.5rem 0;
    align-self: end;
  }

  .file-list-container {
    width: 100%;

    .file-list {
      padding: 0;

      .file-list-item {
        list-style-type: none;
        padding: 10px;
      }
    }
  }
}
</style>

<!--<template>-->
<!--      <div class="container">-->
<!--          <div class="login-container">-->
<!--              <div class="login-display"></div>-->
<!--              <div class="login-form">-->
<!--                  <img src="/logo-mini.png" alt="logo"/>-->
<!--                  <div>-->
<!--                      <div class="user">-->
<!--                          <i class="iconfont icon-email"></i>-->
<!--                          <input v-model="loginForm.userAccount" placeholder="账号" autocomplete="off">-->
<!--                      </div>-->
<!--                      <div class="password">-->
<!--                          <i class="iconfont icon-password"></i>-->
<!--                          <input v-model="loginForm.userPassword" type="password" placeholder="密码" autocomplete="off"-->
<!--                                 @keyup.enter="handleLogin">-->
<!--                      </div>-->
<!--                      <button @click="handleLogin">登录</button>-->
<!--                  </div>-->
<!--              </div>-->
<!--          </div>-->
<!--      </div>-->
<!--</template>-->

<!--<script setup>-->

<!-- import {onBeforeMount, reactive, ref} from 'vue';-->
<!-- import router from "@/router.js";-->
<!-- import {message} from "ant-design-vue";-->
<!-- import getInstance from "@/sdk/Instance.js";-->

<!-- const instance = getInstance()-->

<!-- onBeforeMount(() => {-->
<!--   instance.whoami().then(res => {-->
<!--     if (res.data.code === 1) {-->
<!--       router.push("/home")-->
<!--     }-->
<!--   })-->
<!-- })-->
<!-- const loginForm = reactive({userAccount: '', userPassword: ''});-->
<!-- const handleLogin = () => {-->
<!--   instance.loginApi(loginForm).then(res => {-->
<!--     if (res.data.code === 1) {-->
<!--       router.push('/home')-->
<!--     } else {-->
<!--       message.info(res.data.msg)-->
<!--     }-->
<!--   })-->
<!-- }-->
<!--</script>-->
<!--<style scoped>-->
<!--@import "/font/iconfont.css";-->
<!--@import "style.scss";-->
<!--</style>-->