Feature: Process Psc Data Requests

  Scenario Outline: Processing psc data information successfully

    Given Psc data api service is running
    When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with notification id "<notificationId>"
    Then I should receive 200 status code
    And a record exists with id "<notificationId>"

    Examples:
      | companyNumber | notificationId                 | data                  |
      | OC421554      | DHTUrJoAuKdXw7zvkreyAm_SoH0    | psc_data_api |
