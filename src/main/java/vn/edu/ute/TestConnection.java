package vn.edu.ute;

import vn.edu.ute.config.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestConnection {
    private static final Logger logger = LogManager.getLogger(TestConnection.class);

    public static void main(String[] args) {
        logger.info("Starting Database Connection Test...");
        System.out.println("--- Starting Database Connection Test ---");
        
        try {
            boolean success = DatabaseConfig.getInstance().testConnection();
            
            if (success) {
                System.out.println("SUCCESS: Connected to PostgreSQL database audiogear_ecommerce successfully!");
            } else {
                System.err.println("FAILED: Could not connect to the database.");
            }
            
            DatabaseConfig.getInstance().shutdown();
            System.exit(success ? 0 : 1);
            
        } catch (Exception e) {
            System.err.println("EXCEPTION during test: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
