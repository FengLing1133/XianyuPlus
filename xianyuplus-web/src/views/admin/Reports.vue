<template>
  <div class="reports-page">
    <div class="page-header">
      <h2>举报管理</h2>
      <div class="filter-bar">
        <select v-model="statusFilter" @change="fetchData">
          <option value="">全部状态</option>
          <option value="0">待处理</option>
          <option value="1">已处理</option>
          <option value="2">已驳回</option>
        </select>
      </div>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>商品ID</th>
            <th>举报者ID</th>
            <th>举报原因</th>
            <th>补充说明</th>
            <th>状态</th>
            <th>举报时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="report in reports" :key="report.id">
            <td>{{ report.productId }}</td>
            <td>{{ report.reporterId }}</td>
            <td>{{ reasonText(report.reason) }}</td>
            <td>{{ report.description || '-' }}</td>
            <td>
              <span :class="'status-tag status-' + report.status">
                {{ statusText(report.status) }}
              </span>
            </td>
            <td>{{ formatTime(report.createdAt) }}</td>
            <td>
              <button
                v-if="report.status === 0"
                class="btn-handle"
                @click="openHandle(report)"
              >
                处理
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div v-if="total > 10" class="pagination">
      <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
      <span>{{ page }} / {{ Math.ceil(total / 10) }}</span>
      <button :disabled="page >= Math.ceil(total / 10)" @click="goPage(page + 1)">下一页</button>
    </div>

    <!-- 处理弹窗 -->
    <div v-if="handlingReport" class="handle-modal" @click.self="cancelHandle">
      <div class="handle-form">
        <h4>处理举报</h4>
        <div class="form-group">
          <label>处理方式</label>
          <div class="handle-options">
            <label>
              <input type="radio" :value="1" v-model="handleStatus">
              <span>处理（下架商品）</span>
            </label>
            <label>
              <input type="radio" :value="2" v-model="handleStatus">
              <span>驳回（举报无效）</span>
            </label>
          </div>
        </div>
        <div class="form-group">
          <label>处理备注</label>
          <textarea v-model="adminNote" placeholder="输入处理备注..." rows="3"></textarea>
        </div>
        <div class="form-actions">
          <button @click="cancelHandle">取消</button>
          <button @click="confirmHandle" :disabled="!handleStatus">确认处理</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReports, handleReport } from '@/api/report'
import { Toast } from '@/utils/toast'

const reports = ref([])
const total = ref(0)
const page = ref(1)
const statusFilter = ref('')

const handlingReport = ref(null)
const handleStatus = ref(null)
const adminNote = ref('')

const reasonMap = {
  1: '虚假信息',
  2: '违禁品',
  3: '价格异常',
  4: '恶意欺诈',
  5: '其他'
}

const statusMap = {
  0: '待处理',
  1: '已处理',
  2: '已驳回'
}

function reasonText(reason) {
  return reasonMap[reason] || '未知'
}

function statusText(status) {
  return statusMap[status] || '未知'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function fetchData() {
  const res = await getReports(statusFilter.value || undefined, page.value, 10)
  if (res.code === 200) {
    reports.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

function goPage(p) {
  page.value = p
  fetchData()
}

function openHandle(report) {
  handlingReport.value = report
  handleStatus.value = null
  adminNote.value = ''
}

function cancelHandle() {
  handlingReport.value = null
  handleStatus.value = null
  adminNote.value = ''
}

async function confirmHandle() {
  if (handleStatus.value === null) {
    Toast.warning('请选择处理方式')
    return
  }

  await handleReport(handlingReport.value.id, handleStatus.value, adminNote.value)
  Toast.success('处理成功')
  cancelHandle()
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.reports-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
}

.filter-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.table-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

td {
  font-size: 14px;
  color: #666;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-0 {
  background: #fff3e0;
  color: #e65100;
}

.status-1 {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-2 {
  background: #fef0f0;
  color: #c62828;
}

.btn-handle {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.btn-handle:hover {
  border-color: var(--green-500);
  color: var(--green-500);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:hover:not(:disabled) {
  border-color: var(--green-500);
  color: var(--green-500);
}

.handle-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.handle-form {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  width: 480px;
  max-width: 90vw;
}

.handle-form h4 {
  margin: 0 0 20px;
  font-size: 18px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.handle-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.handle-options label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}

.handle-options input {
  margin: 0;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
}

.form-group textarea:focus {
  outline: none;
  border-color: var(--green-500);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.form-actions button {
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.form-actions button:first-child {
  border: 1px solid #ddd;
  background: #fff;
}

.form-actions button:last-child {
  border: none;
  background: var(--green-500);
  color: #fff;
}

.form-actions button:last-child:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
