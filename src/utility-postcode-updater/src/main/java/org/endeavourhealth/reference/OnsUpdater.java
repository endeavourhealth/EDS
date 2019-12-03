package org.endeavourhealth.reference;

import org.endeavourhealth.reference.helpers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Reader;
import java.util.zip.ZipInputStream;

public class OnsUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(OnsUpdater.class);

    /*
    * 1. Download the latest "NHS Postcode Directory UK Full" dataset from the ONS:
    * https://ons.maps.arcgis.com/home/search.html?q=NHS%20Postcode%20Directory%20UK%20Full&start=1&sortOrder=desc&sortField=modified#content
    * 2. Run this app with parameters: ons_all <path to zip file>
    *
    * Nov 2019 data at: https://ons.maps.arcgis.com/home/item.html?id=d7b33b66949b4bc9b9065de7544ae4d1
    * Aug 2019 data at: https://ons.maps.arcgis.com/home/item.html?id=054714ceec2743c0b59884f5619f4efa
    * May 2019 data at: https://ons.maps.arcgis.com/home/item.html?id=fd3ffbae5dfe4b9db21070486c6a0d64
    * Feb 2019 data at: https://ons.maps.arcgis.com/home/item.html?id=e1dc68a2c7f64adeb834bd089bd87ca5
    * Nov 2018 data at: https://ons.maps.arcgis.com/home/item.html?id=3506c198565a444d9432d31f85257ade
    * Aug 2018 data at: https://ons.maps.arcgis.com/home/item.html?id=1ad8f296756447bf87b011ec445391fc
    * May 2018 data at: https://ons.maps.arcgis.com/home/item.html?id=726532de7e62432dbc0d443c22ad810f
    * Aug 2016 data at: http://ons.maps.arcgis.com/home/item.html?id=dc23a64fa2e34e1289901b27d91c335b
    */
    public static void updateOns(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: ons_all <path of zip file>");
            return;
        }
        String zipFileName = args[1];

        LOG.info("ONS Update Starting");

        File file = new File(zipFileName);
        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist");
            return;
        }
        if (!ZipHelper.isZip(file)) {
            LOG.error("" + file + " isn't a zip file");
            return;
        }

        ZipInputStream zis = null;
        Reader r = null;

        LOG.info("Looking for LSOA file...");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "Documents/Names and Codes/LSOA.*UK.*csv");
        if (r == null) {
            LOG.error("Failed to find LSOA file");
            return;
        }
        OnsLsoaHelper.processFile(r);
        zis.close();

        LOG.info("Looking for MSOA file...");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "Documents/Names and Codes/MSOA.*UK.*csv");
        if (r == null) {
            LOG.error("Failed to find MSOA file");
            return;
        }
        OnsMsoaHelper.processFile(r);
        zis.close();

        LOG.info("Looking for CCG file...");
        zis = ZipHelper.createZipInputStream(file);
        //as of Apr 2019, the UK CCG file was replaced with just an English one
        //r = ZipHelper.findFile(zis, "Documents/Names and Codes/CCG.*UK.*csv");
        r = ZipHelper.findFile(zis, "Documents/Names and Codes/CCG.*EN.*csv");
        if (r == null) {
            LOG.error("Failed to find CCG file");
            return;
        }
        OnsCcgHelper.processFile(r);
        zis.close();

        LOG.info("Looking for Ward file...");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "Documents/Names and Codes/Ward.*UK.*(?<!98).csv$");
        if (r == null) {
            LOG.error("Failed to find Ward file");
            return;
        }
        OnsWardHelper.processFile(r);
        zis.close();

        LOG.info("Looking for Local Authority file...");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "Documents/Names and Codes/LA_UA.*UK.*csv");
        if (r == null) {
            LOG.error("Failed to find Local Authority file");
            return;
        }
        OnsLocalAuthorityHelper.processFile(r);
        zis.close();

        LOG.info("Looking for Postcode file...");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "Data/nhg.*csv");
        if (r == null) {
            LOG.error("Failed to find Postcode file");
            return;
        }
        OnsPostcodeHelper.processFile(r);
        zis.close();


        LOG.info("Finished ONS Import");
    }
}
