<template>
  <div class="webrtc-manage">
    <el-card shadow="never">
      <template #header>
        <div class="wm-head">
          <b>WebRTC 分机管理</b>
          <span class="wm-tip">混合方案：坐席 WebRTC 语音分机（浏览器直连 Asterisk WSS），与 CTI 控制并行</span>
          <el-button type="primary" size="small" @click="openCreate">+ 开通分机</el-button>
        </div>
      </template>

      <!-- 创建表单 -->
      <el-dialog v-model="createVisible" title="开通 WebRTC 分机" width="420px">
        <el-form label-width="80px" size="small">
          <el-form-item label="分机号" required>
            <el-input v-model="form.exten" placeholder="如 3111（2-8 位数字）" />
          </el-form-item>
          <el-form-item label="密码" required>
            <el-input v-model="form.password" placeholder="至少 6 位" show-password />
          </el-form-item>
          <el-form-item label="显示名">
            <el-input v-model="form.displayName" placeholder="如：张三（坐席）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button size="small" @click="createVisible = false">取消</el-button>
          <el-button size="small" type="primary" :loading="creating" @click="doCreate">开通</el-button>
        </template>
      </el-dialog>

      <!-- 列表 -->
      <el-table :data="extensions" v-loading="loading" size="small" border>
        <el-table-column prop="exten" label="分机号" width="120" />
        <el-table-column label="接入地址（WSS）">
          <template #default="{ row }">
            <code>{{ wssBase }}/ws</code>
            <el-button size="small" text type="primary" @click="copyWss(row)">复制</el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag size="small" type="success">已开通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="copyAccess(row)">接入信息</el-button>
            <el-button size="small" text type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && extensions.length === 0" description="暂无 WebRTC 分机，点击「+ 开通分机」创建" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createWebRtcExtension, deleteWebRtcExtension, getWebRtcConfig, listWebRtcExtensions } from '../../../api/webrtc'

const extensions = ref([])
const loading = ref(false)
const createVisible = ref(false)
const creating = ref(false)
const form = reactive({ exten: '', password: '', displayName: '' })
const wssBase = ref('wss://')

async function load() {
  loading.value = true
  try {
    extensions.value = (await listWebRtcExtensions()) || []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.exten = ''
  form.password = ''
  form.displayName = ''
  createVisible.value = true
}

async function doCreate() {
  if (!/^\d{2,8}$/.test(form.exten)) { ElMessage.warning('分机号须为 2-8 位数字'); return }
  if (!form.password || form.password.length < 6) { ElMessage.warning('密码至少 6 位'); return }
  creating.value = true
  try {
    const r = await createWebRtcExtension({ ...form })
    ElMessage.success(`分机 ${r.exten} 已开通（Asterisk 已 reload）`)
    createVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e?.message || '开通失败')
  } finally {
    creating.value = false
  }
}

async function doDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除分机 ${row.exten}？删除后该坐席将无法注册 WebRTC 语音。`, '删除确认', { type: 'warning' })
    await deleteWebRtcExtension(row.exten)
    ElMessage.success(`分机 ${row.exten} 已删除`)
    load()
  } catch (e) { /* 取消 */ }
}

async function copyAccess(row) {
  try {
    const cfg = await getWebRtcConfig(row.exten)
    wssBase.value = cfg.wssUri.replace(/\/ws$/, '')
    const text = `分机: ${row.exten}\nWSS: ${cfg.wssUri}\n服务器: ${cfg.server}\n显示名: ${cfg.displayName}`
    await navigator.clipboard.writeText(text)
    ElMessage.success('接入信息已复制')
  } catch (e) {
    ElMessage.error(e?.message || '获取配置失败')
  }
}

function copyWss(row) {
  navigator.clipboard.writeText(wssBase.value + '/ws')
  ElMessage.success('WSS 地址已复制')
}

onMounted(load)
</script>

<style scoped>
.webrtc-manage { padding: 12px; }
.wm-head { display: flex; align-items: center; gap: 12px; }
.wm-tip { color: #909399; font-size: 12px; flex: 1; }
code { background: #f6f8fa; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
</style>
