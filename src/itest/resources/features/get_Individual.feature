Feature: Get individual

  Scenario Outline: Get individual successfully
    Given Psc data api service is running
    And a PSC exists for "<company_number>"
    When a Get request is sent for "<company_number>" and "<notificationId>"
    And the Get call response body should match "<result>" file
    Then I should receive 200 status code

    Examples:
      | company_number | notificationId              | result |
      | 34777772       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ | result |



  Scenario Outline: Get Individual when sending get request without Eric headers

    Given Psc data api service is running
    And a PSC exists for "<company_number>"
    When a Get request is sent for "<company_number>" and "<notificationId>" without ERIC headers
    Then I should receive 401 status code

    Examples:
      | company_number | notificationId              |
      | 34777772       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ |

  Scenario Outline: Get PSC unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" and "<notificationId>"
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777772       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ |

