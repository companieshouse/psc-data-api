Feature: Get Legal Person Beneficial Owner

  Scenario Outline: Get individual successfully
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Legal Person Beneficial Owner
    When a Get request is sent for "<company_number>" and "<notificationId>" for Legal Person Beneficial Owner
    And the Get call response body should match "<result>" file for Legal Person Beneficial Owner
    Then I should receive 200 status code

    Examples:
      | data                | company_number | notificationId               | result                     |
      | get_legal_person_bo | 34777776       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZVV | get_legal_person_bo_output |


  Scenario Outline: Get Legal Person when sending get request without Eric headers
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Legal Person Beneficial Owner
    When a Get request is sent for "<company_number>" and "<notificationId>" without ERIC headers for Legal Person Beneficial Owner
    Then I should receive 401 status code

    Examples:
      | data                | company_number | notificationId               |
      | get_legal_person_bo | 34777776       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZVV |

  Scenario Outline: Get PSC unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a Get request has been sent for "<company_number>" and "<notificationId>" for Legal Person Beneficial Owner
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId               |
      | 34777776       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZVV |
