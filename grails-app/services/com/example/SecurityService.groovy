package com.example

import grails.compiler.GrailsCompileStatic

import grails.gorm.transactions.Transactional


@Transactional
@GrailsCompileStatic
class SecurityService {

    User getCurrentLoggedInUser() {
        return User.findByUsername('admin')
    }
}
