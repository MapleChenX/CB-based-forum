<script setup>
import {computed, onMounted, reactive, ref} from "vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import {Calendar, Edit, Grid, User} from "@element-plus/icons-vue";
import {useStore} from "@/store";
import axios from "axios";

const store = useStore()

const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)

const users = reactive({
    users: [],
})

const searchForm = reactive({
    id: null,
    username: "",
    email: "",
    timeStart: "",
    timeEnd: ""
});

const fetchUsers = () => get(`/api/admin/all-user?page=${page.value}&size=${size.value}`, data => {
    Object.assign(users, data)
})

const deleteUser = (uid) => {
    get(`/api/admin/delete-user?uid=${uid}`, () => {
        ElMessage.success('删除用户成功！')
        searchUsers()
    })
}

const rankUp = (uid) => {
    get(`/api/admin/rank-up?uid=${uid}`, () => {
        ElMessage.success('提权成功！')
        searchUsers()
    })
}

onMounted(() => {
    searchUsers()
})


const formatDate = (date) => {
    const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' };
    return new Date(date).toLocaleString('zh-CN', options); // 根据需要选择语言和格式
}

const avatar = (avatarUrl) => {
    if(avatarUrl)
        return `${axios.defaults.baseURL}/images${avatarUrl}`
    else
        return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
}

function formatRole(role) {
    if (role === 'admin') {
        return '管理员'; // 如果是管理员，返回"管理员"
    }
    return '用户'; // 如果不是管理员，返回"用户"
}

const handlePageChange = (newPage) => {
    page.value = newPage;
    searchUsers(); // 重新获取用户数据
};

const timeRange = ref([]);

const searchUsers = () => {
    if (timeRange.value.length === 2) {
        searchForm.timeStart = timeRange.value[0];
        searchForm.timeEnd = timeRange.value[1];
    } else {
        searchForm.timeStart = "";
        searchForm.timeEnd = "";
    }

    post(`/api/admin/all-user?page=${page.value}&size=${size.value}`, searchForm, data => {
        Object.assign(users, data)
    })
};

const resetForm = () => {
    Object.assign(searchForm, {
        id: null,
        username: "",
        email: "",
        timeStart: "",
        timeEnd: ""
    });
    page.value = 1;
    size.value = 10;
    timeRange.value = [];
    searchUsers();
};
</script>

<template>
    <el-card style="margin: 20px">
        <div slot="header" class="clearfix">
            <div style="font-weight: bold; font-size: 16px;">
                <el-icon style="color: #409EFF;margin-right: 2px;translate: 0 2px">
                    <Grid />
                </el-icon>
                用户信息展示
            </div>
            <div style="font-size: 14px; color: grey;">在这里可以看到所有用户信息，包括用户名、邮箱、头像、角色、注册时间、性别、电话、QQ、微信、想说的话等。</div>
        </div>
        <el-divider style="margin: 10px 0"/>
        <el-form :model="searchForm" label-width="80px" size="small">
            <el-row :gutter="20" style="height: 24px">
                <el-col :span="5">
                    <el-form-item label="用户ID">
                        <el-input v-model="searchForm.id" placeholder="请输入用户ID" clearable />
                    </el-form-item>
                </el-col>
                <el-col :span="5">
                    <el-form-item label="用户名">
                        <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
                    </el-form-item>
                </el-col>
                <el-col :span="5">
                    <el-form-item label="邮箱">
                        <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable />
                    </el-form-item>
                </el-col>
                <el-col :span="6">
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
                <el-col :span="3" class="button-group">
                    <el-button type="primary" @click="searchUsers">查询</el-button>
                    <el-button @click="resetForm">重置</el-button>
                    <el-button type="success" @click="">新增</el-button>
                </el-col>
            </el-row>
        </el-form>
        <el-divider style="margin: 10px 0"/>
        <el-table :data="users.users" style="width: 100%" v-loading="loading" size="small">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="100" />
            <el-table-column prop="email" label="邮箱" width="180px"/>
            <el-table-column label="头像" width="100">
                <template #default="{ row }">
                    <el-avatar :src="avatar(row.avatar)" size="middle"></el-avatar>
                </template>
            </el-table-column>
            <el-table-column prop="role" label="角色" width="80">
                <template #default="{ row }">
                    {{ formatRole(row.role) }}
                </template>
            </el-table-column>
            <el-table-column label="注册时间" width="180px">
                <template #default="{ row }">
                    {{ formatDate(row.registerTime) }}
                </template>
            </el-table-column>
            <el-table-column label="性别" width="60">
                <template #default="{ row }">
                    {{ row.gender === 1 ? '男' : '女' }}
                </template>
            </el-table-column>
            <el-table-column prop="phone" label="电话" />
            <el-table-column prop="qq" label="QQ" />
            <el-table-column prop="wx" label="微信" />
            <el-table-column prop="desc" label="想说的话" width="400"/>
            <el-table-column label="操作" width="200">
                <template #default="{ row }">
                    <el-button size="small" class="button-group" @click="showEditDialog(row)">编辑</el-button>
                    <el-button size="small" class="button-group" type="danger" @click="deleteUser(row.id)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <div style="display: flex; justify-content: center; margin-top: 20px;">
            <el-pagination
                :current-page="users.curPage"
                :page-size="users.size"
                :total="users.total"
                @current-change="handlePageChange"
                layout="prev, pager, next"
            >
            </el-pagination>
        </div>
    </el-card>
</template>

<style scoped>
.button-group {
    text-align: center;
}
</style>