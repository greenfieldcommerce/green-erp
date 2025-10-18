# Green ERP - API

### About

Green ERP - API is a REST API that allows a series of operations required by managers and contractors at Greenfield, such as:

- Managers can CRUD clients, contractors and their daily rates
- Contractors can report work done by them
- Managers can request the generation of client invoices

(under development)

### Tech Stack

The application is built using Spring Boot and Java 25. The API follows RESTful principles and generates HATEOAS representations for all resources.
It uses Maven for dependency management and build automation.

### Testing and documentation
The API is tested with JUnit and Mockito, using Spring MockMvc. The API documentation is generated using Spring REST Docs.

### Build and start up
To build the project, run `mvn clean package`. 

One can also run the project as a Docker container. In that case, you will probably want to run the following separate containers:

- Keycloak, for authentication
- MySQL, for a relational database
- RabbitMQ, for a message broker

Otherwise, run the application with `docker compose up`, which will start the application and all the required containers.

### Messaging
The application uses RabbitMQ for message brokering and produces the following messages:

| Exchange | Routing Key                                | Description                                 |
|----------|--------------------------------------------|---------------------------------------------|
| contractor-invoice-created  | contractor-invoice-created.<Contractor_Id> | Data on the invoice created by a contractor |


