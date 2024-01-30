Feature: Get corporate entity

  Scenario Outline: Get corporate entity successfully
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Corporate Entity
    When a Get request is sent for "<company_number>" and "<notificationId>" for Corporate Entity
    And the Get call response body should match "<result>" file for Corporate Entity
    Then I should receive 200 status code

    Examples:
      | data                 | company_number | notificationId              | result                      |
      | get_corporate_entity | 34777773       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX | get_corporate_entity_output |


  Scenario Outline: Get corporate entity when sending get request without Eric headers
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Corporate Entity
    When a Get request is sent for "<company_number>" and "<notificationId>" without ERIC headers for Corporate Entity
    Then I should receive 401 status code

    Examples:
      | data                 | company_number | notificationId              |
      | get_corporate_entity | 34777773       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |

  Scenario Outline: Get PSC for corporate entity unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" and "<notificationId>" for Corporate Entity
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777773       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |
