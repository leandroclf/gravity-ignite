package br.com.trustsystems.gravity.security.realm.auth.permission;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;

public class IOTPermissionResolver implements PermissionResolver {

    /**
     * Resolves a Permission based on the given String representation.
     *
     * @param permissionString the String representation of a permission.
     * @return A Permission object that can be used internally to determine a subject's security.
     * @throws org.apache.shiro.authz.permission.InvalidPermissionStringException if the permission string is not valid for this resolver.
     */
    @Override
    public Permission resolvePermission(String permissionString) {
        return new IOTPermission(permissionString);
    }
}
