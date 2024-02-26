<script setup>

import Card from "@/components/Card.vue";
import {reactive, ref} from "vue";
import {get} from "@/net";
import {ElMessage} from "element-plus";
import {Calendar, Edit, User} from "@element-plus/icons-vue";

const topics = reactive({
  list: []
})

const users = reactive({
  list: []
})

const uid = ref(null)
const tid = ref(null)


const fetchUsers = () => get('/api/admin/all-user', data => users.list = data)
const fetchTopics = () => get('/api/admin/all-topic', data => topics.list = data)
fetchUsers()
fetchTopics()
function deleteUser(){
  get(`/api/admin/delete-user?uid=${uid.value}`, () => {
    ElMessage.success('删除用户成功！')
    fetchUsers()
  })
}

function deleteTopic(){
  get(`/api/admin/delete-topic?tid=${tid.value}`, () => {
    ElMessage.success('删除帖子成功！')
    fetchTopics()
  })
}
function deleteTopicConviniently(tid){
  get(`/api/admin/delete-topic?tid=${tid}`, () => {
    ElMessage.success('删除帖子成功！')
    fetchTopics()
  })
}
function rankUp(uid){
  get(`/api/admin/rank-up?uid=${uid}`, () => {
    ElMessage.success('提权成功！')
    fetchUsers()
  })
}
</script>

<template>
  <div style="margin: auto;max-width: 700px;padding-top: 10px;overflow: hidden" >
    <el-input v-model="uid" class="w-50 m-2" placeholder="请输入要删除的用户id" style="display: inline-block;width: 200px;padding-right: 0">
      <template #prefix>
        <el-icon class="el-input__icon"><User /></el-icon>
      </template>
    </el-input>
    <el-button type="danger" @click="deleteUser">删除此用户</el-button>
    <div style="display: inline-block;float: right">
      <el-input v-model="tid" class="w-50 m-2" placeholder="请输入要删除的帖子id" style="display: inline-block;width: 200px;padding-right: 0">
        <template #prefix>
          <el-icon class="el-input__icon"><Edit /></el-icon>
<!--          <el-icon><Edit /></el-icon>-->
        </template>
      </el-input>
      <el-button type="warning" @click="deleteTopic">删除此贴</el-button>
    </div>

  </div>
  <div v-for="i in users.list">

  </div>
  <div class="user-title" style="margin: 10px auto;max-width: 700px;display: flex;justify-content: left;align-items: center;color: #f54343"  v-if="users.list && users.list.length !== 0">
    <div>用户ID</div>
    <div>用户名</div>
    <div>用户邮箱</div>
  </div>
  <Card style="margin: 10px auto;max-width: 700px;display: flex;justify-content: space-between;align-items: center;color: #f54343" v-for="i in users.list">
    <span class="user user-id">{{i.id}}</span>
    <span class="user user-name">{{i.username}}</span>
<!--    <span>id:{{i.password}}</span>-->
<!--    <span>role:{{i.role}}</span>-->
    <span class="user user-email">{{i.email}}</span>
<!--    <span>id:{{i.registerTime}}</span>-->
<!--    <span>id:{{i.avatar}}</span>-->
    <el-button type="primary" @click="rankUp(i.id)">提权</el-button>
  </Card>
  <div class="user-title" style="margin: 10px auto;max-width: 700px;display: flex;justify-content: left;align-items: center;color: #f19e09">
    <div>帖子ID</div>
    <div>帖子类型</div>
    <div>帖子标题</div>
  </div>
  <Card class="topic" style="overflow:hidden;margin: 10px auto;max-width: 700px;display: flex;color: #f19e09" v-for="i in topics.list">
    <span class="topic-id">{{i.id}}</span>
    <span class="topic-name">{{i.type}}</span>
    <!--    <span>id:{{i.password}}</span>-->
    <!--    <span>role:{{i.role}}</span>-->
    <span class="topic-email">{{i.title}}</span>
    <!--    <span>id:{{i.registerTime}}</span>-->
    <!--    <span>id:{{i.avatar}}</span>-->
<!--    <el-button type="primary"></el-button>-->
    <el-button style="position: relative;top:6px;left: 220px" type="warning" @click="deleteTopicConviniently(i.id)">删除</el-button>
  </Card>
</template>

<style  scoped>
.user-title div{
  display: inline-block;
  width: 130px;
  padding: 10px;
}

.user{
  display: inline-block;
  width: 100px;
  padding: 10px;
}
.user-id{
  display: inline-block;
  width: 80px;
  padding: 10px;
}

.user-name{
  display: inline-block;
  width: 90px;
  padding: 10px;
}
.user-email{
  display: inline-block;
  width: 250px;
  padding: 10px;
}
.user span{
  margin: 0;
  padding: 0;
}
.topic .topic-id{
  display: inline-block;
  width: 80px;
  padding: 10px;
  margin-right: 10px;
}
.topic .topic-name{
  display: inline-block;
  width: 80px;
  padding: 10px;
  margin-left: 50px;
  margin-right: 20px;
}
.topic .topic-email{
  display: inline-block;
  width: 80px;
  padding: 10px;
  margin-left: 20px;
}
</style>