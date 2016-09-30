package org.endeavourhealth.ui.database.models;

import javax.persistence.*;

@Entity
@Table(name = "trm_concept", schema = "\"public\"", catalog = "logback")
public class ConceptEntity {
    private int pid;
    private String code;
    private int codesystem_pid;
    private String display;
    private int index_status;

    @Id
    @Column(name = "pid")
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
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
    public int getCodesystem_pid() {
        return codesystem_pid;
    }
    public void setCodesystem_pid(int codesystem_pid) {
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
    public int getIndex_status() {
        return index_status;
    }
    public void setIndex_status(int index_status) {
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
        result = 31 * result + (codesystem_pid ^ (codesystem_pid >>> 32));
        result = 31 * result + (display != null ? display.hashCode() : 0);
        result = 31 * result + (index_status ^ (index_status >>> 32));
        return result;
    }
}
