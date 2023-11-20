Feature: Get list summary

  Scenario Outline: Processing Psc List GET request successfully

    Given Psc data api service is running
    And a PSC exists for "<company_number>" for List summary
    When a Get request is sent for "<company_number>" for  List summary
    Then I should receive 200 status code
    And the Get call response body should match "<result>" file for List Summary

    Examples:
      | company_number | result                       |
      | 34777777       | psc_list_34777777            |



  Scenario Outline: Processing Psc List GET register view request successfully

    Given Psc data api service is running
    And a PSC exists for "<companyNumber>" for List summary
    And Company Metrics API is available for company number "<companyNumber>"
    When a Get request is sent for "<companyNumber>" for  List summary
    Then I should receive 200 status code
    And the Get call response body should match "<result>" file for List Summary

    Examples:
      | companyNumber | result                                     |
      | 34777777      | psc_list_34777777_register_view_true       |


  Scenario Outline: Processing Psc List GET register view request unsuccessfully
  when metrics is unavailable

    Given Psc data api service is running
    And a PSC exists for "<company_number>" for List summary
    And Company Metrics API is unavailable
    When I send a GET statement list request for company number in register view "<company_number>"
    Then I should receive 404 status code

    Examples:
      | company_number |
      | 34777777       |


  Scenario Outline: Processing Psc List GET register view request unsuccessfully
  when no company psc statements in public register

    Given Psc data api service is running
    And nothing is persisted to the database
    And Company Metrics API is available for company number "<company_number>"
    When a Get request is sent for "<company_number>" for  List summary
    Then I should receive 404 status code

    Examples:
      | company_number |
      | 34777777       |

  Scenario Outline: Get Psc List when sending get request without Eric headers

    Given Psc data api service is running
    And a PSC exists for "<company_number>" for List summary
    When a Get request is sent for "<company_number>" without ERIC headers for List summary
    Then I should receive 401 status code

    Examples:
      | company_number |
      | 34777777       |

  Scenario Outline: Get PSC unsuccessfully while database is down
    Given Psc data api service is running
    And a PSC does not exist for "<company_number>"
    And the database is down
    When a Get request is sent for "<company_number>" for  List summary
    Then I should receive 503 status code

    Examples:
      | company_number |
      | 34777777       |

