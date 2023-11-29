@Login
Feature: Login
  As a Registered User of the application
  I want to validate the Login functionality
  In order to check if it works as desired

  Background: A Registered User navigates to Login page
    Given I navigate to the "login" page

  @SuccessfulLogin
  Scenario Outline: Successful login using valid credentials
    When I fill in "Email address" with "<Email address>"
    And I fill in "password" with "<password>"
    And I click on the "Sign in" button
    Then I should be redirected on the "home" page
    And I should see "Hi, newcomer!" message
    And I should see "Dashboard","Profile" and "Logout" links
    And I should see "Resend mail" button
    Examples:
      | Email address			        |	password	|
      |	vlpiatachenko@gmail.com			|	k.AEyci7R6Bc6Lh	|

  @failedLogin
  Scenario Outline: Failed login using wrong credentials
    When I fill in "Email address" with "<Email address>"
    And I fill in "password" with "<password>"
    And I click on the "Log In" button
    Then I should be redirected on the "Login" page
    And I should see "error" message as "<alert>"
    Examples:
      | Email address                       | password               | alert                      |
      | vlpiatachenko@gmail.com	            | !23		             | User or password not known |
      | VLPIATACHENKO@gmail.com	            | k.AEyci7R6Bc6Lh        | User or password not known |
      | vlpiatachenko@gmailcom	            | k.AEyci7R6Bc6Lh        | User or password not known |
      | Vpiatachenko@gmail.com	            | k.AEyci7R6Bc6Lh        | User or password not known |
