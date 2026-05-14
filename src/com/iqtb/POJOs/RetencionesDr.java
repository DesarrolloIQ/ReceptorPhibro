package com.iqtb.POJOs;

import java.math.BigDecimal;

public class RetencionesDr implements java.io.Serializable {

    private Integer idRetencionDr;
    private DocumentosRelacionadosP documentosRelacionadosP;
    private BigDecimal baseDr;
    private String impuestoDr;
    private String tipoFactorDr;
    private BigDecimal tasaOCuotaDr;
    private BigDecimal importeP;

    public RetencionesDr() {}

    public RetencionesDr(Integer idRetencionDr, DocumentosRelacionadosP documentosRelacionadosP,
                         BigDecimal baseDr, String impuestoDr, String tipoFactorDr,
                         BigDecimal tasaOCuotaDr, BigDecimal importeP) {
        this.idRetencionDr = idRetencionDr;
        this.documentosRelacionadosP = documentosRelacionadosP;
        this.baseDr = baseDr;
        this.impuestoDr = impuestoDr;
        this.tipoFactorDr = tipoFactorDr;
        this.tasaOCuotaDr = tasaOCuotaDr;
        this.importeP = importeP;
    }

    public Integer getIdRetencionDr() { return idRetencionDr; }
    public void setIdRetencionDr(Integer idRetencionDr) { this.idRetencionDr = idRetencionDr; }

    public DocumentosRelacionadosP getDocumentosRelacionadosP() { return documentosRelacionadosP; }
    public void setDocumentosRelacionadosP(DocumentosRelacionadosP documentosRelacionadosP) { this.documentosRelacionadosP = documentosRelacionadosP; }

    public BigDecimal getBaseDr() { return baseDr; }
    public void setBaseDr(BigDecimal baseDr) { this.baseDr = baseDr; }

    public String getImpuestoDr() { return impuestoDr; }
    public void setImpuestoDr(String impuestoDr) { this.impuestoDr = impuestoDr; }

    public String getTipoFactorDr() { return tipoFactorDr; }
    public void setTipoFactorDr(String tipoFactorDr) { this.tipoFactorDr = tipoFactorDr; }

    public BigDecimal getTasaOCuotaDr() { return tasaOCuotaDr; }
    public void setTasaOCuotaDr(BigDecimal tasaOCuotaDr) { this.tasaOCuotaDr = tasaOCuotaDr; }

    public BigDecimal getImporteP() { return importeP; }
    public void setImporteP(BigDecimal importeP) { this.importeP = importeP; }
}
