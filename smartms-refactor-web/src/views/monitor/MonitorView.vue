<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="reload">
      <el-tab-pane label="话务监控" name="calls">
        <div class="toolbar">
          <el-button type="primary" size="small" :icon="Refresh" @click="reload">刷新</el-button>
          <span class="hint">CTI 实时服务在线时显示实时数据，未连接时显示占位</span>
        </div>
        <el-table :data="rows" v-loading="loading" stripe border size="small" height="520">
          <el-table-column v-for="c in columns" :key="c" :prop="c" :label="c" min-width="100" />
          <el-table-column v-if="columns.length === 0" label="暂无数据" min-width="200" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="座席状态" name="agents">
        <div class="toolbar">
          <el-button type="primary" size="small" :icon="Refresh" @click="reload">刷新</el-button>
        </div>
        <el-table :data="rows" v-loading="loading" stripe border size="small" height="520">
          <el-table-column v-for="c in columns" :key="c" :prop="c" :label="c" min-width="100" />
          <el-table-column v-if="columns.length === 0" label="暂无数据" min-width="200" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="技能统计" name="skills">
        <div class="toolbar">
          <el-button type="primary" size="small" :icon="Refresh" @click="reload">刷新</el-button>
        </div>
        <el-table :data="rows" v-loading="loading" stripe border size="small" height="520">
          <el-table-column v-for="c in columns" :key="c" :prop="c" :label="c" min-width="100" />
          <el-table-column v-if="columns.length === 0" label="暂无数据" min-width="200" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="IVR 通道" name="ivr">
        <div class="toolbar">
          <el-button type="primary" size="small" :icon="Refresh" @click="reload">刷新</el-button>
        </div>
        <el-table :data="rows" v-loading="loading" stripe border size="small" height="520">
          <el-table-column v-for="c in columns" :key="c" :prop="c" :label="c" min-width="100" />
          <el-table-column v-if="columns.length === 0" label="暂无数据" min-width="200" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="坐席整体" name="all">
        <div class="toolbar">
          <el-button type="primary" size="small" :icon="Refresh" @click="reload">刷新</el-button>
        </div>
        <el-table :data="rows" v-loading="loading" stripe border size="small" height="520">
          <el-table-column v-for="c in columns" :key="c" :prop="c" :label="c" min-width="100" />
          <el-table-column v-if="columns.length === 0" label="暂无数据" min-width="200" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { rtdata, skillDomains } from '../../api/monitor'

const route = useRoute()
const activeTab = ref('calls')
const loading = ref(false)
const rows = ref([])
const columns = ref([])

// 老系统 queryid：301=工作台 302=IVR通道 303=座席状态 304=技能统计 305=话务监控 306=坐席整体
const QUERY_IDS = { calls: 305, agents: 303, skills: 304, ivr: 302, all: 306 }

async function load() {
  loading.value = true
  rows.value = []
  columns.value = []
  try {
    let data
    if (activeTab.value === 'skills') {
      // 技能统计优先用本地库 skill-domains（不依赖 CTI 实时服务）
      data = await skillDomains()
      rows.value = Array.isArray(data) ? data : []
    } else {
      const qid = QUERY_IDS[activeTab.value]
      const raw = await rtdata(qid)
      if (typeof raw === 'string') {
        try { data = JSON.parse(raw) } catch (e) { data = null }
      } else {
        data = raw
      }
      // 兼容 {aaData:[...]} 与裸数组
      if (data && typeof data === 'object' && !Array.isArray(data)) {
        rows.value = data.aaData || data.rows || []
      } else if (Array.isArray(data)) {
        rows.value = data
      }
    }
    if (rows.value.length > 0) {
      columns.value = Object.keys(rows.value[0]).filter(k => k !== 'DT_RowClass')
    }
  } catch (e) {
    rows.value = []
    columns.value = []
    ElMessage.warning('实时服务未连接（CTI 实时服务不可达），展示占位数据')
  } finally {
    loading.value = false
  }
}

function reload() {
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
