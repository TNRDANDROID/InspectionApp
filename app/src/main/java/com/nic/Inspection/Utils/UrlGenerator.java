package com.nic.Inspection.Utils;


/**
 * Created by Achanthi Sundar  on 21/03/16.
 */
public class UrlGenerator {

    public static String getLoginUrl() {
        return "https://tnrd.gov.in/project/webservices_forms/login_service/login_services.php";
    }

    public static String getServicesListUrl() {
        return "https://tnrd.gov.in/project/webservices_forms/master_services/master_services.php";
    }

    public static String getInspectionServicesListUrl() {
        return "https://tnrd.gov.in/project/webservices_forms/inspection/inspection_services.php";
    }

    public static String getTnrdHostName() {
        return "tnrd.gov.in";
    }
}
