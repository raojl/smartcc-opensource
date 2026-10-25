<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="switchTab">
      <!-- 呼入话务 -->
      <el-tab-pane label="呼入话务统计" name="inbound">
        <div class="toolbar">
          <el-date-picker v-model="inboundDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 160px" />
          <el-button type="primary" :icon="Search" @click="loadInbound">统计</el-button>
        </div>
        <EChart v-if="inboundChart.series?.[0]?.data?.length" :option="inboundChart" height="300px" />
        <el-table :data="inboundRows" v-loading="loading" stripe border size="small" style="margin-top:12px">
          <el-table-column prop="starttime_f" label="时段" width="100" />
          <el-table-column prop="c" label="呼入总量" width="90" />
          <el-table-column prop="s1" label="转人工" width="90" />
          <el-table-column prop="s2" label="IVR自助" width="90" />
          <el-table-column prop="s3" label="自助率" width="90" />
          <el-table-column prop="s4" label="进人工数" width="90" />
          <el-table-column prop="s5" label="人工应答" width="90" />
          <el-table-column prop="s6" label="IVR总时长" width="110" />
          <el-table-column prop="s7" label="IVR均时长" width="110" />
          <el-table-column prop="s8" label="排队均时长" width="110" />
          <el-table-column prop="s9" label="接通均时长" width="110" />
          <el-table-column prop="s11" label="20s接通率" width="90" />
          <el-table-column prop="s12" label="整体接通率" width="90" />
        </el-table>
      </el-tab-pane>

      <!-- 外呼话务 -->
      <el-tab-pane label="外呼话务统计" name="outbound">
        <div class="toolbar">
          <el-date-picker v-model="outboundDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 160px" />
          <el-button type="primary" :icon="Search" @click="loadOutbound">统计</el-button>
        </div>
        <EChart v-if="outboundChart.series?.[0]?.data?.length" :option="outboundChart" height="300px" />
        <el-table :data="outboundRows" v-loading="loading" stripe border size="small" style="margin-top:12px">
          <el-table-column prop="starttime_f" label="时段" width="100" />
          <el-table-column prop="c" label="外呼总量" width="90" />
          <el-table-column prop="s1" label="成功次数" width="90" />
          <el-table-column prop="s2" label="成功率" width="90" />
          <el-table-column prop="s3" label="平均时长" width="110" />
        </el-table>
      </el-tab-pane>

      <!-- 坐席KPI -->
      <el-tab-pane label="坐席KPI" name="kpi">
        <div class="toolbar">
          <el-date-picker v-model="kpiDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 160px" />
          <el-input v-model="kpiAgent" placeholder="坐席工号" clearable style="width: 120px" />
          <el-button type="primary" :icon="Search" @click="loadKpi">统计</el-button>
        </div>
        <EChart v-if="kpiChart.series?.[0]?.data?.length" :option="kpiChart" height="300px" />
        <el-table :data="kpiRows" v-loading="loading" stripe border size="small" style="margin-top:12px">
          <el-table-column prop="starttime_f" label="时段" width="100" />
          <el-table-column prop="agentid" label="坐席" width="80" />
          <el-table-column prop="agentname" label="姓名" width="90" />
          <el-table-column prop="s1" label="通话时长" width="100" />
          <el-table-column prop="s2" label="呼入振铃" width="90" />
          <el-table-column prop="s3" label="电话接起" width="90" />
          <el-table-column prop="s7" label="外呼量" width="80" />
          <el-table-column prop="s5" label="在线时长" width="100" />
          <el-table-column prop="s8" label="利用率" width="90" />
          <el-table-column prop="s9" label="20s接通率" width="90" />
        </el-table>
      </el-tab-pane>

      <!-- 满意度 -->
      <el-tab-pane label="满意度统计" name="sat">
        <div class="toolbar">
          <el-date-picker v-model="satRange" type="daterange" value-format="YYYY-MM-DD"
            start-placeholder="开始" end-placeholder="结束" style="width: 260px" />
          <el-button type="primary" :icon="Search" @click="loadSat">统计</el-button>
        </div>
        <EChart v-if="satChart.series?.[0]?.data?.length" :option="satChart" height="300px" />
        <el-table :data="satRows" v-loading="loading" stripe border size="small" style="margin-top:12px">
          <el-table-column prop="agentid" label="坐席" width="90" />
          <el-table-column prop="agentname" label="姓名" width="100" />
          <el-table-column prop="c1" label="呼入量" width="80" />
          <el-table-column prop="c2" label="呼出量" width="80" />
          <el-table-column prop="s1" label="参与评价" width="90" />
          <el-table-column prop="s2" label="参评率" width="90" />
          <el-table-column prop="s3" label="非常满意" width="90" />
          <el-table-column prop="s4" label="满意" width="80" />
          <el-table-column prop="s5" label="一般" width="80" />
          <el-table-column prop="s6" label="不满意" width="80" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { inboundTalk, outboundTalk, agentKpi, satisfaction } from '../../api/report'
import EChart from '../../components/EChart.vue'

const route = useRoute()
const activeTab = ref('inbound')
const loading = ref(false)

const inboundDate = ref('2023-12-13')
const outboundDate = ref('2023-12-13')
const kpiDate = ref('2023-12-13')
const kpiAgent = ref('')
const satRange = ref(['2023-12-01', '2023-12-31'])

const inboundRows = ref([])
const outboundRows = ref([])
const kpiRows = ref([])
const satRows = ref([])

const inboundChart = ref({})
const outboundChart = ref({})
const kpiChart = ref({})
const satChart = ref({})

const axis = (data, extra = {}) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 30, top: 30, bottom: 40 },
  xAxis: { type: 'category', data },
  yAxis: { type: 'value', minInterval: 1 },
  ...extra
})

function barData(name, data, color) {
  return [{ name, type: 'bar', barWidth: 18, itemStyle: { color, borderRadius: [3, 3, 0, 0] }, data }]
}

async function loadInbound() {
  loading.value = true
  try {
    inboundRows.value = await inboundTalk({ timeType: 'day', timeVal: inboundDate.value })
    const rows = inboundRows.value
    inboundChart.value = axis(rows.map(r => r.starttime_f), {
      legend: { data: ['呼入量', '转人工'], bottom: 0 },
      series: [
        { name: '呼入量', type: 'bar', barWidth: 18, itemStyle: { color: '#2563eb', borderRadius: [3, 3, 0, 0] }, data: rows.map(r => Number(r.c) || 0) },
        { name: '转人工', type: 'line', smooth: true, itemStyle: { color: '#d97706' }, data: rows.map(r => Number(r.s1) || 0) }
      ]
    })
  } finally { loading.value = false }
}

async function loadOutbound() {
  loading.value = true
  try {
    outboundRows.value = await outboundTalk({ timeType: 'day', timeVal: outboundDate.value })
    const rows = outboundRows.value
    outboundChart.value = axis(rows.map(r => r.starttime_f), {
      legend: { data: ['外呼量', '成功次数'], bottom: 0 },
      series: [
        { name: '外呼量', type: 'bar', barWidth: 18, itemStyle: { color: '#059669', borderRadius: [3, 3, 0, 0] }, data: rows.map(r => Number(r.c) || 0) },
        { name: '成功次数', type: 'line', smooth: true, itemStyle: { color: '#2563eb' }, data: rows.map(r => Number(r.s1) || 0) }
      ]
    })
  } finally { loading.value = false }
}

async function loadKpi() {
  loading.value = true
  try {
    kpiRows.value = await agentKpi({ timeType: 'day', timeVal: kpiDate.value, agentId: kpiAgent.value || undefined })
    const rows = kpiRows.value
    const names = rows.map(r => r.agentname || r.agentid)
    kpiChart.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 60, right: 60, top: 30, bottom: 30 },
      legend: { data: ['通话时长(s)', '外呼量', '利用率'], bottom: 0 },
      xAxis: { type: 'category', data: names },
      yAxis: [
        { type: 'value', name: '通话', minInterval: 1 },
        { type: 'value', name: '外呼', minInterval: 1 }
      ],
      series: [
        { name: '通话时长(s)', type: 'bar', data: rows.map(r => Number(r.s1) || 0), itemStyle: { color: '#2563eb', borderRadius: [3, 3, 0, 0] } },
        { name: '外呼量', type: 'bar', data: rows.map(r => Number(r.s7) || 0), itemStyle: { color: '#d97706' }, yAxisIndex: 1 },
        { name: '利用率', type: 'line', smooth: true, data: rows.map(r => parseFloat(String(r.s8).replace('%', '')) || 0), itemStyle: { color: '#dc2626' } }
      ]
    }
  } finally { loading.value = false }
}

async function loadSat() {
  loading.value = true
  try {
    satRows.value = await satisfaction({
      startTime: satRange.value?.[0] + ' 00:00:00',
      endTime: satRange.value?.[1] + ' 23:59:59'
    })
    const rows = satRows.value
    const items = rows
      .map(r => ({ name: r.agentname || r.agentid, rate: parseFloat(String(r.s2).replace('%', '')) || 0, s3: Number(r.s3) || 0, s4: Number(r.s4) || 0 }))
      .sort((a, b) => b.rate - a.rate)
      .slice(0, 10)
    satChart.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 70, right: 50, top: 30, bottom: 30 },
      legend: { data: ['参评率(%)', '非常满意', '满意'], bottom: 0 },
      xAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
      yAxis: { type: 'category', data: items.map(x => x.name).reverse() },
      series: [
        { name: '参评率(%)', type: 'bar', data: items.map(x => x.rate).reverse(), itemStyle: { color: '#059669', borderRadius: [0, 3, 3, 0] }, label: { show: true, position: 'right', formatter: '{c}%' } },
        { name: '非常满意', type: 'bar', data: items.map(x => x.s3).reverse(), itemStyle: { color: '#2563eb' } },
        { name: '满意', type: 'bar', data: items.map(x => x.s4).reverse(), itemStyle: { color: '#d97706' } }
      ]
    }
  } finally { loading.value = false }
}

function switchTab() {
  if (activeTab.value === 'inbound') loadInbound()
  else if (activeTab.value === 'outbound') loadOutbound()
  else if (activeTab.value === 'kpi') loadKpi()
  else loadSat()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  if (activeTab.value === 'inbound') loadInbound()
  else if (activeTab.value === 'outbound') loadOutbound()
  else if (activeTab.value === 'kpi') loadKpi()
  else loadSat()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
</style>
