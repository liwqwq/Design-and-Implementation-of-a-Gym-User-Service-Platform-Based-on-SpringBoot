<template>
  <UserLayout>
    <div class="social-container">
      <div class="social-header">
        <h2>💬 健身社区</h2>
        <el-button type="primary" @click="showPostDialog = true">发布动态</el-button>
      </div>

      <div class="social-filters">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="🔥 热度" name="hot" />
          <el-tab-pane label="✨ 最新" name="new" />
        </el-tabs>
        <el-select v-model="categoryFilter" placeholder="全部分类" size="small">
          <el-option label="全部分类" value="" />
          <el-option label="求助" value="help" />
          <el-option label="分享" value="share" />
          <el-option label="组队" value="team" />
          <el-option label="吐槽" value="rant" />
        </el-select>
      </div>

      <div class="posts-list">
        <div v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-header">
            <div class="post-author">
              <div class="author-avatar">{{ post.authorAvatar }}</div>
              <div class="author-info">
                <div class="author-name">{{ post.authorName }}</div>
                <div class="post-time">{{ post.createTime }}</div>
              </div>
            </div>
            <el-tag :type="getCategoryType(post.category)" size="small">{{ getCategoryName(post.category) }}</el-tag>
          </div>
          <div class="post-content">{{ post.content }}</div>
          <div class="post-actions">
            <div class="action-item" @click="likePost(post)">👍 {{ post.likes }}</div>
            <div class="action-item" @click="showComments(post)">💬 {{ post.comments }}</div>
            <div class="action-item" @click="reportPost(post)">🚩 举报</div>
          </div>
          <div v-if="post.showComments" class="post-comments">
            <div v-for="comment in post.commentList" :key="comment.id" class="comment-item">
              <div class="comment-avatar">{{ comment.avatar }}</div>
              <div class="comment-content">
                <div class="comment-author">{{ comment.author }}</div>
                <div class="comment-text">{{ comment.content }}</div>
              </div>
            </div>
            <div class="comment-input">
              <el-input v-model="post.newComment" placeholder="写下你的评论..." size="small">
                <template #append><el-button @click="addComment(post)">发送</el-button></template>
              </el-input>
            </div>
          </div>
        </div>
      </div>

      <el-dialog v-model="showPostDialog" title="发布动态" width="600px">
        <div class="post-form">
          <div>
            <label>分类</label>
            <el-select v-model="newPost.category">
              <el-option label="求助" value="help" />
              <el-option label="分享" value="share" />
              <el-option label="组队" value="team" />
              <el-option label="吐槽" value="rant" />
            </el-select>
          </div>
          <div>
            <label>内容</label>
            <el-input v-model="newPost.content" type="textarea" :rows="4" placeholder="分享你的健身心得..." />
          </div>
        </div>
        <template #footer>
          <el-button @click="showPostDialog = false">取消</el-button>
          <el-button type="primary" @click="submitPost">发布</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="showReportDialog" title="举报帖子" width="400px">
        <div>
          <label>举报原因</label>
          <el-select v-model="reportReason">
            <el-option label="垃圾广告" value="spam" />
            <el-option label="人身攻击" value="attack" />
            <el-option label="虚假信息" value="fake" />
          </el-select>
        </div>
        <template #footer>
          <el-button @click="showReportDialog = false">取消</el-button>
          <el-button type="danger" @click="submitReport">提交举报</el-button>
        </template>
      </el-dialog>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'
import { ElButton, ElDialog, ElSelect, ElInput, ElTag } from 'element-plus'

const activeTab = ref('hot')
const categoryFilter = ref('')
const showPostDialog = ref(false)
const showReportDialog = ref(false)
const reportReason = ref('')

const newPost = ref({ category: 'share', content: '' })

const posts = ref([
  { id: 1, authorAvatar: '💪', authorName: '健身达人小王', createTime: '2小时前', category: 'share', content: '今天完成了5公里跑步，感觉太棒了！', likes: 128, comments: 32, showComments: false, commentList: [], newComment: '' },
  { id: 2, authorAvatar: '🧘', authorName: '瑜伽小姐姐', createTime: '4小时前', category: 'help', content: '有人知道正确的深蹲姿势吗？', likes: 45, comments: 18, showComments: false, commentList: [], newComment: '' },
  { id: 3, authorAvatar: '🏃', authorName: '跑步爱好者', createTime: '6小时前', category: 'team', content: '周末有人一起跑步吗？', likes: 89, comments: 24, showComments: false, commentList: [], newComment: '' }
])

const getCategoryType = (category) => {
  const types = { help: 'warning', share: 'success', team: 'primary', rant: 'danger' }
  return types[category] || 'info'
}

const getCategoryName = (category) => {
  const names = { help: '求助', share: '分享', team: '组队', rant: '吐槽' }
  return names[category] || category
}

const likePost = (post) => { post.likes++ }
const showComments = (post) => { post.showComments = !post.showComments }
const addComment = (post) => {
  if (!post.newComment.trim()) return
  post.commentList.push({ id: Date.now(), avatar: '👤', author: '我', content: post.newComment })
  post.comments++
  post.newComment = ''
}
const reportPost = (post) => { showReportDialog.value = true }
const submitReport = () => { ElMessage.success('举报成功'); showReportDialog.value = false }
const submitPost = () => { ElMessage.success('发布成功'); showPostDialog.value = false; newPost.value = { category: 'share', content: '' } }
</script>

<style scoped>
.social-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.social-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.social-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px;
  border-radius: 15px;
  margin-bottom: 20px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-avatar {
  font-size: 36px;
}

.author-name {
  font-weight: 600;
}

.post-time {
  font-size: 13px;
  color: #666;
}

.post-content {
  line-height: 1.6;
  margin-bottom: 15px;
}

.post-actions {
  display: flex;
  gap: 30px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #666;
}

.action-item:hover {
  color: #667eea;
}

.post-comments {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.comment-item {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.comment-avatar {
  font-size: 24px;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
}

.comment-text {
  margin-top: 5px;
}

.comment-input {
  margin-top: 15px;
}

.post-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-form label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}
</style>
