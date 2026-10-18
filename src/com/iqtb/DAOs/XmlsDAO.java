/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iqtb.DAOs;

import com.iqtb.POJOs.Xmls;
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
public class XmlsDAO {
    
    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger("XmlsDAO");
    
    public XmlsDAO(){
        //Constructor vacio
    }
    
    public Xmls verificarXmls(int idCfd){
        
        List<Xmls> listaXmls = null;
        Xmls xml = null;
        
        try {
            iniciarOperacion();
            String hql = "from Xmls where idCfd=" + idCfd ;
            logger.info("Consulta: " + hql);
            Query query = session.createQuery(hql);
            listaXmls = query.list();
            tx.commit();
            
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener el servicio, ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        
        if(listaXmls.size()>0){
            
            xml= listaXmls.get(0);
            
            logger.info("Se encontro el xml");
            
        }
        
        return xml;
    }
    
    
    
    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }
    
}
