package com.example

import grails.testing.mixin.integration.Integration
import grails.transaction.*

import spock.lang.IgnoreRest

import static org.springframework.http.HttpStatus.*
import geb.spock.*
import grails.plugins.rest.client.RestBuilder

@Integration
@Rollback
class DataModelFunctionalSpec extends GebSpec {

    RestBuilder getRestBuilder() {
        new RestBuilder()
    }

    String getResourcePath() {
        return "${baseUrl}/dataModel"
    }

    void "Test the update action correctly updates an instance"() {
        when: "create dataModel1 to do a reference then"
        def response = restBuilder.post(resourcePath) {
            json dataModel1
        }

        then:
        response.status == CREATED.value()
        response.json.id != null
        response.json.name == 'dataModel1'
        response.json.attributes.size() == 0
        response.json.dynamic == null

        when: "create dataModel2 with an attribute"
        def dataModel1Id = response.json.id
        response = restBuilder.post(resourcePath) {
            json dataModelWithAttributes()
        }

        then:
        response.status == CREATED.value()
        response.json.id != null
        response.json.name == 'dataModel2'
        response.json.attributes.size() == 1
        response.json.attributes[0].name == 'attribute1'
        response.json.dynamic == "dinamic Attribute"
        response.json.dynamic2 == null

        println "\n[START] update\n"

        when: "update dataModel2"
        def dataModel2Id = response.json.id
        response = restBuilder.put("$resourcePath/$dataModel2Id") {
            json dataModelWithAttributesUpdated()
        }

        then:
        response.status == OK.value()
        response.json.attributes.size() == 1
        response.json.attributes[0].name == 'attribute2'
        response.json.dynamic == "dinamic Attribute"
        response.json.dynamic2.name == 'dataModel1'

        when: "delete dataModel1"
        response = restBuilder.delete("$resourcePath/$dataModel1Id")

        then:
        response.status == OK.value()

        when: "try to get the deleted dataModel1"
        response = restBuilder.get("$resourcePath/$dataModel1Id")

        then:
        response.status == NOT_FOUND.value()

        when: "delete dataModel2"
        response = restBuilder.delete("$resourcePath/$dataModel2Id")

        then:
        response.status == OK.value()

        when: "try to get the deleted dataModel2"
        response = restBuilder.get("$resourcePath/$dataModel2Id")

        then:
        response.status == NOT_FOUND.value()
    }

    void "Test the delete action correctly deletes an instance"() {
        when: "The save action is executed with valid data"
        def response = restBuilder.post(resourcePath) {
            json dataModel1
        }

        then: "The response is correct"
        response.status == CREATED.value()
        response.json.id

        when: "The save action is executed with valid data"
        def dataModel1Id = response.json.id
        response = restBuilder.post(resourcePath) {
            json getDataModel2(dataModel1Id)
        }

        then: "The response is correct"
        response.status == CREATED.value()
        response.json.id

        when: "When the delete action is executed on an unknown instance"
        def dataModel2Id = response.json.id
        response = restBuilder.delete("$resourcePath/$dataModel1Id")

        then: "The response is correct"
        response.status == INTERNAL_SERVER_ERROR.value()

        when: "When the delete action is executed on an existing instance"
        response = restBuilder.delete("$resourcePath/$dataModel2Id")

        then: "The response is correct"
        response.status == OK.value()

        when: "try to get the deleted data model"
        response = restBuilder.get("$resourcePath/$dataModel2Id")

        then:
        response.status == NOT_FOUND.value()

        when: "When the delete action is executed on an existing instance"
        response = restBuilder.delete("$resourcePath/$dataModel1Id")

        then: "The response is correct"
        response.status == OK.value()

        when: "try to get the deleted data model"
        response = restBuilder.get("$resourcePath/$dataModel1Id")

        then:
        response.status == NOT_FOUND.value()
    }

    Closure dataModelWithAttributes() {
        { ->
            [
                name      : "dataModel2",
                attributes: [
                    [
                        name        : "attribute1",
                        metadataType: '{"type":"AssociationType", "relatedTo":"dataModel1"}',
                        type        : [
                            name     : "AssociationType",
                            relatedTo: "dataModel1"
                        ]
                    ]
                ],
                dynamic: "dinamic Attribute"
            ]
        }
    }

    Closure dataModelWithAttributesUpdated() {
        { ->
            [
                name      : "dataModel2",
                attributes: [
                    [
                        name        : "attribute2",
                        metadataType: '{"type":"AssociationType", "relatedTo":"dataModel1"}',
                        type        : [
                            name     : "AssociationType",
                            relatedTo: "dataModel1"
                        ]
                    ]
                ],
                dynamic2: [
                    name: "dataModel1"
                ]
            ]
        }
    }

    Closure getDataModel1() {
        { ->
            [
                name: "dataModel1"
            ]
        }
    }

    Closure getDataModel2(def dataModelParentId) {
        { ->
            [
                name  : "dataModel2",
                parent: [
                    id: dataModelParentId
                ]
            ]
        }
    }

}