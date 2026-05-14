/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.DAOs;

import com.iqtb.POJOs.Servicios;
import com.iqtb.Session.HibernateUtil;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Joaquin
 */
public class ServiciosDAO {

    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger("ServiciosDAO");

    public List<Servicios> getServicio(String servicio, String propiedad) {
        List<Servicios> result = null;
        try {
            iniciarOperacion();
            String hql = "from Servicios as s where s.servicio = '" + servicio + "' and s.propiedad = '" + propiedad + "'";
            Query q = session.createQuery(hql);
            result = q.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener el servicio: " + servicio + " propiedad: " + propiedad + " , ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }
}
