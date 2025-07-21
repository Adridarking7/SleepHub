package org.tfc.pruebas.sleephub.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/test", // ✔️ la que usa usted
                "root", // ✔️ sin contraseña
                "");
    }
}
