@mongo @integration @smoke
Feature: MongoDB Connection Verification
  As a system administrator
  I want to verify MongoDB connectivity
  So that I can ensure database operations will work

  Scenario: Successful MongoDB connection
    Given id is "test-1"
    When I connect to MongoDB
    And I insert a test document with id "test-1"
    Then I should find 1 document with id "test-1"
    And I should be able to delete the document with id "test-1"

  Scenario: Successful MongoDB connection 2
    When I connect to MongoDB
    And I insert a test document with id "test-2"
    Then I should find 1 document with id "test-2"
    And I should be able to delete the document with id "test-2"