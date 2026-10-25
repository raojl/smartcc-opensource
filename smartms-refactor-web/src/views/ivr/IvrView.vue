<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="reload">
      <!-- 语音服务 -->
      <el-tab-pane label="语音服务" name="services">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('services')">新增服务</el-button></div>
        <el-table :data="servicesList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="servicename" label="服务名" min-width="120" />
          <el-table-column prop="servicetype" label="类型" width="90" />
          <el-table-column prop="serviceip" label="IP" min-width="120" />
          <el-table-column prop="serviceport" label="端口" width="80" />
          <el-table-column prop="loadcfgtype" label="加载类型" width="90" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('services', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('services', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 中继群 -->
      <el-tab-pane label="中继群" name="trunkGroups">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('trunkGroups')">新增中继群</el-button></div>
        <el-table :data="trunkGroupsList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="trunkgroupid" label="中继群ID" min-width="110" />
          <el-table-column prop="trunkgroupname" label="中继群名" min-width="160" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('trunkGroups', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('trunkGroups', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- IVR 参数 -->
      <el-tab-pane label="IVR 参数" name="params">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('params')">新增参数</el-button></div>
        <el-table :data="paramsList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="servicename" label="服务名" min-width="120" />
          <el-table-column prop="paramname" label="参数名" min-width="140" />
          <el-table-column prop="paramvalue" label="参数值" min-width="120" />
          <el-table-column prop="paramnote" label="说明" min-width="140" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('params', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('params', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- IVR 路由 -->
      <el-tab-pane label="IVR 路由" name="routes">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('routes')">新增路由</el-button></div>
        <el-table :data="routesList" v-loading="loading" stripe border size="small">
          <el-table-column prop="trunksn" label="序号" width="70" />
          <el-table-column prop="routenum" label="路由号码" min-width="120" />
          <el-table-column prop="trunkgroupid" label="中继群" width="90" />
          <el-table-column prop="flowname" label="流程文件" min-width="180" />
          <el-table-column prop="displayname" label="显示名" min-width="110" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('routes', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('routes', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 语音文件 -->
      <el-tab-pane label="语音文件" name="files">
        <div class="toolbar"><span style="color:#64748b;font-size:12px">IVR 流程文件列表（文件管理）</span></div>
        <el-table :data="filesList" v-loading="loading" stripe border size="small">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="fileName" label="文件名" min-width="200" />
          <el-table-column prop="fileSize" label="大小" width="100" />
          <el-table-column prop="createTime" label="创建时间" min-width="150" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="delRow('files', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination class="pager" background layout="total, prev, pager, next"
      :total="total" v-model:current-page="page" :page-size="15" @current-change="reload" />

    <!-- 通用编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="(editing ? '编辑' : '新增') + dialogTitle" width="520px">
      <el-form :model="currentRow" label-width="100px">
        <el-form-item v-for="f in fieldsOf(dialogResource)" :key="f.key" :label="f.label">
          <el-input v-model="currentRow[f.key]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRow">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { ivrPage, ivrSave, ivrDelete } from '../../api/config'

const route = useRoute()
const activeTab = ref('services')
const loading = ref(false)
const total = ref(0)
const page = ref(1)

const servicesList = ref([])
const trunkGroupsList = ref([])
const paramsList = ref([])
const routesList = ref([])
const filesList = ref([])

const dialogVisible = ref(false)
const dialogResource = ref('services')
const dialogTitle = ref('')
const editing = ref(false)
const currentRow = reactive({})

const FIELDS = {
  services: [
    { label: '服务名', key: 'servicename' }, { label: '类型', key: 'servicetype' },
    { label: 'IP', key: 'serviceip' }, { label: '端口', key: 'serviceport' },
    { label: '加载类型', key: 'loadcfgtype' }
  ],
  trunkGroups: [{ label: '中继群ID', key: 'trunkgroupid' }, { label: '中继群名', key: 'trunkgroupname' }],
  params: [
    { label: '服务名', key: 'servicename' }, { label: '参数名', key: 'paramname' },
    { label: '参数值', key: 'paramvalue' }, { label: '说明', key: 'paramnote' }
  ],
  routes: [
    { label: '路由号码', key: 'routenum' }, { label: '中继群', key: 'trunkgroupid' },
    { label: '流程文件', key: 'flowname' }, { label: '显示名', key: 'displayname' },
    { label: '呼叫方向', key: 'calldirection' }, { label: '通道数', key: 'channelCount' }
  ],
  files: [{ label: '文件名', key: 'fileName' }]
}

function fieldsOf(r) { return FIELDS[r] || [] }

async function loadServices() {
  loading.value = true
  try {
    const res = await ivrPage('services', { page: page.value, size: 15 })
    servicesList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadTrunkGroups() {
  loading.value = true
  try {
    const res = await ivrPage('trunkGroups', { page: page.value, size: 15 })
    trunkGroupsList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadParams() {
  loading.value = true
  try {
    const res = await ivrPage('params', { page: page.value, size: 15 })
    paramsList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadRoutes() {
  loading.value = true
  try {
    const res = await ivrPage('routes', { page: page.value, size: 15 })
    routesList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadFiles() {
  loading.value = true
  try {
    const res = await ivrPage('files', { page: page.value, size: 15 })
    filesList.value = res.list || []; total.value = res.total || 0
  } catch (e) {
    filesList.value = []; total.value = 0
  } finally { loading.value = false }
}

function reload() {
  const t = activeTab.value
  if (t === 'services') loadServices()
  else if (t === 'trunkGroups') loadTrunkGroups()
  else if (t === 'params') loadParams()
  else if (t === 'routes') loadRoutes()
  else loadFiles()
}

function openDialog(resource, row) {
  dialogResource.value = resource
  dialogTitle.value = resource === 'services' ? '语音服务' : resource === 'trunkGroups' ? '中继群'
    : resource === 'params' ? 'IVR参数' : 'IVR路由'
  editing.value = !!row
  Object.keys(currentRow).forEach(k => delete currentRow[k])
  if (row) Object.assign(currentRow, row)
  dialogVisible.value = true
}

async function saveRow() {
  await ivrSave(dialogResource.value, { ...currentRow })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  reload()
}

async function delRow(resource, row) {
  await ElMessageBox.confirm('确定删除该记录吗？', '提示', { type: 'warning' })
  const id = row.rid ?? row.trunksn
  await ivrDelete(resource, id)
  ElMessage.success('删除成功')
  reload()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  reload()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
