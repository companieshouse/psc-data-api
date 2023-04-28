Feature: Process Psc Data Requests

  Scenario Outline: Processing psc data information successfully

    Given Psc data api service is running
    When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with notification id "<notificationId>"
    Then I should receive 201 status code
    And a record exists with id "<notificationId>"

    Examples:
      | companyNumber | notificationId                 | data                  |
      | 34777772      | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ    | psc_data_api |
