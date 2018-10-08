package br.com.trustsystems.gravity.security.realm.auth;

import org.apache.shiro.authc.AuthenticationToken;

public class IdPassToken implements AuthenticationToken {

    /*--------------------------------------------
    |             C O N S T A N T S             |
    ============================================*/

    /*--------------------------------------------
    |    I N S T A N C E   V A R I A B L E S    |
    ============================================*/

    /**
     * The identification construct
     */
    private IdConstruct idConstruct;

    /**
     * The password, in char[] format
     */
    private char[] password;


    public IdPassToken(String partition, String username, String clientId, char[] password){
        this.idConstruct = new IdConstruct(partition, username, clientId);
        this.password = password;
    }

    public IdConstruct getIdConstruct() {
        return idConstruct;
    }

    public void setIdConstruct(IdConstruct idConstruct) {
        this.idConstruct = idConstruct;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    /**
     * Returns the account identity submitted during the authentication process.
     * <p>
     * <p>Most application authentications are username/password based and have this
     * object represent a username.  If this is the case for your application,
     * take a look at the {@link org.apache.shiro.authc.UsernamePasswordToken UsernamePasswordToken}, as it is probably
     * sufficient for your use.
     * <p>
     * <p>Ultimately, the object returned is application specific and can represent
     * any account identity (user id, X.509 certificate, etc).
     *
     * @return the account identity submitted during the authentication process.
     * @see org.apache.shiro.authc.UsernamePasswordToken
     */
    @Override
    public Object getPrincipal() {
        return getIdConstruct();
    }

    /**
     * Returns the credentials submitted by the user during the authentication process that verifies
     * the submitted {@link #getPrincipal() account identity}.
     * <p>
     * <p>Most application authentications are username/password based and have this object
     * represent a submitted password.  If this is the case for your application,
     * take a look at the {@link org.apache.shiro.authc.UsernamePasswordToken UsernamePasswordToken}, as it is probably
     * sufficient for your use.
     * <p>
     * <p>Ultimately, the credentials Object returned is application specific and can represent
     * any credential mechanism.
     *
     * @return the credential submitted by the user during the authentication process.
     */
    @Override
    public Object getCredentials() {
        return getPassword();
    }
}
