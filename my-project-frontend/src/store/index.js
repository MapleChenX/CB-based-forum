import { defineStore } from "pinia";
import axios from "axios";

export const useStore = defineStore('general', {
    state: () => {
        return {
            user: {
                id: -1,
                username: '',
                email: '',
                role: '',
                avatar: null,
                registerTime: null
            },
            forum: {
                types: []
            },types: [
                {
                    "id": 1,
                    "name": "日常闲聊",
                    "description": "在这里分享你的各种日常",
                    "color": "#1E90FF"
                },
                {
                    "id": 2,
                    "name": "真诚交友",
                    "description": "在校园里寻找与自己志同道合的朋友",
                    "color": "#CE1EFF"
                },
                {
                    "id": 3,
                    "name": "问题反馈",
                    "description": "反馈你在校园里遇到的问题",
                    "color": "#E07373"
                },
                {
                    "id": 4,
                    "name": "恋爱官宣",
                    "description": "向大家展示你的恋爱成果",
                    "color": "#E0CE73"
                },
                {
                    "id": 5,
                    "name": "踩坑记录",
                    "description": "将你遇到的坑分享给大家，防止其他人再次入坑",
                    "color": "#3BB62A"
                }
            ]

        }
    }, getters: {
        avatarUrl() {
            if(this.user.avatar)
                return `${axios.defaults.baseURL}/images${this.user.avatar}`
            else
                return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }
    }, actions: {
        findTypeById(id) {
            for (let type of this.forum.types) {
                if(type.id === id)
                    return type
            }
        },
        avatarUserUrl(avatar) {
            if(avatar)
                return `${axios.defaults.baseURL}/images${avatar}`
            else
                return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        },getTypeById(id) {
            for (let type of this.types) {
                if(id === type.id)
                    return type
            }
        }
    }
})
