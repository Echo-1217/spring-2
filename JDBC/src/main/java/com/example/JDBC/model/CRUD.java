package com.example.JDBC.model;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Slf4j
public class CRUD {


//    private static final Logger log = LogManager.getLogger(CRUD.class);

    public Connection getConnection() {

        Connection dbConnection = null;
        try {

            //STEP 2: Register JDBC driver
            String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
            Class.forName(JDBC_DRIVER);

            log.info("Connecting to a selected Database...");

            //STEP 3: Open a connection
            //  Database credentials
            String DB_URL = "jdbc:mariadb://localhost/mgn";
            dbConnection = DriverManager.getConnection(DB_URL, "root", "echo");

            log.info("Connected Database successfully...");

        } catch (SQLException | ClassNotFoundException se) {
            se.printStackTrace();
        }
        return dbConnection;
    }


    //STEP 4: Execute a query
    public String getResponse(String id) throws SQLException {
        StringBuilder response = new StringBuilder("result :\n");

        // connect db
        try (Connection dbConnection = getConnection()) {

            // 驗證 id 是否存在
            // PreparedStatement 取代 Statement
            PreparedStatement pStmt = dbConnection.prepareStatement("SELECT * FROM mgni WHERE id= ?");
            pStmt.setString(1, id);

            PreparedStatement pStmtCash = dbConnection.prepareStatement("SELECT * FROM cashi WHERE mgni_id=?");
            pStmtCash.setString(1, id);

            // 設定 ResultSet
            ResultSet exist = pStmt.executeQuery();

            if (!exist.next()) {
                dbConnection.close();
                log.info(id + " doesn't exist" + "Database connect is closed : " + dbConnection.isClosed() + "\n");
                return id + " doesn't exist\n"
                        + "Database connect is closed : " + dbConnection.isClosed() + "\n";
            }

            // 撈出資料
            ResultSet mgni = pStmt.executeQuery();
            ResultSet cash = pStmtCash.executeQuery();

            // 設定 MGNI 格式
            StringBuilder transfer = new StringBuilder();
            while (mgni.next()) {
                transfer.append("id:\t").append(mgni.getString("id"))
                        .append("\n").append("time:\t").append(mgni.getString("time"))
                        .append("\n").append("cm_no:\t").append(mgni.getString("cm_no"))
                        .append("\n").append("kac_type:\t").append(mgni.getString("kac_type"))
                        .append("\n").append("bank_no:\t").append(mgni.getString("bank_no"))
                        .append("\n").append("ccy:\t").append(mgni.getString("ccy"))
                        .append("\n").append("pv_type:\t").append(mgni.getString("pv_type"))
                        .append("\n").append("bicacc_no:\t").append(mgni.getString("bicacc_no"))
                        .append("\n").append("amt:\t").append(mgni.getBigDecimal("amt").setScale(2, RoundingMode.HALF_UP))
                        .append("\n");
            }

            // 設定 detail 格式
            StringBuilder acc = new StringBuilder("accountList:\n");
            while (cash.next()) {
                //Retrieve by column name
                acc.append("\taccNo: ")
                        .append(cash.getString("acc_no"))
                        .append("\t").append("\tccy: ")
                        .append(cash.getString("ccy"))
                        .append("\t").append("\tamt: ")
                        .append(cash.getBigDecimal("amt").setScale(2, RoundingMode.HALF_UP)).append("\n");
            }


            // close connection
            dbConnection.close();

            // 整合 response
            response.append(transfer)
                    .append(acc).append("\n")
                    .append("read data over\n")
                    .append("Database connect is closed : ")
                    .append(dbConnection.isClosed())
                    .append("\n");

            log.info(String.valueOf(response));
            return response.toString();

        } catch (Exception se) {
            // 關閉連線
            se.printStackTrace();
            return se.getMessage();
        }
    }

    @Transactional
    public synchronized String transferInsertAndUpdate(JSONObject request) throws SQLException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd" + "HHmmssSSS");

        // connect db
        try (Connection dbConnection = getConnection()) {

            String id = "MGI" + sdFormat.format(Calendar.getInstance().getTime());
            if (!request.getString("id").isEmpty()) {
                id = request.getString("id");
            }


            // set cashi
            setCashiData(id, request, dbConnection);

            // inset & update sql mgni
            PreparedStatement mgniStmt = dbConnection.prepareStatement(
                    "INSERT INTO `mgni` "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                            + "ON DUPLICATE KEY UPDATE id = ?" // 判定 id 是否存在 ; 若存在，則更新。 ; 若不存在，則新增

            );
            mgniStmt.setString(1, id);
            mgniStmt.setString(2, dateTimeFormatter.format(LocalDateTime.now()));
            mgniStmt.setString(3, "1");
            mgniStmt.setString(4, "JDBC");
            mgniStmt.setString(5, request.getString("kac_type"));
            mgniStmt.setString(6, request.getString("bank_no"));
            mgniStmt.setString(7, request.getString("ccy"));
            mgniStmt.setString(8, request.getString("pv_type"));
            mgniStmt.setString(9, request.getString("bicacc_no"));
            mgniStmt.setString(10, request.getString("i_type"));
            mgniStmt.setString(11, "test jdbc");
            mgniStmt.setBigDecimal(12, calAmt(id, dbConnection));
            mgniStmt.setString(13, "Echo");
            mgniStmt.setString(14, "0987654321");
            mgniStmt.setString(15, "0");
            mgniStmt.setString(16, dateTimeFormatter.format(LocalDateTime.now()));
            mgniStmt.setString(17, id);
            mgniStmt.executeUpdate();


            // close connection
            dbConnection.close();
            log.info("insert & update over " + "connection is closed : " + dbConnection.isClosed() + "\n");
            return "insert & update  success !\n" + getResponse(id);

        } catch (JSONException | SQLException e) {
            e.printStackTrace();
            return "insert & update  fail\n message:" + e.getMessage();
        }
    }

    @Transactional
    public synchronized String deleteTransfer(String id) {
        try {

            // connect db
            Connection dbConnection = getConnection();
            PreparedStatement mgniStmtExist = dbConnection.prepareStatement("SELECT * FROM mgni WHERE id= ?");
            mgniStmtExist.setString(1, id);


            ResultSet exist = mgniStmtExist.executeQuery();
            if (!exist.next()) {
                log.info(id + " doesn't exist");
                dbConnection.close();
                return "Delete fail\n  message :" + id + " doesn't exist\n"
                        + "Database connect is closed : " + dbConnection.isClosed() + "\n";
            }

            PreparedStatement cashiStmtDelete = dbConnection.prepareStatement("DELETE FROM  cashi  WHERE  mgni_id=?");
            cashiStmtDelete.setString(1, id);

            PreparedStatement mgniStmtDelete = dbConnection.prepareStatement("DELETE FROM  mgni  WHERE  id=?");
            mgniStmtDelete.setString(1, id);

            cashiStmtDelete.executeUpdate();
            mgniStmtDelete.executeUpdate();

            // close connection
            dbConnection.close();
            log.info("Delete success\n"
                    + "id: " + id + "\n"
                    + "Database connect is closed : " + dbConnection.isClosed() + "\n");

            return "Delete success\n"
                    + "id: " + id + "\n"
                    + "Database connect is closed : " + dbConnection.isClosed() + "\n";

        } catch (JSONException | SQLException e) {
            e.printStackTrace();
            return "Delete fail\n  message :" + e.getMessage();
        }
    }


    public void setCashiData(String id, JSONObject request, Connection connection) {
        request.getJSONArray("accounts").forEach(
                obj -> {
                    try {
                        JSONObject acc = new JSONObject(obj.toString());
                        PreparedStatement pStmt = connection.prepareStatement(
                                "  INSERT INTO cashi VALUES(?,?,?,?)"
                                        + "ON DUPLICATE KEY UPDATE "
                                        + "mgni_id = ?,"
                                        + "acc_no = ?,"
                                        + "ccy = ?,"
                                        + "amt = ?;"
                        );
                        pStmt.setString(1, id);
                        pStmt.setString(2, acc.getString("acc_no"));
                        pStmt.setString(3, request.getString("ccy"));
                        pStmt.setBigDecimal(4, acc.getBigDecimal("amt"));
                        pStmt.setString(5, id);
                        pStmt.setString(6, acc.getString("acc_no"));
                        pStmt.setString(7, request.getString("ccy"));
                        pStmt.setBigDecimal(8, acc.getBigDecimal("amt"));

                        pStmt.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }


    private BigDecimal calAmt(String id, Connection dbConnection) {
        BigDecimal price = new BigDecimal(BigInteger.ZERO);
        try {


            PreparedStatement pStmt = dbConnection.prepareStatement("SELECT SUM(amt) FROM cashi WHERE mgni_id=?");
            pStmt.setString(1, id);
            ResultSet cashList = pStmt.executeQuery();
            while (cashList.next()) {
                price = price.add(cashList.getBigDecimal("sum(amt)"));
            }
            return price.setScale(2, RoundingMode.HALF_UP);
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }


}
