<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.customName" placeholder="客户姓名" clearable style="width: 140px"
        @keyup.enter="loadData" @clear="loadData" />
      <el-input v-model="query.phoneNumber" placeholder="手机号" clearable style="width: 150px"
        @keyup.enter="loadData" @clear="loadData" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-radio-group v-model="deleted" @change="switchTab">
        <el-radio-button :value="false">客户列表</el-radio-button>
        <el-radio-button :value="true">回收站</el-radio-button>
      </el-radio-group>
      <div style="flex:1"></div>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增客户</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe border>
      <el-table-column prop="customName" label="姓名" min-width="90" />
      <el-table-column prop="customSex" label="性别" width="60">
        <template #default="{ row }">{{ sexText(row.customSex) }}</template>
      </el-table-column>
      <el-table-column prop="companyName" label="公司" min-width="130" />
      <el-table-column prop="customMobile" label="手机" min-width="120" />
      <el-table-column prop="customPhone" label="座机" min-width="110" />
      <el-table-column prop="qqNumber" label="QQ" width="100" />
      <el-table-column prop="customCity" label="城市" width="90" />
      <el-table-column prop="agentid" label="归属坐席" width="90" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="!deleted" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button v-if="deleted" size="small" type="success" @click="handleRestore(row)">恢复</el-button>
          <el-button v-if="!deleted" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="query.page" v-model:page-size="query.size"
      :page-sizes="[10, 15, 20, 50]" @current-change="loadData" @size-change="loadData" />

    <el-dialog v-model="dialogVisible" :title="form.customId ? '编辑客户' : '新增客户'" width="680px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="姓名" prop="customName"><el-input v-model="form.customName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别">
            <el-select v-model="form.customSex" style="width:100%">
              <el-option label="男" value="1" /><el-option label="女" value="2" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.customMobile" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="座机"><el-input v-model="form.customPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="公司"><el-input v-model="form.companyName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="QQ"><el-input v-model="form.qqNumber" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.emailAddr" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="城市"><el-input v-model="form.customCity" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="地址"><el-input v-model="form.customAddr" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.customDesc" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageCustomers, saveCustomer, deleteCustomer, restoreCustomer } from '../../../api/customer'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const deleted = ref(false)
const mine = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ page: 1, size: 15, customName: '', phoneNumber: '' })
const emptyForm = () => ({ customId: '', customName: '', customSex: '1', customMobile: '', customPhone: '',
  companyName: '', qqNumber: '', emailAddr: '', customCity: '', customAddr: '', customDesc: '' })
const form = reactive(emptyForm())
const formRules = { customName: [{ required: true, message: '请输入姓名', trigger: 'blur' }] }

function sexText(s) { return s === '1' ? '男' : s === '2' ? '女' : '-' }

async function loadData() {
  loading.value = true
  try {
    const params = { ...query, deleted: deleted.value }
    if (mine.value) params.agentId = 'my'
    const res = await pageCustomers(params)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

function switchTab() {
  query.page = 1
  loadData()
}

function openDialog(row) {
  Object.assign(form, emptyForm())
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await saveCustomer({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { } finally { saving.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定将客户「${row.customName}」移入回收站吗？`, '提示', { type: 'warning' })
  await deleteCustomer(row.customId)
  ElMessage.success('已移入回收站')
  loadData()
}

async function handleRestore(row) {
  await restoreCustomer(row.customId)
  ElMessage.success('已恢复')
  loadData()
}

onMounted(() => {
  if (route.query.mine === 'true') mine.value = true
  loadData()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
