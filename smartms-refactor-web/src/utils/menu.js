/**
 * 菜单映射 + 最近访问（MainLayout / AgentWorkView 共用）
 */

// 老系统菜单 url → 前端路由（子菜单用 ?tab=/&mine= 区分同页面不同视图）
const ROUTE_MAP = {
  // 账号管理
  '/usermanager': '/system/user',
  '/userrole': '/system/role',
  // PBX 管理
  '/sipExtenController': '/pbx?tab=device',
  '/TrunkController': '/pbx?tab=trunk',
  '/RouteController': '/pbx?tab=route',
  '/serviceconf': '/system/service-config',
  '/configdata': '/system/config-load',
  // CTI 管理
  '/ExtenController': '/cti?tab=extensions',
  '/recordController': '/cti?tab=records',
  '/skillController': '/cti?tab=skills',
  // IVR 管理
  '/filemanager': '/ivr?tab=files',
  '/ivrRouteController': '/ivr?tab=routes',
  // 实时监控
  '/callinfo': '/monitor?tab=calls',
  '/agentinfo': '/monitor?tab=agents',
  '/skillinfo': '/monitor?tab=skills',
  '/ivrchannelinfo': '/monitor?tab=ivr',
  '/allstatus': '/monitor?tab=all',
  // 服务记录
  '/worktaskinfo': '/worktask?tab=all',
  '/worktaskinfo/belongMe': '/worktask?tab=mine',
  '/worktaskinfo/myWorkDone': '/worktask?tab=done',
  // 交互查询
  '/record': '/record?tab=record',
  '/missCall': '/record?tab=missed',
  '/missCall/myRecord': '/record?tab=missed&mine=true',
  '/record/myCall': '/record?tab=record&mine=true',
  '/msgRecordNew': '/im?tab=records',
  // 客户管理
  '/custominfo': '/crm/customer',
  '/custominfo/myCustom': '/crm/customer?mine=true',
  // 订单管理
  '/orderinfo': '/order?tab=order',
  '/productsinfo': '/order?tab=product',
  '/orderinfo/myOrder': '/order?tab=order&mine=true',
  // 智能消息
  '/topicinfo': '/im?tab=topics',
  '/knowinfo': '/im?tab=replies',
  '/interfController': '/im?tab=interfaces',
  // 质检管理
  '/inspectinfo': '/quality?tab=calls',
  '/qualitytempinfo': '/quality?tab=template',
  '/qualityresult': '/quality?tab=results',
  '/qualityresult/myResult': '/quality?tab=results&mine=true',
  // 外呼任务
  '/callTaskController': '/outbound?tab=task',
  '/detailController': '/outbound?tab=detail',
  '/callTaskController/myTask': '/outbound?tab=task&mine=true',
  '/detailController/myTaskInfo': '/outbound?tab=detail&mine=true',
  // 统计分析
  '/report/talk': '/report?tab=inbound',
  '/report/satisfaction': '/report?tab=sat'
}

export function routeOf(url) {
  return ROUTE_MAP[url] || '/dashboard'
}

const GROUP_ICONS = {
  账号管理: '👤', PBX管理: '📡', 'CTI 管理': '🎧', 'IVR 管理': '📞', 实时监控: '📊',
  服务记录: '📋', 交互查询: '🔍', 客户管理: '👥', 订单管理: '🧾', 智能消息: '💬',
  质检管理: '✅', 外呼任务: '📢', 统计分析: '📈'
}

export function groupIcon(name) {
  return GROUP_ICONS[name] || '📁'
}

export function itemIcon(name) {
  if (name.startsWith('我的')) return '⭐'
  if (name.includes('配置') || name.includes('信息')) return '📄'
  if (name.includes('监控') || name.includes('统计') || name.includes('记录')) return '📊'
  if (name.includes('模板') || name.includes('成绩')) return '🏅'
  if (name.includes('路由') || name.includes('通道')) return '🛣️'
  return '🔹'
}

// 反查菜单信息（分组/名称/图标）
export function findMenuInfo(groups, path) {
  for (const g of groups) {
    for (const c of g.children || []) {
      if (routeOf(c.menuUrl) === path) {
        return { group: g.menuName, name: c.menuName, icon: itemIcon(c.menuName) }
      }
    }
  }
  return null
}

// ============ 最近访问（localStorage 记忆 5 条） ============
const RECENT_KEY = 'smartcc.recent'
const MAX = 5

export function getRecent() {
  try {
    return JSON.parse(localStorage.getItem(RECENT_KEY) || '[]')
  } catch (e) {
    return []
  }
}

export function addRecent(groups, path) {
  if (!path || path === '/dashboard' || path === '/login') return
  const info = findMenuInfo(groups, path)
  if (!info) return
  let list = getRecent().filter(x => x.path !== path)
  list.unshift({ path, ...info })
  if (list.length > MAX) list = list.slice(0, MAX)
  localStorage.setItem(RECENT_KEY, JSON.stringify(list))
}

export function clearRecent() {
  localStorage.removeItem(RECENT_KEY)
}
