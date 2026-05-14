/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package receptorphibro;

import com.iqtb.DAOs.CfdsDAO;
import com.iqtb.DAOs.Documentos_RecibidosDAO;
import com.iqtb.DAOs.ErroresGeneracionDAO;
import com.iqtb.DAOs.ServiciosDAO;
import com.iqtb.DAOs.UsuariosRecepcionDAO;
import com.iqtb.POJOs.DocumentosRecibidos;
import com.iqtb.POJOs.ErroresGeneracion;
import com.iqtb.POJOs.Servicios;
import com.iqtb.POJOs.UsuariosRecepcion;
import com.iqtb.recursos.DetectarCodif;
import com.iqtb.recursos.ProcesarLineasPagos;
import com.iqtb.utils.ProcesadorCfdisService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
/**
 *
 * @author macminidesarrollo
 */
public class ReceptorPhibro {

    private static Logger logger = Logger.getLogger("ReceptorPhibro");

    private static ArrayList<String> listFolios;

    public static void main(String[] args) {
        PropertyConfigurator.configure("../configReceptorPhilbro/log4j.properties");
        //PropertyConfigurator.configure("configReceptorPhilbro/log4j.properties");
        
        //En cada corrida se verifica que los Cfds de PAGO o EGRESO ya esten revisados en BD
        ProcesadorCfdisService procesadorCfdisService = new ProcesadorCfdisService();
        
        
        
        List<Servicios> servicios = (new ServiciosDAO()).getServicio("RECEIVER", "SLEEP_TIME");
        int tiempo = 0;
        if (servicios != null && servicios.size() > 0) {
            try {
                tiempo = Integer.valueOf(((Servicios) servicios.get(0)).getValor()).intValue();
                logger.info("ID: " + ((Servicios) servicios.get(0)).getIdServicio());
                logger.info("Prueba para ver si esta compilando al 100");
            } catch (Exception e) {
                logger.error("Error al obtener el tiempo, Error: " + e.getMessage());
                tiempo = 5000;
            }
        } else {
            logger.error("No se encontro Servicio: RECEIVER con propiedad: SLEEP_TIME, se asignara 5000");
            tiempo = 5000;
        }
        logger.info("Tiempo: " + tiempo);
        ReceptorPhibro receptorPhibro = new ReceptorPhibro();
        boolean mostrarLog = true;
        listFolios = new ArrayList<String>();
        int ciclos = 0;
        while (true) {
            //SE LLAMAN LOS METODOS DEL GAS PARA CALCULAR LOS PAGOS DE TODOS LOS CFDS EN LA DB ALV
            procesadorCfdisService.procesarCfdisDePago();
            procesadorCfdisService.procesarCfdisDeEgreso();
            procesadorCfdisService.procesarCfdisDeIngreso();
            
            
            mostrarLog = receptorPhibro.procesar(mostrarLog);
            if (listFolios.size() > 0) {
                if (mostrarLog) {
                    ciclos = 0;
                } else {
                    ciclos++;
                    if (ciclos >= 2) {
                        listFolios = new ArrayList<String>();
                    }
                }
            }
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ex) {
                logger.error("Error al dormir el hilo principal, ERROR: " + ex.getMessage());
            }
        }
    }

    private boolean procesar(boolean mostrarLog) {
        List<String> procesados = new ArrayList<String>();
        UsuariosRecepcionDAO usuariosRecepcionDAO = new UsuariosRecepcionDAO();
        List<UsuariosRecepcion> usuariosRecepcion = usuariosRecepcionDAO.getTodos();
        if (usuariosRecepcion != null && usuariosRecepcion.size() > 0) {
            if (mostrarLog) {
                logger.debug("Existen: " + usuariosRecepcion.size() + " usuarios Recepcion");
                for (UsuariosRecepcion usuarios : usuariosRecepcion) {
                    logger.debug("Nombre: " + usuarios.getNombre());
                    if (usuarios.getHomeftp() != null && !usuarios.getHomeftp().trim().isEmpty()) {
                        logger.debug("Ruta FTP: " + usuarios.getHomeftp());
                    }
                    if (usuarios.getHomesftp() != null && !usuarios.getHomesftp().trim().isEmpty()) {
                        logger.debug("Ruta SFTP: " + usuarios.getHomesftp());
                    }
                    logger.debug("");
                }
            }
            for (UsuariosRecepcion usuarios : usuariosRecepcion) {
                if (usuarios.getHomeftp() != null && !usuarios.getHomeftp().trim().isEmpty()) {
                    List<String> procesadosPorUsuario = recibir(usuarios, usuarios.getHomeftp());
                    if (procesadosPorUsuario != null && procesadosPorUsuario.size() > 0) {
                        logger.info("Se procesaron " + procesadosPorUsuario.size() + " archivos en FTP: " + procesadosPorUsuario);
                        logger.info("En el usuario: " + usuarios.getNombre() + ", Ruta: " + usuarios.getHomeftp());
                        procesados.addAll(procesadosPorUsuario);
                    }
                }
                if (usuarios.getHomesftp() != null && !usuarios.getHomesftp().trim().isEmpty()) {
                    List<String> procesadosPorUsuario = recibir(usuarios, usuarios.getHomesftp());
                    if (procesadosPorUsuario != null && procesadosPorUsuario.size() > 0) {
                        logger.info("Se procesaron " + procesadosPorUsuario.size() + " archivos en SFTP: " + procesadosPorUsuario);
                        logger.info("En el usuario: " + usuarios.getNombre() + ", Ruta: " + usuarios.getHomeftp());
                        procesados.addAll(procesadosPorUsuario);
                    }
                }
            }
            if (procesados.size() > 0) {
                logger.info("Se procesaron " + procesados.size() + " archivos en total: " + procesados);
            }
        } else {
            logger.error("No existen usuarios Recepcion");
        }
        if (procesados.size() > 0) {
            return true;
        }
        return false;
    }

    private List recibir(UsuariosRecepcion usuario, String rutaHome) {
        List<String> procesados = new ArrayList<String>();
        Integer idEmpresa = usuario.getSucursales().getEmpresas().getIdEmpresa();
        Integer idSucursal = usuario.getSucursales().getIdSucursal();
        //logger.info("idSucursal: "+idSucursal);
        CfdsDAO cfdsDAO = new CfdsDAO();
        String ruta = rutaHome;
        if (!ruta.endsWith("/")) {
            ruta = ruta + "/";
        }
        File carpeta = new File(ruta);
        if (carpeta.exists()) {
            logger.debug("Buscando archivos en: " + ruta);
            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    Pattern pat = Pattern.compile(".+\\.((iqtb)|(IQTB)|(txt))$");
                    Matcher mat = pat.matcher(pathname.getName());
                    return mat.matches();
                }
            };
            
            File[] archivos = carpeta.listFiles(fileFilter);
            if (archivos != null && archivos.length > 0) {
                logger.debug("sizeArchivos: "+archivos.length);
                String codificacion = "";
                DetectarCodif detectarCodif = new DetectarCodif();
                FileInputStream fileInputStream;
                Map<String, Long> sizes = new HashMap<String, Long>();
                for (File archivo : archivos) {
                    sizes.put(archivo.getName(), archivo.length());
                    logger.info(archivo.getName() + " Tamanio" + archivo.length());
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    logger.error("Error al esperar un segundo para verificar si los archivos ya se terminaron de subir, ERROR: " + ex.getMessage());
                }
                for (File archivo : archivos) {
                    logger.info("- - - - - - - - - - Procesando Archivo: " + archivo.getName());
                    Long size = sizes.get(archivo.getName());
                    int intento = 0;
                    while (size != archivo.length() && intento < 5) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException ex) {
                            logger.error("Error al esperar un segundo para verificar si el archivo ya se termino de subir, ERROR: " + ex.getMessage());
                        }
                        size = sizes.get(archivo.getName());
                        intento++;
                    }
                    if (size != archivo.length()) {
                        logger.error("El archivo: " + archivo.getName() + " esta tardando en subir, se continuara con el siguiente");
                    } else {
                        procesados.add(archivo.getName());
                        try {
                            fileInputStream = new FileInputStream(archivo);
                            codificacion = detectarCodif.getEnconding(fileInputStream);
                            fileInputStream.close();
                        } catch (IOException ex) {
                            logger.error("Error al obtener la codificacion, ERROR: " + ex.getMessage());
                            procesarError(usuario, archivo, "El contenido del archivo no se puede almacenar debido a que no se reconoce la codificacion", "Codificacion Incorrecta, ERROR: Error al obtener la codificacion, ERROR: " + ex.getMessage());
                            moverIqtb(archivo, new File(ruta + "errors/" + archivo.getName() + ".error"), 0);
                        }
                        logger.info("\t\tArchivo encontrado: " + archivo.getName() + " Codificacion: " + codificacion);
                        logger.info("\t\tRuta donde se encontro: " + ruta);
                        if (codificacion == null) {
                            logger.error("Codificacion Incorrecta [" + codificacion + "]");
                            procesarError(usuario, archivo, "El contenido del archivo no se puede almacenar debido a que no se reconoce la codificacion", "Codificacion Incorrecta [" + codificacion + "]");
                            moverIqtb(archivo, new File(ruta + "errors/" + archivo.getName() + ".error"), 0);
                        } else {
                            logger.info("Leyendo Archivo");
                            try {
                                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), codificacion));
                                String error = "";
                                String contenido = "";
                                String contenidoOriginal = "";
                                boolean contiene20 = false;
                                boolean contiene21 = false;
                                String linea;
                                while ((linea = in.readLine()) != null) {
                                    if (!contenido.isEmpty()) {
                                        contenidoOriginal = contenidoOriginal + "\n";
                                    }
                                    if (linea.startsWith("20|")) {
                                        contiene20 = true;
                                    }
                                    if (linea.startsWith("21|")) {
                                        contiene21=true;
                                    }
                                    contenidoOriginal = contenidoOriginal + linea;
                                    
                                    if (!contenido.isEmpty()) {
                                        contenido = contenido + "\n";
                                    }
                                    contenido = contenido + linea;
                                }
                                in.close();
                                if (error.isEmpty()) {
                                    File movido = moverIqtb(archivo, new File(ruta + "errors/" + archivo.getName()), 0);
                                    if (contiene21 && contiene20) {
                                        logger.info("* * * * * El archivo se procesara debido a que tiene complemento de pagos");
                                        logger.info("Se comenzara a hacer la conversion del complementoPagos[2.0]");
                                        
                                        ProcesarLineasPagos procesarLineas = new ProcesarLineasPagos();
                                        String nuevoArchivo = procesarLineas.procesarArchivoPagos(contenido, idSucursal);
                                        
                                        
                                        //ComplementoPagos20 validado y convertido
                                        if(nuevoArchivo != null){
                                            //Validacion del Txt

                                            if(generarTXT(ruta, archivo.getName().replace(".txt", ".TXT"), nuevoArchivo, 0, codificacion)){
                                                logger.info("Creado Correctamente con complementoPagos20");
                                            }
   
                                        } else {
                                            if (generarTXT(ruta, archivo.getName().replace(".txt", ".TXT"), contenido, 0, codificacion)) {
                                                logger.info("Creado Correctamente con contenido preprocesado");
                                            }
                                        }
                                    } else if (generarTXT(ruta, archivo.getName().replace(".txt", ".TXT"), contenidoOriginal, 0, codificacion)) {
                                        logger.info("Creado Correctamente con contenido Original");
                                    } else {
                                        logger.error("Error al crear el archivo: " + archivo.getName().replace(".txt", ".TXT"));
                                        if (movido != null) {
                                            moverIqtb(movido, new File(ruta + "errors/" + archivo.getName() + ".error"), 0);
                                        }
                                    }
                                } else {
                                    logger.info("ERROR: " + error);
                                    procesarError(usuario, archivo, contenidoOriginal, error);
                                    moverIqtb(archivo, new File(ruta + "errors/" + archivo.getName() + ".error"), 0);
                                }
                            } catch (FileNotFoundException ex) {
                                logger.error("Error al leer el archivo, ERROR[FileNotFoundException]: " + ex.getMessage());
                            } catch (UnsupportedEncodingException ex) {
                                logger.error("Error al leer el archivo, ERROR[UnsupportedEncodingException]: " + ex.getMessage());
                            } catch (IOException ex) {
                                logger.error("Error al leer el archivo, ERROR[IOException]: " + ex.getMessage());
                            }
                        }
                    }
                }
            } else {
                logger.debug("No existen archivos con terminacion .iqtb .IQTB o .txt");
            }
        } else {
            logger.debug("La ruta: " + ruta + " no existe");
        }
        return procesados;
    }

    private boolean verificarExistencia(String carpeta) {
        File trabajo = new File(carpeta);
        if (trabajo.exists()) {
            logger.debug("La carpeta " + carpeta + " existe");
            return true;
        }
        logger.info("La carpeta " + carpeta + " NO existe, se creara");
        if (trabajo.mkdirs()) {
            logger.info("La carpeta " + carpeta + " Creada Correctamente");
            return true;
        }
        logger.error("Error al Crear la carpeta: " + carpeta);
        return false;
    }

    private File moverIqtb(File origen, File destino, int intento) {
        String ruta = destino.getAbsolutePath().replace(destino.getName(), "");
        String error = destino.getName().endsWith("error") ? ".error" : "";
        if (verificarExistencia(ruta)) {
            if (!destino.exists()) {
                if (origen.renameTo(destino)) {
                    logger.info("Archivo movido Correctamente");
                } else {
                    logger.error("Error al mover el Archivo");
                }
                return destino;
            }
            File destinoNuevo = new File(ruta + origen.getName().replace(".iqtb", "-" + (intento + 1) + ".iqtb").replace(".IQTB", "-" + (intento + 1) + ".IQTB").replace(".txt", "-" + (intento + 1) + ".txt") + error);
            logger.info("El destino: " + destino.getName() + " ya existe, se intentara de nuevo con: " + destinoNuevo.getName());
            return moverIqtb(origen, destinoNuevo, intento + 1);
        }
        logger.error("Imposible mover el archivo");
        return null;
    }

    private boolean generarTXT(String ruta, String nombreArchivo, String contenidoArchivo, int intento, String codificacion) {
        logger.info("Generando TXT");
        boolean correcto = false;
        try {
            File archivo = new File(ruta + nombreArchivo);
            if (archivo.exists()) {
                logger.info("El Archivo: " + nombreArchivo + " ya existe");
                if (intento == 0) {
                    logger.info("\tSe intentara con: " + nombreArchivo.replace(".TXT", "-" + (intento + 1) + ".TXT"));
                    return generarTXT(ruta, nombreArchivo.replace(".TXT", "-" + (intento + 1) + ".TXT"), contenidoArchivo, intento + 1, codificacion);
                }
                logger.info("\tSe intentara con: " + nombreArchivo.replace("-" + intento + ".TXT", "-" + (intento + 1) + ".TXT"));
                return generarTXT(ruta, nombreArchivo.replace("-" + intento + ".TXT", "-" + (intento + 1) + ".TXT"), contenidoArchivo, intento + 1, codificacion);
            }
            logger.info("Creando Archivo " + archivo.getName());
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo.getAbsolutePath(), true), codificacion));
            contenidoArchivo = new String(contenidoArchivo.getBytes(codificacion), codificacion);
            out.append(contenidoArchivo);
            out.close();
            correcto = true;
        } catch (IOException e) {
            logger.error("Error al generar el TXT " + ruta + nombreArchivo + ", ERROR[IOException]: " + e);
        }
        return correcto;
    }

    private void procesarError(UsuariosRecepcion usuario, File archivo, String contenido, String error) {
        logger.info("Almacenando Documento recibido y Error Generacion");
        DocumentosRecibidos doc_rec = new DocumentosRecibidos(usuario.getSucursales(), usuario.getTiposCfd(), new Date(), contenido, 0, false);
        doc_rec.setIdentificador(null);
        doc_rec.setNombre(archivo.getName());
        doc_rec.setEstado("ERROR");
        doc_rec.setReportado(null);
        Documentos_RecibidosDAO recibidosDAO = new Documentos_RecibidosDAO();
        doc_rec = recibidosDAO.guardar(doc_rec);
        if (doc_rec != null) {
            ErroresGeneracionDAO errorDAO = new ErroresGeneracionDAO();
            ErroresGeneracion errorGeneracion = new ErroresGeneracion(doc_rec, error, error, null, error);
            if (errorDAO.nuevo(errorGeneracion)) {
                logger.info("Error de Generacion almacenado Correctamente, Error: " + error);
            } else {
                logger.error("No se pudo almacenar el error de Generacion: " + error);
            }
        } else {
            logger.error("Error al almacenar el documeto recibido");
        }
    }
}
