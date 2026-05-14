package com.iqtb.POJOs;

import java.math.BigDecimal;

public class TrasladosDr implements java.io.Serializable {

    private Integer idTrasladoDr;
    private DocumentosRelacionadosP documentosRelacionadosP;
    private BigDecimal baseDr;
    private String impuestoDr;
    private String tipoFactorDr;
    private BigDecimal tasaOCuotaDr;
    private BigDecimal importeDr;

    public TrasladosDr() {}

    public TrasladosDr(Integer idTrasladoDr, DocumentosRelacionadosP documentosRelacionadosP,
                       BigDecimal baseDr, String impuestoDr, String tipoFactorDr,
                       BigDecimal tasaOCuotaDr, BigDecimal importeDr) {
        this.idTrasladoDr = idTrasladoDr;
        this.documentosRelacionadosP = documentosRelacionadosP;
        this.baseDr = baseDr;
        this.impuestoDr = impuestoDr;
        this.tipoFactorDr = tipoFactorDr;
        this.tasaOCuotaDr = tasaOCuotaDr;
        this.importeDr = importeDr;
    }

    public Integer getIdTrasladoDr() { return idTrasladoDr; }
    public void setIdTrasladoDr(Integer idTrasladoDr) { this.idTrasladoDr = idTrasladoDr; }

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

    public BigDecimal getImporteDr() { return importeDr; }
    public void setImporteDr(BigDecimal importeDr) { this.importeDr = importeDr; }
}
