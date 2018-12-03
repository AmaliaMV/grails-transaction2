package com.example

import grails.compiler.GrailsCompileStatic

//@GrailsCompileStatic
class DataModel {

    static mapWith = "neo4j"

    String name
    DataModel parent
    User createdBy
    User updatedBy

    List<Attribute> attributes = []

    static hasMany = [attribute: Attribute]

    static mappedBy = [attributes: null]

    static constraints = {
        name unique: true
        parent nullable: true
        createdBy nullable: true
        updatedBy nullable: true
    }

    void beforeDelete() {
        if (DataModel.findByParent(this)) {
            throw new RuntimeException("Can't delete a data model $name is in use.")
        }
    }
}
