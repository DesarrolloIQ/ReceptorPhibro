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
public class ClientesDAO {
 
    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger("ClientesDAO");
    
    public String getCliente(String rfc, String identificadorCliente) {
        List<Clientes> result = null;
        try {
            iniciarOperacion();
            String hql = "from Clientes  where rfc = '" + rfc + "' and identificador ='" + identificadorCliente + "'";
            Query q = session.createQuery(hql);
            result = q.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener el servicio: " + rfc + " , ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        
        if (result != null && result.size() > 0) {

            logger.info("Se obtuvo, obteniendo clave regimen");
            Clientes cliente = result.get(0);
            Integer idRegimen = cliente.getIdRegimen();
            
            logger.info("El idRegimen es:" + idRegimen);
            
            RegimenDAO regimenDAO = new RegimenDAO();
            
            String claveRegimen = regimenDAO.getIdentificador(idRegimen);
                
            logger.info("regimen: " + claveRegimen);
            return claveRegimen;
        }
        
        return null;
    }
    
    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }
}