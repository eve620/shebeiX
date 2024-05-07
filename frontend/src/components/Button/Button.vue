<template>
  <div class="button button-text">
    <div>
      <Icon v-if="props.icon" :size=16 :icon="props.icon"/>
      <span>{{ props.text }}</span>
    </div>
  </div>
</template>

<script setup>
import Icon from "@/components/Icon/Icon.vue";

const props = defineProps(["text", "icon"])
</script>

<style scoped lang="scss">
@import "style";
</style>