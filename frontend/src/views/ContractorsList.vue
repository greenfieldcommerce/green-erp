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
          <th>Current Daily Rate</th>
          <th>Currency</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr 
          v-for="contractor in contractors" 
          :key="contractor.id"
          :class="{ 'selected': selectedContractorId === contractor.id }"
          @click="selectContractor(contractor.id)"
        >
          <td>{{ contractor.id }}</td>
          <td>{{ contractor.name }}</td>
          <td>{{ contractor.email }}</td>
          <td>{{ contractor.currentRate?.rate ?? '—' }}</td>
          <td>{{ contractor.currentRate?.currency ?? '—' }}</td>
          <td>
            <button 
              class="view-details-btn"
              @click.stop="selectContractor(contractor.id)"
            >
              View Details
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-if="!loading && !error && !contractors.length" class="empty">
      No contractors found.
    </p>

    <!-- Contractor Details Section -->
    <div v-if="selectedContractorId" class="details-section">
      <div class="details-header">
        <h2>Contractor Details</h2>
        <button class="close-details-btn" @click="closeDetails">
          Close Details
        </button>
      </div>
      
      <CurrentContractorInfo 
        :contractor-id="selectedContractorId"
        @close-details="closeDetails"
      />
    </div>
  </div>
</template>

<script>
import api from '../api'
import CurrentContractorInfo from '../components/CurrentContractorInfo.vue'

export default {
  name: 'ContractorsList',
  components: {
    CurrentContractorInfo,
  },
  data() {
    return {
      contractors: [],
      loading: false,
      error: null,
      selectedContractorId: null,
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
    selectContractor(contractorId) {
      this.selectedContractorId = contractorId
    },
    closeDetails() {
      this.selectedContractorId = null
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

/* Table row selection styling */
.contractors-table tr.selected {
  background-color: #e8f5e9;
  border-left: 4px solid #42b883;
}

.contractors-table tr.selected:hover {
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
.contractors-table tbody tr {
  cursor: pointer;
}

.contractors-table tbody tr td:not(:last-child) {
  user-select: none;
}
</style>
