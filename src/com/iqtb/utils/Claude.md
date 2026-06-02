# ReceptorPhibro — Guía de referencia rápida

## Propósito del sistema

Procesador automático de **CFDIs recibidos** (Comprobantes Fiscales Digitales por Internet) conforme al estándar SAT mexicano. Corre en bucle infinito cada 5 segundos y ejecuta tres sprints en secuencia:

| Sprint | Tipo CFDI | Tabla(s) principal | Trigger |
|--------|-----------|-------------------|---------|
| 2 | **P** (Pago) | `CFDIS_PAGOS`, `PAGOS`, `DOCUMENTOS_RELACIONADOS_P` | `FECHA_PEDIMENTO IS NULL` |
| 4 | **E** (Egreso/Nota de crédito) | `CFDIS_RELACIONADOS_PADRE`, `CFDIS_RELACIONADOS_HIJO` | `FECHA_PEDIMENTO IS NULL` |
| 7 | **I** (Ingreso/Factura) | `CFDS.FECHA_PEDIMENTO` | `FECHA_PEDIMENTO IN (NULL, 'PENDIENTE_REVISION')` |

---

## Estructura de paquetes

```
src/
├── receptorphibro/
│   └── ReceptorPhibro.java          ← punto de entrada, bucle principal
├── com/iqtb/
│   ├── utils/
│   │   └── ProcesadorCfdisService.java  ← orquestador de los 3 sprints
│   ├── recursos/
│   │   ├── ProcesarLineasPagos.java     ← parser de archivos .iqtb/.txt
│   │   ├── ProcesarXml.java             ← extracción de impuestos desde XML
│   │   ├── DetectarCP.java              ← detección de código postal
│   │   └── DetectarCodif.java           ← detección de encoding de archivo
│   ├── DAOs/                            ← acceso a datos vía Hibernate
│   ├── POJOs/                           ← entidades + mapeos .hbm.xml
│   └── Session/
│       └── HibernateUtil.java           ← SessionFactory singleton
sql/
└── Script_Crear_Tablas_Pagos.sql        ← DDL de tablas de pagos
configReceptorPhilbro/
└── log4j.properties
```

---

## Modelo de datos de pagos (tablas nuevas)

```
CFDS (existente)
 └── CFDIS_PAGOS          (1:1 con CFDS donde TIPO_CFD='P')
      └── PAGOS            (1:N — cada nodo pago20:Pago del XML)
           └── DOCUMENTOS_RELACIONADOS_P  (1:N — cada DoctoRelacionado)
                ├── RETENCIONES_DR
                └── TRASLADOS_DR
```

Tablas de egresos:
```
CFDS (TIPO_CFD='E')
 └── CFDIS_RELACIONADOS_PADRE
      └── CFDIS_RELACIONADOS_HIJO
```

---

## Campo FECHA_PEDIMENTO (CFDS)

Este campo VARCHAR se reutiliza como campo de estado del procesamiento. **No es una fecha real.**

| Valor | CFDI tipo | Significado |
|-------|-----------|-------------|
| `NULL` | P, E, I | Pendiente de procesar (Sprint 2 o 4) |
| `NO_APLICA` | P, E, I | Procesado sin acción / versión < 4.0 / tipo incorrecto |
| `PENDIENTE_REVISION` | I | Referenciado por ≥1 pago; pendiente de Sprint 7 |
| `PAGADO_PPD` | I | Saldo insoluto = 0, método PPD |
| `PAGO_PARCIAL_PPD` | I | Saldo > 0, con pagos parciales, método PPD |
| `NO_PAGADO` | I | Sin pagos vigentes, método PPD |
| `PAGADO_PUE` | I | Sin pagos (correcto para PUE) |
| `ERROR_PUE` | I | Tiene pagos siendo método PUE (anómalo) |
| `PAGO_DUPLICADO` | P | El CFDI de pago cubre una factura ya liquidada por otro pago vigente |

---

## Flujo de procesamiento (Sprint 2 detallado)

```
procesarCfdisDePago()
  └── procesarUnCfdiDePago(idCfdPago)
        1. actualizarFechaPedimento(idCfdPago, "NO_APLICA")  ← anti-reproceso
        2. parsear XML SAT
        3. validar versión ≥ 4.0
        4. llenarTablaCfdisPagos()        → INSERT CFDIS_PAGOS
        5. llenarTablaPagos()
              └── por cada pago20:Pago → INSERT PAGOS
                    └── buscarDocumentosRelacionados()
                          └── por cada DoctoRelacionado:
                                - buscar UUID en CFDS
                                - INSERT DOCUMENTOS_RELACIONADOS_P
                                - si encontrado: marcar CFDI-I como PENDIENTE_REVISION
```

---

## DAOs relevantes

| DAO | Métodos clave |
|-----|--------------|
| `CfdsDAO` | `getIdCfdByUuid`, `actualizarFechaPedimento`, `getFechaPedimento`, `getIdCfdsPendientesPorTipo` |
| `PagosDAO` | `guardarCfdisPagos`, `guardarPago` |
| `DocumentosRelacionadosPDAO` | `guardar`, `getPagosDeIngreso`, `existePagoCompletoVigente` |
| `CfdisRelacionadosDAO` | `guardarPadre`, `guardarHijo` |
| `XmlsDAO` | `getXmlSat` |

---

## Configuración

- **BD**: MySQL en `localhost:3306/xsa`, usuario `ReceptoPhibro`
- **Hibernate**: `src/hibernate.cfg.xml`
- **Logs**: Log4j → `configReceptorPhilbro/log4j.properties`
- **Build**: Ant (`build.xml`), proyecto NetBeans
- **BATCH_SIZE**: 200 registros por lote en `ProcesadorCfdisService`

---

## Namespaces CFDI usados en XPath

```java
cfdi  → http://www.sat.gob.mx/cfd/4  (o /cfd/3 para versiones anteriores)
pago20 → http://www.sat.gob.mx/Pagos20
pago10 → http://www.sat.gob.mx/Pagos
```

---

## Convenciones importantes

- Todos los montos usan `BigDecimal`; nunca `double`/`float`.
- El campo `ESTADO_RELACION` en `DOCUMENTOS_RELACIONADOS_P` y `CFDIS_RELACIONADOS_HIJO` solo acepta: `VALIDO`, `ERROR`, `DESCONOCIDO`.
- Un CFDI-P se marca `NO_APLICA` al inicio del procesamiento para evitar reproceso en caso de fallo a mitad.
- Solo se procesan CFDIs versión ≥ 4.0.
- Los pagos VIGENTES se determinan por `estadoFiscal` del CFDS padre (ver `obtenerEstatusGeneral()`) o por `estado != 'CANCELADO'`.