package br.com.trustsystems.gravity.security.realm.state;

import br.com.trustsystems.gravity.data.IdKeyComposer;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.shiro.authz.Permission;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class IOTRole implements IdKeyComposer, Serializable {

    protected String partition = "";
    protected String name = null;
    protected Set<Permission> permissions;

    public IOTRole() {
    }

    public IOTRole(String partition, String name) {
        setPartition(partition);
        setName(name);
    }

    public IOTRole(String partition, String name, Set<Permission> permissions) {
        setPartition(partition);
        setName(name);
        setPermissions(permissions);
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public void add(Permission permission) {
        Set<Permission> permissions = getPermissions();
        if (permissions == null) {
            permissions = new LinkedHashSet<>();
            setPermissions(permissions);
        }
        permissions.add(permission);
    }

    public void addAll(Collection<Permission> perms) {
        if (perms != null && !perms.isEmpty()) {
            Set<Permission> permissions = getPermissions();
            if (permissions == null) {
                permissions = new LinkedHashSet<>(perms.size());
                setPermissions(permissions);
            }
            permissions.addAll(perms);
        }
    }

    public boolean isPermitted(Permission p) {
        Collection<Permission> perms = getPermissions();
        if (perms != null && !perms.isEmpty()) {
            for (Permission perm : perms) {
                if (perm.implies(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        return (getName() != null ? getName().hashCode() : 0);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof IOTRole) {
            IOTRole ir = (IOTRole) o;
            //only check name, since role names should be unique across an entire application:
            return (getName() != null ? getName().equals(ir.getName()) : ir.getName() == null);
        }
        return false;
    }

    @Override
    public Serializable generateIdKey() throws UnRetriableException {

        if(null == getName() ){
            throw new UnRetriableException(" Can't save a role without a name");
        }

        return createCacheKey(getPartition(), getName());

    }

    public static String createCacheKey(String partition, String rolename){
        return String.format("%s-%s",partition, rolename);
    }
}
