package com.example

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import grails.compiler.GrailsCompileStatic
import grails.databinding.BindUsing
import grails.databinding.DataBindingSource

import org.grails.web.json.JSONObject

@GrailsCompileStatic
class Attribute {

    User createdBy
    User updatedBy

    String name
    String metadataType

    @BindUsing({ Attribute attribute, DataBindingSource source -> BindingHelper.bindAttributeType(attribute, source) })
    AttributeType type

    static belongsTo = DataModel

    static transients = ['type']

    static constraints = {
        type bindable: true, nullable: true
        createdBy nullable: true
        updatedBy nullable: true
    }

    void afterLoad() {
        type = loadAttributeType(metadataType)
    }

    def beforeInsert() {
        updateMetadata()
    }

    def beforeUpdate() {
        updateMetadata()
    }

    AttributeType loadAttributeType(String metadataType) {
        Map metadata = new JSONObject((Map) new JsonSlurper().parseText(metadataType))
        if (metadata.type == 'AssociationType') {
            AssociationType type = new AssociationType()
            type.relatedTo = DataModel.findByName(metadata.relatedTo)
            return type
        }

        return null
    }

    void updateMetadata() {
        String newMetadata = JsonOutput.toJson([type: 'AssociationType', relatedTo: ((AssociationType) type).relatedTo.name])
        if (this.metadataType != newMetadata) {
            this.metadataType = newMetadata
            this.markDirty('metadataType')
        }
    }
}
