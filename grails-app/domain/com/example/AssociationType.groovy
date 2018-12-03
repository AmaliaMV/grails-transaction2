package com.example

class AssociationType extends AttributeType {

    DataModel relatedTo

    static transients = ['relatedTo']

    static constraints = {
    }
}
