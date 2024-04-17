<template>
  <div>
    <div class="list-content">
      <div class="list-button" @click="()=>{router.push('/home/lab?year=2024')}">
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
const instance = getInstance()

onBeforeMount(() => {
  instance.whoami().then(res => {
    if (res.data.code === 1) {
      user = res.data.data;
      isAdmin.value = user.roleId;
    }
  })
})

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