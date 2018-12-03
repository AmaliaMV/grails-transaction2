package com.example

import grails.gorm.transactions.Transactional

@Transactional
class DataModelService implements PersistenceService<DataModel> {

    DataModel get(Serializable id) {
        return DataModel.get(id)
    }

    List<DataModel> list(Map args) {
        return DataModel.list(args)
    }

    Long count() {
        return DataModel.count()
    }

    DataModel save(DataModel dataModel) {
        return dataModel.save()
    }

    DataModel update(DataModel dataModel) {
        return dataModel.save()
    }

    void delete(DataModel dataModel) {
        dataModel.delete()
    }
}