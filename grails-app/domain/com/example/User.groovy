package com.example

class User {

    String username

    static constraints = {
        username blank: false, unique: true
    }
}
