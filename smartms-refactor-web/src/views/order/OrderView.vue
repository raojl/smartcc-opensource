<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="switchTab">
      <!-- 订单 -->
      <el-tab-pane label="订单信息" name="order">
        <div class="toolbar">
          <el-input v-model="query.no" placeholder="订单号" clearable style="width: 170px" />
          <el-input v-model="query.name" placeholder="客户名" clearable style="width: 120px" />
          <el-button type="primary" :icon="Search" @click="loadOrders">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openOrderDialog()">新增订单</el-button>
        </div>
        <el-table :data="orderList" v-loading="loading" stripe border size="small">
          <el-table-column prop="no" label="订单号" min-width="170" />
          <el-table-column prop="name" label="客户" min-width="90" />
          <el-table-column prop="product" label="产品" min-width="110" />
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column prop="amount" label="金额" width="90" />
          <el-table-column prop="agentid" label="坐席" width="80" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">{{ statusText(row.status) }}</template>
          </el-table-column>
          <el-table-column prop="createtime" label="创建时间" min-width="150" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openOrderDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleOrderDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 产品 -->
      <el-tab-pane label="产品信息" name="product">
        <div class="toolbar">
          <el-input v-model="prodQuery.productName" placeholder="产品名" clearable style="width: 150px" />
          <el-button type="primary" :icon="Search" @click="loadProducts">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openProdDialog()">新增产品</el-button>
        </div>
        <el-table :data="prodList" v-loading="loading" stripe border size="small">
          <el-table-column prop="productId" label="产品编号" min-width="120" />
          <el-table-column prop="productName" label="产品名称" min-width="150" />
          <el-table-column prop="productUnitPrice" label="单价" width="100" />
          <el-table-column prop="productType" label="类型" width="90" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openProdDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleProdDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination class="pager" background layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="page" v-model:page-size="size"
      :page-sizes="[10, 15, 20, 50]" @current-change="reload" @size-change="reload" />

    <!-- 订单弹窗 -->
    <el-dialog v-model="orderVisible" :title="orderForm.id ? '编辑订单' : '新增订单'" width="640px">
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="90px">
        <el-form-item label="订单号" prop="no"><el-input v-model="orderForm.no" /></el-form-item>
        <el-form-item label="客户名"><el-input v-model="orderForm.name" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="产品">
            <el-input v-model="orderForm.product" placeholder="产品编号或名称" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数量"><el-input-number v-model="orderForm.quantity" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="金额"><el-input-number v-model="orderForm.amount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="orderForm.status" style="width:100%">
              <el-option label="待处理" value="1" /><el-option label="处理中" value="2" /><el-option label="已完成" value="3" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="坐席工号"><el-input v-model="orderForm.agentid" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="orderForm.desc" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveOrderItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 产品弹窗 -->
    <el-dialog v-model="prodVisible" :title="prodForm.id ? '编辑产品' : '新增产品'" width="480px">
      <el-form ref="prodFormRef" :model="prodForm" :rules="prodRules" label-width="90px">
        <el-form-item label="产品编号" prop="productId"><el-input v-model="prodForm.productId" /></el-form-item>
        <el-form-item label="产品名称" prop="productName"><el-input v-model="prodForm.productName" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="prodForm.productUnitPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="prodForm.productType" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="prodVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProdItem">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageOrders, saveOrder, deleteOrder, pageProducts, saveProduct, deleteProduct } from '../../api/order'

const route = useRoute()
const activeTab = ref('order')
const mine = ref(false)
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(15)

const orderList = ref([])
const prodList = ref([])
const orderVisible = ref(false)
const prodVisible = ref(false)
const orderFormRef = ref()
const prodFormRef = ref()

const query = reactive({ no: '', name: '' })
const prodQuery = reactive({ productName: '' })
const orderForm = reactive({ id: null, no: '', name: '', product: '', quantity: 1, amount: 0, status: '1', agentid: '', desc: '' })
const prodForm = reactive({ id: null, productId: '', productName: '', productUnitPrice: 0, productType: '' })
const orderRules = { no: [{ required: true, message: '请输入订单号', trigger: 'blur' }] }
const prodRules = {
  productId: [{ required: true, message: '请输入产品编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }]
}

function statusText(s) { return { 1: '待处理', 2: '处理中', 3: '已完成' }[s] || s }

async function loadOrders() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, ...query }
    if (mine.value) params.agentId = 'my'
    const res = await pageOrders(params)
    orderList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await pageProducts({ page: page.value, size: size.value, ...prodQuery })
    prodList.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

function switchTab() {
  page.value = 1
  activeTab.value === 'order' ? loadOrders() : loadProducts()
}

function reload() {
  activeTab.value === 'order' ? loadOrders() : loadProducts()
}

function openOrderDialog(row) {
  Object.assign(orderForm, { id: null, no: '', name: '', product: '', quantity: 1, amount: 0, status: '1', agentid: '', desc: '' })
  if (row) Object.assign(orderForm, row)
  orderVisible.value = true
}

async function saveOrderItem() {
  await orderFormRef.value.validate()
  saving.value = true
  try {
    await saveOrder({ ...orderForm })
    ElMessage.success('保存成功')
    orderVisible.value = false
    loadOrders()
  } catch (e) { } finally { saving.value = false }
}

async function handleOrderDelete(row) {
  await ElMessageBox.confirm(`确定删除订单「${row.no}」吗？`, '提示', { type: 'warning' })
  await deleteOrder(row.id)
  ElMessage.success('删除成功')
  loadOrders()
}

function openProdDialog(row) {
  Object.assign(prodForm, { id: null, productId: '', productName: '', productUnitPrice: 0, productType: '' })
  if (row) Object.assign(prodForm, row)
  prodVisible.value = true
}

async function saveProdItem() {
  await prodFormRef.value.validate()
  saving.value = true
  try {
    await saveProduct({ ...prodForm })
    ElMessage.success('保存成功')
    prodVisible.value = false
    loadProducts()
  } catch (e) { } finally { saving.value = false }
}

async function handleProdDelete(row) {
  await ElMessageBox.confirm(`确定删除产品「${row.productName}」吗？`, '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  loadProducts()
}

onMounted(() => {
  if (route.query.tab) activeTab.value = route.query.tab
  if (route.query.mine === 'true') mine.value = true
  activeTab.value === 'order' ? loadOrders() : loadProducts()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
