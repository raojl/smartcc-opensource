<template>
  <el-card shadow="never">
    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="姓名搜索" clearable style="width: 180px"
        @keyup.enter="loadData" @clear="loadData" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增用户</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" stripe border>
      <el-table-column prop="userId" label="ID" width="70" />
      <el-table-column prop="userName" label="姓名" min-width="100" />
      <el-table-column prop="loginName" label="登录名" min-width="100" />
      <el-table-column prop="telephone" label="电话" min-width="110" />
      <el-table-column label="角色" min-width="140">
        <template #default="{ row }">
          <el-tag v-for="r in row.roles" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleReset(row)">重置密码</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="query.page" v-model:page-size="query.size"
      :page-sizes="[10, 15, 20, 50]" @current-change="loadData" @size-change="loadData" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.userId ? '编辑用户' : '新增用户'" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="姓名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="登录名" prop="loginName">
          <el-input v-model="form.loginName" placeholder="请输入登录名" :disabled="!!form.userId" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password
            :placeholder="form.userId ? '留空则不修改' : '留空默认与登录名相同'" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.telephone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="坐席工号">
          <el-input v-model="form.qualityAgCode" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roleList" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageUsers, createUser, updateUser, deleteUser, resetPassword, allRoles, getUser } from '../../../api/user'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const roleList = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({
  page: 1,
  size: 15,
  username: ''
})

const emptyForm = () => ({
  userId: null,
  userName: '',
  loginName: '',
  password: '',
  telephone: '',
  email: '',
  qualityAgCode: '',
  roleIds: []
})
const form = reactive(emptyForm())

const formRules = {
  userName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  loginName: [{ required: true, message: '请输入登录名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageUsers(query)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function openDialog(row) {
  Object.assign(form, emptyForm())
  if (row) {
    const detail = await getUser(row.userId)
    Object.assign(form, {
      userId: detail.userId,
      userName: detail.userName,
      loginName: detail.loginName,
      password: '',
      telephone: detail.telephone,
      email: detail.email,
      qualityAgCode: detail.qualityAgCode,
      roleIds: detail.roleIds || []
    })
  }
  if (roleList.value.length === 0) {
    roleList.value = await allRoles()
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.userId) {
      await updateUser(form.userId, { ...form })
      ElMessage.success('保存成功')
    } else {
      await createUser({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.userName}」吗？`, '提示', { type: 'warning' })
  await deleteUser(row.userId)
  ElMessage.success('删除成功')
  loadData()
}

async function handleReset(row) {
  await ElMessageBox.confirm(`确定重置「${row.userName}」的密码为登录名吗？`, '提示', { type: 'warning' })
  await resetPassword(row.userId)
  ElMessage.success('密码已重置为登录名')
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
