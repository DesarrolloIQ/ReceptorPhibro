/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iqtb.recursos;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author macminizuri
 */
public class ProcesarXml {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProcesarXml.class);
    
    public ProcesarXml(){
        //Constructor vacio
    }
    
    public ArrayList<String> buscarTasaoCuota(String xmlContent) {
        ArrayList<String> listaTasas=new ArrayList<String>();
        try{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document doc = documentBuilder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            NodeList listaTasaoCuota = doc.getElementsByTagName("cfdi:Traslado");

            if(listaTasaoCuota.getLength()>0 && listaTasaoCuota!=null){

                for (int i = 0; i < listaTasaoCuota.getLength(); i++) {
                    Node node = listaTasaoCuota.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element impuestosElement = (Element) node;

                        // Verificando si el atributo "TotalImpuestosTrasladados" está presente
                        String baseImpuesto = impuestosElement.getAttribute("Base");
                        
                        if(baseImpuesto !=null && !baseImpuesto.isEmpty()){
                            logger.info("La base del impuesto es: " + baseImpuesto);
                            listaTasas.add(0,baseImpuesto);
                        }
                        
                        String impuestoDR = impuestosElement.getAttribute("Impuesto");
                        
                        if(impuestoDR !=null && !impuestoDR.isEmpty()){
                            logger.info("El impuestoDR es: " + impuestoDR);
                            listaTasas.add(1,impuestoDR);
                        }
                        
                        
                        String tasaCuota = impuestosElement.getAttribute("TasaOCuota");

                        if (tasaCuota != null && !tasaCuota.isEmpty()) {
                            logger.info("Tasa o cuota: " + tasaCuota);
                            listaTasas.add(2, tasaCuota);
                        }
                        else{
                            
                            logger.info("No se encontro TasaOCuota, marcando primeros elementos  de la lista como null");
                            
                            listaTasas.add(2,null);
                            
                        }

                        logger.info("Buscando nodo tipoFactor");
                        String tipoFactorDR = impuestosElement.getAttribute("TipoFactor");

                        if(tipoFactorDR !=null && !tipoFactorDR.isEmpty()){
                            logger.info("El tipo Factor DR es: " + tipoFactorDR);
                            listaTasas.add(3,tipoFactorDR);
                        }

                        

                    }//Fin if se encontro nodo

                }//Fin del for por todos los nodos que contengan cfdi:Impuestos

            }else{
                logger.info("No se encontro nodo de impuestos");
                listaTasas.add(0,"01");
            }
        }catch(Exception e){
            logger.error("Error al obtener los campos del XML");
        }

        return listaTasas;

    }
    
}
