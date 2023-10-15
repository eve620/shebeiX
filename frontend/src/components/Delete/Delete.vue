<template>
    <div style="display: inline-block">
        <a-modal v-model:open="isDeleteShow" ok-text="确定" cancel-text="取消" @ok="onDeleteOk" @cancel="onDeleteCancel" title="删除" width="300px">
            <p>确认删除？</p>
        </a-modal>
        <a @click="onDelete()" class="deleted-button">删除</a>
    </div>
</template>

<script setup>
import {ref} from "vue";

const props = defineProps(['tagetId'])
const emit = defineEmits(['delete'])
const isDeleteShow = ref(false);

const onDelete = () => {
    isDeleteShow.value = true;
};
const onDeleteOk = () => {
    emit('delete', props.tagetId)
    isDeleteShow.value = false;
};
const onDeleteCancel = () => {
    isDeleteShow.value = false;
};
</script>

<style scoped lang="scss">
@import "../../scss/content";
</style>