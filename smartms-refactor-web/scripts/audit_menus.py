#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""全菜单逐项移植审计：43 叶子菜单 → 路由映射 → 页面编译 → API 调用+数据量"""
import json, re, sys, urllib.request, urllib.error

BASE = 'http://127.0.0.1:8080'

def login(u, p):
    r = urllib.request.Request(BASE + '/api/auth/login', data=json.dumps({'username': u, 'password': p}).encode(),
                               headers={'Content-Type': 'application/json'})
    return json.load(urllib.request.urlopen(r, timeout=8))['token']

import os
# 账号密码从环境变量读取，避免硬编码：AUDIT_ADMIN_PWD / AUDIT_AGENT_PWD
ADMIN = login('admin', os.environ.get('AUDIT_ADMIN_PWD', ''))
AGENT = login('3004', os.environ.get('AUDIT_AGENT_PWD', ''))

def call(path, token=ADMIN, timeout=12):
    r = urllib.request.Request(BASE + path, headers={'Authorization': 'Bearer ' + token})
    try:
        resp = urllib.request.urlopen(r, timeout=timeout)
        body = resp.read().decode(errors='ignore')
        return resp.status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode(errors='ignore')[:120]
    except Exception as e:
        return 0, str(e)[:80]

def pages():
    return ['users', 'roles', 'pbx-devices', 'pbx-trunks', 'pbx-routes', 'cti-links', 'cti-extensions',
            'cti-profiles', 'cti-records', 'cti-skills', 'ivr-services', 'ivr-trunk-groups', 'ivr-files',
            'ivr-params', 'ivr-routes', 'work-tasks', 'records', 'missed', 'customers', 'orders', 'products',
            'quality-templates', 'quality-calls', 'quality-results', 'outbound-tasks', 'outbound-details',
            'report-inbound', 'report-outbound', 'report-kpi', 'report-sat', 'report-worktask', 'im-topics',
            'im-replies', 'im-records', 'im-interfaces', 'monitor-skill-domains', 'dial-search', 'me', 'menu-tree']

def fmt(code, body=''):
    if code == 200:
        try:
            d = json.loads(body)
            if isinstance(d, dict) and 'total' in d:
                return f'200 数据 {d["total"]} 条'
            if isinstance(d, list):
                return f'200 {len(d)} 条'
            return '200 ✓'
        except Exception:
            return '200 ✓'
    return f'{code} ✗'

# 页面组件文件（路由目标 → 组件路径）
COMP = {
    '/system/user': 'src/views/system/user/UserManageView.vue',
    '/system/role': 'src/views/system/role/RoleManageView.vue',
    '/pbx': 'src/views/pbx/PbxView.vue',
    '/system/service-config': 'src/views/system/SystemConfigView.vue',
    '/system/config-load': 'src/views/system/SystemConfigView.vue',
    '/cti': 'src/views/cti/CtiView.vue',
    '/ivr': 'src/views/ivr/IvrView.vue',
    '/monitor': 'src/views/monitor/MonitorView.vue',
    '/worktask': 'src/views/worktask/WorkTaskView.vue',
    '/record': 'src/views/record/RecordView.vue',
    '/im': 'src/views/im/MessageView.vue',
    '/crm/customer': 'src/views/crm/customer/CustomerManageView.vue',
    '/order': 'src/views/order/OrderView.vue',
    '/quality': 'src/views/quality/QualityView.vue',
    '/outbound': 'src/views/outbound/OutboundView.vue',
    '/report': 'src/views/report/ReportView.vue',
}

# 43 菜单审计表：(分组, 菜单名, 老URL, 目标路由, API检查(参数, token))
MENUS = [
    ('账号管理', '用户管理', '/usermanager', '/system/user', '/api/users?page=1&size=1', ADMIN),
    ('账号管理', '角色管理', '/userrole', '/system/role', '/api/roles?page=1&size=1', ADMIN),
    ('PBX管理', 'SIP分机', '/sipExtenController', '/pbx?tab=device', '/api/pbx/devices?page=1&size=1', ADMIN),
    ('PBX管理', 'PBX出局', '/TrunkController', '/pbx?tab=trunk', '/api/pbx/trunks?page=1&size=1', ADMIN),
    ('PBX管理', 'PBX路由', '/RouteController', '/pbx?tab=route', '/api/pbx/routes?page=1&size=1', ADMIN),
    ('PBX管理', '服务配置', '/serviceconf', '/system/service-config', '静态', ADMIN),
    ('PBX管理', '配置加载', '/configdata', '/system/config-load', '静态', ADMIN),
    ('CTI 管理', '号码配置', '/ExtenController', '/cti?tab=extensions', '/api/cti/extensions?page=1&size=1', ADMIN),
    ('CTI 管理', '录音配置', '/recordController', '/cti?tab=records', '/api/cti/records?page=1&size=1', ADMIN),
    ('CTI 管理', '技能组配置', '/skillController', '/cti?tab=skills', '/api/cti/skills?page=1&size=1', ADMIN),
    ('IVR 管理', '文件管理', '/filemanager', '/ivr?tab=files', '/api/ivr/files?page=1&size=1', ADMIN),
    ('IVR 管理', 'IVR路由', '/ivrRouteController', '/ivr?tab=routes', '/api/ivr/routes?page=1&size=1', ADMIN),
    ('实时监控', '话务监控', '/callinfo', '/monitor?tab=calls', '/api/monitor/rtdata-query?queryId=305', ADMIN),
    ('实时监控', '座席状态', '/agentinfo', '/monitor?tab=agents', '/api/monitor/rtdata-query?queryId=303', ADMIN),
    ('实时监控', '技能统计', '/skillinfo', '/monitor?tab=skills', '/api/monitor/skill-domains', ADMIN),
    ('实时监控', 'IVR通道', '/ivrchannelinfo', '/monitor?tab=ivr', '/api/monitor/rtdata-query?queryId=302', ADMIN),
    ('实时监控', '坐席整体', '/allstatus', '/monitor?tab=all', '/api/monitor/rtdata-query?queryId=306', ADMIN),
    ('服务记录', '工单信息', '/worktaskinfo', '/worktask?tab=all', '/api/work-tasks?page=1&size=1&startTime=2023-01-01&endTime=2023-12-31', ADMIN),
    ('服务记录', '我的工单', '/worktaskinfo/belongMe', '/worktask?tab=mine', '/api/work-tasks?belongAgent=my&page=1&size=1&startTime=2023-01-01&endTime=2023-12-31', AGENT),
    ('服务记录', '我的受理', '/worktaskinfo/myWorkDone', '/worktask?tab=done', '/api/work-tasks?workAgent=my&page=1&size=1&startTime=2023-01-01&endTime=2023-12-31', AGENT),
    ('交互查询', '通话详单', '/record', '/record?tab=record', '/api/records?page=1&size=1&startTime=2023-12-10&endTime=2023-12-15', ADMIN),
    ('交互查询', '未接记录', '/missCall', '/record?tab=missed', '/api/records/missed?type=1&page=1&size=1&startTime=2023-12-01&endTime=2023-12-31', ADMIN),
    ('交互查询', '我的通话', '/record/myCall', '/record?tab=record&mine=true', '/api/records?agentId=my&page=1&size=1&startTime=2023-12-10&endTime=2023-12-15', AGENT),
    ('交互查询', '我的未接', '/missCall/myRecord', '/record?tab=missed&mine=true', '/api/records/missed?agentId=my&type=1&page=1&size=1&startTime=2023-12-01&endTime=2023-12-31', AGENT),
    ('交互查询', '消息记录', '/msgRecordNew', '/im?tab=records', '/api/im/records?limit=10', ADMIN),
    ('客户管理', '客户信息', '/custominfo', '/crm/customer', '/api/customers?page=1&size=1', ADMIN),
    ('客户管理', '我的客户', '/custominfo/myCustom', '/crm/customer?mine=true', '/api/customers?agentId=my&page=1&size=1', AGENT),
    ('订单管理', '订单信息', '/orderinfo', '/order?tab=order', '/api/orders?page=1&size=1&startTime=20231001000000&endTime=20231231235959', ADMIN),
    ('订单管理', '产品信息', '/productsinfo', '/order?tab=product', '/api/orders/products?page=1&size=1', ADMIN),
    ('订单管理', '我的订单', '/orderinfo/myOrder', '/order?tab=order&mine=true', '/api/orders?agentId=my&page=1&size=1&startTime=20231001000000&endTime=20231231235959', AGENT),
    ('智能消息', '精准回复', '/topicinfo', '/im?tab=topics', '/api/im/topics', ADMIN),
    ('智能消息', '自动回复', '/knowinfo', '/im?tab=replies', '/api/im/replies', ADMIN),
    ('智能消息', '接入信息', '/interfController', '/im?tab=interfaces', '/api/im/interfaces', ADMIN),
    ('质检管理', '质检', '/inspectinfo', '/quality?tab=calls', '/api/quality/calls?startTime=2023-12-10&endTime=2023-12-15', ADMIN),
    ('质检管理', '质检模板', '/qualitytempinfo', '/quality?tab=template', '/api/quality/templates?page=1&size=1', ADMIN),
    ('质检管理', '质检成绩', '/qualityresult', '/quality?tab=results', '/api/quality/results?page=1&size=1', ADMIN),
    ('质检管理', '我的成绩', '/qualityresult/myResult', '/quality?tab=results&mine=true', '/api/quality/results?agentId=my&page=1&size=1', AGENT),
    ('外呼任务', '任务信息', '/callTaskController', '/outbound?tab=task', '/api/outbound/tasks?page=1&size=1', ADMIN),
    ('外呼任务', '任务详单', '/detailController', '/outbound?tab=detail', '/api/outbound/details?page=1&size=1', ADMIN),
    ('外呼任务', '我的任务', '/callTaskController/myTask', '/outbound?tab=task&mine=true', '/api/outbound/tasks?agentId=my&page=1&size=1', AGENT),
    ('外呼任务', '我的详单', '/detailController/myTaskInfo', '/outbound?tab=detail&mine=true', '/api/outbound/details?agentId=my&page=1&size=1', AGENT),
    ('统计分析', '话务统计', '/report/talk', '/report?tab=inbound', '/api/report/talk/inbound?timeType=day&timeVal=2023-12-13', ADMIN),
    ('统计分析', '满意度统计', '/report/satisfaction', '/report?tab=sat', '/api/report/satisfaction?startTime=2023-12-01&endTime=2023-12-31', ADMIN),
]

import urllib.parse
def check_page(comp_path):
    url = 'http://localhost:3000/' + comp_path
    r = urllib.request.Request(url)
    try:
        resp = urllib.request.urlopen(r, timeout=6)
        return resp.status
    except urllib.error.HTTPError as e:
        return e.code
    except Exception:
        return 0

print(f'{"分组":<8} {"菜单":<8} {"老URL":<26} {"目标路由":<30} {"页面编译":<6} {"API 检查"}')
print('-' * 110)
fails = []
for grp, name, old, target, api, token in MENUS:
    comp = COMP.get(target.split('?')[0], '?')
    pcode = check_page(comp) if comp != '?' else 200
    if api == '静态':
        ares = '静态展示 ✓'
    else:
        code, body = call(api, token)
        ares = fmt(code, body)
    pmark = '✓' if pcode == 200 else f'✗{pcode}'
    if pcode != 200 or ares.startswith(('4', '5', '0')):
        fails.append((grp, name, target, pcode, ares))
    print(f'{grp:<8} {name:<8} {old:<26} {target:<30} {pmark:<6} {ares}')

print('-' * 110)
print(f'共 {len(MENUS)} 项 | 异常 {len(fails)} 项')
for f in fails:
    print('  ❌', f)
