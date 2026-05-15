<template>
  <div class="auth-page user-theme">
    <div class="auth-language-switch">
      <LanguageSwitch />
    </div>

    <section class="auth-shell">
      <div class="auth-showcase">
        <div class="auth-brand">
          <span class="auth-brand-icon">💪</span>
          <span>FitLife</span>
        </div>

        <div class="auth-headline">
          <span class="auth-kicker">{{ t('common.classBooking') }}</span>
          <h1>{{ t('user.dashboard.startJourney') }}</h1>
          <p>{{ t('user.login.subtitle') }}</p>
          <div class="auth-feature-grid">
            <div class="auth-feature">
              <strong>📅 {{ t('common.classes') }}</strong>
              <span>{{ t('common.classBooking') }} · {{ t('common.schedule') }}</span>
            </div>
            <div class="auth-feature">
              <strong>🏆 {{ t('common.points') }}</strong>
              <span>{{ t('common.pointsMall') }} · {{ t('common.ranking') }}</span>
            </div>
            <div class="auth-feature">
              <strong>✅ {{ t('common.checkin') }}</strong>
              <span>{{ t('common.checkin') }} · {{ t('common.community') }}</span>
            </div>
            <div class="auth-feature">
              <strong>👨‍🏫 {{ t('common.coach') }}</strong>
              <span>{{ t('common.trainer') }} · FitLife</span>
            </div>
          </div>
        </div>

        <TestAccountsPanel active-role="user" />
      </div>

      <div class="auth-panel">
        <div class="auth-card">
          <div class="auth-card-header">
            <div class="auth-card-badge">💪</div>
            <h2>{{ t('user.login.title') }}</h2>
            <p class="auth-card-subtitle">{{ t('common.welcome') }} FitLife</p>
          </div>

          <form class="auth-form" @submit.prevent="login">
            <div class="auth-field">
              <label for="username">👤 {{ t('common.username') }}</label>
              <div class="auth-input-wrap">
                <span class="auth-input-icon">👤</span>
                <input id="username" v-model.trim="form.username" type="text" :placeholder="t('user.login.placeholderUsername')" autocomplete="username" autofocus required />
              </div>
            </div>

            <div class="auth-field">
              <label for="password">🔒 {{ t('common.password') }}</label>
              <div class="auth-input-wrap">
                <span class="auth-input-icon">🔒</span>
                <input id="password" v-model="form.password" type="password" :placeholder="t('user.login.placeholderPassword')" autocomplete="current-password" required />
              </div>
            </div>

            <div class="auth-inline-actions">
              <router-link to="/user/forgot-password">{{ t('common.forgotPassword') }}</router-link>
            </div>

            <button type="submit" class="auth-button" :disabled="isSubmitting">
              {{ isSubmitting ? t('common.loading') : t('common.login') }}
            </button>
          </form>

          <div v-if="error" class="auth-error">❌ {{ error }}</div>

          <div class="auth-divider"><span>{{ t('common.or') }}</span></div>

          <div class="auth-role-grid">
            <button class="auth-role-card" type="button" @click="goToAdmin">
              <strong>👔 {{ t('user.login.adminLink') }}</strong>
              <span>FitLife Admin</span>
            </button>
            <button class="auth-role-card" type="button" @click="goToCoach">
              <strong>👨‍🏫 {{ t('user.login.coachLink') }}</strong>
              <span>FitLife Coach</span>
            </button>
          </div>

          <div class="auth-register-line">
            <router-link class="auth-link" to="/user/register">{{ t('user.login.registerLink') }}</router-link>
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
    const response = await axios.post('/api/auth/login', form.value)
    if (response.data.success) {
      saveSession({
        token: response.data.token,
        username: response.data.username,
        userType: 'user'
      })
      router.push('/user/dashboard')
    } else {
      error.value = response.data.message || t('user.login.loginFailed')
    }
  } catch (err) {
    error.value = err.response?.data?.message || t('user.login.loginFailed')
  } finally {
    isSubmitting.value = false
  }
}

const goToAdmin = () => router.push('/admin/login')
const goToCoach = () => router.push('/coach/login')
</script>

<style src="../../assets/styles/auth-pages.css"></style>
