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
    'newUserCreated'()
    
    /*
     * Send when a user successfully resets their password.
     *
     * @param data The FreshSecurity domain instance of the user
     * @return none
     */
    'passwordReset'()
}