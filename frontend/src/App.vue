<template>
  <div id="app">
    <header class="app-header">
      <div class="header-content">
        <router-link to="/" class="brand">
          <span class="logo">🌿</span>
          <span class="title">GreenERP</span>
        </router-link>
        <nav class="nav-links">
          <router-link v-if="isAdmin" to="/contractors">Contractors</router-link>
          <router-link v-if="isAdmin" to="/clients">Clients</router-link>
        </nav>
        <div class="user-info">
          <span class="username">{{ username }}</span>
          <button class="logout-btn" @click="logout">Logout</button>
        </div>
      </div>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<script>
import keycloak from './keycloak'

export default {
  name: 'App',
  computed: {
    username() {
      return keycloak.tokenParsed?.preferred_username ?? 'User'
    },
    isAdmin() {
      return keycloak.tokenParsed?.realm_access?.roles?.includes('ROLE_ADMIN') ?? false
    },
  },
  methods: {
    logout() {
      keycloak.logout({ redirectUri: window.location.origin })
    },
  },
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, sans-serif;
  background: #f5f5f5;
  color: #333;
}

.app-header {
  background: #2c3e50;
  color: white;
  padding: 0 1.5rem;
  height: 56px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.header-content {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: white;
  padding: 0.5rem;
  border-radius: 4px;
  transition: all 0.2s;
  cursor: pointer;
}

.brand:hover {
  background: rgba(255, 255, 255, 0.15);
}

.brand .logo {
  font-size: 1.5rem;
}

.brand .title {
  font-size: 1.25rem;
  font-weight: 700;
}

.nav-links {
  margin-left: 2rem;
  display: flex;
  gap: 1rem;
}

.nav-links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: white;
  background: rgba(255, 255, 255, 0.15);
}

.user-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  font-size: 0.9rem;
  opacity: 0.9;
}

.logout-btn {
  padding: 0.35rem 0.9rem;
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.7);
}

main {
  padding: 1rem;
}
</style>
