<template>

</template>

<script setup>
import router from "@/router.js";
import {onBeforeMount} from "vue";
onBeforeMount(()=>{
    router.push("/login")
})
</script>

<style scoped>

</style>