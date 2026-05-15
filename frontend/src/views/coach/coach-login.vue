<template>
  <div class="auth-page coach-theme">
    <div class="auth-language-switch">
      <LanguageSwitch />
    </div>

    <section class="auth-shell">
      <div class="auth-showcase">
        <div class="auth-brand">
          <span class="auth-brand-icon">👨‍🏫</span>
          <span>FitLife Coach</span>
        </div>

        <div class="auth-headline">
          <span class="auth-kicker">{{ t('common.coach') }}</span>
          <h1>{{ t('coach.login.subtitle') }}</h1>
          <p>{{ t('coach.login.portalIntro') }}</p>
          <div class="auth-feature-grid">
            <div class="auth-feature"><strong>📅 {{ t('common.classes') }}</strong><span>{{ t('common.schedule') }} · {{ t('common.classBooking') }}</span></div>
            <div class="auth-feature"><strong>👥 {{ t('common.students') }}</strong><span>{{ t('common.membership') }} · {{ t('common.people') }}</span></div>
            <div class="auth-feature"><strong>🏟️ {{ t('common.facilities') }}</strong><span>{{ t('common.schedule') }} · FitLife</span></div>
            <div class="auth-feature"><strong>⭐ {{ t('common.reviews') }}</strong><span>{{ t('common.complaints') }} · {{ t('common.notifications') }}</span></div>
          </div>
        </div>

        <TestAccountsPanel active-role="coach" />
      </div>

      <div class="auth-panel">
        <div class="auth-card">
          <div class="auth-card-header">
            <div class="auth-card-badge">👨‍🏫</div>
            <h2>{{ t('coach.login.title') }}</h2>
            <p class="auth-card-subtitle">{{ t('coach.login.subtitle') }}</p>
          </div>

          <form class="auth-form" @submit.prevent="handleLogin">
            <div class="auth-field">
              <label for="username">👤 {{ t('common.username') }}</label>
              <div class="auth-input-wrap">
                <span class="auth-input-icon">👤</span>
                <input id="username" v-model.trim="username" type="text" :placeholder="t('coach.login.placeholderUsername')" autocomplete="username" required autofocus />
              </div>
            </div>
            <div class="auth-field">
              <label for="password">🔒 {{ t('common.password') }}</label>
              <div class="auth-input-wrap">
                <span class="auth-input-icon">🔒</span>
                <input id="password" v-model="password" type="password" :placeholder="t('coach.login.placeholderPassword')" autocomplete="current-password" required />
              </div>
            </div>
            <button type="submit" class="auth-button" :disabled="loading">
              {{ loading ? t('common.loading') : t('common.login') }}
            </button>
          </form>

          <div v-if="error" class="auth-error">❌ {{ error }}</div>

          <div class="auth-divider"><span>{{ t('common.or') }}</span></div>
          <div class="auth-role-grid">
            <button class="auth-role-card" type="button" @click="router.push('/user/login')">
              <strong>👤 {{ t('common.user') }}</strong>
              <span>{{ t('coach.login.backToUser') }}</span>
            </button>
            <button class="auth-role-card" type="button" @click="router.push('/admin/login')">
              <strong>👔 {{ t('common.admin') }}</strong>
              <span>{{ t('user.login.adminLink') }}</span>
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import LanguageSwitch from '../../components/LanguageSwitch.vue'
import TestAccountsPanel from '../../components/auth/TestAccountsPanel.vue'
import { saveSession } from '../../services/session'

const { t } = useI18n()
const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.post('/api/coach/login', {
      username: username.value,
      password: password.value
    })
    if (response.data.success) {
      saveSession({
        token: response.data.data.token,
        username: response.data.data.username,
        userType: 'coach',
        coachId: response.data.data.id
      })
      ElMessage.success(t('common.success'))
      router.push('/coach/dashboard')
    } else {
      error.value = response.data.message || t('coach.login.loginFailed')
    }
  } catch (err) {
    error.value = err.response?.data?.message || t('coach.login.loginFailed')
  } finally {
    loading.value = false
  }
}
</script>

<style src="../../assets/styles/auth-pages.css"></style>
