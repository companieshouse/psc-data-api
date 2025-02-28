
Feature: Get individual

  Scenario Outline: Get individual with verification state successfully
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Individual
    When an "authorized" Get request is sent for "<company_number>" and "<notificationId>" for Individual with verification state
    And the Get call response body should match "<result>" file for Individual with verification state
    Then I should receive 200 status code

    Examples:
      | data           | company_number | notificationId              | result                                        |
      | get_individual | 34777771       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ | get_individual_output_with_verification_state |

  Scenario Outline: Get PSC with verification state unsuccessfully - PSC resource does not exist
    Given a PSC does not exist for "<company_number>"
    When an "authorized" Get request is sent for "<company_number>" and "<notificationId>" for Individual with verification state
    Then I should receive 404 status code

    Examples:
      | company_number | notificationId              |
      | 34777771       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ |

  Scenario Outline: Get Individual with verification state when sending get request with insufficient privileges
    Given Psc data api service is running
    And a PSC "<data>" exists for "<company_number>" for Individual
    When an "<auth>" Get request is sent for "<company_number>" and "<notificationId>" for Individual with verification state
    Then I should receive <status> status code

    Examples:
      | auth            | data           | company_number | notificationId              | status |
      | unauthorized    | get_individual | 34777771       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ | 403    |
      | unauthenticated | get_individual | 34777771       | ZfTs9WeeqpXTqf6dc6FZ4C0H0ZZ | 401    |

