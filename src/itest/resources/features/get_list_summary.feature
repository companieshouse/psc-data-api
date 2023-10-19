Feature: Get list summary

  Scenario Outline: Get list summary successfully
    Given Psc data api service is running
    And a PSC exists for "<company_number>" for List summary
    When a Get request is sent for "<company_number>" for  List summary
    And the Get call response body should match "<result>" file for List Summary
    Then I should receive 200 status code

    Examples:
      | company_number | notificationId              | result                                     |
      | 34777777       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX | Super_secure_get_request_result        |



  Scenario Outline: Get super secure when sending get request without Eric headers

    Given Psc data api service is running
    And a PSC exists for "<company_number>" for List summary
    When a Get request is sent for "<company_number>" without ERIC headers for List summary
    Then I should receive 401 status code

    Examples:
      | company_number | notificationId              |
      | 34777777       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |

  Scenario Outline: Get PSC for super secure unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" for List summary
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777777       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |

