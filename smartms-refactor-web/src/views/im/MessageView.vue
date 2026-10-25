<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="reload">
      <!-- 精准回复 -->
      <el-tab-pane label="精准回复" name="topics">
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="搜索主题" clearable style="width: 180px" @keyup.enter="load" @clear="load" />
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openTopic()">新增主题</el-button>
        </div>
        <el-table :data="topicList" v-loading="loading" stripe border size="small">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="主题" min-width="240" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openTopic(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delTopic(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 自动回复 -->
      <el-tab-pane label="自动回复" name="replies">
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="搜索标题/描述" clearable style="width: 180px" @keyup.enter="load" @clear="load" />
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openReply()">新增知识</el-button>
        </div>
        <el-table :data="replyList" v-loading="loading" stripe border size="small">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="keywords" label="触发关键词" min-width="160" />
          <el-table-column prop="knowType" label="类型" width="90" />
          <el-table-column prop="verifyStatus" label="状态" width="90">
            <template #default="{ row }">{{ row.verifyStatus === '1' ? '已启用' : '未启用' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openReply(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delReply(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 消息记录 -->
      <el-tab-pane label="消息记录" name="records">
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="搜索发送人/内容" clearable style="width: 180px" @keyup.enter="load" @clear="load" />
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <span class="hint">来自微信/IM 接入的消息（im_msg_received）</span>
        </div>
        <el-table :data="msgList" v-loading="loading" stripe border size="small" height="520">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="fromusername" label="发送方" min-width="130" />
          <el-table-column prop="tousername" label="接收方" min-width="130" />
          <el-table-column prop="msgtype" label="类型" width="90" />
          <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
          <el-table-column prop="create_time" label="时间" min-width="150" />
          <el-table-column prop="handled" label="已处理" width="80">
            <template #default="{ row }">
              <el-tag :type="row.handled === 1 ? 'success' : 'info'" size="small">{{ row.handled === 1 ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 接入信息 -->
      <el-tab-pane label="接入信息" name="interfaces">
        <div class="toolbar">
          <el-button type="primary" :icon="Search" @click="load">刷新</el-button>
          <el-button type="success" :icon="Plus" @click="openInterface()">新增接入</el-button>
          <span class="hint">微信/支付宝/IM 公众号接入配置（im_interface_info）</span>
        </div>
        <el-table :data="interfaceList" v-loading="loading" stripe border size="small">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="appid" label="AppID" min-width="180" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="90">
            <template #default="{ row }">{{ row.type === 1 ? '微信' : row.type === 2 ? '支付宝' : row.type }}</template>
          </el-table-column>
          <el-table-column prop="token" label="Token" min-width="120" show-overflow-tooltip />
          <el-table-column prop="upload_url" label="上传地址" min-width="160" show-overflow-tooltip />
          <el-table-column prop="download_url" label="下载地址" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openInterface(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delInterface(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 主题/知识/接入 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="90px" v-if="!isInterface">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <template v-if="isReply">
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="链接">
            <el-input v-model="form.url" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="form.knowType" style="width: 200px">
              <el-option label="文本" value="text" />
              <el-option label="图文" value="news" />
              <el-option label="图片" value="image" />
            </el-select>
          </el-form-item>
          <el-form-item label="启用">
            <el-switch v-model="form.verifyStatus" active-value="1" inactive-value="0" />
          </el-form-item>
        </template>
      </el-form>
      <el-form :model="ifaceForm" label-width="110px" v-else>
        <el-form-item label="AppID" required>
          <el-input v-model="ifaceForm.appid" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="ifaceForm.type" style="width: 200px">
            <el-option label="微信" :value="1" />
            <el-option label="支付宝" :value="2" />
            <el-option label="其他" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="Token">
          <el-input v-model="ifaceForm.token" />
        </el-form-item>
        <el-form-item label="上传地址">
          <el-input v-model="ifaceForm.upload_url" />
        </el-form-item>
        <el-form-item label="下载地址">
          <el-input v-model="ifaceForm.download_url" />
        </el-form-item>
        <el-form-item label="发送地址">
          <el-input v-model="ifaceForm.send_url" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="isInterface ? saveIface() : save()">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageTopics, saveTopic, deleteTopic, pageReplies, saveReply, deleteReply, pageMsgRecords,
  pageInterfaces, saveInterface, deleteInterface } from '../../api/im'

const route = useRoute()
const activeTab = ref('topics')
const loading = ref(false)
const keyword = ref('')
const topicList = ref([])
const replyList = ref([])
const msgList = ref([])
const interfaceList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isReply = ref(false)
const isInterface = ref(false)
const editingId = ref(null)
const form = reactive({ title: '', description: '', url: '', knowType: 'text', verifyStatus: '1' })
const ifaceForm = reactive({ appid: '', type: 1, token: '', upload_url: '', download_url: '', send_url: '' })

async function load() {
  loading.value = true
  try {
    if (activeTab.value === 'topics') {
      topicList.value = await pageTopics(keyword.value)
    } else if (activeTab.value === 'replies') {
      replyList.value = await pageReplies(keyword.value)
    } else if (activeTab.value === 'records') {
      msgList.value = await pageMsgRecords(keyword.value, 100)
    } else {
      interfaceList.value = await pageInterfaces()
    }
  } finally { loading.value = false }
}

function reload() { load() }

function openTopic(row) {
  isReply.value = false
  isInterface.value = false
  dialogTitle.value = row ? '编辑主题' : '新增主题'
  editingId.value = row ? row.id : null
  Object.assign(form, { title: row ? row.title : '', description: '', url: '', knowType: 'text', verifyStatus: '1' })
  dialogVisible.value = true
}

function openReply(row) {
  isReply.value = true
  isInterface.value = false
  dialogTitle.value = row ? '编辑知识' : '新增知识'
  editingId.value = row ? row.id : null
  Object.assign(form, {
    title: row ? row.title : '',
    description: row ? row.description || '' : '',
    url: row ? row.url || '' : '',
    knowType: row ? row.knowType || 'text' : 'text',
    verifyStatus: row ? row.verifyStatus || '1' : '1'
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.title) { ElMessage.warning('请输入标题'); return }
  const payload = { ...form }
  if (editingId.value) payload.id = editingId.value
  if (isReply.value) {
    await saveReply(payload)
  } else {
    await saveTopic(payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function delTopic(row) {
  await ElMessageBox.confirm('确定删除该主题吗？', '提示', { type: 'warning' })
  await deleteTopic(row.id)
  ElMessage.success('删除成功')
  load()
}

async function delReply(row) {
  await ElMessageBox.confirm('确定删除该知识吗？', '提示', { type: 'warning' })
  await deleteReply(row.id)
  ElMessage.success('删除成功')
  load()
}

function openInterface(row) {
  isInterface.value = true
  isReply.value = false
  dialogTitle.value = row ? '编辑接入' : '新增接入'
  editingId.value = row ? row.id : null
  Object.assign(ifaceForm, {
    appid: row ? row.appid || '' : '',
    type: row ? row.type || 1 : 1,
    token: row ? row.token || '' : '',
    upload_url: row ? row.upload_url || '' : '',
    download_url: row ? row.download_url || '' : '',
    send_url: row ? row.send_url || '' : ''
  })
  dialogVisible.value = true
}

async function saveIface() {
  if (!ifaceForm.appid) { ElMessage.warning('请输入 AppID'); return }
  const payload = { ...ifaceForm }
  if (editingId.value) payload.id = editingId.value
  await saveInterface(payload)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function delInterface(row) {
  await ElMessageBox.confirm('确定删除该接入配置吗？', '提示', { type: 'warning' })
  await deleteInterface(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  load()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.hint { color: #94a3b8; font-size: 12px; }
</style>
