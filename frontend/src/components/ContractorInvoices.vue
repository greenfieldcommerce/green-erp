<template>
  <div class="invoices-section">
    <div class="section-header">
      <h3>Recent Invoices</h3>
      <span class="invoice-count">{{ invoiceCount }} invoices</span>
    </div>

    <div v-if="loading" class="loading">
      <p>Loading invoices...</p>
    </div>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchInvoices">Retry</button>
    </div>

    <div v-if="!loading && !error && invoices.length === 0" class="no-data">
      <p>No invoices found for this contractor.</p>
    </div>

    <div v-if="!loading && !error && invoices.length > 0" class="invoices-container">
      <div class="invoices-list">
        <div
          v-for="invoice in invoices"
          :key="invoice.invoiceId"
          class="invoice-item"
          :class="{ 'selected': selectedInvoiceId === invoice.invoiceId }"
          @click="selectInvoice(invoice.invoiceId)"
        >
          <div class="invoice-header">
            <span class="invoice-number">Invoice #{{ invoice.invoiceId }}</span>
            <span class="invoice-date">{{ formatDateRange(invoice.startDate, invoice.endDate) }}</span>
          </div>
          <div class="invoice-details">
            <div class="detail-row">
              <span class="label">Worked Days:</span>
              <span class="value">{{ invoice.numberOfWorkedDays }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Total:</span>
              <span class="value total">{{ formatCurrency(invoice.total, invoice.currency) }}</span>
            </div>
            <div v-if="invoice.extraAmountLines && invoice.extraAmountLines.length > 0" class="detail-row">
              <span class="label">Extra Amounts:</span>
              <span class="value">{{ invoice.extraAmountLines.length }} item(s)</span>
            </div>
          </div>
        </div>

        <div v-if="hasNextPage && invoices.length > 0" class="load-more-section">
          <button
              @click="loadMoreInvoices"
              :disabled="loadingMore || loading"
              class="load-more-btn"
          >
            {{ loadingMore ? 'Loading...' : 'Load More Invoices' }}
          </button>
        </div>

      </div>

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

      <div v-else-if="invoices.length > 0" class="invoice-details-panel select-prompt-panel">
        <div class="panel-header">
          <h4>Invoice Details</h4>
        </div>

        <div class="select-prompt-content">
          <p>Select an invoice from the list to view details</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'ContractorInvoices',
  props: {
    contractorId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      invoices: [],
      selectedInvoiceId: null,
      selectedInvoice: null,
      loading: false,
      error: null,
      // Pagination state
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      hasNextPage: false,
      loadingMore: false,
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
  methods: {
    async fetchInvoices(page = 0) {
      this.loading = true
      this.error = null

      try {
        const response = await api.get(`/contractors/${this.contractorId}/invoices?page=${page}&size=6`)

        // Extract content and pagination metadata from HAL+JSON response
        if (response.data && response.data._embedded && response.data._embedded.invoices) {
          if (page === 0) {
            // Initial load - replace existing invoices
            this.invoices = response.data._embedded.invoices
          } else {
            // Load more - append to existing invoices
            this.invoices.push(...response.data._embedded.invoices)
          }

          // Update pagination metadata
          this.updatePaginationMetadata(response.data)

          // Auto-select first invoice if this is initial load and no invoice is selected
          if (page === 0 && this.invoices.length > 0 && !this.selectedInvoiceId) {
            this.selectInvoice(this.invoices[0].invoiceId)
          }
        } else {
          if (page === 0) {
            this.invoices = []
          }
          this.updatePaginationMetadata({ page: { totalElements: 0, totalPages: 0, number: 0 } })
        }
      } catch (err) {
        console.error('Failed to fetch invoices:', err)
        if (err.response?.status === 403) {
          this.error = 'Access denied. You do not have permission to view these invoices.'
        } else if (err.response?.status === 404) {
          this.error = 'Contractor not found.'
        } else {
          this.error = 'Failed to load invoices. Please try again.'
        }
      } finally {
        this.loading = false
        this.loadingMore = false
      }
    },

    updatePaginationMetadata(responseData) {
      // Extract pagination info from HAL+JSON response
      if (responseData.page) {
        this.currentPage = responseData.page.number
        this.totalPages = responseData.page.totalPages
        this.totalElements = responseData.page.totalElements
        this.hasNextPage = responseData.page.number < responseData.page.totalPages - 1
      } else {
        // Fallback if page metadata is not available
        this.hasNextPage = false
      }
    },

    async refreshInvoices() {
      // Reset pagination state and fetch first page
      this.currentPage = 0
      this.hasNextPage = false
      this.selectedInvoiceId = null
      this.selectedInvoice = null
      await this.fetchInvoices(0)
    },

    async loadMoreInvoices() {
      if (this.loadingMore || !this.hasNextPage || this.loading) return

      this.loadingMore = true
      await this.fetchInvoices(this.currentPage + 1)
    },

    async selectInvoice(invoiceId) {
      this.selectedInvoiceId = invoiceId
      this.selectedInvoice = null

      try {
        const response = await api.get(`/contractors/${this.contractorId}/invoices/${invoiceId}`)
        this.selectedInvoice = response.data
      } catch (err) {
        console.error('Failed to fetch invoice details:', err)
        this.error = 'Failed to load invoice details. Please try again.'
      }
    },

    clearSelection() {
      this.selectedInvoiceId = null
      this.selectedInvoice = null
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
.invoices-section {
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

.invoices-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.invoices-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.invoice-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fafafa;
}

.invoice-item:hover {
  border-color: #42b883;
  background: #f0fff7;
  transform: translateY(-1px);
}

.invoice-item.selected {
  border-color: #42b883;
  background: #f0fff7;
  box-shadow: 0 2px 8px rgba(66, 184, 131, 0.2);
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.invoice-number {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.1rem;
}

.invoice-date {
  font-size: 0.85rem;
  color: #666;
}

.invoice-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

.value {
  font-weight: 600;
  color: #2c3e50;
}

.total {
  color: #42b883;
  font-weight: 700;
}

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

.extra-amounts {
  border-top: 1px solid #e9ecef;
  padding-top: 1rem;
}

.extra-amounts h5 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.extra-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e9ecef;
}

.extra-line:last-child {
  border-bottom: none;
}

.line-description {
  color: #666;
  font-size: 0.9rem;
}

.line-amount {
  font-weight: 600;
  color: #2c3e50;
}

.total-row {
  border-top: 2px solid #e9ecef;
  padding-top: 1rem;
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.select-prompt-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.select-prompt-content {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 2rem;
}

.select-prompt-content p {
  margin: 0;
  font-size: 1.1rem;
  line-height: 1.5;
}

.select-prompt {
  grid-column: 1 / -1;
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 2rem;
  border: 2px dashed #e9ecef;
  border-radius: 8px;
  background: #fafafa;
}

.load-more-section {
  grid-column: 1 / -1;
  text-align: center;
  margin-top: 1rem;
}

.load-more-btn {
  padding: 0.75rem 2rem;
  background: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 200px;
}

.load-more-btn:hover:not(:disabled) {
  background: #e9ecef;
  border-color: #ced4da;
  transform: translateY(-1px);
}

.load-more-btn:disabled {
  background: #e9ecef;
  color: #6c757d;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.load-more-btn:active:not(:disabled) {
  transform: translateY(0);
}

/* Mobile responsive */
@media (max-width: 768px) {
  .invoices-container {
    grid-template-columns: 1fr;
  }

  .invoice-details {
    grid-template-columns: 1fr;
  }

  .invoice-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .load-more-btn {
    width: 100%;
  }
}
</style>