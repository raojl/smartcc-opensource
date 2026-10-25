<template>
  <div class="dashboard">
    <!-- 欢迎条 -->
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <div>
          <h2>👋 欢迎回来，{{ authStore.userName }}</h2>
          <p>{{ authStore.isAgent ? `坐席工作台 · 工号 ${authStore.agentCode}` : '系统管理台 · 运营总览' }}</p>
        </div>
        <div class="welcome-actions">
          <el-date-picker v-model="range" type="daterange" size="small" value-format="YYYY-MM-DD"
            range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 260px" @change="loadAll" />
          <el-button v-if="authStore.isAgent" type="primary" size="small" @click="goAgentWork">进入坐席工作台</el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="c in statCards" :key="c.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: c.color }">{{ c.icon }}</div>
          <div class="stat-info">
            <div class="stat-value" :class="{ loading: statLoading }">{{ c.value }}</div>
            <div class="stat-title">{{ c.title }}</div>
          </div>
          <div class="stat-trend">{{ c.trend }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><b>话务趋势</b> <span class="chart-hint">呼入 / 呼出量</span></template>
          <EChart :option="trafficOption" height="330px" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><b>坐席满意度</b> <span class="chart-hint">满意率 TOP</span></template>
          <EChart :option="satOption" height="330px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 下方区 -->
    <el-row :gutter="16" class="bottom-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="panel-head">
              <b>待办工单</b>
              <el-button size="small" text type="primary" @click="go('/worktask?tab=all')">全部工单 →</el-button>
            </div>
          </template>
          <el-table :data="todoTickets" v-loading="todoLoading" stripe border size="small">
            <el-table-column prop="workId" label="工单号" min-width="160" />
            <el-table-column prop="customName" label="客户" width="110" />
            <el-table-column prop="bussinessDesc" label="业务描述" min-width="160" show-overflow-tooltip />
            <el-table-column prop="workAgent" label="受理坐席" width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.workStatus === '1' ? 'warning' : row.workStatus === '2' ? 'primary' : 'success'">
                  {{ row.workStatus === '1' ? '待处理' : row.workStatus === '2' ? '处理中' : '已办结' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><b>快捷入口</b></template>
          <div class="quick-grid">
            <div class="quick-item" @click="go('/monitor?tab=calls')"><span class="qi-icon">📊</span>话务监控</div>
            <div class="quick-item" @click="go('/record?tab=record')"><span class="qi-icon">📞</span>通话详单</div>
            <div class="quick-item" @click="go('/record?tab=missed')"><span class="qi-icon">🔕</span>未接记录</div>
            <div class="quick-item" @click="go('/crm/customer')"><span class="qi-icon">👥</span>客户管理</div>
            <div class="quick-item" @click="go('/order?tab=order')"><span class="qi-icon">🧾</span>订单管理</div>
            <div class="quick-item" @click="go('/quality?tab=results')"><span class="qi-icon">🏅</span>质检成绩</div>
            <div class="quick-item" @click="go('/outbound?tab=task')"><span class="qi-icon">📢</span>外呼任务</div>
            <div class="quick-item" @click="go('/report?tab=inbound')"><span class="qi-icon">📈</span>统计分析</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import EChart from '../../components/EChart.vue'
import { pageCustomers } from '../../api/customer'
import { pageWorkTasks } from '../../api/workTask'
import { inboundTalk, outboundTalk, satisfaction } from '../../api/report'

const router = useRouter()
const authStore = useAuthStore()

// 默认展示本地有数据的区间（生产环境自动换当月），可手动调整
const range = ref(['2023-12-07', '2023-12-13'])
const statLoading = ref(false)
const todoLoading = ref(false)
const todoTickets = ref([])

const totals = reactive({ customer: 0, todo: 0, incall: 0, answerRate: '0%' })

const statCards = computed(() => [
  { title: '客户总数', value: totals.customer, icon: '👥', color: '#2563eb', trend: 'CRM 客户库' },
  { title: '待办工单', value: totals.todo, icon: '📋', color: '#d97706', trend: '待处理' },
  { title: '区间呼入', value: totals.incall, icon: '📞', color: '#059669', trend: '通话量' },
  { title: '接通率', value: totals.answerRate, icon: '✅', color: '#dc2626', trend: '呼入接通' }
])

const trafficOption = ref({})
const satOption = ref({})

function fmt(v) { return v == null ? 0 : Number(v) }

async function loadStats() {
  statLoading.value = true
  try {
    // 客户总数
    const c = await pageCustomers({ page: 1, size: 1 })
    totals.customer = c.total || 0
    // 待办工单（待处理）
    const t = await pageWorkTasks({ page: 1, size: 5, workStatus: '1' })
    totals.todo = t.total || 0
    todoTickets.value = t.list || []
    // 话务
    const [s, e] = range.value
    const [inc, out] = await Promise.all([
      inboundTalk({ timeType: 'range', timeStartVal: s, timeEndVal: e }),
      outboundTalk({ timeType: 'range', timeStartVal: s, timeEndVal: e })
    ])
    const incArr = Array.isArray(inc) ? inc : []
    const outArr = Array.isArray(out) ? out : []
    const incTotal = incArr.reduce((a, r) => a + fmt(r.c), 0)
    const incAns = incArr.reduce((a, r) => a + fmt(r.s1), 0)
    totals.incall = incTotal
    totals.answerRate = incTotal > 0 ? Math.round(incAns / incTotal * 100) + '%' : '0%'

    // 话务趋势（按 starttime_f 合并日粒度，取前 12 个时间段）
    const incMap = {}, outMap = {}
    incArr.forEach(r => { incMap[r.starttime_f] = (incMap[r.starttime_f] || 0) + fmt(r.c) })
    outArr.forEach(r => { outMap[r.starttime_f] = (outMap[r.starttime_f] || 0) + fmt(r.c) })
    const hours = [...new Set([...Object.keys(incMap), ...Object.keys(outMap)])].sort()
    trafficOption.value = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['呼入', '呼出'], bottom: 0 },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: { type: 'category', data: hours },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: '呼入', type: 'bar', barWidth: 18, itemStyle: { color: '#2563eb', borderRadius: [3, 3, 0, 0] }, data: hours.map(h => incMap[h] || 0) },
        { name: '呼出', type: 'line', smooth: true, itemStyle: { color: '#059669' }, data: hours.map(h => outMap[h] || 0) }
      ]
    }
  } finally { statLoading.value = false }
}

async function loadSat() {
  try {
    const [s, e] = range.value
    const rows = await satisfaction({ startTime: s, endTime: e })
    const arr = Array.isArray(rows) ? rows : []
    // 取满意率>0 的坐席，按满意率降序 TOP8
    const items = arr
      .map(r => ({ name: r.agentname || r.agentid, rate: parseFloat(String(r.s2).replace('%', '')) || 0, total: fmt(r.c1) }))
      .filter(x => x.rate > 0)
      .sort((a, b) => b.rate - a.rate)
      .slice(0, 8)
    satOption.value = {
      tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>满意率: ${p[0].value}%` },
      grid: { left: 60, right: 30, top: 20, bottom: 30 },
      xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      yAxis: { type: 'category', data: items.map(x => x.name).reverse() },
      series: [{
        type: 'bar', data: items.map(x => x.rate).reverse(),
        itemStyle: { color: '#059669', borderRadius: [0, 3, 3, 0] },
        label: { show: true, position: 'right', formatter: '{c}%' },
        barWidth: 16
      }]
    }
  } catch (e) { satOption.value = {} }
}

async function loadAll() { loadStats(); loadSat() }

function go(path) { router.push(path) }
function goAgentWork() { router.push('/agent-work') }

onMounted(async () => {
  if (!authStore.user) {
    try { await authStore.fetchMe() } catch (e) { }
  }
  loadAll()
})
</script>

<style scoped>
.welcome-card { margin-bottom: 16px; }
.welcome { display: flex; align-items: center; justify-content: space-between; }
.welcome h2 { color: #1e293b; margin-bottom: 6px; font-size: 19px; }
.welcome p { color: #64748b; font-size: 13px; margin: 0; }
.welcome-actions { display: flex; gap: 10px; align-items: center; }
.stat-row { margin-bottom: 16px; }
.stat-card { position: relative; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 26px; color: #fff; flex-shrink: 0; }
.stat-value { font-size: 26px; font-weight: 700; color: #1e293b; line-height: 1.1; }
.stat-value.loading { opacity: .4; }
.stat-title { font-size: 13px; color: #64748b; margin-top: 4px; }
.stat-trend { position: absolute; top: 12px; right: 14px; font-size: 11px; color: #94a3b8; }
.chart-hint { font-size: 12px; color: #94a3b8; font-weight: 400; margin-left: 8px; }
.bottom-row { margin-top: 16px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.quick-item { border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 4px; text-align: center; cursor: pointer; transition: all .15s; background: #f8fafc; }
.quick-item:hover { border-color: #2563eb; color: #2563eb; transform: translateY(-1px); }
.qi-icon { display: block; font-size: 18px; margin-bottom: 3px; }
</style>
