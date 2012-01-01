package test

class TestUserObject {
    String apiKey
    
    static constraints = {
        apiKey(nullable: true)
    }
}