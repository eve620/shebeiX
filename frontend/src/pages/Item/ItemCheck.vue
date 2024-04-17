<template>
  <a-modal v-model:open="isAddCheckOpen" ok-text="确定" cancel-text="取消" @ok="" @cancel="" title="添加年份">
    <a-input :maxlength="4" id=year v-model:value="yearInput" placeHolder="请输入年份"/>
  </a-modal>
  <div>
    <div v-show="isAdmin" style="display: block; width: 100px;margin-left: auto;margin-bottom: 10px">
      <a-button type="primary" @click="onChangeAddCheck">添加</a-button>
    </div>
    <div class="list-content">
      <div class="list-button" @click="()=>{router.push('/home/item?year=2024')}">
        <span>2024年盘查</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import router from "@/router.js";
import {onBeforeMount, ref} from "vue";
import getInstance from "@/sdk/Instance.js";

let user;
const isAdmin = ref(false);
const yearInput = ref("")
const isAddCheckOpen = ref(false)
const instance = getInstance()

onBeforeMount(() => {
  instance.whoami().then(res => {
    if (res.data.code === 1) {
      user = res.data.data;
      isAdmin.value = user.roleId;
    }
  })
})
const onChangeAddCheck = () => {
  isAddCheckOpen.value = true;
}
</script>

<style scoped lang="scss">
.list-content {
  display: flex;
}

.list-button {
  display: flex;
  position: relative;
  font-weight: bold;
  justify-content: center;
  align-items: center;
  margin-right: 1rem;
  width: 8rem;
  border-radius: 10px;
  height: 6rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
  background-color: rgba(173, 255, 47, 0.4);
}

.list-button:hover {
  background-color: rgba(173, 255, 47, 0.6);
}
</style>