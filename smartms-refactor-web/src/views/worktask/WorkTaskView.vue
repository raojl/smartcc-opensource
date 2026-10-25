<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="switchTab">
      <el-tab-pane label="工单信息" name="all">
        <div class="toolbar">
          <el-input v-model="query.workId" placeholder="工单号" clearable style="width: 180px" />
          <el-input v-model="query.customName" placeholder="客户名" clearable style="width: 130px" />
          <el-select v-model="query.workStatus" placeholder="状态" clearable style="width: 110px">
            <el-option label="待处理" value="1" />
            <el-option label="处理中" value="2" />
            <el-option label="已办结" value="3" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openDialog()">新建工单</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的工单" name="mine">
        <div class="toolbar">
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的受理" name="done">
        <div class="toolbar">
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-table :data="list" v-loading="loading" stripe border size="small">
      <el-table-column prop="workId" label="工单号" min-width="170" />
      <el-table-column prop="customName" label="客户" min-width="90" />
      <el-table-column prop="bussinessDesc" label="业务描述" min-width="150" show-overflow-tooltip />
      <el-table-column prop="belongAgent" label="归属坐席" width="90" />
      <el-table-column prop="workAgent" label="受理坐席" width="90" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">{{ statusText(row.workStatus) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="150" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="success" @click="handleFinish(row)">办结</el-button>
          <el-button size="small" type="warning" @click="openTransfer(row)">转办</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="query.page" v-model:page-size="query.size"
      :page-sizes="[10, 15, 20, 50]" @current-change="loadData" @size-change="loadData" />

    <!-- 新建/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.workId ? '编辑工单' : '新建工单'" width="640px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="工单号" prop="workId"><el-input v-model="form.workId" :disabled="!!form.workId" /></el-form-item>
        <el-form-item label="客户名"><el-input v-model="form.customName" /></el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="form.bussinessType" style="width:100%">
            <el-option label="呼入" value="3" /><el-option label="呼出" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.workStatus" style="width:100%">
            <el-option label="待处理" value="1" /><el-option label="处理中" value="2" /><el-option label="已办结" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属坐席"><el-input v-model="form.belongAgent" /></el-form-item>
        <el-form-item label="受理坐席"><el-input v-model="form.workAgent" /></el-form-item>
        <el-form-item label="业务描述"><el-input v-model="form.bussinessDesc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="解决方案"><el-input v-model="form.solution" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 转办 -->
    <el-dialog v-model="transferVisible" title="转办工单" width="420px">
      <el-form label-width="90px">
        <el-form-item label="目标坐席">
          <el-select v-model="transferAgent" placeholder="请选择坐席" style="width:100%" filterable>
            <el-option v-for="a in agents" :key="a.qualityAgCode"
              :label="a.userName + ' (' + a.qualityAgCode + ')'" :value="a.qualityAgCode" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransfer">确认转办</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageWorkTasks, createWorkTask, updateWorkTask, finishWorkTask, transferWorkTask, workTaskAgents } from '../../api/workTask'

const route = useRoute()
const activeTab = ref('all')
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const transferVisible = ref(false)
const formRef = ref()
const agents = ref([])
const transferAgent = ref('')

const query = reactive({ page: 1, size: 15, workId: '', customName: '', workStatus: '' })
const emptyForm = () => ({ workId: '', customName: '', bussinessType: '3', workStatus: '1', belongAgent: '', workAgent: '', bussinessDesc: '', solution: '' })
const form = reactive(emptyForm())
const formRules = { workId: [{ required: true, message: '请输入工单号', trigger: 'blur' }] }
let currentTransferId = null
let newFormId = ''

function statusText(s) { return s === '1' ? '待处理' : s === '2' ? '处理中' : s === '3' ? '已办结' : s }

async function loadData() {
  loading.value = true
  try {
    const params = { ...query }
    if (activeTab.value === 'mine') params.belongAgent = 'my'
    else if (activeTab.value === 'done') params.workAgent = 'my'
    const res = await pageWorkTasks(params)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

function switchTab() {
  query.page = 1
  loadData()
}

function openDialog(row) {
  Object.assign(form, emptyForm())
  newFormId = ''
  if (row) {
    Object.assign(form, row)
    newFormId = row.workId
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const isEdit = !!newFormId
    if (isEdit) {
      await updateWorkTask(form.workId, { ...form })
    } else {
      await createWorkTask({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { } finally { saving.value = false }
}

async function handleFinish(row) {
  await finishWorkTask(row.workId)
  ElMessage.success('工单已办结')
  loadData()
}

async function openTransfer(row) {
  currentTransferId = row.workId
  transferAgent.value = ''
  if (agents.value.length === 0) agents.value = await workTaskAgents()
  transferVisible.value = true
}

async function handleTransfer() {
  if (!transferAgent.value) { ElMessage.warning('请选择目标坐席'); return }
  await transferWorkTask(currentTransferId, transferAgent.value)
  ElMessage.success('转办成功')
  transferVisible.value = false
  loadData()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  loadData()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
