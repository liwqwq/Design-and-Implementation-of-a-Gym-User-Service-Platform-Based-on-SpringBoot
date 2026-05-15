<template>
  <Layout>
    <div class="admin-container">
      <h2 class="page-title">会员管理</h2>
      
      <el-card class="admin-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>会员列表</span>
            <el-button type="primary" @click="getAllUsers">
              <el-icon><Refresh /></el-icon>
              刷新列表
            </el-button>
          </div>
        </template>
        
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="users.length > 0">
          <el-table :data="users" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="phone" label="电话" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ scope.row.status === 'ACTIVE' ? '活跃' : '已禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="role" label="角色" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="primary" size="small" @click="editUser(scope.row)" style="margin-right: 8px">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="deleteUser(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-else class="empty-state">
          <el-empty description="暂无会员数据" />
        </div>
        
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Layout from '../components/Layout.vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const users = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')

const getAllUsers = async () => {
  try {
    loading.value = true
    error.value = ''
    success.value = ''
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/users', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      users.value = response.data.data
      success.value = '获取会员列表成功'
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取会员列表失败'
    console.error('获取会员列表失败', err)
  } finally {
    loading.value = false
  }
}

const editUser = (user) => {
  // 这里可以实现编辑用户的逻辑，比如打开模态框
  console.log('编辑用户', user)
  // 可以在这里添加编辑用户的模态框逻辑
}

const deleteUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const token = localStorage.getItem('token')
    const response = await axios.delete(`/api/admin/users/${userId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      success.value = '删除用户成功'
      getAllUsers() // 重新获取用户列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    if (err !== 'cancel') {
      error.value = '删除用户失败'
      console.error('删除用户失败', err)
    }
  }
}

onMounted(() => {
  getAllUsers()
})
</script>

<style scoped>
.admin-container {
  padding: 20px;
  min-height: 80vh;
  background-color: #f5f7fa;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 20px;
  text-align: center;
  position: relative;
}

.page-title::after {
  content: '';
  display: block;
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #10b981);
  margin: 10px auto 0;
  border-radius: 2px;
}

.admin-card {
  max-width: 1000px;
  margin: 0 auto;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.admin-card:hover {
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #3b82f6, #10b981);
  color: white;
  border-bottom: none;
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.card-header :deep(.el-button) {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.card-header :deep(.el-button:hover) {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

.loading-state {
  padding: 40px 0;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.error-message {
  color: #ef4444;
  font-size: 14px;
  margin-top: 10px;
  text-align: center;
  padding: 10px;
  background-color: #fef2f2;
  border-radius: 4px;
  border-left: 4px solid #ef4444;
}

.success-message {
  color: #10b981;
  font-size: 14px;
  margin-top: 10px;
  text-align: center;
  padding: 10px;
  background-color: #f0fdf4;
  border-radius: 4px;
  border-left: 4px solid #10b981;
}

@media (max-width: 768px) {
  .admin-card {
    margin: 0 10px;
  }
  
  .admin-container {
    padding: 10px;
  }
  
  .card-header {
    padding: 12px 16px;
  }
  
  .card-header span {
    font-size: 16px;
  }
  
  .empty-state {
    padding: 40px 0;
  }
}
</style>