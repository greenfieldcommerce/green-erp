<template>
  <div class="contractors-page">
    <h1>Contractors</h1>

    <div v-if="loading" class="loading">Loading contractors...</div>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchContractors">Retry</button>
    </div>

    <table v-if="!loading && !error && contractors.length" class="contractors-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Email</th>
          <th>Current Rate</th>
          <th>Currency</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="contractor in contractors" :key="contractor.id">
          <td>{{ contractor.id }}</td>
          <td>{{ contractor.name }}</td>
          <td>{{ contractor.email }}</td>
          <td>{{ contractor.currentRate?.rate ?? '—' }}</td>
          <td>{{ contractor.currentRate?.currency ?? '—' }}</td>
        </tr>
      </tbody>
    </table>

    <p v-if="!loading && !error && !contractors.length" class="empty">
      No contractors found.
    </p>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'ContractorsList',
  data() {
    return {
      contractors: [],
      loading: false,
      error: null,
    }
  },
  mounted() {
    this.fetchContractors()
  },
  methods: {
    async fetchContractors() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/contractors')
        // HAL+JSON response: contractors are in _embedded.contractors
        this.contractors = response.data._embedded?.contractors ?? []
      } catch (err) {
        console.error('Failed to fetch contractors:', err)
        if (err.response?.status === 403) {
          this.error = 'Access denied. You need ADMIN role to view contractors.'
        } else {
          this.error = 'Failed to load contractors. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },
  },
}
</script>

<style scoped>
.contractors-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.loading {
  color: #666;
  font-style: italic;
  padding: 2rem 0;
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
}

.error button:hover {
  background: #a22;
}

.contractors-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.contractors-table th,
.contractors-table td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.contractors-table th {
  background: #42b883;
  color: white;
  font-weight: 600;
}

.contractors-table tr:hover {
  background: #f9f9f9;
}

.empty {
  color: #999;
  font-style: italic;
  padding: 2rem 0;
}
</style>
