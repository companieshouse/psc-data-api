Feature: Delete PSC

  Scenario Outline: Delete PSC successfully
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Individual
    When a DELETE request is sent for "<company_number>"
    And a PSC does not exist for "<company_number>"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked with a DELETE event

    Examples:
      | data           | company_number |
      | get_individual | 34777772       |


  Scenario Outline: Delete PSC unsuccessfully - user not authenticated
    When a DELETE request is sent  for "<company_number>" without valid ERIC headers
    Then I should receive 401 status code

    Examples:
      | company_number |
      | 34777772       |

  Scenario Outline: Delete PSC unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When a DELETE request is sent for "<company_number>"
    Then I should receive 404 status code

    Examples:
      | company_number |
      | 34777772       |

  Scenario Outline: Delete PSC unsuccessfully while database is down
    Given Psc data api service is running
    And a PSC does not exist for "<company_number>"
    And the database is down
    When a DELETE request is sent for "<company_number>"
    Then I should receive 503 status code
    And the CHS Kafka API is not invoked with a DELETE event

    Examples:
      | company_number |
      | 34777772       |
