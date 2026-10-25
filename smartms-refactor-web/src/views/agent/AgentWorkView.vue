<template>
  <div class="agent-work">
    <!-- 顶栏 -->
    <header class="aw-header">
      <div class="aw-left">
        <div class="aw-logo">📞 呼叫中心 <span class="aw-logo-sub">坐席工作台</span></div>
        <el-tag v-if="phone.loggedIn" size="small" type="primary" class="aw-phone-tag">
          软电话 {{ phone.workNo }} / {{ phone.extenNo }}
        </el-tag>
      </div>
      <div class="aw-right">
        <div class="aw-identity">
          <el-tag size="small" type="success">{{ authStore.agentCode || '未设工号' }}</el-tag>
          <span class="aw-name">{{ authStore.userName }}</span>
        </div>
        <el-divider direction="vertical" class="aw-sep" />
        <el-button size="small" text type="primary" @click="goConsole">管理台</el-button>
        <el-button size="small" type="danger" plain @click="logout">退出登录</el-button>
      </div>
    </header>

    <div class="aw-body">
      <!-- 中间：来电弹屏 -->
      <main class="aw-main">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <b>来电弹屏</b>
              <div class="panel-tools">
                <el-input v-model="screenNumber" placeholder="输入号码查询客户" size="small" style="width: 200px" @keyup.enter="screenSearch" />
                <el-button type="primary" size="small" :icon="Search" @click="screenSearch">查询</el-button>
              </div>
            </div>
          </template>

          <div v-if="screenCustomer" class="cust-card">
            <div class="cust-title">
              <el-avatar :size="40">{{ (screenCustomer.customName || '?').charAt(0) }}</el-avatar>
              <div>
                <div class="cust-name">{{ screenCustomer.customName }} <el-tag size="small" :type="screenCustomer.customSex === '1' ? 'primary' : 'danger'" style="margin-left:6px">{{ screenCustomer.customSex === '1' ? '男' : '女' }}</el-tag></div>
                <div class="cust-mobile">{{ screenCustomer.customMobile || screenCustomer.customPhone || screenNumber }}</div>
              </div>
            </div>
            <el-descriptions :column="2" size="small" border style="margin-top:12px">
              <el-descriptions-item label="公司">{{ screenCustomer.companyName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="QQ">{{ screenCustomer.qqNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ screenCustomer.emailAddr || '-' }}</el-descriptions-item>
              <el-descriptions-item label="城市">{{ screenCustomer.customCity || '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ screenCustomer.customAddr || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ screenCustomer.customDesc || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div class="cust-actions">
              <el-button size="small" type="primary" @click="quickWorkTask">新建工单</el-button>
              <el-button size="small" @click="quickOrders">客户订单</el-button>
              <el-button size="small" @click="quickDetail">客户详情</el-button>
            </div>
          </div>
          <el-empty v-else description="来电自动弹屏（或输入号码查询客户）" :image-size="80" />
        </el-card>

        <el-card shadow="never" class="panel" style="margin-top:14px">
          <template #header><b>最近通话（我的通话）</b></template>
          <el-table :data="recentCalls" v-loading="recentLoading" stripe border size="small">
            <el-table-column prop="bizANI" label="主叫" width="110" />
            <el-table-column prop="bizDNIS" label="被叫" width="110" />
            <el-table-column prop="agentId" label="坐席" width="70" />
            <el-table-column prop="talkStartTime" label="接通时间" min-width="150" />
            <el-table-column prop="timeLength" label="时长(秒)" width="90" />
          </el-table>
        </el-card>
      </main>

      <!-- 右侧：最近访问 -->
      <aside class="aw-side">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <b>🕘 最近访问</b>
              <el-button size="small" text type="info" @click="clearRecent">清空</el-button>
            </div>
          </template>
          <div v-if="recent.length" class="recent-grid">
            <div v-for="it in recent" :key="it.path" class="recent-item" @click="go(it.path)">
              <span class="qi-icon">{{ it.icon }}</span>
              <span class="recent-name">{{ it.name }}</span>
              <span class="recent-group">{{ it.group }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无最近访问" :image-size="60" />
        </el-card>
        <el-card shadow="never" class="panel" style="margin-top:14px">
          <template #header><b>在线技能组</b></template>
          <div v-for="s in skillOptions" :key="s.skillId" class="skill-line">
            <span>{{ s.skillName }}</span>
            <el-tag size="small" type="info">{{ s.skillGroupType || '-' }}</el-tag>
          </div>
        </el-card>
      </aside>
    </div>

    <!-- 全局悬浮电话条（本页 + 所有业务页可见，状态共享不断） -->
    <PhoneBar />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { usePhoneStore } from '../../stores/phone'
import { ctiPage } from '../../api/config'
import { pageRecords } from '../../api/record'
import { dialSearch } from '../../api/customer'
import { getRecent, clearRecent } from '../../utils/menu'
import PhoneBar from '../../components/PhoneBar.vue'

const router = useRouter()
const authStore = useAuthStore()
const phone = usePhoneStore()

const skillOptions = ref([])
const screenNumber = ref('')
const screenCustomer = ref(null)
const recentCalls = ref([])
const recentLoading = ref(false)

// 最近访问（MainLayout 记录，此处展示）
const recent = ref(getRecent())

async function loadSkills() {
  try {
    const res = await ctiPage('skills', { page: 1, size: 100 })
    skillOptions.value = res.list || []
  } catch (e) { skillOptions.value = [] }
}

// ============ 弹屏 ============
async function screenSearch() {
  if (!screenNumber.value) { ElMessage.warning('请输入号码'); return }
  await screenSearchByNumber(screenNumber.value)
}

async function screenSearchByNumber(num) {
  try {
    const c = await dialSearch({ callNumber: num })
    screenCustomer.value = c || null
    if (!c) ElMessage.info('未找到该号码的客户')
  } catch (e) { screenCustomer.value = null }
}

function handleIncoming(ani) {
  if (ani) {
    screenNumber.value = ani
    screenSearchByNumber(ani)
    ElMessage.info(`来电: ${ani}`)
  }
}

function quickWorkTask() {
  if (!screenCustomer.value) { ElMessage.warning('请先查询客户'); return }
  go(`/worktask?tab=all&customName=${encodeURIComponent(screenCustomer.value.customName || '')}`)
}
function quickOrders() {
  if (!screenCustomer.value) { ElMessage.warning('请先查询客户'); return }
  go(`/order?tab=order&customName=${encodeURIComponent(screenCustomer.value.customName || '')}`)
}
function quickDetail() {
  if (!screenCustomer.value) { ElMessage.warning('请先查询客户'); return }
  go(`/crm/customer?customName=${encodeURIComponent(screenCustomer.value.customName || '')}`)
}

async function loadRecent() {
  recentLoading.value = true
  try {
    const res = await pageRecords({ page: 1, size: 5, agentId: 'my' })
    recentCalls.value = res.list || []
  } catch (e) { recentCalls.value = [] } finally { recentLoading.value = false }
}

function go(path) { router.push(path) }
function goConsole() { router.push('/') }
function logout() {
  phone.logout()
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadSkills()
  loadRecent()
  recent.value = getRecent()   // 从其他页面回来时刷新
  // 全局来电回调：软电话在任何页面收到来电都弹到这里
  phone.onIncoming(handleIncoming)
})
onBeforeUnmount(() => {
  phone.offIncoming(handleIncoming)
})
</script>

<style scoped>
.agent-work { height: 100%; display: flex; flex-direction: column; background: #f1f5f9; }
.aw-header {
  height: 56px; background: #0f172a; color: #fff; display: flex;
  align-items: center; justify-content: space-between; padding: 0 20px;
}
.aw-left {
  display: flex; align-items: center; gap: 14px; min-width: 0;
}
.aw-right {
  display: flex; align-items: center; gap: 12px;
}
.aw-logo { font-size: 17px; font-weight: 600; white-space: nowrap; }
.aw-logo-sub { font-size: 12px; color: #94a3b8; margin-left: 8px; font-weight: 400; }
.aw-phone-tag { flex-shrink: 0; }
.aw-identity {
  display: flex; align-items: center; gap: 8px;
  background: #1e293b; border-radius: 8px; padding: 4px 10px;
}
.aw-sep { border-color: #334155; height: 20px; }
.aw-name { font-size: 14px; color: #e2e8f0; }
.aw-body { flex: 1; display: flex; gap: 14px; padding: 14px; min-height: 0; }
.aw-main { flex: 1; min-width: 0; overflow-y: auto; }
.aw-side { width: 280px; overflow-y: auto; }
.panel { border-radius: 8px; }
.panel-sub { font-size: 11px; color: #94a3b8; font-weight: 400; margin-left: 6px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; }

.cust-card { padding: 4px 2px; }
.cust-title { display: flex; gap: 10px; align-items: center; }
.cust-name { font-size: 16px; font-weight: 600; color: #1e293b; }
.cust-mobile { font-size: 13px; color: #2563eb; margin-top: 2px; }
.cust-actions { margin-top: 12px; display: flex; gap: 8px; }

.recent-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.recent-item {
  border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px 6px; text-align: center;
  cursor: pointer; transition: all .15s; background: #f8fafc; display: flex; flex-direction: column; align-items: center; gap: 2px;
}
.recent-item:hover { border-color: #2563eb; color: #2563eb; transform: translateY(-1px); }
.qi-icon { display: block; font-size: 20px; }
.recent-name { font-size: 13px; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.recent-item:hover .recent-name { color: #2563eb; }
.recent-group { font-size: 11px; color: #94a3b8; }
.skill-line { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px dashed #f1f5f9; font-size: 13px; }
</style>
