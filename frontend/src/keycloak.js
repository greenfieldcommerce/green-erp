import Keycloak from 'keycloak-js'

const keycloak = new Keycloak({
  url: 'http://keycloak:8080',
  realm: 'green-erp',
  clientId: 'green-erp',
})

export default keycloak
