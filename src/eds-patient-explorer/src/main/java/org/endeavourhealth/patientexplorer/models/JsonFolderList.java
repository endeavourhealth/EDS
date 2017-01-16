package org.endeavourhealth.patientexplorer.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonFolderList {

    private List<JsonFolder> folders = new ArrayList<>();

    public JsonFolderList() {
    }

    public void add(JsonFolder jsonFolder) {
        folders.add(jsonFolder);

        //find the next non-null index
        /*for (int i=0; i<folders.length; i++)
        {
            if (folders[i] == null)
            {
                folders[i] = jsonFolder;
                return;
            }
        }

        throw new RuntimeException("Trying to add too many organisations to JsonOrganisationList");*/
    }
    /*public void add(DbFolder folder, int count)
    {
        JsonFolder jsonFolder = new JsonFolder(folder, count);
        add(jsonFolder);
    }*/


    /**
     * gets/sets
     */
    public List<JsonFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<JsonFolder> folders) {
        this.folders = folders;
    }
}
