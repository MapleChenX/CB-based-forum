<script setup>
import {computed, onMounted, reactive, ref} from "vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import {Calendar, Edit, Grid, User} from "@element-plus/icons-vue";
import {useStore} from "@/store";
import axios from "axios";
import TopicTag from "@/components/TopicTag.vue";
import {QuillDeltaToHtmlConverter} from "quill-delta-to-html";

const store = useStore()

const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)

const posts = reactive({
    posts: [],
})

const searchForm = reactive({
    id: null,
    type: null,
    title: "",
    timeStart: "",
    timeEnd: "",
    uid: null,
});

const formInit = {
    id: null,
    type: "",
    title: "",
    timeStart: "",
    timeEnd: "",
    uid: null,
}

const deletePost = (tid) => {
    get(`/api/admin/delete-topic?tid=${tid}`, () => {
        ElMessage.success('删除帖子成功！')
        searchPosts()
    })
}

onMounted(() => {
    searchPosts()
})

const formatDate = (date) => {
    const options = {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    return new Date(date).toLocaleString('zh-CN', options); // 根据需要选择语言和格式
}

const avatar = (avatarUrl) => {
    if (avatarUrl)
        return `${axios.defaults.baseURL}/images${avatarUrl}`
    else
        return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
}

const handlePageChange = (newPage) => {
    page.value = newPage;
    searchPosts();
};

const timeRange = ref([]);

const searchPosts = () => {
    if (timeRange.value.length === 2) {
        searchForm.timeStart = timeRange.value[0];
        searchForm.timeEnd = timeRange.value[1];
    } else {
        searchForm.timeStart = "";
        searchForm.timeEnd = "";
    }

    post(`/api/admin/all-topic?page=${page.value}&size=${size.value}`, searchForm, data => {
        Object.assign(posts, data)
        console.log(data)
        total.value = data.total
        pages.value = data.pages
    })
};

const resetForm = () => {
    Object.assign(searchForm, formInit);
    page.value = 1;
    size.value = 10;
    timeRange.value = [];
    searchPosts();
};

const loading = computed(() => posts.posts.length === 0);

const drawerVisible = ref(false);  // 控制抽屉的显示状态

const topic = reactive({
    id: "",
    title: "",
    content: "",
})

const showDialog = (id) => {
    getSingleTopic(id)
    drawerVisible.value = true;
};

const getSingleTopic = (id) => {
    get(`/api/admin/topic?tid=${id}`, data => {
        Object.assign(topic, data)
    })
}

function convertToHtml(content) {
    const ops = JSON.parse(content).ops
    const converter = new QuillDeltaToHtmlConverter(ops, { inlineStyles: true });
    return converter.convert();
}
</script>

<template>
    <el-card style="margin: 20px">
        <div slot="header" class="clearfix">
            <div style="font-weight: bold; font-size: 16px;">
                <el-icon style="color: #409EFF;margin-right: 2px;translate: 0 2px">
                    <Grid/>
                </el-icon>
                帖子信息展示
            </div>
            <div style="font-size: 14px; color: grey;">
                在这里可以看到所有帖子信息，包括标题，发帖时间，发帖人信息等。
            </div>
        </div>
        <el-divider style="margin: 10px 0"/>
        <el-form :model="searchForm" label-width="80px" size="small">
            <el-row :gutter="20" style="height: 24px">
                <el-col :span="4">
                    <el-form-item label="帖子ID">
                        <el-input v-model="searchForm.id" placeholder="请输入帖子ID" clearable/>
                    </el-form-item>
                </el-col>
                <el-col :span="4">
                    <el-form-item label="帖子类型">
                        <el-select v-model="searchForm.type" placeholder="请选择帖子类型" clearable>
                            <el-option
                                v-for="item in store.types"
                                :key="item.id"
                                :label="item.name"
                                :value="item.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="5">
                    <el-form-item label="帖子标题">
                        <el-input v-model="searchForm.title" placeholder="请输入帖子标题" clearable/>
                    </el-form-item>
                </el-col>
                <el-col :span="4">
                    <el-form-item label="创建时间">
                        <el-date-picker
                            v-model="timeRange"
                            type="daterange"
                            range-separator="至"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期"
                            value-format="YYYY-MM-DD"
                            clearable
                        />
                    </el-form-item>
                </el-col>
                <el-col :span="4">
                    <el-form-item label="贴主ID">
                        <el-input v-model="searchForm.uid" placeholder="请输入帖主ID" clearable/>
                    </el-form-item>
                </el-col>
                <el-col :span="3" class="button-group">
                    <el-button type="primary" @click="searchPosts">查询</el-button>
                    <el-button @click="resetForm">重置</el-button>
                </el-col>
            </el-row>
        </el-form>
        <el-divider style="margin: 10px 0"/>
        <el-table :data="posts.posts" style="width: 100%" v-loading="loading" size="small">
            <el-table-column prop="id" label="ID" width="60"/>
            <el-table-column prop="username" label="贴主" width="80" />
            <el-table-column label="头像" width="100">
                <template #default="{ row }">
                    <el-avatar :src="avatar(row.avatar)" size="middle"></el-avatar>
                </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" width="180px"/>
            <el-table-column prop="type" label="类型" width="80">
                <template #default="{ row }">
                    {{store.getTypeById(row.type)?.name}}
                </template>
            </el-table-column>
            <el-table-column prop="text" label="内容" >
                <template #default="{ row }">
                    {{ row.text }}
                </template>
            </el-table-column>
            <el-table-column label="创建时间" width="150px">
                <template #default="{ row }">
                    {{ formatDate(row.time) }}
                </template>
            </el-table-column>

            <el-table-column prop="like" label="点赞数" width="60px" />
            <el-table-column prop="collect" label="收藏数" width="60px" />
            <el-table-column label="操作" width="200">
                <template #default="{ row }">
                    <el-button size="small" @click="showDialog(row.id)">查看</el-button>
                    <el-button size="small" type="danger" @click="deletePost(row.id)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <div style="display: flex; justify-content: center; margin-top: 20px;">
            <el-pagination
                :current-page="page.value"
                :page-size="size.value"
                :total="posts.total"
                @current-change="handlePageChange"
                layout="prev, pager, next"
            >
            </el-pagination>
        </div>
    </el-card>

    <el-drawer
        v-model="drawerVisible"
        :title="topic ? topic.title : '帖子详情'"
        direction="ltr"
        size="40%"
    >
        <div class="topic-content" v-html="convertToHtml(topic.content)" />
    </el-drawer>

</template>

<style scoped>
.topic-content {
    height: calc(100% - 50px);
    width: 100%;
    font-size: 13px;
    color: grey;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    overflow: auto;
}
</style>