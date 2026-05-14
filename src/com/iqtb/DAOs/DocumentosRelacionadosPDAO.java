package com.iqtb.DAOs;

import com.iqtb.POJOs.DocumentosRelacionadosP;
import com.iqtb.Session.HibernateUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DocumentosRelacionadosPDAO {

    private Transaction tx;
    private Session session;
    private static final Logger logger = Logger.getLogger(DocumentosRelacionadosPDAO.class);
    
public List<DocumentosRelacionadosP> getPagosDeIngreso(Integer idCfdIngreso) {
        List<DocumentosRelacionadosP> resultado = new ArrayList<>();
        try {
            iniciarOperacion();
            Query query = session.createQuery(
                "from DocumentosRelacionadosP drp " +
                "join fetch drp.pagos p " +
                "join fetch p.cfdisPagos cp " +
                "join fetch cp.cfds c " +
                "where drp.cfds.idCfd = :idCfdIngreso " +
                "and drp.estadoRelacion = 'VALIDO' " +
                "and c.estado != 'CANCELADO' " +
                "order by drp.numParcialidad asc");
            query.setParameter("idCfdIngreso", idCfdIngreso);
            resultado = query.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error en getPagosDeIngreso: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
        return resultado;
    }

    public DocumentosRelacionadosP guardar(DocumentosRelacionadosP doc) {
        try {
            iniciarOperacion();
            session.save(doc);
            tx.commit();
            logger.debug("DocumentosRelacionadosP guardado, id=" + doc.getIdDocumentoRelacionadoP()
                    + " uuid=" + doc.getIdDocumento());
            return doc;
        } catch (HibernateException e) {
            manejarExcepcion(e, "guardar DocumentosRelacionadosP uuid=" + doc.getIdDocumento());
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
