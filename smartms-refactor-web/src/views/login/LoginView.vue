<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>呼叫中心系统</h1>
        <p>坐席工作台 · 智能外呼 · 实时监控</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
            :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
      <div class="login-footer">
        <el-tag size="small" type="info">默认账号 admin / admin123</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    // 坐席角色(role_type=1)进入坐席工作台（软电话），管理角色进入管理台
    if (authStore.isAgent) {
      router.push('/agent-work')
    } else {
      router.push('/')
    }
  } catch (e) {
    // 错误提示由 http 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 55%, #2563eb 100%);
}
.login-card {
  width: 400px;
  padding: 40px 36px 28px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.35);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h1 {
  font-size: 24px;
  color: #1e3a8a;
  margin-bottom: 8px;
}
.login-header p {
  color: #94a3b8;
  font-size: 13px;
}
.login-btn {
  width: 100%;
  margin-top: 4px;
}
.login-footer {
  margin-top: 20px;
  text-align: center;
}
</style>
