package org.endeavourhealth.ui.lookups;

import org.endeavourhealth.ui.database.DatabaseManager;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisation;

import java.util.*;

public abstract class SourceOrganisationUpdater {

    public static void updateSourceOrganisations(List<SourceOrganisation> sourceOrganisations) throws Exception {

        HashMap<String, SourceOrganisation> hm = new HashMap<>();
        for (SourceOrganisation org: sourceOrganisations) {
            String odsCode = org.getOdsCode();
            if (hm.containsKey(odsCode)) {
                throw new RuntimeException("Duplicate ODS code " + odsCode);
            }
            hm.put(odsCode, org);
        }

        //update records already on the DB
        List<DbSourceOrganisation> dbSourcesOrgs = DbSourceOrganisation.retrieveAll(true);
        List<DbSourceOrganisation> toSave = new ArrayList<>();

        for (DbSourceOrganisation dbSourceOrg: dbSourcesOrgs) {
            String odsCode = dbSourceOrg.getOdsCode();
            SourceOrganisation sourceOrganisation = hm.remove(odsCode);

            if (applyNewData(dbSourceOrg, sourceOrganisation)) {
                toSave.add(dbSourceOrg);
            }
        }

        //create new records
        Iterator it = hm.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SourceOrganisation> entry = (Map.Entry)it.next();
            String odsCode = entry.getKey();
            SourceOrganisation sourceOrganisation = entry.getValue();

            DbSourceOrganisation dbSourceOrg = new DbSourceOrganisation();
            dbSourceOrg.setOdsCode(odsCode);
            applyNewData(dbSourceOrg, sourceOrganisation);
            toSave.add(dbSourceOrg);
        }

        DatabaseManager.db().writeEntities(toSave);
    }

    private static boolean applyNewData(DbSourceOrganisation db, SourceOrganisation sourceOrganisation) {
        boolean changed = false;

        if (sourceOrganisation == null) {

            if (db.isReferencedByData()) {
                db.setReferencedByData(false);
                changed = true;
            }

        } else {

            if (!db.isReferencedByData()) {
                db.setReferencedByData(true);
                changed = true;
            }

            if (db.getName() == null || !db.getName().equals(sourceOrganisation.getName())) {
                db.setName(sourceOrganisation.getName());
                changed = true;
            }
        }

        return changed;
    }
}
