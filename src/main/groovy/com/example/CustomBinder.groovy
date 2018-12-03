package com.example

import grails.databinding.DataBindingSource
import grails.databinding.SimpleDataBinder
import grails.databinding.events.DataBindingListener

class CustomBinder extends SimpleDataBinder {

    protected void doBind(obj, DataBindingSource source, String filter, List whiteList, List blackList, DataBindingListener listener, errors) {
        Set<String> keys = source.getPropertyNames()
        for (String key in keys) {
            if (!filter || key.startsWith(filter + '.')) {
                String propName = key
                if (filter) {
                    propName = key[(1 + filter.size())..-1]
                }

                MetaProperty metaProperty = obj.metaClass.getMetaProperty propName

                if (metaProperty) { // normal property
                    if (isOkToBind(metaProperty.name, whiteList, blackList)) {
                        def val = source[key]
                        try {
                            def converter = getValueConverter(obj, metaProperty.name)
                            if (converter) {
                                bindProperty obj, source, metaProperty, converter.convert(source), listener, errors
                            }
                            else {
                                processProperty obj, metaProperty, preprocessValue(val), source, listener, errors
                            }
                        }
                        catch (Exception e) {
                            addBindingError(obj, propName, val, e, listener, errors)
                        }
                    }
                }
                else {
                    DataModel dataModel = (DataModel) obj
                    // code...
                    if (isOkToBind(propName, whiteList, blackList)) {
                        def val = source[key]
                        try {
                            processAttribute dataModel, key, preprocessValue(val)
                        }
                        catch (Exception e) {
                            addBindingError(dataModel, propName, val, e, listener, errors)
                        }
                    }

                }
            }
        }
    }

    protected processAttribute(DataModel obj, String key, val) {
        if (val instanceof Map) {
            obj[key] = DataModel.findByName(val.name)
        }
        else {
            obj[key] = val
        }
    }
}
