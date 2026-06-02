package com.iqtb.DAOs;

import com.iqtb.POJOs.CfdisRelacionadosPadre;
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
    
    
    //nuevos  DAO para sprint 28
    public List<CfdisRelacionadosPadre> getCfdisEPadreRelacionados(Integer idCfdiIngreso) {

        List<CfdisRelacionadosPadre> resultado = new ArrayList<>();

        try {

            iniciarOperacion();

            Query query = session.createQuery(
                "select distinct padre " +
                "from CfdisRelacionadosHijo hijo " +
                "join hijo.cfdisRelacionadosPadre padre " +
                "join fetch padre.cfds cfdPadre " +
                "where hijo.cfds.idCfd = :idCfdiIngreso " +
                "and hijo.estadoRelacion = 'VALIDO' " +
                "and hijo.tipoCfdHijo = 'I' " +
                "and padre.tipoCfdPadre = 'E'"
            );

            query.setParameter("idCfdiIngreso", idCfdiIngreso);

            resultado = query.list();

            tx.commit();

        } catch (HibernateException e) {

            tx.rollback();
            logger.error("Error getCfdisEPadreRelacionados: " + e.getMessage());

        } finally {

            cerrarSesion();
        }
        
        if(resultado.size()>0){
            
            logger.info("Se encontraron Cfdis de egreso con hijos I: " + resultado.size());
            
        }

        return resultado;
    }
    
    public Long contarHijosTipoI(Integer idCfdiRelacionadoPadre) {

        Long total = 0L;

        try {

            iniciarOperacion();

            Query query = session.createQuery(
                "select count(hijo.idCfdRelacionadoHijo) " +
                "from CfdisRelacionadosHijo hijo " +
                "where hijo.cfdisRelacionadosPadre.idCfdRelacionadoPadre = :idPadre " +
                "and hijo.tipoCfdHijo = 'I' " +
                "and hijo.estadoRelacion = 'VALIDO'"
            );

            query.setParameter("idPadre", idCfdiRelacionadoPadre);

            total = (Long) query.uniqueResult();

            tx.commit();

        } catch (HibernateException e) {

            tx.rollback();
            logger.error("Error contarHijosTipoI: " + e.getMessage());

        } finally {

            cerrarSesion();
        }

        return total;
    }
    
    /**
     * Retorna los datos del CFDI-P original (serie, folio, uuid, fecha) que ya
     * dejó en cero el saldo insoluto del comprobante indicado, excluyendo el
     * CFDI-P que se está procesando ahora.
     * Cada elemento del resultado es Object[]{serie, folio, uuid, fecha}.
     * Lista vacía significa que no hay duplicado.
     */
    public List<Object[]> getPagosCompletosVigentes(String uuidDocRelacionado, Integer idCfdPagoExcluir) {
        List<Object[]> resultado = new ArrayList<>();
        try {
            iniciarOperacion();
            Query query = session.createQuery(
                "select c.serie, c.folio, c.uuid, c.fecha " +
                "from DocumentosRelacionadosP drp " +
                "join drp.pagos p " +
                "join p.cfdisPagos cp " +
                "join cp.cfds c " +
                "where drp.idDocumento = :uuid " +
                "and drp.estadoRelacion = 'VALIDO' " +
                "and drp.importeSaldoInsoluto = 0 " +
                "and c.idCfd != :idCfdPagoExcluir " +
                "and c.estado != 'CANCELADO'"
            );
            query.setParameter("uuid", uuidDocRelacionado);
            query.setParameter("idCfdPagoExcluir", idCfdPagoExcluir);
            resultado = query.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error en getPagosCompletosVigentes uuid=" + uuidDocRelacionado
                    + ": " + e.getMessage());
        } finally {
            cerrarSesion();
        }
        return resultado;
    }

    private void cerrarSesion() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
