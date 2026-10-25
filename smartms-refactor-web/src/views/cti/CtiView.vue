<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="reload">
      <!-- 链路配置 -->
      <el-tab-pane label="链路配置" name="links">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('links')">新增链路</el-button></div>
        <el-table :data="linksList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="linkServername" label="服务器名" min-width="130" />
          <el-table-column prop="linkUsername" label="用户名" min-width="110" />
          <el-table-column prop="linkVersion" label="版本" width="90" />
          <el-table-column prop="linkApitype" label="API类型" width="90" />
          <el-table-column prop="pbxType" label="PBX类型" width="90" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('links', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('links', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 号码配置 -->
      <el-tab-pane label="号码配置" name="extensions">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('extensions')">新增号码</el-button></div>
        <el-table :data="extensionsList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="extnDn" label="号码" min-width="130" />
          <el-table-column prop="extnType" label="类型" width="90" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('extensions', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('extensions', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- CTI 参数 -->
      <el-tab-pane label="CTI 参数" name="profiles">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('profiles')">新增参数</el-button></div>
        <el-table :data="profilesList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="conSection" label="配置段" min-width="120" />
          <el-table-column prop="conKey" label="参数名" min-width="140" />
          <el-table-column prop="conValue" label="参数值" min-width="120" />
          <el-table-column prop="conDesc" label="说明" min-width="140" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('profiles', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('profiles', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 录音配置 -->
      <el-tab-pane label="录音配置" name="records">
        <div class="toolbar"><el-button type="success" :icon="Plus" @click="openDialog('records')">新增配置</el-button></div>
        <el-table :data="recordsList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="extnDn" label="分机" min-width="120" />
          <el-table-column prop="recordPath" label="录音路径" min-width="260" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('records', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('records', row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 技能组 -->
      <el-tab-pane label="技能组" name="skills">
        <div class="toolbar">
          <el-input v-model="skillQuery.keyword" placeholder="技能名搜索" clearable style="width: 160px" />
          <el-button type="primary" :icon="Search" @click="loadSkills">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openDialog('skills')">新增技能组</el-button>
        </div>
        <el-table :data="skillsList" v-loading="loading" stripe border size="small">
          <el-table-column prop="rid" label="ID" width="60" />
          <el-table-column prop="skillId" label="技能ID" width="90" />
          <el-table-column prop="skillName" label="技能名" min-width="110" />
          <el-table-column prop="skillDesc" label="描述" min-width="140" />
          <el-table-column prop="channelType" label="通道类型" width="90" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDialog('skills', row)">编辑</el-button>
              <el-button size="small" type="danger" @click="delRow('skills', row)">删除</el-button>
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
import { Search, Plus } from '@element-plus/icons-vue'
import { ctiPage, ctiSave, ctiDelete, ivrPage, ivrSave, ivrDelete } from '../../api/config'

const route = useRoute()
const activeTab = ref('links')
const loading = ref(false)
const total = ref(0)
const page = ref(1)

const linksList = ref([])
const extensionsList = ref([])
const profilesList = ref([])
const recordsList = ref([])
const skillsList = ref([])
const skillQuery = reactive({ keyword: '' })

const dialogVisible = ref(false)
const dialogResource = ref('links')
const dialogTitle = ref('')
const editing = ref(false)
const currentRow = reactive({})

// 各资源字段定义（label, key）
const FIELDS = {
  links: [
    { label: '服务器名', key: 'linkServername' }, { label: '用户名', key: 'linkUsername' },
    { label: '密码', key: 'linkPassword' }, { label: '版本', key: 'linkVersion' },
    { label: 'API类型', key: 'linkApitype' }, { label: 'PBX类型', key: 'pbxType' }
  ],
  extensions: [{ label: '号码', key: 'extnDn' }, { label: '类型', key: 'extnType' }],
  profiles: [
    { label: '配置段', key: 'conSection' }, { label: '参数名', key: 'conKey' },
    { label: '参数值', key: 'conValue' }, { label: '说明', key: 'conDesc' }
  ],
  records: [{ label: '分机', key: 'extnDn' }, { label: '录音路径', key: 'recordPath' }],
  skills: [
    { label: '技能ID', key: 'skillId' }, { label: '技能名', key: 'skillName' },
    { label: '技能组类型', key: 'skillGroupType' }, { label: '通道类型', key: 'channelType' },
    { label: '描述', key: 'skillDesc' }
  ]
}

function fieldsOf(r) { return FIELDS[r] || [] }

async function loadLinks() {
  loading.value = true
  try {
    const res = await ctiPage('links', { page: page.value, size: 15 })
    linksList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadExtensions() {
  loading.value = true
  try {
    const res = await ctiPage('extensions', { page: page.value, size: 15 })
    extensionsList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadProfiles() {
  loading.value = true
  try {
    const res = await ctiPage('profiles', { page: page.value, size: 15 })
    profilesList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadRecords() {
  loading.value = true
  try {
    const res = await ctiPage('records', { page: page.value, size: 15 })
    recordsList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadSkills() {
  loading.value = true
  try {
    const res = await ctiPage('skills', { page: page.value, size: 15, keyword: skillQuery.keyword })
    skillsList.value = res.list; total.value = res.total
  } finally { loading.value = false }
}

function reload() {
  const t = activeTab.value
  if (t === 'links') loadLinks()
  else if (t === 'extensions') loadExtensions()
  else if (t === 'profiles') loadProfiles()
  else if (t === 'records') loadRecords()
  else loadSkills()
}

function openDialog(resource, row) {
  dialogResource.value = resource
  dialogTitle.value = resource === 'links' ? '链路' : resource === 'extensions' ? '号码'
    : resource === 'profiles' ? '参数' : resource === 'records' ? '录音配置' : '技能组'
  editing.value = !!row
  Object.keys(currentRow).forEach(k => delete currentRow[k])
  if (row) Object.assign(currentRow, row)
  dialogVisible.value = true
}

async function saveRow() {
  const payload = { ...currentRow }
  if (editing.value) {
    // 编辑时带主键（rid）后端按 id 更新
  }
  if (dialogResource.value === 'skills') {
    await ctiSave('skills', payload)
  } else {
    await ctiSave(dialogResource.value, payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  reload()
}

async function delRow(resource, row) {
  await ElMessageBox.confirm('确定删除该记录吗？', '提示', { type: 'warning' })
  const id = row.rid ?? row.id
  if (resource === 'skills') {
    await ctiDelete('skills', id)
  } else {
    await ctiDelete(resource, id)
  }
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
