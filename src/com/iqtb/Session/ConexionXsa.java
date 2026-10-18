package com.iqtb.Session;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.impl.SessionFactoryImpl;

/**
 * DataSource (pool c3p0) hacia la base xsa, para la libreria RelacionadorFacturasPagos.
 * Usa la misma conexion que ya configura hibernate.cfg.xml, asi no se duplican credenciales.
 */
public final class ConexionXsa {

    /** Esquema de xsa donde viven las tablas de pagos (se puede cambiar con -Drelacion.esquema=... para pruebas). */
    public static final String ESQUEMA = System.getProperty("relacion.esquema", "xsa");

    private static DataSource dataSource;

    private ConexionXsa() {
    }

    public static synchronized DataSource get() {
        if (dataSource == null) {
            Properties p = ((SessionFactoryImpl) HibernateUtil.getSessionFactory()).getProperties();
            ComboPooledDataSource ds = new ComboPooledDataSource();
            try {
                ds.setDriverClass(p.getProperty("hibernate.connection.driver_class"));
            } catch (PropertyVetoException e) {
                throw new IllegalStateException("Driver JDBC invalido: " + e.getMessage(), e);
            }
            ds.setJdbcUrl(p.getProperty("hibernate.connection.url"));
            ds.setUser(p.getProperty("hibernate.connection.username"));
            ds.setPassword(p.getProperty("hibernate.connection.password"));
            ds.setMinPoolSize(1);
            ds.setMaxPoolSize(5);
            ds.setIdleConnectionTestPeriod(300);
            dataSource = ds;
        }
        return dataSource;
    }
}
