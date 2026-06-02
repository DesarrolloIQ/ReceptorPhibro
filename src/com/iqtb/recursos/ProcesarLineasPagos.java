/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iqtb.recursos;

import com.iqtb.DAOs.CfdsDAO;
import com.iqtb.DAOs.ClientesDAO;
import com.iqtb.DAOs.DocumentosRelacionadosPDAO;
import com.iqtb.DAOs.XmlsDAO;
import com.iqtb.POJOs.CfdisRelacionadosPadre;
import com.iqtb.POJOs.Cfds;
import com.iqtb.POJOs.DocumentosRelacionadosP;
import com.iqtb.POJOs.UsuariosRecepcion;
import com.iqtb.POJOs.Xmls;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author macminizuri
 */
public class ProcesarLineasPagos {
    
    org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("Procesar Lineas Pagos");
    
    boolean isError=false;

    public ProcesarLineasPagos(){
        //Constructor vacio
    }
    
    public ArrayList<String> procesarLinea20(String lineaTxt){

        ArrayList<String> listaLinea20 = new ArrayList<>();
        
        boolean isUSD=false;

        String[] linea20 = lineaTxt.split("\\|");

        logger.info("Valor de atributo12: " + linea20[11]);

        logger.info("Linea20: longitud del arreglo: " + linea20.length);

        //Se inicia a identificar la cuenta bancaria
        if(linea20[14].length()>0){
            
            if(linea20[14].equals("03087869")){
                
                logger.info("Se trata de un pago hecho con la cuenta en dolares, no se mueve nada");
                isUSD=true;
                
            }else if(linea20[14].equals("01103212")){
                
                logger.info("Se trata de un pago hecho con la cuenta en pesos mexicanos, cambiando cosas");
                
                //cambiando moneda
                linea20[5]="MXN";
                
                logger.info("Obteniendo tipo de cambio");
                
                BigDecimal tipoC = new BigDecimal(linea20[6]);
                
                BigDecimal montoPagoUsd = new BigDecimal(linea20[7]);
                
                BigDecimal montoPagadoMxn = montoPagoUsd.multiply(tipoC).setScale(2, RoundingMode.HALF_UP);
                logger.info("El nuevo monto de pago es: " + montoPagadoMxn);
                
                linea20[7]=montoPagadoMxn.toString();
                
            }

        }else{
            
            logger.error("No se encontro campo para cuenta bancaria");
            isError=true;
            
        }


        if(isError){

            //Armando lista en error
            listaLinea20.add(0, lineaTxt);
            listaLinea20.add(1, linea20[5]);//moNEDA
            listaLinea20.add(2, linea20[6]);//TIPO CAMBIO
            listaLinea20.add(3, linea20[7]);//MONTO DE PAGO
            listaLinea20.add(4, linea20[1]);//IDENTIFICADOR DE PAGO

        }else{

            logger.info("Armando nueva linea 20");
            String nuevaLinea20 = "";
                    
            //Armando nueva linea 20
            if(isUSD){
                
                nuevaLinea20 = lineaTxt;
                
            }else{
                
                nuevaLinea20 = linea20[0]+"|"+linea20[1]+"|"+linea20[2]+"|"+linea20[3]+"|"+linea20[4]+"|"+linea20[5]+"|"+linea20[6]+"|"+linea20[7]+"|"+linea20[8]+"|"+linea20[9]+"|"+linea20[10]+"|"+linea20[11]+"|"+linea20[12]+"|"+linea20[13]+"|"+linea20[14]+"|"+linea20[15];
            
                
            }
            
            logger.info("Linea20: nueva linea 20: " + nuevaLinea20);
            logger.info("El monto total pagado es: " + linea20[7]);
            
            listaLinea20.add(0, nuevaLinea20);
            listaLinea20.add(1, linea20[5]);//moNEDA
            listaLinea20.add(2, linea20[6]);//TIPO CAMBIO
            listaLinea20.add(3, linea20[7]);//MONTO DE PAGO
            listaLinea20.add(4, linea20[1]);//IDENTIFICADOR DE PAGO

        }

        return listaLinea20;

    }//Fin tratando la linea 20

    public String procesarLinea01(String lineaTxt, Integer idUsuario){
        
        String[] linea01 = lineaTxt.split("\\|");
        
        String serie = linea01[2];
        String folio = linea01[3];
        
        Cfds cfd = null;
        String uuid="";
        
        CfdsDAO cfdsDAO = new CfdsDAO();
        
        cfd=cfdsDAO.obtenerUuid(serie, Long.parseLong(folio), idUsuario);
        
        if(cfd!=null){
            
            logger.info("Ya existe un archivo timbrado con estos datos, rechazando");
            
            return null;
            
        }else {
            
            return lineaTxt;
            
        }
        
    }

    public String procesarLinea03(String lineaTxt){

        String[] linea03 = lineaTxt.split("\\|");
        String usoCfdi = linea03[16];
        
        if(usoCfdi!=null){
            
            if(usoCfdi.equals("CP01")){
                
                logger.info("Uso CFDI correcto");
            }else{
                
                logger.info("Uso CFDI incorrecto, modificando");
                linea03[16]="CP01";
                
            }
            
        }//Fin uso CFDI
        
        String rfcReceptor = linea03[2];
        String identificadorReceptor= linea03[1];
        logger.info("Linea03: rfc receptor: " + rfcReceptor);
        
        String numTrib = linea03[15];
        if(numTrib.length()>0){
            
            logger.info("Verificando si cliente es extranjero");
            
            if(rfcReceptor.equals("XEXX010101000")){
                
                logger.info("El rfc receptor si es extranjero, dejando el numRegIdTrib");
                
            }else{
                
                logger.info("No es un rfc extranjero, se trata de un nacional");
                linea03[15]="";
                
            }
            
        }//Fin NumRegIdTrib
        //buscando regimen fiscal
        ClientesDAO clientesDAO = new ClientesDAO();
        
        String regimen = clientesDAO.getCliente(rfcReceptor,identificadorReceptor);
        
        logger.info("la clave del regimen es: " + regimen);
        
        String nuevaLinea = String.join("|", linea03);
        
        nuevaLinea = nuevaLinea + "|" + regimen;

        return nuevaLinea;
            
    }

    public ArrayList<String> procesarLinea21(String lineaTxt, Integer idUsuario, String monedaPago, String tipoCambio){

        ArrayList<String> listaLinea21 = new ArrayList<>();
        String mensajeError="";

        String[] linea21 = lineaTxt.split("\\|");

        String identificadorPago = linea21[1];
        
        boolean isMXN=false;

        //Obteniendo valores del array
        String serie = linea21[3];
        logger.info("Linea21: La serie es: " + serie);
        
        if(serie.equals("SA")){
            
            logger.info("La serie si es SO");
            
        }else{
            
            serie="SO";
            
        }

        String folio = linea21[4];
        logger.info("Linea21: El folio es: " + folio);
        

        logger.info("Linea21: Iniciando a buscar el cfd");
        Cfds cfd = null;
        String uuid="";
        
        CfdsDAO cfdsDAO = new CfdsDAO();
        
        cfd=cfdsDAO.obtenerUuid(serie, Long.parseLong(folio), idUsuario);
        
        if(cfd!=null){
            
            uuid=cfd.getUuid();
        
            logger.info("El uuid es: " + uuid);
        
            String nuevaLineaTxt="";
        
            String equivalenciaLinea="";
        
            if(monedaPago.equals("USD")){
            
                logger.info("Se trata de un pago en USD");
                equivalenciaLinea="1";
           
            }//FIN SE PAGO CON USD EN LINEA 20
            else if(monedaPago.equals("MXN")){
            
                logger.info("Se trata de un pago en MXN, calculando equivalenciaDR");
                BigDecimal tipoC = new BigDecimal(tipoCambio);
            
                BigDecimal equivalenciaDr =new BigDecimal(1).divide(tipoC, 10, RoundingMode.HALF_UP).setScale(10, RoundingMode.HALF_UP);
                logger.info("La equivalenciaDR es: " + equivalenciaDr);
                equivalenciaLinea = equivalenciaDr.toString();

            }//FIN SE PAGO CON MXN EN LINEA 20
        
            XmlsDAO xmlsDAO = new XmlsDAO();
            Xmls xmlBuscar = xmlsDAO.verificarXmls(cfd.getIdCfd());
        
            //sacar los datos del xml si es factoraje
            ProcesarXml procesarXml = new ProcesarXml();
        
            BigDecimal totalFactura=cfd.getTotal();
        
            logger.info("El total de la factura relacionada es: " + totalFactura.toString());

            String impSaldoAnt = linea21[6];
            
            if(impSaldoAnt.startsWith(".")){
                
                impSaldoAnt = "0" + impSaldoAnt;
                
            }

            String impPagado = linea21[7];
            if(impPagado.startsWith(".")){
                
                impPagado = "0" + impPagado;
                
            }
            
            
            logger.info("El importe pagado es: " + impPagado);

            String parcialidad="";
            
            String saldoFinal="";
            
            String montoCalcular=impPagado;
        
            List<DocumentosRelacionadosP> documentosRelacionadosPs = new DocumentosRelacionadosPDAO().getPagosDeIngreso(cfd.getIdCfd());
            if (documentosRelacionadosPs != null && !documentosRelacionadosPs.isEmpty()) {
                //Obteniendo la parcialidad maxima
                Integer maxParcialidad = documentosRelacionadosPs.stream()
                        .map(DocumentosRelacionadosP::getNumParcialidad)
                        .filter(p -> p != null)
                        .max(Integer::compareTo)
                        .orElse(0);
                
                maxParcialidad=maxParcialidad+1;
                parcialidad = maxParcialidad.toString();
                
                //Obteniendo la parcialidad minima
                String menorSaldoInsoluto = documentosRelacionadosPs.stream()
                    .map(DocumentosRelacionadosP::getImporteSaldoInsoluto)
                    .filter(s -> s != null)
                    .min(BigDecimal::compareTo)
                    .map(BigDecimal::toPlainString)
                    .orElse("mal");
                
                saldoFinal = menorSaldoInsoluto;
                
            }  else {
                parcialidad  = "1";
                
                saldoFinal = "mal";
            }
            
            //SPRINT 1: NOTAS DE CREDITO
            BigDecimal montoRestar = BigDecimal.ZERO;
            
            List<CfdisRelacionadosPadre> listaCfdsPadre = new DocumentosRelacionadosPDAO().getCfdisEPadreRelacionados(cfd.getIdCfd());
            
            if(listaCfdsPadre.size()>0 && !listaCfdsPadre.isEmpty()){
                
                logger.info("Se encontraron al menos 1 documento E");
                
                for (CfdisRelacionadosPadre padre : listaCfdsPadre) {
                    
                    //Contar el numero de hijos
                    Long numeroHijos = new DocumentosRelacionadosPDAO().contarHijosTipoI(padre.getCfds().getIdCfd());
                    
                    if (numeroHijos == 1) {
                        
                        BigDecimal totalCfdiE = cfdsDAO.obtenerTotalCfdiE(padre.getCfds().getIdCfd());
                    
                        montoRestar.add(totalCfdiE);
                        
                    }else{
                        
                        logger.error("No se puede calcular correctamente ");
                        
                        mensajeError="ERROR 21|Se encontraron mas de 1 documento de Ingreso ligado al documento de Egreso " + padre.getCfds().getUuid() + "para el documento con folio: " +folio + ", serie: " + serie;
                        
                        listaLinea21.add(0, mensajeError);
                        return listaLinea21;
                        
                    }
                    
                }
                
            }else{
                
                logger.info("No se encontraron documentos E o notas de credito");
                
            }
            
            String impSaldoInsoluto= linea21[8];
            logger.info("El importe saldo impoluto es: " + impSaldoInsoluto);
            
            if(impSaldoInsoluto.equals("0.00")){
                
                logger.info("Se va a terminar de pagar la factura, entonces no se calcula importe anterior");
                
                if(impSaldoAnt.equals(impPagado)){
                    
                    logger.info("Coincide el saldo anterior con el importe pagado, VERIFICANDO SALDO ANTERIOR con db");
                    
                    if(parcialidad.equals("1")){
                        
                        logger.info("Esta es la primer parcialidad, todo correcto");
                        
                    }//VERIFICANDO QUE SEA PRIMER PACIALIDAD
                    else{
                        
                        if(impSaldoAnt.equals(saldoFinal)){
                            
                            logger.info("Si es igual al importe anterior, entonces no tiene NC o cosas raras de JDE");
                            
                        }else if (montoRestar.compareTo(BigDecimal.ZERO) > 0) {
           
                            logger.info("Tiene al menos una nota de credito, calculando el nuevo monto anterior");
                            
                            BigDecimal saldoFinale = new BigDecimal(saldoFinal).setScale(2, RoundingMode.HALF_UP);
                            
                            saldoFinale = saldoFinale.subtract(montoRestar).setScale(2, RoundingMode.HALF_UP);
                            
                            if(saldoFinale.toString().equals(impPagado)){
                                
                                logger.info("Se encontro que el saldo anterior era debido a una NC");
                                
                            }else{
                                
                                logger.error("No se sabe de donde sacaron ese monto, mandando a error");
                                
                                mensajeError = "ERROR 21|El saldo anterior: " + saldoFinal + " es diferente del importe pagado: " + impPagado + " porque se encontraron Notas de Credito para el documento con folio: " +folio + ", serie: " + serie;
                                
                                listaLinea21.add(0, mensajeError);
                                return listaLinea21;
                                
                            }
                            
                        }else{
                            
                            logger.error("No es la primer parcialidad y no coincide con el saldo anterior registrado: " + saldoFinal + " para el documento con folio: " +folio + ", serie: " + serie);
                            
                            mensajeError="ERROR 21|No es la primer parcialidad y no coincide con el saldo anterior registrado: " + saldoFinal + " para el documento con folio: " +folio + ", serie: " + serie;
                            
                            listaLinea21.add(0, mensajeError);
                            return listaLinea21;
                            
                        }
                        
                    }//ES SEGUNDA O N PARCIALIDAD
                    
                }else{
                    
                    logger.error("Para un pago que se va a liquidar el saldo anterior: " + impSaldoAnt + " e importe pagado: " + impPagado + " para el documento con folio: " +folio + ", serie: " + serie);
                    
                    mensajeError = "ERROR 21|Para un pago que se va a liquidar el saldo anterior: " + impSaldoAnt + " e importe pagado: " + impPagado + " para el documento con folio: " +folio + ", serie: " + serie;
                    
                    listaLinea21.add(0, mensajeError);
                    return listaLinea21;
                    
                }
                
            }//Fin el saldo insoluto es 0
            else{
                
                logger.info("Verificando que el pago sea correcto");
                
                if(parcialidad.equals("1")){
                    
                    logger.info("Es la primer parcialidad, verificando que el saldo anterior sea el total de la factura");
                    
                    if(impSaldoAnt.equals(totalFactura.toString())){
                        
                        logger.info("El saldo anmterior coincide cone l total de la factura, verificando NC");
                        if (montoRestar.compareTo(BigDecimal.ZERO) > 0){
                            
                            logger.error("El saldo anterior:" + impSaldoAnt + " es igual al total de la factura" + totalFactura.toString() + " pero no puede ser porque tiene nota de credito, saldo anterior a restar: " + montoRestar.toString() + " para el documento con folio: " +folio + ", serie: " + serie);
                            
                            mensajeError = "ERROR 21|El saldo anterior:" + impSaldoAnt + " es igual al total de la factura" + totalFactura.toString() + " pero no puede ser porque tiene nota de credito, saldo anterior a restar: " + montoRestar.toString() + " para el documento con folio: " +folio + ", serie: " + serie;
                            
                            listaLinea21.add(0, mensajeError);
                            return listaLinea21;
                            
                        }
                        logger.info("No se encontraron NC");
                        
                    }else{
                        
                        logger.info("El saldo anterior no coincide, verificando NC");
                        
                        if (montoRestar.compareTo(BigDecimal.ZERO) > 0){
                            
                            logger.info("Restando el monto al monto anterior");
                            
                            String totalParcial = totalFactura.toString();
                            
                            totalFactura.subtract(montoRestar).setScale(2, RoundingMode.HALF_UP);
                            logger.info("Nuevo total de Factura y saldo anterior: " + totalFactura);
                            
                            BigDecimal comprobarPago = totalFactura.subtract(new BigDecimal(impPagado).setScale(2, RoundingMode.HALF_UP));
                            
                            logger.info("El saldo anterior nuevo menos el importe pagado es: " + comprobarPago.toString());
                            if(comprobarPago.compareTo(new BigDecimal(impSaldoInsoluto).setScale(2, RoundingMode.HALF_UP)) == 0){
                                
                                logger.info("El pago tiene coherencia con sus notas de credito");
                                
                            }else{
                                
                                logger.error("No tiene sentido el pago para primer parcialidad y NC, total factura : " + totalParcial + " nota de credito: " + montoRestar.toString() + " dando como resultado: " + totalFactura.toString() + " pero al restarlo al importe pagado no coinmcide con el saldo insoluto" + " para el documento con folio: " +folio + ", serie: " + serie);
                                
                                mensajeError = "ERROR 21|No tiene sentido el pago para primer parcialidad y NC, total factura : " + totalParcial + " nota de credito: " + montoRestar.toString() + " dando como resultado: " + totalFactura.toString() + " pero al restarlo al importe pagado no coinmcide con el saldo insoluto" + " para el documento con folio: " +folio + ", serie: " + serie;
                                
                                listaLinea21.add(0, mensajeError);
                                return listaLinea21;
                                
                            }
                            
                        }else{
                            
                            totalFactura = totalFactura.setScale(2, RoundingMode.HALF_UP);
                            impSaldoAnt = totalFactura.toString();
                            
                        }
                        
                    }
                    
                }else{
                    
                    logger.info("Parcialidad diferente de 1, verificando saldo anterior");
                    
                    BigDecimal saldoAnterior = new BigDecimal(impSaldoAnt).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal saldoInsoluto = new BigDecimal(impSaldoInsoluto).setScale(2, RoundingMode.HALF_UP);
                    
                    if (saldoInsoluto.compareTo(saldoAnterior) > 0) {

                        logger.error("El saldo insoluto: " + saldoInsoluto.toString() + " es mayor que el saldo anterior: " + saldoAnterior.toString() + ", no tiene mucho sentido" + " pero al restarlo al importe pagado no coinmcide con el saldo insoluto" + " para el documento con folio: " +folio + ", serie: " + serie);
                        
                        mensajeError = "ERROR 21|El saldo insoluto: " + saldoInsoluto.toString() + " es mayor que el saldo anterior: " + saldoAnterior.toString() + ", no tiene mucho sentido" + " pero al restarlo al importe pagado no coinmcide con el saldo insoluto" + " para el documento con folio: " + folio + ", serie: " + serie;
                        listaLinea21.add(0, mensajeError);
                        return listaLinea21;
                        
                    }else{
                        
                        logger.info("Es congruente con lo mostrado, viendo ya luego si estan bien los calculos o si se obtiene del ultimo registro");
                        
                        //JALARLO DE DOCUMENTOS_RELACIONADOS_P
                        if(saldoFinal.equals("mal")){
                            
                            logger.info("No se encontro el ultimo saldo, asignando el del documento");
                            
                        }else{
                            
                            logger.info("Se encontro el ultimo saldo anterior");
                            
                            if (montoRestar.compareTo(BigDecimal.ZERO) > 0){
                                
                                logger.info("Se encontro notas de credito, modificando saldo anterior");
                                
                                BigDecimal saldoFinale = new BigDecimal(saldoFinal);
                                
                                saldoFinale = saldoFinale.subtract(montoRestar);
                                
                                logger.info("El nuevo saldo final es: " + saldoFinale.toString());
                                
                                if(saldoFinale.subtract(saldoInsoluto).compareTo(new BigDecimal(impPagado)) == 0){
                                    
                                    logger.info("Los montos en la linea son correctos");
                                    impSaldoAnt= saldoFinale.toString();
                                    
                                }
                                else{
                                    
                                    logger.error("No coinciden los montos");
                                    
                                }
                                
                                
                            }//Fin si se encontro nota de credito
                            else{
                                
                                logger.info("Asignando nuevo monto anterior porque no tiene nc");
                                impSaldoAnt = saldoFinal;
                                
                            }//Fin no se encontraron NC
                            
                        }//Fin si se encontro un saldo anterior en DOCUMENTOS_RELACIONADOS_P
                        
                    }//Fin el saldo insoluto no es mayor que el saldo anterior
                    
                }//FRin la parcialidad no es 1
                
            }//Fin el saldo insoluto no es 0
            
            logger.info("El importe saldo anterior: " + impSaldoAnt);

            ArrayList<String> resultadoTasaXml = procesarXml.buscarTasaoCuota(xmlBuscar.getXmlSat());
        
            //Guardarlo en una lista
            String baseImpuesto = resultadoTasaXml.get(0);
            String impuesto = resultadoTasaXml.get(1);
            String tipoFactor = resultadoTasaXml.get(3);
            String tasaOCuota = resultadoTasaXml.get(2);
            
            String metodoPago = resultadoTasaXml.get(4);
            
            logger.info("El metodo de pago es: " + metodoPago);
            if(metodoPago!=null){
                
                logger.info("El metodo de pago es: " + metodoPago);
                
                if(metodoPago.equals("PUE")){
                    
                    logger.error("ERROR 21| NO SE PUEDE HACER UN PAGO DE UN DOCUMENTO RELACIONADO CON PAGO UNICA EXHIBICION, METODO DE PAGO PUE" + " para el documento con folio: " + folio + ", serie: " + serie);
                    
                    mensajeError = "NO SE PUEDE HACER UN PAGO DE UN DOCUMENTO RELACIONADO CON PAGO UNICA EXHIBICION, METODO DE PAGO PUE" + " para el documento con folio: " + folio + ", serie: " + serie;
                    
                    listaLinea21.add(0, mensajeError);
                    return listaLinea21;
                    
                }
                
            }
        
            String objetoImpuesto="";
        
            if(baseImpuesto !=null && impuesto!=null && tipoFactor !=null){
        
                logger.info("Se encontraron los atributos, objeto impuesto 02");
                objetoImpuesto="02";
        
            }else{
            
                logger.info("No se encontro uno o mas atributos, objeto impuesto 02");
                objetoImpuesto="02";
            
            }
        
            String identificadorPagoEnd = uuid + "_" + linea21[1];
            logger.info("El identificador es: " + identificadorPagoEnd);
            logger.info("El monto para calcular lineas 21B es: " + montoCalcular);
        
            nuevaLineaTxt= linea21[0] + "|" + linea21[1] + "|" + uuid + "|" + serie + "|" + folio +  "|" + linea21[5] + "|" + equivalenciaLinea + "|" + parcialidad +"|" + impSaldoAnt + "|" + impPagado + "|" + linea21[8] + "|" + objetoImpuesto + "|" + identificadorPagoEnd +"|";



            listaLinea21.add(0, nuevaLineaTxt);
            listaLinea21.add(1, identificadorPagoEnd);
            listaLinea21.add(2, baseImpuesto);
            listaLinea21.add(3, impuesto);
            listaLinea21.add(4, tipoFactor);
            listaLinea21.add(5, objetoImpuesto);
            listaLinea21.add(6, montoCalcular);
            listaLinea21.add(7, linea21[1]);
            listaLinea21.add(8,tasaOCuota);
            listaLinea21.add(9,linea21[5]);
            listaLinea21.add(10,equivalenciaLinea);

            return listaLinea21;
            
        }else{
            
            logger.error("No se encontro en DB el CFD, retornando null");
            
            return null;
            
        }
        
        

    }//Fin acomodando linea 21
    
    
    public String procesarArchivoPagos(String contenidoTxt, Integer idSucursal){
        
        Pattern linea00 = Pattern.compile("^00\\|");

        Pattern linea01 = Pattern.compile("^01\\|");

        Pattern linea03 = Pattern.compile("^03\\|");
        
        Pattern linea05 = Pattern.compile("^05\\|");
        
        Pattern linea09 = Pattern.compile("^09\\|");
        
        Pattern linea20 = Pattern.compile("^20\\|");

        Pattern linea21 = Pattern.compile("^21\\|");

        Matcher matcher;
        
        BigDecimal tipoCambio = null;
        BigDecimal montoPago = new BigDecimal("0.0");
        BigDecimal montoPagoAcumulado = new BigDecimal("0.0");
        
        String monedaPago="";
        String identificador20="";
        List<String> listaIdentificadores = new ArrayList<>();
        ArrayList<ArrayList<String>> listaDeLineas21 = new ArrayList<>();
        Map<String, String> mapaTc = new HashMap<>();
        String tipoCambioString="";
        
        boolean isUSD20=false;
        boolean isUSD21=false;
        logger.info("Leyendo cada linea del archivo");
        String[] lineasContenidoArchivo = contenidoTxt.split("\n");

        String nuevaLineaTxt="";
        String contenidoFactura="";
        
        for (String lineaTxt : lineasContenidoArchivo) {

            boolean seAgregaLinea= false;
            
            logger.info("Linea txt: " + lineaTxt);
            
            //Buscando linea 20
            matcher = linea00.matcher(lineaTxt);
            Boolean isLinea00 = matcher.find();
            
            if(isLinea00){
                
                nuevaLineaTxt=lineaTxt;
                seAgregaLinea=true;
                
            }
            
            matcher = linea01.matcher(lineaTxt);
            Boolean isLinea01 = matcher.find();
            
            if(isLinea01){
                
                String buscarGenerado = procesarLinea01(lineaTxt, idSucursal);
                
                if(buscarGenerado!=null){
                    
                    nuevaLineaTxt=lineaTxt;
                    seAgregaLinea=true;
                    
                }else{
                    
                    logger.error("Ya existe un documento timbrado con estos datos");
                    return contenidoTxt + "\n" + "ERROR|Este documento ya ha sido timbrado";
                    
                }
                
                
                
            }
            
            matcher = linea03.matcher(lineaTxt);
            Boolean isLinea03 = matcher.find();
            
            if(isLinea03){
                
                nuevaLineaTxt = procesarLinea03(lineaTxt);
                
                seAgregaLinea=true;
                
            }
            
            matcher = linea05.matcher(lineaTxt);
            Boolean isLinea05 = matcher.find();
            
            if(isLinea05){
                
                nuevaLineaTxt=lineaTxt;
                seAgregaLinea=true;
                
            }
            
            matcher = linea09.matcher(lineaTxt);
            Boolean isLinea09 = matcher.find();
            
            if(isLinea09){
                
                nuevaLineaTxt=lineaTxt;
                seAgregaLinea=true;
                
            }
            
            matcher = linea20.matcher(lineaTxt);
            Boolean isLinea20 = matcher.find();
            
            if (isLinea20) {

                logger.info("Se encontro linea 20");
                //Llamando a metodo para procesar linea 20
                ArrayList<String> resultadoLinea20 = procesarLinea20(lineaTxt);

                //Asignando valores a variables necesarias
                nuevaLineaTxt = resultadoLinea20.get(0);
                seAgregaLinea=true;
                tipoCambioString = resultadoLinea20.get(2).trim();
                tipoCambio = new BigDecimal(tipoCambioString).setScale(6, RoundingMode.HALF_UP);
                logger.info("El tipo de cambio obtenido es: " + tipoCambio);
                
                logger.info("Monto totasl pagado: " + resultadoLinea20.get(3));
                
                monedaPago = resultadoLinea20.get(1);
                
                montoPago = new BigDecimal(resultadoLinea20.get(3).trim()).setScale(2, RoundingMode.HALF_UP);
                
                BigDecimal montoPagoActual=null;
                
                if(monedaPago.equals("USD")){
                    logger.info("El pago es en USD, calculando equivalente en MXN");
                    
                    isUSD20=true;
                    
                    montoPagoActual = montoPago.multiply(tipoCambio).setScale(2, RoundingMode.HALF_UP);
                    
                }else{
                    
                    montoPagoActual = montoPago;
                    
                }


                montoPagoAcumulado = montoPagoAcumulado.add(montoPagoActual);

                
                identificador20 = resultadoLinea20.get(4);
                listaIdentificadores.add(identificador20);

                mapaTc.put(identificador20,resultadoLinea20.get(2));
                logger.info("Colocando el identificador en el hashmap: " + identificador20 + "tipo de cambio: " + resultadoLinea20.get(1));

                logger.info("Se termino de procesar la linea 20");

            }//Fin se procesa linea20

            matcher = linea21.matcher(lineaTxt);
            Boolean isLinea21 = matcher.find();

            if (isLinea21) {

                ArrayList<String> resultadoLinea21 = procesarLinea21(lineaTxt, idSucursal, monedaPago, tipoCambioString);

                if(resultadoLinea21!=null && !resultadoLinea21.get(0).startsWith("ERROR")){

                    listaDeLineas21.add(resultadoLinea21);

                }else{

                    //logger.error(resultadoLinea21.get(0));

                    return contenidoTxt + "\n" + resultadoLinea21.get(0);
                    
                }


            } else {

                if(seAgregaLinea){
                    logger.info("Se esta agregando la linea: ");
                    contenidoFactura += nuevaLineaTxt + "\n";
                }

            }

            nuevaLineaTxt=null;
           
        }//Fin del while que recorrio el contenido del txt


        if (!listaDeLineas21.isEmpty()) {
            
            //Verificando que no existan lineas con mismo uuid, si es el caso, combinarlas
            listaDeLineas21=verificarDobles21(listaDeLineas21);

            logger.info("Iniciando a ver el numero de identificadores: " + listaIdentificadores.size());

            Boolean coincideIdentificador = false;

            BigDecimal totalBase16A = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalBase8A = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalBase0A = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalBaseExentoA = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalTraslado16A = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalTraslado8A = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);

            BigDecimal zeroBig = new BigDecimal("0.0");

            for (int j = 0; j < listaIdentificadores.size(); j++) {

                logger.info("Identificando identificadores");

                String identiFor = listaIdentificadores.get(j);

                logger.info("Identificador actual: " + identiFor);


                //Iniciando a procesar todas las lineas 21

                if (listaDeLineas21.size() > 0 && !listaDeLineas21.isEmpty()) {

                    MathContext contexto = new MathContext(6, RoundingMode.HALF_UP);

                    //Declarando variables para linea 23
                    BigDecimal totalTraslados16 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalTraslados8 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalTraslados0 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);

                    BigDecimal totalBase16 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalBase8 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalBase0 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);

                    BigDecimal totalPagado16 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalPagado8 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalPagado0 = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalPagadoExento = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);

                    Boolean isTraslado16 = false;
                    Boolean isTraslado8 = false;
                    Boolean isTraslado0 = false;
                    
                    String equivalenciaDR=null;

                    logger.info("Iniciando a procesar las lineas 21");
                    ArrayList<String> montosPagadosDR = new ArrayList<String>();
                    ArrayList<String> montoPagoNormie = new ArrayList<String>();

                    Integer iterador = 0;

                    for (ArrayList<String> linea21Res : listaDeLineas21) {

                        if (linea21Res.get(7).equals(identiFor)) {

                            logger.info("El identificador coincide");
                            coincideIdentificador = true;

                            montoPagoNormie.add(linea21Res.get(6));

                            contenidoFactura += linea21Res.get(0) + "\n";
                            
                            equivalenciaDR = linea21Res.get(10);
                            logger.info("La equivalenciaDR para calcular linea 23 es: " + equivalenciaDR);

                            //Creanddo Linea 21B
                            String linea21B = "";

                            String nom21 = "21B";
                                
                            String tasaStrDR = null;
                            
                            //Verificando que tasa o cuota no es null
                            if(linea21Res.get(8) != null){
                                    
                                logger.info("No se trata de un documento EXENTO, continuando flujo normal");
                                   
                                tasaStrDR = linea21Res.get(8);
                                BigDecimal montoPagadoDR = new BigDecimal(linea21Res.get(6));
                                
                                String monedaPago21 = linea21Res.get(9);
                                logger.info("La moneda del documento relacionado es: " + monedaPago21);
                                
                                if(monedaPago21.equals("USD")){
                                    
                                    logger.info("La moneda de pago es en usd");
                                    isUSD21=true;
                                    
                                }
                                
                                if(isUSD20 && !isUSD21){
                                    
                                    logger.info("Convirtiendo el pago a pesos mexicanos");
                                    
                                    montoPagadoDR = montoPagadoDR.multiply(tipoCambio).setScale(6, RoundingMode.HALF_UP);
                                    
                                }
                                
                                BigDecimal tasaDR = new BigDecimal(tasaStrDR).setScale(6, RoundingMode.HALF_UP);
                                tasaDR = tasaDR.add(new BigDecimal("1.0").setScale(6, RoundingMode.HALF_UP));
                                logger.info("Linea21B: La tasaDR para dividir es: " + tasaDR);
                                
                                BigDecimal baseDR = montoPagadoDR.divide(tasaDR, 6, RoundingMode.HALF_UP);
                                logger.info("Linea21B: La baseDR es: " + baseDR);

                                //Buscando tasa
                                if (tasaStrDR.equals("0.160000")) {

                                    logger.info("Linea21B: Se encontro tasa 16%");
                                    totalBase16 = totalBase16.add(baseDR);

                                    totalPagado16 = totalPagado16.add(montoPagadoDR);

                                    isTraslado16 = true;

                                }

                                if (tasaStrDR.equals("0.080000")) {

                                    logger.info("Linea21B: Se encontro tasa 8%");
                                    totalBase8 = totalBase8.add(baseDR);

                                    totalPagado8 = totalPagado8.add(montoPagadoDR);

                                    isTraslado8 = true;

                                }

                                if (tasaStrDR.equals("0.000000")) {

                                    logger.info("Linea21B: Se encontro tasa 0%");
                                    totalBase0 = totalBase0.add(baseDR);

                                    totalPagado0 = totalPagado0.add(montoPagadoDR);

                                    isTraslado0 = true;

                                }

                                logger.info("La base DR es: " + baseDR);
                                BigDecimal totalImpuestoDR = baseDR.multiply(new BigDecimal(linea21Res.get(8))).setScale(6, RoundingMode.HALF_UP);


                                logger.info("Linea21B: El mtotal del impuesto es: " + totalImpuestoDR);

                                if (isTraslado16) {

                                    totalTraslados16 = totalTraslados16.add(totalImpuestoDR);

                                }

                                if (isTraslado8) {

                                    totalTraslados8 = totalTraslados8.add(totalImpuestoDR);

                                }

                                if (isTraslado0) {

                                    totalTraslados0 = totalTraslados0.add(totalImpuestoDR);

                                }
                                    
                                logger.info("Creando la linea 21B");
                                if(isUSD20 && !isUSD21){
                                    
                                    logger.info("Se realiza en pesos mexicanos");
                                    logger.info("identificador: " + linea21Res.get(1));
                                    logger.info("tipo de cambio: " + tipoCambio);
                                    logger.info("BaseDR: " + baseDR);
                                    logger.info("Linea 21B3 res: " + linea21Res.get(3));
                                    logger.info("Linea21b4 4: " + linea21Res.get(4));
                                    logger.info("tasaStrDR: " + tasaStrDR);
                                    logger.info("totalImpuestoDR: " + totalImpuestoDR);
                                    
                                    linea21B = nom21 + "|" + linea21Res.get(1) + "|" + baseDR.multiply(tipoCambio).setScale(6, RoundingMode.HALF_UP) + "|" + linea21Res.get(3) + "|" + linea21Res.get(4) + "|" + tasaStrDR + "|" + totalImpuestoDR.multiply(tipoCambio).setScale(6, RoundingMode.HALF_UP);
                                    logger.info("Linea 21B: " + linea21B);
                                    
                                    
                                }else{
                                    
                                    linea21B = nom21 + "|" + linea21Res.get(1) + "|" + baseDR + "|" + linea21Res.get(3) + "|" + linea21Res.get(4) + "|" + tasaStrDR + "|" + totalImpuestoDR;
                                    logger.info("Linea 21B: " + linea21B);
                                    
                                }
                                
                                    
                            }else{
                                    
                                logger.info("Se trata de una factura EXENTA");
                                    
                                BigDecimal montoPagadoDR = new BigDecimal(linea21Res.get(6));
                                    
                                //Armando la nueva linea 21B
                                linea21B = nom21 + "|" + linea21Res.get(1) + "|" + montoPagadoDR.multiply(tipoCambio) + "|" + linea21Res.get(4) + "|" + linea21Res.get(3) + "|||";
                                logger.info("Linea 21B: " + linea21B);
                                    
                                totalPagadoExento = totalPagadoExento.add(montoPagadoDR);
                                    
                            }

                            iterador++;

                            //Agregando linea 21B al contenido de la factura
                            if (listaDeLineas21.size() > iterador) {

                                contenidoFactura += linea21B + "\n";

                            } else {

                                contenidoFactura += linea21B;

                            }//Fin no es la ultima linea


                        }//Fin el identificador coincide
                        //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                        isUSD21=false;
                    }//fiN FOR POR CADA LINEA 21

                    logger.info("La equivalencia DR para calculos es: " + equivalenciaDR);
                    if (coincideIdentificador) {

                        BigDecimal totalTras16 = new BigDecimal("0.0");
                        BigDecimal totalTras8 = new BigDecimal("0.0");
                        
                        logger.info("Identifor: "+identiFor);

                        BigDecimal tcCorrespondiente = new BigDecimal(mapaTc.get(identiFor)).setScale(6, RoundingMode.HALF_UP);
                        BigDecimal equivalenciaDRCorrespondiente = new BigDecimal(equivalenciaDR).setScale(10,RoundingMode.HALF_UP);
                        
                        boolean todoUSD = false;
                        //Comparando que EquivalenciaDR no sea 1
                        if (equivalenciaDRCorrespondiente.compareTo(BigDecimal.ONE) != 0) {
                            // Es diferente de 1
                            logger.info("La equivalenciaDR es diferente de 1, no se cambia");
                        }else{
                            
                            logger.info("La equivalenciaDR es igual a 1, calculandola para obtener el valor correcto en linea 20A");
                            todoUSD = true;
                            
                        }
                        
                        //Iniciando lineas 23
                        logger.info("Linea23: Verificando si existieron traslados con las diferentes tasas de impuesto");

                        //Hacer una linea23 por cada traslado a tasa
                        if (totalBase16.compareTo(zeroBig) == 1) {

                            logger.info("Linea23: Se encontraron traslados con tasa a 16%");
                            BigDecimal base16linea23 = null;
                            BigDecimal traslados16Linea23 = null;
                            
                            if(!isUSD20){
                                
                                logger.info("Cambiando a mxn por ser la moneda de pago");
                                logger.info("La equivalenciaDR seria: " + equivalenciaDRCorrespondiente.toString());
                                base16linea23 = totalBase16.divide(equivalenciaDRCorrespondiente, 6, RoundingMode.HALF_UP);
                                
                                traslados16Linea23 = totalTraslados16.divide(equivalenciaDRCorrespondiente, 6, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                base16linea23=totalBase16;
                                
                                traslados16Linea23=totalTraslados16;
                                
                            }
                            
                            //Creando linea 23
                            logger.info("Linea23: El identificador es: " + identiFor);

                            String linea23 = "23|" + identiFor + "|" + base16linea23 + "|002|Tasa|0.160000|" + traslados16Linea23;
                            logger.info("Linea23: " + linea23);

                            logger.info("Agregando linea 23");
                            contenidoFactura = contenidoFactura + "\n" + linea23 + "\n";

                            //Preparando para linea20A
                            
                            logger.info("La equivalencia DR es: " + equivalenciaDRCorrespondiente.toString());
                            logger.info("La base total a 16 es: " + totalBase16.toString());
                            //BigDecimal base16Actual = totalBase16.multiply(tcCorrespondiente).setScale(2, RoundingMode.HALF_UP);
                            
                            BigDecimal base16Actual=null;
                            
                            if(todoUSD){
                                
                                base16Actual= totalBase16.multiply(tcCorrespondiente);
                                
                            }else{
                                
                                base16Actual = totalBase16.divide(equivalenciaDRCorrespondiente, 10, RoundingMode.HALF_UP);
                                
                            }

                            totalBase16A = totalBase16A.add(base16Actual);
                            logger.info("Linea23: Valor acumulado base tasa 16%:" + totalBase16A);

                            totalTras16 = totalBase16.multiply(new BigDecimal("0.160000").setScale(6, RoundingMode.HALF_UP));

                            BigDecimal traslado16Actual = null;
                            
                            if(todoUSD){
                                
                                traslado16Actual = totalTras16.multiply(tcCorrespondiente).setScale(2, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                traslado16Actual = totalTras16.divide(equivalenciaDRCorrespondiente, 10, RoundingMode.HALF_UP);
                                
                            }

                            totalTraslado16A = totalTraslado16A.add(traslado16Actual);

                            logger.info("Linea23: El total de traslado con tasa a 16% es: " + totalTras16);

                            logger.info("Linea23: El acumulado de traslados tasa 16% es: " + totalTraslado16A);

                        }

                        if (totalBase8.compareTo(zeroBig) == 1) {

                            logger.info("Linea23: Se encontraron traslados con tasa a 8%");
                            
                            //Creando linea 23
                            logger.info("Linea23: El identificador es: " + identiFor);
                            
                            BigDecimal base8linea23 = null;
                            BigDecimal traslados8Linea23 = null;
                            
                            if(!isUSD20){
                                
                                logger.info("Cambiando a mxn por ser la moneda de pago");
                                base8linea23= totalBase8.divide(equivalenciaDRCorrespondiente, 6, RoundingMode.HALF_UP);
                                
                                traslados8Linea23 = totalTraslados8.divide(equivalenciaDRCorrespondiente, 6, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                base8linea23=totalBase8;
                                
                                traslados8Linea23=totalTraslados8;
                                
                            }

                            String linea23 = "23|" + identiFor + "|" + base8linea23 + "|002|Tasa|0.080000|" + traslados8Linea23;
                            logger.info("Linea23: " + linea23);

                            contenidoFactura = contenidoFactura + "\n" + linea23 + "\n";

                            //Preparando para linea20A
                            logger.info("El tipo de cambio es: " + tcCorrespondiente);
                            
                            BigDecimal base8Actual=null;
                            
                            if(todoUSD){
                                
                                base8Actual = totalBase8.multiply(tcCorrespondiente).setScale(2, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                base8Actual = totalBase8.divide(equivalenciaDRCorrespondiente, 10, RoundingMode.HALF_UP);
                                
                            }

                            totalBase8A = totalBase8A.add(base8Actual);

                            totalTras8 = totalBase8.multiply(new BigDecimal("0.080000").setScale(6, RoundingMode.HALF_UP));

                            BigDecimal traslado8Actual = null;
                            if(todoUSD){
                                
                                traslado8Actual = totalTras8.multiply(tcCorrespondiente).setScale(2, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                traslado8Actual = totalTras8.divide(equivalenciaDRCorrespondiente, 10, RoundingMode.HALF_UP);
                                
                            }

                            totalTraslado8A = totalTraslado8A.add(traslado8Actual);

                            logger.info("El total de traslado con tasa a 8% es: " + totalTras8);

                        }

                        if (totalBase0.compareTo(zeroBig) == 1) {

                            logger.info("Linea23: Se encontraron traslados con tasa a 0%");

                            BigDecimal base0linea23 = null;
                            
                            if(!isUSD20){
                                
                                logger.info("Cambiando a mxn por ser la moneda de pago");
                                base0linea23= totalBase0.divide(equivalenciaDRCorrespondiente, 6, RoundingMode.HALF_UP);
                                
                                
                            }else{
                                
                                base0linea23=totalBase0;
                                
                            }
                            
                            //Creando linea 23
                            logger.info("Linea23: El identificador es: " + identiFor);

                            String linea23 = "23|" + identiFor + "|" + base0linea23 + "|002|Tasa|0.000000|0.000000|";
                            logger.info("Linea23: " + linea23);

                            contenidoFactura = contenidoFactura + "\n" + linea23 + "\n";

                            //Preparando para linea20A
                            logger.info("La tasa de cambio es: " + tcCorrespondiente);
                            
                            BigDecimal base0Actual = null;
                            
                            if(todoUSD){
                                
                                base0Actual = totalBase0.multiply(tcCorrespondiente).setScale(2, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                base0Actual = totalBase0.multiply(tcCorrespondiente).setScale(2, RoundingMode.HALF_UP);
                                
                            }
                            
                            totalBase0A = totalBase0A.add(base0Actual);

                            logger.info("El total de traslado con tasa a 0% es: 0.00");

                        }//Fin total base 0
                        
                        if (totalPagadoExento.compareTo(zeroBig) == 1){
                            
                            logger.info("Linea23: Se encontraron exentos");
                            
                            BigDecimal baseExcentolinea23 = null;
                            
                            if(!isUSD20){
                                
                                logger.info("Cambiando a mxn por ser la moneda de pago");
                                baseExcentolinea23= totalPagadoExento.divide(equivalenciaDRCorrespondiente).setScale(6, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                baseExcentolinea23=totalPagadoExento;
                                
                            }
                            
                            //Creando linea 23
                            logger.info("Linea23: El identificador es: " + identiFor);

                            String linea23 = "23|" + identiFor + "|" + baseExcentolinea23 + "|002|Exento|||";
                            logger.info("Linea23: " + linea23);

                            contenidoFactura = contenidoFactura + "\n" + linea23 + "\n";

                            //Preparando para linea 20A
                            
                            BigDecimal baseExentoActual = null;
                            if(todoUSD){
                                
                                baseExentoActual = totalPagadoExento.multiply(tcCorrespondiente).setScale(2, RoundingMode.HALF_UP);
                                
                            }else{
                                
                                baseExentoActual = totalPagadoExento.divide(equivalenciaDRCorrespondiente, 10, RoundingMode.HALF_UP);
                                
                            }
                            
                            totalBaseExentoA = totalBaseExentoA.add(baseExentoActual);

                        }

                        logger.info("");
                        logger.info("Fin proceso de lineas 23");
                        logger.info("------------------------------------------------------------------------------------------");

                    }//Fin coincide con el identificador

                }//Fin la lista de lineas 21 no es vacia


            }//Fin for por cada identificador
//-----------------------------------------------------------------------------------------------------------------------------------------------------


            //Iniciando a procesar linea 20A
            logger.info("Linea20A: Iniciando a crear linea 20A");

            //Mover esto
            String base16A = "";
            String base8A = "";
            String base0A = "";
            String baseExento="";

            String tras16 = "";
            String tras8 = "";
            String tras0 = "";

            //Obteniendo 20A,12

            logger.info("Linea20A: El monto de pago es: " + montoPagoAcumulado);

            //Calculando las bases y traslados de cada uno
            if (totalBase16A.compareTo(zeroBig) == 1) {

                logger.info("Se encontraron impuestos a 16, calculando en moneda nacional");

                logger.info("La base en pesos mexicanos es: " + totalBase16A);
                totalBase16A = totalBase16A.setScale(2, RoundingMode.HALF_UP);
                base16A = totalBase16A.toString();

                logger.info("El total de traslados en pesos mexicanos es: " + totalTraslado16A);
                totalTraslado16A = totalTraslado16A.setScale(2,RoundingMode.HALF_UP);
                tras16 = totalTraslado16A.toString();

            }

            if (totalBase8A.compareTo(zeroBig) == 1) {

                logger.info("Se encontraron impuestos a 8, calculando en moneda nacional");

                logger.info("La base en pesos mexicanos es: " + totalBase8A);
                totalBase8A = totalBase8A.setScale(2, RoundingMode.HALF_UP);
                base8A = totalBase8A.toString();

                logger.info("El total de traslados en pesos mexicanos es: " + totalTraslado8A);
                totalTraslado8A = totalTraslado8A.setScale(2,RoundingMode.HALF_UP);
                tras8 = totalTraslado8A.toString();

            }

            if (totalBase0A.compareTo(zeroBig) == 1) {

                logger.info("Se encontraron impuestos a 0, calculando en moneda nacional");

                logger.info("La base en pesos mexicanos es: " + totalBase0A);
                totalBase0A = totalBase0A.setScale(2, RoundingMode.HALF_UP);
                base0A = totalBase0A.toString();

                tras0 = "0.00";

            }
            
            if (totalBaseExentoA.compareTo(BigDecimal.ZERO) > 0) {
                totalBaseExentoA = totalBaseExentoA.setScale(2, RoundingMode.HALF_UP);
                baseExento=totalBaseExentoA.toString();
            }

            String linea20A = "20A||||" + base16A + "|" + tras16 + "|" + base8A + "|" + tras8 + "|" + base0A + "|" + tras0 + "|" + baseExento + "|" + montoPagoAcumulado.toString() + "|";
            logger.info("Linea20A: " + linea20A);

            contenidoFactura = contenidoFactura + linea20A;

            logger.info("El txt final es: " + contenidoFactura);
            logger.info("");
            logger.info("");
            
            return contenidoFactura;
        
        }else{
            
            logger.error("No se encontraron lineas 21");
            return contenidoTxt + "\n" + "ERROR|No se encontraron lineas con identificador 21 en el archivo";
            
        }
        
        

    }
    

    
    
    public ArrayList<ArrayList<String>> verificarDobles21(ArrayList<ArrayList<String>> listaLineas21){

        Map<String, List<ArrayList<String>>> agrupados = new HashMap<>();

        // Agrupar por identificador (posición 1)
        logger.info("Buscando los diferentes identificadores");
        for (ArrayList<String> linea : listaLineas21) {
            String identificador = linea.get(1);
        
            logger.info("El identificador en turno es:" + identificador);

            agrupados.computeIfAbsent(identificador, k -> new ArrayList<>()).add(linea);
        }

        
        for (Map.Entry<String, List<ArrayList<String>>> entry : agrupados.entrySet()) {

            List<ArrayList<String>> grupo = entry.getValue();

            // Si hay más de uno → están duplicados
            if (grupo.size() > 1) {

                for (ArrayList<String> linea : grupo) {

                    String nuevoValor = linea.get(0);

                    linea.set(0, nuevoValor + "_DUPLICADO");
                
                    logger.info("La nueva liena es:" + nuevoValor);
                }
            }
        }//Fin for por cada linea
        
        //Iniciando nuevo for para unificar duplicados
        
        ArrayList<ArrayList<String>> resultadoFinal = new ArrayList<>();
        Map<String, List<ArrayList<String>>> duplicados = new HashMap<>();
        
        
        for (ArrayList<String> linea : listaLineas21) {
            
            String lineaVerificable=linea.get(0);
            if(lineaVerificable.endsWith("_DUPLICADO")){
                
                logger.info("Se encontro la siguiente liena duplicada");
                logger.info(lineaVerificable);
                String identificador = linea.get(1);
                
                duplicados.computeIfAbsent(identificador, k -> new ArrayList<>()).add(linea);
                
            }else{
                
                logger.info("No se encontro duplicada, agregando asi");
                logger.info(lineaVerificable);
                
                resultadoFinal.add(linea);
            }

        }
        
        for (Map.Entry<String, List<ArrayList<String>>> entry : duplicados.entrySet()) {

            List<ArrayList<String>> grupo = entry.getValue();

            ArrayList<String> base = new ArrayList<>(grupo.get(0));

            BigDecimal suma8 = BigDecimal.ZERO;
            BigDecimal suma9 = BigDecimal.ZERO;

            for (ArrayList<String> linea : grupo) {

                String nuevoValor = linea.get(0);
                    
                String[] linea21Descomponer = nuevoValor.split("\\|");
                    
                String monto1=linea21Descomponer[8];
                logger.info(monto1);
                suma8 = suma8.add(new BigDecimal(monto1));
                    
                String monto2=linea21Descomponer[9];
                logger.info(monto2);
                suma9 = suma9.add(new BigDecimal(monto2));
                
            }

            logger.info("La suma del campo 8 es" + suma8.toString());
            
            logger.info("La suma del campo 8 es" + suma9.toString());
            
            String nuevaLinea21Unificada = base.get(0);
            
            String[] linea21Array = nuevaLinea21Unificada.split("\\|");
            linea21Array[8]=suma8.toString();
            linea21Array[9]=suma9.toString();
            
            nuevaLinea21Unificada = String.join("|", linea21Array);

            // Opcional: quitar el _DUPLICADO
            base.set(0, nuevaLinea21Unificada.replace("_DUPLICADO", ""));

            resultadoFinal.add(base);
        }

        return resultadoFinal;
    }
    
    
    
}
