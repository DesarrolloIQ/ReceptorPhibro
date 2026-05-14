package com.iqtb.POJOs;

public class CfdisRelacionadosHijo implements java.io.Serializable {

    private Integer idCfdRelacionadoHijo;
    private CfdisRelacionadosPadre cfdisRelacionadosPadre;
    private String uuid;
    private Cfds cfds;
    private String tipoCfdHijo;
    private String estadoRelacion;
    private String descripcionError;

    public CfdisRelacionadosHijo() {}

    public CfdisRelacionadosHijo(Integer idCfdRelacionadoHijo,
                                  CfdisRelacionadosPadre cfdisRelacionadosPadre) {
        this.idCfdRelacionadoHijo = idCfdRelacionadoHijo;
        this.cfdisRelacionadosPadre = cfdisRelacionadosPadre;
    }

    public Integer getIdCfdRelacionadoHijo() { return idCfdRelacionadoHijo; }
    public void setIdCfdRelacionadoHijo(Integer idCfdRelacionadoHijo) { this.idCfdRelacionadoHijo = idCfdRelacionadoHijo; }

    public CfdisRelacionadosPadre getCfdisRelacionadosPadre() { return cfdisRelacionadosPadre; }
    public void setCfdisRelacionadosPadre(CfdisRelacionadosPadre cfdisRelacionadosPadre) { this.cfdisRelacionadosPadre = cfdisRelacionadosPadre; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public Cfds getCfds() {return cfds;}
    public void setCfds(Cfds cfds) {this.cfds = cfds;}

    public String getTipoCfdHijo() { return tipoCfdHijo; }
    public void setTipoCfdHijo(String tipoCfdHijo) { this.tipoCfdHijo = tipoCfdHijo; }

    public String getEstadoRelacion() { return estadoRelacion; }
    public void setEstadoRelacion(String estadoRelacion) { this.estadoRelacion = estadoRelacion; }

    public String getDescripcionError() { return descripcionError; }
    public void setDescripcionError(String descripcionError) { this.descripcionError = descripcionError; }
}
