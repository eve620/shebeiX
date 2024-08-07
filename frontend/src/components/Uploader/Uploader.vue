<template>
  <div onClick=""
       onDrop=""
       onDragOver=""
       class="drop-area"
       ref={inputArea}
  >
    <Icon size=32 icon="upload"/>
    <p>点击上传文件或拖动文件夹到此区域上传，最大可上传MB的文件</p>
  </div>
  <Progress strokeLinecap="" percent=""/>
  <div class="upload-file-list">
    {result}
  </div>
  <input ref={fileInput} type="file" onChange="" accept="" style=""/>
</template>

<script setup>
import {ref, reactive, onMounted, computed, watchEffect} from 'vue';
import {Modal, Progress} from 'ant-design-vue';
import Icon from "@/components/Icon/Icon.vue";

const props = defineProps();
const emit = defineEmits(['update:active']);

const active = ref(false);
const path = ref('');
const uploadEnabled = ref(true);
const selected = ref(new Map());
const progress = ref(0);
const maxFileSize = ref(500);

onMounted(async () => {
  try {
  } catch (err) {
  }
});
watchEffect(() => {
  if (active.value) {
    emit('update:active', true);
  }
});
const setActive = (newActive) => {
  active.value = newActive;
};
const onUpload = async () => {
};
</script>

<style scoped>
</style>