/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.recursos;

import cpdetector.io.ASCIIDetector;
import cpdetector.io.CodepageDetectorProxy;
import cpdetector.io.JChardetFacade;
import cpdetector.io.UnicodeDetector;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 * @author Valentin
 */
public class DetectarCP
{
  private final CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

  public DetectarCP()
  {
    JChardetFacade jchardet = JChardetFacade.getInstance();
    jchardet.setGuessing(true);

    detector.add(UnicodeDetector.getInstance());

    detector.add(jchardet);

    detector.add(ASCIIDetector.getInstance());
  }

  public Charset getCharset(File document)
    throws IOException
  {
    Charset charset = null;
    charset = detector.detectCodepage(document.toURL());
    return charset;
  }

  public Charset getCharset(InputStream document)
    throws IOException
  {
    Charset charset = null;
    charset = detector.detectCodepage(document,document.available());//2147483647
    return charset;
  }

}
