<template>
  <div class="contractor-invoices-section">
    <div class="section-header">
      <h3>Open Contractor Invoices</h3>
      <span class="invoice-count">{{ invoiceCount }} invoices</span>
    </div>

    <!-- Date Filter -->
    <div class="filter-section">
      <label for="startDateBefore">Filter by Start Date Before:</label>
      <div class="filter-input-group">
        <input
          id="startDateBefore"
          v-model="filterDate"
          type="text"
          placeholder="YYYY-MM-DDTHH:mm:ss.sssZ (optional)"
          class="date-input"
          @keyup.enter="applyFilter"
        />
        <button
          @click="applyFilter"
          :disabled="loading"
          class="filter-btn"
        >
          {{ loading ? 'Loading...' : 'Apply Filter' }}
        </button>
        <button
          v-if="filterDate"
          @click="clearFilter"
          :disabled="loading"
          class="clear-filter-btn"
        >
          Clear
        </button>
      </div>
      <p class="filter-hint">Leave empty to use today's date. Format: YYYY-MM-DDTHH:mm:ss.sssZ</p>
    </div>

    <div v-if="loading" class="loading">
      <p>Loading contractor invoices...</p>
    </div>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchInvoices">Retry</button>
    </div>

    <div v-if="!loading && !error && invoices.length === 0" class="no-data">
      <p>No open contractor invoices found.</p>
    </div>

    <div v-if="!loading && !error && invoices.length > 0" class="invoices-table-wrapper">
      <table class="invoices-table">
        <thead>
          <tr>
            <th>Invoice ID</th>
            <th>Contractor ID</th>
            <th>Date Range</th>
            <th>Worked Days</th>
            <th>Currency</th>
            <th>Total Amount</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="invoice in invoices"
            :key="invoice.invoiceId"
            class="invoice-row"
            :class="{ 'selected': selectedInvoiceId === invoice.invoiceId }"
            @click="selectInvoice(invoice.invoiceId)"
          >
            <td class="invoice-id">{{ invoice.invoiceId }}</td>
            <td>{{ invoice.contractorId }}</td>
            <td class="date-range">{{ formatDateRange(invoice.startDate, invoice.endDate) }}</td>
            <td>{{ invoice.numberOfWorkedDays }}</td>
            <td>{{ invoice.currency }}</td>
            <td class="total-amount">{{ formatCurrency(invoice.total, invoice.currency) }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(invoice.status)">
                {{ invoice.status }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Invoice Details Panel -->
      <div v-if="selectedInvoice" class="invoice-details-panel">
        <div class="panel-header">
          <h4>Invoice Details</h4>
          <button class="close-btn" @click="clearSelection">×</button>
        </div>

        <div class="detail-content">
          <div class="detail-row">
            <span class="label">Invoice ID:</span>
            <span class="value">{{ selectedInvoice.invoiceId }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Contractor ID:</span>
            <span class="value">{{ selectedInvoice.contractorId }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Status:</span>
            <span class="status-badge" :class="getStatusClass(selectedInvoice.status)">
              {{ selectedInvoice.status }}
            </span>
          </div>

          <div class="detail-row">
            <span class="label">Date Range:</span>
            <span class="value">{{ formatDateRange(selectedInvoice.startDate, selectedInvoice.endDate) }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Worked Days:</span>
            <span class="value">{{ selectedInvoice.numberOfWorkedDays }}</span>
          </div>

          <div class="detail-row">
            <span class="label">Currency:</span>
            <span class="value">{{ selectedInvoice.currency }}</span>
          </div>

          <div v-if="selectedInvoice.extraAmountLines && selectedInvoice.extraAmountLines.length > 0" class="extra-amounts">
            <h5>Extra Amounts</h5>
            <div
              v-for="line in selectedInvoice.extraAmountLines"
              :key="line.id"
              class="extra-line"
            >
              <span class="line-description">{{ line.description }}</span>
              <span class="line-amount">{{ formatCurrency(line.amount, selectedInvoice.currency) }}</span>
            </div>
          </div>

          <div class="total-row">
            <span class="label">Total Amount:</span>
            <span class="value total">{{ formatCurrency(selectedInvoice.total, selectedInvoice.currency) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'ClientContractorInvoices',
  props: {
    clientId: {
      type: [String, Number],
      default: null
    }
  },
  data() {
    return {
      invoices: [],
      selectedInvoiceId: null,
      selectedInvoice: null,
      loading: false,
      error: null,
      filterDate: '',
    }
  },
  computed: {
    invoiceCount() {
      return this.invoices.length
    }
  },
  mounted() {
    this.fetchInvoices()
  },
  watch: {
    clientId: {
      handler(newId, oldId) {
        if (newId && newId !== oldId) {
          this.resetFilters()
          this.fetchInvoices()
        }
      },
      immediate: false
    }
  },
  methods: {
    async fetchInvoices() {
      this.loading = true
      this.error = null
      this.selectedInvoiceId = null
      this.selectedInvoice = null

      try {
        if (!this.clientId) {
          this.error = 'Client ID not found.'
          this.loading = false
          return
        }

        // Build query parameters
        const params = new URLSearchParams()
        if (this.filterDate) {
          params.append('startDateBefore', this.filterDate)
        }

        const url = `/clients/${this.clientId}/contractor-invoices${params.toString() ? '?' + params.toString() : ''}`
        const response = await api.get(url)

         // Extract invoices from API response
         if (response.data && response.data.invoices) {
           this.invoices = response.data.invoices
         } else {
           this.invoices = []
         }
      } catch (err) {
        console.error('Failed to fetch contractor invoices:', err)
        if (err.response?.status === 403) {
          this.error = 'Access denied. You do not have permission to view contractor invoices.'
        } else if (err.response?.status === 404) {
          this.error = 'Client not found.'
        } else {
          this.error = 'Failed to load contractor invoices. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },

    async selectInvoice(invoiceId) {
      this.selectedInvoiceId = invoiceId

      try {
        const params = new URLSearchParams()
        if (this.filterDate) {
          params.append('startDateBefore', this.filterDate)
        }

        const url = `/clients/${this.clientId}/contractor-invoices?${params.toString()}`
        const response = await api.get(url)

        // Find the selected invoice from the response
        if (response.data && response.data.invoices) {
          this.selectedInvoice = response.data.invoices.find(
            inv => inv.invoiceId === invoiceId
          ) || null
        }
      } catch (err) {
        console.error('Failed to fetch invoice details:', err)
        this.error = 'Failed to load invoice details. Please try again.'
      }
    },

    clearSelection() {
      this.selectedInvoiceId = null
      this.selectedInvoice = null
    },

    applyFilter() {
      this.fetchInvoices()
    },

    clearFilter() {
      this.filterDate = ''
      this.fetchInvoices()
    },

    resetFilters() {
      this.filterDate = ''
    },

    formatDateRange(startDate, endDate) {
      if (!startDate || !endDate) return '—'

      const start = new Date(startDate)
      const end = new Date(endDate)

      const startStr = start.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
      const endStr = end.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })

      return `${startStr} - ${endStr}`
    },

    formatCurrency(amount, currency) {
      if (amount === null || amount === undefined || !currency) return '—'
      try {
        return new Intl.NumberFormat('en-US', {
          style: 'currency',
          currency: currency,
        }).format(amount)
      } catch (err) {
        return amount
      }
    },

    getStatusClass(status) {
      if (!status) return ''
      const statusLower = status.toLowerCase()
      switch (statusLower) {
        case 'open':
          return 'status-open'
        case 'billed':
          return 'status-billed'
        case 'closed':
          return 'status-closed'
        default:
          return ''
      }
    },
  },
}
</script>

<style scoped>
.contractor-invoices-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 2rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h3 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.3rem;
}

.invoice-count {
  background: #42b883;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

/* Filter Section */
.filter-section {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.filter-section label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.filter-input-group {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.date-input {
  flex: 1;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  font-family: monospace;
}

.date-input:focus {
  outline: none;
  border-color: #42b883;
  box-shadow: 0 0 0 2px rgba(66, 184, 131, 0.1);
}

.filter-btn {
  background: #42b883;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.filter-btn:hover:not(:disabled) {
  background: #369870;
}

.filter-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.clear-filter-btn {
  background: #f0f0f0;
  color: #2c3e50;
  border: 1px solid #ddd;
  padding: 0.6rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.2s;
}

.clear-filter-btn:hover:not(:disabled) {
  background: #e0e0e0;
  border-color: #ccc;
}

.clear-filter-btn:disabled {
  background: #f0f0f0;
  color: #999;
  cursor: not-allowed;
}

.filter-hint {
  font-size: 0.8rem;
  color: #666;
  margin: 0;
  font-style: italic;
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

.invoices-table-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-top: 1rem;
}

.invoices-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.invoices-table th,
.invoices-table td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 0.9rem;
}

.invoices-table th {
  background: #42b883;
  color: white;
  font-weight: 600;
}

.invoices-table tbody tr {
  cursor: pointer;
  transition: background-color 0.2s;
}

.invoices-table tbody tr:hover {
  background: #f9f9f9;
}

.invoices-table tr.selected {
  background-color: #e8f5e9;
  border-left: 4px solid #42b883;
}

.invoices-table tr.selected:hover {
  background-color: #e8f5e9;
}

.invoice-id {
  font-weight: 600;
  color: #2c3e50;
}

.date-range {
  white-space: nowrap;
}

.total-amount {
  color: #42b883;
  font-weight: 600;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-open {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-billed {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeeba;
}

.status-closed {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* Invoice Details Panel */
.invoice-details-panel {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1.5rem;
  height: fit-content;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.panel-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #666;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #e9ecef;
}

.detail-content {
  display: grid;
  gap: 1rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 0.85rem;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}

.value {
  font-size: 1rem;
  color: #2c3e50;
  font-weight: 500;
}

.total {
  color: #42b883;
  font-weight: 700;
  font-size: 1.1rem;
}

.extra-amounts {
  border-top: 1px solid #e9ecef;
  padding-top: 1rem;
  margin-top: 1rem;
}

.extra-amounts h5 {
  margin: 0 0 0.75rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.extra-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  margin-bottom: 0.5rem;
  background: #fff;
}

.line-description {
  color: #666;
  font-size: 0.9rem;
  flex: 1;
}

.line-amount {
  font-weight: 600;
  color: #2c3e50;
  min-width: 100px;
  text-align: right;
}

.total-row {
  border-top: 2px solid #e9ecef;
  padding-top: 1rem;
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 1024px) {
  .invoices-table-wrapper {
    grid-template-columns: 1fr;
  }
}
</style>
