CREATE TABLE `CFDIS_PAGOS` (
  `IDCFDI_PAGO`                   int             NOT NULL,
  `CFDS_idCFD`                    int unsigned    NOT NULL,
  `TOTAL_RETENCIONES_IVA`         decimal(16,2)   DEFAULT NULL,
  `TOTAL_RETENCIONES_ISR`         decimal(16,2)   DEFAULT NULL,
  `TOTAL_RETENCIONES_IEPS`        decimal(16,2)   DEFAULT NULL,
  `TOTAL_TRASLADOS_BASE_IVA16`    decimal(16,2)   DEFAULT NULL,
  `TOTAL_TRASLADOS_IMPUESTO_IVA16` decimal(16,2)  DEFAULT NULL,
  `TOTAL_TRASLADOS_BASE_IVA8`     decimal(16,2)   DEFAULT NULL,
  `TOTAL_TRASLADOS_IMPUESTO_IVA8` decimal(16,2)   DEFAULT NULL,
  `TOTAL_TRASLADOS_BASE_IVA0`     decimal(16,2)   DEFAULT NULL,
  `TOTAL_TRASLADOS_IMPUESTO_IVAEXENTO` decimal(16,2) DEFAULT NULL,
  `MONTO_TOTAL_PAGOS`             decimal(16,2)   DEFAULT NULL,
  PRIMARY KEY (`IDCFDI_PAGO`),
  UNIQUE KEY `CFDS_idCFD_UNIQUE` (`CFDS_idCFD`),
  KEY `fk_CFDIS_PAGOS_CFDS_idx` (`CFDS_idCFD`),
  CONSTRAINT `fk_CFDIS_PAGOS_CFDS`
    FOREIGN KEY (`CFDS_idCFD`) REFERENCES `CFDS` (`idCFD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
 
CREATE TABLE `PAGOS` (
  `IDPAGO`                    int             NOT NULL AUTO_INCREMENT,
  `CFDIS_PAGOS_IDCFDI_PAGO`   int             NOT NULL,
  `VERSION`                   decimal(1,0)    NOT NULL,
  `FECHA_PAGO`                datetime        NOT NULL,
  `FORMA_PAGO`                enum('01','02','03','04','05','06','08','12','13',
                                   '14','15','17','23','24','25','26','27','28',
                                   '29','30','31','99')
                              COLLATE utf8mb4_general_ci NOT NULL,
  `MONEDA_PAGO`               varchar(4)      COLLATE utf8mb4_general_ci NOT NULL,
  `TIPO_CAMBIO_PAGO`          decimal(16,6)   DEFAULT NULL,
  `MONTO_PAGO`                decimal(16,2)   DEFAULT NULL,
  `NUM_OPERACION`             varchar(45)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `RFC_EMISOR_CTA_ORD`        varchar(13)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `NOMBRE_BANCO_ORD_EXTRANJERO` varchar(300)  COLLATE utf8mb4_general_ci DEFAULT NULL,
  `CUENTA_ORDENANTE`          varchar(50)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `RFC_EMISOR_CTA_BENEFICIARIO` varchar(13)   COLLATE utf8mb4_general_ci DEFAULT NULL,
  `CUENTA_BENEFICIARIO`       varchar(50)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `IMPORTE_RETENCION`         decimal(16,2)   DEFAULT NULL,
  `IMPORTE_IVA_TRASLADO_P`    decimal(16,2)   DEFAULT NULL,
  `TASA_IVA_TRASLADO_P`       decimal(10,6)   DEFAULT NULL,
  PRIMARY KEY (`IDPAGO`),
  KEY `fk_PAGOS_CFDIS_PAGOS1_idx` (`CFDIS_PAGOS_IDCFDI_PAGO`),
  CONSTRAINT `fk_PAGOS_CFDIS_PAGOS1`
    FOREIGN KEY (`CFDIS_PAGOS_IDCFDI_PAGO`) REFERENCES `CFDIS_PAGOS` (`IDCFDI_PAGO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
 
CREATE TABLE `DOCUMENTOS_RELACIONADOS_P` (
  `ID_DOCUMENTO_RELACIONADO_P`  int             NOT NULL AUTO_INCREMENT,
  `PAGOS_IDPAGO`                int             NOT NULL,
  `CFDS_idCFD`                  int unsigned    DEFAULT NULL,
  `IDDOCUMENTO`                 varchar(36)     COLLATE utf8mb4_general_ci NOT NULL,
  `SERIE`                       varchar(25)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `FOLIO`                       varchar(40)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `MONEDA_DR`                   varchar(45)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `EQUIVALENCIA_DR`             decimal(16,6)   DEFAULT NULL,
  `NUM_PARCIALIDAD`             int             NOT NULL,
  `IMPORTE_SALDO_ANTERIOR`      decimal(16,2)   DEFAULT NULL,
  `IMPORTE_PAGADO`              decimal(16,2)   DEFAULT NULL,
  `IMPORTE_SALDO_INSOLUTO`      decimal(16,2)   DEFAULT NULL,
  `OBJETO_IMPUESTOS_DR`         varchar(10)     COLLATE utf8mb4_general_ci NOT NULL,
  `ESTADO_RELACION`             enum('VALIDO','ERROR','DESCONOCIDO')
                                COLLATE utf8mb4_general_ci NOT NULL,
  `DESCRIPCION_ERROR`           text            COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`ID_DOCUMENTO_RELACIONADO_P`),
  KEY `fk_DOCUMENTOS_RELACIONADOS_P_PAGOS1_idx` (`PAGOS_IDPAGO`),
  CONSTRAINT `fk_DOCUMENTOS_RELACIONADOS_P_PAGOS1`
    FOREIGN KEY (`PAGOS_IDPAGO`) REFERENCES `PAGOS` (`IDPAGO`),
  CONSTRAINT `fk_DOCUMENTOS_RELACIONADOS_P_CFDS1`
    FOREIGN KEY (`CFDS_idCFD`) REFERENCES `CFDS` (`idCFD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
 
CREATE TABLE `RETENCIONES_DR` (
  `IDRETENCION_DR`              int             NOT NULL AUTO_INCREMENT,
  `DOCUMENTOS_RELACIONADOS_P_ID_DOCUMENTO_RELACIONADO_P` int NOT NULL,
  `BASE_DR`                     decimal(16,2)   DEFAULT NULL,
  `IMPUESTO_DR`                 varchar(10)     COLLATE utf8mb4_general_ci NOT NULL,
  `TIPO_FACTOR_DR`              enum('TASA','CUOTA')
                                COLLATE utf8mb4_general_ci NOT NULL,
  `TASA_O_CUOTA_DR`             decimal(10,6)   DEFAULT NULL,
  `IMPORTE_P`                   decimal(16,2)   DEFAULT NULL,
  PRIMARY KEY (`IDRETENCION_DR`),
  KEY `fk_RETENCIONES_DR_DOCUMENTOS_RELACIONADOS_P1_idx`
    (`DOCUMENTOS_RELACIONADOS_P_ID_DOCUMENTO_RELACIONADO_P`),
  CONSTRAINT `fk_RETENCIONES_DR_DOCUMENTOS_RELACIONADOS_P1`
    FOREIGN KEY (`DOCUMENTOS_RELACIONADOS_P_ID_DOCUMENTO_RELACIONADO_P`)
    REFERENCES `DOCUMENTOS_RELACIONADOS_P` (`ID_DOCUMENTO_RELACIONADO_P`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
CREATE TABLE `TRASLADOS_DR` (
  `IDTRASLADO_DR`               int             NOT NULL AUTO_INCREMENT,
  `DOCUMENTOS_RELACIONADOS_P_ID_DOCUMENTO_RELACIONADO_P` int NOT NULL,
  `BASE_DR`                     decimal(16,2)   DEFAULT NULL,
  `IMPUESTO_DR`                 varchar(10)     COLLATE utf8mb4_general_ci NOT NULL,
  `TIPO_FACTOR_DR`              enum('TASA','CUOTA')
                                COLLATE utf8mb4_general_ci NOT NULL,
  `TASA_O_CUOTA_DR`             decimal(10,6)   DEFAULT NULL,
  `IMPORTE_DR`                  decimal(16,2)   DEFAULT NULL,
  PRIMARY KEY (`IDTRASLADO_DR`),
  KEY `fk_TRASLADOS_DR_DOCUMENTOS_RELACIONADOS_P1_idx`
    (`DOCUMENTOS_RELACIONADOS_P_ID_DOCUMENTO_RELACIONADO_P`),
  CONSTRAINT `fk_TRASLADOS_DR_DOCUMENTOS_RELACIONADOS_P1`
    FOREIGN KEY (`DOCUMENTOS_RELACIONADOS_P_ID_DOCUMENTO_RELACIONADO_P`)
    REFERENCES `DOCUMENTOS_RELACIONADOS_P` (`ID_DOCUMENTO_RELACIONADO_P`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
CREATE TABLE `RETENCIONES_P` (
  `IDRETENCION_P`   int           NOT NULL AUTO_INCREMENT,
  `PAGOS_IDPAGO`    int           NOT NULL,
  `IMPUESTO_P`      decimal(16,2) DEFAULT NULL,
  `IMPORTE_P`       decimal(16,2) DEFAULT NULL,
  PRIMARY KEY (`IDRETENCION_P`),
  KEY `fk_RETENCIONES_P_PAGOS1_idx` (`PAGOS_IDPAGO`),
  CONSTRAINT `fk_RETENCIONES_P_PAGOS1`
    FOREIGN KEY (`PAGOS_IDPAGO`) REFERENCES `PAGOS` (`IDPAGO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
 
CREATE TABLE `TRASLADOS_P` (
  `IDTRASLADO_P`    int           NOT NULL AUTO_INCREMENT,
  `PAGOS_IDPAGO`    int           NOT NULL,
  `BASE_P`          decimal(16,2) DEFAULT NULL,
  `IMPUESTO_P`      varchar(10)   COLLATE utf8mb4_general_ci DEFAULT NULL,
  `TIPO_FACTOR_P`   enum('TASA','CUOTA')
                    COLLATE utf8mb4_general_ci NOT NULL,
  `TASA_O_CUOTA_P`  decimal(10,6) DEFAULT NULL,
  `IMPORTE_P`       decimal(16,2) DEFAULT NULL,
  PRIMARY KEY (`IDTRASLADO_P`),
  KEY `fk_TRASLADOS_P_PAGOS1_idx` (`PAGOS_IDPAGO`),
  CONSTRAINT `fk_TRASLADOS_P_PAGOS1`
    FOREIGN KEY (`PAGOS_IDPAGO`) REFERENCES `PAGOS` (`IDPAGO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
 
CREATE TABLE `CFDIS_RELACIONADOS_PADRE` (
  `IDCFD_RELACIONADO_PADRE` int             NOT NULL AUTO_INCREMENT,
  `CFDS_idCFD`              int unsigned    NOT NULL,
  `TIPO_CFD_PADRE`          enum('I','T','E','N','P')
                            COLLATE utf8mb4_general_ci NOT NULL,
  `TIPO_RELACION`           enum('01','02','03','04','05','06','07','08','09')
                            COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`IDCFD_RELACIONADO_PADRE`),
  KEY `fk_CFDIS_RELACIONADOS_PADRE_CFDS1_idx` (`CFDS_idCFD`),
  CONSTRAINT `fk_CFDIS_RELACIONADOS_PADRE_CFDS1`
    FOREIGN KEY (`CFDS_idCFD`) REFERENCES `CFDS` (`idCFD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
 
 
CREATE TABLE `CFDIS_RELACIONADOS_HIJO` (
  `IDCFD_RELACIONADO_HIJO`                          int             NOT NULL AUTO_INCREMENT,
  `CFDIS_RELACIONADOS_PADRE_IDCFD_RELACIONADO_PADRE` int            NOT NULL,
  `UUID`                                            varchar(36)     COLLATE utf8mb4_general_ci DEFAULT NULL,
  `CFDS_idCFD`                                      int unsigned    DEFAULT NULL,
  `TIPO_CFD_HIJO`                                   enum('I','T','E','N','P')
                                                    COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ESTADO_RELACION`                                 enum('VALIDO','ERROR','DESCONOCIDO')
                                                    COLLATE utf8mb4_general_ci DEFAULT NULL,
  `DESCRIPCION_ERROR`                               varchar(200)
                                                    CHARACTER SET utf8mb4
                                                    COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`IDCFD_RELACIONADO_HIJO`),
  KEY `fk_CFDIS_RELACIONADOS_HIJO_CFDIS_RELACIONADOS_PADRE1_idx`
    (`CFDIS_RELACIONADOS_PADRE_IDCFD_RELACIONADO_PADRE`),
  KEY `fk_CFDIS_RELACIONADOS_HIJO_CFDS1_idx` (`CFDS_idCFD`),
  CONSTRAINT `fk_CFDIS_RELACIONADOS_HIJO_CFDIS_RELACIONADOS_PADRE1`
    FOREIGN KEY (`CFDIS_RELACIONADOS_PADRE_IDCFD_RELACIONADO_PADRE`)
    REFERENCES `CFDIS_RELACIONADOS_PADRE` (`IDCFD_RELACIONADO_PADRE`),
  CONSTRAINT `fk_CFDIS_RELACIONADOS_HIJO_CFDS1`
    FOREIGN KEY (`CFDS_idCFD`) REFERENCES `CFDS` (`idCFD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;