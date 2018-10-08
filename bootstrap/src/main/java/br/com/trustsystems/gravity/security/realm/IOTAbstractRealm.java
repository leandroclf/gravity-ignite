package br.com.trustsystems.gravity.security.realm;

import br.com.trustsystems.gravity.security.realm.auth.IdConstruct;
import br.com.trustsystems.gravity.security.realm.auth.IdPassToken;
import br.com.trustsystems.gravity.security.realm.state.IOTAccount;
import br.com.trustsystems.gravity.security.realm.state.IOTRole;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public abstract class IOTAbstractRealm extends AuthorizingRealm{

    private IOTAccountDatastore iotAccountDatastore;


    public IOTAbstractRealm(){

        //IOTAbstractRealm is in memory data grid reloaded
        // - no need for an additional cache mechanism since we're
        //already as memory-efficient as one can be:
        setCachingEnabled(false);

    }


    public IOTAccountDatastore getIotAccountDatastore() {
        return iotAccountDatastore;
    }

    public void setIotAccountDatastore(IOTAccountDatastore iotAccountDatastore) {
        this.iotAccountDatastore = iotAccountDatastore;
    }

    /**
     * Retrieves the AuthorizationInfo for the given principals from the underlying data store.  When returning
     * an instance from this method, you might want to consider using an instance of
     * {@link SimpleAuthorizationInfo SimpleAuthorizationInfo}, as it is suitable in most cases.
     *
     * @param principals the primary identifying principals of the AuthorizationInfo that should be retrieved.
     * @return the AuthorizationInfo associated with this principals.
     * @see SimpleAuthorizationInfo
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        IdConstruct idConstruct = (IdConstruct) getAvailablePrincipal(principals);

        return getIOTAccount(idConstruct.getPartition(), idConstruct.getUsername());
    }



    /**
     * Retrieves authentication data from an implementation-specific datasource (RDBMS, LDAP, etc) for the given
     * authentication token.
     * <p>
     * For most datasources, this means just 'pulling' authentication data for an associated subject/user and nothing
     * more and letting Shiro do the rest.  But in some systems, this method could actually perform EIS specific
     * log-in logic in addition to just retrieving data - it is up to the Realm implementation.
     * <p>
     * A {@code null} return value means that no account could be associated with the specified token.
     *
     * @param token the authentication token containing the user's principal and credentials.
     * @return an {@link AuthenticationInfo} object containing account data resulting from the
     * authentication ONLY if the lookup is successful (i.e. account exists and is valid, etc.)
     * @throws AuthenticationException if there is an error acquiring data or performing
     *                                 realm-specific authentication logic for the specified <tt>token</tt>
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        IdConstruct idConstruct = ((IdPassToken) token).getIdConstruct();
        IOTAccount account = getIOTAccount(idConstruct.getPartition(), idConstruct.getUsername());

        if (account != null) {

            if (account.isLocked()) {
                throw new LockedAccountException("Account [" + account + "] is locked.");
            }
            if (account.isCredentialsExpired()) {
                String msg = "The credentials for account [" + account + "] are expired";
                throw new ExpiredCredentialsException(msg);
            }

        }

        return account;

    }

    public IOTAccount getIOTAccount(String partition, String username){

        IOTAccount account= getIotAccountDatastore().getIOTAccount(partition, username);

        if(null != account)
            account.setIotAccountDatastore(getIotAccountDatastore());

        return account;
    }

    public IOTAccount addIOTAccount(String partition, String username, String password) {

        IdConstruct idConstruct = new IdConstruct(partition, username, null);
        IOTAccount account = new IOTAccount(idConstruct, password, getName());
        saveIOTAccount(account);
        return getIOTAccount(partition, username);
    }

    protected void saveIOTAccount(IOTAccount iotAccount){
        getIotAccountDatastore().saveIOTAccount(iotAccount);
    }

    protected IOTRole getIOTRole(String partition, String rolename) {
        return getIotAccountDatastore().getIOTRole(partition, rolename);
    }

    public boolean roleExists(String partition, String name) {
        return getIotAccountDatastore().getIOTRole(partition, name) != null;
    }

    public IOTRole addIOTRole(String partition, String rolename ) {
        saveIOTRole( new IOTRole(partition, rolename));
        return getIotAccountDatastore().getIOTRole(partition, rolename);

    }

    public void saveIOTRole(IOTRole iotRole) {
        getIotAccountDatastore().saveIOTRole(iotRole);

    }


    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && IdPassToken.class.isAssignableFrom(token.getClass());
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        super.onLogout(principals);
    }
}
