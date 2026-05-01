import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/', component: () => import('@/views/Home.vue') },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  { path: '/product/:id', component: () => import('@/views/ProductDetail.vue') },
  { path: '/seller/:id', component: () => import('@/views/SellerProfile.vue') },
  { path: '/publish', component: () => import('@/views/Publish.vue'), meta: { auth: true } },
  { path: '/edit/:id', component: () => import('@/views/Publish.vue'), meta: { auth: true } },
  { path: '/chat', component: () => import('@/views/Chat.vue'), meta: { auth: true } },
  { path: '/chat/:userId', component: () => import('@/views/ChatWindow.vue'), meta: { auth: true } },
  { path: '/favorites', component: () => import('@/views/Favorite.vue'), meta: { auth: true } },
  { path: '/orders', component: () => import('@/views/Order.vue'), meta: { auth: true } },
  { path: '/profile', component: () => import('@/views/Profile.vue'), meta: { auth: true } },
  { path: '/admin', component: () => import('@/views/admin/Dashboard.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/users', component: () => import('@/views/admin/Users.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/products', component: () => import('@/views/admin/Products.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/orders', component: () => import('@/views/admin/Orders.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/reports', component: () => import('@/views/admin/Reports.vue'), meta: { auth: true, admin: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.isLoggedIn) {
    return next('/login')
  }
  if (to.meta.admin && !userStore.isAdmin) {
    return next('/')
  }
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    return next('/')
  }
  next()
})

export default router
