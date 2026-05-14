package com.iqtb.DAOs;

import com.iqtb.POJOs.CfdisPagos;
import com.iqtb.POJOs.Pagos;
import com.iqtb.Session.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class PagosDAO {

    private Transaction tx;
    private Session session;
    private static final Logger logger = Logger.getLogger(PagosDAO.class);

    public CfdisPagos guardarCfdisPagos(CfdisPagos cfdisPagos) {
    try {
        iniciarOperacion();
        session.save(cfdisPagos);
        tx.commit();
        return cfdisPagos;
    } catch (HibernateException e) {
        manejarExcepcion(e, "guardarCfdisPagos");
        // Agrega esto para ver la causa raíz:
        if (e.getCause() != null) {
            logger.error("Causa raíz: " + e.getCause().getMessage());
        }
        return null;
    } finally {
        cerrarSesion();
    }
}

    public Pagos guardarPago(Pagos pago) {
        try {
            iniciarOperacion();
            session.save(pago);
            tx.commit();
            logger.debug("Pago guardado, idPago=" + pago.getIdPago());
            return pago;
        } catch (HibernateException e) {
            manejarExcepcion(e, "guardarCfdisPagos");
            // Agrega esto para ver la causa raíz:
            if (e.getCause() != null) {
                logger.error("Causa raíz: " + e.getCause().getMessage());
            }
            return null;
        } finally {
            cerrarSesion();
        }
    }

    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }

    private void manejarExcepcion(HibernateException e, String metodo) {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (HibernateException ex) {
                logger.error("Error al hacer rollback en " + metodo + ": " + ex.getMessage());
            }
        }
        logger.error("Error en " + metodo + ": " + e.getMessage());
    }

    private void cerrarSesion() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
