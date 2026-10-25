<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="reload">
      <!-- SIP 分机 -->
      <el-tab-pane label="SIP 分机" name="device">
        <div class="toolbar">
          <el-input v-model="deviceQuery.id" placeholder="分机号" clearable style="width: 130px" />
          <el-button type="primary" :icon="Search" @click="loadDevices">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openDevice()">新增分机</el-button>
        </div>
        <el-table :data="deviceList" v-loading="loading" stripe border size="small">
          <el-table-column prop="id" label="分机号" width="100" />
          <el-table-column prop="cid" label="CID" width="100" />
          <el-table-column prop="type" label="类型" width="80" />
          <el-table-column prop="port" label="端口" width="80" />
          <el-table-column prop="context" label="拨号方案" min-width="120" />
          <el-table-column prop="secret" label="密码" min-width="100" />
          <el-table-column prop="host" label="Host" min-width="120" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDevice(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delDevice(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 出局中继 -->
      <el-tab-pane label="出局中继" name="trunk">
        <div class="toolbar">
          <el-button type="success" :icon="Plus" @click="openTrunk()">新增中继</el-button>
        </div>
        <el-table :data="trunkList" v-loading="loading" stripe border size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column prop="type" label="类型" width="80" />
          <el-table-column prop="sipHost" label="SIP主机" min-width="130" />
          <el-table-column prop="sipPort" label="端口" width="80" />
          <el-table-column prop="prefix" label="前缀" width="80" />
          <el-table-column label="启用" width="80">
            <template #default="{ row }">
              <el-tag :type="row.disable === 'off' ? 'success' : 'info'">{{ row.disable === 'off' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openTrunk(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delTrunk(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 路由 -->
      <el-tab-pane label="PBX 路由" name="route">
        <div class="toolbar">
          <el-button type="success" :icon="Plus" @click="openRoute()">新增路由</el-button>
        </div>
        <el-table :data="routeList" v-loading="loading" stripe border size="small">
          <el-table-column prop="routeId" label="ID" width="60" />
          <el-table-column prop="routeName" label="路由名" min-width="130" />
          <el-table-column prop="routeDial" label="拨号规则" min-width="120" />
          <el-table-column prop="routeDemo" label="说明" min-width="140" />
          <el-table-column label="绑定中继" min-width="140">
            <template #default="{ row }">{{ (row.trunkIds || []).join(', ') || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openRoute(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRoute(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 局向 -->
      <el-tab-pane label="局向分配" name="distribute">
        <div class="toolbar">
          <el-button type="success" :icon="Plus" @click="openDistribute()">新增局向</el-button>
        </div>
        <el-table :data="distributeList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="trunkId" label="中继ID" width="90" />
          <el-table-column prop="trunkName" label="中继名" min-width="130" />
          <el-table-column prop="trunkType" label="类型" width="80" />
          <el-table-column prop="tacCode" label="区号" width="80" />
          <el-table-column prop="circuitCode" label="电路号" min-width="100" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDistribute(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delDistribute(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination class="pager" background layout="total, prev, pager, next"
      :total="total" v-model:current-page="page" :page-size="size"
      @current-change="reload" />

    <!-- 分机弹窗 -->
    <el-dialog v-model="deviceVisible" :title="deviceForm.id ? '编辑分机' : '新增分机'" width="520px">
      <el-form ref="deviceFormRef" :model="deviceForm" :rules="deviceRules" label-width="90px">
        <el-form-item label="分机号" prop="id"><el-input v-model="deviceForm.id" :disabled="!!deviceForm.id" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="deviceForm.secret" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="类型"><el-input v-model="deviceForm.type" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="端口"><el-input v-model="deviceForm.port" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="CID"><el-input v-model="deviceForm.cid" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="DTMF"><el-input v-model="deviceForm.dtmfmode" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="拨号方案"><el-input v-model="deviceForm.context" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deviceVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDeviceItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 中继弹窗 -->
    <el-dialog v-model="trunkVisible" :title="trunkForm.id ? '编辑中继' : '新增中继'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="名称"><el-input v-model="trunkForm.name" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="类型"><el-input v-model="trunkForm.type" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="前缀"><el-input v-model="trunkForm.prefix" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="SIP主机"><el-input v-model="trunkForm.sipHost" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="端口"><el-input v-model="trunkForm.sipPort" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="用户名"><el-input v-model="trunkForm.sipUsername" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="密码"><el-input v-model="trunkForm.sipSecret" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="trunkVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTrunkItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 路由弹窗 -->
    <el-dialog v-model="routeVisible" :title="routeForm.routeId ? '编辑路由' : '新增路由'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="路由名"><el-input v-model="routeForm.routeName" /></el-form-item>
        <el-form-item label="拨号规则"><el-input v-model="routeForm.routeDial" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="routeForm.routeDemo" /></el-form-item>
        <el-form-item label="绑定中继">
          <el-input v-model="routeForm.trunkIdsText" placeholder="中继ID，逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="routeVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRouteItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 局向弹窗 -->
    <el-dialog v-model="distributeVisible" :title="distributeForm.rid ? '编辑局向' : '新增局向'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="中继ID"><el-input v-model="distributeForm.trunkId" /></el-form-item>
        <el-form-item label="中继名"><el-input v-model="distributeForm.trunkName" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="类型"><el-input v-model="distributeForm.trunkType" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="区号"><el-input v-model="distributeForm.tacCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电路号"><el-input v-model="distributeForm.circuitCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="方向"><el-input v-model="distributeForm.trunkDirection" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="distributeVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDistributeItem">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageDevices, saveDevice, deleteDevice, pageTrunks, saveTrunk, deleteTrunk,
  pageRoutes, saveRoute, deleteRoute, pageTrunkDistributes, saveTrunkDistribute, deleteTrunkDistribute } from '../../api/pbx'

const route = useRoute()
const activeTab = ref('device')
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = 15

const deviceList = ref([])
const trunkList = ref([])
const routeList = ref([])
const distributeList = ref([])

const deviceQuery = reactive({ id: '' })
const deviceVisible = ref(false)
const trunkVisible = ref(false)
const routeVisible = ref(false)
const distributeVisible = ref(false)
const deviceFormRef = ref()

const deviceForm = reactive({ id: '', secret: '', type: 'peer', port: '5060', cid: '', dtmfmode: 'rfc2833', context: 'from-internal' })
const trunkForm = reactive({ id: null, name: '', type: 'SIP', prefix: '', sipHost: '', sipPort: '5060', sipUsername: '', sipSecret: '' })
const routeForm = reactive({ routeId: null, routeName: '', routeDial: '', routeDemo: '', trunkIdsText: '' })
const distributeForm = reactive({ rid: null, trunkId: '', trunkName: '', trunkType: '5', tacCode: '', circuitCode: '', trunkDirection: '' })
const deviceRules = { id: [{ required: true, message: '请输入分机号', trigger: 'blur' }] }

async function loadDevices() {
  loading.value = true
  try {
    const res = await pageDevices({ page: page.value, size, ...deviceQuery })
    deviceList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}
async function loadTrunks() {
  loading.value = true
  try {
    const res = await pageTrunks({ page: page.value, size })
    trunkList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}
async function loadRoutes() {
  loading.value = true
  try {
    const res = await pageRoutes({ page: page.value, size })
    routeList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}
async function loadDistributes() {
  loading.value = true
  try {
    const res = await pageTrunkDistributes({ page: page.value, size })
    distributeList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

function reload() {
  if (activeTab.value === 'device') loadDevices()
  else if (activeTab.value === 'trunk') loadTrunks()
  else if (activeTab.value === 'route') loadRoutes()
  else loadDistributes()
}

function openDevice(row) {
  Object.assign(deviceForm, { id: '', secret: '', type: 'peer', port: '5060', cid: '', dtmfmode: 'rfc2833', context: 'from-internal' })
  if (row) Object.assign(deviceForm, row)
  deviceVisible.value = true
}
async function saveDeviceItem() {
  await deviceFormRef.value.validate()
  await saveDevice({ ...deviceForm })
  ElMessage.success('保存成功')
  deviceVisible.value = false
  loadDevices()
}
async function delDevice(row) {
  await ElMessageBox.confirm(`确定删除分机「${row.id}」吗？`, '提示', { type: 'warning' })
  await deleteDevice(row.id)
  ElMessage.success('删除成功')
  loadDevices()
}

function openTrunk(row) {
  Object.assign(trunkForm, { id: null, name: '', type: 'SIP', prefix: '', sipHost: '', sipPort: '5060', sipUsername: '', sipSecret: '' })
  if (row) Object.assign(trunkForm, row)
  trunkVisible.value = true
}
async function saveTrunkItem() {
  await saveTrunk({ ...trunkForm })
  ElMessage.success('保存成功')
  trunkVisible.value = false
  loadTrunks()
}
async function delTrunk(row) {
  await ElMessageBox.confirm('确定删除该中继吗？', '提示', { type: 'warning' })
  await deleteTrunk(row.id)
  ElMessage.success('删除成功')
  loadTrunks()
}

function openRoute(row) {
  Object.assign(routeForm, { routeId: null, routeName: '', routeDial: '', routeDemo: '', trunkIdsText: '' })
  if (row) Object.assign(routeForm, { routeId: row.routeId, routeName: row.routeName, routeDial: row.routeDial, routeDemo: row.routeDemo, trunkIdsText: (row.trunkIds || []).join(',') })
  routeVisible.value = true
}
async function saveRouteItem() {
  const trunkIds = routeForm.trunkIdsText ? routeForm.trunkIdsText.split(',').map(s => +s.trim()).filter(Boolean) : []
  await saveRoute({ routeId: routeForm.routeId, routeName: routeForm.routeName, routeDial: routeForm.routeDial, routeDemo: routeForm.routeDemo, trunkIds })
  ElMessage.success('保存成功')
  routeVisible.value = false
  loadRoutes()
}
async function delRoute(row) {
  await ElMessageBox.confirm(`确定删除路由「${row.routeName}」吗？`, '提示', { type: 'warning' })
  await deleteRoute(row.routeId)
  ElMessage.success('删除成功')
  loadRoutes()
}

function openDistribute(row) {
  Object.assign(distributeForm, { rid: null, trunkId: '', trunkName: '', trunkType: '5', tacCode: '', circuitCode: '', trunkDirection: '' })
  if (row) Object.assign(distributeForm, row)
  distributeVisible.value = true
}
async function saveDistributeItem() {
  await saveTrunkDistribute({ ...distributeForm })
  ElMessage.success('保存成功')
  distributeVisible.value = false
  loadDistributes()
}
async function delDistribute(row) {
  await ElMessageBox.confirm('确定删除该局向吗？', '提示', { type: 'warning' })
  await deleteTrunkDistribute(row.rid)
  ElMessage.success('删除成功')
  loadDistributes()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  if (activeTab.value === 'device') loadDevices()
  else if (activeTab.value === 'trunk') loadTrunks()
  else if (activeTab.value === 'route') loadRoutes()
  else loadDistributes()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
