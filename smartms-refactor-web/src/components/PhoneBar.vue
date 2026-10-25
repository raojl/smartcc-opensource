<template>
  <!-- 全局悬浮电话条：坐席任意页面可见，状态/轮询由 store 全局持有，跳转不断 -->
  <div v-if="show" class="phone-bar" :class="{ collapsed: phone.collapsed }">
    <!-- 折叠态：状态胶囊 -->
    <div v-if="phone.collapsed" class="pb-collapsed" @click="phone.collapsed = false">
      <span class="pb-status-dot" :class="'dot-' + phone.connStatus"></span>
      <span class="pb-mini-status">{{ phone.statusText }}</span>
      <el-badge v-if="phone.connStatus === 3" is-dot type="danger" style="margin-left:6px" />
    </div>

    <!-- 展开态：完整面板 -->
    <div v-else class="pb-panel">
      <div class="pb-head">
        <b>📞 软电话</b>
        <span class="pb-head-right">
          <el-tag v-if="phone.loggedIn" size="small" type="success">{{ phone.workNo }} / {{ phone.extenNo }}</el-tag>
          <el-button size="small" text @click="phone.collapsed = true">收起</el-button>
        </span>
      </div>

      <!-- 未登入：登入表单 -->
      <div v-if="!phone.loggedIn" class="pb-login">
        <el-form size="small" label-width="52px">
          <el-form-item label="工号">
            <el-input v-model="loginForm.workNo" :placeholder="authStore.agentCode || '坐席工号'" />
          </el-form-item>
          <el-form-item label="分机">
            <el-input v-model="loginForm.extenNo" placeholder="话机分机号" />
          </el-form-item>
          <el-form-item label="技能组">
            <el-select v-model="loginForm.skills" multiple collapse-tags placeholder="选择技能组" style="width: 100%">
              <el-option v-for="s in skills" :key="s.skillId" :label="s.skillName" :value="s.skillName" />
            </el-select>
          </el-form-item>
          <el-button type="primary" size="small" style="width:100%" :loading="phone.loginLoading" @click="doLogin">登 入</el-button>
        </el-form>
        <el-alert v-if="phone.ctiError" type="error" :closable="false" show-icon :title="phone.ctiError" style="margin-top:8px" />
      </div>

      <!-- 已登入：状态 + 控制 -->
      <div v-else class="pb-body">
        <div class="pb-status" :class="'st-' + phone.connStatus">
          <span class="pb-status-icon">{{ phone.statusIcon }}</span>
          <span class="pb-status-text">{{ phone.statusText }}</span>
          <span class="pb-status-no">{{ phone.currentNumber || phone.extenNo }}</span>
        </div>

        <!-- WebRTC 语音状态（混合方案：语音直连 Asterisk，与 CTI 并行） -->
        <div class="pb-status" :class="'wr-' + webrtc.status">
          <span class="pb-status-icon">🎧</span>
          <span class="pb-status-text">{{ webrtc.statusText }}</span>
          <el-tooltip v-if="webrtc.errorMsg" :content="webrtc.errorMsg" placement="left">
            <span class="pb-status-no">⚠️</span>
          </el-tooltip>
        </div>

        <div class="pb-dialpad">
          <button v-for="k in dialKeys" :key="k" class="pb-key" @click="phone.pressKey(k)">{{ k }}</button>
        </div>
        <div class="pb-dial-row">
          <el-button type="primary" size="small" :disabled="!phone.dialNumber" @click="doDial">拨打</el-button>
          <el-button type="danger" size="small" :disabled="!phone.talking" @click="doHangup">挂断</el-button>
          <el-button size="small" :disabled="!phone.talking" @click="doHold">{{ phone.holding ? '恢复' : '保持' }}</el-button>
        </div>
        <div class="pb-dial-row">
          <el-button size="small" type="success" plain :disabled="!phone.loggedIn || phone.connStatus === 1" @click="phone.setIdle()">示闲</el-button>
          <el-button size="small" type="warning" plain :disabled="!phone.loggedIn || phone.connStatus === 2" @click="phone.setBusy()">示忙</el-button>
          <el-button size="small" type="info" plain @click="doLogout">登出</el-button>
        </div>
        <el-alert v-if="phone.ctiError" type="error" :closable="false" show-icon :title="phone.ctiError" style="margin-top:8px" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { usePhoneStore } from '../stores/phone'
import { useWebrtcPhoneStore } from '../stores/webrtcPhone'
import { ctiPage } from '../api/config'
import { dialSearch } from '../api/customer'

const router = useRouter()
const authStore = useAuthStore()
const phone = usePhoneStore()
const webrtc = useWebrtcPhoneStore()

// 登录用户可见软电话条（坐席登入走 CTI + WebRTC；管理员可查看 UI，登入需坐席工号/分机）
const show = computed(() => authStore.isLoggedIn)

const skills = ref([])
// 分机号自动带出坐席绑定 DN（qualityAgDN），无需手输
const loginForm = reactive({ workNo: authStore.agentCode || '', extenNo: authStore.user?.qualityAgDN || '', skills: [] })
const dialKeys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '0', '#']

async function loadSkills() {
  try {
    const res = await ctiPage('skills', { page: 1, size: 100 })
    skills.value = res.list || []
  } catch (e) { skills.value = [] }
}

async function doLogin() {
  if (!loginForm.workNo) { ElMessage.warning('请输入工号'); return }
  if (!loginForm.extenNo) { ElMessage.warning('请输入分机号'); return }
  if (loginForm.skills.length === 0) { ElMessage.warning('请至少选择一项技能组'); return }
  try {
    const r = await phone.login({
      workNo: loginForm.workNo,
      extenNo: loginForm.extenNo,
      skills: loginForm.skills,
      agentName: authStore.userName
    })
    if (r && r.takenOver) {
      ElMessage.warning(`工号 ${loginForm.workNo} 已在其他位置签入，本端已接管该会话`)
    } else if (r && r.inService) {
      // 工号处于服务冲突状态（实测可能无任何真实签入，多为工号/分机组合配置问题）→ 引导核对
      try {
        await ElMessageBox.confirm(
          `签入被拒绝：工号 ${loginForm.workNo} 已处于服务状态。\n\n请核对：\n① 该工号/分机组合是否在 CTI 侧配置正确\n② 原签入位置（如有）是否已登出\n③ 技能组是否为有效技能组`,
          '签入被拒绝',
          { confirmButtonText: '我已核对，重试', cancelButtonText: '取消', type: 'warning' }
        )
        return doLogin()
      } catch (e) { /* 用户取消 */ }
    } else {
      ElMessage.success(`坐席 ${loginForm.workNo} 已签入`)
    }
    // 混合方案：CTI 签入成功后，自动注册 WebRTC 语音（浏览器直连 Asterisk WSS）
    if (r && !r.inService) {
      const ok = await webrtc.register(loginForm.extenNo)
      if (!ok) ElMessage.warning(`WebRTC 语音注册失败：${webrtc.errorMsg || ''}`)
    }
  } catch (e) {
    ElMessage.error(e?.message || '登入失败')
  }
}

async function doDial() {
  try {
    await phone.dial()
    ElMessage.info('外呼指令已发送')
    // 混合方案：CTI 发外呼指令 + WebRTC 语音同时外呼（媒体走浏览器直连）
    const num = phone.dialNumber || loginForm.extenNo
    if (webrtc.registered && num) {
      webrtc.call(num)
    }
  } catch (e) { ElMessage.error(e?.message || '拨打失败') }
}
async function doHangup() {
  try {
    await phone.hangup()
    webrtc.hangup()
  } catch (e) { ElMessage.error(e?.message || '挂断失败') }
}
async function doHold() {
  try { await phone.holdTalk() } catch (e) { ElMessage.error(e?.message || '操作失败') }
}
async function doLogout() {
  // 混合方案：先注销 WebRTC 语音，再登出 CTI
  try { webrtc.unregister() } catch (e) {}
  phone.logout()
}

// ============ 来电全局通知（任何页面收到来电都弹，点击回工作台弹屏） ============
let lastIncomingAt = 0
async function handleIncoming(ani) {
  if (!ani) return
  // 防抖：同号码 5s 内只弹一次
  const now = Date.now()
  if (now - lastIncomingAt < 5000) return
  lastIncomingAt = now
  // 查客户名
  let custName = ''
  try {
    const c = await dialSearch({ callNumber: ani })
    custName = c?.customName || ''
  } catch (e) { /* 查不到也弹 */ }
  ElNotification({
    title: '📞 来电',
    message: `号码: ${ani}${custName ? ' · 客户: ' + custName : ''}`,
    type: 'warning',
    duration: 10000,
    onClick: () => router.push('/agent-work')
  })
}

onMounted(() => {
  loadSkills()
  phone.onIncoming(handleIncoming)
})
onBeforeUnmount(() => {
  phone.offIncoming(handleIncoming)
})
</script>

<style scoped>
.phone-bar {
  position: fixed;
  top: 72px;
  right: 20px;
  z-index: 3000;
  width: 320px;
}
.pb-collapsed {
  width: auto;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
  font-size: 13px;
  margin-left: auto;
}
.pb-collapsed:hover { transform: translateY(-1px); }
.pb-status-dot {
  width: 10px; height: 10px; border-radius: 50%; display: inline-block;
}
.dot-1, .dot-0, .dot-14 { background: #10b981; }
.dot-2 { background: #ef4444; }
.dot-3 { background: #f59e0b; animation: pb-blink 1s infinite; }
.dot-5, .dot-10 { background: #60a5fa; }
@keyframes pb-blink { 50% { opacity: 0.3; } }
.pb-mini-status { font-weight: 600; }

.pb-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
  padding: 14px;
}
.pb-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.pb-head-right { display: flex; align-items: center; gap: 6px; }
.pb-login :deep(.el-form-item) { margin-bottom: 10px; }
.pb-status {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px; border-radius: 8px; margin-bottom: 10px;
  background: #f8fafc; border: 1px solid #e2e8f0;
}
.pb-status.st-1 { border-color: #10b981; }
.pb-status.st-2 { border-color: #ef4444; }
.pb-status.st-3 { border-color: #f59e0b; }
.pb-status.st-5, .pb-status.st-10 { border-color: #2563eb; background: #eff6ff; }
/* WebRTC 语音状态配色 */
.pb-status.wr-registered, .pb-status.wr-talking { border-color: #7c3aed; background: #f5f3ff; }
.pb-status.wr-connecting, .pb-status.wr-calling { border-color: #f59e0b; }
.pb-status.wr-error { border-color: #ef4444; background: #fef2f2; }
.pb-status-icon { font-size: 18px; }
.pb-status-text { font-weight: 600; color: #1e293b; font-size: 13px; }
.pb-status-no { margin-left: auto; color: #64748b; font-size: 12px; }
.pb-dialpad { display: grid; grid-template-columns: repeat(3, 1fr); gap: 5px; margin-bottom: 10px; }
.pb-key {
  height: 34px; border: 1px solid #e2e8f0; border-radius: 6px; background: #fff;
  font-size: 14px; cursor: pointer; transition: all .15s;
}
.pb-key:hover { background: #2563eb; color: #fff; border-color: #2563eb; }
.pb-dial-row { display: flex; gap: 6px; margin-bottom: 6px; }
</style>
