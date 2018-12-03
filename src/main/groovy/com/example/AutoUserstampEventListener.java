package com.example;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEvent;

import org.grails.datastore.gorm.neo4j.Neo4jSession;
import org.grails.datastore.mapping.core.Datastore;
import org.grails.datastore.mapping.engine.EntityAccess;
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent;
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener;
import org.grails.datastore.mapping.engine.event.PreInsertEvent;
import org.grails.datastore.mapping.engine.event.PreUpdateEvent;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity;
import org.grails.datastore.mapping.model.types.Association;

public class AutoUserstampEventListener extends AbstractPersistenceEventListener implements MappingContext.Listener {

    private static final Log log = LogFactory.getLog(AutoUserstampEventListener.class);

    private SecurityService securityService;

    public static final String CREATED_BY_PROPERTY = "createdBy";
    public static final String UPDATED_BY_PROPERTY = "updatedBy";

    private Map<PersistentEntity, Boolean> entitiesWithCreatedBy = new ConcurrentHashMap<>();
    private Map<PersistentEntity, Boolean> entitiesWithUpdatedBy = new ConcurrentHashMap<>();

    public AutoUserstampEventListener(final Datastore datastore) {
        super(datastore);

        for (PersistentEntity persistentEntity : datastore.getMappingContext().getPersistentEntities()) {
            storeCreatedByInfo(persistentEntity);
            storeUpdatedByInfo(persistentEntity);
        }

        datastore.getMappingContext().addMappingContextListener(this);
    }

    @Override
    protected void onPersistenceEvent(final AbstractPersistenceEvent event) {
        if (event instanceof PreInsertEvent) {
            beforeInsert(event.getEntity(), event.getEntityAccess());
        }
        else if (event instanceof PreUpdateEvent) {
            beforeUpdate(event.getEntity(), event.getEntityAccess());
        }
    }

    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return PreInsertEvent.class.isAssignableFrom(eventType) || PreUpdateEvent.class.isAssignableFrom(eventType);
    }

    public boolean beforeInsert(PersistentEntity entity, EntityAccess ea) {
        if (hasCreatedBy(entity)) {
            setProperty(entity, ea, CREATED_BY_PROPERTY);

            if (hasUpdateBy(entity)) {
                setProperty(entity, ea, UPDATED_BY_PROPERTY);
            }
        }
        return true;
    }

    public boolean beforeUpdate(PersistentEntity entity, EntityAccess ea) {
        if (hasUpdateBy(entity)) {
            setProperty(entity, ea, UPDATED_BY_PROPERTY);
        }
        return true;
    }

    private void setProperty(PersistentEntity entity, EntityAccess ea, String propertyName) {
        log.info("[START] get current user");
        final User user = getSecurityService().getCurrentLoggedInUser();
        log.info("[END] get current user");
        if (user != null) {
            Neo4jSession session = ((Neo4jSession) this.datastore.getCurrentSession());
            Association association = (Association) entity.getPropertyByName(propertyName);
            final EntityAccess aea = session.createEntityAccess(association.getAssociatedEntity(), user);
            session.addPendingRelationshipInsert((Serializable) ea.getIdentifier(), association, (Serializable) aea.getIdentifier());
        }
    }

    private boolean hasUpdateBy(PersistentEntity entity) {
        return entitiesWithUpdatedBy.containsKey(entity) && entitiesWithUpdatedBy.get(entity);
    }

    private boolean hasCreatedBy(PersistentEntity entity) {
        return entitiesWithCreatedBy.containsKey(entity) && entitiesWithCreatedBy.get(entity);
    }

    private void storeUpdatedByInfo(PersistentEntity persistentEntity) {
        entitiesWithUpdatedBy.put(persistentEntity,
                                  persistentEntity.hasProperty(UPDATED_BY_PROPERTY, User.class));
    }

    private void storeCreatedByInfo(PersistentEntity persistentEntity) {
        entitiesWithCreatedBy.put(persistentEntity,
                                  persistentEntity.hasProperty(CREATED_BY_PROPERTY, User.class));
    }

    public void persistentEntityAdded(PersistentEntity entity) {
        storeCreatedByInfo(entity);
        storeUpdatedByInfo(entity);
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

}
