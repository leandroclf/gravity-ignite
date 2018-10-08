package br.com.trustsystems.gravity.security;

import br.com.trustsystems.gravity.security.realm.IOTAccountDatastore;
import br.com.trustsystems.gravity.security.realm.auth.permission.IOTPermissionResolver;
import br.com.trustsystems.gravity.security.realm.impl.IOTIniBasedRealm;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.realm.Realm;

public class IOTIniSecurityManagerFactory extends IniSecurityManagerFactory {

    public static final String INI_REALM_NAME = "gravityIniRealm";

    private IOTAccountDatastore iotAccountDatastore;

    /**
     * Creates a new instance.  See the {@link #getInstance()} JavaDoc for detailed explanation of how an INI
     * source will be resolved to use to build the instance.
     */

    public IOTIniSecurityManagerFactory(Ini config, IOTAccountDatastore iotAccountDatastore) {
        super(config);

        setIotAccountDatastore(iotAccountDatastore);
    }


    public IOTAccountDatastore getIotAccountDatastore() {
        return iotAccountDatastore;
    }

    public void setIotAccountDatastore(IOTAccountDatastore iotAccountDatastore) {
        this.iotAccountDatastore = iotAccountDatastore;
    }

    @Override
    protected org.apache.shiro.mgt.SecurityManager createDefaultInstance() {
        return new IOTSecurityManager();
    }

    @Override
    protected Realm createRealm(Ini ini) {
        IOTIniBasedRealm iniBasedRealm = new IOTIniBasedRealm();
        iniBasedRealm.setName(INI_REALM_NAME);
        iniBasedRealm.setIotAccountDatastore(getIotAccountDatastore());
        iniBasedRealm.setIni(ini);
        iniBasedRealm.setPermissionResolver(new IOTPermissionResolver());
        return iniBasedRealm;
    }
}
