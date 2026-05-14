/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.DAOs;

import com.iqtb.POJOs.DocumentosRecibidos;
import com.iqtb.Session.HibernateUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Joaquin
 */
public class Documentos_RecibidosDAO {

    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger("Documentos_RecibidosDAO");


    public DocumentosRecibidos guardar(DocumentosRecibidos documento) {
        logger.info("Guardardando el Documento Recibido: " + documento.getNombre());
        try {
            iniciarOperacion();
            session.save(documento);
            session.flush();
            session.refresh(documento);
            tx.commit();
            if (documento.getIdArchivo() != null) {
                return documento;
            } else {
                return null;
            }
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al Actualizar el Documento Recibido: " + documento.getNombre() + " , ERROR: " + e.getMessage());
            return null;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }
}
