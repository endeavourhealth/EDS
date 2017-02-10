package org.endeavourhealth.patientexplorer.database.models;

import javax.persistence.*;

@Entity
@Table(name = "trm_concept", schema = "\"public\"", catalog = "coding")
public class ConceptEntity {
    private long pid;
    private String code;
    private long codesystem_pid;
    private String display;
    private long index_status;

    @Id
    @Column(name = "pid")
    public long getPid() {
        return pid;
    }
    public void setPid(long pid) {
        this.pid = pid;
    }

    @Basic
    @Column(name = "code")
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    @Basic
    @Column(name = "codesystem_pid")
    public long getCodesystem_pid() {
        return codesystem_pid;
    }
    public void setCodesystem_pid(long codesystem_pid) {
        this.codesystem_pid = codesystem_pid;
    }

    @Basic
    @Column(name = "display")
    public String getDisplay() {
        return display;
    }
    public void setDisplay(String display) {
        this.display = display;
    }

    @Basic
    @Column(name = "index_status")
    public long getIndex_status() {
        return index_status;
    }
    public void setIndex_status(long index_status) {
        this.index_status = index_status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptEntity that = (ConceptEntity) o;

        if (pid != that.pid) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (codesystem_pid != that.codesystem_pid) return false;
        if (display != null ? !display.equals(that.display) : that.display != null) return false;
        if (index_status != that.index_status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (pid ^ (pid >>> 32));
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = (int) (31 * result + (codesystem_pid ^ (codesystem_pid >>> 32)));
        result = 31 * result + (display != null ? display.hashCode() : 0);
        result = (int) (31 * result + (index_status ^ (index_status >>> 32)));
        return result;
    }
}
