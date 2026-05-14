package com.iqtb.DAOs;

import com.iqtb.POJOs.Clientes;
import com.iqtb.Session.HibernateUtil;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author macminizuri
 */
public class RegimenDAO {
    
    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger("RegimenFiscalDAO");
    
    
    public String getIdentificador(Integer idRegimen) {
        String claveRegimen="";
        List<String> listaString = null;
        try {
            iniciarOperacion();
            String hql = "select claveRegimenFiscal from RegimenFiscal  where idRegimen = " + idRegimen;
            Query q = session.createQuery(hql);
            listaString = q.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener el servicio: " + idRegimen + " , ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        
        if (listaString != null && listaString.size() > 0) {
            
            
            logger.info("Se obtuvo, obteniendo clave regimen");
            String rfcCliente = listaString.get(0);
                
            logger.info("RFC");
            return rfcCliente;
        }
        
        return null;
    }
    
    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }
    
    
}