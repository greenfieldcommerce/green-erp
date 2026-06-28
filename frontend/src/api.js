import axios from 'axios'
import keycloak from './keycloak'

const api = axios.create({
  baseURL: '/api',
  headers: {
    Accept: 'application/json',
  },
})

// Attach the Keycloak token to every request
api.interceptors.request.use(async (config) => {
  try {
    // Refresh token if it expires within 30 seconds
    await keycloak.updateToken(30)
  } catch {
    keycloak.login()
    return Promise.reject(new Error('Token refresh failed'))
  }
  config.headers.Authorization = `Bearer ${keycloak.token}`
  return config
})

export default api
