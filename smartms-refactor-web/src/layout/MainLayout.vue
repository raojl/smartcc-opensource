<template>
  <el-container class="main-layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo" @click="goHome">
        <span class="logo-icon">📞</span>
        <span v-show="!collapsed" class="logo-text">呼叫中心</span>
      </div>

      <el-scrollbar class="menu-scroll">
        <el-menu :default-active="activeMenu" router background-color="#0f172a" :collapse="collapsed"
          :default-openeds="openeds" text-color="#cbd5e1" active-text-color="#60a5fa"
          class="side-menu" :collapse-transition="false">
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title><span>工作台</span></template>
          </el-menu-item>

          <!-- 正常模式：分组菜单 -->
          <el-sub-menu v-for="group in menuGroups" :key="group.menuId" :index="String(group.menuId)">
            <template #title>
              <span class="mi-icon">{{ groupIcon(group.menuName) }}</span>
              <span>{{ group.menuName }}</span>
            </template>
            <el-menu-item v-for="child in group.children" :key="child.menuId"
              :index="routeOf(child.menuUrl)">
              <span class="mi-icon">{{ itemIcon(child.menuName) }}</span>
              <template #title><span>{{ child.menuName }}</span></template>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button size="small" text :icon="collapsed ? Expand : Fold" class="collapse-btn" @click="toggleCollapse" />
          <!-- 面包屑：分组 / 当前菜单 -->
          <div class="breadcrumb">
            <template v-if="currentGroup">
              <span class="bc-group">{{ currentGroup }}</span>
              <span class="bc-sep">/</span>
              <span class="bc-item">{{ currentTitle }}</span>
            </template>
            <span v-else class="bc-item">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-button v-if="authStore.isAgent" size="small" type="primary" plain :icon="PhoneFilled" @click="goAgentWork">坐席工作台</el-button>
          <el-divider direction="vertical" class="header-sep" />
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" style="background:#2563eb">{{ avatarChar }}</el-avatar>
              <span class="user-name">{{ authStore.userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <!-- :key 按完整路径(含 query)重建组件 —— 修复同 path 不同 query 跳转时 tab 不切换
             （我的未接/我的通话、我的工单/我的受理 等 ?tab= 深链菜单） -->
        <router-view :key="route.fullPath" />
      </el-main>
    </el-container>

    <!-- 全局悬浮电话条（坐席角色：所有页面可见，跳转不断） -->
    <PhoneBar />
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { PhoneFilled, Fold, Expand, HomeFilled, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { usePhoneStore } from '../stores/phone'
import { routeOf, groupIcon, itemIcon, findMenuInfo, addRecent } from '../utils/menu'
import PhoneBar from '../components/PhoneBar.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const phoneStore = usePhoneStore()

const menuGroups = computed(() => authStore.menus)

// 路由变化即记录最近访问（侧栏/面包屑/快捷入口等一切入口，坐席工作台展示）
watch(() => route.fullPath, (p) => {
  addRecent(menuGroups.value, p)
})

// 侧栏折叠（记忆到 localStorage）
const collapsed = ref(localStorage.getItem('smartcc.sidebar.collapsed') === '1')
function toggleCollapse() {
  collapsed.value = !collapsed.value
  localStorage.setItem('smartcc.sidebar.collapsed', collapsed.value ? '1' : '0')
}

// 激活高亮：用完整路径(含 query)匹配菜单 index
const activeMenu = computed(() => route.fullPath)

// 当前菜单信息（面包屑）
const currentMenuInfo = computed(() => {
  const full = route.fullPath
  const info = findMenuInfo(menuGroups.value, full)
  if (info) return { group: info.group, name: info.name }
  if (full === '/dashboard') return { group: '', name: '工作台' }
  if (full === '/agent-work') return { group: '', name: '坐席工作台' }
  return { group: '', name: route.meta?.title || '工作台' }
})
const currentGroup = computed(() => currentMenuInfo.value.group)
const currentTitle = computed(() => currentMenuInfo.value.name)

// 分组展开记忆
const openeds = computed(() =>
  collapsed.value ? [] : menuGroups.value.map(g => String(g.menuId))
)

const avatarChar = computed(() => (authStore.userName || 'U').charAt(0))

function handleCommand(cmd) {
  if (cmd === 'logout') {
    phoneStore.logout()          // 释放软电话 CTI 会话
    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

function goAgentWork() {
  router.push('/agent-work')
}

function goHome() {
  router.push('/dashboard')
}

onMounted(async () => {
  if (authStore.menus.length === 0) {
    try {
      await authStore.fetchMenus()
    } catch (e) {
      // 拦截器已处理
    }
  }
  // 恢复软电话会话（刷新后不重复 Login，避免 CTI 203 工号已签入）
  phoneStore.restoreSession()
})
</script>

<style scoped>
.main-layout {
  height: 100%;
}
.aside {
  background: #0f172a;
  display: flex;
  flex-direction: column;
  transition: width .2s;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #0b1220;
  cursor: pointer;
  flex-shrink: 0;
}
.logo-icon {
  font-size: 22px;
}
.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}
.menu-scroll {
  flex: 1;
  min-height: 0;
}
.side-menu {
  border-right: none;
  min-height: 100%;
}
.mi-icon {
  font-size: 15px;
  margin-right: 4px;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  height: 60px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
}
.collapse-btn {
  color: #64748b;
}
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.bc-group {
  color: #94a3b8;
}
.bc-sep {
  color: #cbd5e1;
}
.bc-item {
  color: #1e293b;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-sep {
  height: 20px;
  border-color: #e2e8f0;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.user-name {
  color: #334155;
  font-size: 14px;
}
.main-content {
  background: #f1f5f9;
  padding: 16px;
}
</style>
