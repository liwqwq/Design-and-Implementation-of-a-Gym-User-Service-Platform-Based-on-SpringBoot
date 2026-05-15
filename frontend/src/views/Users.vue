<template>
  <Layout>
    <div class="users-container">
      <h2 class="page-title">用户管理</h2>
      
      <el-card class="users-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>创建用户</span>
          </div>
        </template>
        
        <el-form :model="createForm" @submit.prevent="createUser" label-width="80px">
          <el-form-item label="用户名" required>
            <el-input v-model="createForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" required>
            <el-input v-model="createForm.password" type="password" placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="姓名" required>
            <el-input v-model="createForm.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="邮箱" required>
            <el-input v-model="createForm.email" type="email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="电话" required>
            <el-input v-model="createForm.phone" placeholder="请输入电话" />
          </el-form-item>
          <el-form-item label="生日">
            <el-date-picker
              v-model="createForm.birthday"
              type="date"
              placeholder="选择生日"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="createForm.gender" placeholder="请选择性别">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="createForm.address" placeholder="请输入地址" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit">创建用户</el-button>
          </el-form-item>
        </el-form>
        
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
      </el-card>
      
      <el-card class="users-card" shadow="hover" style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>用户列表</span>
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
            <el-table-column prop="birthday" label="生日">
              <template #default="scope">
                {{ scope.row.birthday || '未设置' }}
              </template>
            </el-table-column>
            <el-table-column prop="gender" label="性别">
              <template #default="scope">
                {{ scope.row.gender || '未设置' }}
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址">
              <template #default="scope">
                {{ scope.row.address || '未设置' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ scope.row.status === 'ACTIVE' ? '活跃' : '已禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="role" label="角色" />
          </el-table>
        </div>
        
        <div v-else class="empty-state">
          <el-empty description="暂无用户数据" />
        </div>
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

const router = useRouter()
const users = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')
const createForm = ref({
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  birthday: '',
  gender: '男',
  address: ''
})

const createUser = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/users', createForm.value, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      success.value = '创建用户成功'
      createForm.value = {
        username: '',
        password: '',
        name: '',
        email: '',
        phone: '',
        birthday: '',
        gender: '男',
        address: ''
      }
      getAllUsers() // 重新获取用户列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '创建用户失败'
  }
}

const getAllUsers = async () => {
  try {
    loading.value = true
    error.value = ''
    success.value = ''
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/users', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      users.value = response.data.data
      success.value = '获取用户列表成功'
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取用户列表失败'
    console.error('获取用户列表失败', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getAllUsers()
})
</script>

<style scoped>
.users-container {
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

.users-card {
  max-width: 1000px;
  margin: 0 auto;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.users-card:hover {
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
  .users-card {
    margin: 0 10px;
  }
  
  .users-container {
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