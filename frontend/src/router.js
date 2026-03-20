import { createRouter, createWebHistory } from 'vue-router'
import ContractorsList from './views/ContractorsList.vue'

const routes = [
  {
    path: '/',
    redirect: '/contractors',
  },
  {
    path: '/contractors',
    name: 'Contractors',
    component: ContractorsList,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
