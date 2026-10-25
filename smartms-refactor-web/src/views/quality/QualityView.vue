<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="switchTab">
      <!-- 质检模板 -->
      <el-tab-pane label="质检模板" name="template">
        <div class="toolbar">
          <el-input v-model="tplQuery.name" placeholder="模板名" clearable style="width: 150px" />
          <el-button type="primary" :icon="Search" @click="loadTemplates">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openTplDialog()">新增模板</el-button>
        </div>
        <el-table :data="tplList" v-loading="loading" stripe border size="small">
          <el-table-column prop="name" label="模板名" min-width="140" />
          <el-table-column prop="desc" label="说明" min-width="150" />
          <el-table-column label="分数区间" width="180">
            <template #default="{ row }">优≥{{ row.excellent }} 良≥{{ row.fine }} 及≥{{ row.pass }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === '2' ? 'success' : 'info'">{{ row.status === '2' ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="creater" label="创建人" width="80" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openTplDialog(row)">编辑</el-button>
              <el-button size="small" :type="row.status === '2' ? 'warning' : 'success'"
                @click="toggleStatus(row)">{{ row.status === '2' ? '停用' : '启用' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 待质检通话 -->
      <el-tab-pane label="待质检通话" name="calls">
        <div class="toolbar">
          <el-input v-model="callQuery.ani" placeholder="主叫" clearable style="width: 120px" />
          <el-input v-model="callQuery.agentId" placeholder="坐席" clearable style="width: 100px" />
          <el-select v-model="callQuery.templateId" placeholder="选择模板" style="width: 150px">
            <el-option v-for="t in tplList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadCalls">查询</el-button>
        </div>
        <el-table :data="callList" v-loading="loading" stripe border size="small">
          <el-table-column prop="talkStartTime" label="通话时间" min-width="150" />
          <el-table-column prop="bizANI" label="主叫" width="110" />
          <el-table-column prop="bizDNIS" label="被叫" width="110" />
          <el-table-column prop="agentID" label="坐席" width="80" />
          <el-table-column prop="agentName" label="坐席名" width="90" />
          <el-table-column label="时长" width="80">
            <template #default="{ row }">{{ Math.round((row.timeLength || 0) / 1000) }}s</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openScore(row)">质检打分</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 质检成绩 -->
      <el-tab-pane label="质检成绩" name="results">
        <div class="toolbar">
          <el-button type="primary" :icon="Search" @click="loadResults">查询</el-button>
        </div>
        <el-table :data="resultList" v-loading="loading" stripe border size="small">
          <el-table-column prop="contactId" label="通话ID" min-width="180" show-overflow-tooltip />
          <el-table-column prop="agentid" label="坐席" width="80" />
          <el-table-column prop="inspector" label="质检员" width="80" />
          <el-table-column label="等级" width="80">
            <template #default="{ row }">
              <el-tag :type="gradeType(row.resultGrade)">{{ gradeText(row.resultGrade) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="resultScore" label="总分" width="80" />
          <el-table-column prop="createTime" label="时间" min-width="150" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="page" v-model:page-size="size"
      :page-sizes="[10, 15, 20, 50]" @current-change="reload" @size-change="reload" />

    <!-- 模板弹窗 -->
    <el-dialog v-model="tplVisible" :title="tplForm.id ? '编辑模板' : '新增模板'" width="640px">
      <el-form ref="tplFormRef" :model="tplForm" :rules="tplRules" label-width="90px">
        <el-form-item label="模板名" prop="name"><el-input v-model="tplForm.name" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="tplForm.desc" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="优秀分"><el-input-number v-model="tplForm.excellent" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="良好分"><el-input-number v-model="tplForm.fine" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="及格分"><el-input-number v-model="tplForm.pass" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="质检项">
          <div style="width:100%">
            <div v-for="(item, i) in tplItems" :key="i" class="item-row">
              <el-input v-model="item.name" placeholder="质检项名称" style="width: 220px" />
              <el-input-number v-model="item.weight" :min="0" :max="100" placeholder="权重" style="width: 120px" />
              <el-button type="danger" size="small" @click="tplItems.splice(i, 1)">删</el-button>
            </div>
            <el-button size="small" type="primary" plain @click="tplItems.push({ name: '', weight: 0 })">+ 添加质检项</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tplVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveTemplateItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 质检打分弹窗 -->
    <el-dialog v-model="scoreVisible" title="质检打分" width="560px">
      <el-form label-width="110px">
        <el-form-item label="通话信息">
          <span style="color:#64748b">{{ scoreCall?.bizANI }} → {{ scoreCall?.bizDNIS }}（{{ scoreCall?.agentID }}）</span>
        </el-form-item>
        <el-form-item v-for="(item, i) in scoreItems" :key="i" :label="item.name + '(' + item.weight + '分)'">
          <el-slider v-model="item.score" :max="100" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitScore">提交成绩</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageTemplates, saveTemplate, changeTemplateStatus, pageInspectCalls, pageResults, generateResult } from '../../api/quality'

const route = useRoute()
const activeTab = ref('template')
const mine = ref(false)
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(15)

const tplList = ref([])
const callList = ref([])
const resultList = ref([])
const tplVisible = ref(false)
const scoreVisible = ref(false)
const tplFormRef = ref()

const tplQuery = reactive({ name: '' })
const callQuery = reactive({ ani: '', agentId: '', templateId: null })
const tplForm = reactive({ id: null, name: '', desc: '', excellent: 90, fine: 75, pass: 60 })
const tplItems = ref([{ name: '', weight: 0 }])
const tplRules = { name: [{ required: true, message: '请输入模板名', trigger: 'blur' }] }

const scoreCall = ref(null)
const scoreItems = ref([])

function gradeText(g) { return { 1: '不及格', 2: '及格', 3: '良好', 4: '优秀' }[g] || g }
function gradeType(g) { return { 1: 'danger', 2: 'warning', 3: '', 4: 'success' }[g] || 'info' }

async function loadTemplates() {
  loading.value = true
  try {
    const res = await pageTemplates({ page: page.value, size: size.value, ...tplQuery })
    tplList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

async function loadCalls() {
  loading.value = true
  try {
    const res = await pageInspectCalls({ page: page.value, size: size.value, ...callQuery })
    callList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

async function loadResults() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (mine.value) params.agentId = 'my'
    const res = await pageResults(params)
    resultList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

function switchTab() {
  page.value = 1
  if (activeTab.value === 'template') loadTemplates()
  else if (activeTab.value === 'calls') { loadCalls(); loadTemplates() }
  else loadResults()
}

function reload() {
  activeTab.value === 'template' ? loadTemplates() : activeTab.value === 'calls' ? loadCalls() : loadResults()
}

function openTplDialog(row) {
  Object.assign(tplForm, { id: null, name: '', desc: '', excellent: 90, fine: 75, pass: 60 })
  tplItems.value = [{ name: '', weight: 0 }]
  if (row) {
    Object.assign(tplForm, { id: row.id, name: row.name, desc: row.desc, excellent: +row.excellent, fine: +row.fine, pass: +row.pass })
    if (row.qualityColumns) {
      tplItems.value = row.qualityColumns.split(';').filter(Boolean).map(s => {
        const [name, weight] = s.split('=')
        return { name, weight: +weight }
      })
    }
  }
  tplVisible.value = true
}

async function saveTemplateItem() {
  const qualityColumns = tplItems.value.filter(i => i.name).map(i => i.name + '=' + i.weight).join(';')
  await saveTemplate({ ...tplForm, qualityColumns })
  ElMessage.success('保存成功')
  tplVisible.value = false
  loadTemplates()
}

async function toggleStatus(row) {
  const next = row.status === '2' ? '1' : '2'
  await changeTemplateStatus(row.id, next)
  ElMessage.success(next === '2' ? '已启用' : '已停用')
  loadTemplates()
}

function openScore(row) {
  scoreCall.value = row
  const tpl = tplList.value.find(t => t.id === callQuery.templateId)
  if (!tpl?.qualityColumns) { ElMessage.warning('请先选择模板'); return }
  scoreItems.value = tpl.qualityColumns.split(';').filter(Boolean).map(s => {
    const [name, weight] = s.split('=')
    return { name, weight: +weight, score: 100 }
  })
  scoreVisible.value = true
}

async function submitScore() {
  const tpl = tplList.value.find(t => t.id === callQuery.templateId)
  await generateResult({
    templateId: callQuery.templateId,
    contactId: scoreCall.value.contactID,
    agentid: scoreCall.value.agentID,
    talkstarttime: scoreCall.value.talkStartTime,
    columnNames: scoreItems.value.map(i => i.name),
    columnScores: scoreItems.value.map(i => i.score),
    columnWeights: scoreItems.value.map(i => i.weight)
  })
  ElMessage.success('质检完成')
  scoreVisible.value = false
  loadCalls()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  if (route.query.mine === 'true') mine.value = true
  loadTemplates(); loadResults()
  if (activeTab.value === 'calls') loadCalls()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
.item-row { display: flex; gap: 8px; margin-bottom: 8px; align-items: center; }
</style>
