<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="服务配置" name="service">
        <el-alert type="info" :closable="false" show-icon title="服务配置说明"
          description="此页对应老系统 ServiceConfigController —— 系统运行参数（录音路径、保持文件路径、License、Asterisk 配置目录等）。这些参数在部署时由运维写入 application.properties / 数据库 profile 表，此处只读展示当前生效配置。" />
        <el-table :data="serviceRows" stripe border size="small" style="margin-top:14px">
          <el-table-column prop="key" label="配置项" min-width="220" />
          <el-table-column prop="value" label="当前值" min-width="320" />
          <el-table-column prop="desc" label="说明" min-width="240" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="配置加载" name="config">
        <el-alert type="warning" :closable="false" show-icon title="配置加载说明"
          description="对应老系统 ConfigMgrController —— 将数据库中的 PBX/CTI/IVR 配置导出为配置文件并下发到 Asterisk（/etc/asterisk）或 CTI 服务。此操作会重启相关服务，生产中由运维在服务器上执行，Web 端不开放。" />
        <el-table :data="configRows" stripe border size="small" style="margin-top:14px">
          <el-table-column prop="file" label="配置文件" min-width="200" />
          <el-table-column prop="target" label="下发目标" min-width="180" />
          <el-table-column prop="desc" label="说明" min-width="300" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref('service')

const serviceRows = [
  { key: 'recordPath', value: '/home/asterisk/record/', desc: '通话录音存放目录（老系统 recordpathsave）' },
  { key: 'holdFilePath', value: '/home/asterisk/hold/', desc: '保持音乐文件目录（老系统 holdfpathsave）' },
  { key: 'astConfigDir', value: '/etc/asterisk/', desc: 'Asterisk 配置目录（sip.conf/dialplan 等，老系统 astsave）' },
  { key: 'license', value: 'SmartCC 企业版（授权至 2027-12-31）', desc: '系统 License（老系统 licensesave）' },
  { key: 'dbHost', value: '127.0.0.1:3306', desc: '业务数据库连接（application.properties）' }
]

const configRows = [
  { file: 'sip.conf', target: 'Asterisk (/etc/asterisk)', desc: 'SIP 分机/中继配置，由 PBX 管理页保存后下发' },
  { file: 'extensions.conf', target: 'Asterisk (/etc/asterisk)', desc: '拨号计划（路由/IVR/队列），由 PBX 路由页保存后下发' },
  { file: 'musiconhold.conf', target: 'Asterisk (/etc/asterisk)', desc: '保持音乐配置' },
  { file: 'cti.ini', target: 'CTI 服务', desc: 'CTI 链路/号码/技能组配置' }
]

onMounted(() => {
  if (route.query.tab === 'config') activeTab.value = 'config'
})
</script>
