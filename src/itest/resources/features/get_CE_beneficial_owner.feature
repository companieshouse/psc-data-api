Feature: Get Corporate Entity Beneficial Owner

  Scenario Outline: Get Corporate Entity Beneficial Owner successfully
    Given Psc data api service is running
    And a PSC exists for "<company_number>" for Corporate Entity Beneficial Owner
    When a Get request is sent for "<company_number>" and "<notificationId>" for Corporate Entity Beneficial Owner
    And the Get call response body should match "<result>" file for Corporate Entity Beneficial Owner
    Then I should receive 200 status code

    Examples:
      | company_number | notificationId              | result                        |
      | 34777774       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZC | cebo_get_request_result        |



  Scenario Outline: Get Corporate Entity Beneficial Owner when sending get request without Eric headers

    Given Psc data api service is running
    And a PSC exists for "<company_number>" for Individual Beneficial Owner
    When a Get request is sent for "<company_number>" and "<notificationId>" without ERIC headers for Corporate Entity Beneficial Owner
    Then I should receive 401 status code

    Examples:
      | company_number | notificationId              |
      | 34777774       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZC |

  Scenario Outline: Get PSC unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" and "<notificationId>" for Corporate Entity Beneficial Owner
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777774       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZC |

