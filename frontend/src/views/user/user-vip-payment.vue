<template>
  <div class="vip-payment-page">
    <header class="user-header">
      <div class="header-content">
        <div class="logo" @click="router.push('/user/dashboard')">
          <span class="logo-icon">💪</span>
          <span class="logo-text">FitLife</span>
        </div>
        <nav class="nav">
          <router-link to="/user/dashboard" class="nav-item">{{ t('common.dashboard') }}</router-link>
          <router-link to="/user/classes" class="nav-item">{{ t('common.classBooking') }}</router-link>
          <router-link to="/user/profile" class="nav-item">{{ t('common.myProfile') }}</router-link>
          <router-link to="/user/points-mall" class="nav-item">{{ t('common.pointsMall') }}</router-link>
          <router-link to="/user/social" class="nav-item">{{ t('common.community') }}</router-link>
          <router-link to="/user/checkin" class="nav-item">{{ t('common.checkin') }}</router-link>
          <router-link to="/user/ranking" class="nav-item">{{ t('common.ranking') }}</router-link>
        </nav>
        <div class="user-info">
          <div class="language-switch">
            <button @click="switchLang('zh')" class="lang-btn" :class="{ active: locale === 'zh' }">{{ t('common.chinese') }}</button>
            <button @click="switchLang('en')" class="lang-btn" :class="{ active: locale === 'en' }">{{ t('common.english') }}</button>
          </div>
          <span class="username">{{ username }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>

    <main class="payment-shell">
      <section class="payment-hero">
        <div>
          <span class="eyebrow">{{ t('user.vipPayment.eyebrow') }}</span>
          <h1>{{ t('user.vipPayment.title') }}</h1>
          <p>{{ t('user.vipPayment.subtitle') }}</p>
          <div class="benefits-grid">
            <div class="benefit"><strong>🎟️</strong><span>{{ t('user.vipPayment.benefitPriority') }}</span></div>
            <div class="benefit"><strong>👑</strong><span>{{ t('user.vipPayment.benefitVipMall') }}</span></div>
            <div class="benefit"><strong>⭐</strong><span>{{ t('user.vipPayment.benefitBadge') }}</span></div>
          </div>
        </div>
      </section>

      <section class="payment-card">
        <div class="plan-panel">
          <h2>{{ t('user.vipPayment.choosePlan') }}</h2>
          <div class="plans">
            <button
              v-for="plan in plans"
              :key="plan.id"
              class="plan-card"
              :class="{ active: selectedPlan === plan.id }"
              @click="selectedPlan = plan.id"
              type="button"
            >
              <span class="plan-badge">{{ t(plan.badgeKey) }}</span>
              <strong>{{ t(plan.nameKey) }}</strong>
              <em>¥{{ plan.price }}</em>
              <small>{{ t(plan.descKey) }}</small>
            </button>
          </div>
        </div>

        <div class="checkout-panel">
          <h2>{{ t('user.vipPayment.paymentInfo') }}</h2>
          <label>{{ t('user.vipPayment.method') }}</label>
          <div class="method-grid">
            <button type="button" class="method" :class="{ active: paymentMethod === 'alipay' }" @click="paymentMethod = 'alipay'">💙 Alipay</button>
            <button type="button" class="method" :class="{ active: paymentMethod === 'wechat' }" @click="paymentMethod = 'wechat'">💚 WeChat Pay</button>
            <button type="button" class="method" :class="{ active: paymentMethod === 'card' }" @click="paymentMethod = 'card'">💳 Card</button>
          </div>

          <div class="demo-pay-box">
            <div class="qr-placeholder">💳</div>
            <div>
              <strong>{{ t('user.vipPayment.demoNoticeTitle') }}</strong>
              <p>{{ t('user.vipPayment.demoNoticeText') }}</p>
            </div>
          </div>

          <div class="summary-row">
            <span>{{ t('user.vipPayment.selectedPlan') }}</span>
            <strong>{{ selectedPlanName }}</strong>
          </div>
          <div class="summary-row total">
            <span>{{ t('user.vipPayment.total') }}</span>
            <strong>¥{{ selectedPrice }}</strong>
          </div>

          <button class="pay-btn" :disabled="submitting" @click="confirmPayment">
            {{ submitting ? t('common.loading') : t('user.vipPayment.payNow') }}
          </button>
          <button class="back-btn" type="button" @click="router.push('/user/dashboard')">
            {{ t('user.vipPayment.backDashboard') }}
          </button>
        </div>
      </section>
    </main>

    <div v-if="toast.show" class="toast show" :class="toast.type">{{ toast.message }}</div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { clearSession, getCurrentUsername } from '../../services/session'
import { purchaseVipMembership } from '../../services/userService'

const router = useRouter()
const { t, locale } = useI18n()
const username = ref(getCurrentUsername(t('common.guestName')))
const selectedPlan = ref('yearly')
const paymentMethod = ref('alipay')
const submitting = ref(false)
const toast = ref({ show: false, message: '', type: 'success' })

const plans = [
  { id: 'monthly', price: 99, badgeKey: 'user.vipPayment.monthlyBadge', nameKey: 'user.vipPayment.monthlyName', descKey: 'user.vipPayment.monthlyDesc' },
  { id: 'quarterly', price: 259, badgeKey: 'user.vipPayment.quarterlyBadge', nameKey: 'user.vipPayment.quarterlyName', descKey: 'user.vipPayment.quarterlyDesc' },
  { id: 'yearly', price: 699, badgeKey: 'user.vipPayment.yearlyBadge', nameKey: 'user.vipPayment.yearlyName', descKey: 'user.vipPayment.yearlyDesc' }
]

const selectedPlanObj = computed(() => plans.find(item => item.id === selectedPlan.value) || plans[2])
const selectedPlanName = computed(() => t(selectedPlanObj.value.nameKey))
const selectedPrice = computed(() => selectedPlanObj.value.price)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value = { show: false, message: '', type: 'success' }
  }, 2600)
}

const confirmPayment = async () => {
  submitting.value = true
  try {
    const result = await purchaseVipMembership({ plan: selectedPlan.value, paymentMethod: paymentMethod.value })
    if (result.success) {
      showToast(t('user.vipPayment.paySuccess'))
      setTimeout(() => router.push('/user/dashboard'), 900)
    } else {
      showToast(result.message || t('user.vipPayment.payFailed'), 'error')
    }
  } catch (error) {
    showToast(error?.response?.data?.message || t('user.vipPayment.payFailed'), 'error')
  } finally {
    submitting.value = false
  }
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}
</script>

<style scoped>
.vip-payment-page {
  min-height: 100vh;
  background: radial-gradient(circle at top left, rgba(102,126,234,.18), transparent 32rem), linear-gradient(180deg,#f7f9ff 0%,#eef3f8 100%);
  color: #0f172a;
}

.logo { cursor: pointer; }

.payment-shell {
  max-width: 1180px;
  margin: 0 auto;
  padding: 34px 28px 60px;
}

.payment-hero {
  position: relative;
  overflow: hidden;
  padding: 44px;
  border-radius: 34px;
  color: white;
  background: linear-gradient(135deg, #1f2a68 0%, #667eea 48%, #764ba2 100%);
  box-shadow: 0 28px 70px rgba(76, 81, 191, .24);
}

.eyebrow {
  display: inline-flex;
  padding: 8px 13px;
  border-radius: 999px;
  background: rgba(255,255,255,.15);
  font-size: 13px;
  font-weight: 900;
  margin-bottom: 12px;
}

.payment-hero h1 { margin: 0; font-size: clamp(34px, 5vw, 58px); line-height: 1.06; }
.payment-hero p { max-width: 720px; color: rgba(255,255,255,.86); font-size: 17px; line-height: 1.8; }

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 26px;
}

.benefit {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 16px;
  border-radius: 18px;
  background: rgba(255,255,255,.13);
  border: 1px solid rgba(255,255,255,.18);
  font-weight: 800;
}

.payment-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 24px;
  margin-top: 24px;
}

.plan-panel,
.checkout-panel {
  border-radius: 28px;
  background: white;
  padding: 28px;
  box-shadow: 0 20px 55px rgba(15,23,42,.09);
  border: 1px solid rgba(226,232,240,.82);
}

.plan-panel h2,
.checkout-panel h2 { margin: 0 0 18px; }

.plans { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.plan-card {
  text-align: left;
  min-height: 210px;
  padding: 20px;
  border-radius: 22px;
  border: 2px solid #e5e7eb;
  background: #f8fafc;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: .2s;
}
.plan-card.active { border-color: #f59e0b; background: linear-gradient(180deg,#fff7ed,#fff); box-shadow: 0 18px 42px rgba(245,158,11,.16); }
.plan-badge { width: fit-content; padding: 6px 10px; border-radius: 999px; background: #fef3c7; color: #b45309; font-size: 12px; font-weight: 900; }
.plan-card strong { font-size: 20px; }
.plan-card em { font-style: normal; font-size: 34px; font-weight: 900; color: #f59e0b; }
.plan-card small { color: #64748b; line-height: 1.6; }

.checkout-panel label { display: block; margin-bottom: 10px; font-weight: 900; }
.method-grid { display: grid; gap: 10px; }
.method { border: 1px solid #e5e7eb; border-radius: 14px; padding: 12px; background: #fff; cursor: pointer; font-weight: 800; }
.method.active { border-color: #667eea; background: #eef2ff; color: #4f46e5; }
.demo-pay-box { display: flex; gap: 12px; align-items: center; margin: 18px 0; padding: 14px; border-radius: 18px; background: #f8fafc; }
.qr-placeholder { width: 58px; height: 58px; display:grid; place-items:center; border-radius: 18px; background: #eef2ff; font-size: 28px; }
.demo-pay-box p { margin: 4px 0 0; color: #64748b; line-height: 1.6; }
.summary-row { display:flex; justify-content:space-between; padding: 12px 0; border-top: 1px solid #edf2f7; color: #475569; }
.summary-row.total { font-size: 20px; color: #0f172a; }
.pay-btn,.back-btn { width: 100%; min-height: 50px; border: none; border-radius: 16px; font-weight: 900; cursor: pointer; margin-top: 12px; }
.pay-btn { color: white; background: linear-gradient(135deg,#f59e0b,#ef4444); box-shadow: 0 14px 30px rgba(245,158,11,.2); }
.pay-btn:disabled { opacity: .65; cursor: not-allowed; }
.back-btn { background: #f1f5f9; color: #475569; }
.toast { position: fixed; right: 24px; top: 96px; z-index: 1000; padding: 14px 18px; border-radius: 14px; color:white; font-weight:900; box-shadow: 0 18px 40px rgba(15,23,42,.18); }
.toast.success { background: #10b981; }
.toast.error { background: #ef4444; }
@media (max-width: 920px) { .payment-card,.benefits-grid,.plans { grid-template-columns: 1fr; } }
</style>
