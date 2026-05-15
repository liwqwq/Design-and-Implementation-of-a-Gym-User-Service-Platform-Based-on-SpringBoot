<template>
  <div class="app">
    <router-view />
  </div>
</template>

<script setup>
import { watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale, t } = useI18n()

function syncDocumentLocale() {
  document.documentElement.lang = locale.value === 'zh' ? 'zh-CN' : 'en'
  document.title = t('common.appTitle')
}

onMounted(() => syncDocumentLocale())
watch(locale, () => syncDocumentLocale())
</script>

<style>
:root {
  --primary-color: #3b82f6;
  --secondary-color: #10b981;
  --background-color: #f3f4f6;
  --card-background: #ffffff;
  --text-color: #1f2937;
  --border-color: #e5e7eb;
  --error-color: #ef4444;
  --success-color: #10b981;
  --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --border-radius: 8px;
  --transition: all 0.3s ease;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Arial', sans-serif;
  background-color: var(--background-color);
  color: var(--text-color);
  line-height: 1.6;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  flex: 1;
}

/* 表单样式 */
.form-container {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  padding: 30px;
  margin-bottom: 20px;
  transition: var(--transition);
}

.form-container:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.form-container h3 {
  margin-bottom: 20px;
  color: var(--primary-color);
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-color);
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  font-size: 16px;
  transition: var(--transition);
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group button {
  width: 100%;
  padding: 12px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
}

.form-group button:hover {
  background-color: #2563eb;
  transform: translateY(-2px);
}

.form-footer {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.form-footer a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 10px;
  }
  
  .form-container {
    padding: 20px;
  }
}

/* 错误消息 */
.error-message {
  color: var(--error-color);
  font-size: 14px;
  margin-top: 5px;
}

/* 成功消息 */
.success-message {
  color: var(--success-color);
  font-size: 14px;
  margin-top: 5px;
}

/* 导航栏 */
.nav {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.nav button {
  padding: 10px 20px;
  background-color: var(--card-background);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: var(--transition);
  font-size: 14px;
  font-weight: 600;
}

.nav button:hover {
  background-color: var(--primary-color);
  color: white;
  transform: translateY(-2px);
}

.nav button.active {
  background-color: var(--primary-color);
  color: white;
}

/* 表格样式 */
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

th {
  background-color: var(--primary-color);
  color: white;
  font-weight: 600;
}

tr:hover {
  background-color: #f9fafb;
}

/* 响应式表格 */
@media (max-width: 768px) {
  table {
    display: block;
    overflow-x: auto;
  }
}

/* 按钮样式 */
button {
  padding: 10px 20px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
}

button:hover {
  background-color: #2563eb;
  transform: translateY(-2px);
}

button.secondary {
  background-color: var(--secondary-color);
}

button.secondary:hover {
  background-color: #059669;
}

button.danger {
  background-color: var(--error-color);
}

button.danger:hover {
  background-color: #dc2626;
}

/* 卡片样式 */
.card {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  padding: 20px;
  margin-bottom: 20px;
  transition: var(--transition);
}

.card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card {
    padding: 15px;
  }
}
</style>