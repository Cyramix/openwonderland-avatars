/*
 * Interface layer for the java.sql libraries
 */
package imi.sql;
        
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author Paul Viet Truong
 */
public class SQLInterface {

////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Connection Parameters */
    private String              m_DatabaseURL   = null; // jdbc:mysql://host_name:port/dbname
    private String              m_szUserName    = null;
    private String              m_szPassword    = null;
    private String              m_szDriver      = "com.mysql.jdbc.Driver";
    private Connection          m_Connection    = null;
    /** Query Parameters */
    private Statement           m_Statement     = null;
    private ResultSet           m_ResultSet     = null;
    private ArrayList<String[]> m_ResultData    = null;
    private static int          m_iColumns      = 0;
    
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    public boolean Connect(String driver, String database, String loginname, String loginpass) {
        if(driver != null)
            m_szDriver  = driver;
        m_DatabaseURL   = database;
        m_szUserName    = loginname;
        m_szPassword    = loginpass;
        
        try {
            Class.forName(m_szDriver).newInstance();
            m_Connection = DriverManager.getConnection(m_DatabaseURL, m_szUserName, m_szPassword);

            if (!m_Connection.isClosed()) {
                System.out.println("Connection Status: CONNECTED!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }
    
    public boolean ReConnect() {
        try {
            Class.forName(m_szDriver).newInstance();
            m_Connection = DriverManager.getConnection(m_DatabaseURL.toString(), m_szUserName, m_szPassword);

            if (!m_Connection.isClosed()) {
                System.out.println("Connection Status: CONNECTED!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }    

    public boolean Disconnect() {
        try {
            if (m_ResultSet != null) {
                m_ResultSet.close();
            }
            if (m_Statement != null) {
                m_Statement.close();
            }
            if (m_Connection != null) {
                m_Connection.close();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
        return false;
    }

    public ResultSet RetrieveRawSQLData(String query) {
        try {
            if (m_Connection == null || m_Connection.isClosed()) {
                System.out.println("Exception: CONNECTION not found");
                return m_ResultSet;
            }

            m_Statement = m_Connection.createStatement();
            m_ResultSet = m_Statement.executeQuery(query);
            return m_ResultSet;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return m_ResultSet;
    }

    public ArrayList<String[]> Retrieve(String query) {
        return ParseQuery(RetrieveRawSQLData(query));
    }
    
    public int Update(String change) {
        int iRecordsUpdated = 0;

        try {
            if (m_Connection == null || m_Connection.isClosed()) {
                System.out.println("Exception: CONNECTION not found");
                return iRecordsUpdated;
            }

            m_Statement = m_Connection.createStatement();
            iRecordsUpdated = m_Statement.executeUpdate(change);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        return iRecordsUpdated;
    }

    public ArrayList<String[]> ParseQuery(ResultSet r) {
        m_ResultData = new ArrayList<String[]>();
        
        try {
            ResultSetMetaData metaData = r.getMetaData();
            m_iColumns = metaData.getColumnCount();
            
            while (r.next()) {
                String[] data = new String[m_iColumns];
                for(int i = 0; i < m_iColumns; i++) {
                    data[i] = r.getString(i+1);
                }
                m_ResultData.add(data);
            }
            return m_ResultData;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
        
        return m_ResultData;
    }
    
////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    public String getURL() {
        return m_DatabaseURL;
    }

    public String getUserName() {
        return m_szUserName;
    }

    public String getPassword() {
        return m_szPassword;
    }

    public String getDriver() {
        return m_szDriver;
    }

    public Connection getConnection() {
        return m_Connection;
    }

    public Statement getStatement() {
        return m_Statement;
    }

    public ResultSet getResultSet() {
        return m_ResultSet;
    }
    
    public ArrayList<String[]> getResultData() {
        return m_ResultData;
    }
    
    public Integer getNumColumns() {
        return m_iColumns;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setURL(String u) {
        m_DatabaseURL = u;
    }

    public void setUserName(String s) {
        m_szUserName = s;
    }

    public void setPassword(String s) {
        m_szPassword = s;
    }
    
//    public void setLogger(LoggingModel logger) {
//        m_Logger = logger;
//    }

    public boolean setDriver(String s) {
        m_szDriver = s;
        try {
            Class.forName(m_szDriver).newInstance();
            return true;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }

    public boolean setConnection(Connection c) {
        try {
            if (c == null || c.isClosed()) {
                System.out.println("Connection not valid");
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
        
        if (m_Connection != null) {
            try {
                m_Connection.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
            }
        }

        m_Connection = c;
        return true;
    }

    public boolean setConnection(String url, String loginName, String loginPass) {
        if (m_Connection != null) {
            try {
                m_Connection.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
            }
        }

        try {
            m_Connection = DriverManager.getConnection(url, loginName, loginPass);
            
            if (!m_Connection.isClosed()) {
                System.out.println("Connection Status: CONNECTED!");
                return true;
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
        return false;
    }
}
