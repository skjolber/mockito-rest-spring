# example
Example application with a simple `@Service` which calls out to an external `Pet` service of HTTP.

Under unit testing, the `Pet` service client is pointed to localhost, where a mock endpoint responds with the desired response for each test method.