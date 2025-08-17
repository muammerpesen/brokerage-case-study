# NOTES

There are two customers in my project. To run apis, Username and Password has to be defined as Basic Auth.

1. Username : cust  Password:admin123 --> Normal User
2. Username : admin Password:admin123 --> Admin User

These two records exist in H2 database. If they doesn't exist, the following query can be run. 

insert into customer values(1 'admin', '$2a$10$onMjCUTvNZvsggjq0NRcxu06BhNSAR9X9ii7VZ4k/0OuJnA9zFzG2', 'ADMIN', 0);
insert into customer values(2 'cust', '$2a$10$onMjCUTvNZvsggjq0NRcxu06BhNSAR9X9ii7VZ4k/0OuJnA9zFzG2', 'CUSTOMER', 0);

# REQUEST EXAMPLES
Postman collection file was added as "/brokerage-case-study.postman_collection.json"
