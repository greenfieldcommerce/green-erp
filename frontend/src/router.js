import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import ContractorsList from './views/ContractorsList.vue'
import ClientsList from './views/ClientsList.vue'
import keycloak from './keycloak'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/contractors',
    name: 'Contractors',
    component: ContractorsList,
    meta: { requiresAdmin: true },
  },
  {
    path: '/clients',
    name: 'Clients',
    component: ClientsList,
    meta: { requiresAdmin: true },
  },
  {
    path: '/access-denied',
    name: 'AccessDenied',
    component: {
      template: `
        <div class="access-denied-page">
          <h1>Access Denied</h1>
          <p>You do not have permission to view this page. Only administrators can access the Contractors section.</p>
          <router-link to="/">Go Home</router-link>
        </div>
      `,
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Route guard to check admin access
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAdmin) {
    const isAdmin = keycloak.tokenParsed?.realm_access?.roles?.includes('ROLE_ADMIN') ?? false
    if (!isAdmin) {
      next('/access-denied')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
