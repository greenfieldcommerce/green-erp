<template>
  <div class="home-page">
    <div class="welcome-section">
      <h1>Welcome to GreenERP</h1>
      <p class="subtitle">Sustainable Enterprise Resource Planning</p>
    </div>

    <CurrentContractorInfo />

    <div class="info-section">
      <h2>About GreenERP</h2>
      <p>
        GreenERP is a modern, sustainable enterprise resource planning system designed to help
        businesses streamline their operations and make data-driven decisions. Our platform
        combines powerful features with an intuitive user interface to support your business growth.
      </p>
    </div>
  </div>
</template>

<script>
import CurrentContractorInfo from '../components/CurrentContractorInfo.vue'

export default {
  name: 'Home',
  components: {
    CurrentContractorInfo,
  },
}
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.welcome-section {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem 0;
}

.welcome-section h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.subtitle {
  font-size: 1.2rem;
  color: #666;
  font-style: italic;
}

.content-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.card h2 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
}

.card p {
  color: #666;
  line-height: 1.6;
}

.info-section {
  background: #f9f9f9;
  border-left: 4px solid #42b883;
  padding: 2rem;
  border-radius: 4px;
}

.info-section h2 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.info-section p {
  color: #555;
  line-height: 1.8;
  font-size: 1rem;
}
</style>
