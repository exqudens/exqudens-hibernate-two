package com.exqudens.hibernate.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.exqudens.hibernate.test.model.c.Item;
import com.exqudens.hibernate.test.model.c.Order;
import com.exqudens.hibernate.test.model.c.OrderId;
import com.exqudens.hibernate.test.model.c.User;
import com.exqudens.hibernate.test.util.ClassPathUtils;
import com.exqudens.hibernate.test.util.ConfigGroovyUtils;
import com.exqudens.hibernate.test.util.DataSourceUtils;
import com.exqudens.hibernate.util.PersistenceUnitInfoUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestModelC {

    private static final String   DS_PREFIX;
    private static final String   JPA_PREFIX;
    private static final String[] DS_IGNORE_KEYS;

    static {
        DS_PREFIX = "dataSources.exqudensHibernateDataSource.";
        JPA_PREFIX = "jpaProviders.hibernateJpaProvider.properties.";
        DS_IGNORE_KEYS = new String[] {
            "host",
            "port",
            "dbName",
            "jdbcUrlParams"
        };
    }

    private static EntityManagerFactory emf;

    private EntityManager em;

    @BeforeClass
    public static void beforeClass() {
        emf = createEntityManagerFactory(DS_PREFIX, JPA_PREFIX, DS_IGNORE_KEYS);
    }

    @Before
    public void before() {
        em = emf.createEntityManager();
    }

    //@Ignore
    @Test
    public void test1Create() {
        System.out.println("=== test1Create =========================================================================");
        List<User> users = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        List<Item> items = new ArrayList<>();

        users.add(new User(null, "name_" + 1, "email_" + 1, new ArrayList<>()));

        orders.add(new Order(OrderId.builder().id(1L).name("name_" + 1L).number("number_" + 1L).build(), null, new ArrayList<>()));
        orders.add(new Order(OrderId.builder().id(2L).name("name_" + 2L).number("number_" + 2L).build(), null, new ArrayList<>()));

        items.add(new Item(null, "name_" + 1, "description_" + 1, null));
        items.add(new Item(null, "name_" + 2, "description_" + 2, null));
        items.add(new Item(null, "name_" + 3, "description_" + 3, null));

        users.get(0).getOrders().addAll(orders);

        orders.get(0).getItems().addAll(items);

        orders.stream().forEach(o -> o.setUser(users.get(0)));

        items.stream().forEach(i -> i.setOrder(orders.get(0)));

        try {
            em.persist(users.get(0));
            em.getTransaction().begin();
            em.flush();
            em.getTransaction().commit();
            em.clear();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("=========================================================================================");
    }

    @Ignore
    @Test
    public void test2Read() {
        System.out.println("=== test2Read ===========================================================================");
        User user = em.find(User.class, 1L);
        em.clear();
        System.out.println(user);
        System.out.println("=========================================================================================");
    }

    @Ignore
    @Test
    public void test3Update() {
        System.out.println("=== test3Update =========================================================================");
        System.out.println();
        System.out.println("=========================================================================================");
    }

    @Ignore
    @Test
    public void test4Delete() {
        System.out.println("=== test4Delete =========================================================================");
        User user = em.find(User.class, 1L);
        em.remove(user);
        em.getTransaction().begin();
        em.flush();
        em.getTransaction().commit();
        em.clear();
        System.out.println("=========================================================================================");
    }

    @After
    public void after() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @AfterClass
    public static void afterClass() {
        if (emf != null && emf.isOpen()) {
            //emf.close();
        }
    }

    private static EntityManagerFactory createEntityManagerFactory(
        String dataSourcePrefix,
        String jpaPrefix,
        String... dataSourceIgnoreKeys
    ) {
        try {
            Map<String, Object> configMap = ConfigGroovyUtils.toMap(ClassPathUtils.toString("config-test.groovy"));

            DataSource dataSource = DataSourceUtils.createDataSource(
                ConfigGroovyUtils.retrieveProperties(configMap, dataSourcePrefix, dataSourceIgnoreKeys)
            );

            Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
            dataSourceMap.put("any", dataSource);

            Map<String, Object> properties = ConfigGroovyUtils.retrieveProperties(configMap, jpaPrefix);

            PersistenceUnitInfo info = PersistenceUnitInfoUtils.createHibernatePersistenceUnitInfo(
                "default",
                dataSourceMap,
                properties,
                User.class,
                Order.class,
                Item.class
            );

            ClassLoader cl = PersistenceUnitInfoUtils.class.getClassLoader();
            Object o = cl.loadClass(info.getPersistenceProviderClassName()).newInstance();
            PersistenceProvider persistenceProvider = PersistenceProvider.class.cast(o);
            EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(
                info,
                info.getProperties()
            );
            return emf;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
