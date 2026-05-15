<template>
  <div class="language-switch">
    <button 
      class="lang-btn" 
      :class="{ active: currentLocale === 'zh' }"
      @click="setLocale('zh')"
    >
      中文
    </button>
    <button 
      class="lang-btn" 
      :class="{ active: currentLocale === 'en' }"
      @click="setLocale('en')"
    >
      EN
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

const setLocale = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}
</script>

<style scoped>
.language-switch {
  display: flex;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 6px;
  border-radius: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.lang-btn {
  padding: 8px 18px;
  border: none;
  border-radius: 16px;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
  min-width: 60px;
}

.lang-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.lang-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}
</style>
