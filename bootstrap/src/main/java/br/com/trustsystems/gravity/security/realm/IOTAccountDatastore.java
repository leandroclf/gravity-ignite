package br.com.trustsystems.gravity.security.realm;

import br.com.trustsystems.gravity.security.realm.state.IOTAccount;
import br.com.trustsystems.gravity.security.realm.state.IOTRole;

public interface IOTAccountDatastore {


    IOTAccount getIOTAccount(String partition, String username);

    void saveIOTAccount(IOTAccount account);

    IOTRole getIOTRole(String partition, String rolename);


    void saveIOTRole(IOTRole iotRole);

}
