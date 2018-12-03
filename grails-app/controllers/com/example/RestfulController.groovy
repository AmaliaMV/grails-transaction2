package com.example

import java.lang.reflect.ParameterizedType

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.transactions.Transactional
import grails.util.GrailsNameUtils

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@Transactional(readOnly = true)
abstract class RestfulController<T> {

	static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    Class<T> resource
    String resourceName

    RestfulController() {
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass()
        this.resource = (Class<T>) pt.getActualTypeArguments()[0]
        resourceName = GrailsNameUtils.getPropertyName(resource)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond listAllResources(params), model: [("${resourceName}Count".toString()): countResources()]
    }

    def show() {
        respond queryForResource(params.id as Long)
    }

    def create() {
        respond createResource()
    }

    @Transactional
    def save() {
        T instance = createResource()
        persistenceService.save(instance)

        respond instance, [status: CREATED]
    }

    @Transactional
    def update() {
        T instance = queryForResource(params.id as Long)

        if (instance == null) {
            render status: NOT_FOUND
            return
        }

       // BindingHelper.withNoAutoFlush {
        CustomBinder binder = new CustomBinder()
        binder.bind(instance, new SimpleMapDataBindingSource((Map) request.JSON), null, null)
        //instance.setProperties(getObjectToBind())
        //}

        persistenceService.update(instance)
        respond instance, [status: OK]
    }

    @Transactional
    def delete() {
        final T instance = queryForResource(params.id as Long)
        if (instance == null) {
            render status: NOT_FOUND
            return
        }

        def displayValue = instance.id
        persistenceService.delete(instance)

        respond ([status: OK], [message: "Object $displayValue deleted"])
    }


    protected T createResource() {
        T instance = resource.newInstance()
        CustomBinder binder = new CustomBinder()
        binder.bind(instance, new SimpleMapDataBindingSource((Map) getObjectToBind().JSON), null, null)
        instance
    }

    protected Object getObjectToBind() {
        request
    }

    protected List<T> listAllResources(Map params) {
        resource.list(params)
    }

    protected Integer countResources() {
        resource.count()
    }

    protected T queryForResource(Serializable id) {
        return getPersistenceService().get(id)
    }

    abstract protected PersistenceService<T> getPersistenceService()

    void handleException(final Exception e) {
        log.error e.message
        e.printStackTrace()
        render status: INTERNAL_SERVER_ERROR

    }
}
