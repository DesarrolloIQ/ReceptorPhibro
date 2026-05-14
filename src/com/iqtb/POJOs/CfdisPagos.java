package com.iqtb.POJOs;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class CfdisPagos implements java.io.Serializable {

    private Integer idCfdiPago;
    private Cfds cfds;
    private BigDecimal totalRetencionesIva;
    private BigDecimal totalRetencionesIsr;
    private BigDecimal totalRetencionesIeps;
    private BigDecimal totalTrasladosBaseIva16;
    private BigDecimal totalTrasladosImpuestoIva16;
    private BigDecimal totalTrasladosBaseIva8;
    private BigDecimal totalTrasladosImpuestoIva8;
    private BigDecimal totalTrasladosBaseIva0;
    private BigDecimal totalTrasladosImpuestoIvaExento;
    private BigDecimal montoTotalPagos;
    private Set<Pagos> pagos = new HashSet<Pagos>(0);

    public CfdisPagos() {}

    public CfdisPagos(Integer idCfdiPago, Cfds cfds) {
        this.idCfdiPago = idCfdiPago;
        this.cfds = cfds;
    }

    public Integer getIdCfdiPago() { return idCfdiPago; }
    public void setIdCfdiPago(Integer idCfdiPago) { this.idCfdiPago = idCfdiPago; }

    public Cfds getCfds() { return cfds; }
    public void setCfds(Cfds cfds) { this.cfds = cfds; }

    public BigDecimal getTotalRetencionesIva() { return totalRetencionesIva; }
    public void setTotalRetencionesIva(BigDecimal totalRetencionesIva) { this.totalRetencionesIva = totalRetencionesIva; }

    public BigDecimal getTotalRetencionesIsr() { return totalRetencionesIsr; }
    public void setTotalRetencionesIsr(BigDecimal totalRetencionesIsr) { this.totalRetencionesIsr = totalRetencionesIsr; }

    public BigDecimal getTotalRetencionesIeps() { return totalRetencionesIeps; }
    public void setTotalRetencionesIeps(BigDecimal totalRetencionesIeps) { this.totalRetencionesIeps = totalRetencionesIeps; }

    public BigDecimal getTotalTrasladosBaseIva16() { return totalTrasladosBaseIva16; }
    public void setTotalTrasladosBaseIva16(BigDecimal totalTrasladosBaseIva16) { this.totalTrasladosBaseIva16 = totalTrasladosBaseIva16; }

    public BigDecimal getTotalTrasladosImpuestoIva16() { return totalTrasladosImpuestoIva16; }
    public void setTotalTrasladosImpuestoIva16(BigDecimal totalTrasladosImpuestoIva16) { this.totalTrasladosImpuestoIva16 = totalTrasladosImpuestoIva16; }

    public BigDecimal getTotalTrasladosBaseIva8() { return totalTrasladosBaseIva8; }
    public void setTotalTrasladosBaseIva8(BigDecimal totalTrasladosBaseIva8) { this.totalTrasladosBaseIva8 = totalTrasladosBaseIva8; }

    public BigDecimal getTotalTrasladosImpuestoIva8() { return totalTrasladosImpuestoIva8; }
    public void setTotalTrasladosImpuestoIva8(BigDecimal totalTrasladosImpuestoIva8) { this.totalTrasladosImpuestoIva8 = totalTrasladosImpuestoIva8; }

    public BigDecimal getTotalTrasladosBaseIva0() { return totalTrasladosBaseIva0; }
    public void setTotalTrasladosBaseIva0(BigDecimal totalTrasladosBaseIva0) { this.totalTrasladosBaseIva0 = totalTrasladosBaseIva0; }

    public BigDecimal getTotalTrasladosImpuestoIvaExento() { return totalTrasladosImpuestoIvaExento; }
    public void setTotalTrasladosImpuestoIvaExento(BigDecimal totalTrasladosImpuestoIvaExento) { this.totalTrasladosImpuestoIvaExento = totalTrasladosImpuestoIvaExento; }

    public BigDecimal getMontoTotalPagos() { return montoTotalPagos; }
    public void setMontoTotalPagos(BigDecimal montoTotalPagos) { this.montoTotalPagos = montoTotalPagos; }

    public Set<Pagos> getPagos() { return pagos; }
    public void setPagos(Set<Pagos> pagos) { this.pagos = pagos; }
}
