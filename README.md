Introduction
------------

This project implements an jSMPP server and client in a Spring Boot enviroment.
A simple SMPP server is started, then a multiple number of concurrent SMPP clients
are connecting, sending some random number of submit_sm messages to the server.

Use
---
Start the application via the Maven Spring Boot plugin:

	$ mvn spring-boot:run

The server listens on port 2075 on localhost, the clients connects to the same port.