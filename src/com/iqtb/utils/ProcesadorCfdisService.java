package com.iqtb.utils;

import com.iqtb.DAOs.CfdsDAO;
import com.iqtb.DAOs.CfdisRelacionadosDAO;
import com.iqtb.DAOs.DocumentosRelacionadosPDAO;
import com.iqtb.DAOs.PagosDAO;
import com.iqtb.DAOs.XmlsDAO;
import com.iqtb.POJOs.Cfds;
import com.iqtb.POJOs.CfdisPagos;
import com.iqtb.POJOs.CfdisRelacionadosHijo;
import com.iqtb.POJOs.CfdisRelacionadosPadre;
import com.iqtb.POJOs.DocumentosRelacionadosP;
import com.iqtb.POJOs.Pagos;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.PropertyConfigurator;



/**
 * Servicio que implementa los Sprints 2(issue 14 y 15) y 4(issue 16 y 17):
 *
 * - Sprint 2: Procesa CFDIs de PAGO (TIPO_CFD='P') con FECHA_PEDIMENTO=NULL,
 * relaciona cada pago con sus DoctoRelacionados y llena las tablas CFDIS_PAGOS,
 * PAGOS y DOCUMENTOS_RELACIONADOS_P.
 *
 * - Sprint 4: Procesa CFDIs de EGRESO (TIPO_CFD='E') con FECHA_PEDIMENTO=NULL,
 * relaciona cada CFDI con los CFDIs que referencia en cfdi:CfdiRelacionados y
 * llena las tablas CFDIS_RELACIONADOS_PADRE y CFDIS_RELACIONADOS_HIJO.
 *
 */
public class ProcesadorCfdisService {

    private static final Logger logger = Logger.getLogger(ProcesadorCfdisService.class);

    // Namespaces usados en los XPath
    private static final String NS_PAGO20 = "http://www.sat.gob.mx/Pagos20";
    private static final String NS_PAGO10 = "http://www.sat.gob.mx/Pagos";

    // Límite de registros por lote para no saturar memoria
    private static final int BATCH_SIZE = 200;

    private final CfdsDAO cfdsDAO = new CfdsDAO();
    private final XmlsDAO xmlsDAO = new XmlsDAO();
    private final PagosDAO pagosDAO = new PagosDAO();
    private final DocumentosRelacionadosPDAO docRelPDAO = new DocumentosRelacionadosPDAO();
    private final CfdisRelacionadosDAO cfdisRelDAO = new CfdisRelacionadosDAO();
    
    /*public static void main(String[] args) {
                PropertyConfigurator.configure("configReceptorPhilbro/log4j.properties");
        
         //En cada corrida se verifica que los Cfds de PAGO o EGRESO ya esten revisados en BD
        ProcesadorCfdisService procesadorCfdisService = new ProcesadorCfdisService();
        procesadorCfdisService.procesarCfdisDeIngreso();
    }*/

    // =========================================================================
    // SPRINT 2 – CFDIs de PAGO
    // =========================================================================
    /**
     * Obtiene los CFDIs de tipo P con FECHA_PEDIMENTO=NULL en lotes de
     * BATCH_SIZE y los procesa hasta que no queden registros pendientes.
     */
    public void procesarCfdisDePago() {
        logger.debug("=== INICIO Sprint 2: procesarCfdisDePago ===");
        List<Integer> lote;

        do {
            lote = cfdsDAO.getIdCfdsPendientesPorTipo("P", BATCH_SIZE);
            if (lote == null || lote.isEmpty()) {
                break;
            }
            logger.debug("Lote de " + lote.size() + " CFDIs de pago por procesar.");
            logger.debug("");
            for (Integer idCfdPago : lote) {
                procesarUnCfdiDePago(idCfdPago);
            }
        } while (lote.size() == BATCH_SIZE); // si vino lleno puede haber más

        logger.debug("=== FIN Sprint 2: procesarCfdisDePago ===");
    }

    /**
     * Procesa un único CFDI de pago: 1. Marca FECHA_PEDIMENTO = 'NO_APLICA' de
     * inmediato para evitar reproceso. 2. Valida versión 4.0. 3. Llena
     * CFDIS_PAGOS, PAGOS y delega en BuscarDocumentosRelacionados.
     */
    private void procesarUnCfdiDePago(Integer idCfdPago) {
        // Marcar de inmediato para evitar reproceso ante fallo a mitad
        cfdsDAO.actualizarFechaPedimento(idCfdPago, "NO_APLICA");

        String xmlSat = xmlsDAO.getXmlSat(idCfdPago);
        if (xmlSat == null || xmlSat.isEmpty()) {
            logger.error("No se encontró XML_SAT para idCFD=" + idCfdPago);
            return;
        }

        String nsCfdi = detectarNamespaceCfdi(xmlSat);

        Document doc = parsearXml(xmlSat);
        if (doc == null) {
            logger.error("No se pudo parsear el XML del idCFD=" + idCfdPago);
            return;
        }

        String version = leerAtributo(doc, "/cfdi:Comprobante/@Version", nsCfdi);
        String serie = leerAtributo(doc, "/cfdi:Comprobante/@Serie", nsCfdi);
        String folio = leerAtributo(doc, "/cfdi:Comprobante/@Folio", nsCfdi);

        if (version == null || version.isEmpty() || Double.parseDouble(version) < 4.0) {
            logger.debug("CFDI " + serie + "-" + folio + " es versión " + version + ", no se trabaja con él.");
            return;
        }

        // Llenar tablas del complemento de pagos
        CfdisPagos cfdisPagos = llenarTablaCfdisPagos(idCfdPago, doc, nsCfdi);
        if (cfdisPagos == null) {
            logger.error("No se pudo guardar CfdisPagos para idCFD=" + idCfdPago);
            return;
        }

        llenarTablaPagos(idCfdPago, cfdisPagos, doc, nsCfdi);
        logger.debug("\n");
    }

    /**
     * Llena la tabla CFDIS_PAGOS con los totales del nodo pago20:Totales.
     */
    private CfdisPagos llenarTablaCfdisPagos(Integer idCfdPago, Document doc, String nsCfd) {
        CfdisPagos cp = new CfdisPagos();

        Cfds cfds = new Cfds();
        cp.setIdCfdiPago(idCfdPago);
        cfds.setIdCfd(idCfdPago);
        cp.setCfds(cfds);

        String xpathTotales = "/cfdi:Comprobante/cfdi:Complemento/pago20:Pagos/pago20:Totales";
        cp.setTotalRetencionesIva(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalRetencionesIVA", nsCfd)));
        cp.setTotalRetencionesIsr(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalRetencionesISR", nsCfd)));
        cp.setTotalRetencionesIeps(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalRetencionesIEPS", nsCfd)));
        cp.setTotalTrasladosBaseIva16(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalTrasladosBaseIVA16", nsCfd)));
        cp.setTotalTrasladosImpuestoIva16(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalTrasladosImpuestoIVA16", nsCfd)));
        cp.setTotalTrasladosBaseIva8(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalTrasladosBaseIVA8", nsCfd)));
        cp.setTotalTrasladosImpuestoIva8(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalTrasladosImpuestoIVA8", nsCfd)));
        cp.setTotalTrasladosBaseIva0(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalTrasladosBaseIVA0", nsCfd)));
        cp.setTotalTrasladosImpuestoIvaExento(toBigDecimal(leerAtributo(doc, xpathTotales + "/@TotalTrasladosImpuestoIVAExento", nsCfd)));
        cp.setMontoTotalPagos(toBigDecimal(leerAtributo(doc, xpathTotales + "/@MontoTotalPagos", nsCfd)));

        return pagosDAO.guardarCfdisPagos(cp);
    }

    /**
     * Llena la tabla PAGOS iterando sobre cada nodo pago20:Pago del XML. Por
     * cada pago llama a buscarDocumentosRelacionados para los DoctoRelacionado.
     */
    private void llenarTablaPagos(Integer idCfdPago, CfdisPagos cfdisPagos, Document doc, String nsCfd) {
        try {
            XPath xp = construirXPath(nsCfd);
            XPathExpression exprPagos = xp.compile(
                    "/cfdi:Comprobante/cfdi:Complemento/pago20:Pagos/pago20:Pago");
            NodeList nodoPagos = (NodeList) exprPagos.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodoPagos.getLength(); i++) {
                Node nodoPago = nodoPagos.item(i);
                Pagos pago = new Pagos();
                pago.setCfdisPagos(cfdisPagos);
                pago.setVersion(new BigDecimal("2.0")); // complemento pago20

                pago.setFechaPago(toDate(attrDe(nodoPago, "FechaPago")));
                pago.setFormaPago(attrDe(nodoPago, "FormaDePagoP"));
                pago.setMonedaPago(attrDe(nodoPago, "MonedaP"));
                pago.setTipoCambioPago(toBigDecimal(attrDe(nodoPago, "TipoCambioP")));
                pago.setMontoPago(toBigDecimal(attrDe(nodoPago, "Monto")));
                pago.setNumOperacion(attrDe(nodoPago, "NumOperacion"));
                pago.setRfcEmisorCtaOrd(attrDe(nodoPago, "RfcEmisorCtaOrd"));
                pago.setNombreBancoOrdExtranjero(attrDe(nodoPago, "NomBancoOrdExt"));
                pago.setCuentaOrdenante(attrDe(nodoPago, "CtaOrdenante"));
                pago.setRfcEmisorCtaBeneficiario(attrDe(nodoPago, "RfcEmisorCtaBen"));
                pago.setCuentaBeneficiario(attrDe(nodoPago, "CtaBeneficiario"));

                Pagos pagoGuardado = pagosDAO.guardarPago(pago);
                if (pagoGuardado == null) {
                    logger.error("No se pudo guardar Pago #" + i + " del idCFD=" + idCfdPago);
                    continue;
                }

                // Procesar DoctoRelacionado dentro de este pago
                buscarDocumentosRelacionados(idCfdPago, pagoGuardado, nodoPago, xp);
            }
        } catch (Exception e) {
            logger.error("Error en llenarTablaPagos idCFD=" + idCfdPago + ": " + e.getMessage());
        }
    }

    /**
     * Sprint 2 – BuscarDocumentosRelacionados Itera sobre los nodos
     * pago20:DoctoRelacionado dentro de un pago20:Pago. Para cada uno busca el
     * CFDI en BD y llena DOCUMENTOS_RELACIONADOS_P.
     */
    private void buscarDocumentosRelacionados(Integer idCfdPago, Pagos pagoGuardado,
            Node nodoPago, XPath xp) {
        try {
            XPathExpression exprDocs = xp.compile("pago20:DoctoRelacionado");
            NodeList doctos = (NodeList) exprDocs.evaluate(nodoPago, XPathConstants.NODESET);

            for (int j = 0; j < doctos.getLength(); j++) {
                Node nodoDoc = doctos.item(j);
                String uuidDocRelacionado = attrDe(nodoDoc, "IdDocumento");

                // Buscar el CFDI de ingreso en BD por UUID
                Integer idCfdIngreso = cfdsDAO.getIdCfdByUuid(uuidDocRelacionado);

                DocumentosRelacionadosP drp = new DocumentosRelacionadosP();
                drp.setPagos(pagoGuardado);
                if (uuidDocRelacionado != null && uuidDocRelacionado.length() > 36) {
                    logger.error("Demasiado larga la cadena para  UUID: " + uuidDocRelacionado);
                }
                drp.setIdDocumento(uuidDocRelacionado);
                drp.setSerie(attrDe(nodoDoc, "Serie"));
                drp.setFolio(attrDe(nodoDoc, "Folio"));
                drp.setMonedaDr(attrDe(nodoDoc, "MonedaDR"));
                drp.setEquivalenciaDr(toBigDecimal(attrDe(nodoDoc, "EquivalenciaDR")));
                drp.setNumParcialidad(toInt(attrDe(nodoDoc, "NumParcialidad")));
                drp.setImporteSaldoAnterior(toBigDecimal(attrDe(nodoDoc, "ImpSaldoAnt")));
                drp.setImportePagado(toBigDecimal(attrDe(nodoDoc, "ImpPagado")));
                drp.setImporteSaldoInsoluto(toBigDecimal(attrDe(nodoDoc, "ImpSaldoInsoluto")));
                drp.setObjetoImpuestosDr(attrDe(nodoDoc, "ObjetoImpDR"));

                if (idCfdIngreso != null) {
                    Cfds cfdRelacionado = new Cfds();
                    cfdRelacionado.setIdCfd(idCfdIngreso);
                    drp.setCfds(cfdRelacionado);
                    drp.setEstadoRelacion("VALIDO");
                    logger.debug("Documento relacionado uuid=" + uuidDocRelacionado
                            + " -> idCFD=" + idCfdIngreso + " (VALIDO)");
                } else {
                    drp.setEstadoRelacion("ERROR");
                    drp.setDescripcionError("UUID " + uuidDocRelacionado + " no encontrado en BD");
                    logger.error("Documento relacionado " + uuidDocRelacionado
                            + " del pago idCFD=" + idCfdPago + " no está en BD");
                }

                docRelPDAO.guardar(drp);
            }
        } catch (Exception e) {
            logger.error("Error en buscarDocumentosRelacionados idCFD=" + idCfdPago
                    + ": " + e.getMessage());
        }
    }

    // =========================================================================
    // SPRINT 4 – CFDIs de EGRESO
    // =========================================================================
    /**
     * Obtiene los CFDIs de tipo E con FECHA_PEDIMENTO=NULL en lotes de
     * BATCH_SIZE.
     */
    public void procesarCfdisDeEgreso() {
        
        logger.debug("=== INICIO Sprint 4: procesarCfdisDeEgreso ===");
        List<Integer> lote;

        do {
            lote = cfdsDAO.getIdCfdsPendientesPorTipo("E", BATCH_SIZE);
            if (lote == null || lote.isEmpty()) {
                break;
            }
            logger.debug("Lote de " + lote.size() + " CFDIs de egreso por procesar.");
            logger.debug("");
            for (Integer idCfdEgreso : lote) {
                procesarUnCfdiDeEgreso(idCfdEgreso);
            }
        } while (lote.size() == BATCH_SIZE);

        logger.debug("=== FIN Sprint 4: procesarCfdisDeEgreso ===");
        
    }

    /**
     * Procesa un único CFDI de egreso: 1. Marca FECHA_PEDIMENTO = 'NO_APLICA'.
     * 2. Valida versión 4.0. 3. Llena CFDIS_RELACIONADOS_PADRE y delega en
     * buscarCfdisRelacionados.
     */
    private void procesarUnCfdiDeEgreso(Integer idCfdEgreso) {
        cfdsDAO.actualizarFechaPedimento(idCfdEgreso, "NO_APLICA");

        String xmlSat = xmlsDAO.getXmlSat(idCfdEgreso);
        if (xmlSat == null || xmlSat.isEmpty()) {
            logger.error("No se encontró XML_SAT para idCFD=" + idCfdEgreso);
            return;
        }

        String nsCfdi = detectarNamespaceCfdi(xmlSat);

        Document doc = parsearXml(xmlSat);
        if (doc == null) {
            logger.error("No se pudo parsear el XML del idCFD=" + idCfdEgreso);
            return;
        }

        String version = leerAtributo(doc, "/cfdi:Comprobante/@Version", nsCfdi);
        String tipoCfd = leerAtributo(doc, "/cfdi:Comprobante/@TipoDeComprobante", nsCfdi);

        if (version == null || version.isEmpty() || tipoCfd==null || tipoCfd.isEmpty() || !tipoCfd.equals("E") || Double.parseDouble(version) < 4.0) {
            logger.debug("CFDI idCFD=" + idCfdEgreso + " es versión " + version + " y tipoCfd "+tipoCfd+", no se trabaja con él.");
            return;
        }

        // Llenar padre y luego hijos
        llenarTablaCfdisRelacionadosPadre(idCfdEgreso, doc, nsCfdi);
         logger.debug("\n");
    }

    /**
     * Itera los bloques cfdi:CfdiRelacionados del XML (puede haber más de uno
     * si tienen distinto TipoRelacion) y por cada uno crea un registro PADRE
     * antes de procesar sus hijos.
     */
    private void llenarTablaCfdisRelacionadosPadre(Integer idCfdEgreso, Document doc, String nsCfdi) {
        try {
            XPath xp = construirXPath(nsCfdi);
            XPathExpression exprBloques = xp.compile(
                    "/cfdi:Comprobante/cfdi:CfdiRelacionados");
            NodeList bloques = (NodeList) exprBloques.evaluate(doc, XPathConstants.NODESET);

            for (int b = 0; b < bloques.getLength(); b++) {
                Node bloque = bloques.item(b);

                CfdisRelacionadosPadre padre = new CfdisRelacionadosPadre();
                Cfds cfds = new Cfds();
                cfds.setIdCfd(idCfdEgreso);
                padre.setCfds(cfds);
                padre.setTipoCfdPadre("E");
                padre.setTipoRelacion(attrDe(bloque, "TipoRelacion"));

                CfdisRelacionadosPadre padreGuardado = cfdisRelDAO.guardarPadre(padre);
                if (padreGuardado == null) {
                    logger.error("No se pudo guardar CfdisRelacionadosPadre para idCFD=" + idCfdEgreso);
                    continue;
                }

                buscarCfdisRelacionados(idCfdEgreso, padreGuardado, bloque, xp);
            }
        } catch (Exception e) {
            logger.error("Error en llenar TablaCfdisRelacionadosPadre idCFD=" + idCfdEgreso
                    + ": " + e.getMessage());
        }
    }

    /**
     * Sprint 4 – BuscarCfdisRelacionados Itera los nodos cfdi:CfdiRelacionado
     * dentro del bloque cfdi:CfdiRelacionados. Para cada UUID busca el CFDI en
     * BD y llena CFDIS_RELACIONADOS_HIJO.
     */
    private void buscarCfdisRelacionados(Integer idCfdEgreso, CfdisRelacionadosPadre padre,
            Node bloqueRelac, XPath xp) {
        try {
            XPathExpression exprHijos = xp.compile("cfdi:CfdiRelacionado");
            NodeList hijos = (NodeList) exprHijos.evaluate(bloqueRelac, XPathConstants.NODESET);

            for (int h = 0; h < hijos.getLength(); h++) {
                Node nodoHijo = hijos.item(h);
                String uuidRelacionado = attrDe(nodoHijo, "UUID");
                if (uuidRelacionado != null) {
                    uuidRelacionado = uuidRelacionado.trim();
                }
                Integer idCfdRelacionado = cfdsDAO.getIdCfdByUuid(uuidRelacionado);

                CfdisRelacionadosHijo hijo = new CfdisRelacionadosHijo();
                hijo.setCfdisRelacionadosPadre(padre);
                
                if (uuidRelacionado != null && uuidRelacionado.length() > 36) {
                    logger.error("Demasiado larga la cadena para  UUID:{" + uuidRelacionado + "}");
                }
                hijo.setUuid(uuidRelacionado);

                if (idCfdRelacionado != null) {
                    Cfds cfdHijo = new Cfds();
                    cfdHijo.setIdCfd(idCfdRelacionado);
                    hijo.setCfds(cfdHijo);
                    // Determinar tipo del CFDI relacionado consultando BD
                    String tipoCfdHijo = cfdsDAO.getTipoCfd(idCfdRelacionado);
                    hijo.setTipoCfdHijo(tipoCfdHijo);
                    hijo.setEstadoRelacion("VALIDO");
                    logger.debug("CfdiRelacionado uuid=" + uuidRelacionado
                            + " -> idCFD=" + idCfdRelacionado + " (VALIDO)");
                } else {
                    hijo.setEstadoRelacion("ERROR");
                    hijo.setDescripcionError("UUID " + uuidRelacionado + " no encontrado en BD");
                    logger.error("El UUID " + uuidRelacionado + " no está en BD"
                            + " (CFDI de egreso idCFD=" + idCfdEgreso + ")");
                }

                cfdisRelDAO.guardarHijo(hijo);
            }
        } catch (Exception e) {
            logger.error("Error en buscarCfdisRelacionados idCFD=" + idCfdEgreso
                    + ": " + e.getMessage());
        }
    }
    
    public void procesarCfdisDeIngreso() {
        logger.debug("\n");
        logger.debug("=== INICIO Sprint 7: procesarCfdisDeIngreso ===");
        List<Integer> lote;

        Integer ultimoIdCfd = null;
        do {
            lote = cfdsDAO.getIdCfdsPendientesPorTipoAndFechaPedimento("I", BATCH_SIZE, ultimoIdCfd);
            if (lote == null || lote.isEmpty()) {
                break;
            }
            logger.debug("Lote de " + lote.size() + " CFDIs de egreso por procesar.");
            logger.debug("");
            for (Integer idCfdEgreso : lote) {
                validarEstadoPago(idCfdEgreso);
            }
            ultimoIdCfd = lote.get(lote.size() - 1);
        } while (lote.size() == BATCH_SIZE);

        logger.debug("=== FIN Sprint 7: procesarCfdisDeIngreso ===");
    }
    
    public void validarEstadoPago(Integer idCfdIngreso) {
    cfdsDAO.actualizarFechaPedimento(idCfdIngreso, "REVISANDO");

    String xmlSat = xmlsDAO.getXmlSat(idCfdIngreso);
    if (xmlSat == null || xmlSat.isEmpty()) {
        logger.error("No se encontró XML_SAT para idCFD=" + idCfdIngreso);
        logger.debug("\n");
        return;
    }

    String nsCfdi    = detectarNamespaceCfdi(xmlSat);
    Document doc     = parsearXml(xmlSat);
    String version    = leerAtributo(doc, "/cfdi:Comprobante/@Version",           nsCfdi);
    String tipoCfdi   = leerAtributo(doc, "/cfdi:Comprobante/@TipoDeComprobante", nsCfdi);
    String metodoPago = leerAtributo(doc, "/cfdi:Comprobante/@MetodoPago",        nsCfdi);

    if (version == null || Double.parseDouble(version) < 4.0
            || tipoCfdi == null || !tipoCfdi.equals("I")) {
        logger.debug("idCFD=" + idCfdIngreso + " version=" + version
                + " tipo=" + tipoCfdi + " -> NO_APLICA");
        cfdsDAO.actualizarFechaPedimento(idCfdIngreso, "NO_APLICA");
        logger.debug("\n");
        return;
    }

    // Obtener documentos relacionados filtrando solo CFDIs de pago VIGENTES
    List<DocumentosRelacionadosP> docs = 
            docRelPDAO.getPagosDeIngreso(idCfdIngreso); // ya filtra ESTADO_RELACION='VALIDO'

    // Filtrar adicionalmente que el CFDI de pago padre esté VIGENTE en CFDS
    List<DocumentosRelacionadosP> docsFiltrados = new ArrayList<>();
    for (DocumentosRelacionadosP drp : docs) {
        String estadoFiscal = obtenerEstatusGeneral(drp.getPagos().getCfdisPagos().getCfds().getEstadoFiscal());
        if ("VIGENTE".equals(estadoFiscal)) {
            docsFiltrados.add(drp);
        } else {
            logger.debug("Pago idPago=" + drp.getPagos().getIdPago()
                    + " ignorado, estadoFiscal=" + estadoFiscal);
        }
    }

    boolean tienePagosPadre = !docsFiltrados.isEmpty();

    if (tienePagosPadre) {
        for (DocumentosRelacionadosP drp : docsFiltrados) {
            Cfds cfdsPago = drp.getPagos().getCfdisPagos().getCfds();
            logger.debug("Parcialidad #"  + drp.getNumParcialidad()
                    + " | CFDI Pago: "  + cfdsPago.getSerie() + "-" + cfdsPago.getFolio()
                    + " | Fecha: "      + drp.getPagos().getFechaPago()
                    + " | FormaPago: "  + drp.getPagos().getFormaPago()
                    + " | Saldo ant: "  + drp.getImporteSaldoAnterior()
                    + " | Pagado: "     + drp.getImportePagado()
                    + " | Saldo ins: "  + drp.getImporteSaldoInsoluto());
        }
    }

    // Ordenar por IMPORTE_SALDO_INSOLUTO de mayor a menor y tomar el más bajo
    BigDecimal saldoMasBajo = null;
    if (tienePagosPadre) {
        saldoMasBajo = docsFiltrados.stream()
                .map(DocumentosRelacionadosP::getImporteSaldoInsoluto)
                .filter(s -> s != null)
                .min(BigDecimal::compareTo)  // el más bajo
                .orElse(null);
        logger.debug("Saldo insoluto más bajo: " + saldoMasBajo);
    }

    // Aplicar los 4 casos
    String nuevaFechaPedimento;

    if (tienePagosPadre && "PPD".equals(metodoPago)) {
        if (saldoMasBajo != null && saldoMasBajo.compareTo(BigDecimal.ZERO) == 0) {
            nuevaFechaPedimento = "PAGADO_PPD";
        } else {
            nuevaFechaPedimento = "PAGO_PARCIAL_PPD";
        }
    } else if (!tienePagosPadre && "PPD".equals(metodoPago)) {
        nuevaFechaPedimento = "NO_PAGADO";
    } else if (tienePagosPadre && "PUE".equals(metodoPago)) {
        nuevaFechaPedimento = "ERROR_PUE";
    } else if (!tienePagosPadre && "PUE".equals(metodoPago)) {
        nuevaFechaPedimento = "PAGADO_PUE";
    } else {
        // MetodoPago desconocido
        nuevaFechaPedimento = "NO_APLICA";
        logger.warn("idCFD=" + idCfdIngreso + " MetodoPago desconocido: " + metodoPago);
    }

    logger.debug("idCFD=" + idCfdIngreso + " -> " + nuevaFechaPedimento);
    cfdsDAO.actualizarFechaPedimento(idCfdIngreso, nuevaFechaPedimento);
    logger.debug("\n");
}
    
    public static String obtenerEstatusGeneral(String estadoFiscal) {
        switch (estadoFiscal) {
            case "VIGENTE":
            case "NO_CANCELABLE":
            case "CANCELABLE_SIN_ACEPTACION":
            case "CANCELABLE_CON_ACEPTACION":
            case "SOLICITUD_RECHAZADA":
                return "VIGENTE";

            case "EN_PROCESO":
            case "CANCELADO":
            case "CANCELADO_SIN_ACEPTACION":
            case "CANCELADO_CON_ACEPTACION":
            case "CANCELADO_PLAZO_VENCIDO":
                return "CANCELADO";

            default:
                return "CANCELADO";
        }
    }
    
    private Document parsearXml(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            logger.error("Error al parsear XML: " + e.getMessage());
            return null;
        }
    }

    private String detectarNamespaceCfdi(String xmlSat) {
        if (xmlSat.contains("http://www.sat.gob.mx/cfd/4")) {
            return "http://www.sat.gob.mx/cfd/4";
        } else {
            return "http://www.sat.gob.mx/cfd/3";
        }
    }

    private XPath construirXPath(String nsCfdi) {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        final String nsCfdiFinal = nsCfdi;
        xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("cfdi".equals(prefix)) {
                    return nsCfdiFinal;
                }
                if ("pago20".equals(prefix)) {
                    return NS_PAGO20;
                }
                if ("pago10".equals(prefix)) {
                    return NS_PAGO10;
                }
                return javax.xml.XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String ns) {
                return null;
            }

            @Override
            public java.util.Iterator<String> getPrefixes(String ns) {
                return null;
            }
        });
        return xp;
    }

    private String leerAtributo(Document doc, String xpathExpr, String nsCfdi) {
        try {
            XPath xp = construirXPath(nsCfdi);
            String val = xp.evaluate(xpathExpr, doc);
            return (val != null && !val.isEmpty()) ? val : null;
        } catch (Exception e) {
            logger.warn("No se pudo leer XPath '" + xpathExpr + "': " + e.getMessage());
            return null;
        }
    }

    private String attrDe(Node nodo, String nombreAtributo) {
        if (nodo == null || nodo.getAttributes() == null) {
            return null;
        }
        Node attr = nodo.getAttributes().getNamedItem(nombreAtributo);
        return attr != null ? attr.getNodeValue() : null;
    }

    private BigDecimal toBigDecimal(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            logger.warn("No se pudo convertir '" + valor + "' a BigDecimal");
            return null;
        }
    }

    private Integer toInt(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            logger.warn("No se pudo convertir '" + valor + "' a Integer");
            return null;
        }
    }

    private java.util.Date toDate(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(valor);
        } catch (ParseException e) {
            logger.warn("No se pudo convertir fecha '" + valor + "'");
            return null;
        }
    }
    
}
