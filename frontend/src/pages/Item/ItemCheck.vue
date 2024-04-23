<template>
  <a-modal v-model:open="isAddCheckOpen" ok-text="确定" cancel-text="取消" @ok="onAddCheckOk" @cancel="onAddCheckCancel"
           title="添加审查">
    <a-input id=year v-model:value="yearInput" placeHolder="请输入"/>
  </a-modal>
  <div>
    <div v-show="isAdmin" style="display: block; width: 100px;margin-left: auto;margin-bottom: 10px">
      <a-button type="primary" @click="onChangeAddCheck">添加</a-button>
    </div>
    <div class="list-content" v-if="checkList.length">
      <div class="list-button" v-for="(item) in checkList" @click="()=>{router.push(`/home/item?year=${item}`)}"
           :key="item.id">
        {{ item + "审查" }}
      </div>
    </div>
    <div v-else>
      <a-empty description="请添加" />
    </div>
  </div>
</template>

<script setup>
import router from "@/router.js";
import {onBeforeMount, ref} from "vue";
import getInstance from "@/sdk/Instance.js";
import {message} from "ant-design-vue";
import Delete from "@/components/Delete/Delete.vue";

let user;
const isAdmin = ref(false);
const isCheckShow = ref(false);
const yearInput = ref("")
const isAddCheckOpen = ref(false)
const instance = getInstance()
const checkList = ref([]);

onBeforeMount(() => {
  instance.whoami().then(res => {
    if (res.data.code === 1) {
      user = res.data.data;
      isAdmin.value = user.roleId;
    }
  })
  instance.getYearList().then(res => {
    checkList.value = res.data.data;
  })
})

const onChangeAddCheck = () => {
  isAddCheckOpen.value = true;
}
const onAddCheckOk = () => {
  instance.addYear(
      {year: yearInput.value}
  ).then(res => {
    if (res.data.code === 1) {
      message.info(res.data.data)
      instance.getYearList().then(res => {
        checkList.value = res.data.data;
      })
    } else message.info(res.data.msg)
  })
  yearInput.value = '';
  isAddCheckOpen.value = false;
}
const onAddCheckCancel = () => {
  yearInput.value = '';
  isAddCheckOpen.value = false;
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