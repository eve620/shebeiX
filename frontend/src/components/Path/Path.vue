<template>
  <div>
    <Button key="-1" text="/" @click="onPathChange('')"/>
    <Button v-for="(value, key) in paths" :key="key" :text="value.split('/').pop()" @click="onPathChange(value)"/>
  </div>
</template>

<script setup>
import {useRoute, useRouter} from "vue-router";
import Button from "@/components/Button/Button.vue";
import {computed} from "vue";

const route = useRoute();
const router = useRouter()
const onPathChange = (path) => {
  router.push("/home/file/" + (path ? path : ""))
}
const paths = computed(() => {
  if (route.path === "/home/file/" || route.path === "/home/file") return []
  return route.path.replace("/home/file/", "")
      .split("/").reduce((path, currentValue, index) => {
        if (index === 0) {
          return [currentValue];
        } else {
          path.push(path[index - 1] + "/" + currentValue);
          return path;
        }
      }, [])
})
</script>

<style scoped lang="scss">

</style>