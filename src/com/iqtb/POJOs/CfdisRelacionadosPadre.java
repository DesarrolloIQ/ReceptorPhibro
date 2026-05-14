package com.iqtb.POJOs;

import java.util.HashSet;
import java.util.Set;

public class CfdisRelacionadosPadre implements java.io.Serializable {

    private Integer idCfdRelacionadoPadre;
    private Cfds cfds;
    private String tipoCfdPadre;
    private String tipoRelacion;
    private Set<CfdisRelacionadosHijo> cfdisRelacionadosHijo = new HashSet<CfdisRelacionadosHijo>(0);

    public CfdisRelacionadosPadre() {}

    public CfdisRelacionadosPadre(Integer idCfdRelacionadoPadre, Cfds cfds,
                                   String tipoCfdPadre, String tipoRelacion) {
        this.idCfdRelacionadoPadre = idCfdRelacionadoPadre;
        this.cfds = cfds;
        this.tipoCfdPadre = tipoCfdPadre;
        this.tipoRelacion = tipoRelacion;
    }

    public Integer getIdCfdRelacionadoPadre() { return idCfdRelacionadoPadre; }
    public void setIdCfdRelacionadoPadre(Integer idCfdRelacionadoPadre) { this.idCfdRelacionadoPadre = idCfdRelacionadoPadre; }

    public Cfds getCfds() { return cfds; }
    public void setCfds(Cfds cfds) { this.cfds = cfds; }

    public String getTipoCfdPadre() { return tipoCfdPadre; }
    public void setTipoCfdPadre(String tipoCfdPadre) { this.tipoCfdPadre = tipoCfdPadre; }

    public String getTipoRelacion() { return tipoRelacion; }
    public void setTipoRelacion(String tipoRelacion) { this.tipoRelacion = tipoRelacion; }

    public Set<CfdisRelacionadosHijo> getCfdisRelacionadosHijo() { return cfdisRelacionadosHijo; }
    public void setCfdisRelacionadosHijo(Set<CfdisRelacionadosHijo> cfdisRelacionadosHijo) { this.cfdisRelacionadosHijo = cfdisRelacionadosHijo; }
}
