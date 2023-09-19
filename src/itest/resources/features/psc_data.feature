Feature: Process Psc Data Requests

  Scenario Outline: Processing psc data information successfully

    Given Psc data api service is running
    When I send a PUT request with payload "<data>" file with notification id "<notificationId>"
    Then I should receive 201 status code
    And a record exists with id "<notificationId>"


    Examples:
      | notificationId                 | data         |
      | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ    | psc_data_api |

  Scenario Outline: Processing old psc data information

    Given Psc data api service is running
    And a psc data record exists with notification id "<notificationId>" and delta_at "<deltaAt>"
    When I send a PUT request with payload "<oldData>" file for record with notification Id "<notificationId>"
    Then I should receive 201 status code
    And a record exists with id "<notificationId>" and delta_at "<deltaAt>"

    Examples:
      | notificationId                 | deltaAt                   | oldData                      |
      | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ    | 2023-11-20T08:47:45.378Z  | psc_data_api_old             |

