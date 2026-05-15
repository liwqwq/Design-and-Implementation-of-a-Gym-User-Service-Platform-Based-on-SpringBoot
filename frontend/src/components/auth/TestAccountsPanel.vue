<template>
  <section class="auth-test-section" aria-label="test accounts">
    <h3>📋 {{ t('common.testAccounts') }}</h3>

    <div class="auth-account-list">
      <article class="auth-account-group active">
        <div class="auth-account-icon">{{ currentGroup.icon }}</div>
        <div class="auth-account-content">
          <strong>{{ currentGroup.title }}</strong>
          <span v-for="line in currentGroup.lines" :key="line">{{ line }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  activeRole: {
    type: String,
    default: 'user',
    validator: value => ['user', 'coach', 'admin'].includes(value)
  }
})

const groups = computed(() => ({
  user: {
    icon: '👤',
    title: t('common.user'),
    lines: [
      'fitness_pro / 123456',
      'gym_master / 123456',
      'yoga_lover / 123456'
    ]
  },
  coach: {
    icon: '👨‍🏫',
    title: t('common.coach'),
    lines: [
      'coach_1 / 123456',
      'coach_2 / 123456',
      'coach_3 / 123456',
      'coach_4 / 123456'
    ]
  },
  admin: {
    icon: '👔',
    title: t('common.admin'),
    lines: ['admin / admin123']
  }
}))

const currentGroup = computed(() => groups.value[props.activeRole] || groups.value.user)
</script>
