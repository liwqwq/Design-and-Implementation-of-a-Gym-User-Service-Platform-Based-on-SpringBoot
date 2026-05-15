<template>
  <div class="auth-page admin-theme">
    <div class="auth-language-switch">
      <LanguageSwitch />
    </div>

    <section class="auth-shell">
      <div class="auth-showcase">
        <div class="auth-brand">
          <span class="auth-brand-icon">🏋️</span>
          <span>FitLife Admin</span>
        </div>

        <div class="auth-headline">
          <span class="auth-kicker">{{ t('common.admin') }}</span>
          <h1>{{ t('admin.login.subtitle') }}</h1>
          <p>{{ t('admin.login.portalIntro') }}</p>
          <div class="auth-feature-grid">
            <div class="auth-feature"><strong>👥 {{ t('common.users') }}</strong><span>{{ t('common.membership') }} · {{ t('common.settings') }}</span></div>
            <div class="auth-feature"><strong>📅 {{ t('common.classes') }}</strong><span>{{ t('common.coach') }} · {{ t('common.classBooking') }}</span></div>
            <div class="auth-feature"><strong>🛍️ {{ t('common.products') }}</strong><span>{{ t('common.pointsMall') }} · {{ t('common.points') }}</span></div>
            <div class="auth-feature"><strong>💬 {{ t('common.reports') }}</strong><span>{{ t('common.community') }} · {{ t('common.complaints') }}</span></div>
          </div>
        </div>

        <TestAccountsPanel active-role="admin" />
      </div>

      <div class="auth-panel">
        <div class="auth-card">
          <div class="auth-card-header">
            <div class="auth-card-badge">👔</div>
            <h2>{{ t('admin.login.title') }}</h2>
            <p class="auth-card-subtitle">{{ t('admin.login.subtitle') }}</p>
          </div>

          <form class="auth-form" @submit.prevent="login">
            <div class="auth-field">
              <label for="username">👤 {{ t('common.username') }}</label>
              <div class="auth-input-wrap">
                <span class="auth-input-icon">👤</span>
                <input id="username" v-model.trim="form.username" type="text" required :placeholder="t('admin.login.placeholderUsername')" autocomplete="username" autofocus />
              </div>
            </div>
            <div class="auth-field">
              <label for="password">🔒 {{ t('common.password') }}</label>
              <div class="auth-input-wrap">
                <span class="auth-input-icon">🔒</span>
                <input id="password" v-model="form.password" type="password" required :placeholder="t('admin.login.placeholderPassword')" autocomplete="current-password" />
              </div>
            </div>
            <button type="submit" class="auth-button" :disabled="isSubmitting">
              {{ isSubmitting ? t('common.loading') : t('common.login') }}
            </button>
          </form>

          <div v-if="error" class="auth-error">❌ {{ error }}</div>

          <div class="auth-divider"><span>{{ t('common.or') }}</span></div>
          <div class="auth-role-grid">
            <button class="auth-role-card" type="button" @click="router.push('/user/login')">
              <strong>👤 {{ t('common.user') }}</strong>
              <span>{{ t('admin.login.backToUser') }}</span>
            </button>
            <button class="auth-role-card" type="button" @click="router.push('/coach/login')">
              <strong>👨‍🏫 {{ t('common.coach') }}</strong>
              <span>{{ t('user.login.coachLink') }}</span>
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
import LanguageSwitch from '../../components/LanguageSwitch.vue'
import TestAccountsPanel from '../../components/auth/TestAccountsPanel.vue'
import { saveSession } from '../../services/session'

const { t } = useI18n()
const router = useRouter()
const form = ref({ username: '', password: '' })
const error = ref('')
const isSubmitting = ref(false)

const login = async () => {
  try {
    isSubmitting.value = true
    error.value = ''
    const response = await axios.post('/api/admin/auth/login', form.value)
    if (response.data.success) {
      saveSession({
        token: response.data.token,
        username: response.data.username,
        userType: 'admin'
      })
      router.push('/admin')
    } else {
      error.value = response.data.message || t('admin.login.loginFailed')
    }
  } catch (err) {
    error.value = err.response?.data?.message || t('admin.login.loginFailed')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style src="../../assets/styles/auth-pages.css"></style>
