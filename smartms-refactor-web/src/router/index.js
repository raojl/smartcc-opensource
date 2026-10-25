import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true }
  },
  {
    // 坐席工作台（软电话），独立布局，不套管理台框架
    path: '/agent-work',
    name: 'AgentWork',
    component: () => import('../views/agent/AgentWorkView.vue'),
    meta: { title: '坐席工作台' }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'system/user',
        name: 'UserManage',
        component: () => import('../views/system/user/UserManageView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('../views/system/role/RoleManageView.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'crm/customer',
        name: 'CustomerManage',
        component: () => import('../views/crm/customer/CustomerManageView.vue'),
        meta: { title: '客户管理' }
      },
      {
        path: 'record',
        name: 'Record',
        component: () => import('../views/record/RecordView.vue'),
        meta: { title: '交互查询' }
      },
      {
        path: 'worktask',
        name: 'WorkTask',
        component: () => import('../views/worktask/WorkTaskView.vue'),
        meta: { title: '服务记录' }
      },
      {
        path: 'quality',
        name: 'Quality',
        component: () => import('../views/quality/QualityView.vue'),
        meta: { title: '质检管理' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/order/OrderView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'outbound',
        name: 'Outbound',
        component: () => import('../views/outbound/OutboundView.vue'),
        meta: { title: '外呼任务' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('../views/report/ReportView.vue'),
        meta: { title: '统计分析' }
      },
      {
        path: 'pbx',
        name: 'Pbx',
        component: () => import('../views/pbx/PbxView.vue'),
        meta: { title: 'PBX 管理' }
      },
      {
        path: 'cti',
        name: 'Cti',
        component: () => import('../views/cti/CtiView.vue'),
        meta: { title: 'CTI 管理' }
      },
      {
        path: 'ivr',
        name: 'Ivr',
        component: () => import('../views/ivr/IvrView.vue'),
        meta: { title: 'IVR 管理' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('../views/monitor/MonitorView.vue'),
        meta: { title: '实时监控' }
      },
      {
        path: 'system/service-config',
        name: 'ServiceConfig',
        component: () => import('../views/system/SystemConfigView.vue'),
        meta: { title: '服务配置' }
      },
      {
        path: 'system/config-load',
        name: 'ConfigLoad',
        component: () => import('../views/system/SystemConfigView.vue'),
        meta: { title: '配置加载' }
      },
      {
        path: 'system/webrtc',
        name: 'WebRtcManage',
        component: () => import('../views/system/webrtc/WebRtcManageView.vue'),
        meta: { title: 'WebRTC 分机' }
      },
      {
        path: 'im',
        name: 'Message',
        component: () => import('../views/im/MessageView.vue'),
        meta: { title: '智能消息' }
      },
      { path: ':pathMatch(.*)*', component: () => import('../views/dashboard/DashboardView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    next()
  } else if (!auth.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
