<template>
  <div class="clients-page">
    <h1>Clients</h1>

    <div v-if="loading" class="loading">Loading clients...</div>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchClients">Retry</button>
    </div>

    <table v-if="!loading && !error && clients.length" class="clients-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Email</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr 
          v-for="client in clients" 
          :key="client.id"
          :class="{ 'selected': selectedClientId === client.id }"
          @click="selectClient(client.id)"
        >
          <td>{{ client.id }}</td>
          <td>{{ client.name }}</td>
          <td>{{ client.email }}</td>
          <td>
            <button 
              class="view-details-btn"
              @click.stop="selectClient(client.id)"
            >
              View Details
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-if="!loading && !error && !clients.length" class="empty">
      No clients found.
    </p>

    <!-- Client Details Section -->
    <div v-if="selectedClientId" class="details-section">
      <div class="details-header">
        <h2>Client Details</h2>
        <button class="close-details-btn" @click="closeDetails">
          Close Details
        </button>
      </div>
      
      <CurrentClientInfo 
        :client-id="selectedClientId"
        @close-details="closeDetails"
      />
    </div>
  </div>
</template>

<script>
import api from '../api'
import CurrentClientInfo from '../components/CurrentClientInfo.vue'

export default {
  name: 'ClientsList',
  components: {
    CurrentClientInfo,
  },
  data() {
    return {
      clients: [],
      loading: false,
      error: null,
      selectedClientId: null,
    }
  },
  mounted() {
    this.fetchClients()
  },
  methods: {
    async fetchClients() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/clients')
        // HAL+JSON response: clients are in _embedded.clients
        this.clients = response.data._embedded?.clients ?? []
      } catch (err) {
        console.error('Failed to fetch clients:', err)
        if (err.response?.status === 403) {
          this.error = 'Access denied. You need ADMIN role to view clients.'
        } else {
          this.error = 'Failed to load clients. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },
    selectClient(clientId) {
      this.selectedClientId = clientId
    },
    closeDetails() {
      this.selectedClientId = null
    },
  },
}
</script>

<style scoped>
.clients-page {
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

.clients-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.clients-table th,
.clients-table td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.clients-table th {
  background: #42b883;
  color: white;
  font-weight: 600;
}

.clients-table tr:hover {
  background: #f9f9f9;
}

.empty {
  color: #999;
  font-style: italic;
  padding: 2rem 0;
}

/* Table row selection styling */
.clients-table tr.selected {
  background-color: #e8f5e9;
  border-left: 4px solid #42b883;
}

.clients-table tr.selected:hover {
  background-color: #e8f5e9;
}

/* View Details button styling */
.view-details-btn {
  background: #42b883;
  color: white;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background-color 0.2s;
}

.view-details-btn:hover {
  background: #369870;
}

/* Details section styling */
.details-section {
  margin-top: 2rem;
  border-top: 2px solid #42b883;
  padding-top: 2rem;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.details-header h2 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.5rem;
}

.close-details-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.close-details-btn:hover {
  background: #5a6268;
}

/* Make table rows clickable */
.clients-table tbody tr {
  cursor: pointer;
}

.clients-table tbody tr td:not(:last-child) {
  user-select: none;
}
</style>
