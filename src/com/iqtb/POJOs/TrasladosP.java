package com.iqtb.POJOs;

import java.math.BigDecimal;

public class TrasladosP implements java.io.Serializable {

    private Integer idTrasladoP;
    private Pagos pagos;
    private BigDecimal baseP;
    private String impuestoP;
    private String tipoFactorP;
    private BigDecimal tasaOCuotaP;
    private BigDecimal importeP;

    public TrasladosP() {}

    public TrasladosP(Integer idTrasladoP, Pagos pagos, BigDecimal baseP,
                      String tipoFactorP, BigDecimal tasaOCuotaP, BigDecimal importeP) {
        this.idTrasladoP = idTrasladoP;
        this.pagos = pagos;
        this.baseP = baseP;
        this.tipoFactorP = tipoFactorP;
        this.tasaOCuotaP = tasaOCuotaP;
        this.importeP = importeP;
    }

    public Integer getIdTrasladoP() { return idTrasladoP; }
    public void setIdTrasladoP(Integer idTrasladoP) { this.idTrasladoP = idTrasladoP; }

    public Pagos getPagos() { return pagos; }
    public void setPagos(Pagos pagos) { this.pagos = pagos; }

    public BigDecimal getBaseP() { return baseP; }
    public void setBaseP(BigDecimal baseP) { this.baseP = baseP; }

    public String getImpuestoP() { return impuestoP; }
    public void setImpuestoP(String impuestoP) { this.impuestoP = impuestoP; }

    public String getTipoFactorP() { return tipoFactorP; }
    public void setTipoFactorP(String tipoFactorP) { this.tipoFactorP = tipoFactorP; }

    public BigDecimal getTasaOCuotaP() { return tasaOCuotaP; }
    public void setTasaOCuotaP(BigDecimal tasaOCuotaP) { this.tasaOCuotaP = tasaOCuotaP; }

    public BigDecimal getImporteP() { return importeP; }
    public void setImporteP(BigDecimal importeP) { this.importeP = importeP; }
}
