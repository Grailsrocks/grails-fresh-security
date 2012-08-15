events = {
    /*
     * Sent when a new user object is created. Receiver can pre-populate the userObject associated
     * with the security user.
     *
     * @param data The event object with properties:
     *      user - the FreshSecurity user domain instance
     *      userObject - the Application's own new user data domain instance
     *
     * @return none
     */
    'newUserCreated'(fork:false)
    
    /*
     * Sent when a user has successfully reset their password.
     *
     * @param data The FreshSecurity domain instance of the user
     * @return none
     */
    'passwordWasReset'(fork:false)

    /*
     * Sent when a user has successfully reset their password in the UI and needs to be redirected
     * to a new page in the UI.
     *
     * @param data The FreshSecurity domain instance of the user
     * @return Map of redirect() arguments. If empty or null, sends them to default post login page
     */
    'passwordResetCompletionPage'(fork:false)

    /*
     * Sent when a user has successfully confirmed their account, to get the page it should redirect to
     *
     * @param data The FreshSecurity domain instance of the user
     * @return Map of redirect() arguments. If empty or null, sends them to default post login page
     */
    'newUserConfirmedPage'(fork:false)
    
    // Private events used internally to hook into email confirmation
    'new.user.confirmed'(namespace:'plugin.freshSecurity')
    'password.reset.confirmed'(namespace:'plugin.freshSecurity')
}