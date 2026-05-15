import { createRouter, createWebHistory } from 'vue-router'
import UserLogin from '../views/user/user-login.vue'
import UserRegister from '../views/user/user-register.vue'
import UserForgotPassword from '../views/user/user-forgot-password.vue'
import UserResetPassword from '../views/user/user-reset-password.vue'
import UserDashboard from '../views/user/user-dashboard.vue'
import UserProfile from '../views/user/user-profile.vue'
import UserPointsMall from '../views/user/user-points-mall.vue'
import UserSocial from '../views/user/user-social.vue'
import UserCheckin from '../views/user/user-checkin.vue'
import UserRanking from '../views/user/user-ranking.vue'
import UserClasses from '../views/user/user-classes.vue'
import UserMall from '../views/user/user-mall.vue'
import UserVipPayment from '../views/user/user-vip-payment.vue'
import AdminLogin from '../views/admin/admin-login.vue'
import AdminDashboard from '../views/admin/admin-dashboard.vue'
import AdminUsers from '../views/admin/admin-users.vue'
import AdminClasses from '../views/admin/admin-classes.vue'
import AdminProducts from '../views/admin/admin-products.vue'
import AdminCommunity from '../views/admin/admin-community.vue'
import CoachLogin from '../views/coach/coach-login.vue'
import CoachDashboard from '../views/coach/coach-dashboard.vue'
import CoachProfile from '../views/coach/coach-profile.vue'
import CoachClasses from '../views/coach/coach-classes.vue'
import CoachMailbox from '../views/coach/coach-mailbox.vue'
import GymCheckinScan from '../views/gym-checkin-scan.vue'
import { getToken, getUserType } from '../services/session'

const authGuard = ({ role, loginPath, fallbackPath }) => (to, from, next) => {
  if (!getToken()) return next(loginPath)
  if (getUserType() !== role) return next(fallbackPath)
  return next()
}

const requireUserAuth = authGuard({ role: 'user', loginPath: '/user/login', fallbackPath: '/admin' })
const requireAdminAuth = authGuard({ role: 'admin', loginPath: '/admin/login', fallbackPath: '/user/dashboard' })
const requireCoachAuth = authGuard({ role: 'coach', loginPath: '/coach/login', fallbackPath: '/user/dashboard' })

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/user/login'
    },
    {
      path: '/user/login',
      name: 'UserLogin',
      component: UserLogin
    },
    {
      path: '/user/register',
      name: 'UserRegister',
      component: UserRegister
    },
    {
      path: '/user/forgot-password',
      name: 'UserForgotPassword',
      component: UserForgotPassword
    },
    {
      path: '/user/reset-password',
      name: 'UserResetPassword',
      component: UserResetPassword
    },
    {
      path: '/gym-scan',
      name: 'GymCheckinScan',
      component: GymCheckinScan
    },
    {
      path: '/user/dashboard',
      name: 'UserDashboard',
      component: UserDashboard,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/profile',
      name: 'UserProfile',
      component: UserProfile,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/points-mall',
      name: 'UserPointsMall',
      component: UserPointsMall,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/mall',
      name: 'UserMall',
      component: UserMall,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/vip-payment',
      name: 'UserVipPayment',
      component: UserVipPayment,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/social',
      name: 'UserSocial',
      component: UserSocial,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/checkin',
      name: 'UserCheckin',
      component: UserCheckin,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/ranking',
      name: 'UserRanking',
      component: UserRanking,
      beforeEnter: requireUserAuth
    },
    {
      path: '/user/classes',
      name: 'UserClasses',
      component: UserClasses,
      beforeEnter: requireUserAuth
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: AdminLogin
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: AdminDashboard,
      beforeEnter: requireAdminAuth
    },
    {
      path: '/admin/users',
      name: 'AdminUsers',
      component: AdminUsers,
      beforeEnter: requireAdminAuth
    },
    {
      path: '/admin/classes',
      name: 'AdminClasses',
      component: AdminClasses,
      beforeEnter: requireAdminAuth
    },
    {
      path: '/admin/products',
      name: 'AdminProducts',
      component: AdminProducts,
      beforeEnter: requireAdminAuth
    },
    {
      path: '/admin/community',
      name: 'AdminCommunity',
      component: AdminCommunity,
      beforeEnter: requireAdminAuth
    },
    {
      path: '/coach/login',
      name: 'CoachLogin',
      component: CoachLogin
    },
    {
      path: '/coach/dashboard',
      name: 'CoachDashboard',
      component: CoachDashboard,
      beforeEnter: requireCoachAuth
    },
    {
      path: '/coach/profile',
      name: 'CoachProfile',
      component: CoachProfile,
      beforeEnter: requireCoachAuth
    },
    {
      path: '/coach/classes',
      name: 'CoachClasses',
      component: CoachClasses,
      beforeEnter: requireCoachAuth
    },
    {
      path: '/coach/mailbox',
      name: 'CoachMailbox',
      component: CoachMailbox,
      beforeEnter: requireCoachAuth
    }
  ]
})

export default router