package database;

import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the Hibernate SessionFactory.
 *
 * <p>Supports two deployment modes controlled by {@link #setMode(DbMode)}:
 * <ul>
 *   <li>{@code LOCAL}  — MS SQL Server (local machine), uses hibernate_local.cfg.xml</li>
 *   <li>{@code CLOUD}  — Supabase / PostgreSQL,         uses hibernate_cloud.cfg.xml</li>
 * </ul>
 *
 * <p>The active mode is set once in {@link application.Main} before the application starts.
 */
public class DB_manager {

    // ----------------------------------------------------------------
    //  Deployment mode enum
    // ----------------------------------------------------------------

    public enum DbMode {
        /** MS SQL Server running locally */
        LOCAL,
        /** Supabase (PostgreSQL) running in the cloud */
        CLOUD
    }

    // ----------------------------------------------------------------
    //  Internal state
    // ----------------------------------------------------------------

    private static DbMode       currentMode = DbMode.CLOUD; // default — overridden by Main
    private static SessionFactory factory    = null;
    private static final Logger   logger     = LoggerFactory.getLogger(DB_manager.class);

    // ----------------------------------------------------------------
    //  Configuration file mapping
    // ----------------------------------------------------------------

    private static String hibernateConfigFile() {
        return switch (currentMode) {
            case LOCAL -> "hibernate_local.cfg.xml";
            case CLOUD -> "hibernate_cloud.cfg.xml";
        };
    }

    // ----------------------------------------------------------------
    //  Environment-variable key mapping
    //  .env file should contain LOCAL_* or CLOUD_* prefixed keys:
    //
    //  LOCAL_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=BlueMoon;...
    //  LOCAL_DB_USER=sa
    //  LOCAL_DB_PASSWORD=secret
    //
    //  CLOUD_DB_URL=jdbc:postgresql://db.<project>.supabase.co:5432/postgres
    //  CLOUD_DB_USER=postgres
    //  CLOUD_DB_PASSWORD=secret
    // ----------------------------------------------------------------

    private static String envKey(String base) {
        return currentMode.name() + "_" + base;   // e.g. "LOCAL_DB_URL"
    }

    // ----------------------------------------------------------------
    //  Public API
    // ----------------------------------------------------------------

    /**
     * Sets the deployment mode. Must be called <em>before</em> {@link #init()}.
     *
     * @param mode {@code LOCAL} or {@code CLOUD}
     */
    public static void setMode(DbMode mode) {
        if (factory != null) {
            throw new IllegalStateException(
                "Cannot change DB mode after SessionFactory has been initialized.");
        }
        currentMode = mode;
        logger.info("DB mode set to: {}", mode);
    }

    /** Returns the currently active deployment mode. */
    public static DbMode getMode() {
        return currentMode;
    }

    /**
     * Initialises the {@link SessionFactory} using the active mode's config and credentials.
     * Idempotent — calling twice has no effect.
     */
    public static void init() {
        if (factory != null) {
            return;
        }
        try {
            Dotenv dotenv = Dotenv.load();

            Configuration config = new Configuration().configure(hibernateConfigFile());

            config.setProperty("hibernate.connection.url",
                    dotenv.get(envKey("DB_URL")));
            config.setProperty("hibernate.connection.username",
                    dotenv.get(envKey("DB_USER")));
            config.setProperty("hibernate.connection.password",
                    dotenv.get(envKey("DB_PASSWORD")));

            factory = config.buildSessionFactory();
            logger.info("SessionFactory initialised in {} mode.", currentMode);

        } catch (Exception e) {
            logger.error("Cannot connect to database [{}]: {}", currentMode, e.getMessage(), e);
            throw new RuntimeException("Database initialisation failed.", e);
        }
    }

    /**
     * Returns the {@link SessionFactory}, initialising it on first call if needed.
     */
    public static SessionFactory getFactory() {
        if (factory == null) {
            init();
        }
        return factory;
    }

    /** Closes the {@link SessionFactory} and releases all resources. */
    public static void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
            factory = null;
            logger.info("SessionFactory closed.");
        }
    }
}
