/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.recursos;

/**
 *Clase que verifica la codificacion del archivo XML
 * 
 * @author Valentin
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Scanner;
import javax.imageio.IIOException;
//import org.apache.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;

public final class DetectarCodif
{
//  private static Logger logger = Logger.getLogger(DetectarCodif.class);
  private static final int BUFFER_SIZE = 4096;
  public static final String UTF_8 = "UTF-8";
  public static final String UTF8 = "UTF8";
  public static final String ISO8859_1 = "ISO8859_1";
  public static final String WINDOWS_1252 = "windows-1252";
  private UniversalDetector detector;
  private boolean useWindows1252 = false;

  private boolean checkUtf16WithoutBOM = false;

  public DetectarCodif()
  {
    detector = new UniversalDetector(null);
  }

  private String getEncondingWithUniversal(InputStream fis)
    throws IOException
  {
    String encoding = null;
    try {
      byte[] buf = new byte[4096];
      byte[] startingBytes = null;
      int nread;
      while (((nread = fis.read(buf)) > 0) && (!detector.isDone())) {
        if ((checkUtf16WithoutBOM) && (startingBytes == null) && (nread > 20)) {
          startingBytes = Arrays.copyOf(buf, nread);
        }
        detector.handleData(buf, 0, nread);
      }
      detector.dataEnd();
      encoding = detector.getDetectedCharset();
      if ((checkUtf16WithoutBOM) && (startingBytes != null))
        encoding = checkUtf16EncodingWithoutBOM(encoding, startingBytes);
    }
    finally
    {
      detector.reset();
    }
    return encoding;
  }

  private String checkUtf16EncodingWithoutBOM(String foundEncoding, byte[] dataBuffer)
  {
    if ("windows-1252".equalsIgnoreCase(foundEncoding)) {
      String s;
      try {
        s = new String(dataBuffer, foundEncoding);
      } catch (UnsupportedEncodingException e) {
        return foundEncoding;
      }

      int index = s.indexOf(0);
      if (index >= 0) {
//        logger.debug("Null character found. Trying to detect UTF-16 or UTF-32 without BOM.");
          System.out.println("Null character found. Trying to detect UTF-16 or UTF-32 without BOM.");
        if ((index == s.length() - 1) || (s.charAt(index + 1) != 0)) {
          if (index % 2 == 0)
            foundEncoding = "UTF-16BE";
          else {
            foundEncoding = "UTF-16LE";
          }
        }
        else if (index % 4 == 0)
          foundEncoding = "UTF-32BE";
        else {
          foundEncoding = "UTF-32LE";
        }
      }

    }

    return foundEncoding;
  }

  public String getEnconding(File file)
    throws IOException
  {
    return getEnconding(new FileInputStream(file));
  }

  public String getEnconding(InputStream inputStream)
    throws IOException
  {
    BufferedInputStream in = new BufferedInputStream(inputStream);
    in.mark(2147483647);
    String encoding = getEncondingWithUniversal(in);
    if (encoding == null) {
      try {
        in.reset();
      } catch (IOException e) {
//        logger.debug("La marca del InputStream se perdió, podría no validarse el encoding correctamente: " + e);
//          System.out.println("La marca del InputStream se perdió, podría no validarse el encoding correctamente: "+e);
          throw new IIOException("No se puede obtener la codificacion de este archivo");
      }
//      logger.warn("No encoding detected. Detenting with DetectarCP");
//        System.out.println("No encoding detected. Detenting with CPDetector");
      DetectarCP cpDetector = new DetectarCP();
      Charset charsetEncoding = cpDetector.getCharset(in);
//        System.out.println("charsetencoding: "+charsetEncoding.displayName());
      encoding = charsetEncoding.name();
//        System.out.println("Encoding de CP DETECTOR:"+encoding);
//        encoding = "null";
    }

//    logger.debug("Detected encoding: " + encoding);
//      System.out.println("Detected encoding: " + encoding);
    if ((encoding.equalsIgnoreCase("windows-1252")) && (!useWindows1252)) {
      encoding = "ISO8859_1";
    }
    return encoding;
  }

  /**
   * Forma de llamar la clase para 
   * obtener la codificacion del XML
   * aunque se puede guardar en una variable
   * y regresarlo para continuar el proceso
   * 
   * @param args 
   */
  public static void main(String[] args)
  {
    DetectarCodif detector = new DetectarCodif();
      File file = new File("/Users/Valentin/Downloads/brinco/022626.txt");//CMO990720SX8_CMOD_UTF-8//EB0000.TXT
//      archivoStream = new FileInputStream(file);
//      String charset = detector.getEnconding(archivoStream);
//      System.out.println("CHAR  CP: " + charset);
//        System.out.println("contiene el archivo: "+(InputStream)archivoStream.toString());
FileInputStream inputStream = null;
Scanner sc = null;
String line="";
try {   
    inputStream = new FileInputStream(file);
    String charset = detector.getEnconding(inputStream);
      System.out.println("CHAR  CP: " + charset);
    sc = new Scanner(file, charset);
    while (sc.hasNextLine()) {
        line += sc.nextLine();
        line += "\n";
//         System.out.println("entro aqui: "+ line);
    }
    
    // note that Scanner suppresses exceptions
//    if (sc.ioException() != null) {
//        throw sc.ioException();
//    }
    }catch (FileNotFoundException ex) {
          System.out.println("Error en file: "+ ex.getMessage());
      } catch (IOException ex) {
          System.out.println("Error en ioexcetion: "+ex.getMessage());
      } finally {
    if (inputStream != null) {
        try {
            inputStream.close();
        } catch (IOException ex) {
            System.out.println("Error IO: "+ex.getMessage());
        }
    }
    if (sc != null) {
        sc.close();
    }
//    System.out.print("imprimiendo lineas: "+line);
}
      
  }

  public void setUseWindows1252(boolean useWindows1252)
  {
    this.useWindows1252 = useWindows1252;
  }

  public void setCheckUtf16WithoutBOM(boolean checkIfUTF16WithoutBOM)
  {
    checkUtf16WithoutBOM = checkIfUTF16WithoutBOM;
  }
}