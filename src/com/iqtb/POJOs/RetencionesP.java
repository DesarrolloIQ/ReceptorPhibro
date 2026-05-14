package com.iqtb.POJOs;

import java.math.BigDecimal;

public class RetencionesP implements java.io.Serializable {

    private Integer idRetencionP;
    private Pagos pagos;
    private BigDecimal impuestoP;
    private BigDecimal importeP;

    public RetencionesP() {}

    public RetencionesP(Integer idRetencionP, Pagos pagos,
                        BigDecimal impuestoP, BigDecimal importeP) {
        this.idRetencionP = idRetencionP;
        this.pagos = pagos;
        this.impuestoP = impuestoP;
        this.importeP = importeP;
    }

    public Integer getIdRetencionP() { return idRetencionP; }
    public void setIdRetencionP(Integer idRetencionP) { this.idRetencionP = idRetencionP; }

    public Pagos getPagos() { return pagos; }
    public void setPagos(Pagos pagos) { this.pagos = pagos; }

    public BigDecimal getImpuestoP() { return impuestoP; }
    public void setImpuestoP(BigDecimal impuestoP) { this.impuestoP = impuestoP; }

    public BigDecimal getImporteP() { return importeP; }
    public void setImporteP(BigDecimal importeP) { this.importeP = importeP; }
}
