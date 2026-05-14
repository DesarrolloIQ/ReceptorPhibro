/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.DAOs;

import com.iqtb.POJOs.ErroresGeneracion;
import com.iqtb.Session.HibernateUtil;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Joaquin
 */
public class ErroresGeneracionDAO {
    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger(ErroresGeneracionDAO.class);

    public void eliminar(int idArchivo){
        try{
            List<ErroresGeneracion> listErrores = errores(idArchivo);
            iniciarOperacion();
            if(listErrores!=null&&listErrores.size()>0){
                logger.info("idArchivo: "+idArchivo);
                logger.info("Errores por eliminar: "+listErrores.size());
                for(int i=0;i<listErrores.size();i++){
                    ErroresGeneracion error = (ErroresGeneracion)listErrores.get(i);
                    int id = error.getIdError();
                    session.delete(error);
                    logger.info("Se elimino correctamente el error de generacion con id: "+id);
                }
                tx.commit();
            }else{
                logger.error("No se encontraron errores a eliminar");
            }
        }catch(HibernateException e){
            tx.rollback();
            logger.error("ERROR: "+e.getMessage());
        }
        finally{
            //session.flush();
            //session.clear();
            if(session.isOpen())
                session.close();
        }
    }
    
    private List<ErroresGeneracion> errores(int idArchivo){
        List<ErroresGeneracion> listErrores = null;
        try {
            iniciarOperacion();
            listErrores = session.createQuery("select e from ErroresGeneracion as e "
                    + "where e.documentosRecibidos.idArchivo = "+idArchivo+" ").list();
                    //+ "and e.descripcionXml like '%Content is not allowed in prolog.%'").list();
            tx.commit();
            
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("ERROR: "+e.getMessage());
        }
        finally{
            if(session.isOpen())
                session.close();
        }
        return listErrores;
    }
    
    public boolean nuevo(ErroresGeneracion errorGen){
        boolean result=false;
        try{
            iniciarOperacion();
            session.save(errorGen);
            tx.commit();
            result=true;
        }catch(HibernateException e){
            result=false;
            tx.rollback();
            logger.error("ERROR: "+e.getMessage());
        }
        finally{
            //session.flush();
            //session.clear();
            if(session.isOpen())
                session.close();
        }
        return result;
    }
    
    private void iniciarOperacion() throws HibernateException{
        session = HibernateUtil.getSessionFactory().openSession();
        tx= session.beginTransaction();
    }
}
