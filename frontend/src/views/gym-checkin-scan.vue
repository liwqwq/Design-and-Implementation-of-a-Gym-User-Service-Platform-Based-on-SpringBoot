<template>
  <div class="gym-scan-page">
    <div class="card">
      <h1>{{ t('gymScan.title') }}</h1>
      <p class="hint">{{ t('gymScan.hint') }}</p>
      <label class="lbl">{{ t('gymScan.tokenLabel') }}</label>
      <textarea v-model="token" rows="6" class="ta" :placeholder="t('gymScan.placeholder')" />
      <button type="button" class="btn" :disabled="loading || !token.trim()" @click="submit">
        {{ loading ? t('gymScan.submitting') : t('gymScan.submit') }}
      </button>
      <p v-if="message" :class="['msg', ok ? 'ok' : 'err']">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const token = ref('')
const loading = ref(false)
const message = ref('')
const ok = ref(false)

const submit = async () => {
  message.value = ''
  loading.value = true
  try {
    const { data } = await axios.post('/api/checkin/scan', { token: token.value.trim() })
    ok.value = !!data.success
    message.value = data.message || (data.success ? t('gymScan.success') : t('gymScan.fail'))
  } catch (e) {
    ok.value = false
    message.value = e.response?.data?.message || e.message || t('gymScan.fail')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.gym-scan-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
  padding: 24px;
}
.card {
  max-width: 520px;
  width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}
h1 {
  margin: 0 0 12px;
  font-size: 22px;
  color: #1a1a1a;
}
.hint {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  line-height: 1.5;
}
.lbl {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #444;
  margin-bottom: 8px;
}
.ta {
  width: 100%;
  box-sizing: border-box;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-family: ui-monospace, monospace;
  font-size: 12px;
  resize: vertical;
}
.btn {
  margin-top: 16px;
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.msg {
  margin-top: 16px;
  font-size: 14px;
}
.msg.ok {
  color: #15803d;
}
.msg.err {
  color: #b91c1c;
}
</style>
