<script setup>
import LightCard from "@/components/LightCard.vue";
import {
  Calendar,
  Clock,
  CollectionTag,
  Compass,
  Document,
  Edit,
  EditPen,
  Link,
  Picture,
  Microphone, CircleCheck, Star, FolderOpened, ArrowRightBold, Close, Top, Bottom
} from "@element-plus/icons-vue";
import Weather from "@/components/Weather.vue";
import {onMounted, onUnmounted, computed, reactive, ref, watch} from "vue";
import {get} from "@/net";
import {ElMessage} from "element-plus";
import TopicEditor from "@/components/TopicEditor.vue";
import {useStore} from "@/store";
import ColorDot from "@/components/ColorDot.vue";
import router from "@/router";
import TopicTag from "@/components/TopicTag.vue";
import TopicCollectList from "@/components/TopicCollectList.vue";
import emitter from "@/utils/mitt";

const store = useStore()

const weather = reactive({
    location: {},
    now: {},
    hourly: [],
    success: false
})
const editor = ref(false)
const topics = reactive({
    list: [],
    type: 0,
    page: 0,
    end: false,
    top: []
})
const collects = ref(false)

watch(() => topics.type, () => resetList(), {immediate: true})

const today = computed(() => {
    const date = new Date()
    return `${date.getFullYear()} 年 ${date.getMonth() + 1} 月 ${date.getDate()} 日`
})
get('/api/forum/top-topic', data => topics.top = data)
function updateList(){
    if(topics.end) return
    get(`/api/forum/list-topic?page=${topics.page}&type=${topics.type}`, data => {
        if(data) {
            data.forEach(d => topics.list.push(d))
            topics.page++
        }
        if(!data || data.length < 10)
            topics.end = true
    })
}

function onTopicCreate() {
    editor.value = false
    resetList()
}

function resetList() {
    topics.page = 0
    topics.end = false
    topics.list = []
    updateList()
}

navigator.geolocation.getCurrentPosition(position => {
    const longitude = position.coords.longitude
    const latitude = position.coords.latitude
    get(`/api/forum/weather?longitude=${longitude}&latitude=${latitude}`, data => {
        Object.assign(weather, data)
        weather.success = true
    })
}, error => {
    console.info(error)
    // ElMessage.warning('位置信息获取超时，请检测网络设置')
    get(`/api/forum/weather?longitude=116.40529&latitude=39.90499`, data => {
        Object.assign(weather, data)
        weather.success = true
    })
}, {
    timeout: 3000,
    enableHighAccuracy: true
})

function deleteTopicById(tid){
  get(`/api/admin/delete-topic?tid=${tid}`, () => {
    ElMessage.success('删除帖子成功！')
    get(`/api/forum/list-topic?page=${topics.page}&type=${topics.type}`, data => {
      if(data) {
        data.forEach(d => topics.list.push(d))
        topics.page++
      }
      if(!data || data.length < 10)
        topics.end = true
    })
    router.push('/index')
    router.go(0)
  })
}

function setTop(tid){
  get(`/api/admin/set-top?tid=${tid}`, () => {
    ElMessage.success('置顶成功！')
    get(`/api/forum/list-topic?page=${topics.page}&type=${topics.type}`, data => {
      if(data) {
        data.forEach(d => topics.list.push(d))
        topics.page++
      }
      if(!data || data.length < 10)
        topics.end = true
    })
    // 跳转到index页面
    router.push('/index')
    router.go(0)
  })
}

function downTop(tid){
  get(`/api/admin/down-top?tid=${tid}`, () => {
    ElMessage.success('取消置顶成功！');
    get(`/api/forum/list-topic?page=${topics.page}&type=${topics.type}`, data => {
      if(data) {
        data.forEach(d => topics.list.push(d))
        topics.page++
      }
      if(!data || data.length < 10)
        topics.end = true
    })
    router.push('/index')
    router.go(0)
  })
}

const handleCategoryChange = (id) => {
    topics.type = id
}

onMounted(() => {
    emitter.on('change-category', handleCategoryChange)
})

onUnmounted(() => {
    emitter.off('change-category', handleCategoryChange)
})
</script>

<template>
    <div style="display: flex;margin: 20px auto;gap: 20px;max-width: 1100px">
        <div style="flex: 1">
            <light-card>
                <div class="create-topic" @click="editor = true">
                    <el-icon><EditPen/></el-icon> 点击发表主题...
                </div>
                <div style="margin-top: 10px;display: flex;gap: 13px;font-size: 18px;color: grey">
                    <el-icon><Edit /></el-icon>
                    <el-icon><Document /></el-icon>
                    <el-icon><Compass /></el-icon>
                    <el-icon><Picture /></el-icon>
                    <el-icon><Microphone /></el-icon>
                </div>
            </light-card>
            <light-card style="margin-top: 10px;display: flex;flex-direction: column;gap: 10px">
                <div v-for="item in topics.top" class="top-topic" >
                    <el-tag type="info" size="small">置顶</el-tag>
                    <div @click="router.push(`/index/topic-detail/${item.id}`)">{{item.title}}</div>
                    <div>{{new Date(item.time).toLocaleDateString()}}</div>
                    <div style="margin-left: 5px; position: relative;top:1px; color: lightgray;font-size: 12px" @click="downTop(item.id)" v-if="store.user.role === 'admin'">
                    <el-icon style="vertical-align: middle"><Bottom /></el-icon>
                    取消置顶
                    </div>
                </div>
            </light-card>
            <light-card style="margin-top: 10px;display: flex;gap: 7px">
                <div :class="`type-select-card ${topics.type === item.id ? 'active' : ''}`"
                     v-for="item in store.forum.types"
                     @click="topics.type = item.id">
                    <color-dot :color="item.color"/>
                    <span style="margin-left: 5px">{{item.name}}</span>
                </div>
            </light-card>
            <transition name="el-fade-in" mode="out-in">
                <div v-if="topics.list.length">
                    <div style="margin-top: 10px;display: flex;flex-direction: column;gap: 10px"
                         v-infinite-scroll="updateList">
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
                                <topic-tag :type="item.type"/>
                                <span style="font-weight: bold;margin-left: 7px"
                                      @click="router.push({ path: '/index/topic-detail/' + item.id, query: { from: 'index' } })"
                                >
                                    {{item.title}}
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
                                <div style="color: lightgray" @click="deleteTopicById(item.id)" v-if="store.user.role === 'admin'">
                                  <el-icon style="vertical-align: middle"><Close /></el-icon>
                                  删除
                                </div>
                              <div style="color: lightgray" @click="setTop(item.id)" v-if="store.user.role === 'admin'">
                                <el-icon style="vertical-align: middle"><Top /></el-icon>
                                置顶
                              </div>
                            </div>
                        </light-card>
                    </div>
                </div>
            </transition>
        </div>
        <div style="width: 280px">
            <div style="position: sticky;top: 20px">
                <light-card>
                    <div class="collect-list-button" @click="collects = true">
                        <span><el-icon><FolderOpened /></el-icon> 查看我的收藏</span>
                        <el-icon style="transform: translateY(3px)"><ArrowRightBold/></el-icon>
                    </div>
                </light-card>
                <light-card style="margin-top: 10px">
                    <div style="font-weight: bold">
                        <el-icon><CollectionTag/></el-icon>
                        论坛公告
                    </div>
                    <el-divider style="margin: 10px 0"/>
                    <div style="font-size: 14px;margin: 10px;color: grey">
                        网站目前仍处于更新维护期，欢迎大家提出宝贵意见！
                    </div>
                </light-card>
                <light-card style="margin-top: 10px">
                    <div style="font-weight: bold">
                        <el-icon><Calendar/></el-icon>
                        天气信息
                    </div>
                    <el-divider style="margin: 10px 0"/>
                    <weather :data="weather"/>
                </light-card>
                <light-card style="margin-top: 10px">
                    <div class="info-text">
                        <div>当前日期</div>
                        <div>{{today}}</div>
                    </div>
                    <div class="info-text">
                        <div>当期IP地址</div>
                        <div>127.0.0.1</div>
                    </div>
                </light-card>
                <div style="font-size: 14px;margin-top: 10px;color: grey">
                    <el-icon><Link/></el-icon>
                    项目部分技术
                    <el-divider style="margin: 10px 0"/>
                </div>
                <div style="display: grid;grid-template-columns: repeat(2, 1fr);grid-gap: 10px;margin-top: 10px">
                    <div class="friend-link">
                        <el-image style="height: 100%" src="src/assets/images/springboot.png"/>
                    </div>
                    <div class="friend-link">
                        <el-image style="height: 100%" src="src/assets/images/springsecurity.png"/>
                    </div>
                  <div class="friend-link">
                    <el-image style="height: 100%" src="src/assets/images/redis.png"/>
                  </div>
                  <div class="friend-link">
                    <el-image style="height: 100%" src="src/assets/images/rabbitmq.png"/>
                  </div>
                  <div class="friend-link">
                    <el-image style="height: 100%" src="src/assets/images/vue.png"/>
                  </div>
                  <div class="friend-link">
                    <el-image style="height: 100%" src="src/assets/images/element.png"/>
                  </div>
                </div>
            </div>
        </div>
        <topic-editor :show="editor" @success="onTopicCreate" @close="editor = false"/>
        <topic-collect-list :show="collects" @close="collects = false"/>
    </div>
</template>

<style lang="less" scoped>
.collect-list-button {
    font-size: 14px;
    display: flex;
    justify-content: space-between;
    transition: .3s;

    &:hover {
        cursor: pointer;
        opacity: 0.6;
    }
}

.top-topic {
    display: flex;

    div:first-of-type {
        font-size: 14px;
        margin-left: 10px;
        font-weight: bold;
        opacity: 0.8;
        transition: color .3s;

        &:hover {
            color: grey;
        }
    }

    div:nth-of-type(2) {
        flex: 1;
        color: grey;
        font-size: 13px;
        text-align: right;
    }

    &:hover {
        cursor: pointer;
    }
}

.type-select-card {
    background-color: #f5f5f5;
    padding: 2px 7px;
    font-size: 14px;
    border-radius: 3px;
    box-sizing: border-box;
    transition: background-color .3s;

    &.active {
        border: solid 1px #ead4c4;
    }

    &:hover {
        cursor: pointer;
        background-color: #dadada;
    }
}

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

.info-text {
    display: flex;
    justify-content: space-between;
    color: grey;
    font-size: 14px;
}

.friend-link {
    border-radius: 5px;
    overflow: hidden;
}

.create-topic {
    background-color: #efefef;
    border-radius: 5px;
    height: 40px;
    color: grey;
    font-size: 14px;
    line-height: 40px;
    padding: 0 10px;

    &:hover {
        cursor: pointer;
    }
}

.dark {
    .create-topic {
        background-color: #232323;
    }

    .type-select-card {
        background-color: #282828;

        &.active {
            border: solid 1px #64594b;
        }

        &:hover {
            background-color: #5e5e5e;
        }
    }
}
</style>
