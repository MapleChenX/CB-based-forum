<script setup>
import {Delta, QuillEditor} from "@vueup/vue-quill";
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {ref} from "vue";
import {post} from "@/net";
import {ElMessage} from "element-plus";

const props = defineProps({
    show: Boolean,
    tid: String,
    quote: Object
})

const content = ref()

const emit = defineEmits(['close', 'comment'])

const init = () => content.value = new Delta()

function submitComment() {
    if (deltaToText(content.value).length > 50000) {
        ElMessage.warning('评论字数已经超出最大限制，请缩减评论内容！')
        return
    }
    post('/api/forum/add-comment', {
        tid: props.tid,
        quote: props.quote ? props.quote.id : -1,
        content: JSON.stringify(content.value)
    }, () => {
        ElMessage.success('发表评论成功')
        emit('comment')
    })
}

function deltaToSimpleText(delta) {
    let str = deltaToText(JSON.parse(delta))
    if(str.length > 35) str = str.substring(0, 35) + "..."
    return str
}

function deltaToText(delta) {
    if(!delta?.ops) return ""
    let str = ""
    for (let op of delta.ops)
        str += op.insert
    return str.replace(/\s/g, "")
}

</script>

<template>
    <div>
        <el-drawer :model-value="show"
                   :title="quote ? `发表对评论: ${deltaToSimpleText(quote.content)} 的回复` : '发表帖子回复'"
                   @open="init" @close="emit('close')"
                   direction="btt" :size="270"
                   :close-on-click-modal="false">
            <div>
                <div>
                    <quill-editor style="height: 120px" v-model:content="content"
                                  placeholder="请发表友善的评论，不要使用脏话骂人，都是大学生素质高一点"/>
                </div>
                <div style="margin-top: 10px;display: flex">
                    <div style="flex: 1;font-size: 13px;color: grey">
                        字数统计: {{deltaToText(content).length}}（最大支持50000字）
                    </div>
                    <el-button type="success" @click="submitComment" plain>发表评论</el-button>
                </div>
            </div>
        </el-drawer>
    </div>
</template>

<style lang="less" scoped>
:deep(.el-drawer) {
    width: 800px;
    margin: 20px auto;
    border-radius: 10px;
}
:deep(.el-drawer__header) {
    margin: 0;
}
:deep(.el-drawer__body) {
    padding: 10px;
}
</style>
