/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.DAOs;

import com.iqtb.POJOs.UsuariosRecepcion;
import com.iqtb.Session.HibernateUtil;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Joaquin
 */
public class UsuariosRecepcionDAO {

    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger("UsuariosRecepcionDAO");

    public UsuariosRecepcionDAO() {
    }

    public List<UsuariosRecepcion> getUsuariosByRFC(String RFC) {
        List<UsuariosRecepcion> result = null;
        try {
            iniciarOperacion();
            String hql = "from UsuariosRecepcion as u where u.sucursales.empresas.rfc ='" + RFC + "'";
            Query q = session.createQuery(hql);
            result = q.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener los Usuarios Recepcion, ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    public List<UsuariosRecepcion> getTodos() {
        List<UsuariosRecepcion> result = null;
        try {
            iniciarOperacion();
            String hql = "from UsuariosRecepcion as u";
            Query q = session.createQuery(hql);
            result = q.list();
            for (UsuariosRecepcion d : result) {
                Hibernate.initialize(d.getSucursales().getEmpresas());
                Hibernate.initialize(d.getTiposCfd());
            }
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener los Usuarios Recepcion, ERROR: " + e.getMessage());
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
