<script setup>
import {reactive} from "vue";
import {get} from "@/net";
import {ElMessage} from "element-plus";

const topics = reactive({
    posts: [],
    total: 0,
    curPage: 1,
    size: 10,
    pages: 0,
})


const fetchTopics = () => get('/api/admin/all-topic', data => {
    Object.assign(topics, data)
})

const deleteTopic = (tid) => {
    get(`/api/admin/delete-topic?tid=${tid}`, () => {
        ElMessage.success('删除帖子成功！')
        fetchTopics()
    })
}

fetchTopics()
</script>

<template>

</template>

<style scoped>

</style>