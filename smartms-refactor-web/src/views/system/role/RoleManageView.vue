<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.roleName" placeholder="角色名搜索" clearable style="width: 180px"
        @keyup.enter="loadData" @clear="loadData" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button type="success" :icon="Plus" @click="openDialog()">新增角色</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe border>
      <el-table-column prop="roleId" label="ID" width="70" />
      <el-table-column prop="roleName" label="角色名" min-width="120" />
      <el-table-column prop="description" label="描述" min-width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="query.page" v-model:page-size="query.size"
      :page-sizes="[10, 15, 20, 50]" @current-change="loadData" @size-change="loadData" />

    <!-- 新增/编辑弹窗（含菜单授权树） -->
    <el-dialog v-model="dialogVisible" :title="form.roleId ? '编辑角色' : '新增角色'" width="640px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="菜单授权">
          <el-tree ref="treeRef" :data="menuTree" show-checkbox node-key="menuId"
            :props="{ label: 'menuName', children: 'children' }" style="width:100%;max-height:300px;overflow:auto"
            default-expand-all />
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
import { onMounted, reactive, ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageRoles, createRole, updateRole, deleteRole, getRole, menuTree as fetchMenuTree } from '../../../api/role'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const treeRef = ref()
const menuTree = ref([])

const query = reactive({ page: 1, size: 15, roleName: '' })
const form = reactive({ roleId: null, roleName: '', description: '', menuIds: [] })

const formRules = {
  roleName: [{ required: true, message: '请输入角色名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageRoles(query)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function openDialog(row) {
  Object.assign(form, { roleId: null, roleName: '', description: '', menuIds: [] })
  if (menuTree.value.length === 0) {
    menuTree.value = await fetchMenuTree()
  }
  dialogVisible.value = true
  await nextTick()
  if (row) {
    const detail = await getRole(row.roleId)
    Object.assign(form, {
      roleId: detail.roleId,
      roleName: detail.roleName,
      description: detail.description
    })
    treeRef.value.setCheckedKeys(detail.menuIds || [])
  } else {
    treeRef.value.setCheckedKeys([])
  }
}

async function handleSave() {
  await formRef.value.validate()
  const checked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  saving.value = true
  try {
    const payload = { ...form, menuIds: [...checked, ...halfChecked] }
    if (form.roleId) {
      await updateRole(form.roleId, payload)
      ElMessage.success('保存成功')
    } else {
      await createRole(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  await deleteRole(row.roleId)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
