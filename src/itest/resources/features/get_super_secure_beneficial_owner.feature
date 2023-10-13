Feature: Get super secure beneficial owner

  Scenario Outline: Get super secure beneficial owner successfully
    Given Psc data api service is running
    And a PSC exists for "<company_number>" for Super Secure Beneficial Owner
    When a Get request is sent for "<company_number>" and "<notificationId>" for Super Secure Beneficial Owner
    And the Get call response body should match "<result>" file for Super Secure Beneficial Owner
    Then I should receive 200 status code

    Examples:
      | company_number | notificationId              | result                                     |
      | 34777776       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX | Super_secure_BO_get_request_result         |

  Scenario Outline: Get super secure beneficial owner when sending get request without Eric headers

    Given Psc data api service is running
    And a PSC exists for "<company_number>" for Super Secure Beneficial Owner
    When a Get request is sent for "<company_number>" and "<notificationId>" without ERIC headers for Super Secure Beneficial Owner
    Then I should receive 401 status code

    Examples:
      | company_number | notificationId              |
      | 34777776       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |

  Scenario Outline: Get PSC for super secure beneficial owner unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" and "<notificationId>" for Super Secure Beneficial Owner
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777776       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZX |

