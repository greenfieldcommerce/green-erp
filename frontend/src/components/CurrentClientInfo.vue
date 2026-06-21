<template>
  <div class="client-info-card">
    <div v-if="loading" class="loading">
      <p>Loading client information...</p>
    </div>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchClientInfo">Retry</button>
    </div>

    <div v-if="!loading && !error && client" class="client-details">
      <div class="card-header">
        <h2>Profile</h2>
        <span class="badge">Client</span>
      </div>

      <div class="details-grid">
        <div class="detail-item">
          <label>Name</label>
          <p class="detail-value">{{ client.name }}</p>
        </div>

        <div class="detail-item">
          <label>Email</label>
          <p class="detail-value">{{ client.email }}</p>
        </div>
      </div>
    </div>

    <div v-if="!loading && !error && !client" class="no-data">
      <p>No client information available.</p>
    </div>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'CurrentClientInfo',
  props: {
    clientId: {
      type: [String, Number],
      default: null
    }
  },
  data() {
    return {
      client: null,
      loading: false,
      error: null,
    }
  },
  mounted() {
    this.fetchClientInfo()
  },
  watch: {
    clientId: {
      handler(newId, oldId) {
        // Only fetch if the ID actually changed and is not null
        if (newId && newId !== oldId) {
          this.fetchClientInfo()
        }
      },
      immediate: false // Don't run on initial mount since mounted() already handles it
    }
  },
  methods: {
    async fetchClientInfo() {
      this.loading = true
      this.error = null

      try {
        if (!this.clientId) {
          this.error = 'Client ID not found.'
          this.loading = false
          return
        }

        // Fetch client details
        const response = await api.get(`/clients/${this.clientId}`)
        
        // Extract client from HAL+JSON response
        this.client = response.data
      } catch (err) {
        console.error('Failed to fetch client info:', err)
        if (err.response?.status === 403) {
          this.error = 'Access denied. You do not have permission to view this information.'
        } else if (err.response?.status === 404) {
          this.error = 'Client not found.'
        } else {
          this.error = 'Failed to load client information. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },
  },
}
</script>

<style scoped>
.client-info-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.loading {
  text-align: center;
  color: #666;
  padding: 2rem 0;
  font-style: italic;
}

.error {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 8px;
  padding: 1rem;
  color: #c33;
  margin-bottom: 1rem;
}

.error button {
  margin-top: 0.5rem;
  padding: 0.4rem 1rem;
  background: #c33;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.error button:hover {
  background: #a22;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 2rem 0;
  font-style: italic;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.card-header h2 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.5rem;
}

.badge {
  background: #42b883;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-item label {
  font-size: 0.85rem;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.detail-value {
  font-size: 1.1rem;
  color: #2c3e50;
  margin: 0;
  font-weight: 500;
}
</style>
