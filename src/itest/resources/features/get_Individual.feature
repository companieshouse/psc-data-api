Feature: Get individual

  Scenario Outline: Get individual successfully
    Given Psc data api service is running
    And a PSC exists for "<company_number>"
    When a Get request is sent for "<company_number>" and "<pscId>"
    And the Get call response body should match "<result>" file
    Then I should receive 200 status code

    Examples:
      | company_number | pscId                       | result |
      | 34777772       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ | result |


