/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.DAOs;

import com.iqtb.POJOs.Cfds;
import com.iqtb.Session.HibernateUtil;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Joaquin
 */
public class CfdsDAO {

    private Transaction tx;
    private Session session;
    Logger logger = Logger.getLogger("CfdsDAO");

    public String getUUID(Long folio, Integer idEmpresa) {
        List<String> listUUIDs = null;
        try {
            iniciarOperacion();
            Query query = session.createQuery("select uuid from Cfds where folio=:folio and sucursalesByIdSucursal.empresas.idEmpresa=:idEmpresa");
            query.setParameter("folio", folio);
            query.setParameter("idEmpresa", idEmpresa);
            listUUIDs = query.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener el UUID del CFD con Folio: " + folio + " , idEmpresa: " + idEmpresa + ", ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        if (listUUIDs != null && listUUIDs.size() > 0) {
            if (listUUIDs.size() > 1) {
                logger.error("ERROR, Existe mas de un CFDI con el Folio: " + folio + " , idEmpresa: " + idEmpresa + ", Se tomara el primero: ");
            }
            return listUUIDs.get(0);
        }
        return null;
    }

    public String getUUID(String serie, Long folio, Integer idEmpresa) {
        List<String> listUUIDs = null;
        try {
            iniciarOperacion();
            Query query = session.createQuery("select uuid from Cfds where serie=:serie and folio=:folio and sucursalesByIdSucursal.empresas.idEmpresa=:idEmpresa");
            query.setParameter("serie", serie);
            query.setParameter("folio", folio);
            query.setParameter("idEmpresa", idEmpresa);
            listUUIDs = query.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al obtener el UUID del CFD con Serie: " + serie + " , Folio: " + folio + " , idEmpresa: " + idEmpresa + ", ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        if (listUUIDs != null && listUUIDs.size() > 0) {
            if (listUUIDs.size() > 1) {
                logger.error("ERROR, Existe mas de un CFDI con el Folio: " + folio + " , idEmpresa: " + idEmpresa + ", Se tomara el primero: ");
            }
            return listUUIDs.get(0);
        }
        return null;
    }

    private boolean existe(String serie, Long folio, Integer idSucursal) {
        boolean existe = false;
        List<Cfds> listCFDS = null;
        try {
            iniciarOperacion();
            listCFDS = session.createQuery("from Cfds where serie='" + serie + "' and folio=" + folio + " and sucursalesByIdSucursal.idSucursal=" + idSucursal).list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error al verificar si ya existe el CFD con Serie: " + serie + " , Folio: " + folio + " , idSucursal: " + idSucursal + ", ERROR: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        if (listCFDS != null && listCFDS.size() > 0) {
            return true;
        }
        return existe;
    }
    
    
    public Cfds obtenerUuid(String serie, Long folio, Integer idSucursal) {
        boolean existe = false;
        
        Cfds cfdReturn = null;
        List<Cfds> listCFDS = null;
        
        logger.info("Iniciando a comprobar la serie");
        
        if(serie.equals("SO")){
            
            logger.info("La serie es SO");
            try {
                iniciarOperacion();
                String hql = "from Cfds where (serie = :serie or serie = 'SA') "
                + "and folio = :folio "
                + "and sucursalesByIdSucursal.idSucursal = :idSucursal";

                listCFDS = session.createQuery(hql)
                .setParameter("serie", serie)
                .setParameter("folio", folio)
                .setParameter("idSucursal", idSucursal)
                .list();
                
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                logger.error("Error al verificar si ya existe el CFD con Serie: " + serie + " , Folio: " + folio + " , idSucursal: " + idSucursal + ", ERROR: " + e.getMessage());
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
        }else{
            
            try {
                logger.info("La serie probablemente es SH");
                iniciarOperacion();
                listCFDS = session.createQuery("from Cfds where tiposCfd.idTipocfd=1 and folio=" + folio + " and estadoFiscal='VIGENTE'").list();
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                logger.error("Error al verificar si ya existe el CFD con Serie: " + serie + " , Folio: " + folio + " , idSucursal: " + idSucursal + ", ERROR: " + e.getMessage());
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
            
        }
        
        if (listCFDS != null && listCFDS.size() > 0 && listCFDS.size() == 1) {
            
            logger.info("Se encontro la lista con cfds");
            cfdReturn=listCFDS.get(0);
            
        }else{
            
            logger.info("Buscando ahora con serie SA");
            serie="SA";
            try {
                iniciarOperacion();
                listCFDS = session.createQuery("from Cfds where serie='" + serie + "' and folio=" + folio + " and sucursalesByIdSucursal.idSucursal=" + idSucursal).list();
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                logger.error("Error al verificar si ya existe el CFD con Serie: " + serie + " , Folio: " + folio + " , idSucursal: " + idSucursal + ", ERROR: " + e.getMessage());
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
            
            if (listCFDS != null && listCFDS.size() > 0 && listCFDS.size() == 1) {
            
                logger.info("Se encontro la lista con cfds");
                cfdReturn=listCFDS.get(0);
            
            }
            
        }
        return cfdReturn;
    }
    
    
    public Cfds comprobarExistente(String serie, Long folio, Integer idSucursal) {
        boolean existe = false;
        
        Cfds cfdReturn = null;
        List<Cfds> listCFDS = null;
        
        logger.info("Iniciando a comprobar la serie");
        
        logger.info("La serie es SO");
        try {
            iniciarOperacion();
            String hql = "from Cfds where serie = :serie "
            + "and folio = :folio "
            + "and sucursalesByIdSucursal.idSucursal = :idSucursal";

            listCFDS = session.createQuery(hql)
            .setParameter("serie", serie)
            .setParameter("folio", folio)
            .setParameter("idSucursal", idSucursal)
            .list();
                
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                logger.error("Error al verificar si ya existe el CFD con Serie: " + serie + " , Folio: " + folio + " , idSucursal: " + idSucursal + ", ERROR: " + e.getMessage());
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
        
        
        if (listCFDS != null && listCFDS.size() > 0 && listCFDS.size() == 1) {
            
            logger.info("Se encontro la lista con cfds");
            cfdReturn=listCFDS.get(0);
            
        }else{
            
            logger.info("No se encontro el cfd, CONTINUANDO EL PROCESO");
            
        }
        
        return cfdReturn;
    }
    
    
    

    public List<Integer> getIdCfdsPendientesPorTipo(String tipoCfd, int limite) {
        List<Integer> resultado = Collections.emptyList();
        try {
            iniciarOperacion();
            Query query = session.createQuery(
                    "select idCfd from Cfds " +
                    "where tipoCfd = :tipoCfd " +
                    "and fechaPedimento is null");
            query.setParameter("tipoCfd", tipoCfd);
            query.setMaxResults(limite);
            resultado = query.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error en getIdCfdsPendientesPorTipo tipoCfd=" + tipoCfd
                    + ": " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
        return resultado;
    }
    
     public List<Integer> getIdCfdsPendientesPorTipoAndFechaPedimento(
        String tipoCfd, int limite, Integer ultimoIdCfd) {
    List<Integer> resultado = Collections.emptyList();
    try {
        iniciarOperacion();
        String hql = "select idCfd from Cfds " +
                     "where tipoCfd = :tipoCfd " +
                     "and (fechaPedimento is null   or fechaPedimento = 'PENDIENTE_REVISION') " +
                     (ultimoIdCfd != null ? "and idCfd > :ultimoIdCfd " : "") + "order by idCfd asc";
        Query query = session.createQuery(hql);
        query.setParameter("tipoCfd", tipoCfd);
        if (ultimoIdCfd != null) {
            query.setParameter("ultimoIdCfd", ultimoIdCfd);
        }
        query.setMaxResults(limite);
        resultado = query.list();
        tx.commit();
    } catch (HibernateException e) {
        tx.rollback();
        logger.error("Error en getIdCfdsPendientesPorTipoAndFechaPedimento: " + e.getMessage());
    } finally {
        if (session.isOpen()) session.close();
    }
    return resultado;
}




    public Integer getIdCfdByUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        List<Integer> lista = null;
        try {
            iniciarOperacion();
            Query query = session.createQuery(
                    "select idCfd from Cfds where uuid = :uuid");
            query.setParameter("uuid", uuid);
            lista = query.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error en getIdCfdByUuid uuid=" + uuid + ": " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
        if (lista != null && !lista.isEmpty()) {
            if (lista.size() > 1) {
                logger.warn("Existe más de un CFDI con UUID=" + uuid + ". Se toma el primero.");
            }
            return lista.get(0);
        }
        return null;
    }



    public String getTipoCfd(Integer idCfd) {
        if (idCfd == null) return null;
        List<String> lista = null;
        try {
            iniciarOperacion();
            Query query = session.createQuery(
                    "select tipoCfd from Cfds where idCfd = :idCfd");
            query.setParameter("idCfd", idCfd);
            lista = query.list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error en getTipoCfd idCfd=" + idCfd + ": " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
        return (lista != null && !lista.isEmpty()) ? lista.get(0) : null;
    }
    
    public String getFechaPedimento(Integer idCfd) {
       String resultado = null;
        try {
            iniciarOperacion();
            String hql = "select fechaPedimento from Cfds where idCfd = :idCfd";
            Query query = session.createQuery(hql);
            query.setParameter("idCfd", idCfd);
            resultado = (String) query.uniqueResult();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error en getFechaPedimento idCFD=" + idCfd + ": " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
        return resultado;
    }



    public void actualizarFechaPedimento(Integer idCfd, String valor) {
        try {
            iniciarOperacion();
            Query query = session.createQuery(
                    "update Cfds set fechaPedimento = :valor where idCfd = :idCfd");
            query.setParameter("valor", valor);
            query.setParameter("idCfd", idCfd);
            query.executeUpdate();
            tx.commit();
            logger.debug("FECHA_PEDIMENTO actualizada a '" + valor + "' para idCFD=" + idCfd);
        } catch (HibernateException e) {
            tx.rollback();
            logger.error("Error en actualizarFechaPedimento idCfd=" + idCfd
                    + ": " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    
    public BigDecimal obtenerTotalCfdiE(Integer idCfdiPadre) {

        BigDecimal total = BigDecimal.ZERO;

        try {

            iniciarOperacion();

            Query query = session.createQuery(
                "select c.total " +
                "from Cfds c " +
                "where c.idCfd = :idCfdi"
            );

            query.setParameter("idCfdi", idCfdiPadre);

            BigDecimal resultado = (BigDecimal) query.uniqueResult();

            if (resultado != null) {
                total = resultado;
            }

            tx.commit();

        } catch (HibernateException e) {

            tx.rollback();
            logger.error("Error obtenerTotalCfdiE: " + e.getMessage());

        } finally {

            if (session.isOpen()) session.close();
        }

        return total;
    }
    


    private void iniciarOperacion() throws HibernateException {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();
    }
}