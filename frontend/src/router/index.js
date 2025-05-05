import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/test',
    name: 'test',
    component: () => import('../views/TestView.vue')
  },
  {
    path: '/rider',
    name: 'rider',
    component: () => import('../views/RiderAppView.vue')
  },
  {
    path: '/customer',
    name: 'customer',
    component: () => import('../views/CustomerAppView.vue')
  },
  {
    path: '/store',
    name: 'store',
    component: () => import('../views/StoreAppView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
