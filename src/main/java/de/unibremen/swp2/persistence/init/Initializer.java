package de.unibremen.swp2.persistence.init;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
@Startup
public class Initializer {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private static final Path init = Paths
            .get(System.getProperty("user.home"))
            .resolve("IGradeBook")
            .resolve("initDB")
            .resolve("init.sql");

    @PostConstruct
    public void init() {
        if (!Files.exists(init)) {
            try (InputStream in = getClass()
            .getResourceAsStream("/init.sql")) {
                Files.createDirectories(init.getParent());
                Files.copy(in, init);
            } catch (final IOException e) {
                System.out.println("Konnte Daten nicht schreiben");
            }
            entityManager.createNativeQuery("RUNSCRIPT FROM '~/IGradeBook/initDB/init.sql'").executeUpdate();
        }
    }

}
