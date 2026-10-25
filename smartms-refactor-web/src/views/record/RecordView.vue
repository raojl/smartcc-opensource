<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="switchTab">
      <el-tab-pane label="通话详单" name="record">
        <div class="toolbar">
          <el-input v-model="query.oriAni" placeholder="主叫号码" clearable style="width: 140px" />
          <el-input v-model="query.oriDnis" placeholder="被叫号码" clearable style="width: 140px" />
          <el-input v-model="query.agentId" placeholder="坐席工号" clearable style="width: 130px" />
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
            start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
          <el-button type="primary" :icon="Search" @click="loadRecords">查询</el-button>
        </div>
        <el-table :data="recordList" v-loading="loading" stripe border size="small">
          <el-table-column prop="talkStartTime" label="通话时间" min-width="150" />
          <el-table-column prop="bizANI" label="主叫" width="110" />
          <el-table-column prop="bizDNIS" label="被叫" width="110" />
          <el-table-column label="方向" width="70">
            <template #default="{ row }">{{ row.callDirection === '1' ? '呼入' : '呼出' }}</template>
          </el-table-column>
          <el-table-column prop="agentId" label="坐席" width="80" />
          <el-table-column prop="deviceDN" label="分机" width="80" />
          <el-table-column label="时长" width="80">
            <template #default="{ row }">{{ row.timeLength }}s</template>
          </el-table-column>
          <el-table-column prop="fileName" label="录音" min-width="160" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="未接记录" name="missed">
        <div class="toolbar">
          <el-select v-model="missType" style="width: 130px" @change="loadMissed">
            <el-option label="坐席未接" :value="1" />
            <el-option label="队列未接" :value="2" />
            <el-option label="客户未接" :value="3" />
            <el-option label="留言" :value="4" />
          </el-select>
          <el-input v-model="missQuery.customMobile" placeholder="客户号码" clearable style="width: 140px" />
          <el-button type="primary" :icon="Search" @click="loadMissed">查询</el-button>
        </div>
        <el-table :data="missedList" v-loading="loading" stripe border size="small">
          <el-table-column prop="startTime" label="来电时间" min-width="150" />
          <el-table-column prop="bizANI" label="主叫" width="110" />
          <el-table-column prop="bizDNIS" label="被叫" width="110" />
          <el-table-column prop="agentId" label="坐席" width="90" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="page" v-model:page-size="size"
      :page-sizes="[10, 15, 20, 50]" @current-change="reload" @size-change="reload" />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { pageRecords, pageMissedCalls } from '../../api/record'

const route = useRoute()
const activeTab = ref('record')
const mine = ref(false)
const loading = ref(false)
const recordList = ref([])
const missedList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const dateRange = ref([])
const missType = ref(1)

const query = reactive({ oriAni: '', oriDnis: '', agentId: '' })
const missQuery = reactive({ customMobile: '' })

async function loadRecords() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, ...query }
    if (mine.value) params.agentId = 'my'
    if (dateRange.value?.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await pageRecords(params)
    recordList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

async function loadMissed() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, type: missType.value, ...missQuery }
    if (mine.value) params.agentId = 'my'
    const res = await pageMissedCalls(params)
    missedList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

function switchTab() {
  page.value = 1
  activeTab.value === 'record' ? loadRecords() : loadMissed()
}

function reload() {
  activeTab.value === 'record' ? loadRecords() : loadMissed()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  if (route.query.mine === 'true') mine.value = true
  activeTab.value === 'record' ? loadRecords() : loadMissed()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
