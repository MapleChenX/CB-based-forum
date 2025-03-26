<script setup>
import Card from "@/components/Card.vue";
import {onMounted, reactive, ref} from "vue";
import {get} from "@/net";
import {ElMessage} from "element-plus";
import {Calendar, Edit, Grid, User} from "@element-plus/icons-vue";
import {useStore} from "@/store";
import axios from "axios";

const store = useStore()

const users = reactive({
    users: [],
    total: 0,
    curPage: 1,
    size: 10,
    pages: 0,
})

const searchRequest = reactive({
    name: '',
    email: '',
    role: '',
})

const fetchUsers = () => get('/api/admin/all-user', data => {
    Object.assign(users, data)
})

const deleteUser = (uid) => {
    get(`/api/admin/delete-user?uid=${uid}`, () => {
        ElMessage.success('删除用户成功！')
        fetchUsers()
    })
}

const rankUp = (uid) => {
    get(`/api/admin/rank-up?uid=${uid}`, () => {
        ElMessage.success('提权成功！')
        fetchUsers()
    })
}

fetchUsers()

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
</script>

<template>
    <el-card style="margin: 0 20px">

    </el-card>

    <el-card style="margin: 20px">
        <div slot="header" class="clearfix">
            <div style="font-weight: bold; font-size: 18px;">
                <el-icon style="color: #409EFF;margin-right: 2px;translate: 0 2px">
                    <Grid />
                </el-icon>
                用户信息展示
            </div>
            <div style="font-size: 14px; color: grey;">在这里可以看到所有用户信息，包括用户名、邮箱、头像、角色、注册时间、性别、电话、QQ、微信、想说的话等。</div>
        </div>
        <el-divider style="margin: 10px 0"/>
        <el-table :data="users.users" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="100" />
            <el-table-column prop="email" label="邮箱" width="180px"/>
            <el-table-column label="头像" width="100">
                <template #default="{ row }">
                    <el-avatar :src="avatar(row.avatar)" size="large"></el-avatar>
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
                    <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
                    <el-button size="small" type="danger" @click="deleteUser(row.id)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination
            :current-page="users.curPage"
            :page-size="users.size"
            :total="users.total"
            @current-change="handlePageChange"
            layout="prev, pager, next"
        >
        </el-pagination>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑用户' : '添加用户'">
        <el-form :model="isEditing ? editUser : newUser">
            <el-form-item label="姓名"><el-input v-model="(isEditing ? editUser : newUser).name" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="(isEditing ? editUser : newUser).email" /></el-form-item>
            <el-form-item label="角色"><el-input v-model="(isEditing ? editUser : newUser).role" /></el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveUser">保存</el-button>
        </template>
    </el-dialog>
</template>

<style  scoped>
</style>