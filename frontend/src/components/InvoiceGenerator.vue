<template>
  <div class="invoice-generator-section">
    <div class="section-header">
      <h3>Generate Invoice</h3>
      <span class="period-badge">{{ currentMonthPeriod }}</span>
    </div>

    <div v-if="loading" class="loading">
      <p>Creating invoice...</p>
    </div>

    <div v-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="clearError">Close</button>
    </div>

    <div v-if="success" class="success">
      <div class="success-icon">✓</div>
      <div class="success-content">
        <h4>Invoice Created Successfully!</h4>
        <p>
          Invoice #{{ success.invoiceId }} generated for {{ currentMonthPeriod }}
        </p>
        <p class="success-total">
          Total: {{ formatCurrency(success.total, success.currency) }}
        </p>
        <button @click="resetForm" class="success-btn">Create Another Invoice</button>
      </div>
    </div>

    <div v-if="!loading && !error && !success" class="generator-form">
      <div class="form-group">
        <label for="workedDays">Number of Worked Days</label>
        <input
          id="workedDays"
          type="number"
          v-model.number="workedDays"
          :min="0"
          :max="31"
          step="0.5"
          class="days-input"
          placeholder="Days worked (0-31)"
          @input="validateInput"
        />
        <div v-if="validationError" class="error-message">{{ validationError }}</div>
      </div>

      <div class="estimate-section">
        <h4>Invoice Estimate</h4>
        <div class="estimate-details">
          <div class="estimate-row">
            <span class="label">Daily Rate:</span>
            <span class="value">{{ formatCurrency(dailyRate, currency) }}</span>
          </div>
          <div class="estimate-row">
            <span class="label">Worked Days:</span>
            <span class="value">{{ workedDays || 0 }}</span>
          </div>
          <div class="estimate-row total-row">
            <span class="label">Estimated Total:</span>
            <span class="value total">{{ formatCurrency(estimatedTotal, currency) }}</span>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button 
          @click="generateInvoice" 
          :disabled="!isFormValid || loading"
          class="generate-btn"
        >
          Generate Invoice
        </button>
        <button @click="resetForm" class="reset-btn">Reset</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'InvoiceGenerator',
  props: {
    contractorId: {
      type: Number,
      required: true
    },
    currentRate: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      workedDays: null,
      loading: false,
      error: null,
      success: null,
      validationError: null,
    }
  },
  computed: {
    dailyRate() {
      return this.currentRate?.rate || 0
    },
    currency() {
      return this.currentRate?.currency || 'USD'
    },
    estimatedTotal() {
      if (!this.workedDays || !this.dailyRate) return 0
      return this.workedDays * this.dailyRate
    },
    currentMonthPeriod() {
      const now = new Date()
      const year = now.getFullYear()
      const month = now.toLocaleDateString('en-US', { month: 'long' })
      const daysInMonth = new Date(year, now.getMonth() + 1, 0).getDate()
      return `${month} 1-${daysInMonth}, ${year}`
    },
    isFormValid() {
      return this.workedDays !== null && 
             this.workedDays >= 0 && 
             this.workedDays <= 31 && 
             this.workedDays !== '' &&
             !this.validationError
    }
  },
  methods: {
    validateInput() {
      this.validationError = null
      
      if (this.workedDays === '' || this.workedDays === null) {
        this.validationError = 'Please enter the number of worked days.'
        return
      }

      const days = Number(this.workedDays)
      
      if (isNaN(days)) {
        this.validationError = 'Please enter a valid number.'
        return
      }

      if (days < 0) {
        this.validationError = 'Worked days cannot be negative.'
        return
      }

      if (days > 31) {
        this.validationError = 'Worked days cannot exceed 31.'
        return
      }

      // Check if it's a reasonable decimal (allowing half-days)
      if (!Number.isInteger(days) && !Number.isInteger(days * 2)) {
        this.validationError = 'Please enter whole days or half-days (e.g., 1.5).'
        return
      }
    },
    
    async generateInvoice() {
      this.validateInput()
      
      if (!this.isFormValid) {
        return
      }

      this.loading = true
      this.error = null
      this.success = null

      try {
        const response = await api.post(`/contractors/${this.contractorId}/invoices`, {
          numberOfWorkedDays: this.workedDays
        })

        const createdInvoice = response.data
        
        this.success = {
          invoiceId: createdInvoice.invoiceId,
          total: createdInvoice.total,
          currency: createdInvoice.currency
        }
        
        // Emit event to notify parent that invoice was created
        this.$emit('invoice-created', createdInvoice)
        
        // Reset form after successful creation
        this.workedDays = null
      } catch (err) {
        console.error('Failed to create invoice:', err)
        if (err.response?.status === 400) {
          // Try to use the specific message from API response
          if (err.response.data?.message) {
            this.error = err.response.data.message
          } else {
            this.error = 'Invalid data. Please check your input and try again.'
          }
        } else if (err.response?.status === 403) {
          this.error = 'Access denied. You do not have permission to create invoices.'
        } else if (err.response?.status === 404) {
          this.error = 'Contractor not found.'
        } else if (err.response?.status === 409) {
          // Also check for message in 409 errors
          if (err.response.data?.message) {
            this.error = err.response.data.message
          } else {
            this.error = 'An invoice for this period already exists.'
          }
        } else {
          this.error = 'Failed to create invoice. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },
    
    resetForm() {
      this.workedDays = null
      this.error = null
      this.success = null
      this.validationError = null
    },
    
    clearError() {
      this.error = null
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
.invoice-generator-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 2rem;
  border: 2px solid #e8f5e9;
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

.period-badge {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  border: 1px solid #c8e6c9;
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
  display: flex;
  align-items: center;
  gap: 1rem;
}

.error p {
  margin: 0;
  flex: 1;
}

.error button {
  margin: 0;
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

.success {
  background: #e8f5e9;
  border: 1px solid #c8e6c9;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.success-icon {
  font-size: 2rem;
  color: #2e7d32;
  font-weight: bold;
  background: #c8e6c9;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-content h4 {
  margin: 0 0 0.5rem 0;
  color: #2e7d32;
  font-size: 1.2rem;
}

.success-content p {
  margin: 0 0 0.5rem 0;
  color: #388e3c;
}

.success-total {
  font-weight: 700;
  font-size: 1.1rem;
}

.success-btn {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background: #2e7d32;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.success-btn:hover {
  background: #1b5e20;
}

.generator-form {
  display: grid;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2c3e50;
}

.days-input {
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
  width: 200px;
}

.days-input:focus {
  outline: none;
  border-color: #42b883;
  box-shadow: 0 0 0 3px rgba(66, 184, 131, 0.1);
}

.error-message {
  color: #c33;
  font-size: 0.85rem;
  font-weight: 500;
}

.estimate-section {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1.5rem;
}

.estimate-section h4 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.estimate-details {
  display: grid;
  gap: 0.5rem;
}

.estimate-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e9ecef;
}

.estimate-row:last-child {
  border-bottom: none;
}

.estimate-row .label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.estimate-row .value {
  font-weight: 600;
  color: #2c3e50;
}

.total-row {
  border-top: 2px solid #e9ecef;
  padding-top: 0.5rem;
  margin-top: 0.5rem;
}

.total {
  color: #42b883;
  font-weight: 700;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-start;
}

.generate-btn {
  padding: 0.75rem 2rem;
  background: #42b883;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 150px;
}

.generate-btn:hover:not(:disabled) {
  background: #369870;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(66, 184, 131, 0.3);
}

.generate-btn:disabled {
  background: #b0bec5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.reset-btn {
  padding: 0.75rem 2rem;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-btn:hover {
  background: #e0e0e0;
  border-color: #ccc;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .success {
    flex-direction: column;
    text-align: center;
  }
  
  .days-input {
    width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>