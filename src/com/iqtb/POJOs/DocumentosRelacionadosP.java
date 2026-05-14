package com.iqtb.POJOs;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class DocumentosRelacionadosP implements java.io.Serializable {

    private Integer idDocumentoRelacionadoP;
    private Pagos pagos;
    private Cfds cfds;
    private String idDocumento;
    private String serie;
    private String folio;
    private String monedaDr;
    private BigDecimal equivalenciaDr;
    private Integer numParcialidad;
    private BigDecimal importeSaldoAnterior;
    private BigDecimal importePagado;
    private BigDecimal importeSaldoInsoluto;
    private String objetoImpuestosDr;
    private String estadoRelacion;
    private String descripcionError;
    private Set<RetencionesDr> retencionesDr = new HashSet<RetencionesDr>(0);
    private Set<TrasladosDr> trasladosDr = new HashSet<TrasladosDr>(0);

    public DocumentosRelacionadosP() {}

    public DocumentosRelacionadosP(Integer idDocumentoRelacionadoP, Pagos pagos,
                                   String idDocumento, Integer numParcialidad,
                                   BigDecimal importeSaldoAnterior, BigDecimal importePagado,
                                   BigDecimal importeSaldoInsoluto, String objetoImpuestosDr,
                                   String estadoRelacion) {
        this.idDocumentoRelacionadoP = idDocumentoRelacionadoP;
        this.pagos = pagos;
        this.idDocumento = idDocumento;
        this.numParcialidad = numParcialidad;
        this.importeSaldoAnterior = importeSaldoAnterior;
        this.importePagado = importePagado;
        this.importeSaldoInsoluto = importeSaldoInsoluto;
        this.objetoImpuestosDr = objetoImpuestosDr;
        this.estadoRelacion = estadoRelacion;
    }

    public Integer getIdDocumentoRelacionadoP() { return idDocumentoRelacionadoP; }
    public void setIdDocumentoRelacionadoP(Integer idDocumentoRelacionadoP) { this.idDocumentoRelacionadoP = idDocumentoRelacionadoP; }

    public Pagos getPagos() { return pagos; }
    public void setPagos(Pagos pagos) { this.pagos = pagos; }

    public Cfds getCfds() {return cfds;}
    public void setCfds(Cfds cfds) {this.cfds = cfds;}

    public String getIdDocumento() { return idDocumento; }
    public void setIdDocumento(String idDocumento) { this.idDocumento = idDocumento; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public String getMonedaDr() { return monedaDr; }
    public void setMonedaDr(String monedaDr) { this.monedaDr = monedaDr; }

    public BigDecimal getEquivalenciaDr() { return equivalenciaDr; }
    public void setEquivalenciaDr(BigDecimal equivalenciaDr) { this.equivalenciaDr = equivalenciaDr; }

    public Integer getNumParcialidad() { return numParcialidad; }
    public void setNumParcialidad(Integer numParcialidad) { this.numParcialidad = numParcialidad; }

    public BigDecimal getImporteSaldoAnterior() { return importeSaldoAnterior; }
    public void setImporteSaldoAnterior(BigDecimal importeSaldoAnterior) { this.importeSaldoAnterior = importeSaldoAnterior; }

    public BigDecimal getImportePagado() { return importePagado; }
    public void setImportePagado(BigDecimal importePagado) { this.importePagado = importePagado; }

    public BigDecimal getImporteSaldoInsoluto() { return importeSaldoInsoluto; }
    public void setImporteSaldoInsoluto(BigDecimal importeSaldoInsoluto) { this.importeSaldoInsoluto = importeSaldoInsoluto; }

    public String getObjetoImpuestosDr() { return objetoImpuestosDr; }
    public void setObjetoImpuestosDr(String objetoImpuestosDr) { this.objetoImpuestosDr = objetoImpuestosDr; }

    public String getEstadoRelacion() { return estadoRelacion; }
    public void setEstadoRelacion(String estadoRelacion) { this.estadoRelacion = estadoRelacion; }

    public String getDescripcionError() { return descripcionError; }
    public void setDescripcionError(String descripcionError) { this.descripcionError = descripcionError; }

    public Set<RetencionesDr> getRetencionesDr() { return retencionesDr; }
    public void setRetencionesDr(Set<RetencionesDr> retencionesDr) { this.retencionesDr = retencionesDr; }

    public Set<TrasladosDr> getTrasladosDr() { return trasladosDr; }
    public void setTrasladosDr(Set<TrasladosDr> trasladosDr) { this.trasladosDr = trasladosDr; }
}
