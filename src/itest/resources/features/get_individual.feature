Feature: Get individual

  Scenario Outline: Get individual successfully
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Individual
    When a Get request is sent for "<company_number>" and "<notificationId>" for Individual
    And the Get call response body should match "<result>" file for Individual
    Then I should receive 200 status code

    Examples:
      | data           | company_number | notificationId              | result                |
      | get_individual | 34777771       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ | get_individual_output |


  Scenario Outline: Get Individual when sending get request without Eric headers
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Individual
    When a Get request is sent for "<company_number>" and "<notificationId>" without ERIC headers for Individual
    Then I should receive 401 status code

    Examples:
      | data           | company_number | notificationId              |
      | get_individual | 34777771       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ |

  Scenario Outline: Get PSC unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" and "<notificationId>" for Individual
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777771       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ |
