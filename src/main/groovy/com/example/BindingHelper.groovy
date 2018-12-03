package com.example

import grails.databinding.DataBindingSource

class BindingHelper {

    static AttributeType bindAttributeType(Attribute attribute, DataBindingSource source) {

        if (source['type']) {
            AttributeType newType = createAttributeType(source['type'] as Map)
            if (newType != attribute.type) {
                attribute.markDirty('type', newType)
            }
            return newType
        }

        return null
    }

    private static AttributeType createAttributeType(Map data) {
        if (data.name == 'AssociationType') {
            AssociationType type = new AssociationType()
            type.relatedTo = DataModel.findByName(data.relatedTo)
            return type
        }

        return null
    }
}
