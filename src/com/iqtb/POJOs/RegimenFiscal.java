package com.iqtb.POJOs;

import java.util.Date;

/**
 *
 * @author macminizuri
 */
public class RegimenFiscal implements java.io.Serializable {
    
    private Integer idRegimen;
    private String idTipoPersona;
    private String claveRegimenFiscal;
    private String descripcionRegimen;
    private Date fechaInicioVigencia;
    private Date fechaUltimaActualizacion;
    
    public RegimenFiscal(){
        //Constructor vacio
    }
    
    public RegimenFiscal(Integer idRegimen, String idTipoPersona, String claveRegimenFiscal, String descripcionRegimen, Date fechaInicioVigencia, Date fechaUltimaActualizacion){
        
        this.idRegimen=idRegimen;
        this.idTipoPersona=idTipoPersona;
        this.claveRegimenFiscal=claveRegimenFiscal;
        this.descripcionRegimen=descripcionRegimen;
        this.fechaInicioVigencia=fechaInicioVigencia;
        this.fechaUltimaActualizacion=fechaUltimaActualizacion;
        
    }

    public Integer getIdRegimen() {
        return idRegimen;
    }

    public void setIdRegimen(Integer idRegimen) {
        this.idRegimen = idRegimen;
    }

    public String getIdTipoPersona() {
        return idTipoPersona;
    }

    public void setIdTipoPersona(String idTipoPersona) {
        this.idTipoPersona = idTipoPersona;
    }

    public String getClaveRegimenFiscal() {
        return claveRegimenFiscal;
    }

    public void setClaveRegimenFiscal(String claveRegimenFiscal) {
        this.claveRegimenFiscal = claveRegimenFiscal;
    }

    public Date getFechaInicioVigencia() {
        return fechaInicioVigencia;
    }

    public void setFechaInicioVigencia(Date fechaInicioVigencia) {
        this.fechaInicioVigencia = fechaInicioVigencia;
    }

    public Date getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(Date fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public String getDescripcionRegimen() {
        return descripcionRegimen;
    }

    public void setDescripcionRegimen(String descripcionRegimen) {
        this.descripcionRegimen = descripcionRegimen;
    }
    
    
    
}