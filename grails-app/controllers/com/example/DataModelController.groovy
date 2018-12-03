package com.example

import grails.gorm.transactions.Transactional

@Transactional
class DataModelController extends RestfulController<DataModel> {

    DataModelService dataModelService

    @Override
    protected PersistenceService<DataModel> getPersistenceService() {
        return dataModelService
    }
}
