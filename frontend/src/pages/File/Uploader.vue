<template>
  <uploader
      ref="uploaderRef"
      :options="options"
      :autoStart="false"
      :file-status-text="fileStatusText"
      @file-added="onFileAdded"
      @file-success="onFileSuccess"
      @file-error="onFileError"
      @file-progress="onFileProgress"
      class="uploader-example"
  >
    Author：@JQG
    <uploader-unsupport></uploader-unsupport>
    <uploader-drop>
      <p>拖动文件到这里上传</p>
      <uploader-btn>选择文件</uploader-btn>
    </uploader-drop>
    <uploader-list>
      <a-collapse active-key(v-model)="[]" :bordered="false">
        <a-collapse-panel key="1" header="文件列表">
          <ul class="file-list">
            <li class="file-item" v-for="file in uploadFileList" :key="file.id">
              <uploader-file :file="file" :list="true" ref="uploaderFile"/>
            </li>
            <div class="no-file" v-if="!uploadFileList.length">
              <i class="icon icon-empty-file"></i> 暂无待上传文件
            </div>
          </ul>
        </a-collapse-panel>
      </a-collapse>
    </uploader-list>
  </uploader>
</template>

<script setup>
import {nextTick, ref, onMounted, reactive} from 'vue'
import {message} from "ant-design-vue";

const props = defineProps(["path"])
const emits = defineEmits(["refreshDir"])
const CHUNK_SIZE = 1 * 1024 * 1024;
const uploadFileList = reactive([])
const uploaderRef = ref(null)
const options = {
  target: 'http://localhost:9000/fileStorage/upload', // '//jsonplaceholder.typicode.com/posts/',
  testChunks: true,
  // 真正上传的时候使用的 HTTP 方法,默认 POST
  uploadMethod: "post",
  forceChunkSize: true,
  // 分片大小
  chunkSize: CHUNK_SIZE,
  // 并发上传数，默认为 3
  simultaneousUploads: 3,
  /**
   * 判断分片是否上传，秒传和断点续传基于此方法
   * 这里根据实际业务来 用来判断哪些片已经上传过了 不用再重复上传了 [这里可以用来写断点续传！！！]
   */
  checkChunkUploadedByResponse: (chunk, message) => {
    console.log("message", message)
    // message是后台返回
    let messageObj = JSON.parse(message);
    let dataObj = messageObj.data;
    if (dataObj.uploaded !== null) {
      return dataObj.uploaded;
    }
    // 判断文件或分片是否已上传，已上传返回 true
    // 这里的 uploadedChunks 是后台返回
    return (dataObj.uploadedChunks || []).indexOf(chunk.offset + 1) >= 0;
  },
  parseTimeRemaining: function (timeRemaining, parsedTimeRemaining) {
    //格式化时间
    return parsedTimeRemaining
        .replace(/\syears?/, "年")
        .replace(/\days?/, "天")
        .replace(/\shours?/, "小时")
        .replace(/\sminutes?/, "分钟")
        .replace(/\sseconds?/, "秒");
  },
  processParams: (file) => {
    return {
      chunkNumber: file.chunkNumber,
      chunkSize: file.chunkSize,
      currentChunkSize: file.currentChunkSize,
      totalSize: file.totalSize,
      identifier: file.identifier,
      filename: file.filename,
      relativePath: file.relativePath,
      totalChunks: file.totalChunks,
      dirPath: props.path
    }
  }
}

const fileStatusTextObj = {
  success: '上传成功',
  error: '上传失败',
  uploading: '上传中',
  paused: '已暂停',
  waiting: '等待上传'
}

function onFileAdded(file, event) {
  // event.preventDefault();
  uploadFileList.push(file);
  console.log("file :>> ", file);
  // 有时 fileType为空，需截取字符
  console.log("文件类型：" + file.fileType + "文件大小：" + file.size + "B");
  // 1. todo 判断文件类型是否允许上传
  // 2. 计算文件 MD5 并请求后台判断是否已上传，是则取消上传
  console.log("校验MD5");
  getFileMD5(file, (md5) => {
    if (md5 !== "") {
      // 修改文件唯一标识
      file.uniqueIdentifier = md5;
      // 请求后台判断是否上传
      // 恢复上传
      file.resume();
    }
  });
}

function onFileSuccess(rootFile, file, response, chunk) {
  console.log("上传成功", rootFile, file, response, chunk);
  emits("refreshDir")
  // 这里可以做一些上传成功之后的事情，比如，如果后端需要合并的话，可以通知到后端合并
}

function onFileError(rootFile, file, message, chunk) {
  console.log("上传出错：" + message, rootFile, file, message, chunk);
}

function onFileProgress(rootFile, file, chunk) {
  console.log(`当前进度：${Math.ceil(file._prevProgress * 100)}%`);
}

// 计算文件的MD5值
function getFileMD5(file, callback) {
  let spark = new SparkMD5.ArrayBuffer();
  let fileReader = new FileReader();
  //获取文件分片对象（注意它的兼容性，在不同浏览器的写法不同）
  let blobSlice =
      File.prototype.slice ||
      File.prototype.mozSlice ||
      File.prototype.webkitSlice;
  // 当前分片下标
  let currentChunk = 0;
  // 分片总数(向下取整)
  let chunks = Math.ceil(file.size / CHUNK_SIZE);
  // MD5加密开始时间
  let startTime = new Date().getTime();
  // 暂停上传
  file.pause();
  loadNext();
  // fileReader.readAsArrayBuffer操作会触发onload事件
  fileReader.onload = function (e) {
    // console.log("currentChunk :>> ", currentChunk);
    spark.append(e.target.result);
    if (currentChunk < chunks) {
      currentChunk++;
      loadNext();
    } else {
      // 该文件的md5值
      let md5 = spark.end();
      console.log(
          `MD5计算完毕：${md5}，耗时：${new Date().getTime() - startTime} ms.`
      );
      // 回调传值md5
      callback(md5);
    }
  };
  fileReader.onerror = function () {
    message.error("文件读取错误");
    file.cancel();
  };

  // 加载下一个分片
  function loadNext() {
    const start = currentChunk * CHUNK_SIZE;
    const end =
        start + CHUNK_SIZE >= file.size ? file.size : start + CHUNK_SIZE;
    // 文件分片操作，读取下一分片(fileReader.readAsArrayBuffer操作会触发onload事件)
    fileReader.readAsArrayBuffer(blobSlice.call(file.file, start, end));
  }
}

function fileStatusText(status, response) {
  if (status === "md5") {
    return "校验MD5";
  } else {
    return fileStatusTextObj[status];
  }
}

// 点击下载
function download(file, id) {
  console.log("file:>> ", file);
  window.location.href = `http://localhost:9000/fileStorage/download/${file.uniqueIdentifier}`;
}

onMounted(() => {
  nextTick(() => {
    window.uploader = uploaderRef.value.uploader
  })
})
</script>

<style scoped lang="scss">
::v-deep(.ant-collapse > .ant-collapse-item > .ant-collapse-content > .ant-collapse-content-box) {
  padding: 0;
}

.file-list {
  margin: 0;
  padding: 0;
  position: relative;
  height: 240px;
  overflow-x: hidden;
  overflow-y: auto;
  transition: all 0.3s;

  .file-item {
  }
}

.uploader-file-icon {
  &:before {
    content: '' !important;
  }

  &[icon='image'] {
    background: url(./images/image-icon.png);
  }

  &[icon=audio] {
    background: url(./images/audio-icon.png);
    background-size: contain;
  }

  &[icon='video'] {
    background: url(./images/video-icon.png);
  }

  &[icon='document'] {
    background: url(./images/text-icon.png);
  }

  &[icon=unknown] {
    background: url(./images/zip.png) no-repeat center;
    background-size: contain;
  }
}
</style>