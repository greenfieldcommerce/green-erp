<template>
  <div class="contractor-info-card">
    <div v-if="loading" class="loading">
      <p>Loading contractor information...</p>
    </div>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchContractorInfo">Retry</button>
    </div>

    <div v-if="!loading && !error && contractor" class="contractor-details">
      <div class="card-header">
        <h2>Your Profile</h2>
        <span class="badge">Contractor</span>
      </div>

      <div class="details-grid">
        <div class="detail-item">
          <label>Name</label>
          <p class="detail-value">{{ contractor.name }}</p>
        </div>

        <div class="detail-item">
          <label>Email</label>
          <p class="detail-value">{{ contractor.email }}</p>
        </div>

        <div class="detail-item">
          <label>Current Rate</label>
          <p class="detail-value">
            <span v-if="contractor.currentRate">
              {{ formatCurrency(contractor.currentRate.rate, contractor.currentRate.currency) }}
            </span>
            <span v-else class="no-rate">No active rate</span>
          </p>
        </div>

        <div v-if="contractor.currentRate" class="detail-item">
          <label>Currency</label>
          <p class="detail-value">{{ contractor.currentRate.currency }}</p>
        </div>
      </div>
    </div>

    <div v-if="!loading && !error && !contractor" class="no-data">
      <p>No contractor information available.</p>
    </div>
  </div>
</template>

<script>
import api from '../api'
import keycloak from '../keycloak'

export default {
  name: 'CurrentContractorInfo',
  data() {
    return {
      contractor: null,
      loading: false,
      error: null,
    }
  },
  mounted() {
    this.fetchContractorInfo()
  },
  methods: {
    async fetchContractorInfo() {
      this.loading = true
      this.error = null

      try {
        // Get contractor ID from Keycloak token
        const contractorId = keycloak.tokenParsed?.contractorId
        
        if (!contractorId) {
          this.error = 'Contractor ID not found in your profile.'
          this.loading = false
          return
        }

        // Fetch contractor details
        const response = await api.get(`/contractors/${contractorId}`)
        
        // Extract contractor from HAL+JSON response
        this.contractor = response.data
      } catch (err) {
        console.error('Failed to fetch contractor info:', err)
        if (err.response?.status === 403) {
          this.error = 'Access denied. You do not have permission to view this information.'
        } else if (err.response?.status === 404) {
          this.error = 'Contractor not found.'
        } else {
          this.error = 'Failed to load contractor information. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },
    formatCurrency(amount, currency) {
      if (!amount || !currency) return '—'
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency,
      }).format(amount)
    },
  },
}
</script>

<style scoped>
.contractor-info-card {
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

.no-rate {
  color: #999;
  font-style: italic;
}
</style>
