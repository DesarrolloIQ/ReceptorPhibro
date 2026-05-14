package com.iqtb.POJOs;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Pagos implements java.io.Serializable {

    private Integer idPago;
    private CfdisPagos cfdisPagos;
    private BigDecimal version;
    private Date fechaPago;
    private String formaPago;
    private String monedaPago;
    private BigDecimal tipoCambioPago;
    private BigDecimal montoPago;
    private String numOperacion;
    private String rfcEmisorCtaOrd;
    private String nombreBancoOrdExtranjero;
    private String cuentaOrdenante;
    private String rfcEmisorCtaBeneficiario;
    private String cuentaBeneficiario;
    private BigDecimal importeRetencion;
    private BigDecimal importeIvaTraslado;
    private BigDecimal tasaIvaTraslado;
    private Set<RetencionesP> retencionesP = new HashSet<RetencionesP>(0);
    private Set<TrasladosP> trasladosP = new HashSet<TrasladosP>(0);
    private Set<DocumentosRelacionadosP> documentosRelacionadosP = new HashSet<DocumentosRelacionadosP>(0);

    public Pagos() {}

    public Pagos(Integer idPago, CfdisPagos cfdisPagos, BigDecimal version,
                 Date fechaPago, String formaPago, String monedaPago,
                 BigDecimal tipoCambioPago, BigDecimal montoPago) {
        this.idPago = idPago;
        this.cfdisPagos = cfdisPagos;
        this.version = version;
        this.fechaPago = fechaPago;
        this.formaPago = formaPago;
        this.monedaPago = monedaPago;
        this.tipoCambioPago = tipoCambioPago;
        this.montoPago = montoPago;
    }

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }

    public CfdisPagos getCfdisPagos() { return cfdisPagos; }
    public void setCfdisPagos(CfdisPagos cfdisPagos) { this.cfdisPagos = cfdisPagos; }

    public BigDecimal getVersion() { return version; }
    public void setVersion(BigDecimal version) { this.version = version; }

    public Date getFechaPago() { return fechaPago; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

    public String getMonedaPago() { return monedaPago; }
    public void setMonedaPago(String monedaPago) { this.monedaPago = monedaPago; }

    public BigDecimal getTipoCambioPago() { return tipoCambioPago; }
    public void setTipoCambioPago(BigDecimal tipoCambioPago) { this.tipoCambioPago = tipoCambioPago; }

    public BigDecimal getMontoPago() { return montoPago; }
    public void setMontoPago(BigDecimal montoPago) { this.montoPago = montoPago; }

    public String getNumOperacion() { return numOperacion; }
    public void setNumOperacion(String numOperacion) { this.numOperacion = numOperacion; }

    public String getRfcEmisorCtaOrd() { return rfcEmisorCtaOrd; }
    public void setRfcEmisorCtaOrd(String rfcEmisorCtaOrd) { this.rfcEmisorCtaOrd = rfcEmisorCtaOrd; }

    public String getNombreBancoOrdExtranjero() { return nombreBancoOrdExtranjero; }
    public void setNombreBancoOrdExtranjero(String nombreBancoOrdExtranjero) { this.nombreBancoOrdExtranjero = nombreBancoOrdExtranjero; }

    public String getCuentaOrdenante() { return cuentaOrdenante; }
    public void setCuentaOrdenante(String cuentaOrdenante) { this.cuentaOrdenante = cuentaOrdenante; }

    public String getRfcEmisorCtaBeneficiario() { return rfcEmisorCtaBeneficiario; }
    public void setRfcEmisorCtaBeneficiario(String rfcEmisorCtaBeneficiario) { this.rfcEmisorCtaBeneficiario = rfcEmisorCtaBeneficiario; }

    public String getCuentaBeneficiario() { return cuentaBeneficiario; }
    public void setCuentaBeneficiario(String cuentaBeneficiario) { this.cuentaBeneficiario = cuentaBeneficiario; }

    public BigDecimal getImporteRetencion() { return importeRetencion; }
    public void setImporteRetencion(BigDecimal importeRetencion) { this.importeRetencion = importeRetencion; }

    public BigDecimal getImporteIvaTraslado() { return importeIvaTraslado; }
    public void setImporteIvaTraslado(BigDecimal importeIvaTraslado) { this.importeIvaTraslado = importeIvaTraslado; }

    public BigDecimal getTasaIvaTraslado() { return tasaIvaTraslado; }
    public void setTasaIvaTraslado(BigDecimal tasaIvaTraslado) { this.tasaIvaTraslado = tasaIvaTraslado; }

    public Set<RetencionesP> getRetencionesP() { return retencionesP; }
    public void setRetencionesP(Set<RetencionesP> retencionesP) { this.retencionesP = retencionesP; }

    public Set<TrasladosP> getTrasladosP() { return trasladosP; }
    public void setTrasladosP(Set<TrasladosP> trasladosP) { this.trasladosP = trasladosP; }

    public Set<DocumentosRelacionadosP> getDocumentosRelacionadosP() { return documentosRelacionadosP; }
    public void setDocumentosRelacionadosP(Set<DocumentosRelacionadosP> documentosRelacionadosP) { this.documentosRelacionadosP = documentosRelacionadosP; }
}
