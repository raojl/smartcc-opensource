<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="switchTab">
      <!-- 任务列表 -->
      <el-tab-pane label="任务管理" name="task">
        <div class="toolbar">
          <el-input v-model="query.taskDesc" placeholder="任务描述" clearable style="width: 160px" />
          <el-button type="primary" :icon="Search" @click="loadTasks">查询</el-button>
          <el-button type="success" :icon="Upload" @click="importVisible = true">导入号码</el-button>
        </div>
        <el-table :data="taskList" v-loading="loading" stripe border size="small">
          <el-table-column prop="task_id" label="任务ID" min-width="200" show-overflow-tooltip />
          <el-table-column prop="desc" label="任务描述" min-width="130" />
          <el-table-column prop="agentid" label="坐席" min-width="100" />
          <el-table-column label="完成率" width="90">
            <template #default="{ row }">{{ row.wcl }}</template>
          </el-table-column>
          <el-table-column label="接通率" width="90">
            <template #default="{ row }">{{ row.jtl }}</template>
          </el-table-column>
          <el-table-column prop="timestamp" label="创建时间" min-width="150" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="handleStart(row)">启动</el-button>
              <el-button size="small" type="warning" @click="handleStop(row)">停止</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 任务详单 -->
      <el-tab-pane label="任务详单" name="detail">
        <div class="toolbar">
          <el-input v-model="detailQuery.taskId" placeholder="任务ID" clearable style="width: 200px" />
          <el-input v-model="detailQuery.phoneNumber" placeholder="号码" clearable style="width: 140px" />
          <el-button type="primary" :icon="Search" @click="loadDetails">查询</el-button>
        </div>
        <el-table :data="detailList" v-loading="loading" stripe border size="small">
          <el-table-column prop="taskId" label="任务ID" min-width="200" show-overflow-tooltip />
          <el-table-column prop="phoneNumber" label="号码" min-width="120" />
          <el-table-column prop="callingNumber" label="呼叫号码" min-width="120" />
          <el-table-column prop="agentid" label="坐席" width="90" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">{{ row.taskStatus === '1' ? '呼叫中' : '待呼叫' }}</template>
          </el-table-column>
          <el-table-column prop="outboundStatus" label="外呼状态" width="100" />
          <el-table-column prop="timestamp" label="时间" min-width="150" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="page" v-model:page-size="size"
      :page-sizes="[10, 15, 20, 50]" @current-change="reload" @size-change="reload" />

    <!-- 号码导入弹窗 -->
    <el-dialog v-model="importVisible" title="导入外呼号码" width="560px">
      <el-form label-width="90px">
        <el-form-item label="任务描述" required>
          <el-input v-model="importForm.taskDesc" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="号码列表" required>
          <el-input v-model="importForm.numbersText" type="textarea" :rows="8"
            placeholder="每行一个号码，支持「呼叫号码,被叫号码」格式" />
        </el-form-item>
        <el-form-item label="坐席分配">
          <el-input v-model="importForm.agentsText" placeholder="平均分配坐席，多个用分号分隔（留空=当前坐席）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload } from '@element-plus/icons-vue'
import { pageTasks, pageTaskDetails, startTask, stopTask, deleteTask, importNumbers } from '../../api/outbound'

const route = useRoute()
const activeTab = ref('task')
const mine = ref(false)
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(15)
const importVisible = ref(false)

const taskList = ref([])
const detailList = ref([])
const query = reactive({ taskDesc: '' })
const detailQuery = reactive({ taskId: '', phoneNumber: '' })
const importForm = reactive({ taskDesc: '', numbersText: '', agentsText: '' })

async function loadTasks() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, ...query }
    if (mine.value) params.agentId = 'my'
    const res = await pageTasks(params)
    taskList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

async function loadDetails() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, ...detailQuery }
    if (mine.value) params.agentId = 'my'
    const res = await pageTaskDetails(params)
    detailList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

function switchTab() {
  page.value = 1
  activeTab.value === 'task' ? loadTasks() : loadDetails()
}

function reload() {
  activeTab.value === 'task' ? loadTasks() : loadDetails()
}

async function handleStart(row) {
  await startTask(row.task_id)
  ElMessage.success('任务已启动')
  loadTasks()
}

async function handleStop(row) {
  await stopTask(row.task_id)
  ElMessage.success('任务已停止')
  loadTasks()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除任务「${row.desc || row.task_id}」吗？`, '提示', { type: 'warning' })
  await deleteTask(row.task_id)
  ElMessage.success('删除成功')
  loadTasks()
}

async function handleImport() {
  if (!importForm.taskDesc) { ElMessage.warning('请输入任务描述'); return }
  const numbers = importForm.numbersText.split('\n').map(s => s.trim()).filter(Boolean)
  if (numbers.length === 0) { ElMessage.warning('请输入号码列表'); return }
  const agentIds = importForm.agentsText ? importForm.agentsText.split(';').filter(Boolean) : null
  saving.value = true
  try {
    const res = await importNumbers({ numbers, taskDesc: importForm.taskDesc, agentIds })
    ElMessage.success(`成功导入 ${res.imported} 个号码`)
    importVisible.value = false
    importForm.numbersText = ''
    loadTasks()
  } catch (e) { } finally { saving.value = false }
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  if (route.query.mine === 'true') mine.value = true
  activeTab.value === 'task' ? loadTasks() : loadDetails()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
