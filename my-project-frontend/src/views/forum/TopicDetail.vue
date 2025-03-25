<script setup>
import {useRoute} from "vue-router";
import {get, post} from "@/net";
import {computed, onMounted, reactive, ref, watch} from "vue";
import {
    ArrowLeft,
    ChatSquare,
    CircleCheck,
    Clock, Close,
    Delete,
    EditPen,
    Female, Loading,
    Male,
    Plus, Search,
    Star, Top
} from "@element-plus/icons-vue";
import { QuillDeltaToHtmlConverter } from 'quill-delta-to-html';
import Card from "@/components/Card.vue";
import router from "@/router";
import TopicTag from "@/components/TopicTag.vue";
import InteractButton from "@/components/InteractButton.vue";
import {ElMessage} from "element-plus";
import {useStore} from "@/store";
import TopicEditor from "@/components/TopicEditor.vue";
import TopicCommentEditor from "@/components/TopicCommentEditor.vue";
import RecommendationCard from "@/components/RecommendationCard.vue";

const route = useRoute()
const store = useStore()

const tid = computed(() => route.params.tid);
const from = computed(() => route.params.from);

const topic = reactive({
    data: null,
    like: false,
    collect: false,
    comments: null,
    page: 1
})
const edit = ref(false)
const comment = reactive({
    show: false,
    text: '',
    quote: null
})

const topics = reactive({
    list: []
})

const jump = (id) => {
    router.push({
        name: 'topic-detail',
        params: {tid: id}
    })
    console.log('jump to:' + id)
}

const getSimilarRecommendation = () => {
    // 清空列表
    topics.list = []
    get(`/api/recommend/similar/${tid.value}`, data => {
        topics.list = data
    })
}

const getSimilarRecommendationV2 = () => {
    // 清空列表
    topics.list = []
    get(`/api/recommend/similar/v2/${tid.value}`, data => {
        topics.list = data
    })
}

const init = () => get(`api/forum/topic?tid=${tid.value}`, data => {
    topic.data = data // 加载帖子内容
    topic.like = data.interact.like
    topic.collect = data.interact.collect
    loadComments(1) // 加载评论
    getSimilarRecommendationV2() // 加载相关推荐
})

onMounted(() => {
    init()
})

watch(() => route.params.tid, (newTid, oldTid) => {
    if (newTid !== oldTid) {
        init(); // 路由参数 tid 变化时重新加载数据
    }
});


function convertToHtml(content) {
    const ops = JSON.parse(content).ops
    const converter = new QuillDeltaToHtmlConverter(ops, { inlineStyles: true });
    return converter.convert();
}

function interact(type, message) {
    get(`/api/forum/interact?tid=${tid.value}&type=${type}&state=${!topic[type]}`, () => {
        topic[type] = !topic[type]
        if(topic[type])
            ElMessage.success(`${message}成功！`)
        else
            ElMessage.success(`已取消${message}！`)
    })
}

function updateTopic(editor) {
    post('/api/forum/update-topic', {
        id: tid.value,
        type: editor.type.id,
        title: editor.title,
        content: editor.text
    }, () => {
        ElMessage.success('帖子内容更新成功！')
        edit.value = false
        init()
    })
}

function loadComments(page) {
    topic.comments = null
    topic.page = page
    get(`/api/forum/comments?tid=${tid.value}&page=${page - 1}`, data => topic.comments = data)
}

function onCommentAdd() {
    comment.show = false
    loadComments(Math.floor(++topic.data.comments / 10) + 1)
}

function deleteComment(id) {
    get(`/api/forum/delete-comment?id=${id}`, () => {
        ElMessage.success('删除评论成功！')
        loadComments(topic.page)
    })
}

const return2origin = () => {
    // router.push(from.value)
    router.push('/index')
}
</script>

<template>
    <div class="frame" v-if="topic.data" style="margin: 0 auto;max-width: 1200px;display: flex">
        <div class="topic-page" v-if="topic.data">
            <div class="topic-main" style="position: sticky;top: 0;z-index: 10">
                <card style="display: flex;width: 100%;">
                    <el-button :icon="ArrowLeft" type="info" size="small"
                               plain round @click="return2origin"
                    >
                        返回主页
                    </el-button>
                    <div style="text-align: center;flex: 1">
                        <topic-tag :type="topic.data.type"/>
                        <span style="font-weight: bold;margin-left: 5px">{{topic.data.title}}</span>
                    </div>
                </card>
            </div>
            <div class="topic-main">
                <div class="topic-main-left">
                    <el-avatar :src="store.avatarUserUrl(topic.data.user.avatar)" :size="60"/>
                    <div>
                        <div style="font-size: 18px;font-weight: bold">
                            {{topic.data.user.username}}
                            <span style="color: hotpink" v-if="topic.data.user.gender === 1">
                                <el-icon><Female/></el-icon>
                            </span>
                            <span style="color: dodgerblue" v-if="topic.data.user.gender === 0">
                                <el-icon><Male/></el-icon>
                            </span>
                        </div>
                        <div class="desc">{{topic.data.user.email}}</div>
                    </div>
                    <el-divider style="margin: 10px 0"/>
                    <div style="text-align: left;margin: 0 5px">
                        <div class="desc">微信号: {{topic.data.user.wx || '已隐藏或未填写'}}</div>
                        <div class="desc">QQ号: {{topic.data.user.qq || '已隐藏或未填写'}}</div>
                        <div class="desc">手机号: {{topic.data.user.phone || '已隐藏或未填写'}}</div>
                    </div>
                    <el-divider style="margin: 10px 0"/>
                    <div class="desc" style="margin: 0 5px">{{topic.data.user.desc}}</div>
                </div>
                <div class="topic-main-right">
                    <div class="topic-content" v-html="convertToHtml(topic.data.content)"></div>
                    <el-divider/>
                    <div style="font-size: 13px;color: grey;text-align: center">
                        <div>发帖时间: {{new Date(topic.data.time).toLocaleString()}}</div>
                    </div>
                    <div style="text-align: right;margin-top: 30px">
                        <interact-button name="编辑帖子" color="dodgerblue" :check="false"
                                         @check="edit = true" style="margin-right: 20px"
                                         v-if="store.user.id === topic.data.user.id">
                            <el-icon><EditPen/></el-icon>
                        </interact-button>
                        <interact-button name="点个赞吧" check-name="已点赞" color="pink" :check="topic.like"
                                         @check="interact('like', '点赞')">
                            <el-icon><CircleCheck/></el-icon>
                        </interact-button>
                        <interact-button name="收藏本帖" check-name="已收藏" color="orange" :check="topic.collect"
                                         @check="interact('collect', '收藏')"
                                         style="margin-left: 20px">
                            <el-icon><Star/></el-icon>
                        </interact-button>
                    </div>
                </div>
            </div>
            <transition name="el-fade-in-linear" mode="out-in">
                <div v-if="topic.comments">
                    <div class="topic-main" style="margin-top: 10px" v-for="item in topic.comments">
                        <div class="topic-main-left">
                            <el-avatar :src="store.avatarUserUrl(item.user.avatar)" :size="60"/>
                            <div>
                                <div style="font-size: 18px;font-weight: bold">
                                    {{item.user.username}}
                                    <span style="color: hotpink" v-if="item.user.gender === 1">
                                <el-icon><Female/></el-icon>
                            </span>
                                    <span style="color: dodgerblue" v-if="item.user.gender === 0">
                                <el-icon><Male/></el-icon>
                            </span>
                                </div>
                                <div class="desc">{{item.user.email}}</div>
                            </div>
                            <el-divider style="margin: 10px 0"/>
                            <div style="text-align: left;margin: 0 5px">
                                <div class="desc">微信号: {{item.user.wx || '已隐藏或未填写'}}</div>
                                <div class="desc">QQ号: {{item.user.qq || '已隐藏或未填写'}}</div>
                                <div class="desc">手机号: {{item.user.phone || '已隐藏或未填写'}}</div>
                            </div>
                        </div>
                        <div class="topic-main-right">
                            <div style="font-size: 13px;color: grey">
                                <div>评论时间: {{new Date(item.time).toLocaleString()}}</div>
                            </div>
                            <div v-if="item.quote" class="comment-quote">
                                回复: {{item.quote}}
                            </div>
                            <div class="topic-content" v-html="convertToHtml(item.content)"></div>
                            <div style="text-align: right">
                                <el-link :icon="ChatSquare" @click="comment.show = true;comment.quote = item"
                                         type="info">&nbsp;回复评论</el-link>
                                <el-link :icon="Delete" type="danger" v-if="item.user.id === store.user.id"
                                         style="margin-left: 20px" @click="deleteComment(item.id)">&nbsp;删除评论</el-link>
                            </div>
                        </div>
                    </div>
                    <div style="width: fit-content;margin: 20px auto">
                        <el-pagination background layout="prev, pager, next"
                                       v-model:current-page="topic.page" @current-change="loadComments"
                                       :total="topic.data.comments" :page-size="10"
                                        hide-on-single-page/>
                    </div>
                </div>
            </transition>
            <topic-editor :show="edit" @close="edit = false" v-if="topic.data && store.forum.types"
                          :default-type="topic.data.type" :default-text="topic.data.content"
                          :default-title="topic.data.title" submit-button="更新帖子内容" :submit="updateTopic"/>
            <topic-comment-editor :show="comment.show" @close="comment.show = false" :tid="tid.value"
                                  :quote="comment.quote" @comment="onCommentAdd"/>
            <div class="add-comment" @click="comment.show = true;comment.quote = null">
                <el-icon><Plus/></el-icon>
            </div>
        </div>
        <div class="recommendation" style="width: 400px;margin-left: 30px">
            <div style="font-size: 20px;font-weight: bold;line-height: 45px;height: 45px;padding: 10px">
                <span >
                 相关推荐<el-icon style="vertical-align: middle;margin-left: 5px"><Search/></el-icon>
                </span>
            </div>
            <div v-show="!topics.list.length" v-loading="!topics.list.length" style="width: 400px;height: 100vh"></div>
            <RecommendationCard v-if="topics.list.length" v-for="item in topics.list" class="topic-card"
            >
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
                    <span style="font-weight: bold;margin-left: 7px" @click=jump(item.id)>{{item.title}}</span>
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
            </RecommendationCard>
        </div>
    </div>
</template>

<style lang="less" scoped>
.comment-quote {
    font-size: 13px;
    color: grey;
    background-color: rgba(94, 94, 94, 0.1);
    padding: 10px;
    margin-top: 10px;
    border-radius: 5px;
}

.add-comment {
    position: fixed;
    bottom: 20px;
    right: 20px;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    font-size: 18px;
    color: var(--el-color-primary);
    text-align: center;
    line-height: 45px;
    background: var(--el-bg-color-overlay);
    box-shadow: var(--el-box-shadow-lighter);

    &:hover {
        background: var(--el-border-color-extra-light);
        cursor: pointer;
    }
}

.topic-page {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 10px 0;
}

.topic-main {
    display: flex;
    border-radius: 7px;
    margin: 0 auto;
    background-color: var(--el-bg-color);
    width: 800px;

    .topic-main-left {
        width: 200px;
        padding: 10px;
        text-align: center;
        border-right: solid 1px var(--el-border-color);

        .desc {
            font-size: 12px;
            color: grey;
        }
    }

    .topic-main-right {
        width: 600px;
        padding: 10px 20px;
        display: flex;
        flex-direction: column;

        .topic-content {
            font-size: 14px;
            line-height: 22px;
            opacity: 0.8;
            flex: 1;
        }
    }
}

.topic-card {
    padding: 10px;
    transition: scale .3s;
    margin-bottom: 20px;

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

.recommend-title {
    display: flex;       /* 使用 Flexbox 布局 */
    align-items: center; /* 垂直居中对齐图标和文字 */
    gap: 5px;            /* 设置图标和文字之间的间距 */
}
</style>
