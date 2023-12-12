Feature: Get super secure

  Scenario Outline: Get super secure successfully
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Super Secure
    When a Get request is sent for "<company_number>" and "<notificationId>" for Super Secure
    And the Get call response body should match "<result>" file for Super Secure
    Then I should receive 200 status code

    Examples:
      | data             | company_number | notificationId              | result                  |
      | get_super_secure | 34777777       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX | get_super_secure_output |


  Scenario Outline: Get super secure when sending get request without Eric headers
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Super Secure
    When a Get request is sent for "<company_number>" and "<notificationId>" without ERIC headers for Super Secure
    Then I should receive 401 status code

    Examples:
      | data             | company_number | notificationId              |
      | get_super_secure | 34777777       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |

  Scenario Outline: Get PSC for super secure unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" and "<notificationId>" for Super Secure
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777777       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |
