package reproducer.issue1819;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.Version;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIssue1819Test {

    private static final long TENANT_ONE = 1L;
    private static final long TENANT_TWO = 2L;
    private static final long DOC_ONE = 101L;
    private static final long DOC_TWO = 202L;

    private TestDatabase database;
    private EntityManagerFactory emf;
    private CriteriaBuilderFactory cbf;
    private EntityViewManager evm;

    @BeforeAll
    void startDatabaseAndJpa() {
        String actualHibernateVersion = Version.getVersionString();
        assertTrue(actualHibernateVersion.startsWith(expectedHibernateVersionPrefix()),
                () -> "Expected Hibernate " + expectedHibernateVersionPrefix()
                        + " but resolved " + actualHibernateVersion);

        database = TestDatabase.start(databaseType());
        TenantResolver.use(TENANT_ONE);
        emf = Persistence.createEntityManagerFactory("issue1819", database.properties());
        cbf = Criteria.getDefault().createCriteriaBuilderFactory(emf);

        EntityViewConfiguration views = EntityViews.createDefaultConfiguration();
        views.addEntityView(TagView.class);
        views.addEntityView(FilteredDocumentView.class);
        views.addEntityView(TenantDocumentView.class);
        evm = views.createEntityViewManager(cbf);

        System.out.println("Running issue #1819 tests on " + database.name());
    }

    protected abstract TestDatabase.Type databaseType();

    protected abstract String expectedHibernateVersionPrefix();

    @BeforeEach
    void cleanTables() {
        TenantResolver.use(TENANT_ONE);
        inTransaction(em -> {
            em.createNativeQuery("delete from tenant_doc_tag").executeUpdate();
            em.createNativeQuery("delete from tenant_doc").executeUpdate();
            em.createNativeQuery("delete from filtered_doc_tag").executeUpdate();
            em.createNativeQuery("delete from filtered_doc").executeUpdate();
        });
    }

    @AfterAll
    void stopDatabaseAndJpa() throws Exception {
        if (emf != null) {
            emf.close();
        }
        if (database != null) {
            database.close();
        }
    }

    @Test
    void filteredEntityViewCollectionSaveCompletesAndKeepsOtherTenantUntouched() {
        seedFilteredDocuments();

        EntityManager em = emf.createEntityManager();
        try {
            em.unwrap(Session.class).enableFilter("tenantFilter").setParameter("tenantId", TENANT_ONE);
            em.getTransaction().begin();

            assertNull(evm.find(
                    em, EntityViewSetting.create(FilteredDocumentView.class), DOC_TWO));
            FilteredDocumentView view = evm.find(
                    em, EntityViewSetting.create(FilteredDocumentView.class), DOC_ONE);
            assertNotNull(view);
            view.getTags().add(newTag("c", "3"));
            evm.save(em, view);
            em.flush();
            em.getTransaction().commit();

            assertEquals(3, tagCount("filtered_doc_tag", DOC_ONE));
            assertEquals(2, tagCount("filtered_doc_tag", DOC_TWO));
        } finally {
            rollbackAndClose(em);
        }
    }

    @Test
    void tenantIdEntityViewCollectionSaveCompletesAndKeepsOtherTenantUntouched() {
        seedTenantDocuments();

        TenantResolver.use(TENANT_ONE);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            assertNull(evm.find(
                    em, EntityViewSetting.create(TenantDocumentView.class), DOC_TWO));
            TenantDocumentView view = evm.find(
                    em, EntityViewSetting.create(TenantDocumentView.class), DOC_ONE);
            assertNotNull(view);
            view.getTags().add(newTag("c", "3"));
            evm.save(em, view);
            em.flush();
            em.getTransaction().commit();

            assertEquals(3, tagCount("tenant_doc_tag", DOC_ONE));
            assertEquals(2, tagCount("tenant_doc_tag", DOC_TWO));
        } finally {
            rollbackAndClose(em);
        }
    }

    private void seedFilteredDocuments() {
        inTransaction(em -> {
            em.persist(new FilteredDocument(
                    DOC_ONE, TENANT_ONE, new Tag("a", "1"), new Tag("b", "2")));
            em.persist(new FilteredDocument(
                    DOC_TWO, TENANT_TWO, new Tag("a", "1"), new Tag("b", "2")));
        });
    }

    private void seedTenantDocuments() {
        TenantResolver.use(TENANT_ONE);
        inTransaction(em -> em.persist(new TenantDocument(
                DOC_ONE, new Tag("a", "1"), new Tag("b", "2"))));
        TenantResolver.use(TENANT_TWO);
        inTransaction(em -> em.persist(new TenantDocument(
                DOC_TWO, new Tag("a", "1"), new Tag("b", "2"))));
        TenantResolver.use(TENANT_ONE);
    }

    private TagView newTag(String key, String value) {
        TagView tag = evm.create(TagView.class);
        tag.setKey(key);
        tag.setValue(value);
        return tag;
    }

    private int tagCount(String table, long documentId) {
        EntityManager em = emf.createEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                            "select count(*) from " + table + " where doc_id = :id")
                    .setParameter("id", documentId)
                    .getSingleResult();
            return count.intValue();
        } finally {
            em.close();
        }
    }

    private void inTransaction(Consumer<EntityManager> work) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            work.accept(em);
            em.getTransaction().commit();
        } finally {
            rollbackAndClose(em);
        }
    }

    private static void rollbackAndClose(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }
}
