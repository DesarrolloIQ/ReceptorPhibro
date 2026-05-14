package com.iqtb.DAOs;

import com.iqtb.POJOs.CfdisRelacionadosHijo;
import com.iqtb.POJOs.CfdisRelacionadosPadre;
import com.iqtb.Session.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CfdisRelacionadosDAO {

    private Transaction tx;
    private Session session;
    private static final Logger logger = Logger.getLogger(CfdisRelacionadosDAO.class);

    public CfdisRelacionadosPadre guardarPadre(CfdisRelacionadosPadre padre) {
        try {
            iniciarOperacion();
            session.save(padre);
            tx.commit();
            logger.debug("CfdisRelacionadosPadre guardado, id=" + padre.getIdCfdRelacionadoPadre()
                    + " tipoRelacion=" + padre.getTipoRelacion());
            return padre;
        } catch (HibernateException e) {
            manejarExcepcion(e, "guardarPadre idCfd=" + (padre.getCfds() != null ? padre.getCfds().getIdCfd() : "null"));
            return null;
        } finally {
            cerrarSesion();
        }
    }

    public CfdisRelacionadosHijo guardarHijo(CfdisRelacionadosHijo hijo) {
        try {
            iniciarOperacion();
            session.save(hijo);
            tx.commit();
            logger.debug("CfdisRelacionadosHijo guardado, id=" + hijo.getIdCfdRelacionadoHijo()
                    + " uuid=" + hijo.getUuid()
                    + " estado=" + hijo.getEstadoRelacion());
            return hijo;
        } catch (HibernateException e) {
            manejarExcepcion(e, "guardarHijo uuid=" + hijo.getUuid());
            return null;
        } finally {
            cerrarSesion();
        }
    }

    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }

    private void manejarExcepcion(HibernateException e, String contexto) {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (HibernateException ex) {
                logger.error("Error en rollback (" + contexto + "): " + ex.getMessage());
            }
        }
        logger.error("Error en " + contexto + ": " + e.getMessage());
    }

    private void cerrarSesion() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
