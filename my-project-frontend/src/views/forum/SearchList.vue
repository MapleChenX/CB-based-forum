<script setup>
import {useRoute} from "vue-router";
import {computed, reactive, watch} from "vue";
import {get} from "@/net";
import router from "@/router";
import {CircleCheck, Clock, Close, Star, Top} from "@element-plus/icons-vue";
import TopicTag from "@/components/TopicTag.vue";
import LightCard from "@/components/LightCard.vue";
import {useStore} from "@/store";

const route = useRoute()
const store = useStore()

const text = computed(() => route.params.text);

const topics = reactive({
    list: [],
    page: 1,
    size: 30,
    end: false
})

const search = () => {
    if(topics.end) return
    get(`/api/forum/search/${text.value}?page=${topics.page}&size=${topics.size}`, data => {
        if(data) {
            data.forEach(d => topics.list.push(d))
            topics.page++
        }
        if(!data || data.length < topics.size)
            topics.end = true
    })
}

const resetList = () => {
    topics.page = 1
    topics.end = false
    topics.list = []
    search()
}

watch(
    () => text.value,
    (newVal, oldVal) => {
        if (newVal !== oldVal) {
            resetList()
        }
    }
)
</script>

<template>
    <div style="width: 900px;margin: 0 auto" v-infinite-scroll="search">
        <div v-if="topics.list.length">
            <div style="margin-top: 10px;display: flex;flex-direction: column;gap: 10px">
                <light-card v-for="item in topics.list" class="topic-card">
                    <div style="display: flex">
                        <div>
                            <el-avatar :size="30" :src="store.avatarUserUrl(item.avatar)"/>
                        </div>
                        <div style="margin-left: 7px;transform: translateY(-2px)">
                            <div style="font-size: 13px;font-weight: bold">{{item.username}}</div>
                            <div style="font-size: 12px;color: grey">
                                <el-icon><Clock/></el-icon>
                                <div style="margin-left: 2px;display: inline-block;transform: translateY(-2px)">
                                    {{new Date(item.time).toLocaleString()}}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div style="margin-top: 5px">
<!--                        <topic-tag :type="item.type"/>-->
                        <span
                            style="font-weight: bold;"
                            @click="router.push({ path: '/index/topic-detail/' + item.id, query: { from: 'search' } })"
                        >
                          {{ item.title }}
                        </span>
                    </div>
                    <div class="topic-content">{{item.text}}</div>
                    <div style="display: grid;grid-template-columns: repeat(3, 1fr);grid-gap: 10px">
                        <el-image class="topic-image" v-for="img in item.images" :src="img" fit="cover"></el-image>
                    </div>
                    <div style="display: flex;gap: 20px;font-size: 13px;margin-top: 10px;opacity: 0.8">
                        <div>
                            <el-icon style="vertical-align: middle"><CircleCheck/></el-icon> {{item.like}}点赞
                        </div>
                        <div>
                            <el-icon style="vertical-align: middle"><Star/></el-icon> {{item.collect}}收藏
                        </div>
                    </div>
                </light-card>
            </div>
        </div>
    </div>
</template>

<style scoped>
.topic-card {
    padding: 15px;
    transition: scale .3s;

    &:hover {
        scale: 1.015;
        cursor: pointer;
    }

    .topic-content {
        font-size: 13px;
        color: grey;
        margin: 5px 0;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 3;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .topic-image {
        width: 100%;
        height: 100%;
        max-height: 110px;
        border-radius: 5px;
    }
}
</style>