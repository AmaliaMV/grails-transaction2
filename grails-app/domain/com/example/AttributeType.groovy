package com.example

class AttributeType {

    static belongsTo = [attribute: Attribute]

    static constraints = {
        attribute nullable: true
    }
}
