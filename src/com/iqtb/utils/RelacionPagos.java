package com.iqtb.utils;

import com.iqtb.Session.ConexionXsa;
import com.iqtb.relacionador.RelacionadorFacturasPagos;
import com.iqtb.relacionador.proceso.ProcesadorCfdis;

/**
 * Acceso unico a la libreria RelacionadorFacturasPagos (relacion factura-pago en xsa).
 * Reemplaza al antiguo ProcesadorCfdisService y a sus DAO/POJO de pagos.
 */
public final class RelacionPagos {

    private static ProcesadorCfdis procesador;
    private static RelacionadorFacturasPagos relacionador;

    private RelacionPagos() {
    }

    /** Sprints 2 (pagos), 4 (egresos) y 7 (ingresos): llena las tablas de pagos y el estado de pago de las facturas. */
    public static synchronized ProcesadorCfdis procesador() {
        if (procesador == null) {
            procesador = new ProcesadorCfdis(ConexionXsa.get(), ConexionXsa.ESQUEMA);
        }
        return procesador;
    }

    /** Consultas de la relacion (pagos de una factura, egresos relacionados) y reparaciones. */
    public static synchronized RelacionadorFacturasPagos relacionador() {
        if (relacionador == null) {
            relacionador = new RelacionadorFacturasPagos(ConexionXsa.get(), ConexionXsa.ESQUEMA);
        }
        return relacionador;
    }
}
