<script setup>
import {get, logout} from '@/net'
import router from "@/router";
import {useStore} from "@/store";
import {reactive, ref} from "vue";
import {
    Avatar,
    Back,
    Bell,
    ChatDotSquare, Check, Collection, Comment, DataLine,
    Document, Files,
    Location, Lock, Message, Monitor,
    Notification, Operation, Picture,
    Position,
    School, Search,
    Umbrella, User
} from "@element-plus/icons-vue";
import LightCard from "@/components/LightCard.vue";

const store = useStore()
const show = ref(false)
const loading = ref(true)

const searchInput = reactive({
    text: ''
})
const notification = ref([])

get('/api/user/info', (data) => {
    store.user = data
    // 如果store.user.role为“admin”，则show.value为true
    show.value = store.user.role === 'admin'
    loading.value = false
})
const loadNotification =
        () => get('/api/notification/list', data => notification.value = data)
loadNotification()

function userLogout() {
    logout(() => router.push("/"))
}

function confirmNotification(id, url) {
    get(`/api/notification/delete?id=${id}`, () => {
        loadNotification()
        window.open(url)
    })
}

function deleteAllNotification() {
    get(`/api/notification/delete-all`, loadNotification)
}

const toSearch = () => {
    if (searchInput.text) {
        router.push(`/index/search/${searchInput.text}`)
    }
}
</script>

<template>
    <div class="main-content" v-loading="loading" element-loading-text="正在进入，请稍后...">
        <el-container style="height: 100%" v-if="!loading">
            <el-header class="main-content-header">
                <el-image style="border-radius: 50px" class="logo" src="/favicon.ico"/>
              <h4 style="color: #79b7f8;margin-left: 10px"> 听竹雨</h4>
                <div style="flex: 1;padding: 0 20px;text-align: center">
                    <el-input v-model="searchInput.text" style="width: 100%;max-width: 500px" placeholder="搜索相关内容...">
                        <template #append>
                            <el-icon @click="toSearch" style="cursor: pointer">
                                <Search/>
                            </el-icon>
                        </template>
                    </el-input>
                </div>
                <div class="user-info">
                    <el-popover placement="bottom" :width="350" trigger="click">
                        <template #reference>
                            <el-badge style="margin-right: 15px" is-dot :hidden="!notification.length">
                                <div class="notification">
                                    <el-icon><Bell/></el-icon>
                                    <div style="font-size: 10px">消息</div>
                                </div>
                            </el-badge>
                        </template>
                        <el-empty :image-size="80" description="暂时没有未读消息哦~" v-if="!notification.length"/>
                        <el-scrollbar :max-height="500" v-else>
                            <light-card v-for="item in notification" class="notification-item"
                                        @click="confirmNotification(item.id, item.url)">
                                <div>
                                    <el-tag size="small" :type="item.type">消息</el-tag>&nbsp;
                                    <span style="font-weight: bold">{{item.title}}</span>
                                </div>
                                <el-divider style="margin: 7px 0 3px 0"/>
                                <div style="font-size: 13px;color: grey">
                                    {{item.content}}
                                </div>
                            </light-card>
                        </el-scrollbar>
                        <div style="margin-top: 10px">
                            <el-button size="small" type="info" :icon="Check" @click="deleteAllNotification"
                                       style="width: 100%" plain>清除全部未读消息</el-button>
                        </div>
                    </el-popover>
                    <div class="profile">
                        <div>{{ store.user.username }}</div>
                        <div>{{ store.user.email }}</div>
                    </div>
                    <el-dropdown>
                        <el-avatar :src="store.avatarUrl"/>
                        <template #dropdown>
                            <el-dropdown-item @click="this.$router.push('/index/user-setting')">
                                <el-icon>
                                    <Operation/>
                                </el-icon>
                                个人设置
                            </el-dropdown-item>
                            <el-dropdown-item @click="userLogout" divided>
                                <el-icon>
                                    <Back/>
                                </el-icon>
                                退出登录
                            </el-dropdown-item>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>
            <el-container>
                <el-aside width="230px">
                    <el-scrollbar style="height: calc(100vh - 55px)">
                        <el-menu
                                router
                                :default-active="$route.path"
                                :default-openeds="['1', '2']"
                                style="min-height: calc(100vh - 55px)">
                            <el-sub-menu index="1">
                                <template #title>
                                    <el-icon>
                                        <Location/>
                                    </el-icon>
                                    <span><b>校园论坛</b></span>
                                </template>
                                <el-menu-item index="/index">
                                    <template #title>
                                        <el-icon>
                                            <ChatDotSquare/>
                                        </el-icon>
                                        帖子广场
                                    </template>
                                </el-menu-item>
<!--                                <el-menu-item>-->
<!--                                    <template #title>-->
<!--                                        <el-icon>-->
<!--                                            <Bell/>-->
<!--                                        </el-icon>-->
<!--                                        日常闲聊-->
<!--                                    </template>-->
<!--                                </el-menu-item>-->
<!--                                <el-menu-item>-->
<!--                                    <template #title>-->
<!--                                        <el-icon>-->
<!--                                            <Notification/>-->
<!--                                        </el-icon>-->
<!--                                        真诚交友-->
<!--                                    </template>-->
<!--                                </el-menu-item>-->
<!--                                <el-menu-item>-->
<!--                                    <template #title>-->
<!--                                        <el-icon>-->
<!--                                            <Umbrella/>-->
<!--                                        </el-icon>-->
<!--                                        问题反馈-->
<!--                                    </template>-->
<!--                                </el-menu-item>-->
<!--                                <el-menu-item>-->
<!--                                    <template #title>-->
<!--                                        <el-icon>-->
<!--                                            <School/>-->
<!--                                        </el-icon>-->
<!--                                        恋爱官宣-->
<!--                                    </template>-->
<!--                                </el-menu-item>-->
<!--                                <el-menu-item>-->
<!--                                    <template #title>-->
<!--                                        <el-icon>-->
<!--                                            <Picture/>-->
<!--                                        </el-icon>-->
<!--                                        踩坑记录-->
<!--                                    </template>-->
<!--                                </el-menu-item>-->
                            </el-sub-menu>

                            <el-sub-menu index="2">
                                <template #title>
                                    <el-icon>
                                        <Operation/>
                                    </el-icon>
                                    <span><b>个人设置</b></span>
                                </template>
                                <el-menu-item index="/index/user-setting">
                                    <template #title>
                                        <el-icon>
                                            <User/>
                                        </el-icon>
                                        个人信息设置
                                    </template>
                                </el-menu-item>
                                <el-menu-item index="/index/privacy-setting">
                                    <template #title>
                                        <el-icon>
                                            <Lock/>
                                        </el-icon>
                                        账号安全设置
                                    </template>
                                </el-menu-item>

                              <el-menu-item index="/index/user-admin-setting"  v-if="show" >
                                <template #title>
                                  <el-icon>
                                    <Avatar />
                                  </el-icon>
                                  用户管理
                                </template>
                              </el-menu-item>

                                <el-menu-item index="/index/post-admin-setting"  v-if="show" >
                                    <template #title>
                                        <el-icon>
                                            <Comment />
                                        </el-icon>
                                        帖子管理
                                    </template>
                                </el-menu-item>

                            </el-sub-menu>
                        </el-menu>
                    </el-scrollbar>
                </el-aside>
                <el-main class="main-content-page">
                    <el-scrollbar style="height: calc(100vh - 55px)">
                        <router-view v-slot="{ Component }">
                            <transition name="el-fade-in-linear" mode="out-in">
                                <component :is="Component" style="height: 100%"/>
                            </transition>
                        </router-view>
                    </el-scrollbar>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<style lang="less" scoped>
.notification-item {
    transition: .3s;
    &:hover {
        cursor: pointer;
        opacity: 0.7;
    }
}

.notification {
    font-size: 22px;
    line-height: 14px;
    text-align: center;
    transition: color .3s;

    &:hover {
        color: grey;
        cursor: pointer;
    }
}

.main-content-page {
    padding: 0;
    background-color: #f7f8fa;
}

.dark .main-content-page {
    background-color: #212225;
}

.main-content {
    height: 100vh;
    width: 100vw;
}

.main-content-header {
    border-bottom: solid 1px var(--el-border-color);
    height: 55px;
    display: flex;
    align-items: center;
    box-sizing: border-box;

    .logo {
        height: 32px;
    }

    .user-info {
        display: flex;
        justify-content: flex-end;
        align-items: center;

        .el-avatar:hover {
            cursor: pointer;
        }

        .profile {
            text-align: right;
            margin-right: 20px;

            :first-child {
                font-size: 18px;
                font-weight: bold;
                line-height: 20px;
            }

            :last-child {
                font-size: 10px;
                color: grey;
            }
        }
    }
}
</style>
