package com.example

interface PersistenceService<T> {

    T save(T instance)

    T update(T instance)

    void delete(T instance)

    T get(Serializable id)
}