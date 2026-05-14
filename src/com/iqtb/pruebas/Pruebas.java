/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iqtb.pruebas;

import com.iqtb.DAOs.CfdsDAO;

/**
 *
 * @author Joaquin
 */
public class Pruebas {

    public static void main(String[] args) {
        String pedimentoAntes = "17  47  3905  7002258";
        String pedimentoDespues = "";

        if (pedimentoAntes.length() == 15) {
            pedimentoDespues = pedimentoAntes.substring(0, 2) + "  " + pedimentoAntes.substring(2, 4) + "  " + pedimentoAntes.substring(4, 8) + "  " + pedimentoAntes.substring(8);
        } else {
            if (pedimentoAntes.length() == 21) {
                System.out.println("El pedimento ya tiene 21 caracteres");
                pedimentoDespues = pedimentoAntes;
            } else {
            }
        }

        System.out.println("pedimentoAntes: " + pedimentoAntes);
        System.out.println("pedimentoDespues: " + pedimentoDespues);

        System.exit(0);

        String serie = "NOM";
        Long folio = Long.valueOf("244");
        Integer idSucursal = 3;

        CfdsDAO cfdsDAO = new CfdsDAO();
        String uuid = cfdsDAO.getUUID(serie, folio, idSucursal);
        if (uuid != null) {
            System.out.println("uuid: " + uuid);
        } else {
            System.out.println("uuid nulo");
        }
    }
}
