Feature: Error and Retry Psc Data Requests

Scenario Outline: Put psc statement when kafka-api is not available (Should be 503 and nothing saved to db)

Given Psc data api service is running
When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with notification id  "<notificationId>"
Then I should receive 200 status code
And the CHS Kafka API is not invoked
And nothing is persisted in the database


Examples:
|data               | companyNumber | notificationId              |
|psc_data_api       | 34777772      | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ |


Scenario Outline: Processing bad psc statement payload

Given Psc data api service is running
When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with notification id  "<notificationId>"
Then I should receive <statusCode> status code
And the CHS Kafka API is not invoked
And nothing is persisted in the database

Examples:
| data                                   | companyNumber| notificationId             | statusCode    |
| invalid_psc_data_api                   | 34777772     | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ| 400           |


Scenario Outline: Put psc statement when stubbed chs kafka api will return 503

Given Psc data api service is running
When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with notification id  "<notificationId>"
Then I should receive 503 status code
And the CHS Kafka API is not invoked
And nothing is persisted in the database


Examples:
|data               | companyNumber | notificationId              |
|psc_data_api       | 34777772      | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ |


